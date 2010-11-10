package jadex.testcases.misc;

import jadex.runtime.*;
import jadex.model.*;
import jadex.planlib.TestReport;

/**
 *  This plan dynamically creates a belief, goal and a plan.
 *  It then changes the belief value, which triggers the creation
 *  condition of the goal, leading to plan execution.
 */
public class DynamicModelCreationPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		IMCapability mcapa = (IMCapability)getScope().getModelElement();

		getLogger().info("----------------------------------");
		TestReport tr = new TestReport("#1", "Create a new belief, goal and plan model " +
			"in the agent and test if the plan triggers.");
		getLogger().info("Testcase 1: Create a new belief, goal and plan model " +
			"in the agent and test if the plan triggers.");
		// Creating a new belief model element.
		IMBelief mbel = mcapa.getBeliefbase().createBelief("new_belief_model", int.class, 0, IMReferenceableElement.EXPORTED_FALSE);
		getBeliefbase().registerBelief(mbel);
		getLogger().info("Created belief model at runtime: "+mbel+" "+getBeliefbase().getBelief("new_belief_model"));
		// Creating a new goal model element.
		IMAchieveGoal mgoal = mcapa.getGoalbase().createAchieveGoal("new_goal_model", IMReferenceableElement.EXPORTED_FALSE, true, 0, IMGoal.EXCLUDE_WHEN_FAILED);
		mgoal.createParameter("test_no", Integer.class, IMParameter.DIRECTION_INOUT, 0, "1", IMExpression.MODE_STATIC);
		mgoal.createCreationCondition("$beliefbase.new_belief_model>0");
		getGoalbase().registerGoal(mgoal);
		getLogger().info("Created goal model at runtime: "+mgoal);
		// Creating a new plan model element.
		IMPlan mplan = mcapa.getPlanbase().createPlan("new_plan_model", 0,
			"new jadex.testcases.ResultPlan($plan.pp)", IMPlanBody.BODY_STANDARD);
			//"getLogger().info(\"Result: \"+getParameter(\"pp\").getValue());", IMPlanBody.BODY_STANDARD, null, null);
		//mplan.getBody().setInline(true);
		IMPlanTrigger mtrig = mplan.createTrigger();
		mtrig.createGoal("new_goal_model");
		mplan.createPlanParameter("pp", Integer.class, IMParameter.DIRECTION_INOUT, 0, null,
			null, null, null, new String[]{"new_goal_model.test_no"});
		getPlanbase().registerPlan(mplan);
		getLogger().info("Created plan model at runtime: "+mplan);
		getBeliefbase().getBelief("new_belief_model").setFact(new Integer(1));
		IGoal goal = createGoal("new_goal_model");
		goal.getParameter("test_no").setValue(new Integer(2));
		try
		{
			dispatchSubgoalAndWait(goal);
			getLogger().info("Test succeeded: subgoal succeeded!");
			tr.setSucceeded(true);
		}
		catch(GoalFailureException e)
		{
			getLogger().info("Test failed: subgoal failed.");
			tr.setReason("Subgoal failed: "+e);
		}
		getLogger().info("----------------------------------");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#2", "Remove the plan and test if it still triggers.");
		getLogger().info("Testcase 2: Remove the plan and test if it still triggers.");
		// todo: delete from model
		getGoalbase().deregisterGoal(mgoal);
		getPlanbase().deregisterPlan(mplan);
		getBeliefbase().getBelief("new_belief_model").setFact(new Integer(2));
		getBeliefbase().deregisterBelief(mbel);
		mcapa.getGoalbase().deleteAchieveGoal(mgoal);
		mcapa.getPlanbase().deletePlan(mplan);
		mcapa.getBeliefbase().deleteBelief(mbel);
		try
		{
			goal = createGoal("new_goal_model");
			goal.getParameter("test_no").setValue(new Integer(2));
			dispatchSubgoalAndWait(goal);
			getLogger().info("Test failed: subgoal failed.");
			tr.setReason("Subgoal failed.");
		}
		catch(Exception e)
		{
			getLogger().info("Test succeeded.");
			tr.setSucceeded(true);
		}
		getLogger().info("----------------------------------");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#3", "Create a goal reference and dispatch a goal that is handled in another capability.");
		getLogger().info("Testcase 3: Create a goal reference and dispatch a goal that is handled in another capability.");
		IMAchieveGoalReference mgoalref = mcapa.getGoalbase().createAchieveGoalReference(
			"new_goal_ref", IMReferenceableElement.EXPORTED_FALSE, "dyncapa.capagoal");
		mgoalref.createParameterReference("param", String.class);
		getGoalbase().registerGoalReference(mgoalref);
		getLogger().info("Created goal reference model at runtime: "+mgoalref);
		goal = createGoal("new_goal_ref");
		goal.getParameter("param").setValue("The goal ref value.");
		try
		{
			dispatchSubgoalAndWait(goal);
			getLogger().info("Test succeeded: subgoal succeeded!");
			tr.setSucceeded(true);
		}
		catch(GoalFailureException e)
		{
			getLogger().info("Test failed: subgoal failed.");
			tr.setReason("Subgoal failed: "+e);
		}
		getLogger().info("----------------------------------");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#4", "Create an event reference and dispatch an event that is handled in another capability.");
		getLogger().info("Testcase 4: Create an event reference and dispatch an event that is handled in another capability.");
		IMInternalEventReference meventref = mcapa.getEventbase().createInternalEventReference(
			"new_event_ref", IMReferenceableElement.EXPORTED_FALSE, "dyncapa.capaevent", false);
		try
		{
			meventref.createParameterReference("param", String.class);
			getEventbase().registerEventReference(meventref);
			getLogger().info("Created event reference model at runtime: "+meventref);
			IInternalEvent event = createInternalEvent("new_event_ref");
			event.getParameter("param").setValue("The event ref value.");
			dispatchInternalEvent(event);
			tr.setSucceeded(true);
		}
		catch(Exception e)
		{
			tr.setReason("Exception occurred: "+e);
		}
		getLogger().info("----------------------------------");
		getBeliefbase().getBeliefSet("reports").addFact(tr);

		tr = new TestReport("#5", " Deregister and remove everything created.");
		getLogger().info("Testcase 5: Deregister and remove everything created.");
		try
		{
			getGoalbase().deregisterGoalReference(mgoalref);
			mcapa.getGoalbase().deleteAchieveGoalReference(mgoalref);
			getEventbase().deregisterEventReference(meventref);
			mcapa.getEventbase().deleteInternalEventReference(meventref);
			tr.setSucceeded(true);
		}
		catch(Exception e)
		{
			// todo: test if model is clean
			tr.setReason("Exception occurred: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
}
