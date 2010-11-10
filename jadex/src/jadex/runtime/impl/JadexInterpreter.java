package jadex.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import jadex.adapter.IAgentAdapter;
import jadex.adapter.IJadexAgent;
import jadex.adapter.IMessageAdapter;
import jadex.model.IMBDIAgent;
import jadex.model.IMCapability;
import jadex.model.ISystemEventTypes;
import jadex.runtime.AgentDeathException;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.IAgenda;
import jadex.runtime.impl.agenda.IAgendaAction;
import jadex.runtime.impl.agenda.IAgendaEntry;
import jadex.runtime.impl.agenda.agent.InvokeLaterAction;
import jadex.runtime.impl.agenda.agent.MessageArrivalAction;
import jadex.runtime.impl.agenda.agent.NotifyDueAction;
import jadex.runtime.impl.agenda.agent.StartAgentAction;
import jadex.runtime.impl.agenda.agent.TerminateAgentAction;
import jadex.runtime.impl.agenda.treeimpl.TreeAgenda;
import jadex.runtime.impl.agenda.treeimpl.TreeAgendaEntry;
import jadex.util.collection.SCollection;

/**
 *  Main entry point for the reasoning engine
 *  holding the relevant agent data structure
 *  and performing the agent execution when
 *  being called from the platform.
 */
public class JadexInterpreter implements IJadexAgent, IEncodable, Serializable
{
	//-------- constants --------

	/** The execution mode normal. */
	public static final String EXECUTION_MODE_NORMAL = "normal";

	/** The execution mode step. */
	public static final String EXECUTION_MODE_STEP = "step";

	/** The execution mode step. */
	public static final String EXECUTION_MODE_CYCLE = "cycle";

	/** The execution mode step. */
	public static final String EXECUTION_MODE_SLOW = "slow";

	/** The debugging flag. */
	public static final String DEBUGGING = "debugging";

	//-------- attributes --------
	
	/** The agent .*/
	protected RBDIAgent	agent;
	
	/** The agent adpater on the platform.*/
	protected IAgentAdapter	adapter;
	
	/** The agenda. */
	protected IAgenda	agenda;

	/** The thread executing the agent (null for none). */
	// Todo: need not be transient, because agent should only be serialized when no action is running?
	protected transient Thread	agentthread;

	//-------- tool settings --------
	
	/** The execution mode. */
	protected String mode;

	/** The allowed steps counter. */
	protected int stepcnt;

	//-------- constructors --------
	
	/**
	 *  Create an agent interpreter for the given agent.
	 *  @param adapter	The platform adapter for the agent. 
	 *  @param agent	The agent model (i.e. the loaded XML file).
	 *  @param config	The name of the configuration (or null for default configuration) 
	 *  @param arguments	The arguments for the agent as name/value pairs.
	 */
	public JadexInterpreter(IAgentAdapter adapter, IMBDIAgent agent, String config, Map arguments)
	{
		this.agent	= new RBDIAgent(adapter, agent, config, arguments, this);
		this.agenda = new TreeAgenda(this.agent);

		Boolean debug = (Boolean)this.agent.getPropertybase().getProperty(DEBUGGING);
		if(debug!=null && debug.booleanValue())
		{
			this.mode	= EXECUTION_MODE_CYCLE;
			this.stepcnt	= 0;
		}
		else
		{
			this.mode	= EXECUTION_MODE_NORMAL;
			this.stepcnt	= -1;
		}

		// Initialize the agent.
		// todo: no precondition, is that right
		agenda.add(new StartAgentAction(this.agent, null), null);
	}
	
	//-------- IJadexAgent interface --------
	
	// used for asserts in executeAction and plan executors only.
	public boolean agent_executing;
	public boolean plan_executing1;
	public boolean plan_executing2;
	
