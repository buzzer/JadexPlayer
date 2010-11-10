package jadex.runtime.impl;

import java.util.Iterator;

/**
 *  Interface for pure runtime conditions.
 */
public interface IInterpreterCondition extends ICleanable
{
	/**
	 *  Execute the condition's action when it is triggered.
	 *  @param events The systems events that occurrred.
	 */
	public void	systemEventsOccurred(Iterator events);
}
