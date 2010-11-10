package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  The config capability base.
 */
public class MConfigCapabilitybase extends MConfigBase implements IMConfigCapabilitybase
{
	//-------- xml attributes --------

	/** The capability configurations. */
	protected ArrayList capabilities;

	//-------- constructors --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if names of elements in base are unique.
		checkRefUniqueness(this, report, getElements());
	}

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		ret.addAll(capabilities);
		return ret;
	}

	//-------- initial capabilities --------

	/**
	 *  Get all defined capabilities.
	 *  @return The capabilities.
	 */
	public IMConfigCapability[] getCapabilityConfigurations()
	{
		if(capabilities==null)
			return new IMConfigCapability[0];
		return (IMConfigCapability[])capabilities
			.toArray(new IMConfigCapability[capabilities.size()]);
	}

	/**
	 *  Create a new initial capability.
	 *  @param ref	The name of the referenced capability.
	 *  @param state	The configuration.
	 *  @return	The newly created initial capability.
	 */
	public IMConfigCapability	createCapabilityConfiguration(String ref, String state)
	{
		if(capabilities==null)
			capabilities = SCollection.createArrayList();

		MConfigCapability ret = new MConfigCapability();
		ret.setReference(ref);
		ret.setConfiguration(state);
		ret.setOwner(this);
		ret.init();
		capabilities.add(ret);
		return ret;
	}

	/**
	 *  Delete a capability.
	 *  @param capability	The capability to delete.
	 */
	public void	deleteCapabilityConfiguration(IMConfigCapability capability)
	{
		if(!capabilities.remove(capability))
			throw new RuntimeException("Initial capability not found: "+capability);
	}

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected IMElement findOriginalElement()
	{
		return getScope();
	}

	//-------- jibx related --------

	/**
	 *  Add a initialcapability.
	 *  @param initialcapability The initialcapability.
	 */
	public void addInitialCapability(MConfigCapability initialcapability)
	{
		if(capabilities==null)
			capabilities = SCollection.createArrayList();
		capabilities.add(initialcapability);
	}

	/**
	 *  Get an iterator for all initialcapabilitys.
	 *  @return The iterator.
	 */
	public Iterator iterInitialCapabilities()
	{
		return capabilities==null? Collections.EMPTY_LIST.iterator(): capabilities.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigCapabilitybase clone = (MConfigCapabilitybase)cl;
		if(capabilities!=null)
		{
			clone.capabilities = SCollection.createArrayList();
			for(int i=0; i<capabilities.size(); i++)
				clone.capabilities.add(((MElement)capabilities.get(i)).clone());
		}
	}

	/**
	 *  Get the initial configuration for a given capability.
	 *  @param subcap	The subcapability.
	 *  @return	The initial capability configuration.
	 */
	public IMConfigCapability getCapabilityConfiguration(IMCapabilityReference subcap)
	{
		IMConfigCapability[]	inicaps	= getCapabilityConfigurations();
		IMConfigCapability	ret	= null;
		for(int i=0; i<inicaps.length && ret==null; i++)
		{
			if(inicaps[i].getReference().equals(subcap.getName()))
				ret	= inicaps[i];
		}
		return ret;
	}
}
