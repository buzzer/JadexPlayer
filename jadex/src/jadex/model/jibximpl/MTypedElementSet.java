package jadex.model.jibximpl;

import java.util.Map;

import jadex.model.*;
import jadex.util.SReflect;

/**
 *  A typed element has a type (java class), a multiplicity
 *  and optional some initial values.
 */
public abstract class MTypedElementSet extends MReferenceableElement
{
	//-------- xml attributes --------

	/** The class name. */
	protected String classname;

	/** The update rate. */
	protected long updaterate	= 0;

	/** The transient flag. */
	protected boolean trans	= false;
	
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
			if(refelem instanceof IMTypedElementSetReference)
			{
				Class	clazz	= ((IMTypedElementSetReference)refelem).getClazz();
				// The abstract element must be supertype of original element.
				if(clazz!=null && getClazz()!=null && !SReflect.isSupertype(clazz, getClazz()))
					report.addEntry(this, "Abstract element's class is not supertype: "+clazz+" vs. "+getClazz());
			}
		}
}
	
	//-------- xml methods --------

	/**
	 *  Get the class name.
	 *  @return The class name.
	 */
	public String getClassname()
	{
		return this.classname;
	}

	/**
	 *  Set the class name.
	 *  @param classname The class name.
	 */
	public void setClassname(String classname)
	{
		this.classname = classname;
	}

	/**
	 *  Get the update rate.
	 *  @return The update rate.
	 */
	public long getUpdateRate()
	{
		return this.updaterate;
	}

	/**
	 *  Set the update rate.
	 *  @param updateRate The update rate.
	 */
	public void setUpdateRate(long updateRate)
	{
		this.updaterate = updateRate;
	}

	/**
	 *  Is this element transient.
	 *  Transient beliefs or prameter values are not retained,
	 *  when persisting or migrating an agent. This is useful, e.g.,
	 *  when a value class is not serializable.
	 */
	public boolean	isTransient()
	{
		return trans;
	}
	
	/**
	 *  Change the transient state.
	 *  Transient beliefs or prameter values are not retained,
	 *  when persisting or migrating an agent. This is useful, e.g.,
	 *  when a value class is not serializable.
	 */
	public void	setTransient(boolean trans)
	{
		this.trans	= trans;
	}
	
	//-------- attributes --------

	/** The java clazz of the values. */
	protected Class	clazz;

	//-------- attribute accessors --------

	/**
	 *  Get the class of the values.
	 *  @return	The class of the values.
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
	 *  Set the class.
	 *  @param clazz The clazz.
	 */
	public void setClazz(Class clazz)
	{
		this.clazz = clazz;
		setClassname(getClassname()!=null ? clazz.getName() : null);
	}

	//-------- overridings --------

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		Map representation = super.getEncodableRepresentation();
		representation.put("valueclass", SReflect.getInnerClassName(clazz));
		representation.put("update", ""+getUpdateRate());
		return representation;
	}
}
