package jadex.model.jibximpl;

import jadex.model.*;

/**
 *  The base class for config elements.
 */
public abstract class MConfigElement extends MElement implements IMConfigElement
{
	//-------- attributes --------

	/** The original element. */
	protected IMElement ref;

	//-------- methods --------

	/**
	 *  Get the element to be initialized.
	 *  @return The element.
	 *  // todo rename!!
	 */
	public IMElement	getOriginalElement()
	{
		if(ref==null)
			ref = findOriginalElement();

		if(ref==null)
			throw new RuntimeException("Original element of initial element not found: "+this);
		//assert ref!=null : this;

		return ref;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 * /
	// handled in MinitialReferenceableElement
	protected void doCheck(IReport report)
	{
		super.doCheck(report);

		if(!(this instanceof IMInitialStatebase))
		{
			try
			{
				getOriginalElement();
			}
			catch(Exception e)
			{
				report.addEntry(this, "Referenced element not found.");
			}
		}
	}*/

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected abstract IMElement findOriginalElement();

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigElement clone = (MConfigElement)cl;
		if(ref!=null)
			clone.ref = (IMElement)((MElement)ref).clone();
	}
}
