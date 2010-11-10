package jadex.runtime.impl;

import jadex.adapter.IAgentAdapter;
import jadex.model.IMBDIAgent;
import jadex.model.IMCapability;
import jadex.model.ISystemEventTypes;
import jadex.runtime.BasicAgentIdentifier;
import jadex.runtime.IFilter;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.agent.GarbageCollectionAction;
import jadex.util.SReflect;
import jadex.util.collection.IndexMap;
import jadex.util.collection.SCollection;
import jadex.util.collection.SortedList;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  The bdi agent runtime object.
 */
public class RBDIAgent extends RCapability
{
	//-------- constants --------
	
	/** The creating state. */
	public static final String	LIFECYCLESTATE_CREATING	= "creating";
	
	/** The alive state. */
	public static final String	LIFECYCLESTATE_ALIVE	= "alive";
	
	/** The terminating state (only cleanup-related activities are allowed). */
	public static final String	LIFECYCLESTATE_TERMINATING	= "terminating";

	/** The terminated state (no more actions are allowed). */
	public static final String	LIFECYCLESTATE_TERMINATED	= "terminated";

	//-------- attributes --------

	/** The platform adapter for the agent. */
	protected IAgentAdapter	adapter;
	
	/** The interpreter for the agent. */
	protected JadexInterpreter	interpreter;
	
	/** The time table. */
	protected List	timetable;

	/** The capability mappings. To get an rcap from an mcap. */
	protected IndexMap capamap;

  	/**  The currently executing plan */
  	protected RPlan currentplan;

	/** The internal lifecycle state of the agent (unrelated to licecycle states according to FIPA platform specification). */
	protected String	lifecyclestate;
		
	/** The arguments. */
	protected Map arguments;

	/** The flag indicating whether agenda changes are monitored. */
	protected int monitor_consequences;

	/** The agenda state when monitoring was started. */
	protected int agenda_state;

//	/** Lock for synchronization debugging with Java 1.5. */
//	java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
//	/** Time to wait for a lock. */
//	static long LOCKTIME	= 20000; 

	//-------- system event handling --------

	/** The flag indicating that a system event transaction is running. */
	protected boolean transaction_running;

	/** The flag indicating that several transaction should be performed as a whole. */
	protected boolean atomic;

	//-------- garbage collection --------

	/** The set of elements to be cleaned up. */
	protected Set	cleanables;
	
	/** Flag indicating that garbage collection has been scheduled. */
	protected boolean garbagecollection;
	
	//-------- constructors --------

	/**
	 *  Create a new bdi agent.
	 *  Initialization is as follows:
	 *  <ol>
	 * 		<li>The constructor of the agent (and recursively the capabilities)
	 * 			is called, and leads to the creation of all bases.
	 * 		<li>The agent constructor calls the first init() method,
	 * 			which evaluates the properties and creates all beliefs.
	 * 		<li>After the constructor call returns, and the execution starts,
	 * 			the start agent action calls the second init(), creating all the
	 * 			initial elements (e.g. goals and plans) and assigning values to the beliefs.
	 *  </ol> 
	 *  @param adapter	The platform adapter for the agent. 
	 *  @param agent	The agent model (i.e. the loaded XML file).
	 *  @param config	The name of the configuration (or null for default configuration) 
	 *  @param arguments	The arguments for the agent as name/value pairs.
	 *  @param interpreter	The interpreter for the agent. 
	 */
	public RBDIAgent(IAgentAdapter adapter, IMBDIAgent agent, String config, Map arguments, JadexInterpreter interpreter)
	{
		super(adapter.getAgentIdentifier().getLocalName(), agent, config, null, null);
		this.adapter	= adapter;
		this.arguments	= arguments;
		this.interpreter	= interpreter;
		this.lifecyclestate	= LIFECYCLESTATE_CREATING;
		this.timetable	= new SortedList(); // todo: use SCollection
		this.cleanables	= SCollection.createHashSet();
		
		// Check arguments.
		if(arguments!=null)
			agent.checkArguments(arguments);

		//setExpressionParameter("$agent", getAgent());

		//System.out.println("Ready: "+this.getName());
	}

