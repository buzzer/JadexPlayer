package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.collection.SCollection;

import java.util.*;


/**
 *  Superclass for configurations of the different bases (e.g. goalbase).
 */
public abstract class MConfigBase extends MConfigElement implements IMConfigBase
{
	//-------- constructors --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if names of elements in base are unique.
		checkNameUniqueness(this, report, getElements());
	}

	//-------- methods --------

	/**
	 *  Get the element to be initialized.
	 *  @return The element.
	 */
	public IMElement	getOriginalElement()
	{
		// todo: Hack, use 3 setups? bases->elements->parameters
		if(ref==null)
			ref = findOriginalElement();

		//assert ref!=null: this;
		if(ref==null)
			throw new RuntimeException("Original element of initial element not found: "+this);

		return ref;
	}

	/**
	 *  Get the elements of the base.
	 *  @return All elements.
	 *  // todo: ? cannot return IMInitialElements as MinitialStates are only elements :-(
	 */
	public IMElement[] getElements()
	{
		List ch = getChildren();
		return (IMElement[])ch.toArray(new IMElement[ch.size()]);
	}

	/**
	 *  Check the uniqueness of names among the given elements, and report any errors.
	 */
	protected static void checkNameUniqueness(IMElement owner, Report report, IMElement[] elems)
	{
		Map	melems	= SCollection.createHashMap();
		for(int i=0; i<elems.length; i++)
		{
			String name = elems[i].getName();
			if(name!=null)
			{
				if(!melems.containsKey(name))
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

	/**
	 *  Check that element is referenced only once.
	 */
	protected static void checkRefUniqueness(IMElement owner, Report report, IMElement[] elems)
	{
		Set mrefs = SCollection.createHashSet();
		for(int i=0; i<elems.length; i++)
		{
			try
			{
				IMElement ref = ((IMConfigElement)elems[i]).getOriginalElement();
				if(!mrefs.contains(ref))
				{
					mrefs.add(ref);
				}
				else
				{
					report.addEntry(owner, "Element is referenced more than once: "+ref);
				}
			}
			catch(Exception e)
			{
				// Already handled in doCheck of MInitialElement
			}
		}
	}

}
