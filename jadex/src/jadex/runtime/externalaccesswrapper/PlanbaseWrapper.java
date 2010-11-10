package jadex.runtime.externalaccesswrapper;

import jadex.model.IMPlan;
import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.RPlan;
import jadex.runtime.impl.RPlanbase;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;

/**
 *  The planbase wrapper accessible from within plans.
 */
public class PlanbaseWrapper extends ElementWrapper implements IPlanbase
{
	//-------- attributes --------

	/** The original plan base. */
	protected RPlanbase planbase;

	//-------- constructors --------

	/**
	 *  Create a new planbase wrapper.
	 */
	protected PlanbaseWrapper(RPlanbase planbase)
	{
		super(planbase.getScope().getAgent(), planbase);
		this.planbase = planbase;
	}

	//-------- methods --------

	/**
	 *  Get all running plans of this planbase.
	 *  @return The plans.
	 */
	public IPlan[] getPlans()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  planbase.getPlans();
			}
		};
		RPlan[]	rplans	= (RPlan[])exe.object;
		IPlan[]	plans	= new IPlan[rplans.length];
		for(int i=0; i<plans.length; i++)
			plans[i]	= new PlanWrapper(rplans[i]);
		return plans;
	}

	/**
	 *  Get all plans of a specified type (=model element name).
	 *  @param type The plan type.
	 *  @return All plans of the specified type.
	 */
	public IPlan[] getPlans(final String type)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  planbase.getPlans(type);
			}
		};
		RPlan[]	rplans	= (RPlan[])exe.object;
		IPlan[]	plans	= new IPlan[rplans.length];
		for(int i=0; i<plans.length; i++)
			plans[i]	= new PlanWrapper(rplans[i]);
		return plans;
	}

	/**
	 *  Get a plan by name.
	 *  @param name	The plan name.
	 *  @return The plan with that name (if any).
	 */
	public IPlan	getPlan(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void run()
			{
				object =  planbase.getPlan(name);
			}
		};
		return exe.object==null? null: new PlanWrapper((RPlan)exe.object);
	}

	/**
	 *  Register a new plan.
	 *  @param mplan The new plan model.
	 */
	public void registerPlan(final IMPlan mplan)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				planbase.registerPlan(mplan);
			}
		};
	}

	/**
	 *  Deregister a plan.
	 *  @param mplan The plan model.
	 */
	public void deregisterPlan(final IMPlan mplan)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				planbase.deregisterPlan(mplan);
			}
		};
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a plan listener.
	 *  @param listener The plan listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addPlanListener(final String type, final IPlanListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.PLAN_ADDED, ISystemEventTypes.PLAN_REMOVED}, type);
				final AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, planbase, type}));
				getCapability().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removePlanListener(final String type, final IPlanListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(new Object[]{userlistener, planbase, type});
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
}
