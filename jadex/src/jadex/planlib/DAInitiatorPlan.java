package jadex.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.BDIFailureException;
import jadex.runtime.IGoal;
import jadex.runtime.IMessageEvent;
import jadex.runtime.MessageEventFilter;
import jadex.runtime.TimeoutException;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.util.List;


/**
 *  This plan implements the initiator of the "FIPA Dutch Auction Interaction
 *  Protocol Specification" (XC00032 - Experimental)
 *  
 *  A dutch auction is one where the auctioneer starts with a high start price
 *  and continually lowers it until the first bidder accepts the price.
 */
public class DAInitiatorPlan extends AbstractInitiatorPlan
{
	//-------- attributes --------
	
	/** The message filter for all protocol related messages. */
	protected MessageEventFilter mf;
	
	/** The initial message. */
	// Hack!!! Needed for cancel-meta-protocol and for avoiding garbage collection.
	protected IMessageEvent	start;
	
	//-------- methods --------
	
	/**
	 * The plan body.
	 */
	public void body()
	{
		AuctionDescription auctiondesc = (AuctionDescription)getParameter("auction_description").getValue();
		if(auctiondesc.getRoundTimeout()<=0)
		{
			getLogger().warning(getAgentName()+"No round timeout specified");
			fail();
		}
		
		// Fetch the timeout for each round of the auction.
		long roundtimeout = auctiondesc.getRoundTimeout();
		
		// Fetch the receivers.
		List receivers = SUtil.arrayToList(getParameterSet("receivers").getValues());
		
		// Initialize negotiations.
		String convid = SFipa.createUniqueId(getAgentName());
		mf = new MessageEventFilter(null);
		mf.addValue(SFipa.CONVERSATION_ID, convid);
		getWaitqueue().addFilter(mf);
		
		// Announce the auction by sending information about it.
		start = announceAuction(auctiondesc, receivers, convid);
		
		// Wait for the auction to begin.
		// Removes receivers that do not want to participate.
		waitForAuctionStart(auctiondesc, receivers);
		
		// Send calls for proposal until no more proposals are received.
		boolean running = true;
		Object winning_offer = null;
		AgentIdentifier winner = null;
		Object cfp = getParameter("cfp").getValue();
		Object cfp_info = getParameter("cfp_info").getValue();
		List history = SCollection.createArrayList();
		history.add(cfp);
		
		// Send calls for proposal until the limit price is reached or an agent is
		// willing to pay the actual price.
		// Auction ends when winner is determined, limit price is reached or
		// no receiver is left.
		while(running && receivers.size()>0) 
		{
			//System.out.println(getAgentName()+" current offer is: "+cfp+" "+receivers);
			
			// Send CFP.
			sendCFP(cfp, convid, receivers);
			
			// Wait for proposals.
			// Removes receivers that do not offer.
			winner = waitForProposals(cfp, roundtimeout, receivers);
			//System.out.println(getAgentName()+" winner is: "+winner);
		
			// Set the winner if propsals have been received, otherwise
			// cease sending CFP-messages (so the winner of the last round will
			// be the winner of the auction).
			if(winner != null)
			{
				winning_offer = cfp;
				running = false;
			}
			else
			{
				Object[] next = decideIteration(cfp_info, history.toArray());
				//System.out.println(getAgentName()+" next cfp: "+next);
				if(next==null)
				{
					// The initiator has decided to cancel the next round for some reason.
					running = false;
				}
				else
				{
					cfp = next[0];
					cfp_info = next[1];
					history.add(cfp);
				}
			}
		}	
			
		//System.out.println("END----------END---------END");
		
		// Evaluate the auction results and determine if a winner exists.
		evaluateAuctionResults(auctiondesc, cfp_info, history.toArray(), 
			winner, winning_offer);
		
		// Announce the auction end to all (still involved) participants.
		announceAuctionEnd(receivers, convid, winning_offer, winner);
		
		getWaitqueue().removeFilter(mf);
	}
	
