package jadex.runtime.impl;

import jadex.adapter.IAgentAdapter;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.*;
import jadex.util.concurrent.ThreadPoolFactory;
import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ExternalAccess;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.agent.CleanupAgentAction;
import jadex.runtime.impl.agenda.eventprocessing.ProcessEventAction;

import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.*;

/**
 * A capability instance stores runtime
 * information about a capability.
 */
public class RCapability extends RElement
{
	//-------- constants --------

	/** The number of init level(s). */
	public static int	INIT_LEVELS	= 2;

	//-------- attributes --------

	/** The configuration. */
	protected IMConfiguration config;

	/** The bdi agent. */
	protected RBDIAgent agent;

	/** The belief base. */
	protected RBeliefbase beliefbase;

	/** The plan base. */
	protected RGoalbase goalbase;

	/** The plan base. */
	protected RPlanbase planbase;

	/** The expression base. */
	protected RExpressionbase expressionbase;

	/** The event base. */
	protected REventbase eventbase;

	/** The property base. */
	protected RPropertybase propertybase;

	/** The external access wrapper. */
	protected ExternalAccess extaccess;

	/** The child capabilities. */
	protected IndexMap children;

	/** The logger. */
	protected transient Logger logger;
  
	/** The change listeners (listener -> listenerinfos(filter, notification attributes)). */
	protected IndexMap listenerinfos;

	/** Indicating if the capability is currently initing. */
	//protected boolean initing;
	
	/** The agent termination condition (referenced to avoid garbage collection). */
	protected InterpreterCondition	killcond;

	//-------- constructor --------

	/**
	 *  Create a new capability.
	 *  @param name       The name.
	 *  @param capability The model element.
	 *  @param owner      The owner
	 *  @param exparams	The expression parameters.
	 */
	protected RCapability(String name, IMCapability capability, String statename, RElement owner, Map exparams)
	{
		super(name, capability, owner, exparams);

		assert capability!=null: name;

		if(capability.isAbstract())
			throw new RuntimeException("Abstract capability "+capability+" can't be instantiated (problem in platform properties).");
		
		this.config	= statename==null || statename.length()==0?
			capability.getConfigurationbase().getDefaultConfiguration()
			: capability.getConfigurationbase().getConfiguration(statename);
		this.children = SCollection.createIndexMap();
		this.listenerinfos = SCollection.createIndexMap();

		// Cache resolved reference to agent (= top-level capability).
		RCapability tmp = this;
		while(tmp.getParent()!=null)
			tmp = tmp.getParent();
		this.agent = (RBDIAgent)tmp;
		setExpressionParameter("$agent", getAgent()); // Hack! Needed for property base.

		// Add myself to to mappings of mcap->rcap.
		agent.addCapabilityMapping(capability, this);

		// Instantiate subcapabilites. To be done before bases are created,
		// because bases try to resolve assignto references.
		IMCapabilityReference[] subcaps	= capability.getCapabilityReferences();
		for(int i = 0; i<subcaps.length; i++)
		{
			// Hack!!! What is the right way to combine initial and reference settings
			// e.g. name, state, etc.
			IMConfigCapability	inicap	= getConfiguration()==null? null:
				getConfiguration().getInitialCapability(subcaps[i]);
			// Hack!!! Do not use addChild as it throws a system event...
			this.children.add(subcaps[i].getName(), new RCapability(subcaps[i].getName(), subcaps[i].getCapability(),
				inicap!=null? inicap.getConfiguration(): null, this, null));
		}

		// Create bases (will be started later, when this capability is started).
		this.beliefbase = new RBeliefbase(capability.getBeliefbase(), this);
		this.goalbase = new RGoalbase(capability.getGoalbase(), this);
		this.planbase = new RPlanbase(capability.getPlanbase(), this);
		this.eventbase = new REventbase(capability.getEventbase(), this);
		this.expressionbase = new RExpressionbase(capability.getExpressionbase(), this);
		this.propertybase = new RPropertybase(capability.getPropertybase(), this);

		// Initialize expression parameters.
		setExpressionParameter("$scope", this);
		setExpressionParameter("$beliefbase", beliefbase);
		setExpressionParameter("$goalbase", goalbase);
		setExpressionParameter("$planbase", planbase);
		setExpressionParameter("$expressionbase", expressionbase);
		setExpressionParameter("$eventbase", eventbase);
		setExpressionParameter("$propertybase", propertybase);

		this.propertybase.constructorInit(); // Hack!
	}