	/**
	 *  Initialize this element.
	 *  Initialization is as follows:
	 *  <ol>
	 * 		<li>The constructor of the agent (and recursively the capabilities)
	 * 			is called, and leads to the creation of all bases.
	 * 		<li>The agent constructor calls the first init(0) method,
	 * 			which evaluates the properties and creates all beliefs.
	 * 		<li>After the constructor call returns, and the execution starts,
	 * 			the start agent action calls the second init(1), creating all the
	 * 			initial elements (e.g. goals and plans) and assigning values to the beliefs.
	 *  </ol>
	 */
	public void	init(int level)
	{
		super.init(level);
		//agenda.init(level);
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;
		
		// Todo: make sure that event gets thrown (i.e. not as agenda action!?)
		throwSystemEvent(new SystemEvent(SystemEvent.AGENT_DIED, this));
		
		super.cleanup();
	}

	//-------- methods --------

	/**
	 *  Get the lifecycle state of the agent.
	 */
	public String	getLifecycleState()
	{
		return this.lifecyclestate;
	}

	/**
	 *  Set the lifecyclestate of the agent.
	 */
	public void setLifecycleState(String lifecyclestate)
	{
		this.lifecyclestate	= lifecyclestate;
	}
		
	/**
	 *  Add a new timetable entry, that causes a event
	 *  (timeout or custom) to be dispatched when the given
	 *  time is reached.
	 *  @param entry The new timetable entry.
	 */
	public void addTimetableEntry(TimetableData entry)
	{
		Object first = null;
		if(timetable.size()>0)
			first	= timetable.get(0);

		timetable.add(entry);
		//System.out.println("timetable added: "+timetable.size()+" new: "+entry);
		// Restart because of empty queue or new first entry.
		if(first==null || first!=timetable.get(0))
			adapter.notifyIn(entry.getDuration());
	}

	/**
	 *  Remove a time table entry.
	 *  todo: use with id
	 *  @return True if could be removed.
	 */
	public boolean removeTimetableEntry(TimetableData entry)
	{
		// Inform the platform, if necessary (i.e. when the first entry is removed).
		if(timetable.size()>0 && timetable.get(0)==entry)
		{
			// More entries left.
			if(timetable.size()>1)
			{
				adapter.notifyIn(((TimetableData)timetable.get(0)).getDuration());
			}
			// Removing last entry.
			else
			{
				adapter.notifyIn(-1);				
			}
		}

		//System.out.println("timetable removing: "+timetable+" removed: "+entry);
		return timetable.remove(entry);

	}

	/**
	 *  Get the timetable.
	 *  @return The timetable.
	 */
	public List getTimetable()
	{
		return timetable;
	}

	/**
	 *  Get the state of this capability and enclosed subcapabilities
	 *  encoded in a set of corresponding change events.
	 *  @param types	The system event types, the caller is interested in.
	 */
	public List	getState(String[] types)
	{
  		List	ret = SCollection.createArrayList();

  		// Add agent birth.
  		if(ISystemEventTypes.Subtypes.isSubtype(SystemEvent.AGENT_BORN, types))
  			ret.add(new SystemEvent(SystemEvent.AGENT_BORN, this));

  		// Add interpreter state.
  		ret.addAll(interpreter.getState(types));

		// Add all events from capability method.
		ret.addAll(super.getState(types));

		return ret;
	}

	/**
	 *  Get the adapter agent.
	 *  @return The adapter agent.
	 */
	public IAgentAdapter	getAgentAdapter()
	{
		return this.adapter;
	}

	/**
	 *  Get the platform type.
	 */
	public String getPlatformType()
	{
		return getAgentAdapter().getPlatformType();
	}

	/**
	 *  Return an agent-identifier that allows to send
	 *  messages to this agent.
	 */
	public BasicAgentIdentifier getAgentIdentifier()
	{
		return getAgentAdapter().getAgentIdentifier();
	}

	//-------- capability methods --------
	
	/**
	 *  WARNING. Does only work when MCapabilities
	 *  are loaded as prototypes! (hack?)
	 *  Add a capability mapping.
	 *  @param mcap The model capability.
	 *  @param rcap The runtime capability.
	 */
	protected void addCapabilityMapping(IMCapability mcap, RCapability rcap)
	{
		//System.out.println(getAgentName()+" adding: "+mcap.getName()+":"+mcap.hashCode()+" "+rcap.getName()+":"+rcap.hashCode());
		// Hack! :-( Needed because capa is inited before agent
		if(this.capamap==null)
			this.capamap = SCollection.createIndexMap();
		this.capamap.add(mcap, rcap);
	}

