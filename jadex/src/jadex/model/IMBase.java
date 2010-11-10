package jadex.model;

/**
 *  Interface for all bases = element containers.
 */
public interface IMBase extends IMElement
{

	//-------- not xml related --------
	
	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public IMReferenceableElement[]	getReferenceableElements();

	/**
	 *  Get an element contained in the base by name.
	 *  @return The element.
	 */
	public IMReferenceableElement	getReferenceableElement(String name);

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem);

	/**
	 *  Find elements referencing the given element using assign-from.
	 *  @param element	The referenced element.
	 *  @return The element references.
	 */
	public IMElementReference[]	getElementReferences(IMReferenceableElement element);

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public IMBase	getCorrespondingBase(IMCapability scope);
}