	/**
	 *  Initialize this capability and its subcapabilities
	 *  with the given configuration.
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
	protected void	init(int level)
	{
		// Init inner capabilities first (assuming there are more dependencies downwards than upwards).
		RCapability[]	caps	= getChildren();
		for(int i=0; i<caps.length; i++)
		{
			caps[i].init(level);
		}

		// Start local bases.
		this.beliefbase.init(level);
		this.goalbase.init(level);
		this.planbase.init(level);
		this.eventbase.init(level);
		this.expressionbase.init(level);
		this.propertybase.init(level);
	}

	/**
	 *  Get the configuration  supplied on capability creation.
	 *  Used from bases to configure themselves when started or ended. 
	 */
	protected IMConfiguration	getConfiguration()
	{
		return config;
	}
	
	//-------- public methods --------

	/**
	 *  Get the gui wrapper.
	 *  @return The gui wrapper.
	 */
	public IExternalAccess	getExternalAccess()
	{
		// Hack!!! Should be synchronized?
		// But extaccess is just cached for speed, duplicates do not matter?
		if(extaccess==null)
		{
			this.extaccess	= new ExternalAccess(this);
		}
		return extaccess;
	}

	/**
	 *  Get the fully qualified name with respect its owner.
	 *  @return The fully qualified name.
	 */
	public String getDetailName()
	{
		return this.getParent()==null? getName(): getParent().getDetailName()+"."+getName();
	}

	/**
	 * Get the belief base.
	 * @return The belief base.
	 */
	public RBeliefbase getBeliefbase()
	{
		return this.beliefbase;
	}

	/**
	 * Get the plan base.
	 * @return The plan base.
	 */
	public RPlanbase getPlanbase()
	{
		return this.planbase;
	}

	/**
	 * Get the goal base.
	 * @return The goal base.
	 */
	public RGoalbase getGoalbase()
	{
		return this.goalbase;
	}

	/**
	 * Get the expression base.
	 * @return The expression base.
	 */
	public RExpressionbase getExpressionbase()
	{
		return this.expressionbase;
	}

	/**
	 * Get the event base.
	 * @return The event base.
	 */
	public REventbase getEventbase()
	{
		return this.eventbase;
	}

	/**
	 * Get the property base.
	 * @return The property base.
	 */
	public RPropertybase getPropertybase()
	{
		return this.propertybase;
	}

	/**
	 *  Add a new subcapability.
	 *  @param name	The name to give to the new capability.
	 *  @param capafile The capability definition.
	 *  @deprecated
	 */
	public void	addSubcapability(String name, String capafile)
	{
		try
		{
			// Load capability model from definition file.
			IMCapability	mcap	= SXML.loadCapabilityModel(capafile,
				((IMCapability)getModelElement()).getFullImports(), getModelElement());

			// Create capability instance as child of the local scope.
			// Will initialize itself (mostly).
			// Imported beliefs, and access to agent wide parameters
			// (e.g $arg1) are not supported!
			RCapability capa = new RCapability(name, mcap, null, this, null);
			addChild(capa);
			// Perform all inits at once.
			for(int i=0; i<RCapability.INIT_LEVELS; i++)
				capa.init(i);
		
		}
		catch(Exception e)
		{
			System.out.println("Could not dynamically add subcapability: "+capafile);
			e.printStackTrace();
		}
	}

	/**
	 *  Remove a subcapability.
	 *  @param name The subcapability name.
	 *  @deprecated
	 */
	public void removeSubcapability(String name)
	{
		// todo: implement me! what else needs to be done?
		removeChild(getChild(name));
	}

	/**
	 *  Register a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	registerSubcapability(IMCapabilityReference subcap)
	{
		// Get initial capability configuration (if any).
		IMConfigCapabilitybase	initialbase	= getScope().getConfiguration()!=null?
			getScope().getConfiguration().getCapabilitybase() : null;
		IMConfigCapability	inicap	= initialbase!=null ? initialbase.getCapabilityConfiguration(subcap) : null;
		// Create capability instance as child of the local scope.
		RCapability capa = new RCapability(subcap.getName(), subcap.getCapability(), inicap!=null ? inicap.getConfiguration() : null, this, null);
		addChild(capa);
		// Perform all inits at once.
		for(int i=0; i<RCapability.INIT_LEVELS; i++)
			capa.init(i);
	}

	/**
	 *  Deregister a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	deregisterSubcapability(IMCapabilityReference subcap)
	{
		// todo: implement me! what else needs to be done?
		removeChild(getChild(subcap.getName()));
	}
	
	//-------- methods --------

	/**
	 * Get the parent scope.
	 * @return The outer scope.
	 */
	protected RCapability getParent()
	{
		return (RCapability)getOwner();
	}

