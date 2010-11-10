package jadex.config;

import jadex.model.MessageType;
import jadex.model.SXML;
import jadex.util.ResourceInfo;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *  A configuration contains settings for a platform.
 */
public class Configuration
{
	//-------- constants --------

	/** The Jadex configuration system property. Use with -Dconf=<path/to/jadex.properties> */
	public static String JADEX_CONFIGURATION = "conf";

	/** The platform name. */
	public static final String PLATFORMNAME = "platformname";

	/** The start time name. */
	public static final String STARTTIME = "starttime";

	/** Model caching. */
	public static String MODEL_CACHING = "model_caching";

	/** Model cache auto refresh. */
	public static String MODEL_CACHE_AUTOREFRESH = "model_cache_autorefresh";

	/** Model checking. */
	public static String MODEL_CHECKING = "model_checking";

	/** Expression evaluator. */
	public static String PARSER_NAME = "parser_name";

	/** Write expressions into cache. */
	public static String JANINO_WRITE_CACHE = "janino_write_cache";

	/** Read expressions from cache. */
	public static String JANINO_READ_CACHE = "janino_read_cache";

	/** Reload plan classes. */
	public static String JAVACC_PLAN_RELAODING = "javacc_plan_reloading";

	/** XML Databinding. */
	public static String XML_DATABINDING = "xml_databinding";

	/** Flag to suppress welcome message. */
	public static String NO_WELCOME = "no_welcome";

	/** Property to indicate behavior on JCC exit. */
	public static String JCC_EXIT = "jcc_exit";

	/** Shutdown platform on jcc exit. */
	public static String JCC_EXIT_SHUTDOWN = "jcc_exit_shutdown";

	/** Keep platform running after jcc exit. */
	public static String JCC_EXIT_KEEP = "jcc_exit_keep";

	/** Ask for behavior on jcc exit. */
	public static String JCC_EXIT_ASK = "jcc_exit_ask";

	//-------- attributes --------

	/** The filename. */
	protected String filename;

	/** The properties. */
	protected Properties properties;

	/** The singleton instance. */
	protected static Configuration instance;
	
	/** The fallback properties, when no conf has been specified. */
	protected static String	fallback_conf;

	/** The known message types. */
	protected static Map messagetypes;

	//-------- constructor --------

	/**
	 *  Get the singleton configuration instance.
	 *  The configuration file is searched the following way:
	 *  First the "conf" system property is used.
	 *  If no "conf" is specified, the current directory and the classpath
	 *  is searched for a file "jadex.properties".
	 *  Finally, the fallback (which may have been set from the outside, e.g.,
	 *  the platform) is used.
	 */
	public static synchronized Configuration	getConfiguration()
	{
		if(instance==null)
		{
			// Find configuration file.
			String	filename	= null;
			// Hack!!! Might throw exception in applet / webstart.
			try
			{
				filename	= System.getProperty(Configuration.JADEX_CONFIGURATION);
			}
			catch(SecurityException e){}

			if(filename==null && SUtil.getResourceInfo0("jadex.properties")!=null)
			{
				filename	= "jadex.properties";
			}
			else if(filename==null && fallback_conf!=null)
			{
				filename	= fallback_conf;
			}

			// Load configuration.
			if(filename!=null)
			{
				instance = new Configuration(filename);
			}
			else
			{
				throw new RuntimeException("Jadex started without configuration. Please use java -Dconf=<config>.properties");
			}
		}
		return instance;
	}

	/**
	 *  Set the fallback configuration file.
	 */
	public static void	setFallbackConfiguration(String filename)
	{
		fallback_conf	= filename;
	}
	
	/**
	 *  Create a new runtime for a filename.
	 */
	protected Configuration(String filename)
	{
		assert filename!=null;

	  	// Read Jadex version information from property file.
  		Properties	versionprops	= new Properties();
	  	InputStream	is	= SUtil.getResource0("/jadex/config/version.properties");
	  	if(is!=null)
	  	{
	  		try
			{
				versionprops.load(is);
			}
			catch(IOException e){}
	   	}

	  	// Read jadex configuration from property file.
		this.filename = filename;
		this.properties = new Properties(versionprops);
		ResourceInfo rinfo = SUtil.getResourceInfo0(filename);
		if(rinfo!=null)
		{
			try
			{
				// Adjust the filename if file was found.
				this.filename = rinfo.getFilename();
				properties.load(rinfo.getInputStream());
				rinfo.cleanup();
			}
			catch(IOException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new RuntimeException("Could not load Jadex configuration  file:\n"+sw.toString());
			}

			if(!"true".equalsIgnoreCase(properties.getProperty(NO_WELCOME)))
			{
		 		System.out.println("\n    This is Jadex "
		 	  		+ properties.getProperty("jadex.release.number")+" - "
		 			+ properties.getProperty("jadex.release.date"));
		 		System.out.println("    Using configuration: "+this.filename+"\n");
			}
		}
		else
		{
			throw new RuntimeException("Could not find Jadex configuration file: "+filename);
		}
		/*else
		{
			setModelCaching(true);
			setModelChecking(true);
			setParserName(SParser.PARSER_JANINO);
			setXMLDatabinding(SXML.FACTORY_JIBX);
			setJaninoReadCache(true);
			setJaninoWriteCache(true);
		}*/
	}

