package jadex.model.jibximpl;

import java.io.*;
import java.util.Map;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;
import jadex.config.Configuration;
import org.jibx.runtime.*;

/**
 *  Class for loading jibx models. 
 */
public class JibxLoader implements IModelLoader
{
	//-------- attributes --------
	
	/** Cached models. */
	protected ObjectCache cache;

	/** The cached filenames. */
	protected Map filenames;

	//-------- constructors --------

	/**
	 *  Create a new JiBX loader.
	 */
	public JibxLoader()
	{
		filenames = SCollection.createHashMap(); // todo: use weak
		// todo: make filename explicit
		//cache = ObjectCache.loadObjectCache("modelcache.cam");
		//if(cache==null)
		//	cache = new ObjectCache("modelcache.cam");
		cache = new ObjectCache("modelcache.cam", false, false);
	}

	//-------- IModelLoader interface --------
	
	/**
	 *  Load an agent model.
	 *  @param name The agent name.
	 *  @param imports	The imports (if any).
	 */
	public IMBDIAgent	loadAgentModel(String name, String[] imports)	throws IOException
	{
		MBDIAgent	ret	= null;

		// Create tuple containing all identifying elements.
		Object[]	keys	= imports!=null? new Object[imports.length+1]: new Object[1];
		keys[0]	= name;
		if(imports!=null)
		System.arraycopy(imports, 0, keys, 1, imports.length);
		// String array provides no usable hashcode.
		Tuple keytuple	= new Tuple(keys);
		String fn = (String)filenames.get(keytuple);
		long lastmod = -1;
		ResourceInfo rinfo = null;

		// Determine the last modified date for the file
		if(fn==null || (Configuration.getConfiguration().isModelCaching()
				&& Configuration.getConfiguration().isModelCacheAutoRefresh()))
		{
			if(fn!=null)
				rinfo = SXML.getResourceInfo(fn, SXML.FILE_EXTENSION_AGENT, null);
			if(rinfo==null)
			{
				rinfo = SXML.getResourceInfo(name, SXML.FILE_EXTENSION_AGENT, imports);
				if(rinfo==null)
					return ret;
				fn = rinfo.getFilename();
				filenames.put(keytuple, fn);
			}
			lastmod = rinfo.getLastModified();
		}

		// Try to load model from cache and use last modified date for up-to-date check
		if(Configuration.getConfiguration().isModelCaching())
		{
			CachedObject co = cache.loadCachedObject(fn.substring(0, fn.length()-3)+"cam", lastmod); // todo:
			if(co!=null)
			{
				MElement master = (MElement)co.getObject();
				ret = (MBDIAgent)master.clone();
				if(rinfo!=null)
					rinfo.cleanup();
			}
		}

		if(ret==null)
		{
			if(rinfo==null)
				rinfo = SXML.getResourceInfo(name, SXML.FILE_EXTENSION_AGENT, imports);
			try
			{
				// Read an object from xml
				IBindingFactory bfa = BindingDirectory.getFactory(MBDIAgent.class);
				IUnmarshallingContext uc = bfa.createUnmarshallingContext();
				ret = (MBDIAgent)uc.unmarshalDocument(rinfo.getInputStream(), null);
				ret.setFilename(rinfo.getFilename());
				ret.setLastModified(rinfo.getLastModified());
				//System.out.println("Could read object: "+ret);
				
				// Initialize loaded model, when valid.
				if(Configuration.getConfiguration().isModelChecking() && ret.getReport().isEmpty())
				{
					ret.setup();
				}
				else
				{
					// When model has errors ignore setup exceptions.
					try
					{
						ret.setup();
					}
					catch(Throwable e){}
				}				

				// Insert model into cache.
				if(Configuration.getConfiguration().isModelCaching())
				{
					// Call check already on master to avoid time for checking in every model
					if(Configuration.getConfiguration().isModelChecking())
						ret.getReport();

					cache.add(new CachedObject(rinfo.getFilename().substring(0, rinfo.getFilename().length()-3)+"cam",
						rinfo.getLastModified(), (Serializable)ret.clone()));
				}
			}
			catch(JiBXException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new IOException(sw.toString());
			}
			finally
			{
				rinfo.cleanup();
			}
		}

		return ret;
	}