	/**
	 *  Get the nesting level of a capability
	 *  (distance to agent).
	 *  @return The level.
	 */
	protected int getNestingLevel()
	{
		int ret = 0;
		RCapability cap = this;
		while(cap!=null)
		{
			cap = cap.getParent();
			if(cap!=null)
				ret++;
		}
		return ret;
	}

	/**
	 * Get the agent.
	 * @return The agent.
	 */
	public RBDIAgent getAgent()
	{
		assert agent!=null;
		return agent;
	}

	/**
	 * Get the platform agent.
	 * @return The platform agent.
	 */
	public Object getPlatformAgent()
	{
		return agent.getAgentAdapter();
	}

	/**
	 * Set the parent.
	 * Do not use directly, use addChild() instead.
	 * param parent	The new parent of this goal.
	 * /
	protected void setParent(RCapability parent)
	{
		this.owner	= parent;	// Hack ???	
	}*/

	/**
	 * Get the logger for this scope.
	 * @return The logger.
	 */
	public Logger getLogger()
	{
		// todo: repair this
		//assert !isCleanedup(): this;

		// if logger does not already exists, create it
		if(logger==null)
		{
			// get logger with unique capability name
			String name = getDetailName();

			// Hack!!! Might throw exception in applet / webstart.
			try
			{
				logger = Logger.getLogger(name);
				//System.out.println(logger.getParent().getLevel());

			
				// get logging properties (from ADF)
				// the level of the logger
				// can be Integer or Level
				Object level = getPropertybase().getProperty("logging.level");
				if(level!=null)
				{
					logger.setLevel(Level.parse(level.toString()));
				}
	
				// if logger should use Handlers of parent (global) logger
				// the global logger has a ConsoleHandler(Level:INFO) by default
				Boolean useParentHandlers = (Boolean)getPropertybase().getProperty("logging.useParentHandlers");
				if(useParentHandlers!=null)
				{
					logger.setUseParentHandlers(useParentHandlers.booleanValue());
				}
	
	            //  add a ConsoleHandler to the logger to print out
	            // logs to the console. Set Level to given property value
	            Object consolelevel = getPropertybase().getProperty("logging.addConsoleHandler");
	            if(consolelevel!=null)
	            {
	                ConsoleHandler console = new ConsoleHandler();
	                console.setLevel(Level.parse(consolelevel.toString()));
	                logger.addHandler(console);
	            }
			}
			catch(SecurityException e)
			{
				// Hack!!! For applets / webstart use anonymous logger.
				logger	= Logger.getAnonymousLogger();

							// get logging properties (from ADF)
				// the level of the logger
				// can be Integer or Level
				Object level = getPropertybase().getProperty("logging.level");
				if(level!=null)
				{
					logger.setLevel(Level.parse(level.toString()));
				}
	
				// if logger should use Handlers of parent (global) logger
				// the global logger has a ConsoleHandler(Level:INFO) by default
				Boolean useParentHandlers = (Boolean)getPropertybase().getProperty("logging.useParentHandlers");
				if(useParentHandlers!=null)
				{
					logger.setUseParentHandlers(useParentHandlers.booleanValue());
				}
	
	            //  add a ConsoleHandler to the logger to print out
	            // logs to the console. Set Level to given property value
	            Object consolelevel = getPropertybase().getProperty("logging.addConsoleHandler");
	            if(consolelevel!=null)
	            {
	                ConsoleHandler console = new ConsoleHandler();
	                console.setLevel(Level.parse(consolelevel.toString()));
	                logger.addHandler(console);
	            }
			}
		}
		return logger;
	}  
      
	/**
	 * Add a child.
	 * @param child The new child.
	 */
	protected void addChild(RCapability child)
	{
		this.children.add(child.getName(), child);
		// info event
		throwSystemEvent(new SystemEvent(SystemEvent.CAPABILITY_ADDED, child));
	}

	/**
	 *  Remove a child.
	 *  @param child The child to remove.
	 */
	protected void removeChild(RCapability child)
	{
		this.children.removeKey(child.getName());
		child.cleanup();
		// info event
		throwSystemEvent(new SystemEvent(SystemEvent.CAPABILITY_REMOVED, child));
	}

	/**
	 *  Get the child capabilities.
	 *  @return The children.
	 */
 	public RCapability[] getChildren()
	{
		return (RCapability[])children.getObjects(RCapability.class);
	}

