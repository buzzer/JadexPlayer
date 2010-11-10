package jadex.examples.cleanerworld.single;

import java.util.ArrayList;
import jadex.runtime.*;

/**
 *  Update the beliefbase because new waste has been detected.
 */
public class WasteSeenPlan extends Plan
{
	//-------- attributes --------

	IExpression newwastelocs;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public WasteSeenPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		ArrayList newlocs = (ArrayList)getExpression("query_new_waste_location").execute();
		for(int i=0; i<newlocs.size(); i++)
		{
			//System.out.println("New waste seen at: "+newlocs.get(i));
			getBeliefbase().getBeliefSet("known_waste_locations").addFact(newlocs.get(i));
		}
	}
}
