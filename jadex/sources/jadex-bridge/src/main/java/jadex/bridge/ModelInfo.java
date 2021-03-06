package jadex.bridge;

import jadex.commons.SUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Public model information that is provided as result from 
 *  component factories when a model is loaded.
 */
public class ModelInfo implements IModelInfo
{
	//-------- attributes --------
	
	/** The name. */
	protected String name;
	
	/** The package. */
	protected String packagename;
	
	/** The description. */
	protected String description;
	
	/** The report. */
	protected IErrorReport report;
	
	/** The configurations. */
	protected String[] configurations;
	
	/** The arguments. */
	protected List arguments;
	
	/** The results. */
	protected List results;
	
	/** Flag if startable. */
	protected boolean startable;
	
	/** The filename. */
	protected String filename;
	
	/** The properties. */
	protected Map properties;
	
	/** The classloader. */
	protected ClassLoader classloader;
	
	/** The required services. */
	protected Class[] requiredservices;
	
	/** The provided services. */
	protected Class[] providedservices;
	
	/** The suspend flag. */
	protected IModelValueProvider suspend;
	
	/** The master flag. */
	protected IModelValueProvider master;
	
	/** The daemon flag. */
	protected IModelValueProvider daemon;

	/** The autoshutdown flag. */
	protected IModelValueProvider autoshutdown;
	
	//-------- constructors --------
	
	/**
	 *  Create a new model info.
	 */
	public ModelInfo()
	{
		this(null, null, null, null, null, null, null, 
			false, null, null, null, null, null, null, null, null);
	}
	
	/**
	 *  Create a new model info.
	 */
	public ModelInfo(String name, String packagename,
		String description, IErrorReport report, String[] configurations,
		IArgument[] arguments, IArgument[] results, boolean startable,
		String filename, Map properties, ClassLoader classloader, 
		Class[] requiredservices, Class[] providedservices, 
		IModelValueProvider master, IModelValueProvider daemon, IModelValueProvider autoshutdown)
	{
		this.name = name;
		this.packagename = packagename;
		this.description = description;
		this.report = report;//!=null? report: new ErrorReport();
		this.configurations = configurations;
		this.arguments = arguments!=null? SUtil.arrayToList(arguments): null;
		this.results = results!=null? SUtil.arrayToList(results): null;
		this.startable = startable;
		this.filename = filename;
		this.properties = properties!=null? properties: new HashMap();
		this.classloader = classloader;
		this.requiredservices = requiredservices;
		this.providedservices = providedservices;
		this.master = master;
		this.daemon = daemon;
		this.autoshutdown = autoshutdown;
	}

	//-------- methods --------
	
	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 *  Get the package name.
	 *  @return The package name.
	 */
	public String getPackage()
	{
		return packagename;
	}
	
	/**
	 *  Get the full model name (package.name)
	 *  @return The full name.
	 */
	public String getFullName()
	{
		String pkg = getPackage();
		return pkg!=null && pkg.length()>0? pkg+"."+getName(): getName();
	}
	
	/**
	 *  Get the model description.
	 *  @return The model description.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 *  Get the report.
	 *  @return The report.
	 */
	public IErrorReport getReport()
	{
		return report;
	}
	
	/**
	 *  Get the configurations.
	 *  @return The configuration.
	 */
	public String[] getConfigurations()
	{
		return configurations!=null? configurations: SUtil.EMPTY_STRING_ARRAY;
	}
	
	/**
	 *  Get the arguments.
	 *  @return The arguments.
	 */
	public IArgument[] getArguments()
	{
		return arguments!=null? (IArgument[])arguments.toArray(new IArgument[arguments.size()]): new IArgument[0];
	}
	
	/**
	 *  Get an argument per name.
	 *  @param name The name.
	 *  @return The argument.
	 */
	public IArgument getArgument(String name)
	{
		Argument ret = null;
		if(arguments!=null)
		{
			for(int i=0; i<arguments.size() && ret==null; i++)
			{
				Argument tmp = (Argument)arguments.get(i);
				if(tmp.getName().equals(name))
					ret = tmp;
			}
		}
		return ret;
	}
	
	/**
	 *  Get the results.
	 *  @return The results.
	 */
	public IArgument[] getResults()
	{
		return results!=null? (IArgument[])results.toArray(new IArgument[results.size()]): new IArgument[0];
	}
	
	/**
	 *  Get a result per name.
	 *  @param name The name.
	 *  @return The result.
	 */
	public IArgument getResult(String name)
	{
		Argument ret = null;
		if(results!=null)
		{
			for(int i=0; i<results.size() && ret==null; i++)
			{
				Argument tmp = (Argument)results.get(i);
				if(tmp.getName().equals(name))
					ret = tmp;
			}
		}
		return ret;
	}
	
	/**
	 *  Is the model startable.
	 *  @return True, if startable.
	 */
	public boolean isStartable()
	{
		return startable;
	}
	
	/**
	 *  Get the model type.
	 *  @return The model type (kernel specific).
	 */
//	public String getType();
	
	/**
	 *  Get the filename.
	 *  @return The filename.
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 *  Get the properties.
	 *  Arbitrary properties that can e.g. be used to
	 *  define model-specific settings to configure tools. 
	 *  @return The properties.
	 */
	public Map	getProperties()
	{
		return properties;
	}

