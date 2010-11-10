package jadex.model.jibximpl;

import jadex.model.IMRelevantElement;

/**
 *  The relevant element.
 */
public class MRelevantElement extends MElement implements IMRelevantElement
{
	//-------- xml attributes --------

	/** The event type. */
	protected String eventtype;

	/** The reference name. */
	protected String reference;

	//-------- event methods --------

	/**
	 * Get the event type.
	 * @return The event type.
	 */
	public String getEventType()
	{
		return eventtype;
	}

	/**
	 * Set the event type.
	 * @param type The event type.
	 */
	public void setEventType(String type)
	{
		this.eventtype = type;
	}

	//-------- event methods --------

	/**
	 * Get the reference to the inhibited goal type.
	 * @return	The inhibited goal type.
	 */
	public String getReference()
	{
		return reference;
	}

	/**
	 * Set the reference to the inhibited goal type.
	 * @param ref The inhibited goal type.
	 */
	public void setReference(String ref)
	{
		this.reference = ref;
	}
}
