package jadex.planlib;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.util.SUtil;

import java.util.List;

/**
 *  This plan serves as a base class for initiator plans.
 *  It automatically handles protocol cancellation when
 *  the plan is aborted. 
 */
public abstract class AbstractInitiatorPlan extends Plan
{
	//-------- attributes --------
	
	/** The timeout. */
	private long	timeout;
	
	//-------- constructors --------
	
	/**
	 *  Initialize the plan
	 */
	public AbstractInitiatorPlan()
	{
		// Determine timeout.
		if(hasParameter("timeout") && getParameter("timeout").getValue()!=null)
		{
			timeout = ((Long)getParameter("timeout").getValue()).longValue();
		}
		else if(getBeliefbase().containsBelief("timeout") && getBeliefbase().getBelief("timeout").getFact()!=null)
		{
			timeout = ((Long)getBeliefbase().getBelief("timeout").getFact()).longValue();
		}
		else
		{
			timeout = -1;
		}
		
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
	}
	
	//-------- methods --------
	
	/**
	 *  Get the timeout.
	 */
	public long	getTimeout()
	{
		return timeout;
	}
	
	/**
	 *  Called when the plan is aborted, i.e.,
	 *  when the corresponding interaction goal
	 *  is dropped.
	 *  Terminates the interaction using the 
	 *  FIPA-Cancel-Meta-Protocol.
	 */
	public void aborted()
	{
		// Debugging code to see why interactions are cancelled.
//		List	procs	= new ArrayList();
//		RProcessGoal	proc	= (RProcessGoal)((ProcessGoalWrapper)getRootGoal()).unwrap();
//		while(proc!=null)
//		{
//			procs.add(proc.getScope().getName()+"."+proc.getName()+"("+proc.getProcessingState()+")");
//			if(proc.getProprietaryGoal()!=null)
//				proc	= proc.getProprietaryGoal().getRealParent();
//		}
//		System.out.println(getAgentName()+": Cancelling "+procs);
		
		// Try to abort the interaction using FIPA-Cancel-Meta-Protocol.
		// Results of cancellation will be stored in interaction state (if any).
		InteractionState	state	= null;
		if(hasParameter("interaction_state"))
		{
			state	= (InteractionState)getParameter("interaction_state").getValue();
		}

		// Copy message properties from initial message.
		IMessageEvent	cancel	= createMessageEvent("cm_cancel");
		cancel.getParameterSet(SFipa.RECEIVERS).addValues(getInitialMessage().getParameterSet(SFipa.RECEIVERS).getValues());
		cancel.getParameter(SFipa.CONVERSATION_ID).setValue(getInitialMessage().getParameter(SFipa.CONVERSATION_ID).getValue());
		cancel.getParameter(SFipa.LANGUAGE).setValue(getInitialMessage().getParameter(SFipa.LANGUAGE).getValue());
		cancel.getParameter(SFipa.ONTOLOGY).setValue(getInitialMessage().getParameter(SFipa.ONTOLOGY).getValue());
		// Use extra reply-with to avoid intermingling with other protocol messages.
		cancel.getParameter(SFipa.REPLY_WITH).setValue(SFipa.createUniqueId(getName()));

		// Send cancel message to participants.
		getWaitqueue().addReply(cancel);
		sendMessage(cancel);
		long	time	= System.currentTimeMillis();
		List	rec	= SUtil.arrayToList(getInitialMessage().getParameterSet(SFipa.RECEIVERS).getValues());
		try
		{
			while(rec.size()>0)
			{
				// Wait for the replies.
				long wait_time = getTimeout() + time - System.currentTimeMillis();
				if(wait_time <= 0)
					break;

				IMessageEvent reply = waitForReply(cancel, wait_time);
				rec.remove(reply.getParameter(SFipa.SENDER).getValue());
				
				// Store result in interaction state.
				if(state!=null)
				{
					String	response	= "cm_inform".equals(reply.getType()) ? InteractionState.CANCELLATION_SUCCEEDED
						: "cm_failure".equals(reply.getType()) ? InteractionState.CANCELLATION_FAILED
						: InteractionState.CANCELLATION_UNKNOWN;
					state.addCancelResponse((AgentIdentifier)reply.getParameter(SFipa.SENDER).getValue(),
						response, reply.getParameter(SFipa.CONTENT).getValue());
				}
			}
		}
		catch(TimeoutException e)
		{
			// Set result of non-responders to unknown.
			if(state!=null)
			{
				for(int i=0; i<rec.size(); i++)
				{
					state.addCancelResponse((AgentIdentifier)rec.get(i),
						InteractionState.CANCELLATION_UNKNOWN, null);
				}
			}		
		}
	}
	
	//-------- template methods --------

	/**
	 *  Get the initial message.
	 *  Has to be provided by subclasses.
	 *  Needed for sending cancel message in
	 *  correct conversation.
	 */
	protected abstract IMessageEvent	getInitialMessage();
}
