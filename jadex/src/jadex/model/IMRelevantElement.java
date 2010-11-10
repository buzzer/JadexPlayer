package jadex.model;


/**
 *  Helper object to represent from which elements an expression is affected.
 */
public interface IMRelevantElement	extends	IMElement
{

	//-------- reference --------

	/**
	 *  Get the reference to the inhibited goal type.
	 *  @return	The inhibited goal type.
	 */
	public String	getReference();
	
	/**
	 *  Set the reference to the inhibited goal type.
	 *  @param ref	The inhibited goal type.
	 */
	public void	setReference(String ref);

	
	//-------- event type --------

	/**
	 *  Get the event type.
	 *  @return The event type.
	 */
	public String	getEventType();

	/**
	 *  Set the event type.
	 *  @param type	The event type.
	 */
	public void	setEventType(String type);
}
