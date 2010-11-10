package jadex.model.jibximpl;

import jadex.model.*;

/**
 *  Configuration for an included capability.
 */
public class MConfigCapability extends MConfigReferenceableElement implements IMConfigCapability
{
	//-------- xml attributes --------

	/** The name of the configuration. */
	protected String configuration;
	
	//-------- configurations --------

	/**
	 *  Get the specified configuration (i.e. predefined configuration).
	 *  @return The configuration name.
	 */
	public String	getConfiguration()
	{
		return configuration;
	}

	/**
	 *  Set the name of the configuration.
	 *  @param configuration	The name of the configuration.
	 */
	public void	setConfiguration(String configuration)
	{
		this.configuration = configuration;
	}

	//-------- methods --------

	/**
	 *  Resolve the reference to the original element.
	 */
	protected IMElement findOriginalElement()
	{
		return getScope().getCapabilityReference(getReference());
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., an initialbeliefsetreference can only be a reference to a beliefset(reference),
	 *  and not a belief(reference).
	 */
	protected boolean assignable(IMElement orig)
	{
		return orig instanceof IMCapabilityReference;
	}
}
