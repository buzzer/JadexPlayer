package jadex.model.jibximpl;

import java.util.List;
import jadex.model.*;

/**
 *  The reference for a goal.
 */
public abstract class MGoalReference extends MParameterElementReference implements IMGoalReference
{
	//-------- attributes --------

	protected MDeliberation deliberation;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(deliberation!=null)
			ret.add(deliberation);
		return ret;
	}

	//-------- deliberation --------

	/**
	 *  Get the deliberation properties of the goal (if any).
	 *  @return The deliberation properties.
	 */
	public IMDeliberation	getDeliberation()
	{
		return this.deliberation;
	}

	/**
	 *  Create new the deliberation properties for the goal.
	 *  @param cardinality	The cardinality (i.e. number of concurrently active goals) of this type.
	 *  @return The deliberation properties.
	 */
	public IMDeliberation	createDeliberation(int cardinality)
	{
		this.deliberation = new MDeliberation();
		deliberation.setCardinality(cardinality);
		deliberation.setOwner(this);
		deliberation.init();
		return deliberation;
	}

	/**
	 *  Delete the deliberation properties of the goal.
	 */
	public void	deleteDeliberation()
	{
		deliberation = null;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MGoalReference clone = (MGoalReference)cl;
		if(deliberation!=null)
			clone.deliberation = (MDeliberation)deliberation.clone();
	}
}
