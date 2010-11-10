package jadex.model;

import java.util.List;
import java.util.Map;

/**
 *  The base interface of all model elements.
 */
public interface IMElement
{
	//-------- name	--------
	
	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName();

	/**
	 *  Set the element name.
	 *  @param name The element name.
	 */
	public void	setName(String name);


	//-------- description --------
	
	/**
	 *  Get the description (i.e. a natural language text)
	 *  of the element.
	 *  @return The description text.
	 */
	public String	getDescription();
	
	/**
	 *  Set the description (i.e. a natural language text)
	 *  of the element.
	 *  @param description	The description text.
	 */
	public void	setDescription(String description);

	
	//-------- not xml related --------

	/**
	 *  Get the owner of the element (i.e. the enclosing element).
	 *  @return The owner.
	 */
	public IMElement	getOwner();

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren();

	/**
	 *  Get the elements scope, that means the
	 *  capability it is contained in.
	 *  @return The capability.
	 */
	public IMCapability getScope();

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	// todo: remove.
	public Map getEncodableRepresentation();

	/**
	 *  Get the expression parameters.
	 *  If this element has no local parameters, will return
	 *  the parameters of the owner, or null if the element
	 *  has no owner.
	 */
	public List	getSystemExpressionParameters();

	/**
	 *  Check the validity of the model.
	 *  Returns a report of errors or an empty report
	 *  when the model is valid.
	 */
	public IReport	check();

	/**
	 *  Return the last check report, or generate a new one, if the model
	 *  has not yet been checked.
	 *  @see #check()
	 */
	public IReport	getReport();
	
	/**
	 *  Get the source location of this element.
	 *  @return null, if the source location is unknown.
	 */
	public SourceLocation	getSourceLocation();
}
