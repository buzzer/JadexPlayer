package jadex.examples.blocksworld;

import java.util.*;
import jadex.runtime.*;


/**
 *  Stack blocks according to the target configuration.
 */
public class ConfigureBlocksPlan	extends Plan
{
	//-------- attributes --------

	/** The desired target configuration. */
	protected Table	configuration;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public ConfigureBlocksPlan()
	{
		this.configuration	= (Table)getParameter("configuration").getValue();
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Create set of blocks currently on the table.
		Table	table	= (Table)getBeliefbase().getBelief("table").getFact();
		Block[]	blocks	= table.getAllBlocks();
		Set	oldblocks	= new HashSet();
		for(int i=0; i<blocks.length; i++)
			oldblocks.add(blocks[i]);

		// Move all blocks in configuration to desired target location.
		Block[][]	stacks	= configuration.getStacks();
		for(int i=0; i<stacks.length; i++)
		{
			for(int j=0; j<stacks[i].length; j++)
			{
				// Get blocks from beliefs.
				Block	block	= (Block)getBeliefbase().getBeliefSet("blocks").getFact(stacks[i][j]);
				Block	target	= stacks[i][j].getLower()==configuration ? table
					: (Block)getBeliefbase().getBeliefSet("blocks").getFact(stacks[i][j].getLower());
	
				// Create stack goal.
				IGoal stack = createGoal("stack");
				stack.getParameter("block").setValue(block);
				stack.getParameter("target").setValue(target);
				dispatchSubgoalAndWait(stack);
	
				// Remove processed block from oldblocks.
				oldblocks.remove(block);
			}
		}

		// Move old blocks, which are not part of configuration, to bucket.
		Object	bucket	= getBeliefbase().getBelief("bucket").getFact();
		for(Iterator i=oldblocks.iterator(); i.hasNext(); )
		{
			// Create stack goal.
 			IGoal stack = createGoal("stack");
			stack.getParameter("block").setValue(i.next());
			stack.getParameter("target").setValue(bucket);
			dispatchSubgoalAndWait(stack);
		}
	}
}
