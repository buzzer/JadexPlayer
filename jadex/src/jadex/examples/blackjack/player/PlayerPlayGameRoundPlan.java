package jadex.examples.blackjack.player;

import jadex.examples.blackjack.*;
import jadex.examples.blackjack.player.HumanPlayerInterface.HumanPlayerControlPanel;
import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  This plan is executed for every game round.
 */
public class PlayerPlayGameRoundPlan extends Plan
{
	//-------- attributes --------

	/** The human player interface. */
	HumanPlayerInterface hpi = null;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public PlayerPlayGameRoundPlan()
	{
		getLogger().info("created: " + this);
		if(getBeliefbase().containsBelief("gui"))
			hpi = (HumanPlayerInterface)getBeliefbase().getBelief("gui").getFact();
	}

	//-------- attributes --------
	
	/**
	 *  Execute a plan.
	 */
	public void body()
	{
		IMessageEvent	querybet	= (IMessageEvent)getInitialEvent();
		RequestBet rb = (RequestBet)querybet.getContent();
		Player me = (Player)getBeliefbase().getBelief("myself").getFact();
		me.setState(Player.STATE_GAME_STARTED);
		long	timeout	= ((Number)getBeliefbase().getBelief("timeout").getFact()).longValue();

		int mybet = determineBet();
		//me.setAccount(me.getAccount()-mybet);
		me.makeBet(mybet);
		getLogger().info("new account-status=" + me.getAccount() + ", myBet=" + mybet);

		// Reply to dealer.
		IMessageEvent	msg	= querybet.createReply("inform_bet");
		rb.setBet(mybet);
		msg.setContent(new Done(rb));
		getLogger().info("sending bet to the dealer ... waiting for cardSet");
		sendMessage(msg);
		me.setState(Player.STATE_PLAYING);

		waitForCondition(getCondition("start_playing"));
		Card dealercard = ((GameState)getBeliefbase().getBelief("gamestate").getFact()).getDealer().getCards()[0];

		// Now draw as many cards as the strategy suggests.
		while(CardSet.calculateDeckValue(me.getCards())<21 && shouldDrawCard(dealercard))
		{
			// draw one more card
			getLogger().info("player decided to draw one more card");
			RequestDraw rd = new RequestDraw();
			IMessageEvent	draw_request	= querybet.createReply("request_draw", rd);
			// Hack!!! Use large timeout because other players might also draw cards.
			IMessageEvent ans = sendMessageAndWait(draw_request, timeout*10);
			Card[] cards = ((RequestDraw)((Done)ans.getContent()).getAction()).getCards();
			me.setCards(cards);
		}

		// this player finished
		getLogger().info("player decided not to draw more cards");
		me.setState(Player.STATE_FINISHED);
		RequestFinished rf = new RequestFinished();
		IMessageEvent	finished	= querybet.createReply("request_finished", rf);
		// Hack!!! Use large timeout because other players might still draw cards.
		IMessageEvent	resultmsg	= sendMessageAndWait(finished, timeout*10);

		// When player has won the game, increment account.
		GameResult gr = ((RequestFinished)((Done)resultmsg.getContent()).getAction()).getGameresult();
		if(gr.isWon())
		{
			// Update the account-status
			me.setAccount(me.getAccount()+gr.getMoney());
			getLogger().info("I won " + gr.getMoney());
		}
		else
		{
			// nothing more to do if the player lost
			getLogger().info("I lost :-(");
		}

		// Reset state.
		me.setState(Player.STATE_IDLE);
	}

	/**
	 *
	 */
	public int determineBet()
	{
		Player me = (Player)getBeliefbase().getBelief("myself").getFact();
		int mybet = 10;
		if(hpi!=null)
		{
			((HumanPlayerControlPanel)hpi.getControlPanel()).enableBid();
			try
			{
				waitForBeliefChange("gui", 5000);
				//System.out.println("Player bets: "+hpi.getBet());
			}
			catch(TimeoutException e)
			{
				//System.out.println("No bet made, using strategy.");
				//mybet = me.getStrategy().makeBet(me.getAccount());
			}
			mybet = ((HumanPlayerControlPanel)hpi.getControlPanel()).getBet();
			((HumanPlayerControlPanel)hpi.getControlPanel()).disableBid();
		}
		else
		{
			// apply betting-strategy
			mybet = me.getStrategy().makeBet(me.getAccount());
		}
		return mybet;
	}

	/**
	 *
	 * @param dealercard
	 * @return True, if another card should be drawn.
	 */
	public boolean shouldDrawCard(Card dealercard)
	{
		boolean draw = false;
		Player me = (Player)getBeliefbase().getBelief("myself").getFact();
		if(hpi!=null)
		{
			((HumanPlayerControlPanel)hpi.getControlPanel()).enableDrawCard();
			try
			{
				waitForBeliefChange("gui", 5000);
				//System.out.println("Player wants to: "+hpi.isDrawCard());
				draw = ((HumanPlayerControlPanel)hpi.getControlPanel()).isDrawCard();
			}
			catch(TimeoutException e)
			{
				//System.out.println("No draw card decision made, using strategy.");
				//draw = me.getStrategy().drawCard(me.getCards(), dealercard);
			}
			((HumanPlayerControlPanel)hpi.getControlPanel()).disableDrawCard();
		}
		else
		{
			draw = me.getStrategy().drawCard(me.getCards(), dealercard);
		}
		return draw;
	}

	/**
	 *  Called when something went wrong (e.g. timeout).
	 */
	public void	failed()
	{
		getException().printStackTrace();
		
		// Remove dealer fact.
		getBeliefbase().getBelief("dealer").setFact(null);

		// Reset state.
		Player me = (Player)getBeliefbase().getBelief("myself").getFact();
		me.setState(Player.STATE_IDLE);
	}
}
