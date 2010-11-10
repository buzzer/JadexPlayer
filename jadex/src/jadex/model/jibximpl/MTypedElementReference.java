package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;

/**
 *  The typed element reference.
 */
public abstract class MTypedElementReference extends MElementReference implements IMTypedElementReference
{
	//-------- xml attributes --------

	/** The clas name. */
	protected String classname;

	//-------- attributes --------

	/** The class. */
	protected Class clazz;

	//-------- constructors --------
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		if(getClassname()!=null)
		{
			if(SReflect.findClass0(getClassname(), getScope().getFullImports())==null)
				report.addEntry(this, "Class '"+getClassname()+"' not found.");
		}

		String[] astos = getAssignTos();
		for(int i=0; i<astos.length; i++)
		{
			IMReferenceableElement	refelem = findReferencedElement(astos[i]);
			if(refelem instanceof IMTypedElementReference)
			{
				Class	clazz	= ((IMTypedElementReference)refelem).getClazz();
				// The abstract element must be supertype of original element.
				if(clazz!=null && getClazz()!=null && !SReflect.isSupertype(clazz, getClazz()))
					report.addEntry(this, "Abstract element's class is not supertype: "+clazz+" vs. "+getClazz());
			}
		}
		
		if(getReference()!=null)
		{
			IMReferenceableElement	refelem	= findReferencedElement(getReference());
			if(refelem instanceof IMTypedElementReference)
			{
				Class	clazz	= ((IMTypedElementReference)refelem).getClazz();
				// The referenced element must be subtype of original element.
				if(clazz!=null && getClazz()!=null && !SReflect.isSupertype(getClazz(), clazz))
					report.addEntry(this, "Referenced element's class is not subtype: "+getClazz()+" vs. "+clazz);
			}
			else if(refelem instanceof IMTypedElement)
			{
				Class	clazz	= ((IMTypedElement)refelem).getClazz();
				// The referenced element must be subtype of original element.
				if(clazz!=null && getClazz()!=null && !SReflect.isSupertype(getClazz(), clazz))
					report.addEntry(this, "Referenced element's class is not subtype: "+getClazz()+" vs. "+clazz);
			}
		}
	}

	//-------- classname --------

	/**
	 *  Get the classname.
	 *  @return The classname.
	 */
	public String getClassname()
	{
		return this.classname;
	}

	/**
	 *  Set the class name.
	 *  @param classname The classname.
	 */
	public void setClassname(String classname)
	{
		this.classname = classname;
	}

	//-------- clazz --------

	/**
	 *  Get the expected type.
	 *  @return The expected type.
	 */
	public Class	getClazz()
	{
		if(clazz==null && getClassname()!=null)
		{
			//clazz = getScope().getParser().parseType(getClassname());
			clazz = SReflect.findClass0(getClassname(), getScope().getFullImports());
		}
		return clazz;
	}

	/**
	 *  Set the expected value type.
	 *  @param clazz	The expected value type.
	 */
	public void	setClazz(Class clazz)
	{
		this.clazz = clazz;
		this.classname = clazz.getName();
	}

}
