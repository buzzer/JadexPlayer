package jadex.model;

/**
 *  An configuration element specifies how to instantiate an element
 *  when an agent is born or terminated.
 */
public interface IMConfigElement extends IMElement
{
	//-------- not xml specific --------
	
	/**
	 *  Get the element to be initialized.
	 *  @return The element.
	 */
	public IMElement	getOriginalElement();
	
}