	/**
	 *  Get the direct child with name.
	 *  @return The children.
	 */
	protected RCapability getChild(String name)
	{
		return (RCapability)children.get(name);
	}

	/**
	 *  Dispatch an event.
	 *  @param event The event.
	 */
	// Convenience method used from RElements (e.g. goals).
	public void dispatchEvent(IREvent event, IAgendaActionPrecondition precond)
	{ 
		//IREvent	orig	= REventbase.getOriginalEvent(event);
		//getAgent().addEventListElement(event, source);
		//event	= REventbase.getOriginalEvent(event);
		event.dispatched();	// Adjusts parameter protection mode.
    
		//getAgent().addEventListElement(event, source);
		agent.getInterpreter().addAgendaEntry(new ProcessEventAction(precond, agent, event), null); // todo: set cause?
	}

	/**
	 * Get the agent name.
	 * @return The agent name.
	 */
	public String getAgentName()
	{
		return getAgent().getName();
	}

	/**
	 * Get the configuration name.
	 * @return The configuration name.
	 */
	public String getConfigurationName()
	{
		return config==null? null: config.getName();
	}

	/**
	 *  Get the adapter agent.
	 *  @return The adapter agent.
	 */
	protected 	IAgentAdapter getAgentAdapter()
	{
		return getAgent().getAgentAdapter();
	}

	/**
	 *  Add a change listener (Notification is delayed, ie. on separate agenda action and
	 *  not async, ie. on the agent thread).
	 *  @param listener The change listener.
	 *  @param filter The filter for system event types the listener is interested in.
	 */
	public void addSystemEventListener(ISystemEventListener listener, IFilter filter)
	{
		addSystemEventListener(listener, filter, true, false);
//		System.out.println("listeners "+this+": "+listeners.size());
	}
	
	/**
	 *  Add a change listener.
	 *  @param listener The change listener.
	 *  @param filter The filter for system event types the listener is interested in.
	 *  @param delayed Notify on separate agenda action.
	 *  @param async Notify on separate thread. 
	 */
	public void addSystemEventListener(ISystemEventListener listener, IFilter filter, boolean delayed, boolean async)
	{
		this.listenerinfos.add(listener, new ListenerInfo(filter, delayed, async));
//		System.out.println("listeners "+this+": "+listeners.size());
	}
	
	/**
	 *  Remove a change listener.
	 *  @param listener The change listener.
	 */
	public void removeSystemEventListener(ISystemEventListener listener)
	{
		this.listenerinfos.removeKey(listener);
	}
	
	/**
	 *  Get all system event listeners.
	 *  @return All system event listeners.
	 */
	public ISystemEventListener[] getSystemEventListeners()
	{
		return (ISystemEventListener[])listenerinfos.keySet().toArray(new ISystemEventListener[listenerinfos.size()]);
	}

	/**
	 *  Get the state of the agent
	 *  encoded in a set of corresponding change events.
	 *  @param types The system event types, the caller is interested in.
	 */
	public List	getAgentState(String[] types)
	{
		return getAgent().getState(types);
	}

	/**
	 *  Get the state of this capability and enclosed subcapabilities
	 *  encoded in a set of corresponding change events.
	 *  @param types The system event types, the caller is interested in.
	 */
	public List	getState(String[] types)
	{
		ArrayList ret	= SCollection.createArrayList();

		// Add events for components.
		ret.addAll(beliefbase.getState(types));
		ret.addAll(goalbase.getState(types));
		ret.addAll(planbase.getState(types));

		// Add events for subcapabilites
		RCapability[] subcaps = getChildren();
		for(int i = 0; i<subcaps.length; i++)
		{
			if(ISystemEventTypes.Subtypes.isSubtype(SystemEvent.CAPABILITY_ADDED, types))
			{
				ret.add(new SystemEvent(SystemEvent.CAPABILITY_ADDED, subcaps[i]));
			}
			ret.addAll(subcaps[i].getState(types));
		}

		return ret;
	}

	protected int nested;

	//-------- expression parameters --------

	/**
	 *  Create a local map for holding expression parameters.
	 *  @return The local map.
	 */
	protected Map createLocalExpressionParameters()
	{
		return SCollection.createHashMap();
	}


	/**
	 *  Get the expression parameters.
	 */
	public Map	getExpressionParameters()
	{
		// Overridden to avoid connection to owner.
		return exparams;
	}

	//-------- helper methods --------
	
