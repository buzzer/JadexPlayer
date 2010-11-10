package jadex.examples.blackjack.player;

import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;

/**
 *  This plan is executed when the agent wants to decline a game.
 */
public class PlayerDeclineGamePlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PlayerDeclineGamePlan()
	{
		getLogger().info("created: " + this);
	}

	//-------- attributes --------
	
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		IMessageEvent	querybet	= (IMessageEvent)getInitialEvent();

		// Reply to dealer.
		IMessageEvent	msg	= querybet.createReply("refuse_bet", querybet.getContent());
		getLogger().info("sending decline to the dealer...");
		sendMessage(msg);
	}
}
