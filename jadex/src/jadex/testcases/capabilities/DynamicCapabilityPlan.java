package jadex.testcases.capabilities;

import jadex.adapter.fipa.SFipa;
import jadex.model.*;
import jadex.planlib.TestReport;
import jadex.runtime.IMessageEvent;
import jadex.runtime.Plan;
import jadex.runtime.TimeoutException;
import jadex.util.Tuple;

/**
 *  Plan to dynamically add the translation capability
 *  and a reference to the egwords beliefset.
 */
public class DynamicCapabilityPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void	body()
	{
		// "translate english_german dog" -> sends back a not understood
		requestTranslation("1st_request", SFipa.NOT_UNDERSTOOD);
		
		// "add capability" (adds a translation capability)
		addCapability();

		// "translate english_german dog" -> sends back an inform with dog - Hund
		requestTranslation("2nd_request", SFipa.INFORM);
		
		// "remove capability" (removes the translation capability)
		removeCapability();

		// "translate english_german dog" -> sends back a not understood
		requestTranslation("3rd_request", SFipa.NOT_UNDERSTOOD);
	}
	
	//-------- helper methods --------
	
	/**
	 *  Request a translation and check if the answer has correct performative.
	 */
	protected void	requestTranslation(String testname, String performative)
	{
		TestReport	report	= new TestReport(testname, "Request translation (expecting '"+performative+"')");
		try
		{
			
			IMessageEvent	rtm	= createMessageEvent("request_translation");
			IMessageEvent	res	= sendMessageAndWait(rtm, 3000);
			if(performative.equals(res.getParameter(SFipa.PERFORMATIVE).getValue()))
			{
				report.setSucceeded(true);
			}
			else
			{
				report.setReason("Wrong result message: "+res);
			}
		}
		catch(TimeoutException e)
		{
			report.setReason("No result message received.");
		}
		getBeliefbase().getBeliefSet("reports").addFact(report);
	}

	/**
	 *  Add the capability.
	 */
	protected void addCapability()
	{
		// Load and register capability.
		IMCapabilityReference	subcap	= ((IMCapability)getScope().getModelElement())
			.createCapabilityReference("transcap", "jadex.tutorial.TranslationD2");
		getScope().registerSubcapability(subcap);
		getLogger().info("Capability transcap successfully added.");
		
		// Create and register beliefset reference.
		IMBeliefSetReference	belsetref	= ((IMBeliefbase)getScope().getBeliefbase().getModelElement())
			.createBeliefSetReference("egwordsref", Tuple.class, IMReferenceableElement.EXPORTED_FALSE, "transcap.egwords");
		getScope().getBeliefbase().registerBeliefSetReference(belsetref);
		getLogger().info("Beliefset reference egwordsref successfully added.");
	}

	/**
	 *  Remove the capability.
	 */
	protected void removeCapability()
	{
		// Deregister and delete beliefset reference.
		IMBeliefSetReference	belsetref	= ((IMBeliefbase)getScope().getBeliefbase().getModelElement())
			.getBeliefSetReference("egwordsref");
		getScope().getBeliefbase().deregisterBeliefSetReference(belsetref);
		((IMBeliefbase)getScope().getBeliefbase().getModelElement()).deleteBeliefSetReference(belsetref);
		getLogger().info("Beliefset reference egwordsref successfully added.");

		// Deregister and unload capability.
		IMCapabilityReference	subcap	= ((IMCapability)getScope().getModelElement())
			.getCapabilityReference("transcap");
		getScope().deregisterSubcapability(subcap);
		((IMCapability)getScope().getModelElement()).deleteCapabilityReference(subcap);
		getLogger().info("Capability transcap successfully removed.");
	}
}
