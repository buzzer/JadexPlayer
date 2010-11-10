package jadex.examples.marsworld.sentry;

import jadex.examples.marsworld.*;
import jadex.runtime.*;

/**
 *  Add a new unknown target to test.
 */
public class AddTargetPlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public AddTargetPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		//System.out.println("AddPlan found");
		Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
		IMessageEvent req = (IMessageEvent)getInitialEvent();

		Target ot = (Target)req.getContent();
		Target target = env.getTarget(ot.getId());

		//if(ts.length>0)
		//	System.out.println("Sees: "+SUtil.arrayToString(ts));

		if(target!=null)
		{
			if(!getBeliefbase().getBeliefSet("my_targets").containsFact(target)
			 && !getBeliefbase().getBeliefSet("analysed_targets").containsFact(target))
			{
				//System.out.println("Found a new target: "+target);
				getBeliefbase().getBeliefSet("my_targets").addFact(target);
			}
		}
	}

	//-------- static part --------

	/**
	 *  Get the filter.
	 * /
	public static IFilter getEventFilter()
	{
		MessageTemplate temp = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		MessageFilter filt = new MessageFilter(temp, null, OInformTarget.class);
		return filt;
	}*/
}