	/**
	 *  WARNING. Does only work when MCapabilities
	 *  are loaded as prototypes! (hack?)
	 *  Remove a capability mapping.
	 *  @param rcap The runtime capability.
	 */
	protected void removeCapabilityMapping(RCapability rcap)
	{
		this.capamap.removeKey(rcap.getModelElement());
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
		return (RCapability)capamap.get(mcap);
	}

	/**
	 *  WARNING. Does only work when MCapabilities
	 *  are loaded as prototypes! (hack?)
	 *  Get all capabilities (including the agent itself).
	 *  @return All runtime capabilities.
	 */
	public List getAllCapabilities()
	{
		return capamap.getAsList();
	}

	/**
	 * @return the current executing plan or null if no plan or this is not the plan thread
	 */
	public RPlan getCurrentPlan()
	{
	  // System.out.println("Stub: RBDIAgent.getCurrentPlan");
	  RPlan plan = currentplan;
	  if(plan!=null && plan.getThread()==Thread.currentThread())
	  {
		  return plan;
	  }
	  return null;
	}

	/**
	 *  Sets the plan currently executed by this agent.
	 *  @param currentplan The current plan.
	 */
	protected void setCurrentPlan(RPlan currentplan)
	{
		this.currentplan = currentplan;
	}

	//-------- list methods ---------

	/**
	 *  Get the agenda.
	 *  @return The agenda.
	 * /
	public IAgenda	getAgenda()
	{
		return interpreter.getAgenda();
	}*/

	/**
	 *  Create a string representation of this element.
	 *  @return	This element represented as string.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(name);
//		sb.append("\nbeliefbase="+beliefbase);
//		sb.append("\ngoalbase="+goalbase);
//		sb.append("\nplanbase="+planbase);
		sb.append(")");
		return sb.toString();
	}

	//-------- methods --------

	/**
	 *  Apply a filter to an object.
	 *  This method is fail-safe.
	 *  Filter can null! (is this a good choice?)
	 *  If the filter throws an exception false is returned
	 *  and a warning is logged.
	 *  @param filter	The filter to apply.
	 *  @param object	The object to apply the filter to.
	 *  @return True if the filter matches the object, false otherwise.
	 */
	public boolean	applyFilter(IFilter filter, Object object)
	{
		boolean	ret	= false;

		if(filter!=null)
		{
			try
			{
				ret	= filter.filter(object);
			}
//			catch(UnknownPropertyException e)
//			{
//				//e.printStackTrace();
//				//System.out.println("Filter Exception: "+filter+", "+object);
//				StringWriter	sw	= new StringWriter();
//				e.printStackTrace(new PrintWriter(sw));
//				getLogger().fine("Filter threw exception (filter, object): "+filter+", "+object+"\n"+sw);
//			}
			catch(Exception e)
			{
				//e.printStackTrace();
				//System.out.println("Filter Exception: "+filter+", "+object);
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				getLogger().warning("Filter threw exception (filter, object): "+filter+", "+object+"\n"+sw);
			}
		}

/*		if(!ret)
		{
			System.out.println("Unmatched filter: "+filter+", "+object);
		}
*/
		return ret;
	}


	/**
	 *  Start when a new transaction when possible (only one allowed).
	 */
	protected void	startSystemEventTransaction()
	{
		// Hack!!! Todo: Distinguish between internal (allowed)
		// and external (not allowed) threads.
		assert !transaction_running;
		transaction_running	= true;
	}

	/**
	 *  End the currently running transaction.
	 *  Results is getting all collected events beeing throwed.
	 */
	protected void	commitSystemEventTransaction()
	{
		assert transaction_running;

		// Use try/finally to safely exit after exception.
		try
		{
			if(!atomic)
				processTransactionConsequences();
		}
		finally
		{
			transaction_running	= false;
		}
	}

	/**
	 *  Test if a transaction is started.
	 *  @return True, if started.
	 */
	protected boolean isTransactionStarted()
	{
		return this.transaction_running;
	}

	/**
	 *  Start an atomic transaction.
	 *  All possible side-effects (i.e. triggered conditions)
	 *  of internal changes (e.g. belief changes)
	 *  will be delayed and evaluated after endAtomic() has been called.
	 *  @see #endAtomic()
	 */
	public void	startAtomic()
	{
		assert !atomic: "Is already atomic.";
		atomic	= true;
	}

//RuntimeException	st;