	/**
	 *  Return the class loader corresponding to the model.
	 *  @return The class loader corresponding to the model.
	 */
	public ClassLoader getClassLoader()
	{
		return classloader;
	}

	/**
	 *  Set the name.
	 *  @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 *  Set the packagename.
	 *  @param packagename The packagename to set.
	 */
	public void setPackage(String packagename)
	{
		this.packagename = packagename;
	}

	/**
	 *  Set the description.
	 *  @param description The description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 *  Set the report.
	 *  @param report The report to set.
	 */
	public void setReport(IErrorReport report)
	{
		this.report = report;
	}

	/**
	 *  Set the configurations.
	 *  @param configurations The configurations to set.
	 */
	public void setConfigurations(String[] configurations)
	{
		this.configurations = configurations;
	}

	/**
	 *  Set the arguments.
	 *  @param arguments The arguments to set.
	 */
	public void setArguments(IArgument[] arguments)
	{
		this.arguments = arguments!=null? SUtil.arrayToList(arguments): null;
	}
	
	/**
	 *  Add an argument.
	 *  @param argument The argument.
	 */
	public void addArgument(IArgument argument)
	{
		if(arguments==null)
			arguments = new ArrayList();
		arguments.add(argument);
	}

	/**
	 *  Set the results.
	 *  @param results The results to set.
	 */
	public void setResults(IArgument[] results)
	{
		this.results = results!=null? SUtil.arrayToList(results): null;
	}
	
	/**
	 *  Add a result.
	 *  @param result The result.
	 */
	public void addResult(IArgument result)
	{
		if(results==null)
			results = new ArrayList();
		results.add(result);
	}

	/**
	 *  Set the startable.
	 *  @param startable The startable to set.
	 */
	public void setStartable(boolean startable)
	{
		this.startable = startable;
	}

	/**
	 *  Set the filename.
	 *  @param filename The filename to set.
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	/**
	 *  Set the properties.
	 *  @param properties The properties to set.
	 */
	public void setProperties(Map properties)
	{
		this.properties = properties;
	}
	
	/**
	 *  Add a property.
	 */
	public void	addProperty(String name, Object value)
	{
		if(properties==null)
			properties = new HashMap();
		properties.put(name, value);
	}

	// Exclude from transfer?!
	/**
	 *  Set the classloader.
	 *  @param classloader The classloader to set.
	 */
	public void setClassloader(ClassLoader classloader)
	{
		this.classloader = classloader;
	}

	/**
	 *  Get the required services.
	 *  @return The required services.
	 */
	public Class[] getRequiredServices()
	{
		return requiredservices==null? SUtil.EMPTY_CLASS_ARRAY: requiredservices;
	}

	/**
	 *  Set the required services.
	 *  @param required services The required services to set.
	 */
	public void setRequiredServices(Class[] requiredservices)
	{
		this.requiredservices = requiredservices;
	}

	/**
	 *  Get the provided services.
	 *  @return The provided services.
	 */
	public Class[] getProvidedServices()
	{
		return providedservices==null? SUtil.EMPTY_CLASS_ARRAY: providedservices;
	}

	/**
	 *  Set the provided services.
	 *  @param provided services The provided services to set.
	 */
	public void setProvidedServices(Class[] providedservices)
	{
		this.providedservices = providedservices;
	}
	
	/**
	 *  Get the master flag.
	 *  @param configname The configname.
	 *  @return The master flag value.
	 */
	public Boolean getMaster(String configname)
	{
		return master==null? null: (Boolean)master.getValue(configname);
	}
	
	/**
	 *  Get the daemon flag.
	 *  @param configname The configname.
	 *  @return The daemon flag value.
	 */
	public Boolean getDaemon(String configname)
	{
		return daemon==null? null: (Boolean)daemon.getValue(configname);
	}
	
	/**
	 *  Get the autoshutdown flag.
	 *  @param configname The configname.
	 *  @return The autoshutdown flag value.
	 */
	public Boolean getAutoShutdown(String configname)
	{
		return autoshutdown==null? null: (Boolean)autoshutdown.getValue(configname);
	}

	/**
	 *  Get the suspend flag.
	 *  @param configname The configname.
	 *  @return The suspend flag value.
	 */
	public Boolean getSuspend(String configname)
	{
		return suspend==null? null: (Boolean)suspend.getValue(configname);
	}
	
	/**
	 *  Set the master.
	 *  @param master The master to set.
	 */
	public void setMaster(IModelValueProvider master)
	{
		this.master = master;
	}

	/**
	 *  Set the daemon.
	 *  @param daemon The daemon to set.
	 */
	public void setDaemon(IModelValueProvider daemon)
	{
		this.daemon = daemon;
	}

	/**
	 *  Set the autoshutdown.
	 *  @param autoshutdown The autoshutdown to set.
	 */
	public void setAutoShutdown(IModelValueProvider autoshutdown)
	{
		this.autoshutdown = autoshutdown;
	}

	/**
	 *  Set the suspend flag.
	 *  @param suspend The suspend to set.
	 */
	public void setSuspend(IModelValueProvider suspend)
	{
		this.suspend = suspend;
	}
}
