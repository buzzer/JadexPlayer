package jadex.model;

import jadex.config.Configuration;
import jadex.util.*;

// note for mobile version: comment javax.xml.*-packages, cause they are not supported
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;


/**
 *  This class contains static xml helper methods.
 */
public class SXML
{
	//-------- constants --------

	/** The jibx factory name. */
	public static final String FACTORY_JIBX = "jadex.model.jibximpl.JibxLoader";

	/** The Jadex agent extension. */
	public static final String FILE_EXTENSION_AGENT = ".agent.xml";

	/** The Jadex capability extension. */
	public static final String FILE_EXTENSION_CAPABILITY = ".capability.xml";

	/** The Jadex properties extension. */
	public static final String FILE_EXTENSION_PROPERTIES = ".properties.xml";

	//-------- attributes --------

	/** The loader factory. */
	public static IModelLoader loader;

	//-------- initializers --------

	/**
	 *  Load a model loader.
	 *  @param name The loader name.
	 */
	public static void setFactory(String name)
	{
		try
		{
			loader = (IModelLoader)SReflect.classForName(name).newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Could not initialize Modelloader: "+name);
		}
	}

	/**
	 *  Get the factory name.
	 *  @return The factory name.
	 */
	public static IModelLoader getFactory()
	{
		if(loader==null || !loader.getClass().getName().equals(Configuration.getConfiguration().getXMLDatabinding()))
			setFactory(Configuration.getConfiguration().getXMLDatabinding());
		return loader;
	}

	/**
	 *  Get the factory name.
	 *  @return The factory name.
	 */
	public static String getFactoryName()
	{
		return getFactory().getClass().getName();
	}

	//-------- methods -------

	/**
	 *  Load an xml agent model.
	 *  Creates file name when specified as package
	 *  (eg jadex.examples.Helloworld is converted to jadex/examples/Helloworld.agent.xml).
	 *  @return The loaded agent model.
	 */
	public static IMBDIAgent	loadAgentModel(String xml, String[] imports)
		throws IOException
	{
		return getFactory().loadAgentModel(xml, imports);
	}

	/**
	 *  Load an xml capability model.
	 *  (eg jadex.planlib.DF is converted to jadex/planlib/DF.capability.xml).
	 *  @return The loaded capability model.
	 */
	public static IMCapability	loadCapabilityModel(String xml, String[] imports, IMElement owner)
		throws IOException
	{
		return getFactory().loadCapabilityModel(xml, imports, owner);
	}

	/**
	 *  Load an xml agent model.
	 *  Creates file name when specified as package
	 *  (eg jadex.config.Runtime is converted to jadex/config/runtime.properties.xml).
	 *  @return The loaded property base model.
	 */
	public static IMPropertybase	loadPropertiesModel(String xml, String[] imports, IMElement owner)
		throws IOException 
	{
		return getFactory().loadPropertyModel(xml, imports, owner);
	}

	/**
	 *  Loads any xml Jadex model (e.g. agent or capability).
	 *  Applies the XSLT template to the resource before parsing it.
	 *  Used from Jadexdoc to move comments into the description attributes.
	 *  @return The loaded model.
	 */
	public static IMElement	loadModel(String xml, String xslt) throws IOException
	{
		ResourceInfo rinfo = getResourceInfo(xml, "", null);

		// Apply XSLT (if any).
		// note for mobile version: comment if-block, javax.xml.* not supported, and fully exclude applyXSLT-method
		if(xslt!=null)
		{
			rinfo	= applyXSLT(rinfo, xslt);
		}

		return getFactory().loadModel(rinfo);
	}

	/**
	 *  Clear the model cache.
	 *  Needed for being able to reload models.
	 */
	public static void clearModelCache(String filename)
	{
		ObjectCache cache = getFactory().getModelCache();
		if(cache!=null)
		{
			if(filename!=null)
				cache.remove(filename);
			else
				cache.clear();
		}
	}

	/**
	 *  Persist the model cache.
	 *  Needed for being able to reload models.
	 */
	public static void persistModelCache() throws IOException
	{
		ObjectCache cache = getFactory().getModelCache();
		if(cache!=null)
			cache.persist();
	}

	//-------- helper methods --------
	