	/**
	 *  Get the capability per full name.
	 */
	public RCapability getCapability(String name)
	{
		// Get the capability.
		RCapability cap = getAgent();
		if(name!=null)
		{
			StringTokenizer stok = new StringTokenizer(name, ".");
			stok.nextToken();	// Skip first token (agent).
			while(stok.hasMoreTokens())
			{
				cap = cap.getChild(stok.nextToken());
				if(cap==null)
				{
					throw new RuntimeException("No such capability: "+name);
				}
			}
		}
		return cap;
	}
  
	/** 
	 * @return the subcapabilities of this one
	 */
	public RCapability[] getSubCapabilities()
	{
		return getChildren();
	}

	/**
	 *  Exit the running state.
	 *  Before changing to the end state all currently
	 *  active elements (e.g. goals and plans) are removed.
	 */
	public void exitRunningState()
	{
		this.planbase.exitRunningState();
		this.goalbase.exitRunningState();
		this.beliefbase.exitRunningState();

		this.eventbase.exitRunningState();
		this.expressionbase.exitRunningState();
		this.propertybase.exitRunningState();

		// Activate end state of subcapabilities.
		RCapability[] subcaps = getChildren();
		for(int i=0; i<subcaps.length; i++)
		{
			subcaps[i].exitRunningState();
		}
	}

	/**
	 *  Activate the end state.
	 *  When changing to the end state
	 *  the elements from the end state are created.
	 */
	public void activateEndState()
	{
		this.planbase.activateEndState();
		this.goalbase.activateEndState();
		this.beliefbase.activateEndState();

		this.eventbase.activateEndState();
		this.expressionbase.activateEndState();
		this.propertybase.activateEndState();

		// Activate end state of subcapabilities.
		RCapability[] subcaps = getChildren();
		for(int i=0; i<subcaps.length; i++)
		{
			subcaps[i].activateEndState();
		}

		// Activate condition to kill agent, when end state has finished.
		// The precondition assures, that all capabilities are terminated before the agent is killed.
		// Todo: support custom conditions and timeout for each capability.
		this.killcond	= new InterpreterCondition(new CleanupAgentAction(this.getAgent(), new IAgendaActionPrecondition()
			{
				public boolean check()
				{
					// Agent should not be already terminated (if condition is triggered from different capabilities).
					return RBDIAgent.LIFECYCLESTATE_TERMINATING.equals(getAgent().getLifecycleState());
				}
			}), this)
		{
			public boolean isAffected(SystemEvent event)
			{
				return true;
			}
			
			public boolean isTriggered()
			{
				// Hack!!! Check global end state in each capability !?
				boolean	triggered	= getAgent().isEndStateTerminated();
				// Stop tracing ONCE (Hack!!! add to base class).
				if(triggered && ICondition.TRACE_ONCE.equals(getTraceMode()))
				{
					// Hack!!! Do not use setTraceMode, to avoid action being removed from agenda.
					trace = ICondition.TRACE_NEVER;
					getScope().getExpressionbase().removeCondition(this);
				}
				
//				if(triggered)
//				{
//					System.out.println("cleaning up agent: "+getAgent());
//				}
//				else
//				{
//					System.out.println("scope: "+getScope());
//				}
				
				return triggered;
			}
		};
		killcond.traceOnce();
	}
	
	/**
	 *  Check if the end state is terminated.
	 */
	public boolean isEndStateTerminated()
	{
		boolean	terminated	= planbase.isEndStateTerminated()
			&& goalbase.isEndStateTerminated()
			&& beliefbase.isEndStateTerminated()
			&& eventbase.isEndStateTerminated()
			&& expressionbase.isEndStateTerminated()
			&& propertybase.isEndStateTerminated();

		// Check end state of subcapabilities.
		if(terminated)
		{
			RCapability[] subcaps = getChildren();
			for(int i=0; terminated && i<subcaps.length; i++)
			{
				terminated = terminated && subcaps[i].isEndStateTerminated();
			}
		}
		
		return terminated;
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

		super.cleanup();

//		System.err.println("cleanup: "+this);
		
		// Cleanup kill condition.
		if(killcond!=null)
			killcond.cleanup();
		
		// Cleanup bases.
		this.planbase.cleanup();
		this.goalbase.cleanup();
		this.beliefbase.cleanup();

		this.eventbase.cleanup();
		this.expressionbase.cleanup();
		this.propertybase.cleanup();

		// Cleanup subcapabilities.
		RCapability[] subcaps = getChildren();
		for(int i=0; i<subcaps.length; i++)
		{
			subcaps[i].cleanup();
		}

		agent.removeCapabilityMapping(this);
//		System.err.println("finished cleanup: "+this);

		throwSystemEvent(new SystemEvent(SystemEvent.CAPABILITY_REMOVED, this));
	}

