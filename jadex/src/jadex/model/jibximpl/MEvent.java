package jadex.model.jibximpl;

import jadex.model.*;

import java.util.List;

/**
 *  The base class for events.
 */
public abstract class MEvent extends MParameterElement implements IMEvent
{
	//-------- xml attributes --------

	/** The posttoall flag. */
	protected Boolean posttoall;

	/** The random selection flag. */
	protected boolean randomselection	= false;

	//-------- event flag methods --------

	/**
	 *  Get the posttoall state.
	 *  @return True, if event is posttoall.
	 */
	public boolean isPostToAll()
	{
		return this.posttoall.booleanValue();
	}

	/**
	 *  Set the posttoall state.
	 *  @param posttoall The state.
	 */
	public void setPostToAll(boolean posttoall)
	{
		this.posttoall = new Boolean(posttoall);
	}

	/**
	 *  Get the random selection state.
	 *  @return True, if random selection is on.
	 */
	public boolean isRandomSelection()
	{
		return this.randomselection;
	}

	/**
	 *  Set the random selection state.
	 *  @param rs The state;
	 */
	public void setRandomSelection(boolean rs)
	{
		this.randomselection = rs;
	}

	//-------- methods --------

	/**
	 *  Get the expression parameters.
	 *  If this element has no local parameters, will return
	 *  the parameters of the owner, or null if the element
	 *  has no owner.
	 */
	public List	getSystemExpressionParameters()
	{
		List copy = super.getSystemExpressionParameters();
		copy.add(new ExpressionParameterInfo("$event", this, "jadex.runtime.impl.IREvent"));
		return copy;
	}
}
