package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.adapter.fipa.*;
import jadex.examples.cleanerworld.multi.CleanerOntology;
import jadex.runtime.*;

import java.util.Random;


/**
 *  Offers a methods for interacting with the cleanerworld environment.
 */
public abstract class RemoteActionPlan extends MobilePlan
{
	/**
	 *  Request an action from the environment. 
	 */
	public void	action(IEvent event)
	{
		// Search  the environment agent.
		AgentIdentifier	env	= (AgentIdentifier)getBeliefbase().getBelief("environmentagent").getFact();
		if(env==null && event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			// Create a service description to search for.
			ServiceDescription sd = new ServiceDescription();
			sd.setType("dispatch vision");
			AgentDescription dfadesc = new AgentDescription();
			dfadesc.addService(sd);
			SearchConstraints	cons	= new SearchConstraints();
			cons.setMaxResults(-1);
				
			// Use a subgoal to search for a translation agent
			IGoal ft = createGoal("df_search");
			ft.getParameter("description").setValue(dfadesc);
			ft.getParameter("constraints").setValue(cons);
			if(getBeliefbase().containsBelief("df"))
				ft.getParameter("df").setValue(getBeliefbase().getBelief("df").getFact());
			dispatchSubgoalAndWait(ft);
		}
		
		// Store the environment agent and request the action.
		else if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo()
			|| event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("df_search"))
		{
			if(((IGoalEvent)event).getGoal().getType().equals("df_search"))
			{
				AgentDescription[] tas = (AgentDescription[])((IGoalEvent)event).getGoal().getParameterSet("result").getValues();
	
				if(tas.length>0)
				{
					// Use random environment. (todo: prefer environment(s) on local container)
					env	= tas[new Random().nextInt(tas.length)].getName();
					getBeliefbase().getBelief("environmentagent").setFact(env);
					System.out.println("found "+tas.length+" environments.");
				}
				else
				{
					// Not found.
					throw new PlanFailureException();
				}
			}

			IGoal rg = createGoal("rp_initiate");
			rg.getParameter("receiver").setValue(env);
			rg.getParameter("action").setValue(getAction());
			rg.getParameter("ontology").setValue(CleanerOntology.ONTOLOGY_NAME);
			//rg.getParameter("language").setValue(SFipa.NUGGETS_XML);
			dispatchSubgoalAndWait(rg);
		}
		
		// Handle the result.
		else if(event instanceof IGoalEvent && ((IGoalEvent)event).getGoal().getType().equals("rp_initiate"))
		{
			handleResult(((IGoalEvent)event).getGoal().getParameter("result").getValue());
		}
	}

	/**
	 *  When the plan has failed, assume that environment is down.
	 *  Remove fact to enable new search for environment.
	 */
	public void failed(IEvent event)
	{
		// Received a timeout. Probably the environment agent has died.
		getBeliefbase().getBelief("environmentagent").setFact(null);
	}

	//-------- template methods --------
	
	/**
	 *  Return the action to be requested.
	 */
	protected abstract AgentAction	getAction();
	
	/**
	 *  Handle the result (if any).
	 */
	protected void	handleResult(Object result)
	{
		// Dummy implementation.
	}
}