	/**
	 *  Get a matching content codec.
	 *  @param props The properties.
	 *  @return The content codec.
	 */
	public IContentCodec getContentCodec(Properties props)
	{
		IContentCodec ret = null;
		String[] codecnames = getPropertybase().getPropertyNames("contentcodec.");
		for(int i=0; ret==null && i<codecnames.length; i++)
		{
			IContentCodec tmp = (IContentCodec)getPropertybase().getProperty(codecnames[i]);
			if(tmp.match(props))
				ret = tmp;
		}
		//if(ret==null)
		//	throw new ContentException("No content codec found for: "+props);
		return ret;
	}

	//-------- system event handling --------

	protected HashSet changeevents = SCollection.createHashSet();

	/** The set of collected info events (including change relevant).
	 *  Is linked hash set as insertion order is imported. */
	protected Set infoevents = SCollection.createLinkedHashSet(); // note for mobile-version: use createHashSet()

	//public boolean blocked = false;

	/**
	 *  Throws a system event.
	 *  @param event The event.
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		boolean	change	= event.isChangeRelevant();

		// Ignore info events, when no listeners registered.
		if(!change && listenerinfos.isEmpty())
			return;
		
		if(agent.isTransactionStarted() && !change)
		{
			// When info event occurs inside transaction just add it.
			addInfoEvent(event);
		}
		else
		{
			agent.startSystemEventTransaction();
			this.collectSystemEvents(event);
			agent.commitSystemEventTransaction();
		}
	}

	/**
	 *  Collect the system events generated by other elements.
	 *  Checks for affected expressions and adds propagated events.
	 */
	protected void collectSystemEvents(SystemEvent event)
	{
		// Assure that a system event transaction is active.
		assert agent.isTransactionStarted();
		
		if(!infoevents.add(event))
		{
//			if(getName().indexOf("BeliefSetChanges")!=-1)
//				System.err.println("ignored: "+event);
			return;
		}
//		else
//		{
//			if(getName().indexOf("BeliefSetChanges")!=-1)
//				System.err.println("added: "+event);
//		}

		RPlan pl = agent.getCurrentPlan();
		if(pl!=null)
			event.setCause(pl.getName());
		
		if(event.isChangeRelevant())
		{
			changeevents.add(event);
			RExpression[]	exps = (RExpression[])expressionbase.getExpressions().toArray(new RExpression[0]);
			for(int i=0; i<exps.length; i++)
			{
				// Check if expression is affected and produces another event.
				RExpression	exp	= exps[i];
				SystemEvent causedevent = exp.getSystemEvent(event);
				if(causedevent!=null)
				{
					// For referenceable elements collect events of references also.
					if(causedevent.getSource() instanceof RReferenceableElement)
					{
						// Check if expression belongs to element reference.
						RReferenceableElement	source	= (RReferenceableElement)causedevent.getSource();
						if(source instanceof RElementReference)
						{
							while(source instanceof RElementReference)
								source	= source.getOriginalElement();
						}
						source.createSystemEvents(causedevent);
					}
					else
					{
						collectSystemEvents(causedevent);
					}
				}
			}
		}
	}

	/**
	 *  Add an info event to a running transaction.
	 *  @param event
	 */
	public void addInfoEvent(SystemEvent event)
	{
		assert agent.isTransactionStarted() && !event.isChangeRelevant() : event;
		if(infoevents.contains(event))
			return;

		RPlan pl=agent.getCurrentPlan();
		if (pl!=null) event.setCause(pl.getName());
			infoevents.add(event);
	}

