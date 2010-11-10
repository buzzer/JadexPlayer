package jadex.testcases.planlib;

import jadex.runtime.Plan;


/**
 *  Decides if the actual price of the auction is acceptable.
 */
public class EAMakeProposalPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		Double maxprice = (Double)getBeliefbase().getBelief("max_price").getFact();
		Double price = (Double)getParameter("cfp").getValue();
		getParameter("accept").setValue(new Boolean(price.doubleValue() <= maxprice.doubleValue()));
	}
}
