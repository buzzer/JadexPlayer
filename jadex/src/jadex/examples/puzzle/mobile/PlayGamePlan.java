package jadex.examples.puzzle.mobile;

import javax.swing.SwingUtilities;

import jadex.examples.puzzle.IBoard;
import jadex.runtime.*;

/**
 *  Play the game until a solution is found.
 */
public class PlayGamePlan extends MobilePlan
{
	long start	= 0;

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		if(!(event instanceof IGoalEvent))
		{
			System.out.println("Now puzzling:");
			start = System.currentTimeMillis();
			IGoal play = createGoal("makemove");
			play.getParameter("depth").setValue(new Integer(0));
			dispatchSubgoalAndWait(play);
		}
		else
		{
			IBoard board = (IBoard)getBeliefbase().getBelief("board").getFact();
			long end = System.currentTimeMillis();
//			if(((IGoalEvent)event).getGoal().isSucceeded())
//				System.out.println("Found a solution: "
//				+board.getMoves().size()+" "+board.getMoves());
			System.out.println("Needed: "+(end-start)+" millis.");
			killAgent();
		}
	}
}