	/**
	 *  End an atomic transaction.
	 *  Side-effects (i.e. triggered conditions)
	 *  of all internal changes (e.g. belief changes)
	 *  performed after the last call to startAtomic()
	 *  will now be evaluated and performed.
	 *  @see #startAtomic()
	 */
	public void	endAtomic()
	{
//		if(!atomic)
//		{
//			System.err.println("hier1: "+currentplan);
//			st.printStackTrace(System.err);
//			System.err.println("hier2: "+currentplan);
//			throw new RuntimeException("second end atomic: "+currentplan);
//		}
//		try
//		{
//			throw new RuntimeException("first end atomic: "+currentplan);
//		}
//		catch(RuntimeException e)
//		{
//			st	=e;
//		}

		assert atomic;
		atomic	= false;

		// Hack!!! Do not use processTransactionConsequences() directly,
		// as info events need to know if transaction committing is in progress.
		startSystemEventTransaction();
		commitSystemEventTransaction();
	}

	/**
	 *  Check if atomic state is enabled.
	 */
	public boolean	isAtomic()
	{
		return atomic;
	}

	/**
	 *  Internal method invoked, when the consequences of
	 *  a transaction should be processed.
	 */
	protected void processTransactionConsequences()
	{
		//int state = getAgenda().getState();

		// Hack!!! Speedup for plain agents.
		if(children.isEmpty())
		{
			executeConditions();
			notifySystemEventListeners();
			return;
		}

		List capas = getAllCapabilities();

		// Execute conditions in all capabilities.
		// May lead to additional info events (i.e. factRead) being thrown!!!
		for(int i=0; i<capas.size(); i++)
			((RCapability)capas.get(i)).executeConditions();

		// Throw info events in all capabilities.
		for(int i=0; i<capas.size(); i++)
			((RCapability)capas.get(i)).notifySystemEventListeners();
	}

	/**
	 *  Start monitoring the consequences.
	 */
	public void startMonitorConsequences()
	{
		//assert monitor_consequences==0; //start/endAtomic()

		monitor_consequences++;
		if(monitor_consequences==1)
			agenda_state = getInterpreter().getAgendaState();
	}

	/**
	 *  Checks if consequences have been produced and
	 *  interrupts the executing plan accordingly.
	 */
	public void endMonitorConsequences()
	{
		assert monitor_consequences>0;
	
		monitor_consequences--;
		if(monitor_consequences==0)
		{
			// When consequences pause plan (micro step)
			// to allow consequences being executed.
			// todo: only when actions are added this is of importance
			if(agenda_state!=getInterpreter().getAgendaState())
			{
				RPlan	rplan	= getCurrentPlan();
				if(rplan!=null)
					rplan.interruptPlanStep();
			}
		}
	}

	// Hack!!! Needed, because Plan.dispatchSubgoalAndWait() is crappy.
	public void resetMonitorConsequences()
	{
		monitor_consequences	= 0;
	}

	//-------- additional IJadexAgent methods --------
		
	/**
	 *  Get the command-line arguments.
	 *  @return The command-line arguments. 
	 */
	protected  Map getArguments()
	{
		return arguments;
	}

	/**
	 *  Get the  command-line argument names.
	 *  @return The  command-line argument names.
	 *  todo: would require to find exported beliefs in agent and map their
	 *  names to the current scope :-( 
	 */
	//public Map getArgumentNames();
	
	//-------- garbage collection of unreferenced elements --------
	
	/**
	 *  Schedule an element to be garbage collected.
	 *  This method can safely be called from external
	 *  threads (e.g. Java's finalizer thread.
	 */
	public void	scheduleGarbageCollection (ICleanable element)
	{
		synchronized(cleanables)
		{
			if(cleanables.add(element))
			{
				if(!garbagecollection)
				{
					addTimetableEntry(new TimetableData(500, new GarbageCollectionAction(this)));
					garbagecollection	= true;
				}
			}
			else
			{
				assert false : "Garbage collection scheduled twice for element: "+element;
			}
		}
	}
	
	/**
	 *  Perform garbage collection of unreferenced elements.
	 *  Called from the agenda action (agent thread).
	 */
	public void performGarbageCollection()
	{
		synchronized(cleanables)
		{
			assert garbagecollection;
//			System.out.println("Performing garbage collection: "+this);
			for(Iterator it=cleanables.iterator(); it.hasNext(); )
				((ICleanable)it.next()).cleanup();
			
			cleanables.clear();
			garbagecollection	= false;
		}
	}

	/**
	 *  Get the interpreter.
	 *  @return The interpreter.
	 */
	public JadexInterpreter	getInterpreter()
	{
		return interpreter;
	}	
}
