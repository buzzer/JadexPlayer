package jadex.model;

/**
 *  Base type for referenceable elements in a configuration.
 */
public interface IMConfigReferenceableElement extends IMConfigElement
{

	//-------- reference --------

	/**
	 *  Get the reference name.
	 *  @return	The reference name.
	 */
	public String	getReference();
	
	/**
	 *  Set the reference.
	 *  @param ref	The inhibited goal type.
	 */
	public void	setReference(String ref);
	

	//-------- not xml related ---------

	/**
	 *  Get the referenced element.
	 *  @return The referenced element.
	 */
	//public IMReferenceableElement getReferencedElement();
}
