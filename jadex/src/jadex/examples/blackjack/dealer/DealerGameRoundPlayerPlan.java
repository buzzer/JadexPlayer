package jadex.examples.blackjack.dealer;

import jadex.examples.blackjack.*;
import jadex.runtime.*;
import jadex.adapter.fipa.*;

/**
 *  Play a game round with the given player.
 */
public class DealerGameRoundPlayerPlan extends Plan
{
	//-------- attributes --------

	/** The timout for communication with the player. */
	protected long	timeout;

	/** The player object. */
	protected Player	player;

	//-------- constructors --------

	/**
	 *  Create a new DealerPlayGameRoundPlan.
	 */
	public DealerGameRoundPlayerPlan()
	{
		this.timeout = ((Number)getBeliefbase().getBelief("playerwaitmillis").getFact()).longValue();
		this.player = (Player)getParameter("player").getValue();
	}

	//-------- methods --------
	
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// Ask player for her bet.
		getLogger().info("Asking for bet from player: "+player);
		RequestBet rb = new RequestBet();
		IMessageEvent	msg	= createMessageEvent("request_bet");
		msg.setContent(rb);
		msg.getParameterSet(SFipa.RECEIVERS).addValue(player.getAgentID());
		IMessageEvent	betmsg	= sendMessageAndWait(msg, timeout);
		getLogger().info("Received bet from player: "+player+", "+betmsg.getContent());

		// When player does not provide a bet (e.g. declines to play), end plan.
		if(!(betmsg.getContent() instanceof Done))
		{
			fail();
		}

		// Extract bet and update player state.
		int bet	= ((RequestBet)((Done)betmsg.getContent()).getAction()).getBet();
		if(bet>player.getAccount())
		{
			startAtomic();
			player.setState(Player.STATE_UNREGISTERED);
			getBeliefbase().getBeliefSet("players").removeFact(player);
			endAtomic();
			//fail(); // Not necessary because context becomes invalid
		}
		player.makeBet(bet);
		player.setState(Player.STATE_PLAYING);

		// Give two cards to player.
		drawCard();
		drawCard();

		MessageEventFilter fil = new MessageEventFilter(null);
		fil.addValue(SFipa.CONVERSATION_ID, betmsg.getParameter(SFipa.CONVERSATION_ID).getValue());
		getWaitqueue().addFilter(fil);

		// Wait for dealer to draw a card for itself.
		getLogger().info("Waiting for dealer card: "+player);
		waitForCondition(getCondition("dealer_card"), timeout*10);	// Hmmm... use timeout???

		// Inform player about dealer card and wait for answer.
		getLogger().info("Informing player about dealer card: "+player);
		Dealer	me	= (Dealer)getBeliefbase().getBelief("myself").getFact();

		IMessageEvent answer = (IMessageEvent)waitFor(fil, timeout);
		getWaitqueue().removeFilter(fil);

		// Give cards to player as long she wants one.
		while(answer.getContent() instanceof RequestDraw)
		{
			drawCard();
			RequestDraw rd = (RequestDraw)answer.getContent();
			rd.setCards(player.getCards());
			Done done = new Done(rd);
			IMessageEvent mdone = answer.createReply("inform_action_done", done);
			answer = sendMessageAndWait(mdone, timeout);
			//correctGameState();
			getLogger().info("Player wants to draw a card: "+player);
		}
		getLogger().info("Player is finished: "+player);
		player.setState(Player.STATE_FINISHED);

		// Wait for dealer to finish, too.
		getLogger().info("Waiting for dealer to finish: "+player);
		waitForCondition(getCondition("dealer_finished"), timeout*10);	// Hmmm... use timeout???

		// Check if the player won or lost and inform player about result.
		GameResult gr = new GameResult();
		int	moneywon	= player.getMoneyWon(me.getCards());
		if(moneywon > 0) // player won
		{
			gr.setWon(true);
			gr.setMoney(moneywon);
			player.setAccount(player.getAccount() + moneywon);
		}
		else
		{
			gr.setWon(false);
		}
		RequestFinished rf = (RequestFinished)answer.getContent();
		rf.setGameresult(gr);
		Done done = new Done(rf);
		IMessageEvent	result	= answer.createReply("inform_action_done", done);

		sendMessage(result);
		getLogger().info("Player result" + player + "-" + result.getContent());

		// Wait until allowed to proceed (step-mode or delay).
		if(((Boolean)getBeliefbase().getBelief("singleStepMode").getFact()).booleanValue())
		{
			waitForInternalEvent("step");
		}
		else
		{
			waitFor(1000*((Number)getBeliefbase().getBelief("stepdelay").getFact()).intValue());
		}
	}

	/**
	 *  Something went wrong. Remove player from beliefs.
	 */
	public void	failed()
	{
		//System.out.println("player failed :-( "+getName());
		getLogger().info("Player failure :"+player);
		player.setState(Player.STATE_UNREGISTERED);
		getBeliefbase().getBeliefSet("players").removeFact(player);
	}

	/**
	 *  Game complete, reset player.
	 */
	public void	passed()
	{
		getLogger().info("Game completed :"+player);
		player.setState(Player.STATE_IDLE);
	}
	
	/**
	 *  Aborted, reset player.
	 */
	public void	aborted()
	{
		getLogger().info("Game aborted :"+player);
		player.setState(Player.STATE_IDLE);
	}

	//-------- helper methods --------

	/**
	 *  Draw a card for the player.
	 */
	protected void	drawCard()
	{
		// Wait until it is the players turn.
		getLogger().info("Waiting for players turn: "+player);
		ICondition	turn	= getCondition("players_turn");
		turn.setParameter("$player", player);
		waitForCondition(turn, timeout*10);	// Hmmm... use timeout???

		// Wait until allowed to draw card (step-mode or delay).
		if(((Boolean)getBeliefbase().getBelief("singleStepMode").getFact()).booleanValue())
		{
			waitForInternalEvent("step");
		}
		else
		{
			waitFor(1000*((Number)getBeliefbase().getBelief("stepdelay").getFact()).intValue());
		}
		
		// Draw card for player.
		CardSet	cardset	= (CardSet)getBeliefbase().getBelief("cardset").getFact();
		Card	card	= cardset.drawCard();
		player.addCard(card);
	}
}
