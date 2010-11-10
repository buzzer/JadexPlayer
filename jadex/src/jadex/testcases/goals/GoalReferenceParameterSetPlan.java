package jadex.testcases.goals;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;

/**
 *  Test parameter sets in referenced goals.
 */
public class GoalReferenceParameterSetPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// Test getting a value when parameter set is unmodified.
		TestReport	report	= new TestReport("get_unmodifed", "Test getting a value when parameter set is unmodified");
		try
		{
			getParameterSet("paramset").getValue("testvalue 1");
			report.setSucceeded(true);
		}
		catch(RuntimeException e)
		{
			report.setFailed(e.toString());
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		// Test adding a value.
		report	= new TestReport("add", "Test adding a value");
		try
		{
			getParameterSet("paramset").addValue("result");
			report.setSucceeded(true);
		}
		catch(RuntimeException e)
		{
			report.setFailed(e.toString());
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		// Test getting a value when parameter set is modified.
		report	= new TestReport("get_modifed", "Test getting a value when parameter set is modified");
		try
		{
			getParameterSet("paramset").getValue("testvalue 1");
			report.setSucceeded(true);
		}
		catch(RuntimeException e)
		{
			report.setFailed(e.toString());
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
		
		
		// Test removing a value.
		report	= new TestReport("remove", "Test removing a value");
		try
		{
			getParameterSet("paramset").removeValue("testvalue 1");
			report.setSucceeded(true);
		}
		catch(RuntimeException e)
		{
			report.setFailed(e.toString());
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}
