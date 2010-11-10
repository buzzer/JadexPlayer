package jadex.util;

import jadex.util.collection.IndexMap;
import jadex.util.collection.SCollection;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *  This loader supports reloading of modified classes,
 *  and also offers the functionality to add and remove URLs.
 *  A global instance of this class is provided by the static
 *  getInstance() method.
 *  For use in applets and webstart static facade methods are provided,
 *  which implement a fallback to the standard class loader in case
 *  of security restrictions.
 */
public class DynamicURLClassLoader extends ClassLoader
{
	//-------- attributes --------

	/** The added urls (url->classloader). */
	protected IndexMap	urls;
	
	/** Lookup table with timestamps of loaded classes (name->date). */
	protected Map	classdates	= SCollection.createHashMap();
		
	//-------- constructors --------

	/**
	 *  Create a new url class loader.
	 *  @param parent the parent class loader for delegation
	 *  @throws SecurityException if a security manager exists and its
	 *                            <code>checkCreateClassLoader</code> method doesn't allow
	 *                            creation of a class loader.
	 *  @see SecurityManager#checkCreateClassLoader
	 */
	public DynamicURLClassLoader(ClassLoader parent)
	{
		super(parent);
		this.urls	= SCollection.createIndexMap();
	}
	

	//-------- URL handling methods --------
	
	/**
	 *  Appends the specified URL to the list of URLs to search for
	 *  classes and resources.
	 *  Checks if the URL is already contained in the list of urls
	 *  and adds only when URL is new.
	 *
	 *  @param url the URL to be added to the search path of URLs
	 */
	public void addURL(URL url)
	{
		// Add url, if not already present.
		if(!urls.containsKey(url))
		{
			urls.put(url, new InnerURLClassLoader(url, this));
			SReflect.clearClassCache();
		}
	}

	/** 
	 *  Removes the specified URL from the list of URLs to search for
	 *  classes and resources.
	 *
	 *  @param url the URL to be added to the search path of URLs
	 */
	public void	removeURL(URL url)
	{
		// Remove url.
		if(urls.removeKey(url)!=null)
		{
			// Clear caches if url was present.
			SReflect.clearClassCache();
		}
	}
	
	/**
	 *  Reset the class loader (remove all urls).
	 */
	public void	reset()
	{
		if(!urls.isEmpty())
		{
			urls.clear();
			SReflect.clearClassCache();
		}
	}
	
	/**
	 *  Return the URLs of the class loader.
	 */
	public URL[]	getURLs()
	{
		return (URL[])urls.getKeys(URL.class);
	}
	
	//-------- class reloading --------
	
	/**
	 *  Reload a given class.
	 *  Checks the if the class has been modified
	 *  and also reloads anonymous inner classes.
	 *  @return A newly loaded version of the class (if modified)
	 *    or the original class object.
	 */
 	public Class loadModifiedClass(Class clazz)
	{
		try
		{
			// Get the modification date of the class
			ProtectionDomain	pDomain	= clazz.getProtectionDomain();
			CodeSource	cSource	= pDomain.getCodeSource();
			URL loc = cSource.getLocation();
			if(loc.toString().endsWith("/"))
				loc	= new URL(loc, clazz.getName().replace('.','/')+".class");
			URLConnection	uc	= loc.openConnection();
			uc.setUseCaches(false);
			Date	modified	= new Date(uc.getLastModified());
		
			// Reload the class, if modifed.
			if(modified.after(creation))
			{
				Date	last	= (Date)classdates.get(clazz.getName());
				if(last==null || modified.after(last))
				{
					// Hack!!! Create new instance on every reload,
					// because class must not be loaded twice by same class loader.
					InnerURLClassLoader	reloader = new InnerURLClassLoader(cSource.getLocation(), this);
					clazz	= reloader.internalLoadModifiedClass(clazz);
					classdates.put(clazz.getName(), modified);
				}
			}
		}
		catch(IOException e)
		{
			// Shouldn't happen, as the class told us the location...
			e.printStackTrace();
			throw new RuntimeException("Modification date of reloadable class not available: "+clazz);
		}

		return clazz;
	}
	
	//-------- helper methods --------

 	/**
	 *  Get all urls from the classpath including the added ones.
	 */
	public List	getAllClasspathURLs()
	{
		List ret = SCollection.createArrayList();
		StringTokenizer	stok	= new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
		while(stok.hasMoreTokens())
		{
			try
			{
				String	entry	= stok.nextToken();
				File	file	= new File(entry);
				if(file.isDirectory() && !entry.endsWith(System.getProperty("file.separator")))
				{
					// Hack!!! URLClassLoder only considers directories that end with "/".
					entry	+= System.getProperty("file.separator");
				}
				ret.add(new URL("file:///"+entry));
			}
			catch(MalformedURLException e)
			{
				// Maybe invalid classpath entries --> just ignore.
				// Hack!!! Print warning?
				//e.printStackTrace();
			}
		}
		ret.addAll(urls.keySet());
		return ret;
	}

