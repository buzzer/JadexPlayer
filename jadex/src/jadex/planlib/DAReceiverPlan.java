package jadex.planlib;

import java.util.List;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.*;
import jadex.util.collection.SCollection;

/**
 *  This plan implements the receiver of the "FIPA Dutch Auction Interaction
 *  Protocol Specification" (XC00032 - Experimental).
 *  
 *  A dutch auction is one where the auctioneer starts with a high start price
 *  and continually lowers it until the first bidder accepts the price.
 */
public class DAReceiverPlan extends AbstractReceiverPlan
{
	/**
	 * The plan body.
	 */
	public void body()
	{
		// Fetch the auction information.
		IMessageEvent me = (IMessageEvent)getParameter("message").getValue();
		AuctionDescription auctiondesc = (AuctionDescription)me.getParameter(SFipa.CONTENT).getValue();
		getLogger().info(getAgentName()+": Received inform_start_auction message with auction description " +
			"start time: "+auctiondesc.getStarttime()+" Round time "+auctiondesc.getRoundTimeout()
			+" topic: "+auctiondesc.getTopic());
		if(auctiondesc.getRoundTimeout()<=0)
		{
			getLogger().warning(getAgentName()+"No round timeout specified");
			fail();
		}
		
		// Offer the possibility to decide not to participate in the auction
		Object[] tmp = decideParticipation(auctiondesc, (AgentIdentifier)me.getParameter(SFipa.SENDER).getValue());
		boolean participate = ((Boolean)tmp[0]).booleanValue();
		Object auctioninfo = tmp[1];

		// Wait for messages with the conversation-id of the initial event (inform_start_auction).
		String convid = (String)me.getParameter(SFipa.CONVERSATION_ID).getValue();
		MessageEventFilter mef = new MessageEventFilter(null);
		mef.addValue(SFipa.CONVERSATION_ID, convid);
		getWaitqueue().addFilter(mef);
		
		long buftimeout = (long)(auctiondesc.getRoundTimeout()*1.1);
		long firsttimeout = auctiondesc.getStarttime()==0 || (auctiondesc.getStarttime()-System.currentTimeMillis()<=0)
			? -1 : auctiondesc.getStarttime()-System.currentTimeMillis()+buftimeout;
	
		List offers = SCollection.createArrayList();
		boolean running = true;
		Object winning_offer = null; // my winning offer
		Object auction_wo = null; // the winning offer of the auction
		int missing_cnt = 0;
		
		while(participate && running)
		{
			try
			{
				getLogger().info(getAgentName()+" waiting for: "+(firsttimeout==-1? buftimeout: firsttimeout));
				IMessageEvent msg = (IMessageEvent)waitFor(mef, firsttimeout==-1? buftimeout: firsttimeout);
				getLogger().info(getAgentName()+" received cfp: "+msg.getContent());
				missing_cnt = 0; // Reset missing_cnt as auction continues
				firsttimeout=-1;
				
				if(msg.getType().equals("da_cfp"))
				{
					handleCFP(msg, auctiondesc, auctioninfo, offers);
				}
				else if(msg.getType().equals("da_accept_proposal"))
				{
					winning_offer = msg.getContent();
					running = false;
				}
				else if(msg.getType().equals("da_reject_proposal"))
				{
					winning_offer = null;
				}
				else if(msg.getType().equals("da_inform_end_auction"))
				{
					Object[] res = (Object[])msg.getContent();
					
					if(!((Boolean)res[0]).booleanValue())
						winning_offer = null;
					
					auction_wo = res[1];
					running = false;
				}
				else
				{
					getLogger().warning("Could not understand: "+msg+" "+msg.getType());
				}
			}
			catch(TimeoutException e)
			{
				getLogger().info(getAgentName()+" "+e.getMessage());
				// Exit when no offers are received any more (for 3 times).
				//System.out.println(getAgentName()+" missed cfp: "+missing_cnt);
				if(++missing_cnt==3)
					running = false; 
			}
		}
		
		if(!running)
			getParameter("result").setValue(new Object[]{winning_offer, auction_wo});
		
		getWaitqueue().removeFilter(mef);
	}
	
	/**
	 *  Decide about participation.
	 *  If the goal is not handled participation is true.
	 *  @param auctiondesc The auction description.
	 *  @return The participation state (Boolean) and the local auction info (Object).
	 */
	protected Object[] decideParticipation(AuctionDescription auctiondesc, AgentIdentifier initiator)
	{
		Object[] ret = new Object[2];
		ret[0] = Boolean.TRUE; // participate
		ret[1] = null; // auction info
		
		try
		{
			IGoal dp = getScope().getGoalbase().createGoal("da_decide_participation");
			dp.getParameter("auction_description").setValue(auctiondesc);
			dp.getParameter("initiator").setValue(initiator);
			dispatchSubgoalAndWait(dp);
			ret[1] = dp.getParameter("auction_info").getValue();
			Boolean part = (Boolean)dp.getParameter("participate").getValue();
			ret[0] = part==null? Boolean.TRUE: part;
		}
		catch(GoalFailureException e)
		{
			// Participate if no explicit decision was made.
			getLogger().info("Optional goal ea_decide_request has not been handled.");
		}
		
		return ret;
	}
	
	/**
	 *  Handle a cfp message.
	 *  @param auctiondesc The auction description.
	 *  @return The participation state (Boolean) and the local auction info (Object).
	 */
	protected Object[] handleCFP(IMessageEvent cfp, AuctionDescription auctiondesc, 
			Object auctioninfo, List offers)
	{
		//System.out.println("cfp: "+cfp.getContent());
	
		Object[] ret = new Object[2];
		ret[0] = Boolean.TRUE; // participate
		ret[1] = auctioninfo;
		
		// Instantiate make_proposal-goal with the offer of the received CFP.
		IGoal mp = createGoal("da_make_proposal");
		Object offer = cfp.getContent();
		offers.add(offer);
		mp.getParameter("cfp").setValue(offer);
		mp.getParameter("auction_description").setValue(auctiondesc);
		mp.getParameter("auction_info").setValue(auctioninfo);
		mp.getParameterSet("history").addValues(offers.toArray());
		
		try
		{
			dispatchSubgoalAndWait(mp, auctiondesc.getRoundTimeout());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			getLogger().info(getAgentName() + e.getMessage());
		}
		ret[1] = mp.getParameter("auction_info").getValue();
		Boolean leave = (Boolean)mp.getParameter("leave").getValue();
		Boolean accept = (Boolean)mp.getParameter("accept").getValue();
		if(leave!=null && leave.booleanValue())
		{
			getLogger().info(getAgentName() + " informs the initiator of the auction "
				+auctiondesc.getTopic()+" that it doesn't want to participate.");
		
			sendMessage(cfp.createReply("da_not_understood"));
			
			ret[0] = Boolean.FALSE;
		}
		else if(accept!=null && accept.booleanValue())
		{
			// System.out.println(getAgentName()+" sending proposal: "+offer);
			// Send propsal.
			sendMessage(cfp.createReply("da_propose"));
			getLogger().info(getAgentName()+" accepted proposal: "+cfp.getContent());
		}
		else
		{
			getLogger().info(getAgentName()+" does not accept proposal and waits: "+cfp.getContent());
		}
		
		return ret;
	}
}