	/**
	 *  Main method to perform agent execution.
	 *  Whenever this method is called, the agent performs
	 *  one of its scheduled actions.
	 *  The platform can provide different execution models for agents
	 *  (e.g. thread based, or synchroneous).
	 *  To avoid idle waiting, the return value can be checked. 
	 *  @return true, when there are more actions waiting to be executed. 
	 */
	public synchronized boolean executeAction()
//	public boolean executeAction()
	{
//		boolean haslock = false;
//		try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//		catch(InterruptedException e){e.printStackTrace();}
//		if(!haslock)
//			throw new RuntimeException("Could not get lock: "
//				+Thread.currentThread()+" "+agent.lock+" "+getCurrentPlan().getName());

			// This method does not check if a next action is available but only
			// this one was executed. Calling executeAction() one more time than
			// necessary is more efficient than checking each time if the agenda is empty.
		
			assert !agent_executing: this;
			assert !plan_executing1: this;
			assert !plan_executing2: this;
			this.agent_executing	= true;
			
			boolean	executed	= false;
			if(stepcnt!=0)	// stepcnt==-1 indicates normal execution, stepcnt>0 is stepped execution, setpcnt==0 is stop
			{
				this.agentthread	= Thread.currentThread();
				IAgendaEntry	entry	= agenda.executeAction();
				if(entry!=null)
				{
					stepDone(entry);
					executed	= true;
				}
				this.agentthread	= null;
			}

			assert agent_executing: this;
			assert !plan_executing1: this;
			assert !plan_executing2: this;
			this.agent_executing	= false;

//		lock.unlock();

		return executed && !RBDIAgent.LIFECYCLESTATE_TERMINATED.equals(agent.getLifecycleState());
	}
	
	/**
	 *  Callback when the next time point has been reached.
	 */
	public void	notifyDue()
	{
		if(agent.isCleanedup())
			return;	// Hack!!!

		assert !agent.getLifecycleState().equals(RBDIAgent.LIFECYCLESTATE_TERMINATED) : agent;
		assert !agent.isCleanedup(): agent;

		this.agenda.addExternal(new NotifyDueAction(agent, null));
	}

	/**
	 *  Inform the agent that a message has arrived.
	 *  (Can be called from external thread as it adds an external
	 *  agenda entry for message arrival)
	 */
	public void  messageArrived(IMessageAdapter message)
	{
		this.agenda.addExternal(new MessageArrivalAction(agent, null, message));
	}

	/**
	 *  Request agent to kill itself.
	 */
	public void killAgent()
	{
		// When using plan or agent thread, add directly to agenda.
		if(isAgentThread() || isPlanThread())
		{
			this.agenda.add(new TerminateAgentAction(agent), null);
		}
		// For external threads use separate thread safe queue.
		else
		{
			this.agenda.addExternal(new TerminateAgentAction(agent));
		}
	}

	//-------- optional accessor methods --------

	//todo: remove as much as possible from these methods
	
	/**
	 *  Get the logger for the agent.
	 *  @return The logger object.
	 */
	public Logger getLogger()
	{
		return agent.getLogger();
	}

	/**
	 *  Get a property of the agent as defined in the ADF,
	 *  or any enclosed capabilities.
	 */
	public synchronized Object getProperty(String name)
//	public Object getProperty(String name)
	{
//		boolean haslock = false;
//		try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//		catch(InterruptedException e){e.printStackTrace();}
//		if(!haslock)
//			throw new RuntimeException("Could not get lock: "
//				+Thread.currentThread()+" "+agent.lock+" "+getCurrentPlan().getName());

			assert !agent.isCleanedup(): agent;
	
			Object	val	= agent.getPropertybase().getProperty(name);
			if(val==null)
			{
				List	cs	= SCollection.createArrayList();
				cs.add(agent.getChildren());
	
				// Breadth-search by traversing list of child arrays
				// ( [children1], [children2], ...)
				for(int i=0; val==null && i<cs.size(); i++)
				{
					RCapability[] children = (RCapability[])cs.get(i);
					for(int j=0; val==null && j<children.length; j++)
					{
						val = children[j].getPropertybase().getProperty(name);
						if(val==null)
						{
							cs.add(children[j].getChildren());
						}
					}
				}
			}

//		lock.unlock();

		return val;
	}

