package jadex.planlib;

import jadex.adapter.fipa.SFipa;
import jadex.runtime.IExpression;
import jadex.runtime.IGoal;
import jadex.runtime.IMessageEvent;

/**
 *  Receiver plan for FIPA-Cancel-Meta-Protocol.
 *  Tries to terminate a conversation by finding
 *  and dropping the corresponding top-level goal.
 */
public class CMReceiverPlan extends AbstractReceiverPlan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Object	failure_reason	= null;
		
		// Try to find interaction goal.
		IMessageEvent	msg	= (IMessageEvent)getParameter("message").getValue();
		String	convid	= (String)msg.getParameter(SFipa.CONVERSATION_ID).getValue();
		IExpression	find_interaction_goal	= getExpression("find_interaction_goal");
		String	goalname	= (String)find_interaction_goal.execute("$convid", convid);
		if(goalname!=null)
		{
			IGoal	interaction_goal	= getGoalbase().getGoal(goalname);
			IMessageEvent	inimsg	= (IMessageEvent)interaction_goal.getParameter("message").getValue(); 
						
			try
			{
				// Post approve cancel goal to infporm domain layer abotu cancellation.
				IGoal	approve_cancel	= createGoal("cm_approve_cancel");
				approve_cancel.getParameter(SFipa.CONVERSATION_ID).setValue(convid);
				approve_cancel.getParameter(SFipa.PROTOCOL).setValue(inimsg.getParameter(SFipa.PROTOCOL).getValue());
				approve_cancel.getParameter("initiator").setValue(msg.getParameter(SFipa.SENDER).getValue());
				dispatchSubgoalAndWait(approve_cancel);
				
				// If cancel is not approved, store failure reason.
				// Cancel anyways, as interaction is already aborted on initiator side.
				// Initiator has to check cancel result and try to settle open issues as described in failure reason.
				if(!((Boolean)approve_cancel.getParameter("result").getValue()).booleanValue())
				{
					failure_reason	= approve_cancel.getParameter("failure_reason").getValue();
					if(failure_reason==null)
					{
						failure_reason	= "Participant did not approve cancel request.";
					}
				}
				
				// To decide between internal drops and drops due to cancel
				// the interaction state is set from the outside (hack!!!).
				if(interaction_goal.hasParameter("interaction_state"))
				{
					// State should not be null otherwise it is not possible to access value from process goal.
					InteractionState	state	= (InteractionState)interaction_goal.getParameter("interaction_state").getValue();
					if(state==null)
					{
						throw new RuntimeException("InteractionState is null, cannot safely cancel interaction.");
					}
					else if(InteractionState.INTERACTION_RUNNING.equals(state.getInteractionState()))
					{
						state.setInteractionState(InteractionState.INTERACTION_CANCELLED);
					}
				}
			
				interaction_goal.drop();
				waitForGoal(interaction_goal);
			}
			catch(Exception e)
			{
//				e.printStackTrace();
				failure_reason	= failure_reason!=null ? failure_reason : e.toString();
			}
		}
		else
		{
			failure_reason	= "No interaction goal found for conversation id: "+convid;
		}

		// Post result to requester.
		IMessageEvent	reply;
		if(failure_reason==null)
		{
			reply	= msg.createReply("cm_inform");
			// Todo: content?
		}
		else
		{
			reply	= msg.createReply("cm_failure");
			// Use user defined language/ontology (hack???, may not support string content?).
			reply.getParameter(SFipa.CONTENT).setValue(failure_reason);
		}
		sendMessage(reply);
	}
}
