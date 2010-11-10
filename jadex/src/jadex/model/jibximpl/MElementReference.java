package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;

/**
 *  This element is a reference to another element.
 */
public abstract class MElementReference extends MReferenceableElement implements IMElementReference
{
	//-------- xml attributes --------

	/** The assign from element name. */
	protected String reference;

	/** Is the element required. */
	protected Boolean required;

	//-------- constructors --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		if(!isAbstract() && reference==null)
		{
			report.addEntry(this, "No 'ref' specified.");
		}
		else if(!isAbstract())
		{
			IMReferenceableElement	refelem	= findReferencedElement(reference);
			if(refelem==null)
			{
				report.addEntry(this, "Cannot resolve reference '"+reference+"'.");
			}
			else if(!((MReferenceableElement)refelem).assignable(this))
			{
				report.addEntry(this, "Invalid reference specification '"+reference+"'. Cannot reference incompatible element type '"+SReflect.getInnerClassName(refelem.getClass())+"'.");
			}
			else if(IMReferenceableElement.EXPORTED_FALSE.equals(refelem.getExported()))
			{
				report.addEntry(this, "Invalid reference specification '"+reference+"'. Cannot reference unexported element '"+refelem.getName()+"'.");
			}
		}
	}
	
	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MElementReference	clone	= (MElementReference)cl;
		if(assignfrom!=null)
			clone.assignfrom	= (IMReferenceableElement)((MReferenceableElement)assignfrom).clone();
			
	}
	
	//-------- xml methods --------

	/**
	 *  Get the reference.
	 *  @return The reference.
	 */
	public String getReference()
	{
		return this.reference;
	}

	/**
	 *  Set the reference.
	 *  @param reference The reference.
	 */
	public void setReference(String reference)
	{
		this.reference = reference;
	}

	/**
	 *  Is the element reference abstract.
	 *  @return True, if element is abstract.
	 */
	public boolean isAbstract()
	{
		return required!=null;
	}

	/**
	 *  Set the element reference to abstract (i.e. does not provide an assign-from element).
	 *  @param abs	True, if element is abstract.
	 */
	public void setAbstract(boolean abs)
	{
		if(abs && required==null)
		{
			required	= Boolean.TRUE;
		}
		else if(!abs && required!=null)
		{
			required	= null;
		}
		//else do nothing
	}
	
	/**
	 *  Is an implementation of this element required.
	 *  Is yes, there must be an element in an outer
	 *  capabilitythat references this element with
	 *  an "assignTo".
	 *  @return True, if element is required.
	 */
	public boolean isRequired()
	{
		return this.required.booleanValue();
	}

	/**
	 *  Set the required state.
	 *  @param required The required state.
	 */
	public void setRequired(boolean required)
	{
		this.required = required ? Boolean.TRUE : Boolean.FALSE;	// Boolean.valueOf(required);	// since 1.4
	}

	//-------- attributes --------

	/** The element this element is assigned to. */
	protected IMReferenceableElement assignfrom;

	//-------- methods --------

	/**
	 *  Set the assigned element.
	 *  @param from The from element.
	 */
	public void setAssignFromElement(IMReferenceableElement from)
	{
		// Must only be called once!
		if(assignfrom!=null)
			System.out.println("hihi");
		assert this.assignfrom==null;
		assert from!=null;
		this.assignfrom = from;
	}

	/**
	 *  Set the assigned element.
	 *  @param from The from element.
	 */
	public void setReferencedElement(IMReferenceableElement from)
	{
		// Must only be called once!
		assert this.assignfrom==null;
		assert from!=null;
		this.assignfrom = from;
	}

	/**
	 *  Get the assigned elements.
	 *  @return The assigned elements.
	 */
	public IMReferenceableElement getReferencedElement()
	{
		if(!isAbstract() && assignfrom==null)
		{
			// Hack!!! Avoid side effect while checking.
			if(isChecking())
			{
				return reference!=null ? findReferencedElement(reference) : null;
			}
			else
			{
				assert reference!=null;
				setAssignFromElement(findReferencedElement(reference));
			}
		}
		return assignfrom;
	}

	/**
	 *  Resolves all references (if any).
	 */
	public IMReferenceableElement	getOriginalElement()
	{
		IMReferenceableElement orig = this;
		while(orig instanceof IMElementReference && getReferencedElement()!=null)
			orig = ((IMElementReference)orig).getReferencedElement();
		return orig;
	}
}
