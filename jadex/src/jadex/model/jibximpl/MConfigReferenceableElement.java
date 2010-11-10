package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;

/**
 *  The config referenceable element (an element contained in config bases).
 */
public abstract class MConfigReferenceableElement extends MConfigElement implements IMConfigReferenceableElement
{
	//-------- xml attributes --------

	/** The name of the referenced element. */
	protected String reference;

	//-------- constructors --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		if(getReference()==null)
		{
			report.addEntry(this, "No 'ref' specified.");
		}
		else
		{
			IMElement	orig	= null;
			try
			{
				orig	= findOriginalElement();
			}
			catch(Exception e) {}
			
			if(orig==null)
			{
				report.addEntry(this, "Referenced element '"+getReference()+"' not found.");
			}
			else if(!assignable(orig))
			{
				report.addEntry(this, "Invalid reference specification '"+getReference()+"'. Cannot reference incompatible element '"+SReflect.getInnerClassName(orig.getClass())+"'.");
			}
		}
	}
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., an initialbeliefsetreference can only be a reference to a beliefset(reference),
	 *  and not a belief(reference).
	 */
	protected abstract boolean	assignable(IMElement orig);
	
	
	//-------- xml methods --------

	/**
	 *  Get the name of the referenced element.
	 *  @return The name
	 */
	public String getReference()
	{
		return this.reference;
	}

	/**
	 *  Set the name of the referenced element.
	 *  @param referenced The name.
	 */
	public void setReference(String referenced)
	{
		this.reference = referenced;
	}

	//-------- methods --------

//	/**
//	 *  Get the referenced element.
//	 *  @return The referenced element.
//	 */
//	public IMReferenceableElement getReferencedElement()
//	{
//		// Hack???
//		return (IMReferenceableElement)getOriginalElement();
//	}

	/**
	 *  Create a string representation of this element.
	 */
	public String	toString()
	{
		// Overridden as getName() assert name not null.
		// Use reference as fallback, when name is null.
		String	name	= this.name!=null ? getName() : getReference();
		return SReflect.getInnerClassName(this.getClass())+"("+name+")";
	}

	/**
	 *  Resolve the reference to the original element.
	 */
	protected IMElement findOriginalElement()
	{
		IMElement ret = null;

		if(!(getOwner() instanceof IMConfigBase))
			throw new RuntimeException("Owner not IMInitialBase: "+getOwner()+" "+this);

		IMBase base = (IMBase)((IMConfigBase)this.getOwner()).getOriginalElement();

		if(base==null)
			throw new RuntimeException("Base is null: "+getOwner()+" "+this);
		//assert base!=null: this;

		ret = base.getReferenceableElement(getReference());

		return ret;
	}

}
