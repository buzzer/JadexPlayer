package jadex.examples.blackjack.dealer;

import jadex.adapter.fipa.*;
import jadex.examples.blackjack.*;
import jadex.examples.blackjack.player.strategies.*;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;

/**
 *  Plan to handle join request of a player.
 */
public class DealerRegisterPlayerPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		IMessageEvent	request	= (IMessageEvent)getInitialEvent();

		// the player should have sent its name, account-status and its strategy-name
		// the message-content should look like this: 'join:name:strategy:account:color'
		RequestJoin rj = (RequestJoin)request.getContent();
		Player player = rj.getPlayer();
		player.setAgentID((AgentIdentifier)request.getParameter("sender").getValue());
		getLogger().info("New player "+player);
		if(!getBeliefbase().getBeliefSet("players").containsFact(player))
		{
			getBeliefbase().getBeliefSet("players").addFact(player);
			player.setState(Player.STATE_IDLE);
		}
		else
		{
			// Reset entry.
			Player	old	= (Player)getBeliefbase().getBeliefSet("players").getFact(player);
			old.setState(Player.STATE_IDLE);
		}

		// set FIPA-performative for the message sent back to the player
		// the content of the answer is just the games standard timeout.
		rj.setTimeout(((Long)getBeliefbase().getBelief("playerwaitmillis").getFact()).intValue());
		Done done = new Done(rj);
		sendMessage(request.createReply("inform_action_done", done));
	}
}
