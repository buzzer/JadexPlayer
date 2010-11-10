package jadex.examples.puzzle;

import jadex.runtime.*;

/**
 *  Make a move and dispatch a subgoal for the next.
 */
public class BenchmarkMovePlan extends Plan
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
	public void body()
	{
		int triescnt = ((Integer)getBeliefbase().getBelief("triescnt").getFact()).intValue()+1;
		getBeliefbase().getBelief("triescnt").setFact(new Integer(triescnt));
		print("Trying "+move+" ("+triescnt+") ", depth);
		IBoard board = (IBoard)getBeliefbase().getBelief("board").getFact();
		board.move(move);

		if(!board.isSolution()) // Comment out this line when using goal target condition in the adf.
		{
			IGoal mm = createGoal("makemove");
			mm.getParameter("depth").setValue(new Integer(depth+1));
			dispatchSubgoalAndWait(mm);
		}
	}

	/**
	 *  The plan failure code.
	 */
	public void failed()
	{
		print("Failed "+move, depth);
		IBoard board = (IBoard)getBeliefbase().getBelief("board").getFact();
		assert board.getLastMove().equals(move): "Tries to takeback wrong move.";
		board.takeback();
	}

	/**
	 *  The plan passed code.
	 */
	public void passed()
	{
		print("Succeeded "+move, depth);
	}

	/**
	 *  The plan aborted code.
	 */
	public void aborted()
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
