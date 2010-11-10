package jadex.testcases.misc;

import java.io.*;

import jadex.planlib.TestReport;
import jadex.runtime.IEvent;
import jadex.runtime.MobilePlan;


/**
 *  Serialize the agent and write it to a file.
 */
public class SerializationPlan extends MobilePlan
{
	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		TestReport	report	= new TestReport("serialization", "Trying to serialize the agent.");
		try
		{
			ObjectOutputStream	oos	= new ObjectOutputStream(new ByteArrayOutputStream());
			oos.writeObject(getScope().getPlatformAgent());
			oos.close();
			report.setSucceeded(true);
		}
		catch(IOException e)
		{
			report.setReason("Unexpected exception: "+e);
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}
}

