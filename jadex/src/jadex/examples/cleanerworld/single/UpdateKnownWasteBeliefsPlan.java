package jadex.examples.cleanerworld.single;

import java.util.*;
import jadex.runtime.Plan;


/**
 *  Clean-up known waste belieset.
 */
public class UpdateKnownWasteBeliefsPlan extends Plan
{

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public UpdateKnownWasteBeliefsPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Collection	coll	= (Collection)getExpression("query_all_vanished_wastes").execute();
		for(Iterator i=coll.iterator(); i.hasNext(); )
		{
			getBeliefbase().getBeliefSet("known_waste_locations").removeFact(i.next());
		}
	}

}
