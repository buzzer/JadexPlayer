package jadex.planlib;

import jadex.runtime.*;
import jadex.adapter.fipa.*;


/**
 *  The fipa request plan performs the initiator side
 *  of the fipa-request protocol.
 *  The parameters have to be specified according to the goal.
 */
public class RPInitiatorMobilePlan extends MobilePlan
{
	//-------- attributes --------

	/** The wait timeout for response messages. */
	protected long timeout;

	/** The receiver of the request. */
	protected AgentIdentifier receiver;

	/** The content of the request. */
	protected Object content;

	/** The language (if any). */
	protected String language;

	/** The ontology (if any). */
	protected String ontology;

	/** Is the first answer received? */
	protected boolean	firstanswer;
	
	//-------- constructors --------

	/**
	 *  Create a plan for the initiator side of the fipa-request protocol.
	 *  The parameters have to be specified in the goal.
	 */
	public RPInitiatorMobilePlan()
	{
		if(hasParameter("timeout") && getParameter("timeout").getValue()!=null)
		{
			this.timeout = ((Long)getParameter("timeout").getValue()).longValue();
		}
		else if(getBeliefbase().containsBelief("timeout") && getBeliefbase().getBelief("timeout").getFact()!=null)
		{
			this.timeout = ((Long)getBeliefbase().getBelief("timeout").getFact()).longValue();
		}
		else	// No timeout.
		{
			this.timeout = -1;
		}
		this.receiver = (AgentIdentifier)getParameter("receiver").getValue();
		this.content = getParameter("content").getValue();
		this.language = (String)getParameter("language").getValue();
		this.ontology = (String)getParameter("ontology").getValue();
	}

	//-------- methods --------

	/**
	 *  Perform the request.
	 */
	public void action(IEvent event)
	{
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo())
		{
			getLogger().info(getScope().getName() + ": Request initiator action called: " + this);
	
			// Prepare message event.
			IMessageEvent request = createMessageEvent("rp_request");
			request.setContent(content);
			request.getParameterSet("receivers").addValue(receiver);
			if(language!=null)
				request.getParameter("language").setValue(language);
			if(ontology!=null)
				request.getParameter("ontology").setValue(ontology);
	
			// Send message and wait for answer.
			sendMessageAndWait(request, timeout);
		}
		else if(event instanceof IMessageEvent && !firstanswer)
		{
			handleFirstAnswer((IMessageEvent)event);
			firstanswer	= true;
		}
		else if(event instanceof IMessageEvent)
		{
			handleSecondAnswer((IMessageEvent)event);
		}
	}

	//-------- helper methods --------

	/**
	 *  Process the first answer.
	 */
	protected void	handleFirstAnswer(IMessageEvent answer)
	{
		getLogger().info("First answer: " + answer);

		// Initiator side of FIPA Request protocol.
		if(answer.getType().equals("rp_not_understood"))
		{
			handleNotUnderstood(answer);
		}
		else if(answer.getType().equals("rp_refuse"))
		{
			handleRefuse(answer);
		}
		else if(answer.getType().equals("rp_failure"))
		{
			handleFailure(answer);
		}
		else if(answer.getType().equals("rp_agree"))
		{
			handleAgree(answer);

			MessageEventFilter	inform	= new MessageEventFilter("rp_inform");
			inform.addValue(SFipa.CONVERSATION_ID, answer.getParameter("conversation-id").getValue());
			MessageEventFilter	failure	= new MessageEventFilter("rp_failure");
			failure.addValue(SFipa.CONVERSATION_ID, answer.getParameter("conversation-id").getValue());
			IFilter response = new ComposedFilter(
				new IFilter[]{inform, failure}, ComposedFilter.OR);

			waitFor(response, timeout);
		}
		else if(answer.getType().equals("rp_inform"))	// Hack!!! Done/Result not yet supported.
		{
			handleInform(answer);
		}
	}

	/**
	 *  Process the second answer.
	 */
	protected void handleSecondAnswer(IMessageEvent answer)
	{
		getLogger().info("Second answer: " + answer.getMessage());

		if(answer.getType().equals("rp_failure"))
		{
			handleFailure(answer);
		}
		else if(answer.getType().equals("rp_inform"))
		{
			handleInform(answer);
		}
	}

	/**
	 *  Method to handle the agree.
	 */
	protected void handleAgree(IMessageEvent message)
	{
		getLogger().info(getScope().getName() + ": Received agree.");
	}

	/**
	 *  Method to handle not understood.
	 */
	protected void handleNotUnderstood(IMessageEvent message)
	{
		// Message content should be tuple[action, not understood proposition].
		getLogger().info(getScope().getName() + ": Received not understood: " + message.getContent());
//		Object cont = message.getContent();
//		if (cont != null && cont instanceof ContentElementList)
//		{
//			res = ((ContentElementList) cont).get(1);
//		}
//		else
//		{
//			res = cont;
//		}
		requestFinished(false, message.getContent());
	}

	/**
	 *  Method to handle the refuse.
	 */
	protected void handleRefuse(IMessageEvent message)
	{
		// Message content should be tuple[action, refusal proposition].
		getLogger().info(getScope().getName() + ": Received refuse: " + message.getContent());
//		if(res instanceof ContentElementList)
//		{
//			res = ((ContentElementList) res).get(1);
//		}
		requestFinished(false, message.getContent());
	}

	/**
	 *  Method to handle the failure.
	 */
	protected void handleFailure(IMessageEvent message)
	{
		getLogger().info(getScope().getName() + ": Received failure: " + message.getContent());
		requestFinished(false, message.getContent());
	}

	/**
	 *  Method to handle the result of the request.
	 */
	protected void handleInform(IMessageEvent message)
	{
		getLogger().info(getScope().getName() + ": Request succeeded.");
		requestFinished(true, message.getContent());
	}

	/**
	 *  Method, that is being called, when the request has finished.
	 *  Default implementation sets status and result on goal.
	 *  @param success	The final status of the request.
	 *  @param result	The result object.
	 */
	protected void requestFinished(boolean success, Object result)
	{
		getParameter("result").setValue(result);
		//System.out.println("act goal: "+getActiveGoal());
		//System.out.println("result: "+getActiveGoal().getResult());

		if(!success)
		{
			fail();
		}
	}
}
