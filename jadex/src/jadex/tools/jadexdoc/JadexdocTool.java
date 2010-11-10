package jadex.tools.jadexdoc;

import java.io.File;
import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.doclets.MessageRetriever;

/**
 *  The Jadexdoc tool.
 */
public class JadexdocTool
{
	private final MessageRetriever messager;

	/** The string used as separator in class paths. */
	private static final char pathSep = File.pathSeparatorChar;

	/** The system-dependent path-separator character, represented as a string for convenience. */
	public static final String pathSeparator = File.pathSeparator;

	private String classPath;
	private String sourceClassPath;

	private List agents;
	private List packages;


	/**
	 *  Create a new jadexdoc.
	 */
	protected JadexdocTool(MessageRetriever messager, List options)
	{
		this.messager = messager;
		setPaths(options);
	}

	/**
	 * 
	 * @param jadexNames
	 * @param subPackages
	 * @param excludedPackages
	 * @return
	 */
	public boolean setJadexNames(List jadexNames, List subPackages, List excludedPackages)
	{
		List pkgnames = new ArrayList();

		Set packages = new HashSet();
		Set agents = new HashSet();

		for(int i = 0; i<jadexNames.size(); i++)
		{
			String name = (String)jadexNames.get(i);
			if(SXML.isJadexFilename(name))
			{// && new File(name).exists()) {
				messager.notice("main.Loading_source_file", name);
				IMCapability model = loadCapability(name);
				if(model!=null)
				{
					agents.add(model);
					packages.add(model.getPackage()==null? "": model.getPackage());
				}
				//                    Tree tree = parse(name);
				//                    classTrees.add(tree);
			}
			else
			{
				pkgnames.add(name);
			}
			// todo: is not enough, packages written as directories should also be allowed
			// Hence all unknown names are assumed being packages
			/*else if(isValidPackageName(name))
			{
				pkgnames.add(name);
			}
			else if(isValidAgentSourceFile(name))
			{
				messager.error("main.file_not_found", name);
			}
			else
			{
				messager.error("main.illegal_package_name", name);
			}*/
		}

		searchSubPackages(subPackages, pkgnames, excludedPackages);
		for(int i = 0; i<pkgnames.size(); i++)
		{
			String pack = (String)pkgnames.get(i);
			parsePackageClasses(pack, agents, packages, excludedPackages);
		}
//        if (messager.nerrors() == 0) {
//            messager.notice("main.Building_tree");
		//enter.main(classTrees.toList().appendList(packTrees.toList()));
//        }
		messager.notice("main.Building_jadexdoc");


		this.agents = new ArrayList(agents);
		this.packages = new ArrayList(packages);

		return (messager.nerrors()==0);
	}

	/**
	 * search all directories in path for subdirectory name. Add all
	 * adf files found in such a directory to args.
	 */
	private void parsePackageClasses(String pkgname, Set agents, Set packages, List excludedPackages)
	{
		if(excludedPackages.contains(pkgname))
		{
			return;
		}
		boolean hasFiles = false;
		String path = sourceClassPath;
		if(path==null)
			path = classPath;
		int plen = path.length();
		int i = 0;
		messager.notice("main.Loading_source_files_for_package", pkgname);
		pkgname = pkgname.replace('.', File.separatorChar);
		while(i<plen)
		{
			int end = path.indexOf(pathSep, i);
			String pathname = path.substring(i, end);
			File f = new File(pathname, pkgname);
			String[] names = f.list();
			if(names!=null)
			{
				String dir = f.getAbsolutePath();
				if(!dir.endsWith(File.separator))
					dir = dir+File.separator;
				for(int j = 0; j<names.length; j++)
				{
					if(SXML.isJadexFilename(names[j]))
					{
						String fn = dir+names[j];
						//                        messager.notice("main.Loading_source_file", fn);
						IMCapability model = loadCapability(fn);
						if(model!=null)
						{
							agents.add(model);
							packages.add(model.getPackage()==null? "": model.getPackage());
							hasFiles = true;

						}
					}
				}
			}
			i = end+1;
		}
		if(!hasFiles)
		{
			messager.warning("main.no_source_files_for_package", pkgname.replace(File.separatorChar, '.'));
		}
	}

	/**
	 * Recursively search all directories in path for subdirectory name.
	 * Add all packages found in such a directory to packages list.
	 */
	private void searchSubPackages(List subPackages, List packages, List excludedPackages)
	{
		//System.out.println("searchSubPackages1: "+packages+" "+packages+" "+excludedPackages);
		for(int i = 0; i<subPackages.size(); i++)
		{
			String subpack = (String)subPackages.get(i);
			searchSubPackages(subpack, packages, excludedPackages);
		}
	}