	/**
	 *  Inform listeners about system events.
	 */
	public void notifySystemEventListeners()
	{
		List tonotify = SCollection.createArrayList();
		
		for(int i=0; i<listenerinfos.size(); i++)
		{
			final ISystemEventListener listener = (ISystemEventListener)listenerinfos.getKey(i);
			ListenerInfo li = (ListenerInfo)listenerinfos.get(listener);
			IFilter filter = li.getFilter();
			// Undelayed means not in a separate agenda action
			boolean delayed = li.isDelayed();
			// Asynchrnous means on a separate thread 
			// (allows invocation of all agent methods such as waitFor())
			boolean async = li.isAsync();

			// Filter out events relevant for listener.
			SystemEvent[]	ses	= (SystemEvent[])infoevents.toArray(new SystemEvent[infoevents.size()]);
			final ArrayList	matched	= SCollection.createArrayList();
			for(int e=0; e<ses.length; e++)
			{
				if(getAgent().applyFilter(filter, ses[e]))
				{
					//if(ses[e].getType().equals(ISystemEventTypes.AGENT_DIED))
					//	System.out.println("huzi");
					matched.add(ses[e]);
					// Add values for element sets (hack!!!)
					if(ses[e].getType().equals(SystemEvent.ESVALUES_CHANGED))
					{
						try
						{
							IRParameterSet	set	= (IRParameterSet)ses[e].getSource();
							ses[e].setValue(Arrays.asList(set.getValues()));
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}					
					}
					else if(ses[e].getType().equals(SystemEvent.BSFACTS_CHANGED))// && types.contains(SystemEvent.BSFACT_ADDED)) // todo:?
					{
						try
						{
							IRBeliefSet	set	= (IRBeliefSet)ses[e].getSource();
							ses[e].setValue(Arrays.asList(set.getFacts()));
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}					
					}
				}
			}

			// Inform listener, when some event matched.
			if(matched.size()>0)
			{						
				if(!delayed)
				{
					if(async)
					{
						ThreadPoolFactory.getThreadPool().execute(new Runnable()
						{
							public void run()
							{
								listener.systemEventsOccurred((SystemEvent[])
									matched.toArray(new SystemEvent[matched.size()]));
							}
						});
					}
					else
					{
						listener.systemEventsOccurred((SystemEvent[])
							matched.toArray(new SystemEvent[matched.size()]));
					}
				}
				else
				{
					tonotify.add(new Object[]{listener, (SystemEvent[])matched.toArray(new SystemEvent[matched.size()])});
				}
			}
		}
		
		// Inform listeners on agent thread.
		// Is it is not the agent thread, i.e. e.g. the plan thread postpone listener notification.
		// Problem: this cannot be done as an action always because in this case the action itself
		// would lead to the addition of new actions (InvokeLaterAction (ILA) - agenda step done - ILA...)
		//if(!isAgentThread() && tonotify.size()>0)
		if(tonotify.size()>0)
		{
			final Object[] todo = tonotify.toArray();
			
			getAgent().getInterpreter().addAgendaEntry(new IAgendaAction()
			{
				/**
				 *  Notify listeners.
				 */
				public void execute()
				{
					for(int i=0; i<todo.length; i++)
					{
						Object[] tmp = (Object[])todo[i];
						final ISystemEventListener listener = (ISystemEventListener)tmp[0];
						final SystemEvent[] events = (SystemEvent[])tmp[1];
						ListenerInfo li = (ListenerInfo)listenerinfos.get(listener);
						if(li!=null)
						{
							final boolean async = li.isAsync();
							
							if(async)
							{
								ThreadPoolFactory.getThreadPool().execute(new Runnable()
								{
									public void run()
									{
										try
										{
											listener.systemEventsOccurred(events);
										}
										catch(RuntimeException e)
										{
											StringWriter	sw	= new StringWriter();
											e.printStackTrace(new PrintWriter(sw));
											Level level = (Level)getPropertybase().getProperty(RPlan.PROPERTY_LOGGING_LEVEL_EXCEPTIONS);
											getLogger().log(level, getAgent().getName()+
												": Exception while executing: "+listener+"\n"+sw);
										}
									}
								});
							}
							else
							{
								try
								{
									listener.systemEventsOccurred(events);
								}
								catch(RuntimeException e)
								{
									StringWriter	sw	= new StringWriter();
									e.printStackTrace(new PrintWriter(sw));
									Level level = (Level)getPropertybase().getProperty(RPlan.PROPERTY_LOGGING_LEVEL_EXCEPTIONS);
									getLogger().log(level, getAgent().getName()+
										": Exception while executing: "+tmp[0]+"\n"+sw);
								}
							}
						}
					}
				}
				
				/**
				 *  Notifying listeners is always valid.
				 */
				public boolean isValid()
				{
					return true;
				}
			}, null);	// Todo: Which event to use as cause?
		}
			
		// Clear list of collected info events.
		infoevents.clear();
	}

	/**
	 *  Execute the conditions and clear the event set.
	 */
	protected void executeConditions()
	{
		if(!changeevents.isEmpty())
		{
			// Trigger conditions.
			Collection	sconds	= expressionbase.getConditions();

//			for(Iterator i=sconds.iterator(); i.hasNext(); )
//				((RCondition)i.next()).systemEventsOccurred(changeevents.iterator());

			// Hack!!! Need to copy to array, because new conditions might be created
			// due to hack in GoalCreationAction. Do not use fixed size array, due to weak set.
			IInterpreterCondition[] conds	= (IInterpreterCondition[])sconds.toArray(new IInterpreterCondition[0]);
			for(int i=0; i<conds.length; i++)
			{
				conds[i].systemEventsOccurred(changeevents.iterator());
			}
	
			// Clear list of collected change events.
			changeevents.clear();
		}
	}

