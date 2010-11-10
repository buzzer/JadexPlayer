package jadex.runtime.planwrapper;

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
		super(planbase);
		this.planbase = planbase;
	}

	//-------- methods --------

	/**
	 *  Get all running plans of this planbase.
	 *  @return The plans.
	 */
	public IPlan[] getPlans()
	{
		checkThreadAccess();
		RPlan[]	rplans	= planbase.getPlans();
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
	public IPlan[] getPlans(String type)
	{
		checkThreadAccess();
		RPlan[]	rplans	= planbase.getPlans(type);
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
	public IPlan	getPlan(String name)
	{
		checkThreadAccess();
		RPlan plan = planbase.getPlan(name);
		return plan==null? null: new PlanWrapper(plan);
	}

	/**
	 *  Register a new plan.
	 *  @param mplan The new plan model.
	 */
	public void registerPlan(IMPlan mplan)
	{
		checkThreadAccess();
		planbase.registerPlan(mplan);
	}

	/**
	 *  Deregister a plan.
	 *  @param mplan The plan model.
	 */
	public void deregisterPlan(IMPlan mplan)
	{
		checkThreadAccess();
		planbase.deregisterPlan(mplan);
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a plan listener.
	 *  @param listener The plan listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addPlanListener(String type, IPlanListener userlistener, boolean async)
	{
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.PLAN_ADDED, ISystemEventTypes.PLAN_REMOVED}, type);
		AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(new Object[]{userlistener, planbase, type}));
		getCapability().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a goal listener.
	 *  @param listener The goal listener.
	 */
	public void removePlanListener(String type, IPlanListener userlistener)
	{
		checkThreadAccess();
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

}
