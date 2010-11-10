package jadex.benchmarks;

import javax.swing.JFrame;
import javax.swing.JLabel;

import jadex.runtime.Plan;

/**
 *  Plan for receiving messages.
 */
public class MessageReceiverPlan extends Plan
{
	/**
	 *  Create a new plan body.
	 */
	public void body()
	{
		int msgcnt = ((Integer)getBeliefbase().getBelief("msg_cnt").getFact()).intValue();
		int received = ((Integer)getBeliefbase().getBelief("received").getFact()).intValue();
		received++;
		getBeliefbase().getBelief("received").setFact(new Integer(received));
		
		if(received==msgcnt)
		{
			long starttime = ((Long)getBeliefbase().getBelief("starttime").getFact()).longValue();
			long dur = System.currentTimeMillis() - starttime;
			getLogger().info("Sending/receiving " + msgcnt + " messages took: " + dur + " milliseconds.");
			killAgent();
		}
	}
}
