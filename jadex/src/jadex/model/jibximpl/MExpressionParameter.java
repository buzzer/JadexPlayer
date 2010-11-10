package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;


/**
 *  The parameter for expressions.
 */
public class MExpressionParameter extends MElement implements IMExpressionParameter
{
	//-------- xml attributes --------

	/** The classname. */
	protected String classname;

	//-------- attributes --------

	/** The clazz. */
	protected Class clazz;

	//-------- constructors --------
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if class can be found.
		if(getClassname()!=null && SReflect.findClass0(getClassname(), getScope().getFullImports())==null)
			report.addEntry(this, "Unknown parameter type: "+getClassname());
	}

	//-------- classname --------

	/**
	 *  Get the classname.
	 *  @return classname The classname.
	 */
	public String getClassname()
	{
		return classname;
	}

	/**
	 *  Set the classname.
	 *  @param classname The classname.
	 */
	public void setClassname(String classname)
	{
		this.classname = classname;
	}

	//-------- non xml-related --------

	/**
	 *  Get the class.
	 *  @return classname The classname.
	 */
	public Class getClazz()
	{
		if(clazz==null && getClassname()!=null)
		{
			//clazz = getScope().getParser().parseType(getClassname());
			clazz = SReflect.findClass0(getClassname(), getScope().getFullImports());
		}
		return clazz;
	}

	/**
	 *  Set the clazz.
	 *  @param clazz The clazz.
	 */
	public void setClazz(Class clazz)
	{
		this.clazz	= clazz;
		setClassname(clazz==null? null: clazz.getName());
	}
	
}
