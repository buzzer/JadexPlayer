package jadex.testcases.misc;

import jadex.planlib.TestReport;
import jadex.runtime.Plan;
import jadex.model.*;

/**
 *  Test a dynamically added trigger.
 */
public class DynamicTriggerPlan extends Plan
{

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		TestReport tr = new TestReport("#1", "Create a dynamic plan and test if it can be triggerd.");

		try
		{
			IMPlanbase pmodel = (IMPlanbase)getPlanbase().getModelElement();
			IMPlan beliefcatch = pmodel.createPlan("catchBeliefChange", 0, "new BeliefChangeCatchPlan()", IMPlanBody.BODY_STANDARD);
			beliefcatch.createTrigger().createBeliefChange("test");
			getPlanbase().registerPlan(beliefcatch);
			getLogger().info("Plan created");
			//waitFor(2000);
			getBeliefbase().getBelief("test").setFact(new Boolean(true));
			getLogger().info("Fact Changed");
			waitFor(300);
			
			if(((Boolean)getBeliefbase().getBelief("invoked").getFact()).booleanValue())
				tr.setSucceeded(true);
			else
				tr.setReason("Plan was not invoked.");
		}
		catch(Exception e)
		{
			tr.setReason(e.getMessage());
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
