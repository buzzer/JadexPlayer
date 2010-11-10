package jadex.model.jibximpl;

import jadex.model.*;

/**
 *  The parameter reference.
 */
public class MParameterReference extends MTypedElementReference implements IMParameterReference
{
	//-------- constructors --------

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMParameterReference;
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
			ret	= ((IMParameterElement)ownerref).getParameter(refname);
		}
		else if(ownerref instanceof IMParameterElementReference)
		{
			ret	= ((IMParameterElementReference)ownerref).getParameterReference(refname);
		}

		return ret;
	}

	/**
	 *  Init the assign to elements.
	 * /
	protected void initAssignToElements()
	{
		assert !isChecking() : this;
		assert getOwner()!=null;

		String[] asstos = getAssignTos();
		for(int i=0; i<asstos.length; i++)
		{
			String refname = asstos[i];

			IMReferenceableElement refelem = findReferencedElement(refname);
			assert refelem!=null;
			addAssignToElement(refelem);
		}
	} */
}
