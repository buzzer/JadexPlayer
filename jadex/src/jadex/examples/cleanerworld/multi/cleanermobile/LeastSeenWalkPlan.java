package jadex.examples.cleanerworld.multi.cleanermobile;

import java.util.List;
import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;


/**
 *  Walk to the least visited positions.
 *  Uses a relative measure to go to seldom seen positions.
 */
public class LeastSeenWalkPlan extends MobilePlan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public LeastSeenWalkPlan()
	{
		getLogger().info("Created: "+this+" for goal "+getRootGoal());
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			// Select randomly one of the least seen locations.
			List	mps = (List)getExpression("query_min_seen").execute();
			MapPoint mp = (MapPoint)mps.get(0);
			int cnt	= 1;
			for( ; cnt<mps.size(); cnt++)
			{
				MapPoint mp2 = (MapPoint)mps.get(cnt);
				if(mp.getSeen()!=mp2.getSeen())
					break;
			}
			mp	= (MapPoint)mps.get((int)(Math.random()*cnt));
			Location dest = mp.getLocation();
	
			IGoal moveto = createGoal("achievemoveto");
			moveto.getParameter("location").setValue(dest);
			dispatchSubgoalAndWait(moveto);
		}
	}
}