	//-------- overridings --------

	/**
	 * Create a string representation of this element.
	 *
	 * @return	This element represented as string.
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(name);
		// todo: what should the toString() print out?
		/*sb.append("\nbeliefbase="+beliefbase.getName());
		sb.append("\ngoalbase="+goalbase.getName());
		sb.append("\nplanbase="+planbase.getName());
		//sb.append("\nservices="+services);*/
		sb.append(")");
		return sb.toString();
	}

	//-------- manage element instances --------

	/** The instance count table. */
	protected transient Map	counters;

	/** The active instance count table (for debugging). */
	protected transient Map	active;

	/**
	 *  Get and increment the instance count for a modelelement.
	 *  @return The new instance count.
	 */
	protected int	instanceCount(Object element)
	{
		if(counters==null)
			counters	= SCollection.createWeakHashMap();
		int count	= 0;
		Integer icount	= (Integer)counters.get(element);
		if(icount!=null)
		{
			count	= icount.intValue();
		}

		count++;
		counters.put(element, new Integer(count));
		return count;
	}

	/**
	 *  Increment the active instance count for a modelelement.
	 */
	protected void	incActive(IMElement element)
	{
		if(active==null)
			active	= SCollection.createWeakHashMap();
		int count	= 0;
		Integer icount	= (Integer)active.get(element);
		if(icount!=null)
		{
			count	= icount.intValue();
		}

		count++;
		active.put(element, new Integer(count));

//		if(count%100==99)
//		{
//			// Garbage collect, to get more accurate results.
//			Runtime.getRuntime().gc();
//
//			System.out.println("+++---+++---Element count: ");
//			for(Iterator it=active.keySet().iterator(); it.hasNext(); )
//			{
//				Object key	= it.next();
//				System.out.println(key+":\t"+active.get(key));
//			}
//		}
	}

	/**
	 *  Decrement the active instance count for a modelelement.
	 */
	protected void	decActive(IMElement element)
	{
		if(active==null)
			active	= SCollection.createWeakHashMap();
		int count	= 0;
		Integer icount	= (Integer)active.get(element);
		if(icount!=null)
		{
			count	= icount.intValue();
			count--;
		}

		active.put(element, new Integer(count));
	}
		
	//-------- serialization handling --------

	protected Map	serialized_counters;
	protected Map	serialized_active;
	
	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	writeReplace() throws ObjectStreamException
	{
		// Copy weak maps as they are not serializable.
		if(counters!=null)
		{
			serialized_counters	= SCollection.createHashMap();
			serialized_counters.putAll(counters);
		}
		if(active!=null)
		{
			serialized_active	= SCollection.createHashMap();
			serialized_active.putAll(active);
		}
		return super.writeReplace();
	}

	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	readResolve() throws ObjectStreamException
	{
		// Restore weak maps as they are not serialized.
		if(serialized_counters!=null)
		{
			counters	= SCollection.createWeakHashMap();
			counters.putAll(serialized_counters);
			serialized_counters	= null;
		}
		if(serialized_active!=null)
		{
			active	= SCollection.createWeakHashMap();
			active.putAll(serialized_active);
			serialized_active	= null;
		}
		return super.readResolve();
	}
	
	/**
	 *  Information about a listener, ie. filter and filter attributes.
	 */
	protected static class ListenerInfo
	{
		//-------- attributes --------
		
		/** The filter. */
		protected IFilter filter;
		
		/** The delayed flag (delayed means notofication in separate agenda action). */
		protected boolean delayed;
		
		/** The async flag (async means on separate thread). */
		protected boolean async;
		
		//-------- constructors --------
		
		/**
		 *  Create a new listener info.
		 */
		public ListenerInfo(IFilter filter, boolean delayed, boolean async)
		{
			this.filter = filter;
			this.delayed = delayed;
			this.async = async;
		}

		//-------- methods --------
		
		/**
		 *  Get the async state.
		 *  @return The async state.
		 */
		public boolean isAsync()
		{
			return async;
		}

		/**
		 *  Get the delayed state.
		 *  @return The delayed state.
		 */
		public boolean isDelayed()
		{
			return delayed;
		}

		/**
		 *  Get the filter.
		 *  @return The filter.
		 */
		public IFilter getFilter()
		{
			return filter;
		}
	}
}
