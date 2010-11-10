package jadex.planlib;

import jadex.runtime.Plan;

/**
 *  Evaluate proposals using the proposal evaluator interface.
 */
public class CNPEvaluateProposalsPlan extends Plan
{
	/**
	 *  Plan body.
	 */
	public void body()
	{
		Object	cfp	= getParameter("cfp").getValue();
		Object	cfp_info	= getParameter("cfp_info").getValue();
		NegotiationRecord[]	history	= (NegotiationRecord[])getParameterSet("history").getValues();
		ParticipantProposal[]	proposals	= (ParticipantProposal[])getParameterSet("proposals").getValues();

		IProposalEvaluator	evaluator	= (IProposalEvaluator)cfp_info;
		ParticipantProposal[]	acceptables	= evaluator.evaluateProposals(cfp, cfp_info, history, proposals);
		
		getParameterSet("acceptables").addValues(acceptables);
	}
}
