package jadex.model;

/**
 *  An element that can be referenced.
 */
public interface IMReferenceableElement extends IMElement
{
	//-------- constants --------

	/** Don't export an element. */
	public static final String EXPORTED_FALSE = "false";

	/** Fully export an element. */
	public static final String EXPORTED_TRUE = "true";

	/** Shielded export of an element. */
	public static final String EXPORTED_SHIELDED = "shielded";


	//-------- exported --------

	/**
	 *  Get the exported flag.
	 *  @return The flag indicating that this element may be referenced by other capabilities.
	 */
	public String	getExported();

	/**
	 *  Set the exported flag.
	 *  @param exported	The flag indicating that this element may be referenced by other capabilities.
	 */
	public void	setExported(String exported);


	//-------- assigntos --------

	/**
	 *  Get the assignto elements (as reference string).
	 *  @return	The assignto elements.
	 */
	public String[]	getAssignTos();

	/**
	 *  Create an assignto element (as reference string).
	 *  @param ref	The reference.
	 */
	public void	createAssignTo(String ref);

	/**
	 *  Delete an assignto element (as reference string).
	 *  @param ref	The reference.
	 */
	public void	deleteAssignTo(String ref);

	
	//-------- not xml related ---------

	/**
	 *  Get the references to which this element is assigned.
	 *  @return The references to which this element is assigned.
	 */
	public IMElementReference[] getAssignToElements();

	/**
	 *  Get the elements scope, that means the
	 *  capability it is contained in.
	 *  @return The capability.
	 * /
	public IMCapability getScope();*/
}
