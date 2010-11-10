package jadex.runtime.planwrapper;

import java.util.Map;
import java.util.logging.Logger;

import jadex.model.IMCapabilityReference;
import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
import jadex.util.Tuple;


/**
 *  The user level view on a capability.
 *  Methods can only be called from plans,
 *  otherwise exceptions are thrown.
 */
public class CapabilityWrapper extends ElementWrapper implements ICapability
{
	//--------attributes --------

	/** The original capability. */
	protected RCapability	cap;

	/** The beliefbase wrapper. */
	protected BeliefbaseWrapper	beliefbase;
	
	/** The goalbase wrapper. */
	protected GoalbaseWrapper	goalbase;
	
	/** The planbase wrapper. */
	protected PlanbaseWrapper	planbase;
	
	/** The eventbase wrapper. */
	protected EventbaseWrapper	eventbase;
	
	/** The expressionbase wrapper. */
	protected ExpressionbaseWrapper	expressionbase;

	/** The expressionbase wrapper. */
	protected PropertybaseWrapper	propertybase;

	//-------- constructors --------

	/**
	 *  Create a new CapabilityWrapper.
	 *  @param cap	The original capability.
	 */
	public CapabilityWrapper(RCapability cap)
	{
		super(cap);
		this.cap	= cap;
	}

	//-------- methods --------

	/**
	 *  Get the gui wrapper.
	 *  @return The gui wrapper.
	 */
	public IExternalAccess getExternalAccess()
	{
		return cap.getExternalAccess();
	}

	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger()
	{
		return cap.getLogger();
	}
	
	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IBeliefbase getBeliefbase()
	{
		checkThreadAccess();
		return beliefbase!=null ? beliefbase
			: (beliefbase=new BeliefbaseWrapper(cap.getBeliefbase()));
	}

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IGoalbase getGoalbase()
	{
		checkThreadAccess();
		return goalbase!=null ? goalbase
			: (goalbase=new GoalbaseWrapper(cap.getGoalbase()));
	}

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IPlanbase getPlanbase()
	{
		checkThreadAccess();
		return planbase!=null ? planbase
			: (planbase=new PlanbaseWrapper(cap.getPlanbase()));
	}

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IEventbase getEventbase()
	{
		checkThreadAccess();
		return eventbase!=null ? eventbase
			: (eventbase=new EventbaseWrapper(cap.getEventbase()));
	}

	/**
	 *  Get the expression base.
	 *  @return The expression base.
	 */
	public IExpressionbase getExpressionbase()
	{
		checkThreadAccess();
		return expressionbase!=null ? expressionbase
			: (expressionbase=new ExpressionbaseWrapper(cap.getExpressionbase()));
	}

	/**
	 * Get the property base.
	 * @return The property base.
	 */
	public IPropertybase getPropertybase()
	{
		checkThreadAccess();
		return propertybase!=null ? propertybase
			: (propertybase=new PropertybaseWrapper(cap.getPropertybase()));
	}

	/**
	 * Get the agent name.
	 * @return The agent name.
	 */
	public String getAgentName()
	{
		checkThreadAccess();
		return cap.getAgentName();
	}

	/**
	 * Get the configuration name.
	 * @return The configuration name.
	 */
	public String getConfigurationName()
	{
		checkThreadAccess();
		return cap.getConfigurationName();
	}

	/**
	 * Get the agent identifier.
	 * @return The agent identifier.
	 */
	public BasicAgentIdentifier	getAgentIdentifier()
	{
		checkThreadAccess();
		return cap.getAgent().getAgentIdentifier();
	}

	/**
	 * Get the JADE wrapper agent.
	 * @return The JADE wrapper agent.
	 */
	public Object getPlatformAgent()
	{
		checkThreadAccess();
		return cap.getPlatformAgent();
	}

	/**
	 * Get the platform type.
	 * @return The platform type.
	 */
	public String getPlatformType()
	{
		checkThreadAccess();
		return cap.getAgent().getPlatformType();
	}

	/**
	 *  Add a new subcapability.
	 *  @param name	The name to give to the new capability.
	 *  @param capafile The capability definition.
	 *  @deprecated
	 */
	public void addSubcapability(String name, String capafile)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{cap.addSubcapability(name, capafile);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Remove a subcapability.
	 *  @param name The subcapability name.
	 *  @deprecated
	 */
	public void removeSubcapability(String name)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{cap.removeSubcapability(name);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Register a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	registerSubcapability(IMCapabilityReference subcap)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{cap.registerSubcapability(subcap);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Deregister a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	deregisterSubcapability(IMCapabilityReference subcap)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{cap.deregisterSubcapability(subcap);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Kill an agent.
	 */
	public void killAgent()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{cap.getAgent().getInterpreter().killAgent();}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}
	
	//-------- listeners --------
	
	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addAgentListener(IAgentListener userlistener, boolean async)
	{
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.AGENT_TERMINATING}, unwrap());
		final AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, cap));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 */
	public void removeAgentListener(IAgentListener userlistener)
	{
		checkThreadAccess();
		Object	identifier	= new Tuple(userlistener, cap);
		ISystemEventListener[] listeners = getAgent().getSystemEventListeners();
		for(int i=0; i<listeners.length; i++)
		{
			if((listeners[i] instanceof AsynchronousSystemEventListener) 
				&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
			{
				getCapability().removeSystemEventListener(listeners[i]);
				break;
			}
		}
	}
}
