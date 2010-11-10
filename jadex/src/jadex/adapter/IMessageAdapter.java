package jadex.adapter;

import jadex.model.IMMessageEvent;
import jadex.runtime.impl.RCapability;

/**
 *  Interface for external messages to be passed to a Jadex agent.
 *  The methods of this interface are called in the following order:
 *  <ol>
 * 	<li>The system checks if the message is a reply to a message sent earlier
 *      using calling the getValue() method for conversation identifying parameters.
 * 	<li>The match() method is used to check which event type(s) can be used
 *      to represent the message. When the message is a reply,
 *      only event types of correponding capabilities are checked.
 *  <!--<li>The best matching type (automatically determined by the system)
 *      is used to create an event instance which should be filled in the
 *      prepareReceiving() method.-->
 *  <li>The getMessage() method is provided to allow access to the message
 *      representation of the underlying platform (e.g. from plans).
 *      This method may be called several times.
 *  </ol>
 */
public interface IMessageAdapter
{
	/**
	 *  todo: is it possible to remove this method somehow?
	 *  Match a message with a message event.
	 *  @param msgevent The message event.
	 *  @return True, if message matches the message event.
	 */
	public boolean match(IMMessageEvent msgevent);

	/**
	 *  Get the platform message.
	 *  @return The platform specific message.
	 */
	public Object getMessage();

	/**
	 *  Get a parameter or parameter set value.
	 *  The value must possibly converted to the Jadex format,
	 *  e.g. when native agent identifiers are used.
	 *  @return The value or array of values of the parameter (set). 
	 */
	public Object	getValue(String name, RCapability scope);

	/** 
	 *  Get the unique message id.
	 *  @return The id of this message.
	 */
	public String getId();
}
