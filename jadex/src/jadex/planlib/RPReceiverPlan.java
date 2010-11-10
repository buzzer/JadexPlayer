package jadex.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.*;

/**
 *  Receive a request and answer it.
 */
public class RPReceiverPlan extends AbstractReceiverPlan
{
	/**
	 *  The body method is called on the
	 *  instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		IMessageEvent me = (IMessageEvent)getParameter("message").getValue();
		getLogger().info("Receiver got request: "+me);
		IMessageEvent reply;

		try
		{
			Boolean res = decideRequest(me.getContent(), 
				(AgentIdentifier)me.getParameter(SFipa.SENDER).getValue());

			if(res!=null)
			{
				if(res.booleanValue())
				{
					getLogger().info("Receiver sent agree.");
					reply = me.createReply("rp_agree");
					sendMessage(reply);
				}
				else
				{
					getLogger().info("Receiver sent refuse.");
					reply = me.createReply("rp_refuse");
					sendMessage(reply);
					return;
				}
			}
		}
		catch(GoalFailureException e)
		//catch(Exception e)
		{
			getLogger().info("No agree/refuse sent.");
			//e.printStackTrace();
		}

		try
		{
			Object res = executeRequest(me.getContent(), 
				(AgentIdentifier)me.getParameter(SFipa.SENDER).getValue());
			reply = me.createReply("rp_inform");
			reply.setContent(res);
			getLogger().info("Receiver sent inform.");
			sendMessage(reply);
			getParameter("result").setValue(res);
		}
		//catch(Exception e)
		catch(GoalFailureException e)
		{
			getLogger().info("Receiver sent failure: "+e);
			reply = me.createReply("rp_failure");
			sendMessage(reply);
		}
	}

	/**
	 *  Decide about the request.
	 *  @param request The request.
	 *  @param initiator The requesting agent.
	 *  @return True, if should send agree. False for sending refuse. Exception/null for sending nothing.
	 */
	public Boolean decideRequest(Object request, AgentIdentifier initiator)
	{
		IGoal decide_request = createGoal("rp_decide_request");
		decide_request.getParameter("action").setValue(request);
		decide_request.getParameter("initiator").setValue(initiator);
		dispatchSubgoalAndWait(decide_request);
		return (Boolean)decide_request.getParameter("accept").getValue();
	}

	/**
	 *  Execute the request.
	 *  @param request The request.
	 *  @param initiator The requesting agent.
	 *  @return The result.
	 */
	public Object executeRequest(Object request, AgentIdentifier initiator)
	{
		IGoal execute_request = createGoal("rp_execute_request");
		execute_request.getParameter("action").setValue(request);
		execute_request.getParameter("initiator").setValue(initiator);
		dispatchSubgoalAndWait(execute_request);
		return execute_request.getParameter("result").getValue();
	}
}
