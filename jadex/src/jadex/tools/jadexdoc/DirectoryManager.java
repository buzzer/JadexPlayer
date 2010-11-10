package jadex.tools.jadexdoc;


import java.io.File;
import jadex.model.IMCapability;
import jadex.tools.jadexdoc.doclets.DocletAbortException;


/**
 * Handle the directory creations and the path string generations.
 */
public class DirectoryManager
{

	/**
	 * The file separator string, "/", used in the formation of the URL path.
	 */
	public static final String urlfileseparator = "/";

	/**
	 * The actual platform dependent file separator.
	 * @see java.io.File#separator
	 */
	public static final String fileseparator = File.separator;

	/**
	 * Never instaniated.
	 */
	private DirectoryManager()
	{
	}

	/**
	 * Given a package name, return its URL path string.
	 * @param pd The package name
	 * @see #getPath(String)
	 */
	public static String createPathString(Package pd)
	{
		if(pd==null)
		{
			return "";
		}
		return getPath(pd.getName());
	}


	/**
	 * Given a IMCapability, return the corresponding directory name
	 * with the platform-dependent file separator between subdirectory names.
	 * For example, if name of the capability is  is "DF"  and
	 * the package name is "jadex.runtime.planlib", then it
	 * returns "jadex/runtime/planlib" on Unix and "jadex\runtime\planlib" on Windows.
	 * If name of the package contains no dot, then the value
	 * will be returned unchanged.  Because package names cannot
	 * end in a dot, the return value will never end with a slash.
	 * <p>
	 * Also see getPath for the URL separator version of this method
	 * that takes a string instead of a IMCapability.
	 * @param pd the PackageDoc
	 * @return the platform-dependent directory path for the package
	 */
	public static String getDirectoryPath(IMCapability pd)
	{
		if(pd==null)
		{
			return "";
		}
		String name = pd.getPackage();
		StringBuffer pathstr = new StringBuffer();
		for(int i = 0; i<name.length(); i++)
		{
			char ch = name.charAt(i);
			if(ch=='.')
			{
				pathstr.append(fileseparator);
			}
			else
			{
				pathstr.append(ch);
			}
		}
		return pathstr.toString();
	}

	/**
	 * Given a package name (a string), return the path string,
	 * with the URL separator "/" separating the subdirectory names.
	 * If name of the package contains no dot, then the value
	 * will be returned unchanged.  Because package names cannot
	 * end in a dot, the return value will never end with a slash.
	 * <p>
	 * For example if the string is "jadex.runtime.planlib" then the URL
	 * path string will be "jadex/runtime/panlib".
	 * @param name the package name as a String
	 * @return the String URL path
	 */
	public static String getPath(String name)
	{
		if(name==null || name.length()==0)
		{
			return "";
		}
		StringBuffer pathstr = new StringBuffer();
		for(int i = 0; i<name.length(); i++)
		{
			char ch = name.charAt(i);
			if(ch=='.')
			{
				pathstr.append(urlfileseparator);
			}
			else
			{
				pathstr.append(ch);
			}
		}
		return pathstr.toString();
	}

	/**
	 * Given two package names as strings, return the relative path
	 * from the package directory corresponding to the first string
	 * to the package directory corresponding to the second string,
	 * with the URL file separator "/" separating subdirectory names.
	 * <p>
	 * For example, if the parameter "from" is "java.lang"
	 * and parameter "to" is "java.applet", return string
	 * "../../java/applet".
	 * @param from the package name from which path is calculated
	 * @param to the package name to which path is calculated
	 * @return relative path between "from" and "to" with URL
	 *         separators
	 * @see #getRelativePath(String)
	 * @see #getPath(String)
	 */
	public static String getRelativePath(String from, String to)
	{
		StringBuffer pathstr = new StringBuffer();
		pathstr.append(getRelativePath(from));
		pathstr.append(getPath(to));
		pathstr.append(urlfileseparator);
		return pathstr.toString();
	}

