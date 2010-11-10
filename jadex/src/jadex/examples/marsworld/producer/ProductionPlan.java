package jadex.examples.marsworld.producer;

import jadex.examples.marsworld.*;
import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  The main plan for the Production Agent. <br>
 *  first the Agent waits for an incomming request.
 *  It can be called to move home or to a given location.
 *  Being called to a location it will dispatch a subgoal to produce
 *  the ore there look up available carry agents and call one to collect it.
 */
public class ProductionPlan extends Plan
{
	//-------- attributes --------

	protected int visited;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public ProductionPlan()
	{
		getLogger().info("Created: "+this);
		this.visited = 0;

		Environment env = ((Environment)getBeliefbase().getBelief("environment").getFact());
		env.setAgentInfo(new AgentInfo(getAgentName(),
			(String)getBeliefbase().getBelief("my_type").getFact(), (Location)getBeliefbase()
			.getBelief("my_home").getFact(),((Double)getBeliefbase().getBelief("my_vision").getFact()).doubleValue()));
	}

	//-------- methods --------

	/**
	 *  Method body.
	 */
	public void body()
	{
		while(true)
		{
			// Wait for a request.
			IMessageEvent req = waitForMessageEvent("request_production");

			Target ot = ((RequestProduction)req.getContent()).getTarget();
			Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
			Target target = env.getTarget(ot.getId());

			// Producing ore here.
			IGoal produce_ore = createGoal("produce_ore");
			produce_ore.getParameter("target").setValue(target);
			dispatchSubgoalAndWait(produce_ore);

			//System.out.println("Production of ore has finished....");
			//System.out.println("Calling Carry Agent....");
			callCarryAgent(target);

			/*RGoal go_home = createGoal("move_dest");
			go_home.getParameter("destination", getBeliefbase().getBelief("???").getFact("my_home"));
			RGoalEvent ev_home = dispatchSubgoalAndWait(go_home);*/
		}
	}

	/**
	 * Sending a locaton to the Production Agent.
	 * Therefore it has first to be looked up in the DF.
	 * @param target
	 */
	private void callCarryAgent(Target target)
	{
		// Create a service description to search for.
		ServiceDescription sd = new ServiceDescription("service_carry", null, null);
		AgentDescription dfadesc = new AgentDescription();
		dfadesc.addService(sd);

		// a hack - default is 2! to reach more Agents, we have
		// to increase the number of possible results.
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(-1);

		// Use a subgoal to search
		IGoal ft = createGoal("df_search");
		ft.getParameter("description").setValue(dfadesc);
		ft.getParameter("constraints").setValue(constraints);

		dispatchSubgoalAndWait(ft);
		//Object result = ft.getResult();
		AgentDescription[] carriers = (AgentDescription[])ft.getParameterSet("result").getValues();

		if(carriers.length>0)
		{
			//System.out.println("Carry Agent: Found Carry Agents: "+carriers.length);

			RequestCarry rc = new RequestCarry();
			rc.setTarget(target);
			//Action action = new Action();
			//action.setAction(rc);
			//action.setActor(new AID("dummy", true)); // Hack!! What to do with more than one receiver?
			IMessageEvent mevent = createMessageEvent("request_carries");
				for(int i=0; i<carriers.length; i++)
				mevent.getParameterSet(SFipa.RECEIVERS).addValue(carriers[i].getName());
			mevent.setContent(rc);
			sendMessage(mevent);
			//System.out.println("Production Agent sent target to: "+carriers.length);
		}
	}
}