	/**
	 *  Load a capability model.
	 *  @param name The capability name.
	 *  @param imports	The imports (if any).
	 *  @param owner The owner.
	 */
	public IMCapability	loadCapabilityModel(String name, String[] imports, IMElement owner)	throws IOException
	{
		MCapability ret = null;

		// Create tuple containing all identifying elements.
		Object[]	keys	= imports!=null? new Object[imports.length+1]: new Object[1];
		keys[0]	= name;
		if(imports!=null)
		System.arraycopy(imports, 0, keys, 1, imports.length);
		// String array provides no usable hashcode.
		Tuple keytuple	= new Tuple(keys);
		String fn = (String)filenames.get(keytuple);
		long lastmod = -1;
		ResourceInfo rinfo = null;

		if(fn==null || (Configuration.getConfiguration().isModelCaching()
				&& Configuration.getConfiguration().isModelCacheAutoRefresh()))
		{
			if(fn!=null)
				rinfo = SXML.getResourceInfo(fn, SXML.FILE_EXTENSION_CAPABILITY, null);
			if(rinfo==null)
			{
				rinfo = SXML.getResourceInfo(name, SXML.FILE_EXTENSION_CAPABILITY, imports);
				if(rinfo==null)
					return ret;
				fn = rinfo.getFilename();
				filenames.put(keytuple, fn);
			}
			lastmod = rinfo.getLastModified();
		}

		if(Configuration.getConfiguration().isModelCaching())
		{
			CachedObject co = cache.loadCachedObject(fn.substring(0, fn.length()-3)+"cam", lastmod);
			if(co!=null)
			{
				MElement master = (MElement)co.getObject();
				ret = (MCapability)master.clone();
				if(rinfo!=null)
					rinfo.cleanup();
			}
		}

		if(ret==null)
		{
			if(rinfo==null)
				rinfo = SXML.getResourceInfo(name, SXML.FILE_EXTENSION_CAPABILITY, imports);

			try
			{
				// Read an object from xml
				IBindingFactory bfa = BindingDirectory.getFactory(MCapability.class);
				IUnmarshallingContext uc = bfa.createUnmarshallingContext();
				ret = (MCapability)uc.unmarshalDocument(rinfo.getInputStream(), null);
				ret.setFilename(rinfo.getFilename());
				ret.setLastModified(rinfo.getLastModified());

				// Insert into cache only valid models.
				if(Configuration.getConfiguration().isModelCaching())
				{
					// Call check already on master to avoid time for checking in every model
					if(Configuration.getConfiguration().isModelChecking())
						ret.getReport();

					cache.add(new CachedObject(rinfo.getFilename().substring(0, rinfo.getFilename().length()-3)+"cam",
						rinfo.getLastModified(), (Serializable)ret.clone()));
				}
				// Must set owner, otherwise doCheck() initializes MMessageEvent without MessageType.
				ret.setOwner(owner);
			}
			catch(JiBXException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new IOException(sw.toString());
			}
			finally
			{
				rinfo.cleanup();
			}
		}

		// Initialize loaded model, when valid.
		//ret.setOwner(owner);
		if(Configuration.getConfiguration().isModelChecking() && ret.getReport().isEmpty())
		{
			ret.setup();
		}
		else
		{
			// When model has errors ignore setup exceptions.
			try
			{
				ret.setup();
			}
			catch(Throwable e){}
		}

		//System.out.println("Loaded: "+ret.getName()+":"+ret.hashCode());
		return ret;
	}

