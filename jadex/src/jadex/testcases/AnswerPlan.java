package jadex.testcases;

import jadex.runtime.*;


/**
 *  A plan thats sends one or more answers to a request.
 */
public class AnswerPlan	extends Plan
{
	//-------- attributes --------

	/** The number of answers to send. */
	protected int	num;

	/** The delay between the answers. */
	protected long	delay;

	//-------- constructors --------

	/**
	 *  Create an answer plan.
	 */
	public AnswerPlan()
	{
		this(1, 0);
	}

	/**
	 *  Create an answer plan.
	 *  @param num	The number of answers to send.
	 *  @param delay	The delay between the answers.
	 */
	public AnswerPlan(int num, long delay)
	{
		this.num	= num;
		this.delay	= delay;
	}

	//-------- methods --------

	/**
	 *  The body of the plan.
	 */
	public void	body()
	{
		IMessageEvent	event	= (IMessageEvent)getInitialEvent();
		for(int i=1; i<=num; i++)
		{
			IMessageEvent	answer	= event.createReply("inform", ""+i);
			getLogger().info("Sending answer "+i+".");
			sendMessage(answer);
			if(delay>0)
			{
				waitFor(delay);
			}
		}
	}
}

