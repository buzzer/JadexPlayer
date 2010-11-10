package jadex.model;
/**
 *  The behaviour interface of the typed element set.
 */
public interface IMTypedElementSet extends IMReferenceableElement
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

	
	//--------- update rate ---------

	/**
	 *  Get the update rate.
	 *  @return	The update rate.
	 */
	public long	getUpdateRate();

	/**
	 *  Set the update rate.
	 *  @param updaterate	The update rate.
	 */
	public void setUpdateRate(long updaterate);

	
	// Todo: propagate?

	
	//-------- transient --------
	
	/**
	 *  Is this element transient.
	 *  Transient beliefs or prameter values are not retained,
	 *  when persisting or migrating an agent. This is useful, e.g.,
	 *  when a value class is not serializable.
	 */
	public boolean	isTransient();
	
	/**
	 *  Change the transient state.
	 *  Transient beliefs or prameter values are not retained,
	 *  when persisting or migrating an agent. This is useful, e.g.,
	 *  when a value class is not serializable.
	 */
	public void	setTransient(boolean trans);
	
	
	//-------- not xml related --------

	/**
	 *  Get the initial values.
	 *  @return The initial values.
	 * /
	public IMExpression[] getDefaultValues();*/

	/**
	 *  Get the initial facts.
	 *  @return The initial facts.
	 * /
	public IMExpression[] getDefaultFacts();*/
}