	//-------- methods --------

	/**
	 *  Get the file name.
	 */
	public String	getFilename()
	{
		return filename;
	}
	
	/**
	 *  Save the settings to file.
	 */
	public void persist() throws IOException
	{
		assert filename != null;

		try
		{
			FileOutputStream fos = new FileOutputStream(filename);
			properties.store(fos, null); // todo: save comments?!
		}
		catch(IOException e)
		{
			// Failed to save, try current directory.
			FileOutputStream fos = new FileOutputStream("./jadex.properties");
			properties.store(fos, null); // todo: save comments?!
			Logger.getLogger(getClass().getName()).info("No write access to '"+filename+"' saved jadex.properties to current directory.");
			filename	= "./jadex.properties";
		}
	}

	/**
	 *  Get a property per name.
	 *  @return The property.
	 */
	public String getProperty(String name)
	{
		return properties.getProperty(name);
	}

	/**
	 *  Set a property per name.
	 *  @param name The name.
	 *  @param value The value.
	 */
	public void setProperty(String name, String value)
	{
		if(name.equals(PARSER_NAME))
			SXML.clearModelCache(null); // Hack?!
		if(value==null)
		{
			properties.remove(name);
		}
		else
		{
			properties.setProperty(name, value);
		}
	}

	/**
	 *  Get property names.
	 *  @return All property names.
	 */
	public String[] getPropertyNames()
	{
		return (String[])properties.keySet().toArray(new String[properties.size()]);
	}

	/**
	 *  Is model checking.
	 *  True, if model checking is turned on.
	 */
	public boolean isModelChecking()
	{
		boolean ret = false;
		String tmp = properties.getProperty(MODEL_CHECKING);
		if(tmp!=null)
			ret = new Boolean(tmp).booleanValue(); // Hack!!! Use parseBoolean (1.5)
		return ret;
	}

	/**
	 *  Set model checking.
	 */
	public void setModelChecking(boolean check)
	{
		properties.setProperty(MODEL_CHECKING, ""+check);
	}

	/**
	 *  Is model caching.
	 *  True, if model caching is turned on.
	 */
	public boolean isModelCaching()
	{
		boolean ret = false;
		String tmp = properties.getProperty(MODEL_CACHING);
		if(tmp!=null)
			ret = new Boolean(tmp).booleanValue(); // Hack!!! Use parseBoolean (1.5)
		return ret;
	}

	/**
	 *  Set model caching.
	 */
	public void setModelCaching(boolean cache)
	{
		properties.setProperty(MODEL_CACHING, ""+cache);
	}

	/**
	 *  Is model cache refreshing.
	 *  True, if model cache refreshing is turned on.
	 */
	public boolean isModelCacheAutoRefresh()
	{
		boolean ret = false;
		String tmp = properties.getProperty(MODEL_CACHE_AUTOREFRESH);
		if(tmp!=null)
			ret = new Boolean(tmp).booleanValue(); // Hack!!! Use parseBoolean (1.5)
		return ret;
	}

	/**
	 *  Set model caching.
	 */
	public void setModelCacheAutoRefresh(boolean autorefresh)
	{
		properties.setProperty(MODEL_CACHE_AUTOREFRESH, ""+autorefresh);
	}

	/**
	 *  Get the parser class name.
	 */
	public String getParserName()
	{
		return properties.getProperty(PARSER_NAME);
	}

	/**
	 *  Set the parser name.
	 */
	public void setParserName(String name)
	{
		if(!name.equals(properties.getProperty(PARSER_NAME)))
		{
			SXML.clearModelCache(null); // Hack?!
			properties.setProperty(PARSER_NAME, name);
		}
	}

	/**
	 *  Get the XML databinding framework.
	 */
	public String getXMLDatabinding()
	{
		return properties.getProperty(XML_DATABINDING);
	}