	/**
	 *  Announce the planned auction.
	 *  @param auctiondesc the auction description.
	 *  @param receivers The receivers.
	 *  @param convid The conversation id.
	 */
	protected IMessageEvent announceAuction(Object auctiondesc, List receivers, String convid)
	{
		// Send the inform_start_auction-message to all receivers.
		IMessageEvent me = getEventbase().createMessageEvent("da_inform_start_auction");
		me.getParameterSet(SFipa.RECEIVERS).addValues(receivers.toArray());
		me.getParameter(SFipa.CONTENT).setValue(auctiondesc);
		me.getParameter(SFipa.CONVERSATION_ID).setValue(convid);
		getLogger().info(getAgentName() + ": inform_start_auction");
		
		sendMessage(me);
		return me;
	}
	
	
	/** 
	 *  Wait for the auction start time.
	 *  @param auctiondesc The auction description.
	 *  @param receivers The receivers.
	 */
	protected void waitForAuctionStart(AuctionDescription auctiondesc, List receivers)
	{
		// The initiator of the interaction protocol shall wait until interested
		// agents are ready to participate.
		// If agents indicate that they do not wish to participate they are excluded
		// from the auction.
		
		long timetowait = auctiondesc.getStarttime()==0? 0: 
			auctiondesc.getStarttime() - System.currentTimeMillis();

		//System.out.println(getAgentName()+" waiting for: "+timetowait);
		while(timetowait > 0)
		{
			IMessageEvent removebidder;
			try
			{
				removebidder = (IMessageEvent)waitFor(mf, timetowait);
			}
			catch(TimeoutException e)
			{
				break;
			}
			
			if(removebidder.getType().equals("da_not_understood"))
			{
				receivers.remove(removebidder.getParameter(SFipa.SENDER).getValue());
				getLogger().info("Removed "+((AgentIdentifier)removebidder.getParameter(SFipa.SENDER).getValue()).getName() + ".");
			}
			else
			{
				getLogger().warning("Could not handle message of type "+removebidder.getType() 
					+" from "+((AgentIdentifier)removebidder.getParameter(SFipa.SENDER).getValue()).getName()+".");
			}
			
			timetowait =  - System.currentTimeMillis();
		}
	}
	
	/**
	 *  Send cfps to all receivers.
	 *  @param cfp The cfp.
	 *  @param convid The conversation id.
	 *  @param receivers The receivers.
	 */
	protected void sendCFP(Object cfp, String convid, List receivers)
	{
		// Send CFP.
		IMessageEvent cfpm = getEventbase().createMessageEvent("da_cfp");
		cfpm.getParameterSet(SFipa.RECEIVERS).addValues(receivers.toArray());
		cfpm.setContent(cfp);
		cfpm.getParameter(SFipa.CONVERSATION_ID).setValue(convid);
		getLogger().info(getAgentName() + ": cfp(" + cfp + ")");
		sendMessage(cfpm);
	}
	
