package jadex.runtime.impl;

import java.util.*;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;

/**
 *  A struct for storing data about MPlan candidates.
 */
public class PlanInfo	extends CandidateInfo
{
	//-------- attributes --------

	/** The plan element. */
	protected IMPlan	plan;

	/** The configuration. */
	protected IMConfigPlan config;

	/** The binding (additional parameters). */
	protected Map binding;

	//-------- constructor -------

	/**
	 *  Create a waitqueue info object.
	 *  @param event	The event.
	 *  @param config	The plan configuration.
	 *  @param binding The binding.
	 */
	public PlanInfo(IMConfigPlan config, IREvent event, IMPlan plan, Map binding)
	{
		super(event, null);
		this.config = config;
		this.plan = plan;
		this.binding = binding;
	}

	//-------- overridden methods --------

	/**
	 *  Get the plan instance info.
	 *  @return	The plan instance info.
	 */
	public RPlan	getPlanInstance()
	{
		if(rplan==null)
		{
			RCapability	scope	= revent.getScope();
			IMPlan plan = getPlanModel();
			Map	bd = getBinding();
			// Hack!!! Avoid event to be part of original bd.
			bd	= bd!=null ? new HashMap(bd) : SCollection.createHashMap();
			bd.put("$event", revent);
			IRGoal propgoal = null;
			// Set the proprietary goal, when a process goal event occured.
			if(revent instanceof IRGoalEvent && !((IRGoalEvent)revent).isInfo())
				propgoal = ((IRGoalEvent)revent).getGoal();
			// Create (maybe dummy) root goal for new plan instance.
			RProcessGoal processgoal = null;
			try
			{
				processgoal = scope.getGoalbase().createProcessGoal(propgoal, this);
				// Create the plan instance rplan
			}
			catch(Exception e)
			{
				processgoal = scope.getGoalbase().createProcessGoal(propgoal, this);
				e.printStackTrace();
			}
			rplan = scope.getPlanbase().createPlan(config!=null? config.getName(): null, 
				plan, getConfiguration(), bd, processgoal, revent);
			processgoal.setPlanInstance(rplan);

			assert rplan!=null;
		}
		return this.rplan;
	}

	//-------- additional methods --------

	/**
	 *  Get the plan.
	 *  @return	The plan.
	 */
	public IMPlan	getPlanModel()
	{
		return this.plan;
	}

	/**
	 *  Get the binding.
	 *  @return	The binding.
	 */
	public Map	getBinding()
	{
		return this.binding;
	}

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		Map representation	= super.getEncodableRepresentation();
		representation.put("name", "MPlanCandidate_"+plan.getName());
		representation.put("plan", plan.getName());
		representation.put("binding", ""+binding);
		return representation;
	}

	/**
	 *  Set the event.
	 */
	// Hack!!! Only required to create Initial/conditional plan events (RPlanbase).
	protected void	setEvent(IREvent event)
	{
		this.revent	= event;
	}

	/**
	 *  Get the configuration.
	 *  @return The configuration.
	 */
	protected IMConfigPlan getConfiguration()
	{
		return config;
	}

	//-------- overridings --------

	/**
	 *  Test if this plan info is equal to another object.
	 *  Two plan infos are equal when they have the same
	 *  plan and binding.
	 *  @param object	The object against which to test equality.
	 *  @return	True, when the object is equal to this plan info.
	 */
	public boolean	equals(Object object)
	{
		return (object instanceof PlanInfo)
			&& plan.equals(((PlanInfo)object).getPlanModel())
			&& (binding==null && ((PlanInfo)object).getBinding()==null
				|| binding.equals(((PlanInfo)object).getBinding()));
	}

	/**
	 *  Calculate the hashcode.
	 */
	public int hashCode()
	{
		int code = 0;
		if(plan!=null)
			code = code ^ plan.hashCode();
		if(binding!=null)
			code = code ^ binding.hashCode();
		return code;
	}

	/**
	 *  Create a string representation of this plan instance info.
	 *  @return	This plan instance info represented as string.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(plan=");
		sb.append(plan.getName());
		sb.append(", binding=");
		sb.append(binding);
		sb.append(")");
		return sb.toString();
	}
}

