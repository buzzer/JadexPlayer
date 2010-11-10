package jadex.model;

/**
 *  The expression parameter.
 */
public interface IMExpressionParameter extends IMElement
{
	//-------- classname --------

	/**
	 *  Get the classname.
	 *  @return classname The classname.
	 */
	public String getClassname();

	/**
	 *  Set the classname.
	 *  @param classname The classname.
	 */
	public void setClassname(String classname);

	//-------- non xml-related --------

	/**
	 *  Get the class.
	 *  @return classname The classname.
	 */
	public Class getClazz();

	/**
	 *  Set the clazz.
	 *  @param clazz The clazz.
	 */
	public void setClazz(Class clazz);
}