	/**
	 *  Decide about the next iteration.
	 *  @param cfp_info The cfp info.
	 *  @param history The history.
	 *  @return The new cfp and cfp_info as an object array.
	 */
	protected Object[] decideIteration(Object cfp_info, Object[] history)
	{
		Object[] ret = null;
		IGoal di = createGoal("da_decide_iteration");
		di.getParameter("cfp_info").setValue(cfp_info);
		di.getParameterSet("history").addValues(history);
		try
		{
			dispatchSubgoalAndWait(di);
			ret = new Object[2];
			ret[0] = di.getParameter("cfp").getValue();
			ret[1] = di.getParameter("cfp_info").getValue();
			
			getLogger().info(getAgentName() + "calculated new cfp: "+ret[0]);
		}
		catch(BDIFailureException e)
		{
			getLogger().fine("No further iteration: "+e);
			//e.printStackTrace();
		}
		return ret;
	}
	
	
	/**
	 *  Wait for proposals of participants.
	 *  @param cfp the cfp.
	 *  @param roundtimeout The round timeout.
	 *  @param receivers The receivers.
	 *  @return The message of the winner.
	 */
	protected AgentIdentifier waitForProposals(Object cfp, long roundtimeout, List receivers)
	{
		AgentIdentifier winner = null;
		
		// Perform a negotiation round as long as no winner could be determined.
		long roundstart = System.currentTimeMillis();
		while(System.currentTimeMillis() - roundstart < roundtimeout)
		{
			IMessageEvent tmp = null;
			try
			{
				tmp = (IMessageEvent)waitFor(mf, roundtimeout);
				if(tmp.getType().equals("da_propose"))
				{
					// Accept the first winner
					if(winner==null)
					{
						// Send the accept_proposal-message to the agent with the first proposal.
						sendMessage(tmp.createReply("da_accept_proposal"));
						getLogger().info(getAgentName() + " found winner: "+tmp.getParameter(SFipa.SENDER).getValue());
						
						// Send the inform_end_auction-message.
						IMessageEvent end = tmp.createReply("da_inform_end_auction");
						end.setContent(cfp);
						sendMessage(end);
						getLogger().info(getAgentName() + ": inform_end_auction");
						
						// Set the parameter "winner" to the AgentIdentifier of the
						// winning agent.
						winner = (AgentIdentifier)tmp.getParameter(SFipa.SENDER).getValue();
					}
					// Reject all other proposals
					else
					{
						// Send reject_proposal-message.
						sendMessage(tmp.createReply("da_reject_proposal"));
						getLogger().info(getAgentName() + ": rejected proposal");
					}
				}
				else
				{
					// Remove agent from the list of receivers on any
					// other of message. So you can use e.g. a
					// not_understood_message to exit the auction
					receivers.remove(tmp.getParameter(SFipa.SENDER).getValue());
				}
			}
			catch(TimeoutException e)
			{
			}
		}
		return winner;
	}
	
	/**
	 *  Evaluate the auction results and decide about participation.
	 *  @param auctiondesc The auction description.
	 *  @param cfp_info The cfp info.
	 *  @param history The historz of cfps.
	 *  @param winner the winner.
	 *  @param winning_offer The winning offer.
	 */
	protected void evaluateAuctionResults(AuctionDescription auctiondesc, Object cfp_info, 
		Object[] history, AgentIdentifier winner, Object winning_offer)
	{
		if(winner == null)
		{
			getLogger().info(getAgentName() + ": auction finished (no winner)");
		}
		else
		{
			getLogger().info(getAgentName() + ": auction finished (winner: " 
				+winner.getName() + " - winning offer: " + winning_offer + ")");
		
			getParameter("result").setValue(new Object[]{winner, winning_offer});
		}	
	}
	
	/**
	 *  Announce the end of the auction to all participants that did not leave the auction.
	 *  @param receivers The receivers.
	 *  @param convid The conversation id.
	 *  @param winning_offer The winning offer.
	 */
	protected void announceAuctionEnd(List receivers, String convid, Object winning_offer, AgentIdentifier winner)
	{
		// Send the inform_end_auction-message.
		List losers = SCollection.createArrayList();
		losers.addAll(receivers);
		
		if(winner!=null)
		{
			IMessageEvent end = getEventbase().createMessageEvent("da_inform_end_auction");
			end.getParameter(SFipa.CONTENT).setValue(new Object[]{Boolean.TRUE,winning_offer});
			end.getParameterSet(SFipa.RECEIVERS).addValue(winner);
			end.getParameter(SFipa.CONVERSATION_ID).setValue(convid);
			sendMessage(end);
			// Remove the winner from list of losers to inform.
			losers.remove(winner);
		}
		if(losers.size()>0)
		{
			IMessageEvent end = getEventbase().createMessageEvent("da_inform_end_auction");
			end.getParameter(SFipa.CONTENT).setValue(new Object[]{Boolean.FALSE,winning_offer});
			end.getParameterSet(SFipa.RECEIVERS).addValues(losers.toArray());
			end.getParameter(SFipa.CONVERSATION_ID).setValue(convid);
			sendMessage(end);
		}
	}
	
	//-------- AbstractInitiatorPlan template methods --------
	
	/**
	 *  Get the initial message.
	 */
	protected IMessageEvent getInitialMessage()
	{
		return start;
	}
}

