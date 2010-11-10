package jadex.runtime.impl;

import java.util.Map;
import jadex.model.*;
import jadex.runtime.ICandidateInfo;
import jadex.runtime.planwrapper.ElementWrapper;

/**
 *  A meta-goal is used for mete-level reasoning.
 */
public class RMetaGoal extends RQueryGoal
{
	//-------- constructors --------

	/**
	 *  Create a new goal.
	 *  @param name The name.
	 *  @param goal The model element.
	 *  @param owner The owner.
	 *  @param binding The binding.
	 */
	protected RMetaGoal(String name, IMMetaGoal goal, IMConfigGoal state, RElement owner,
			RReferenceableElement creator, Map binding)
	{
		super(name, goal, state, owner, creator, binding);
	}
	
	//-------- methods --------
	
	/**
	 *  Get an encodable representation of the metagoal.
	 */
	public Map getEncodableRepresentation()
	{
		Map	ret	= super.getEncodableRepresentation();
		
		IRElement	trigger	= getTrigger();
		ret.put("trigger", trigger.getName());
		ret.put("triggerscope", trigger.getScope().getDetailName());
		
		return ret;
	}
	
	/**
	 *  Get the triggering element (goal or event). 
	 */
	public IRElement	getTrigger()
	{
		// Hack!!! Todo: store event directly.
		ICandidateInfo	cand	= (ICandidateInfo)getParameterSet("applicables").getValues()[0];
		IRElement	trigger	= ((ElementWrapper)cand.getEvent()).unwrap();
		if(trigger instanceof IRGoalEvent)
		{
			trigger	= ((IRGoalEvent)trigger).getGoal().getOriginalElement();
		}
		return trigger;
	}
}
