package jadex.examples.hunterprey.creature.preys.basicbehaviour;

import jadex.examples.hunterprey.*;
import jadex.runtime.*;
import jadex.util.SUtil;

/**
 *  Try to go to a specified location.
 */
/*  @handles goal goto_location
 *  @requires belief my_self
 */
public class GotoLocationPlan extends Plan
{
	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Location target = (Location)getParameter("location").getValue();
		Creature me = (Creature)getBeliefbase().getBelief("my_self").getFact();
		//System.out.println("goto: "+target);	
		
		while(!me.getLocation().equals(target))
		{
	        WorldObject[] obs = ((Vision)getBeliefbase().getBelief("vision").getFact()).getObjects();
	        String[] dirs = me.getDirections(me.getLocation(), target);
		    String[] posdirs = me.getPossibleDirections(obs);
	        String[] posmoves = (String[])SUtil.cutArrays(dirs, posdirs);
		    
	        boolean success = false;
			// Try different possible directions towards target.
	        for(int i=0; i<posmoves.length && !success; i++)
	        {
		        IGoal move = createGoal("move");
		        move.getParameter("direction").setValue(posmoves[i]);
		        try
				{
		        	dispatchSubgoalAndWait(move);
		            me = (Creature)getBeliefbase().getBelief("my_self").getFact();
		            success = true;
				}
		        catch(GoalFailureException gfe){}
		    }
		    if(!success)
		    {
				throw new PlanFailureException();
		        //System.out.println("Cannot reach location :-(");
		    }
		}
	}

}