	/**
	 *  Get property names matching the specified start string.
	 *  @return Matching property names of the agent as defined in the ADF,
	 *  or any enclosed capabilities.
	 */
	public synchronized String[] getPropertyNames(String name)
//	public String[] getPropertyNames(String name)
	{
//		boolean haslock = false;
//		try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//		catch(InterruptedException e){e.printStackTrace();}
//		if(!haslock)
//			throw new RuntimeException("Could not get lock: "
//				+Thread.currentThread()+" "+agent.lock+" "+getCurrentPlan().getName());

			assert !agent.isCleanedup(): this;
	
			List	ret	= SCollection.createArrayList();
			String[]	names	= agent.getPropertybase().getPropertyNames(name);
			for(int i=0; i<names.length; i++)
				ret.add(names[i]);
	
			// Breadth-search by traversing list of child arrays
			// ( [children1], [children2], ...)
			List	cs	= SCollection.createArrayList();
			cs.add(agent.getChildren());
			for(int i=0; i<cs.size(); i++)
			{
				RCapability[] children = (RCapability[])cs.get(i);
				for(int j=0; j<children.length; j++)
				{
					names	= children[j].getPropertybase().getPropertyNames(name);
					for(int k=0; k<names.length; k++)
						if(!ret.contains(names[k]))
							ret.add(names[k]);
	
					cs.add(children[j].getChildren());
				}
			}

//		lock.unlock();

		return (String[])ret.toArray(new String[ret.size()]);
	}

	/**
	 *  WARNING. Does only work when MCapabilities
	 *  are loaded as prototypes! (hack?)
	 *  Get the capability for the capability model.
	 *  @param mcap The model capability.
	 *  @return The corresponding runtime capability.
	 */
	public RCapability lookupCapability(IMCapability mcap)
	{
		return agent.lookupCapability(mcap);
	}
	
	//-------- impl methods --------
	
	/**
	 *  Get the current agent thread.
	 *  @return The thread executing the agent currently (otherwise null).
	 */
	protected Thread getAgentThread()
	{
		return this.agentthread;
	}

	/**
	 *  Invoke some code with agent behaviour synchronized on the agent.
	 *  @param code The code to execute.
	 *  Note: 1.4 compliant code.
	 *  Problem: Deadlocks cannot be detected and no exception is thrown.
	 */
	public synchronized void invokeSynchronized(Runnable code)
//	public void invokeSynchronized(Runnable code)
	{
//		boolean haslock = false;
//		try{haslock = lock.tryLock(LOCKTIME, java.util.concurrent.TimeUnit.MILLISECONDS);}
//		catch(InterruptedException e){e.printStackTrace();}
//		if(!haslock)
//			throw new RuntimeException("Could not get lock: "
//				+Thread.currentThread()+" "+agent.lock+" "+getCurrentPlan().getName());

			// todo: refactor callers to aquire agent lock directly?!
			if(RBDIAgent.LIFECYCLESTATE_TERMINATED.equals(agent.getLifecycleState()) || agent.isCleanedup())
				throw new AgentDeathException(agent);
			code.run();

//		lock.unlock();
	}

	/**
	 *  Add some code to the agande, that will
	 *  be executed on the agent's thread.
	 *  This method can be safely called from any (agent or non-agent) thread.
	 */
	public void invokeLater(Runnable code)
	{
		agenda.addExternal(new InvokeLaterAction(agent, null, code));
	}

	/**
	 *  Check if the agent thread is accessing.
	 *  @return True, if access is ok.
	 */ 
	public boolean isAgentThread()
	{
		return agentthread==Thread.currentThread();
	}
	
	/**
	 *  Check if the agent thread is accessing.
	 *  @return True, if access is ok.
	 */ 
	public boolean isPlanThread()
	{
		RPlan	rp	= agent.getCurrentPlan();
		Object pt = rp!=null ? rp.getThread() : null;
		return Thread.currentThread()==pt;
	}
	
	/**
	 *  Check if the external thread is accessing.
	 *  @return True, if access is ok.
	 */ 
	public boolean isExternalThread()
	{
		return !isAgentThread() && !isPlanThread();
	}

	//-------- agenda methods --------
	
	/**
	 *  Get the agenda.
	 *  @return The agenda.
	 * /
	public IAgenda	getAgenda()
	{
		return this.agenda;
	}*/
	
