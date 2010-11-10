package jadex.planlib;

import jadex.runtime.*;

/**
 *  The ping plan reacts on ping requests.
 */
public class AnswerPingPlan extends Plan
{
	//-------- attributes --------

	/** The ping string. */
	protected String ping;

	/** The ping reply string. */
	protected String reply;

	//-------- constructors --------

	/**
	 *  Create a new ping plan.
	 */
	public AnswerPingPlan()
	{
		this.ping = (String)getBeliefbase().getBelief("ping_content").getFact();
		this.reply = (String)getBeliefbase().getBelief("ping_answer").getFact();
	}

	/**
	 *  Create a new ping plan.
	 */
	public AnswerPingPlan(String ping, String reply)
	{
		this.ping = ping;
		this.reply = reply;
	}

	//-------- methods --------

	/**
	 *  Handle the ping request.
	 */
	public void body()
	{
		// Get the initial event.
		IMessageEvent me = (IMessageEvent)getInitialEvent();

		// Create the reply.
		IMessageEvent	re	= me.createReply("inform_alive", reply);

		// Send back the reply and terminate.
		sendMessage(re);
	}
}
