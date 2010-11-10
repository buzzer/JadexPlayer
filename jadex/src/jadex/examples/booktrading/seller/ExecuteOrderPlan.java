package jadex.examples.booktrading.seller;

import jadex.examples.booktrading.common.NegotiationReport;
import jadex.examples.booktrading.common.Order;
import jadex.runtime.Plan;

import java.util.Date;

/**
 * Execute the order by setting execution price and date.
 */
public class ExecuteOrderPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		// Extract order data.
		Integer price = (Integer)getParameter("proposal").getValue();
		Order order = (Order)getParameter("proposal_info").getValue();
		
		// Initiate payment and delivery.
		// IGoal pay = createGoal("payment");
		// pay.getParameter("order").setValue(order);
		// dispatchSubgoalAndWait(pay);
		// IGoal delivery = createGoal("delivery");
		// delivery.getParameter("order").setValue(order);
		// dispatchSubgoalAndWait(delivery);
	
		// Save successful transaction data.
		order.setExecutionPrice(price);
		order.setExecutionDate(new Date());
		
		String report = "Sold for: "+price;
		NegotiationReport nr = new NegotiationReport(order, report, System.currentTimeMillis());
		getBeliefbase().getBeliefSet("negotiation_reports").addFact(nr);

		getParameter("result").setValue(price);
	}
}
