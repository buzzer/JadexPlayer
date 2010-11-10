package jadex.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.*;
import jadex.util.collection.SCollection;

import java.util.List;

/**
 *  Receive a contract net protocol (cnp) and answer it.
 */
public class CNPReceiverPlan extends AbstractReceiverPlan
{
	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		long timeout;
		if(getBeliefbase().containsBelief("timeout") && getBeliefbase().getBelief("timeout").getFact()!=null)
		{
			timeout = ((Long)getBeliefbase().getBelief("timeout").getFact()).longValue();
		}
		else
		{
			timeout = -1;
		}

		IMessageEvent me = (IMessageEvent)getParameter("message").getValue();
		AgentIdentifier initiator = (AgentIdentifier)me.getParameter(SFipa.SENDER).getValue();
		Object cfp;
		IMessageEvent reply;
		List records = SCollection.createArrayList();
		Object[] proposal = null;

		for(int i=0; me.getType().equals(getShortProtocolName()+"_cfp") 
			&& (i==0 || isIterated()); i++)
		{
			getLogger().info("Negotiation round: "+i+" receiver got cfp: "+me);
			records.add(me);
			cfp = me.getContent();

			Exception ex = null;
			try
			{
				proposal = makeProposal(cfp, initiator); // todo: include information about negotiation round
			}
			catch(Exception e)
			{
				ex = e;
				getLogger().info("No proposal made due to exception: "+e);
				//e.printStackTrace();
			}
			if(ex!=null || proposal == null || proposal[0]==null)
			{
				reply = me.createReply(getShortProtocolName()+"_refuse");
				getLogger().info("No proposal made. Finished.");
				sendMessage(reply);
				return; // todo: ?
				//fail();
			}

			reply = me.createReply(getShortProtocolName()+"_propose");
			reply.setContent(proposal[0]);
			getLogger().info("Receiver sent proposal: "+reply);
			me = sendMessageAndWait(reply, timeout);
		}

		if(me.getType().equals(getShortProtocolName()+"_accept"))
		{
			getLogger().info(getAgentName()+" excuting the cnp task.");
			try
			{
				Object result = executeTask(proposal[0], proposal[1], initiator);
				reply = me.createReply(getShortProtocolName()+"_inform");
				reply.setContent(result); // todo: how to put in the Done()???
				getLogger().info("Receiver sent done: "+reply);
				sendMessage(reply);
				getParameter("result").setValue(result);
			}
			catch(Exception e)
			{
//				e.printStackTrace();
				reply = me.createReply(getShortProtocolName()+"_failure");
				getLogger().info("Receiver sent failure: "+reply);
				sendMessage(reply);
			}
		}
		else
		{
			getLogger().info("Proposal rejected. Finished.");
		}
	}
	
	/**
	 *  Test if it is the iterated contract-net version.
	 *  @return True, if is is the iterated version.
	 */
	protected boolean isIterated()
	{
		return ((Boolean)getParameter("iterated").getValue()).booleanValue();	
	}
	
	/**
	 *  Get protocol abbrev name.
	 *  @return The protocol abbrev name.
	 */
	protected String getShortProtocolName()
	{
		String ret = "cnp";
		if(isIterated())
			ret = "icnp";
		return ret;
	}

	/**
	 *  Make a proposal based on the task description.
	 *  @param cfp The cfp including the task to execute.
	 *  @return The proposal for executing the task.
	 */
	public Object[] makeProposal(Object cfp, AgentIdentifier initiator)
	{
		IGoal make_proposal = createGoal(getShortProtocolName()+"_make_proposal");
		make_proposal.getParameter("cfp").setValue(cfp);
		make_proposal.getParameter("initiator").setValue(initiator);
		dispatchSubgoalAndWait(make_proposal);
		return new Object[]{make_proposal.getParameter("proposal").getValue(),
			make_proposal.getParameter("proposal_info").getValue()};
	}

	/**
	 *  Execute the task.
	 *  @param proposal The proposal.
	 *  @param proposal_info The proposal info.
	 *  @return The result of the task.
	 */
	public Object executeTask(Object proposal, Object proposal_info, AgentIdentifier initiator)
	{
		IGoal execute_task = createGoal(getShortProtocolName()+"_execute_task");
		execute_task.getParameter("proposal").setValue(proposal);
		execute_task.getParameter("proposal_info").setValue(proposal_info);
		execute_task.getParameter("initiator").setValue(initiator);
		dispatchSubgoalAndWait(execute_task);
		return execute_task.getParameter("result").getValue();
	}
}

