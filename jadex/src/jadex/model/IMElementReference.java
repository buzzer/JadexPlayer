package jadex.model;

/**
 *  A reference to another element of the same type.
 */
public interface IMElementReference extends IMReferenceableElement
{

	//-------- abstract/required --------

	/**
	 *  Is the element reference abstract (i.e. does not provide an assign-from element).
	 *  @return True, if element is abstract.
	 */
	public boolean isAbstract();

	/**
	 *  Set the element reference to abstract (i.e. does not provide an assign-from element).
	 *  @param abs	True, if element is abstract.
	 */
	public void	setAbstract(boolean abs);


	/**
	 *  Is an implementation of this element required.
	 *  For required references there must be an element in an outer
	 *  capabilitythat references this element with an "assignTo".
	 *  Only valid for abstract references.
	 *  @return True, if element is required.
	 */
	public boolean isRequired();

	/**
	 *  Set the required flag of the reference.
	 *  For required references there must be an element in an outer
	 *  capabilitythat references this element with an "assignTo".
	 *  Only valid for abstract references.
	 *  @param required	The required flag.
	 */
	public void	setRequired(boolean required);

	
	//-------- reference --------
	
	/**
	 *  Get the referenced element name.
	 *  Only for reference elements (=assignFrom element).
	 *  Can null, when abstract.
	 *  @return The reference.
	 */
	public String	getReference();

	/**
	 *  Set the referenced element name.
	 *  Only for reference elements (=assignFrom element).
	 *  Can null, when abstract.
	 *  @param ref	The reference.
	 */
	public void setReference(String ref);


	//-------- not xml related --------

	/**
	 *  Get the referenced element.
	 *  @return The referenced element.
	 */
	public IMReferenceableElement	getReferencedElement();

	/**
	 *  Recursively resolves all references (if any).
	 */
	public IMReferenceableElement	getOriginalElement();
}