	/**
	 *  Load an xml Jadex model.
	 *  Creates file name when specified with or without package.
	 *  Transforms the model via the Jadex default xslt when not null.
	 *  Configures the model via setup() when configurable.
	 *  @param xml The filename | fully qualified classname
	 *  @return The loaded model.
	 */
	// Todo: fix directory stuff!???
	public static ResourceInfo getResourceInfo(String xml, String suffix, String[] imports) throws IOException
	{
		if(xml==null)
			throw new IllegalArgumentException("Required ADF name nulls.");
		if(suffix==null)
			throw new IllegalArgumentException("Required suffix nulls.");

		// Try to find directly as absolute path.
		String resstr = xml;
		ResourceInfo ret = SUtil.getResourceInfo0(resstr);

		if(ret==null || ret.getInputStream()==null)
		{
			// Fully qualified package name? Can also be full package name with empty package ;-)
			//if(xml.indexOf(".")!=-1)
			//{
				resstr	= SUtil.replace(xml, ".", "/") + suffix;
				//System.out.println("Trying: "+resstr);
				ret	= SUtil.getResourceInfo0(resstr);
			//}

			// Try to find in imports.
			for(int i=0; (ret==null || ret.getInputStream()==null) && imports!=null && i<imports.length; i++)
			{
				// Package import
				if(imports[i].endsWith(".*"))
				{
					resstr = SUtil.replace(imports[i].substring(0,
						imports[i].length()-1), ".", "/") + xml + suffix;
					//System.out.println("Trying: "+resstr);
					ret	= SUtil.getResourceInfo0(resstr);
				}
				// Direct import
				else if(imports[i].endsWith(xml))
				{
					resstr = SUtil.replace(imports[i], ".", "/") + suffix;
					//System.out.println("Trying: "+resstr);
					ret	= SUtil.getResourceInfo0(resstr);
				}
			}
		}

		if(ret==null || ret.getInputStream()==null)
			throw new IOException("File "+xml+" not found in imports: "+SUtil.arrayToString(imports));

		return ret;
	}

	/**
	 *  Apply an xslt to an xml and write the result.
	 *  @param is The original xml document stream.
	 *  @param xslt The xslt document.
	 *  @return The resulting ducoment stream.
	 *  @throws IOException
	 */
	protected static ResourceInfo	applyXSLT(ResourceInfo rinfo, String xslt)
		throws IOException
	{
		// note for mobile version: fully exclude this method
		try
		{
			InputStream template = SUtil.getResource(xslt);
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			TransformerFactory tfac = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tfac.newTransformer(new StreamSource(template));
			transformer.transform(new StreamSource(rinfo.getInputStream()), new StreamResult(result));
			// Close streams.
			template.close();
			result.close();
			rinfo.cleanup();
		
			// For debugging dump out.
//			FileOutputStream fos = new FileOutputStream("dump.xml");
//			fos.write(bos.toByteArray());
//			fos.close();
		
			return new ResourceInfo(rinfo.getFilename(), new ByteArrayInputStream(result.toByteArray()), rinfo.getLastModified());
		}
		catch(TransformerException e)
		{
			StringWriter	sw	= new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new IOException(sw.toString());
		}
	}

	/**
	 *  Test if a file is a Java source file.
	 *  @param filename The filename.
	 *  @return True, if it is a Java source file.
	 */
	public static boolean isJavaSourceFilename(String filename)
	{
		return filename!=null && filename.toLowerCase().endsWith(".java");
	}
	
	/**
	 *  Test if a file is a Jadex file.
	 *  @param filename The filename.
	 *  @return True, if it is a Jadex file.
	 */
	public static boolean isJadexFilename(String filename)
	{
		return filename!=null && (filename.toLowerCase().endsWith(FILE_EXTENSION_AGENT)
			|| filename.toLowerCase().endsWith(FILE_EXTENSION_CAPABILITY));
	}

	/**
	 *  Test if a file is an agent file.
	 *  @param filename The filename.
	 *  @return True, if it is an agent file.
	 */
	public static boolean isAgentFilename(String filename)
	{
		return filename!=null && filename.toLowerCase().endsWith(FILE_EXTENSION_AGENT);
	}

	/**
	 *  Test if a file is a capability file.
	 *  @param filename The filename.
	 *  @return True, if it is a capability file.
	 */
	public static boolean isCapabilityFilename(String filename)
	{
		return filename!=null && filename.toLowerCase().endsWith(FILE_EXTENSION_CAPABILITY);
	}

	/**
	 *  Test if a file is a properties file.
	 *  @param filename The filename.
	 *  @return True, if it is a properties file.
	 */
	public static boolean isPropertiesFilename(String filename)
	{
		return filename!=null && filename.toLowerCase().endsWith(FILE_EXTENSION_PROPERTIES);
	}

	/**
	 *  Strip Jadex filename extension.
	 *  @param filename The filename.
	 *  @return The filename without extension.
	 */
	public static String stripJadexExtension(String filename)
	{
		int capend = filename.lastIndexOf(FILE_EXTENSION_CAPABILITY);
		if(capend!=-1)
			return filename.substring(0, capend);

		int agentend = filename.lastIndexOf(FILE_EXTENSION_AGENT);
		if(agentend!=-1)
			return filename.substring(0, agentend);

		int propend = filename.lastIndexOf(FILE_EXTENSION_PROPERTIES);
		if(propend!=-1)
			return filename.substring(0, propend);

		return filename;
	}

	/**
	 *  Strip the short type name from a model filename.
	 *  @param filename The filename.
	 *  @return The short type name.
	 */
	public static String getShortName(String filename)
	{
		String shortname = SXML.stripJadexExtension(filename);

		int namestart = Math.max(shortname.lastIndexOf(File.separatorChar),
			Math.max(shortname.lastIndexOf("/"), shortname.lastIndexOf(".")));
		if(namestart!=-1)
			shortname = shortname.substring(namestart+1);

		return shortname;
	}
}
