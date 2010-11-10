package jadex.planlib;

import jadex.adapter.fipa.SFipa;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;

/**
 *  This plan serves as a base class for receiver plans.
 *  It creates the interaction state object and
 *  updates it when the plan terminates.
 */
public abstract class AbstractReceiverPlan extends Plan
{
	//-------- constructors --------
	
	/**
	 *  Initialize the plan
	 */
	public AbstractReceiverPlan()
	{
		// Update interaction state
		if(hasParameter("interaction_state"))
		{
			InteractionState	state	= (InteractionState)getParameter("interaction_state").getValue();
			if(state==null)
			{
				state	= new InteractionState();
			}
			state.setInteractionState(InteractionState.INTERACTION_RUNNING);
		}

		// Store interaction description
		if(hasParameter("interaction_description") && hasParameter("message"))
		{
			getParameter("interaction_description").setValue(((IMessageEvent)getParameter("message").getValue()).getParameter(SFipa.CONTENT));
		}
	}
	
	//-------- methods --------

	/**
	 *  Called when the plan is finished, i.e. when
	 *  the interaction is completed.
	 */
	public void passed()
	{
		if(hasParameter("interaction_state"))
		{
			InteractionState	state	= (InteractionState)getParameter("interaction_state").getValue();
			if(InteractionState.INTERACTION_RUNNING.equals(state.getInteractionState()))
			{
				state.setInteractionState(InteractionState.INTERACTION_FINISHED);
			}
		}
	}
	
	/**
	 *  Called when the plan is aborted, i.e.,
	 *  when the corresponding interaction goal
	 *  is dropped.
	 */
	public void aborted()
	{
		if(hasParameter("interaction_state"))
		{
			// When interaction was aborted on receiver side (i.e. not due to cancel request)
			// state is still "running".
			InteractionState	state	= (InteractionState)getParameter("interaction_state").getValue();
			if(InteractionState.INTERACTION_RUNNING.equals(state.getInteractionState()))
			{
				state.setInteractionState(InteractionState.INTERACTION_CANCELLED);
				
				// Inform initator side about dropped out participant using "not-understood" message.
				if(hasParameter("message"))
				{
					IMessageEvent	msg	= (IMessageEvent)getParameter("message").getValue();
					IMessageEvent	reply	= msg.createReply("cm_not_understood");
					sendMessage(reply);
				}
			}
		}
	}
	
	/**
	 *  Called when the plan fails, i.e.,
	 *  a problem occurred during protocol execution.
	 */
	public void failed()
	{
		getLogger().severe("Problem during interaction: "+this+", "+getException());
	}
}
