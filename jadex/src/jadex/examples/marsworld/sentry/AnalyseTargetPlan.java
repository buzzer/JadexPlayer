package jadex.examples.marsworld.sentry;

import jadex.examples.marsworld.*;
import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Analyse a target.
 */
public class AnalyseTargetPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public AnalyseTargetPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Target target = (Target)getParameter("target").getValue();

		// Move to the target.
		IGoal go_target = createGoal("move_dest");
		go_target.getParameter("destination").setValue(target.getLocation());
		dispatchSubgoalAndWait(go_target);

		// Analyse the target.
		waitFor(1000);
		target.setMarked();
		if(target.getOreCapacity()>0)
			callProductionAgent(target);

		startAtomic();
		getBeliefbase().getBeliefSet("analysed_targets").addFact(target);
		getBeliefbase().getBeliefSet("my_targets").removeFact(target);
		endAtomic();
	}

	/**
	 *  Sending a locaton to the Production Agent.
	 *  Therefore it has first to be looked up in the DF.
	 *  @param target
	 */
	private void callProductionAgent(Target target)
	{
		//System.out.println("Calling some Production Agent...");

		// Search for Production_Service
		// Create a service description to search for.
		ServiceDescription sd = new ServiceDescription("service_produce", null, null);
		AgentDescription dfadesc = new AgentDescription();
		dfadesc.addService(sd);

		// A hack - default is 2! to reach more Agents, we have
		// to increase the number of possible results.
		SearchConstraints constraints = new SearchConstraints();
		constraints.setMaxResults(-1);

		// Use a subgoal to search
		IGoal ft = createGoal("df_search");
		ft.getParameter("description").setValue(dfadesc);
		ft.getParameter("constraints").setValue(constraints);

		dispatchSubgoalAndWait(ft);
		//Object result = ft.getResult();
		AgentDescription[] producers = (AgentDescription[])ft.getParameterSet("result").getValues();

		if(producers.length>0)
		{
			int sel = (int)(Math.random()*(double)producers.length); // todo: Select not randomly
			//System.out.println("Found agents: "+producers.length+" selected: "+sel);

			RequestProduction rp = new RequestProduction();
			rp.setTarget(target);
			//Action action = new Action();
			//action.setAction(rp);
			//action.setActor(SJade.convertAIDtoJade(producers[sel].getName()));
			IMessageEvent mevent = createMessageEvent("request_producer");
			mevent.getParameterSet(SFipa.RECEIVERS).addValue(producers[sel].getName());
			mevent.setContent(rp);
			sendMessage(mevent);
			//System.out.println("Sentry Agent: sent location to: "+production);
		}
	}
}