	/**
	 *  Return the package (if any).
	 *  @return The package (e.g. "java.lang") corresponding to a filename.
	 *  	 
	 */
	public String	getPackageOfFile(String filename)
	{
		String	pkg	= null;
		StringTokenizer	stok	= new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
		while(pkg==null && stok.hasMoreElements())
		{
			String	entry	= stok.nextToken();
//			System.out.println(entry);
			entry	= new File(entry).getAbsolutePath();
			pkg = extractPackage(filename, entry);
		}
		if(pkg==null)
		{
			URL[]	urls	= getURLs();
			for(int i=0; pkg==null && i<urls.length; i++)
			{
				String	entry	= urls[i].getFile();
				if(entry.endsWith("/"))
					entry	= entry.substring(0, entry.length()-1);
				pkg = extractPackage(filename, entry);
			}
		}
//		System.out.println("file: "+filename);
//		System.out.println("package: "+pkg);
//		if(pkg==null)
//			System.out.println("classpath: "+System.getProperty("java.class.path"));
		return pkg;
	}


	/**
	 *  Extract the package of the file using the given classpath entry.
	 *  @param filename	The file name.
	 *  @param entry The classpath entry.
	 *  @return The package, if it can be deduced from the classpath entry.
	 */
	protected String extractPackage(String filename, String entry)
	{
		// Normalize file separators (e.g. "/" vs "\").
		filename	= SUtil.replace(filename, System.getProperty("file.separator"), "/");
		entry	= SUtil.replace(entry, System.getProperty("file.separator"), "/");
		
		String	pkg	= null;
		int	index	= filename.indexOf(entry);
		if(index!=-1)
		{
			// Strip classpath part of filename (e.g. ".../classes/").
			pkg	= filename.substring(index + entry.length() + 1);
			
			if(pkg.startsWith("/"))
				pkg	= pkg.substring(1);

			index	= pkg.lastIndexOf("/");
			if(index!=-1)
			{
				pkg	= pkg.substring(0, index);
				pkg	= SUtil.replace(pkg, "/", ".");
			}
		}
		
//		System.out.println("file: "+filename);
//		System.out.println("entry: "+entry);
//		System.out.println("pkg: "+pkg);

		return pkg;
	}

	//-------- class loader methods --------
	
	/**
	 *  Load a given class.
	 */
	public Class loadClass(String name) throws ClassNotFoundException
	{
		// Catch Java NoClassDefFoundError bug. Is thrown when a package name is resolved that
		// ends similar to a class name, e.g. jadex.examples.blackjack.player whereby
		// jadex.examples.blackjack.player.Player exists
		try
		{
			return super.loadClass(name);
		}
		catch(NoClassDefFoundError e)
		{
			throw new ClassNotFoundException(name);
		}
	}
	
