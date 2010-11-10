package jadex.testcases.plans;

import jadex.runtime.ICandidateInfo;
import jadex.runtime.Plan;

/**
 *  Simple meta-level reasoning plan to select among candidates.
 */
public class MLRPlan extends Plan
{
	//-------- methods --------

	/**
	 * The plan body.
	 */
	public void body()
	{
		ICandidateInfo[] apps = (ICandidateInfo[])getParameterSet("applicables").getValues();
		getLogger().info("Meta-level reasoning selects: " + apps[0]);
		getParameterSet("result").addValue(apps[0]);
	}
}
