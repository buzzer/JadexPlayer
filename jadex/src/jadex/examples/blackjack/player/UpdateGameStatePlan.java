package jadex.examples.blackjack.player;

import java.util.List;
import jadex.runtime.Plan;
import jadex.examples.blackjack.*;
import jadex.util.SUtil;

/**
 *  Update the game state.
 */
public class UpdateGameStatePlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		//System.out.println("Game state updated.");
		GameState gs = (GameState)getParameter("gamestate").getValue();
		GameState gsold = (GameState)getBeliefbase().getBelief("gamestate").getFact();
		Player me = (Player)getBeliefbase().getBelief("myself").getFact();

		if(gs.getDealer()==null || !gs.getDealer().equals(gsold.getDealer()))
		{
			gsold.setDealer(gs.getDealer());
		}
		else
		{
			gsold.updateDealer(gs.getDealer());
		}

		List olds = SUtil.arrayToList(gsold.getPlayers());
		Player[] news = gs.getPlayers();
		for(int i=0; i<news.length; i++)
		{
			olds.remove(news[i]);
			// Don't add myself to the game state.
			if(!news[i].equals(me))
				gsold.updateOrAddPlayer(news[i]);
		}
		for(int i=0; i<olds.size(); i++)
		{
			gsold.removePlayer((Player)olds.get(i));
		}

		//getBeliefbase().getBelief("gamestate").setFact(gs);
		Player mec = gs.getPlayer(me.getName());
		// extract the card out of the received message
		if(mec!=null && mec.getCards().length!=me.getCards().length)
		{
			me.setCards(mec.getCards());
			me.setAccount(mec.getAccount());
			me.setBet(mec.getBet());
		}
	}
}