	/**
	 *  Unsynchronized reimplementation of method
	 *  to avoid deadlock.
	 *  The dynamic url class loader is just a proxy
	 *  for the inner class loaders, so this should be safe.
	 */
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		// First, check if the class has already been loaded
		Class c = findLoadedClass(name);
		if(c == null)
		{
			try
			{
				if(getParent() != null)
				{
					c = getParent().loadClass(name);
				}
				else
				{
					// Should always have a parent.
					// c = findBootstrapClass0(name);
				}
			}
			catch(ClassNotFoundException e)
			{
				// If still not found, then invoke findClass in order
				// to find the class.
				c = findClass(name);
			}
		}
		if(resolve)
		{
			resolveClass(c);
		}
		return c;
	}

    /**
	 *  Find a class.
	 *  Called when parent class loader couldn't find class.
	 *  Why is this method synchronized?
	 */
	protected synchronized Class	findClass(String name) throws ClassNotFoundException
    {
		Class ret	= null;
		InnerURLClassLoader[]	loaders	= (InnerURLClassLoader[])urls.getObjects(InnerURLClassLoader.class);
		for(int i=0; ret==null && i<loaders.length; i++)
		{
			// Do not use loadClass() to avoid loops.
			ret	= loaders[i].loadClassFromURL(name);
		}
		
		if(ret==null)
			throw new ClassNotFoundException(name);
		
		return ret;
    }
	
	/**
	 *  Find a resource.
	 *  Called when parent class loader couldn't find resource.
	 */
	protected URL findResource(String name)
	{
		URL ret	= null;
		InnerURLClassLoader[]	loaders	= (InnerURLClassLoader[])urls.getObjects(InnerURLClassLoader.class);
		for(int i=0; ret==null && i<loaders.length; i++)
		{
			ret	= loaders[i].findResource(name);
		}
		
		return ret;
	}
	
    //-------- static part --------

	/** The creation date (used to determine if classes have to be reloaded at all). */
	protected static Date	creation	= new Date();

	/** The singleton instance. */
	protected static ClassLoader instance;

	/**
	 *  Retrieve the global Jadex class loader.
	 *  @return The Jadex class loader.
	 */
	public synchronized static ClassLoader getInstance()
	{
		if(instance==null)
		{
			// Hack!!! Might throw exception in applet / webstart.
			try
			{
				instance = new DynamicURLClassLoader(DynamicURLClassLoader.class.getClassLoader());
			}
			catch(SecurityException e)
			{
				// Use standard class loader as fallback.
				instance	= DynamicURLClassLoader.class.getClassLoader();
			}
		}

		return instance;
	}

	//-------- These methods hide implementation details which are not available for applets --------

	/**
	 * Operate on the singleton instance if available.
	 */
	public static void	addURLToInstance(URL url)
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
				((DynamicURLClassLoader)getInstance()).addURL(url);
		}
	}

	/**
	 * Operate on the singleton instance if available.
	 */
	public static void	removeURLFromInstance(URL url)
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
				((DynamicURLClassLoader)getInstance()).removeURL(url);
		}
	}

	/**
	 *  Reset the global Jadex class loader.
	 */
	public static void resetInstance()
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
				((DynamicURLClassLoader)getInstance()).reset();
		}
	}

	/**
	 * Operate on the singleton instance if available.
	 */
	public static URL[]	getURLsFromInstance()
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
				return ((DynamicURLClassLoader)getInstance()).getURLs();
			else
				return new URL[0];
		}
	}

	/**
	 * Operate on the singleton instance if available.
	 */
	public static Class	loadClassWithInstance(String name, boolean resolve) throws ClassNotFoundException
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
				return ((DynamicURLClassLoader)getInstance()).loadClass(name, resolve);
			else
				return Class.forName(name);
		}
	}

	/**
	 * Operate on the singleton instance if available.
	 */
	public static Class	loadModifiedClassWithInstance(Class clazz)
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
				return ((DynamicURLClassLoader)getInstance()).loadModifiedClass(clazz);
			else
				return clazz;
		}
	}
		
	/**
	 *  Return the package (if any).
	 *  @return The package (e.g. "java.lang") corresponding to a filename.
	 *  	 
	 */
	public static String getPackageOfFileFromInstance(String filename)
	{
		synchronized(getInstance())
		{
			if(getInstance() instanceof DynamicURLClassLoader)
			{
				return ((DynamicURLClassLoader)getInstance()).getPackageOfFile(filename);
			}
			else
			{
				return null;
			}
		}
	}

	//-------- helper classes --------
	
	/**
	 *  Extend URLClassLoader to be able to call loadClass().
	 */
	static class InnerURLClassLoader	extends URLClassLoader
	{
		//-------- constructors --------
		
		/**
		 *  Create a new InnerURLClassLoader.
		 *  @param url	The url to load classes from.
		 */
		public InnerURLClassLoader(URL url, ClassLoader parent)
		{
			// Use no parent loader.
			super(new URL[]{url}, parent);
		}
		
		//-------- methods --------
		
		/**
		 *  Find the given class.
		 */
		// Needs not be synchronized as its only called from outer class
		// which should be synchronized.
		protected Class loadClassFromURL(String name)
		{
			// Load class if not already loaded.
			Class	clazz	= findLoadedClass(name);
			if(clazz==null)
			{
				try
				{
					clazz	= super.findClass(name);
				}
				catch(ClassNotFoundException e)
				{
				}
			}
			return clazz;
		}

		/**
		 *  Reload a given class and its inner classes.
		 */
		protected Class internalLoadModifiedClass(Class clazz)
		{
			try
			{
				// Recursively reload inner classes first.
				Class[]	inner	= SReflect.getAnonymousInnerClasses(clazz);
				for(int i=0; i<inner.length; i++)
					internalLoadModifiedClass(inner[i]);
	
				// findClass() of URLClassLoader always loads the given class.
				clazz	= super.findClass(clazz.getName());
				System.out.println("Loaded modified class: "+clazz.getName());
			}
			catch(ClassNotFoundException e)
			{
				// Shouldn't happen, as the class was once
				// successfully loaded...
				throw new RuntimeException("Reloadable class not available: "+clazz);
			}
		
			return clazz;
		}
	}

}
