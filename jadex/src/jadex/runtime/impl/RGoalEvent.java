package jadex.runtime.impl;

import jadex.model.IMGoalEvent;
import jadex.util.SReflect;

import java.util.Map;

/**
 *  An event that contains a goal.
 */
public class RGoalEvent extends REvent implements IRGoalEvent
{
	//-------- attributes --------

	/** The original goal. */
	protected IRGoal goal;

	/** The goal event direction. */
	protected boolean info;

	//-------- constructors --------

	/**
	 *  Create a new event.
	 *  @param event	The model element.
	 *  @param owner	The owner.
	 *  @param goal	The goal.
	 */
	protected RGoalEvent(IMGoalEvent event, RElement owner, IRGoal goal, boolean info, Map exparams)
	{
		super(null, event, null, owner, null, exparams);
		assert goal!=null && goal.getScope()==getScope();

		this.goal	= goal;
		this.info = info;
	}

	//-------- BDI event properties --------

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public boolean	isRandomSelection()
	{
		return goal.isRandomSelection();
	}

	/**
	 *  Get the post-to-all flag.
	 *  @return True, when goal should be posted to all applicable plans.
	 */
	public boolean	isPostToAll()
	{
		// Info events are considered post-2-all
		return info || goal.isPostToAll();
	}

	//-------- methods --------

	/**
	 *  Is the event a info (result) event.
	 *  @return True, if it is an info event.
	 */
	public boolean isInfo()
	{
		return info;
	}

	/**
	 *  Get the goal.
	 *  @return The goal.
	 */
	public IRGoal getGoal()
	{
		return this.goal;
	}

	/**
	 * The the goal.
	 * @param goal The goal.
	 */
	protected void setGoal(IRGoal goal)
	{
		this.goal = goal;
	}

	/**
	 *  Get (or create) the apl for the event.
	 *  @return The apl.
	 *  // todo: improve/remove this method
	 */
	public ApplicableCandidateList getApplicableCandidatesList()
	{
		ApplicableCandidateList ret = isInfo()? new ApplicableCandidateList(this): goal.getApplicableCandidateList();
		if(ret==null)
		{
			ret = new ApplicableCandidateList(this);
			goal.setApplicableCandidateList(ret);
		}
		else
		{
			// todo: Hack! Could be false event (ref or not?) find some more elegant solution for apl storing
			ret.setEvent(this);
		}
		return ret;
	}

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map rep = super.getEncodableRepresentation();
		rep.put("info", ""+info);
		rep.put("goal", goal.getName());
		return rep;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	buf	= new StringBuffer();
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(info=");
		buf.append(info);
		buf.append(", goal=");
		buf.append(goal);
		buf.append(")");
		return buf.toString();
	}

	/**
	 *  Get a goal for an event (if any).
	 *  @param event The event.
	 *  @return The goal.
	 */
	public static RGoal getGoal(IRGoalEvent event)
	{
		RGoal ret = null;
		RReferenceableElement	tmp	= (RReferenceableElement)event;
		while(tmp instanceof RElementReference)
			tmp	= ((RElementReference)tmp).getReferencedElement();
	
		ret = (RGoal)((RGoalEvent)tmp).getGoal().getOriginalElement();
	
		return ret;
	}
}


