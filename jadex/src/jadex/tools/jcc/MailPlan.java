package jadex.tools.jcc;

import jadex.runtime.Plan;
import jadex.runtime.externalaccesswrapper.MessageEventWrapper;
import jadex.runtime.impl.IRMessageEvent;
import jadex.runtime.planwrapper.ElementWrapper;


/**
 * A plan to receive and display messages.
 */
public class MailPlan extends Plan
{
	/**
	 * The plan body.
	 */
	public void body()
	{
		final ControlCenter ctrl = (ControlCenter)getBeliefbase().getBelief("jcc").getFact();
		if(ctrl != null)
		{
			ctrl.processMessage(new MessageEventWrapper((IRMessageEvent)((ElementWrapper)getInitialEvent()).unwrap()));
		}
	}
}
