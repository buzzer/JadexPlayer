package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.util.List;
import java.util.Map;

/**
 *  Base class for all bases.
 */
public abstract class MBase extends MElement implements IMBase
{
	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		ret.addAll(SUtil.arrayToList(getReferenceableElements()));
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if names of elements in base are unique.
		checkNameUniqueness(this, report, getReferenceableElements());
	}

	//--------  methods --------

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public abstract IMReferenceableElement[]	getReferenceableElements();

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public abstract IMBase	getCorrespondingBase(IMCapability scope);

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public abstract void deleteReferenceableElement(IMReferenceableElement elem);

	/**
	 *  Get an elements contained in the base by name.
	 *  Does not distinguish between original elements and references.
	 *  @return The element.
	 */
	public IMReferenceableElement	getReferenceableElement(String name)
	{
		IMReferenceableElement	ret	= null;
		IMReferenceableElement[]	elements	= getReferenceableElements();
		for(int i=0; ret==null && i<elements.length; i++)
		{
			if(name.equals(elements[i].getName()))
			{
				ret	= elements[i];
			}
		}
		return ret;
	}

	/**
	 *  Find elements referencing the given element using assign-from.
	 *  @param element	The referenced element.
	 *  @return The element references.
	 */
	public IMElementReference[]	getElementReferences(IMReferenceableElement element)
	{
		List	ret	= SCollection.createArrayList();
		IMReferenceableElement[]	elements	= getReferenceableElements();
		for(int i=0; i<elements.length; i++)
		{
			if(elements[i] instanceof IMElementReference)
			{
				if(((IMElementReference)elements[i]).getReferencedElement()==element)
				{
					ret.add(elements[i]);
				}
			}
		}
		return (IMElementReference[])ret.toArray(new IMElementReference[ret.size()]);
	}

	//-------- helper methods --------

	/**
	 *  Check the uniqueness of names among the given elements, and report any errors.
	 *  Also complains about elements where the name nulls.
	 */
	protected static void checkNameUniqueness(IMElement owner, Report report, IMElement[] elems)
	{
		Map	melems	= SCollection.createHashMap();
		for(int i=0; i<elems.length; i++)
		{
			if(((MElement)elems[i]).name==null)
			{
				report.addEntry(owner, "Element requires a name.");
			}
			else
			{
				if(!melems.containsKey(elems[i].getName()))
				{
					melems.put(elems[i].getName(), elems[i]);
				}
				else
				{
					report.addEntry(owner, "Name conflict between "+melems.get(elems[i].getName())+" and "+elems[i]);
				}
			}
		}
	}
}
