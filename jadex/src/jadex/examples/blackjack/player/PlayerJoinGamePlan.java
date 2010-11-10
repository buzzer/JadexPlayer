package jadex.examples.blackjack.player;

import jadex.adapter.fipa.*;
import jadex.examples.blackjack.*;
import jadex.runtime.IGoal;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;

import java.util.Random;

/**
 *  Find a dealer and join the game.
 */
public class PlayerJoinGamePlan extends Plan
{
	//-------- constructors --------
	
	/**
	 *  Create a new plan.
	 */
	public PlayerJoinGamePlan()
	{		
		getLogger().info("created: " + this);
	}

	//-------- methods --------
	
	/**
	 *  First the player searches a dealer, then sends a join-request to this
	 *  dealer.
	 */
	public void body()
	{
		// Search for dealer.
		AgentIdentifier	dealer	= (AgentIdentifier)getBeliefbase().getBelief("dealer").getFact();

		Player	me	= (Player)getBeliefbase().getBelief("myself").getFact();

		// create the join-message
		IMessageEvent	msg	= createMessageEvent("request_join");
		msg.getParameterSet(SFipa.RECEIVERS).addValue(dealer);
		RequestJoin rj = new RequestJoin();
		rj.setPlayer(me);
		//msg.setContent("join:" + getAgentName() + ":" + me.getStrategyName() + ":" + me.getAccount() + ":" + Player.color2Hex(me.getColor()));
		msg.setContent(rj);

		getLogger().info("sending join-message");
		
		// send the join-message and wait for a response
		IMessageEvent	reply	= sendMessageAndWait(msg, 10000);

		// evaluate content of the reply-message
		if(reply.getContent() instanceof Done)
		{
			getLogger().info("request was accepted, timeout is: " + reply.getContent());
			getBeliefbase().getBelief("timeout").setFact(
				new Integer(((RequestJoin)((Done)reply.getContent()).getAction()).getTimeout()));
			getBeliefbase().getBelief("dealer").setFact(dealer);
		}
	}

	/**
	 *  Called when something went wrong (e.g. timeout).
	 */
	public void	failed()
	{
		// Remove dealer fact.
		getBeliefbase().getBelief("dealer").setFact(null);
	}
}
