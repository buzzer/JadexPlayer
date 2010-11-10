package jadex.examples.puzzle.mobile;

import jadex.examples.puzzle.*;
import jadex.runtime.*;

/**
 *  Make a move and dispatch a subgoal for the next.
 */
public class BenchmarkMovePlan extends MobilePlan
{
	//-------- attributes --------

	/** The move to try. */
	protected Move move;

	/** The recusion depth. */
	protected int depth;

	//-------- constrcutors --------

	/**
	 *  Create a new move plan.
	 */
	public BenchmarkMovePlan()
	{
		this.move = (Move)getParameter("move").getValue();
		this.depth = ((Integer)getParameter("depth").getValue()).intValue();
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		IBoard board = (IBoard)getBeliefbase().getBelief("board").getFact();

		// Process move goal.
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			int triescnt = ((Integer)getBeliefbase().getBelief("triescnt").getFact()).intValue()+1;
			getBeliefbase().getBelief("triescnt").setFact(new Integer(triescnt));
			print("Trying "+move+" ("+triescnt+") ", depth);
			board.move(move);

			if(!board.isSolution()) // This works, but does not use the target condition :-(
			{
				IGoal mm = createGoal("makemove");
				mm.getParameter("depth").setValue(new Integer(depth+1));
				dispatchSubgoalAndWait(mm);
			}
		}
		
		// else dispatched subgoal succeeded (nothing to do)
	}

	/**
	 *  The plan failure code.
	 */
	public void failed(IEvent event)
	{
		print("Failed "+move, depth);
		IBoard board = (IBoard)getBeliefbase().getBelief("board").getFact();
		assert board.getLastMove().equals(move): "Tries to takeback wrong move.";
		board.takeback();
	}

	/**
	 *  The plan passed code.
	 */
	public void passed(IEvent event)
	{
		print("Succeeded "+move, depth);
	}

	/**
	 *  The plan aborted code.
	 */
	public void aborted(IEvent event)
	{
		print("Aborted "+(isAbortedOnSuccess()?
			"on success: ": "on failure: ")+move, depth);
	}

	/**
	 *  Print out an indented string.
	 *  @param text The text.
	 *  @param indent The number of cols to indent.
	 */
	protected void print(String text, int indent)
    {
        for(int x=0; x<indent; x++)
            System.out.print(" ");
        System.out.println(text);
    }
}
