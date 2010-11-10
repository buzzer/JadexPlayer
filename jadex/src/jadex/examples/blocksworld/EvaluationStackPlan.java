package jadex.examples.blocksworld;

import jadex.runtime.Plan;

/**
 *  Stack a block on top of another.
 *  Fast version for evaluation.
 */
public class EvaluationStackPlan	extends Plan
{
	//-------- attributes --------

	/** The block to be moved. */
	protected Block	block;

	/** The block on to which to put the other block. */
	protected Block	target;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public EvaluationStackPlan()
	{
		this.block	= (Block)getParameter("block").getValue();
		this.target	= (Block)getParameter("target").getValue();
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// This operation has to be performed atomic,
		// because it fires bean changes on several affected blocks. 
		startAtomic();
		block.stackOn(target);
		endAtomic();
	}
}
