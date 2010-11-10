package jadex.model.jibximpl;

import jadex.model.*;

/**
 *  The reference for a parameter set element.
 */
public class MParameterSetReference extends MTypedElementSetReference implements IMParameterSetReference
{
	//-------- constructors --------

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMParameterSetReference;
	}
	
	//-------- additional methods --------

	/**
	 *  Init the assign from element.
	 */
	protected IMReferenceableElement	findReferencedElement(String refname)
	{
		IMReferenceableElement ret	= null;
		IMReferenceableElement	ownerref	= ((IMParameterElementReference)getOwner()).getReferencedElement();

		if(ownerref instanceof IMParameterElement)
		{
			ret	= ((IMParameterElement)ownerref).getParameterSet(refname);
		}
		else if(ownerref instanceof IMParameterElementReference)
		{
			ret	= ((IMParameterElementReference)ownerref).getParameterSetReference(refname);
		}

		return ret;
	}
}
