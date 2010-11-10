package jadex.tools.jadexdoc;

import java.io.*;
import java.net.*;
import java.util.*;
import jadex.tools.jadexdoc.doclets.*;


/**
 * Class creates, controls and invokes doclets.
 */
public class DocletInvoker
{
	private final Class docletClass;
	private final ClassLoader appClassLoader;
	private final MessageRetriever messager;

	private final IJadexDoclet doclet;


	/**
	 * Constructor
	 */
	public DocletInvoker(MessageRetriever messager, String docletClassName,
			String docletPath) throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		super();
		this.messager = messager;
		String cpString = null;
		cpString = appendPath(System.getProperty("env.class.path"), cpString);
		cpString = appendPath(System.getProperty("java.class.path"), cpString);
		cpString = appendPath(docletPath, cpString);
		URL[] urls = pathToURLs(cpString);
		appClassLoader = new URLClassLoader(urls);
		docletClass = appClassLoader.loadClass(docletClassName);
		doclet = (IJadexDoclet)docletClass.newInstance();

	}

	/**
	 * Generate documentation here.  Return true on success.
	 */
	public boolean start()
	{
		return doclet.start();
	}

	/**
	 * Check for doclet added options here. Zero return means
	 * option not known.  Positive value indicates number of
	 * arguments to option.  Negative value means error occurred.
	 */
	public int optionLength(String option)
	{
		return doclet.getConfiguration().getOptionLength(option);
	}

	/**
	 * Let doclet check that all options are OK. Returning true means
	 * options are OK.  If method does not exist, assume true.
	 */
	public boolean validOptions(List optlist)
	{
		return doclet.getConfiguration().validOptions(optlist);
	}

	/**
	 * Set options for the configurations.
	 */
	public void setOptions(List options)
	{
		doclet.getConfiguration().setOptions(options);
	}

	/**
	 *
	 * @param agents
	 */
	public void setAgents(List agents)
	{
		doclet.getConfiguration().setAgents(agents);
	}

	/**
	 *
	 * @param packages
	 */
	public void setPackages(List packages)
	{
		doclet.getConfiguration().setPackages(packages);
	}


	/**
	 * Utility method for converting a search path string to an array
	 * of directory and JAR file URLs.
	 * @param path the search path string
	 * @return the resulting array of directory and JAR file URLs
	 */
	static URL[] pathToURLs(String path)
	{
		StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		URL[] urls = new URL[st.countTokens()];
		int count = 0;
		while(st.hasMoreTokens())
		{
			URL url = fileToURL(new File(st.nextToken()));
			if(url!=null)
			{
				urls[count++] = url;
			}
		}
		if(urls.length!=count)
		{
			URL[] tmp = new URL[count];
			System.arraycopy(urls, 0, tmp, 0, count);
			urls = tmp;
		}
		return urls;
	}

	/**
	 * Returns the directory or JAR file URL corresponding to the specified
	 * local file name.
	 * @param file the File object
	 * @return the resulting directory or JAR file URL, or null if unknown
	 */
	static URL fileToURL(File file)
	{
		String name;
		try
		{
			name = file.getCanonicalPath();
		}
		catch(IOException e)
		{
			name = file.getAbsolutePath();
		}
		name = name.replace(File.separatorChar, '/');
		if(!name.startsWith("/"))
		{
			name = "/"+name;
		}
		if(!file.isFile())
		{
			name = name+"/";
		}
		try
		{
			return new URL("file", "", name);
		}
		catch(MalformedURLException e)
		{
			throw new IllegalArgumentException("file");
		}
	}

	/**
	 *
	 * @param path1
	 * @param path2
	 * @return
	 */
	private String appendPath(String path1, String path2)
	{
		if(path1==null || path1.length()==0)
		{
			return path2==null? ".": path2;
		}
		else if(path2==null || path2.length()==0)
		{
			return path1;
		}
		else
		{
			return path1+File.pathSeparator+path2;
		}
	}


	/**
	 *
	 */
	public class DocletInvokeException extends Exception
	{
		public DocletInvokeException()
		{
			super();
		}
	}


}
