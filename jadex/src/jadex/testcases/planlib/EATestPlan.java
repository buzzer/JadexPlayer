package jadex.testcases.planlib;

import java.util.List;
import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.planlib.AuctionDescription;
import jadex.planlib.ExponentialPriceCalculator;
import jadex.planlib.TestReport;
import jadex.runtime.*;
import jadex.testcases.AbstractMultipleAgentsPlan;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;


/**
 *  Tests the english auction protocol with one initiator and four bidders with
 *  different price-strategies.
 */
public class EATestPlan extends AbstractMultipleAgentsPlan
{
	/**
	 * The body method is called on the instatiated plan instance from the
	 * scheduler.
	 */
	public void body()
	{
		// The round timeout, if the testcase fails, adjust it according to the
		// speed of your pc.
		long roundtimeout = 250;

		// Create 4 bidders
		Map[] args = new Map[4];
		//Map[] args = new Map[1];
		for(int i=0; i<args.length; i++)
		{
			args[i] = SCollection.createHashMap();
			args[i].put("max_price", new Double(115+i*5));
			//args[i].put("participate", new Boolean(i!=2));
			args[i].put("participate", Boolean.TRUE);
		}
		List agents = createAgents("jadex.testcases.planlib.EAReceiver", args);	

		TestReport tr = new TestReport("#1", "Test with four bidders. " +
			"Should terminate with a winner.");
		if(assureTest(tr))
		{
			try
			{
				IGoal ea = createGoal("ea_initiate");
				ea.getParameterSet("receivers").addValues(agents.toArray(new AgentIdentifier[agents.size()]));
				ea.getParameter("cfp").setValue(new Double(10));
				ea.getParameter("auction_description").setValue(new AuctionDescription(System.currentTimeMillis()+1000,
					roundtimeout, "Test auction 1"));
				ea.getParameter("limit").setValue(new Double(100));
				dispatchSubgoalAndWait(ea);
				Object[] res = (Object[])ea.getParameter("result").getValue();
				getLogger().info("Auction result: "+SUtil.arrayToString(res));
				if(ea.getParameter("result").getValue() != null)
				{
					tr.setSucceeded(true);
				}
				else
				{
					tr.setFailed("No winner determined though this testcase should terminate with a winner.");
				}
	
			}
			catch(GoalFailureException e)
			{
				tr.setFailed("Exception occurred: " + e);
			}
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		
		tr = new TestReport("#2", "Test with four bidders and offer generator. " +
			"Should terminate with a winner.");
		if(assureTest(tr))
		{
			try
			{
				IGoal ea = createGoal("ea_initiate");
				ea.getParameterSet("receivers").addValues(agents.toArray(new AgentIdentifier[agents.size()]));
					
				ExponentialPriceCalculator pc = new ExponentialPriceCalculator(10, 10000, 1.1);
			
				ea.getParameter("cfp").setValue(pc.getCurrentOffer());
				ea.getParameter("cfp_info").setValue(pc);
				
				ea.getParameter("auction_description").setValue(new AuctionDescription(System.currentTimeMillis()+1000,
					roundtimeout, "Test auction 1"));
				dispatchSubgoalAndWait(ea);
				Object[] res = (Object[])ea.getParameter("result").getValue();
				getLogger().info("Auction result: "+SUtil.arrayToString(res));
				if(ea.getParameter("result").getValue() != null)
				{
					tr.setSucceeded(true);
				}
				else
				{
					tr.setFailed("No winner determined though this testcase should terminate with a winner.");
				}
	
			}
			catch(GoalFailureException e)
			{
				tr.setFailed("Exception occurred: " + e);
			}
		}
		getBeliefbase().getBeliefSet("reports").addFact(tr);
		
		tr = new TestReport("#3", "Test if acceptance plan was invoked once.");
		int ac = ((Integer)getBeliefbase().getBelief("acceptplan_invoked").getFact()).intValue();
		if(ac==1)
			tr.setSucceeded(true);
		else
			tr.setReason("Acceptance plan was invoked: "+ac);
		
		// Add the last report, so that the testcase can terminate.
		getBeliefbase().getBeliefSet("reports").addFact(tr);
	}
	
	
}