	/**
	 *  Load a properties model.
	 *  @param name The properties name.
	 *  @param imports	The imports (if any).
	 *  @param owner The owner.
	 */
	public IMPropertybase	loadPropertyModel(String name, String[] imports, IMElement owner)	throws IOException
	{
		MPropertybase	ret	= null;

		// Create tuple containing all identifying elements.
		Object[]	keys	= imports!=null? new Object[imports.length+1]: new Object[1];
		keys[0]	= name;
		if(imports!=null)
		System.arraycopy(imports, 0, keys, 1, imports.length);
		// String array provides no usable hashcode.
		Tuple keytuple	= new Tuple(keys);
		String fn = (String)filenames.get(keytuple);
		long lastmod = -1;
		ResourceInfo rinfo = null;

		if(fn==null || (Configuration.getConfiguration().isModelCaching()
			&& Configuration.getConfiguration().isModelCacheAutoRefresh()))
		{
			if(fn!=null)
				rinfo = SXML.getResourceInfo(fn, SXML.FILE_EXTENSION_PROPERTIES, null);
			if(rinfo==null)
			{
				rinfo = SXML.getResourceInfo(name, SXML.FILE_EXTENSION_PROPERTIES, imports);
				if(rinfo==null)
					return ret;
				fn = rinfo.getFilename();
				filenames.put(keytuple, fn);
			}
			lastmod = rinfo.getLastModified();
		}

		if(Configuration.getConfiguration().isModelCaching())
		{
			CachedObject co = cache.loadCachedObject(fn.substring(0, fn.length()-3)+"cam", lastmod);
			if(co!=null)
			{
				MElement master = (MElement)co.getObject();
				ret = (MPropertybase)master.clone();
				if(rinfo!=null)
					rinfo.cleanup();
			}
		}

		if(ret==null)
		{
			if(rinfo==null)
				rinfo = SXML.getResourceInfo(name, SXML.FILE_EXTENSION_PROPERTIES, imports);

			try
			{
				// Read an object from xml
				IBindingFactory bfa = BindingDirectory.getFactory(MPropertybase.class);
				IUnmarshallingContext uc = bfa.createUnmarshallingContext();
				ret = (MPropertybase)uc.unmarshalDocument(rinfo.getInputStream(), null);

				// Insert into cache only valid models.
				if(Configuration.getConfiguration().isModelCaching())
				{
					cache.add(new CachedObject(rinfo.getFilename().substring(0, rinfo.getFilename().length()-3)+"cam"
						, rinfo.getLastModified(), (Serializable)ret.clone()));
				}
			}
			catch(JiBXException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new IOException(sw.toString());
			}
			finally
			{
				rinfo.cleanup();
			}
		}
		
		ret.setOwner(owner);
		// Hack!!! Do not init external property bases.
//		// Initialize loaded model, when valid.
//		if(Report.isChecking() && ret.getReport().isEmpty())
//		{
//			ret.setup();
//		}
//		else
//		{
//			// When model has errors ignore setup exceptions.
//			try
//			{
//				ret.setup();
//			}
//			catch(Throwable e){}
//		}

		return ret;
	}

	/**
	 *  Loads any xml Jadex model (e.g. agent, capability, or propertybase).
	 *  The model is loaded from the input stream (and therefore not cached.
	 *  Used from Jadexdoc.
	 *  @return The loaded model.
	 */
	public IMElement	loadModel(ResourceInfo rinfo)	throws IOException
	{
		MElement	ret	= null;
//		Tuple	keytuple	= null;
//		
//		if(Configuration.getConfiguration().isModelCaching())
//		{
//			// Create tuple containing all identifying elements.
//			keytuple	= new Tuple(new Object[]{rinfo.getFilename()});
//			ret = findModel(keytuple);
//		}

		//if(ret==null)
		//{
			try
			{
				// Read an object from xml
				IBindingFactory bfa = BindingDirectory.getFactory(MElement.class);
				IUnmarshallingContext uc = bfa.createUnmarshallingContext();
				ret = (MElement)uc.unmarshalDocument(rinfo.getInputStream(), null);
	
				if(ret instanceof MCapability)
				{
					((MCapability)ret).setFilename(rinfo.getFilename());
					((MCapability)ret).setLastModified(rinfo.getLastModified());
				}
	
//				// Must be cloned before ret.getReport() is called as it inits the agents accidently.
//				Object clone = ret.clone();
//				// Insert into cache
//				if(Configuration.getConfiguration().isModelCaching())// && ret.getReport().isEmpty())
//					models.put(keytuple, clone);

				// Initialize loaded model, when valid.
				if(Configuration.getConfiguration().isModelChecking() && ret.getReport().isEmpty())
				{
					ret.setup();
				}
				else
				{
					// When model has errors ignore setup exceptions.
					try
					{
						ret.setup();
					}
					catch(Throwable e){}
				}
			}
			catch(JiBXException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new IOException(sw.toString());
			}
			finally
			{
				rinfo.cleanup();
			}
		//}
		return ret;
	}

	/**
	 *  Get the model cache.
	 *  @return The model cache.
	 */
	public ObjectCache getModelCache()
	{
		return cache;
	}

	/**
	 *  Clear the model cache.
	 *  Needed for being able to reload models.
	 *  @param filename The filename.
	 */
	public void clearModelCache(String filename)
	{
		if(filename==null)
			cache.clear();
		else
			cache.remove(filename);
	}

}
