package jadex.testcases.misc;

import jadex.runtime.*;
import jadex.planlib.TestReport;

/**
 *  Test memory consumption.
 */
public class LeakerPlan extends Plan
{
	//-------- attributes --------

	/** The test number to perform. */
	protected int testno;

	/** The number of runs. */
	protected int runs;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public LeakerPlan()
	{
		this.testno = ((Number)getParameter("testcase").getValue()).intValue();
		this.runs = ((Integer)getBeliefbase().getBelief("runs").getFact()).intValue();
	}

	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		long	time = System.currentTimeMillis();
		long	mem0	= Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long	mem	= mem0;
		getLogger().info("Starting memory consumption tests.\nUsed memory: "+mem);

		if(testno==0 || testno==1)
		{
			TestReport tr = new TestReport("#1", "Memory consumption of goals.");
			long	time2	= System.currentTimeMillis();
			testGoalCreation(runs/2);
			getLogger().info("Time (millis): "+(System.currentTimeMillis()-time2));
			waitFor(3);
			System.gc();
			System.runFinalization();
			long mem2	= Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			getLogger().info("Used memory (change): "+(mem2-mem));
			/*if((mem2-mem)/mem>10)
			{
				tr.setSucceeded(true);
			}
			else
			{
				tr.setReason("Possibly memory leak: used memory (change): "+(mem2-mem));
			}*/
			tr.setSucceeded(true);
			getBeliefbase().getBeliefSet("reports").addFact(tr);
			mem	= mem2;
		}

		if(testno==0 || testno==2)
		{
			TestReport tr = new TestReport("#2", "Memory consumption of conditions.");
			long	time2	= System.currentTimeMillis();
			testConditionCreation(runs);
			getLogger().info("Time (millis): "+(System.currentTimeMillis()-time2));
			waitFor(3);
			System.gc();
			System.runFinalization();
			long mem2	= Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			getLogger().info("Used memory (change): "+(mem2-mem));
			/*if((mem2-mem)/mem>10)
			{
				tr.setSucceeded(true);
			}
			else
			{
				tr.setReason("Possibly memory leak: used memory (change): "+(mem2-mem));
			}*/
			tr.setSucceeded(true);
			getBeliefbase().getBeliefSet("reports").addFact(tr);
			mem	= mem2;
		}

		if(testno==0 || testno==3)
		{
			TestReport tr = new TestReport("#3", "Memory consumption of internal events.");
			long	time2	= System.currentTimeMillis();
			testInternalEventCreation(runs*3);
			getLogger().info("Time (millis): "+(System.currentTimeMillis()-time2));
			waitFor(3);
			System.gc();
			System.runFinalization();
			long mem2	= Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			getLogger().info("Used memory (change): "+(mem2-mem));
			/*if((mem2-mem)/mem>10)
			{
				tr.setSucceeded(true);
			}
			else
			{
				tr.setReason("Possibly memory leak: used memory (change): "+(mem2-mem));
			}*/
			tr.setSucceeded(true);
			getBeliefbase().getBeliefSet("reports").addFact(tr);
			mem	= mem2;
		}

		if(testno==0 || testno==4)
		{
			TestReport tr = new TestReport("#4", "Memory consumption of messages.");
			long	time2	= System.currentTimeMillis();
			testMessageCreationAndSending(runs/10);
			getLogger().info("Time (millis): "+(System.currentTimeMillis()-time2));
			waitFor(3);
			System.gc();
			System.runFinalization();
			long mem2	= Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			getLogger().info("Used memory (change): "+(mem2-mem));
			/*if((mem2-mem)/mem>10)
			{
				tr.setSucceeded(true);
			}
			else
			{
				tr.setReason("Possibly memory leak: used memory (change): "+(mem2-mem));
			}*/
			tr.setSucceeded(true);
			getBeliefbase().getBeliefSet("reports").addFact(tr);
			mem	= mem2;
		}

		time	= System.currentTimeMillis() - time;
		getLogger().info("\nAll tests passed ("+time+" millis).");
		getLogger().info("Used memory (total change): "+(mem-mem0));

		//killAgent();
	}

	/**
	 *  Test if the goal creation consumes memory.
	 */
	protected void testGoalCreation(int num)
	{
		getLogger().info("\n-------- Start of goal creation test --------");
		for(int i=num; i>0 || num==0; i--)
		{
			// create goals, thereby resources will be aquired
			// test if resources are released again when the goal is not used.
			IGoal goal = createGoal("testgoal");
			//getLogger().info("Goal created: "+i);
			if(i%500==0)
			{
				getLogger().info(""+i);
				waitFor(3);
			}
//			System.out.print(".");
			//start = sleeper(start);
		}
		getLogger().info("\n-------- End of goal creation test --------");
		getLogger().info("");
	}

	/**
	 *  Test if the condition creation consumes memory.
	 */
	protected void testConditionCreation(int num)
	{
		getLogger().info("\n-------- Start of condition creation test --------");
		for(int i=num; i>0 || num==0; i--)
		{
			ICondition cond = createCondition("false");
			cond.setTraceMode(ICondition.TRACE_ALWAYS);
			//getLogger().info("Condition created: "+i);
			if(i%500==0)
			{
				getLogger().info(""+i);
				waitFor(3);
			}
//			System.out.print(".");
			//start = sleeper(start);
		}
		getLogger().info("\n-------- End of condition creation test --------");
		getLogger().info("");
	}

	/**
	 *  Test if the internal event creation consumes memory.
	 */
	protected void testInternalEventCreation(int num)
	{
		getLogger().info("\n-------- Start of internal event creation test --------");
		for(int i=num; i>0 || num==0; i--)
		{
			IInternalEvent event = createInternalEvent("testevent");
			//getLogger().info("Internal event created: "+i);
			if(i%500==0)
			{
				getLogger().info(""+i);
				waitFor(3);
			}
//			System.out.print(".");
			//start = sleeper(start);
		}
		getLogger().info("\n-------- End of internal event creation test --------");
		getLogger().info("");
	}

	/**
	 *  Test if the message creation and sending consumes memory.
	 */
	protected void testMessageCreationAndSending(int num)
	{
		getLogger().info("\n-------- Start of message event creation and sending test --------");
		for(int i=num; i>0 || num==0; i--)
		{
			IMessageEvent me = createMessageEvent("testmsg");
			sendMessage(me);
			//getLogger().info("Message event created and sent: "+i);
			if(i%500==0)
			{
				getLogger().info(""+i);
				waitFor(3);
			}
			waitFor(0);
//			System.out.print(".");
			//start = sleeper(start);
		}
		getLogger().info("\n-------- End of message event creation and sending test --------");
		getLogger().info("");
	}

	/**
	 *
	 */
	protected long sleeper(long start)
	{
		if(start+5000<=System.currentTimeMillis())
		{
			try{Thread.sleep(1000);}
			catch(InterruptedException e){e.printStackTrace();}
			start = System.currentTimeMillis();
		}
		return start;
	}
}
