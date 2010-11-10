package jadex.model;

import java.io.Serializable;

import jadex.util.SReflect;

/**
 *  Information for expression parameters. An expression parameter has
 *  an identifying name, an (optional) modelelement, and a clazz
 */
public class ExpressionParameterInfo	implements Serializable
{
	//-------- attributes --------

	/** The parameter name. */
	protected String name;

	/** The parameter modelelement. */
	protected IMElement modelelement;

	/** The parameter runtime type. */
	protected String classname;
	protected Class clazz;

	//-------- constructor --------

	/**
	 *  Create a new expression parameter info.
	 *  @param name The name.
	 *  @param modelelement The modelelement.
	 *  @param classname The classname.
	 */
	public ExpressionParameterInfo(String name, IMElement modelelement, String classname)
	{
		this.name = name;
		this.modelelement = modelelement;
		this.classname = classname;
	}

	/**
	 *  Create a new expression parameter info.
	 *  @param name The name.
	 *  @param modelelement The modelelement.
	 *  @param clazz The runtimetype.
	 */
	public ExpressionParameterInfo(String name, IMElement modelelement, Class clazz)
	{
		this.name = name;
		this.modelelement = modelelement;
		this.clazz = clazz;
	}

	//-------- accessors --------

	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 *  Set the name
	 *  @param name The name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 *  Get the modelelement.
	 *  @return The modelelement.
	 */
	public IMElement getModelElement()
	{
		return modelelement;
	}

	/**
	 *  Set the modelelement.
	 *  @param modelelement The modelelement.
	 */
	public void setModelElement(IMElement modelelement)
	{
		this.modelelement = modelelement;
	}

	/**
	 *  Get the classname.
	 *  @return The classname.
	 */
	public String getClassname()
	{
		if(classname==null && clazz!=null)
			classname = clazz.getName();
		return classname;
	}

	/**
	 *  Set the classname
	 *  @param classname The classname.
	 */
	public void setClassname(String classname)
	{
		this.classname = classname;
	}

	/**
	 *  Get the runtime type.
	 *  @return The runtime type.
	 */
	public Class getClazz()
	{
		if(clazz==null && classname!=null)
		{
			clazz = SReflect.findClass0(classname, null);
		}
		return clazz;
	}

	/**
	 *  Set the runtime type.
	 *  @param clazz The runtimetype.
	 */
	public void setClazz(Class clazz)
	{
		this.clazz = clazz;
	}

	//-------- helper methods --------

	/**
	 *  Test if two elements are equal.
	 */
	public boolean equals(Object o)
	{
		//return o instanceof ExpressionParameterInfo && ((ExpressionParameterInfo)o).getName().equals(getName());
		boolean ret = false;
		if(o instanceof ExpressionParameterInfo)
		{
			ExpressionParameterInfo tst = (ExpressionParameterInfo)o;
			ret = tst.getName().equals(getName())
				&& tst.getClassname().equals(getClassname());
				//&& (tst.getModelElement()==null && getModelElement()==null
				//	|| tst.getModelElement().getName().equals(getModelElement().getName()));
		}
		return ret;
	}

	/**
	 *  Get the hashcode.
	 */
	public int hashCode()
	{
		int ret = getName().hashCode()^getClassname().hashCode();
		//if(getModelElement()!=null)
		//	ret^=getModelElement().getName().hashCode();
		return ret;
	}
}
