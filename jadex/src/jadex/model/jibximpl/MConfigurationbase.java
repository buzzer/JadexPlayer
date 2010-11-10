package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  Container for configurations.
 */
public class MConfigurationbase extends MConfigBase implements IMConfigurationbase
{
	//-------- attributes --------

	/** The name of the default configuration. */
	protected String defaultconfig;

	/** The included configurations. */
	protected ArrayList configurations;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(configurations!=null)
			ret.addAll(configurations);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		// Check if names of configurations are unique.
		MBase.checkNameUniqueness(this, report, getConfigurations());
	}

	//-------- configurations --------

	/**
	 *  Get all configurations.
	 *  @return The configurations.
	 */
	public IMConfiguration[] getConfigurations()
	{
		if(configurations==null)
			return new IMConfiguration[0];
		return (IMConfiguration[])configurations
			.toArray(new IMConfiguration[configurations.size()]);
	}

	/**
	 * Get a configuration per name.
	 * @param name The name.
	 * @return The configuration.
	 */
	public IMConfiguration getConfiguration(String name)
	{
		IMConfiguration ret = null;

		for(int i=0; configurations!=null && i<configurations.size() && ret==null; i++)
		{
			IMConfiguration tmp = (IMConfiguration)configurations.get(i);
			if(tmp.getName().equals(name))
				ret = tmp;
		}

		return ret;
	}

	/**
	 *  Create a configuration.
	 *  @param name	The configuration name.
	 *  @return The configuration.
	 */
	public IMConfiguration createConfiguration(String name)
	{
		if(configurations==null)
			configurations = SCollection.createArrayList();

		MConfiguration ret = new MConfiguration();
		ret.setName(name);
		ret.setOwner(this);
		ret.init();
		configurations.add(ret);
		return ret;
	}

	/**
	 *  Delete a configuration.
	 *  @param config	The configuration.
	 */
	public void	deleteConfiguration(IMConfiguration config)
	{
		if(!configurations.remove(config))
			throw new RuntimeException("Configuration not found: "+config);
	}

	/**
	 *  Get the name of the default configuration.
	 *  @return The name of the default configuration.
	 */
	public String getDefaultConfigurationName()
	{
		return defaultconfig;
	}

	/**
	 *  Set the deafult state name.
	 *  @param defaultconfig The name of the default configuration.
	 */
	public void setDefaultConfigurationName(String defaultconfig)
	{
		this.defaultconfig = defaultconfig;
	}

	//-------- non xml related methods --------

	/**
	 *  Get default configuration.
	 *  @return The default configuration (If no default, the first will be returned)
	 */
	public IMConfiguration getDefaultConfiguration()
	{
		String defname = getDefaultConfigurationName();
		IMConfiguration ret = null;
		for(int i=0; configurations!=null && i<configurations.size() && ret==null && defname!=null; i++)
		{
			IMConfiguration tst = (IMConfiguration)configurations.get(i);
			if(tst.getName().equals(defname))
			{
				ret = tst;
			}
		}
		if(ret==null && configurations!=null && configurations.size()>0)
			ret = (IMConfiguration)configurations.get(0);
		return ret;
	}

	/**
	 *  Resolve the reference to the original element.
	 *  @return The original element.
	 */
	protected IMElement findOriginalElement()
	{
		return null;
	}

	//-------- jibx related --------

	/**
	 *  Add a configuration.
	 *  @param configuration The configuration.
	 */
	public void addConfiguration(MConfiguration configuration)
	{
		if(configurations==null)
			configurations = SCollection.createArrayList();
		configurations.add(configuration);
	}

	/**
	 *  Get an iterator for all configurations.
	 *  @return The iterator.
	 */
	public Iterator iterConfigurations()
	{
		return configurations==null? Collections.EMPTY_LIST.iterator(): configurations.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigurationbase clone = (MConfigurationbase)cl;
		if(configurations!=null)
		{
			clone.configurations = SCollection.createArrayList();
			for(int i=0; i<configurations.size(); i++)
				clone.configurations.add(((MElement)configurations.get(i)).clone());
		}
	}
}
