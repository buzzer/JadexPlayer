package jadex.tools.common;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.tools.ontology.ToolRequest;


/**
 *  Plan to issue a request to an observed agent.
 */
public class ToolRequestPlan extends Plan
{
	//-------- methods --------
	
	/**
	 *  Plan body.
	 */
	public void body()
	{
		// Extract parameter values.
		IToolPanel	tool	= (IToolPanel)getParameter("tool").getValue();
		AgentIdentifier	agent	= (AgentIdentifier)getParameter("agent").getValue();
		ToolRequest	request	= (ToolRequest)getParameter("request").getValue();

		// Create request message.
		// Using reply-with assures that this plan will get the answer
		// (and not the update plan, which only waits for the conversation id).
		IMessageEvent	request_msg	= createMessageEvent("tool_request");
		request_msg.getParameterSet(SFipa.RECEIVERS).addValue(agent);
		request_msg.getParameter(SFipa.REPLY_WITH).setValue(SFipa.createUniqueId(tool.getId()));
		request_msg.getParameter(SFipa.CONVERSATION_ID).setValue(tool.getId());
		request_msg.setContent(request);
		
//		System.out.println("Sending tool request "+request+" to "+agent);

		long timeout = ((Long)getBeliefbase().getBelief("timeout").getFact()).longValue();
		IMessageEvent	reply	= sendMessageAndWait(request_msg, timeout);
		getParameter("result").setValue(reply.getContent());

//		System.out.println("Received tool request answer to "+request+" from "+agent);

		if(reply.getType().equals("tool_failure"))
			fail();
	}
}
