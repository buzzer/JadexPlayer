package jadex.examples.blackjack.manager;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.Plan;

import java.awt.EventQueue;

/**
 *  Register at dealer, when dealer belief has changed.
 */
public class ManagerDealerRegisterPlan extends Plan
{
	/**
	 *  Execute a plan.
	 */
	public void body()
	{		
		final AgentIdentifier dealerAID = (AgentIdentifier)getBeliefbase().getBelief("localDealerAID").getFact();
		final ManagerFrame	gui	= (ManagerFrame)getBeliefbase().getBelief("gui").getFact();
		getLogger().info("Dealer-AID has changed " + dealerAID);
		
		// new dealer found
		/*if (dealerAID != null)
		{
//			// Send register message to the dealer.
//			IMessageEvent	register	= createMessageEvent("dealer_register");
//			register.getParameterSet(SFipa.RECEIVERS).addValue(dealerAID);
//			getLogger().info("send registerAsTracker-Query to Dealer");
//			sendMessage(register);
			
			// AWTThread.
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					gui.setDealerLabels(dealerAID.getName(), null);
				}
			});
		}
		else // no dealer found
		{
			getLogger().info("No dealer in beliefbase, reset GUILabel");

			// AWTThread.
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					gui.resetDealerLabels();
					gui.setLocalDealerButtonMode(true);
				}
			});
		}*/
	}
}
