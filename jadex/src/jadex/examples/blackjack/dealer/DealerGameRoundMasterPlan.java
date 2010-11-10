package jadex.examples.blackjack.dealer;

import jadex.examples.blackjack.*;
import jadex.runtime.*;
import jadex.util.SUtil;


/**
 *  Play a game round (controls the plans responsible for single players).
 */
public class DealerGameRoundMasterPlan extends Plan
{
	//-------- methods --------
	
	/**
	 *  Plan body.
	 */
	public void body()
	{
		getLogger().info("Starting new game.");
		long timeout = ((Long)getBeliefbase().getBelief("playerwaitmillis").getFact()).longValue();

		CardSet	cardset	= new CardSet();
		getBeliefbase().getBelief("cardset").setFact(cardset);
		Dealer	me	= (Dealer)getBeliefbase().getBelief("myself").getFact();
		me.setState(Dealer.STATE_GAME_STARTED);

		// Trigger plan for each player.
		Player[]	players	= (Player[])getBeliefbase().getBeliefSet("players").getFacts();
		IGoal[]	goals	= new IGoal[players.length];
		for(int i=0; i<players.length; i++)
		{
			players[i].setState(Player.STATE_GAME_STARTED);
			goals[i]	= createGoal("play_with_player");
			goals[i].getParameter("player").setValue(players[i]);
			getWaitqueue().addSubgoal(goals[i]);
			dispatchSubgoal(goals[i]);
			getLogger().info("Playing with player: "+players[i]);
		}

		// Draw cards for dealer.
		me.setState(Dealer.STATE_PLAYING);
		// Dealer has to draw more cards until it's deckvalue is > 16
		// this is a blackjack-rule
		while(CardSet.calculateDeckValue(me.getCards()) <= 16)
		{
			// Wait for the dealer's turn.
			getLogger().info("Now waiting for dealer's turn.");
			ICondition	turn	= getCondition("dealers_turn");
			waitForCondition(turn, timeout*10);	// Hmmm... use timeout???
			getLogger().info("Dealer's turn. Players: "+SUtil.arrayToString(players));

			// Wait until allowed to draw card (step-mode or delay).
			if(((Boolean)getBeliefbase().getBelief("singleStepMode").getFact()).booleanValue())
			{
				waitForInternalEvent("step");
			}
			else
			{
				waitFor(1000*((Number)getBeliefbase().getBelief("stepdelay").getFact()).intValue());
			}
			
			// now go ahead, draw the card and update the beliefbase
			Card dealerCard = cardset.drawCard();
			me.addCard(dealerCard);
			getLogger().info("Dealer draws a new card, it's " + dealerCard + " deck-value=" + CardSet.calculateDeckValue(me.getCards()));
		}

		// Dealer result calculation.
		getLogger().info("Dealer finished drawing cards");
		int	newaccount	= me.getAccount();
		for(int i=0; i<players.length; i++)
		{
			if(!players[i].getState().equals(Player.STATE_IDLE))
			{
				newaccount	+= players[i].getBet() - players[i].getMoneyWon(me.getCards());
			}
		}
		me.setAccount(newaccount);
		me.setState(Dealer.STATE_FINISHED);

		// Wait until allowed to proceed (step-mode or delay).
		if(((Boolean)getBeliefbase().getBelief("singleStepMode").getFact()).booleanValue())
		{
			waitForInternalEvent("step");
		}
		else
		{
			waitFor(1000*((Number)getBeliefbase().getBelief("stepdelay").getFact()).intValue());
		}

		// Wait for player subgoals to finish.
		for(int i=0; i<players.length; i++)
		{
			waitForSubgoal(goals[i]);
		}

		// Store history.
		((GameStatistics)getBeliefbase().getBelief("statistics").getFact())
			.addGameRound(me, players);

		me.setState(Dealer.STATE_IDLE);
	}

	/**
	 *  Something went wrong. Reset playing state.
	 */
	public void	failed()
	{
		System.out.println(":-( "+getName());
		Dealer	me	= (Dealer)getBeliefbase().getBelief("myself").getFact();
		getLogger().info("Dealer failure :"+getBeliefbase().getBelief("myself").getFact());
		me.setState(Dealer.STATE_IDLE);
	}
}
