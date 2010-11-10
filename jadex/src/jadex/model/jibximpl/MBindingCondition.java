package jadex.model.jibximpl;

import java.util.List;

import jadex.model.*;

/**
 *  Condition for bindings.
 */
public class MBindingCondition extends MCondition implements IMBindingCondition
{
	/**
	 *  Get the expression parameters.
	 *  @return
	 */
	public List getSystemExpressionParameters()
	{
		// Make binding parameters available in the condition. (Hack!!!)

		List	exparams	= super.getSystemExpressionParameters();
		IMParameter[] bps;
		if(getOwner() instanceof IMGoal)
		{
			bps = ((IMGoal)getOwner()).getBindingParameters();
		}
		else
		{
			assert getOwner().getOwner() instanceof IMPlan : this;
			bps = ((IMPlan)getOwner().getOwner()).getBindingParameters();
		}

		for(int i=0; i<bps.length; i++)
		{
			exparams.add(new ExpressionParameterInfo(bps[i].getName(), null, bps[i].getClazz()));
		}

		return exparams;
	}
}
