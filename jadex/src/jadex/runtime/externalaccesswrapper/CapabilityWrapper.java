package jadex.runtime.externalaccesswrapper;

import jadex.model.IMCapabilityReference;
import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.util.Tuple;

import java.util.logging.Logger;


/**
 *  The user level view on a capability.
 *  Methods can only be called from external threads,
 *  otherwise exceptions are thrown.
 */
public abstract class CapabilityWrapper	extends ElementWrapper	implements ICapability
{
	//--------attributes --------

	/** The original capability. */
	private RCapability	cap;

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
		super(cap.getAgent(), cap);
		this.cap	= cap;
	}

	//-------- methods --------

	/**
	 *  Get the gui wrapper.
	 *  @return The gui wrapper.
	 */
	public IExternalAccess getExternalAccess()
	{
		return (IExternalAccess)this;
	}

	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger()
	{
		//checkThreadAccess();
		return cap.getLogger();
	}

	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IBeliefbase getBeliefbase()
	{
		//checkThreadAccess();
		if(beliefbase==null)
		{
			AgentInvocation	exe	= new AgentInvocation()
			{
				public void	run()
				{
					object	= cap.getBeliefbase();
				}
			};
			beliefbase	= new BeliefbaseWrapper((RBeliefbase)exe.object);
		}
		return beliefbase;
	}

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IGoalbase getGoalbase()
	{
		//checkThreadAccess();
		if(goalbase==null)
		{
			AgentInvocation	exe	= new AgentInvocation()
			{
				public void	run()
				{
					object	= cap.getGoalbase();
				}
			};
			goalbase	= new GoalbaseWrapper((RGoalbase)exe.object);
		}
		return goalbase;
	}

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IPlanbase getPlanbase()
	{
		//checkThreadAccess();
		if(planbase==null)
		{
			AgentInvocation	exe	= new AgentInvocation()
			{
				public void	run()
				{
					object	= cap.getPlanbase();
				}
			};
			planbase	= new PlanbaseWrapper((RPlanbase)exe.object);
		}
		return planbase;
	}

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IEventbase getEventbase()
	{
		//checkThreadAccess();
		if(eventbase==null)
		{
			AgentInvocation	exe	= new AgentInvocation()
			{
				public void	run()
				{
					object	= cap.getEventbase();
				}
			};
			eventbase	= new EventbaseWrapper((REventbase)exe.object);
		}
		return eventbase;
	}

	/**
	 *  Get the expression base.
	 *  @return The expression base.
	 */
	public IExpressionbase getExpressionbase()
	{
		//checkThreadAccess();
		if(expressionbase==null)
		{
			AgentInvocation	exe	= new AgentInvocation()
			{
				public void	run()
				{
					object	= cap.getExpressionbase();
				}
			};
			expressionbase	= new ExpressionbaseWrapper((RExpressionbase)exe.object);
		}
		return expressionbase;
	}

	/**
	 * Get the property base.
	 * @return The property base.
	 */
	public IPropertybase getPropertybase()
	{
		//checkThreadAccess();
		if(propertybase==null)
		{
			AgentInvocation	exe	= new AgentInvocation()
			{
				public void	run()
				{
					object	= cap.getPropertybase();
				}
			};
			propertybase	= new PropertybaseWrapper((RPropertybase)exe.object);
		}
		return propertybase;
	}

	/**
	 * Get the agent name.
	 * @return The agent name.
	 */
	public String getAgentName()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				string	= cap.getAgentName();
			}
		};
		return exe.string;
	}

	/**
	 * Get the configuration name.
	 * @return The configuration name.
	 */
	public String getConfigurationName()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				string	= cap.getConfigurationName();
			}
		};
		return exe.string;
	}

	/**
	 * Get the agent identifier.
	 * @return The agent identifier.
	 */
	public BasicAgentIdentifier	getAgentIdentifier()
	{
//		//checkThreadAccess();
//		AgentInvocation	exe	= new AgentInvocation()
//		{
//			public void	run()
//			{
//				object	= cap.getAgent().getAgentIdentifier();
//			}
//		};
//		return (BasicAgentIdentifier)exe.object;
		
		return cap.getAgent().getAgentIdentifier();
	}

	/**
	 *  Get the platform agent (e.g. a Jade agent).
	 *  @return The platform agent.
	 */
	public Object	getPlatformAgent()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= cap.getPlatformAgent();
			}
		};
		return exe.object;
	}

	/**
	 * Get the platform type.
	 * @return The platform type.
	 */
	public String getPlatformType()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				string	= cap.getAgent().getPlatformType();
			}
		};
		return exe.string;
	}

	/**
	 *  Add a new subcapability.
	 *  @param name	The name to give to the new capability.
	 *  @param capafile The capability definition.
	 */
	public void addSubcapability(final String name, final String capafile)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				cap.addSubcapability(name, capafile);
			}
		};
	}

	/**
	 *  Remove a subcapability.
	 *  @param name The subcapability name.
	 */
	public void removeSubcapability(final String name)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				cap.removeSubcapability(name);
			}
		};
	}

	/**
	 *  Register a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	registerSubcapability(final IMCapabilityReference subcap)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				cap.registerSubcapability(subcap);
			}
		};
	}

	/**
	 *  Deregister a subcapability.
	 *  @param subcap	The subcapability.
	 */
	public void	deregisterSubcapability(final IMCapabilityReference subcap)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				cap.deregisterSubcapability(subcap);
			}
		};
	}

	/**
	 *  Kill an agent.
	 */
	public void killAgent()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				cap.getAgent().getInterpreter().killAgent();
			}
		};
	}
	
	//-------- listeners --------
	
	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addAgentListener(final IAgentListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.AGENT_TERMINATING, ISystemEventTypes.AGENT_DIED}, unwrap());
				final AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, cap));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Add an agent listener
	 *  @param listener The listener.
	 */
	public void removeAgentListener(final IAgentListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
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
		};
	}

	/**
	 *  Get the capability.
	 *  @return The capability.
	 */
	protected RCapability getCapability()
	{
		return cap;
	}
}
