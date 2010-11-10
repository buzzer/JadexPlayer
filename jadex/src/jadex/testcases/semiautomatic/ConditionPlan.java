package jadex.testcases.semiautomatic;

import jadex.runtime.ICondition;
import jadex.runtime.Plan;

/**
 *  Plan to continuously wait for a condition.
 */
public class ConditionPlan extends Plan
{
	public void body()
	{
		ICondition	condition	= getCondition("five_seconds");
				
		while(true)
		{
			System.out.println("waiting...");
			waitForCondition(condition);
			System.out.println("hier");
		} 
	}
}
