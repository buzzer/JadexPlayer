package jadex.examples.garbagecollector;

import jadex.runtime.*;

/**
 *  Check the grid for garbage.
 */
public class CheckingPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Environment env = (Environment)getBeliefbase().getBelief("env").getFact();
		int size = env.getGridSize();
		Position mypos = (Position)getBeliefbase().getBelief("pos").getFact();
		Position newpos = computeNextPosition(mypos, size);

		IGoal go = createGoal("go");
		go.getParameter("pos").setValue(newpos);
		dispatchSubgoalAndWait(go);
	}

	/**
	 *  Compute the next position.
	 */
	protected static Position computeNextPosition(Position pos, int size)
	{
		if(pos.getX()+1<size && pos.getY()%2==0)
		{
			pos = new Position(pos.getX()+1, pos.getY());
		}
		else if(pos.getX()-1>=0 && pos.getY()%2==1)
		{
			pos = new Position(pos.getX()-1, pos.getY());
		}
		else
		{
			pos = new Position(pos.getX(), (pos.getY()+1)%size);
		}

		return pos;
	}

	/*public static void main(String[] args)
	{
		Position pos = new Position(0,0);
		while(true)
		{
			System.out.println(pos);
			pos = computeNextPosition(pos, 5);
		}
	}*/
}
