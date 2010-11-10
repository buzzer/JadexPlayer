package jadex.model;

/**
 *  A reference to a typed element set.
 */
public interface IMTypedElementSetReference extends IMElementReference
{
	//-------- class --------

	/**
	 *  Get the class of the values.
	 *  @return	The class of the values.
	 */
	public Class	getClazz();

	/**
	 *  Set the class.
	 *  @param clazz The clazz.
	 */
	public void setClazz(Class clazz);
}
