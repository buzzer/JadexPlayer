package jadex.testcases.semiautomatic;

import jadex.runtime.IGoal;
import jadex.runtime.Plan;

/**
 *  This plan performs an illegal action. 
 */
public class ErrorPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
//		IGoal	goal	= getExternalAccess().createGoal("testgoal");
//		getExternalAccess().dispatchTopLevelGoal(goal);
//		getExternalAccess().dispatchTopLevelGoal(goal);

		IGoal	goal	= createGoal("testgoal");
		dispatchTopLevelGoal(goal);
		dispatchTopLevelGoal(goal);
	
	}
}
