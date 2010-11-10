package jadex.model.jibximpl;

import jadex.model.IMConditionReference;
import jadex.model.IMElementReference;
import jadex.model.IMExpressionReference;

/**
 *  A reference to an expression.
 */
public class MExpressionReference extends MElementReference implements IMExpressionReference
{
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMExpressionReference && !(ref instanceof IMConditionReference);
	}
}
