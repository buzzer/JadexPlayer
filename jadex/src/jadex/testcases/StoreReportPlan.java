package jadex.testcases;

import jadex.planlib.TestReport;
import jadex.runtime.*;

/**
 *  This plan stores a test report in the reports belief set.
 */
public class StoreReportPlan extends Plan
{
	//-------- attributes --------

	/** The test report. */
	protected TestReport	report;
	
	//-------- constructors --------

	/**
	 *  Create a new result plan.
	 *  @param report The result value.
	 */
	public StoreReportPlan(TestReport report)
	{
		this.report	= report;
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
