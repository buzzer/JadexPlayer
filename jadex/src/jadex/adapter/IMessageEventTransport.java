package jadex.adapter;

import jadex.runtime.impl.IRMessageEvent;

/**
 *  A message event transport allows a Jadex agent to send a message
 *  over an arbitrary transport.
 */
public interface IMessageEventTransport
{
	/**
	 *  Send a message event.
	 *  @param mevent The message event.
	 */
	public boolean sendMessage(IRMessageEvent mevent);
}
