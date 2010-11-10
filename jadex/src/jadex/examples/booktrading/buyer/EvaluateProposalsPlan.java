package jadex.examples.booktrading.buyer;

import jadex.planlib.ParticipantProposal;
import jadex.runtime.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *  Evaluate the received proposals.
 */
public class EvaluateProposalsPlan extends Plan
{
	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Get order properties and calculate acceptable price.
		int acceptable_price = ((Integer)getParameter("cfp_info").getValue()).intValue();

		ParticipantProposal[] proposals = (ParticipantProposal[])getParameterSet("proposals").getValues();
		
		// Determine acceptables
		List accs = new ArrayList();
		for(int i=0; i<proposals.length; i++)
		{
			if(((Integer)proposals[i].getProposal()).intValue() <= acceptable_price)
				accs.add(proposals[i]);
		}

		// Sort acceptables by price.
		if(accs.size()>1)
		{
			Collections.sort(accs, new Comparator()
			{
				public int compare(Object arg0, Object arg1) {
					return ((Comparable) ((ParticipantProposal)arg0).getProposal())
						.compareTo(((ParticipantProposal)arg1).getProposal());
				}
			});
		}

		if(accs.size()>0)
			getParameterSet("acceptables").addValue(accs.get(0));
	}
}