	protected SystemEvent	event_agenda_changed	= new SystemEvent(ISystemEventTypes.AGENDA_CHANGED, this);
	/**
	 *  Add an agenda entry.
	 *  @param action The agenda action.
	 *  @param cause The action cause (todo: should be always system event).
	 */
	public void addAgendaEntry(IAgendaAction action, Object cause)
	{
		agenda.add(action, cause);
		agent.throwSystemEvent(event_agenda_changed);
	}
	
	/**
	 *  Get the current agenda entry.
	 *  @return The current agenda entry.
	 */
	public IAgendaEntry getCurrentAgendaEntry()
	{
		return agenda.getCurrentEntry();
	}
	
	/**
	 *  Get the agenda state. Changes whenever the
	 *  agenda changes. Can be used to determine changes.
	 *  @return The actual state.
	 */
	public int getAgendaState()
	{
		return agenda.getState();
	}

	//-------- execution control --------

	/**
	 *  Set the execution mode.
	 *  @param mode The execution mode.
	 */
	public void setExecutionMode(String mode)
	{
		//System.out.println("Setting dispatcher mode to: "+mode);
		if(!(mode.equals(this.mode)))
		{
			this.mode = mode;

			if(mode.equals(EXECUTION_MODE_NORMAL))
			{
				this.stepcnt = -1;
				agent.getAgentAdapter().wakeup();
			}
			else
			{
				this.stepcnt = 0;
			}
			// info event
			agent.throwSystemEvent(new SystemEvent(ISystemEventTypes.AGENDA_MODE_CHANGED, this));
		}
	}

	/**
	 *  String the execution mode.
	 */
	public String	getExecutionMode()
	{
		return mode;
	}

	/**
	 *  Set the number of allowed steps.
	 *  @param steps The number of steps.
	 */
	public void setSteps(int steps)
	{
		if(!mode.equals(EXECUTION_MODE_NORMAL))
		{
			this.stepcnt = steps;
			// info event
			agent.throwSystemEvent(new SystemEvent(ISystemEventTypes.AGENDA_STEPS_CHANGED, this));
			if(stepcnt>0)
				agent.getAgentAdapter().wakeup();
		}
	}

	/**
	 *  A step was executed.
	 */
	protected void stepDone(IAgendaEntry ae)
	{
		//if(agent.getName().indexOf("Sok")!=-1)
		//	System.out.println("Step done: "+ae);
		if(!mode.equals(EXECUTION_MODE_NORMAL))
		{
			if(this.stepcnt > 0)
			{
				this.stepcnt--;
			}
		}
		// info event
		SystemEvent	event_step_done	= new SystemEvent(ISystemEventTypes.AGENDA_STEP_DONE, this);
		event_step_done.setValue(ae);
		agent.throwSystemEvent(event_step_done);
	}
	
	/**
	 *  Get the state of this capability and enclosed subcapabilities
	 *  encoded in a set of corresponding change events.
	 *  @param types	The system event types, the caller is interested in.
	 */
	public List	getState(String[] types)
	{
  		List ret = SCollection.createArrayList();

  		if(ISystemEventTypes.Subtypes.isSubtype(ISystemEventTypes.EVENT_TYPE_STEPPABLE, types))
  			ret.add(new SystemEvent(ISystemEventTypes.EVENT_TYPE_STEPPABLE, this));

		return ret;
	}
	
	/**
	 *  Create encodable representation of this element.
	 */
	public Map getEncodableRepresentation()
	{
		HashMap rep	= SCollection.createHashMap();
		rep.put("isencodeablepresentation", "true"); // to distinguish this map from normal maps.

		// Add info about mode and steps.
		rep.put("mode", mode);
		rep.put("steps", ""+stepcnt);
		// rep.put("entries", agenda.getEncodableRepresentation());
		
		// Add entries.
        List entries = agenda.getEntries();
		for(int i=0; i<entries.size(); i++)
			rep.put(""+i, ((TreeAgendaEntry)entries.get(i)).getEncodableRepresentation());
        rep.put("num", new Integer(entries.size()));

        //System.out.println("Agenda: "+num+" "+rep);
		return rep;
	}
}
