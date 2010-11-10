package jadex.examples.marsworld.carrier;

import jadex.examples.marsworld.Target;
import jadex.runtime.*;
import jadex.adapter.fipa.*;
import jadex.util.SUtil;

/**
 *  Inform the sentry agent about a new target.
 */
public class InformNewTargetPlan extends Plan
{
	//-------- attributes --------

	/** The target. */
	protected Target target;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 *  todo: make this work via binding
	 *  problem: Event does only contain string rep of new target
	 * /
	public InformNewTargetPlan(Target target)
	{
		getLogger().info("Created: "+this);
		this.target = target;
	}*/

	/**
	 *  Create a new plan.
	 */
	public InformNewTargetPlan()
	{
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Substract both sets.
		Target[] ts = (Target[])getBeliefbase().getBeliefSet("my_targets").getFacts();
		Target[] fts = (Target[])getBeliefbase().getBeliefSet("finished_targets").getFacts();
		Target[] res = (Target[])SUtil.substractArrays(ts, fts);

		for(int i=0; i<res.length; i++)
		{
			//System.out.println("Infoming sentry about a new target!!!");
			informSentryAgents(res[i]);
			getBeliefbase().getBeliefSet("finished_targets").addFact(res[i]);
		}
	}

	/**
	 *  Sending a locaton to the Production Agent.
	 *  Therefore it has first to be looked up in the DF.
	 *  @param target
	 */
	private void informSentryAgents(Target target)
	{
		//System.out.println("Informing all sentry agents.");

		// Search for Production_Service
		// Create a service description to search for.
		ServiceDescription sd = new ServiceDescription("service_sentry", null, null);
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
		AgentDescription[] sentries = (AgentDescription[])ft.getParameterSet("result").getValues();

		if(sentries.length>0)
		{
			//InformTarget it = new InformTarget();
			//it.setTarget(target);
			//Action action = new Action();
			//action.setAction(it);
			//action.setActor(new AID("dummy", true)); // Hack!! What to do with more than one receiver?
			IMessageEvent mevent = createMessageEvent("inform_target");
			for(int i=0; i<sentries.length; i++)
				mevent.getParameterSet(SFipa.RECEIVERS).addValue(sentries[i].getName());
			mevent.setContent(target);
			sendMessage(mevent);
			//System.out.println("Informing sentries: "+getScope().getPlatformAgent().getLocalName());
		}
	}
}
