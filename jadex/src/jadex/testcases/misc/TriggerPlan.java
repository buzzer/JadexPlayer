package jadex.testcases.misc;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;

/**
 *  A plan to be triggered by conditions
 *  of the repeatability test agent.
 */
public class TriggerPlan extends Plan
{
	/** The number, when the plan should be triggered. */
	private int	no;

	/** A description of the test case. */
	private String	description;

	/**
	 *  Create a new trigger plan.
	 *  @param no	The number, when the plan should be triggered.
	 *  @param	description	A description of the test case.
	 */
	public TriggerPlan(int no, String description)
	{
		this.no	= no;
		this.description	= description;
	}
	
	/**
	 *  The plan code.
	 */
	public void body()
	{
		TestReport	report	= new TestReport(getName(), description);
		int	cnt	= ((Number)getBeliefbase().getBelief("cnt").getFact()).intValue();
		cnt++;
		if(cnt==no)
		{
			report.setSucceeded(true);
		}
		else
		{
			report.setFailed("Wrong execution order: Was "+no+" but should be "+cnt+".");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		getBeliefbase().getBelief("cnt").setFact(new Integer(cnt));
	}
}
