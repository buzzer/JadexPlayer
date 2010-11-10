package jadex.examples.marsworld.carrier;

import jadex.examples.marsworld.*;
import jadex.runtime.*;

/**
 *  This is the main plan for the different Carry Agents.
 *  It waits for an incomming request, extracts the sended location
 *  and dispatches a new (sub) Goal to carry the ore.
 *  Finally a subgoal is created to return home.
 */
public class CarryPlan extends Plan
{
	//-------- constructors--------

	/**
	 *  Create a new plan.
	 */
	public CarryPlan()
	{
		getLogger().info("Created: "+this);
		Environment env = ((Environment)getBeliefbase().getBelief("environment").getFact());
		env.setAgentInfo(new AgentInfo(getAgentName(),
			(String)getBeliefbase().getBelief("my_type").getFact(), (Location)getBeliefbase()
			.getBelief("my_home").getFact(), ((Double)getBeliefbase().getBelief("my_vision")
			.getFact()).doubleValue()));
	}

	//-------- methods --------

	/**
	 *  Plan body.
	 */
	public void body()
	{
		while(true)
		{
			// Wait for a request to carry.
			IMessageEvent req = waitForMessageEvent("request_carry");

			Target ot = ((RequestCarry)req.getContent()).getTarget();
			Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
			Target target = env.getTarget(ot.getId());
			Location dest = target.getLocation();

			IGoal go_carry = createGoal("carry_ore");
			go_carry.getParameter("destination").setValue(dest);
			dispatchSubgoalAndWait(go_carry);
		}
	}
}