	/**
	 * Recursively search all directories in path for subdirectory name.
	 * Add all packages found in such a directory to packages list.
	 */
	private void searchSubPackages(String name, List packages, List excludedPackages)
	{
		//System.out.println("searchSubPackages2: "+name+" "+packages+" "+excludedPackages);
		
		if(excludedPackages.contains(name))
		{
			return;
		}
		String path = sourceClassPath+pathSep+classPath;
		int plen = path.length();
		int i = 0;
		String packageName = name.replace('.', File.separatorChar);
		boolean addedPackage = false;
		while(i<plen)
		{
			int end = path.indexOf(pathSep, i);
			String pathname = path.substring(i, end);
			File f = new File(pathname, packageName);
			String[] names = f.list();
			if(names!=null)
			{
				for(int j = 0; j<names.length; j++)
				{
					if(!addedPackage && SXML.isJadexFilename(names[j]) && !packages.contains(name))
					{
						packages.add(name);
						addedPackage = true;
					}
					else if((new File(f.getPath(), names[j])).isDirectory())
					{
						searchSubPackages(name+"."+names[j], packages, excludedPackages);
					}
				}
			}
			i = end+1;
		}
	}


	/**
	 *  Is the given string a valid package name?
	 * /
	boolean isValidPackageName(String s)
	{
		int index;
		while((index = s.indexOf('.'))!=-1)
		{
			if(!isValidClassName(s.substring(0, index)))
				return false;
			s = s.substring(index+1);
		}
		return isValidClassName(s);
	}*/

	/**
	 * Return true if given file name is a valid class file name.
	 * @param file the name of the file to check.
	 * @return true if given file name is a valid class file name
	 *         and false otherwise.
	 * /
	private static boolean isValidJavaClassFile(String file)
	{
		if(!file.endsWith(".class"))
			return false;
		String clazzName = file.substring(0, file.length()-".class".length());
		return isValidClassName(clazzName);
	}*/

	/**
	 * Return true if given file name is a valid class name.
	 * @param clazzName the name of the class to check.
	 * @return true if given class name is a valid class name
	 *         and false otherwise.
	 */
	private static boolean isValidClassName(String clazzName)
	{
		if(clazzName.length()<1)
			return false;
		if(!Character.isJavaIdentifierStart(clazzName.charAt(0)))
			return false;
		for(int i = 1; i<clazzName.length(); i++)
			if(!Character.isJavaIdentifierPart(clazzName.charAt(i)))
				return false;
		return true;
	}

	/**
	 * Set classPath  and sourceClassPath.
	 */
	private void setPaths(List optlist)
	{
		String[][] options = (String[][])optlist.toArray(new String[optlist.size()][]);
		String cp = null;
		String sp = null;

		for(int oi = 0; oi<options.length; ++oi)
		{
			String[] os = options[oi];
			String opt = os[0].toLowerCase();
			if(opt.equals("-classpath"))
				cp = os[1];

			if(opt.equals("-sourcepath"))
				sp = os[1];
		}

		if(cp==null)
			cp = System.getProperty("env.class.path");
		if(cp==null && System.getProperty("application.home")==null)
			cp = System.getProperty("java.class.path");
		if(cp==null)
			cp = ".";
		classPath = terminate(cp);


		if(sp!=null)
		{
			sourceClassPath = terminate(sp);
		}
		else
		{
			sourceClassPath = null;
		}
	}


	/**
	 * Add path separator to string if it does not end in one already
	 */
	private String terminate(String s)
	{
		return (s.endsWith(pathSeparator))? s: s+pathSeparator;
	}

	public List getAgents()
	{
		return agents;
	}

	public List getPackages()
	{
		return packages;
	}

	/**
	 * Load an xml Jadex model with simple error handling.
	 * @param xml The filename for the xml based adf.
	 * @return The agent model.
	 */
	protected IMCapability loadCapability(String xml)
	{
		IMCapability ret = null;
		try
		{
			ret = (IMCapability)SXML.loadModel(xml, "jadex/tools/jadexdoc/resources/jadexdoc.xsl");
		}
		catch(Exception e)
		{
//			e.printStackTrace();
			messager.error("jadexdoc.File_Parse_Error", xml);
		}
		return ret;
	}
}
