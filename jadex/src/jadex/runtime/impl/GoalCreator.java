package jadex.runtime.impl;

import jadex.model.*;

/**
 *  The creator for goal structures.
 */
public class GoalCreator implements IElementCreator
{
	//-------- attributes --------

	/** The goalbase. */
	protected RGoalbase goalbase;

	/** The goal type. */
	protected IMGoal goal;

	//-------- constructors --------

	/**
	 *  Create a new goal creator.
	 *  @param goalbase The goalbase.
	 *  @param goal The goal.
	 */
	public GoalCreator(RGoalbase goalbase, IMGoal goal)
	{
		this.goalbase = goalbase;
		this.goal = goal;
	}

	//-------- methods --------

	/**
	 *  Create a new original element.
	 *  @return The new original element.
	 */
	public RReferenceableElement create()
	{
		// Cast is ok, because only originals are created here.
		return (RReferenceableElement)goalbase.internalCreateGoal(null, goal, null, null, null);
	}

	/**
	 *  Create a new reference element.
	 *  @param ref The orig structure element (Only the scope!) is interesting.
	 *  @param creator The element to which the new one will be connected.
	 *  @return The new reference element.
	 */
	public RElementReference createReference(RReferenceableElement ref, RReferenceableElement creator)
	{
		RCapability targetscope = ref.getScope();
		RCapability creatorscope = creator.getScope();

		assert targetscope.getParent()==creatorscope
			|| creatorscope.getParent()==targetscope;

		IMReferenceableElement creatormodel = (IMReferenceableElement)creator.getModelElement();
		IMGoalReference targetmodel = null;
		boolean go_out = creatorscope.getParent()==targetscope;
		if(go_out)
		{
			// The new element will be created in outer scope.
			// This means that a concrete ref is outside.
			// One reference has a suitable assignfrom element.
			IMGoalReference[] goalrefs = ((IMGoalbase)targetscope.getGoalbase()
				.getModelElement()).getGoalReferences();
			for(int i=0; i<goalrefs.length && targetmodel==null; i++)
			{
				if(goalrefs[i].getReferencedElement()==creatormodel)
				{
					targetmodel = goalrefs[i];
				}
			}
			assert targetmodel!=null;
		}
		else
		{
			// The new element will be created in a child capability.
			// This means that the new element was defined abstract.
			// One assingto of creator must match.
			IMElementReference[] goalrefs = creatormodel.getAssignToElements();
			for(int i=0; i<goalrefs.length && targetmodel==null; i++)
			{
				if(goalrefs[i].getScope()==targetscope)
				{
					targetmodel = (IMGoalReference)goalrefs[i];
				}
			}
			assert targetmodel!=null;
		}

		RElementReference	goalref = (RElementReference)targetscope.getGoalbase()
			.internalCreateGoal(null, targetmodel, null, creator, null);

		return goalref;
	}
}