	/**
	 *  Set the parser name.
	 */
	public void setXMLDatabinding(String name)
	{
		properties.setProperty(XML_DATABINDING, name);
	}

	/**
	 *  Is expression cache.
	 *  True, janino should read expressions from cache.
	 */
	public boolean isJaninoReadCache()
	{
		boolean ret = false;
		String tmp = properties.getProperty(JANINO_READ_CACHE);
		if(tmp!=null)
			ret = new Boolean(tmp).booleanValue(); // Hack!!! Use parseBoolean (1.5)
		return ret;
	}

	/**
	 *  Set javacc plan reloading.
	 */
	public void setJavaCCPlanReloading(boolean reloading)
	{
		properties.setProperty(JAVACC_PLAN_RELAODING, ""+reloading);
	}

	/**
	 *  Is javacc plan reloading.
	 *  True, javacc should reload modified plan classes.
	 */
	public boolean isJavaCCPlanReloading()
	{
		boolean ret = false;
		String tmp = properties.getProperty(JAVACC_PLAN_RELAODING);
		if(tmp!=null)
			ret = new Boolean(tmp).booleanValue(); // Hack!!! Use parseBoolean (1.5)
		return ret;
	}

	/**
	 *  Set janino read caching.
	 */
	public void setJaninoReadCache(boolean cache)
	{
		properties.setProperty(JANINO_READ_CACHE, ""+cache);
	}

	/**
	 *  Is expression cache.
	 *  True, janino should write expressions to cache.
	 */
	public boolean isJaninoWriteCache()
	{
		boolean ret = false;
		String tmp = properties.getProperty(JANINO_WRITE_CACHE);
		if(tmp!=null)
			ret = new Boolean(tmp).booleanValue(); // Hack!!! Use parseBoolean (1.5)
		return ret;
	}

	/**
	 *  Set janino read caching.
	 */
	public void setJaninoWriteCache(boolean cache)
	{
		properties.setProperty(JANINO_WRITE_CACHE, ""+cache);
	}

	/**
	 *  Get the message type per name.
	 *  @param name The name.
	 *  @return The corresponding message type or null if not found.
	 */
	public MessageType getMessageType(String name)
	{
		MessageType ret = null;
		if(messagetypes==null)
			messagetypes = SCollection.createHashMap();
		String mtname = properties.getProperty(name);
		if(mtname!=null)
		{
			ret = (MessageType)messagetypes.get(mtname);
			if(ret==null)
			{
				try
				{
					Class mtclass = SReflect.classForName(mtname);
					ret = (MessageType)mtclass.newInstance();
					messagetypes.put(mtname, ret);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 *  Get version information.
	 *  @return The release number and release date.
	 * /
	public String[] getVersionInfo()
	{
		String[] ret = null;

		// Hack!!! Use some objec/class to get versioning information.
	  	// Show Jadex version information.
	  	InputStream	is	= SUtil.getResource0("/jadex/config/version.properties");
	  	if(is!=null)
	  	{
			ret = new String[2];
	  		Properties	props	= new Properties();
	  		try
			{
				props.load(is);
		 		ret[0]	= props.getProperty("jadex.release.number");
		 		ret[1]	= props.getProperty("jadex.release.date");
			}
			catch(Exception e)
			{
			}
	   	}

		return ret;
	}*/

	/**
	 *  Get the release number.
	 *  @return The release number.
	 */
	public String getReleaseNumber()
	{
		String ret = null;

		// Hack!!! Use some objec/class to get versioning information.
	  	// Show Jadex version information.
	  	InputStream	is	= SUtil.getResource0("/jadex/config/version.properties");
	  	if(is!=null)
	  	{
	  		Properties	props	= new Properties();
	  		try
			{
				props.load(is);
		 		ret	= props.getProperty("jadex.release.number");
			}
			catch(Exception e)
			{
			}
	   	}

		return ret;
	}

	/**
	 *  Get the release date.
	 *  @return The release date.
	 */
	public String getReleaseDate()
	{
		String ret = null;

		// Hack!!! Use some objec/class to get versioning information.
	  	// Show Jadex version information.
	  	InputStream	is	= SUtil.getResource0("/jadex/config/version.properties");
	  	if(is!=null)
	  	{
	  		Properties	props	= new Properties();
	  		try
			{
				props.load(is);
		 		ret	= props.getProperty("jadex.release.date");
			}
			catch(Exception e)
			{
			}
	   	}

		return ret;
	}
}