	/**
	 * Given a package name as a string, return relative path string
	 * from the corresponding package directory to the root of
	 * the documentation, using the URL separator "/" between
	 * subdirectory names.
	 * <p>
	 * For example, if the string "from" is "java.lang",
	 * return "../../"
	 * @param from the package name
	 * @return String relative path from "from".
	 * @see #getRelativePath(String, String)
	 */
	public static String getRelativePath(String from)
	{
		if(from==null || from.length()==0)
		{
			return "";
		}
		StringBuffer pathstr = new StringBuffer();
		for(int i = 0; i<from.length(); i++)
		{
			char ch = from.charAt(i);
			if(ch=='.')
			{
				pathstr.append(".."+urlfileseparator);
			}
		}
		pathstr.append(".."+urlfileseparator);
		return pathstr.toString();
	}

	/**
	 * Given a relative or absolute path that might be empty,
	 * convert it to a path that does not end with a
	 * URL separator "/".
	 * @param path the path to convert.  An empty path represents
	 * the current directory.
	 */
	public static String getPathNoTrailingSlash(String path)
	{
		if(path.equals(""))
		{
			return ".";
		}
		if(path.equals("/"))
		{
			return "/.";
		}
		if(path.endsWith("/"))
		{
			// Remove trailing slash
			path = path.substring(0, path.length()-1);
		}
		return path;
	}

	/**
	 * Given a URL path string, this will return the reverse path. For
	 * example, if the URL path string is "java/lang" the method will return
	 * URL specific string "../".
	 */
	public static String getBackPath(String path)
	{
		if(path==null || path.length()==0)
		{
			return "";
		}
		StringBuffer backpath = new StringBuffer();
		for(int i = 0; i<path.length(); i++)
		{
			char ch = path.charAt(i);
			if(ch=='/')
			{
				backpath.append("..");
				backpath.append(urlfileseparator);
			}       // there is always a trailing fileseparator
		}
		return backpath.toString();
	}

	/**
	 * Given a path string create all the directories in the path. For example,
	 * if the path string is "java/applet", the method will create directory
	 * "java" and then "java/applet" if they don't exist. The file separator
	 * string "/" is platform dependent system property.
	 * @param path Directory path string.
	 */
	public static void createDirectory(Configuration configuration,
			String path)
	{
		if(path==null || path.length()==0)
		{
			return;
		}
		File dir = new File(path);
		if(dir.exists())
		{
			return;
		}
		else
		{
			if(dir.mkdirs())
			{
				return;
			}
			else
			{
				configuration.message.error("doclet.Unable_to_create_directory_0", path);
				throw new DocletAbortException();
			}
		}
	}

	/**
	 * Given a package name and a file name, return the full path to that file.
	 * For example, if PackageDoc passed is for "java.lang" and the filename
	 * passed is "package-summary.html", then the string returned is
	 * "java/lang/package-summary.html".
	 * @param pd PackageDoc.
	 * @param filename File name to be appended to the path of the package.
	 */
	public static String getPathToPackage(Package pd, String filename)
	{
		StringBuffer buf = new StringBuffer();
		String pathstr = createPathString(pd);
		if(pathstr.length()>0)
		{
			buf.append(pathstr);
			buf.append("/");
		}
		buf.append(filename);
		return buf.toString();
	}

	/**
	 * Given a package name and a file name, return the full path to that file.
	 * For example, if package for the capability passed is for "jadex.runtime.planlib"
	 * and the filename
	 * passed is "package-summary.html", then the string returned is
	 * "jadex/runtime/planlib/package-summary.html".
	 * @param document IMCapability.
	 * @param filename File name to be appended to the path of the package.
	 */
	public static String getPathToAgent(IMCapability document, String filename)
	{
		StringBuffer buf = new StringBuffer();
		String pathstr = getPath(document.getPackage());
		if(pathstr.length()>0)
		{
			buf.append(pathstr);
			buf.append("/");
		}
		buf.append(filename);
		return buf.toString();
	}


	/**
	 * Given a class name return the full path to the class file.
	 * For example, if ClassDoc passed is for "java.lang.Object" then the
	 * string returned is "java/lang/Object.html".
	 * @param cd Class.
	 */
	public static String getPathToClass(Class cd)
	{
		return getPath(cd.getName())+".html";
	}

}
