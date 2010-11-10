package jadex.examples.ping.mobile;

import jadex.runtime.*;

/**
 *  The ping plan reacts on ping requests.
 */
public class PingPlan extends MobilePlan
{
	//-------- attributes --------

	/** The ping string. */
	protected String ping;

	/** The reply string. */
	protected String reply;

	//-------- constructors --------

	/**
	 *  Create a new ping plan.
	 */
	public PingPlan()
	{
		this("ping", "alive");
	}

	/**
	 *  Create a new ping plan.
	 *  @param ping The string to react on.
	 *  @param reply The string to send back.
	 */
	public PingPlan(String ping, String reply)
	{
		this.ping = ping;
		this.reply = reply;
	}

	//-------- methods --------

	/**
	 *  Handle the ping request.
	 */
	public void action(IEvent event)
	{
		// Event must be incoming message.
		IMessageEvent	me	= (IMessageEvent)event;

		// Create reply message.
		IMessageEvent	re	= me.createReply("inform", reply);

		// Send reply.
		sendMessage(re);
	}
}
