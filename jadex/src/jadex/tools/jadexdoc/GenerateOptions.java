package jadex.tools.jadexdoc;

import java.io.File;
import java.util.*;
import jadex.model.SXML;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

/**
 *  The generate options.
 */
public class GenerateOptions implements Cloneable
{
	//-------- constants --------
	
	public static final String SEPARATOR = ",";
	public static final String PREPEND = "doc.";
	
	public static final String INCLUDESUBPACKAGES = "includesubpackages";
	public static final String PACKAGES = "packages";
	public static final String FILES = "files";
	public static final String SPLITINDEX = "splitindex";
	public static final String CREATEINDEX = "createindex";
	public static final String CREATETREE = "createtree";
	public static final String NONAVBAR = "nonavbar";
	public static final String OVERVIEW = "overview";
	public static final String OVERVIEWFILE = "overviewfile";
	public static final String DESTDIRNAME = "destdirname";
	public static final String CREATEDOCTITLE = "createdoctitle";
	public static final String DOCTITLE = "doctitle";
	public static final String EXTRAOPTIONSJAVADOC = "extraoptionsjavadoc";
	public static final String EXTRAOPTIONSJADEXDOC = "extraoptionsjadexdoc";
	public static final String JAVADOC = "javadoc";
	public static final String JADEXDOC = "jadexdoc";
	public static final String JAVADOCLOC = "javadocloc";
	public static final String JAVALOC = "javaloc";
	public static final String OPENBROWSER = "openbroswer";
	public static final String LINKS = "links";
	public static final String SOURCEPATH = "sourcepath";
	public static final String CLASSPATH = "classpath";
		
	//-------- attributes --------

	/** Include subpackages. */
	public boolean includesubpackages = true;

	/** The included packages. */
	public List packages;
	
	/** The included single files. */
	public List files;

	/** True if command line option "-splitindex" is used. Default value is false. */
	public boolean splitindex = false;

	/** False if command line option "-noindex" is used. Default value is true. */
	public boolean createindex = true;

	/** False if command line option "-notree" is used. Default value is true. */
	public boolean createtree = true;

	/** True if command line option "-nonavbar" is used. Default value is false. */
	public boolean nonavbar = false;

	/** True if command line option "-overview" is used. Default value is false. */
	public boolean overview = false;

	/** Name of the overview file and null if it does not exist. */
	public String overviewfile = null;

	/** Destination directory name, in which doclet will generate the entire
	    documentation. Default is current directory. */
	public String destdirname = ".";

	/** Flag for creating a document title. */
	public boolean createdoctitle = false;

	/** The document title. */
	public String doctitle;

	/** Optional extra options javadoc. */
	public String extraoptionsjavadoc;
	
	/** Optional extra options jadexdoc. */
	public String extraoptionsjadexdoc;

	/** Generate Jadexdoc. */
	public boolean jadexdoc = true;
	
	/** Generate Javadoc. */
	public boolean javadoc = true;

	/** The javadoc location. */
	public String javadocloc;
	
	/** The java location for executing Jadexdoc. */
	public String javaloc;

	/** The documentation links. */
	public List links;
	
	/** The source path. */
	public List sourcepath;
	
	/** The class path. */
	public List classpath;

	//-------- constructors --------

	/**
	 *  Create new options.
	 */
	public GenerateOptions()
	{
		this.packages = new ArrayList();
		this.files = new ArrayList();
		this.links = new ArrayList();
		this.sourcepath = new ArrayList();
		this.classpath = new ArrayList();
	}
	
	//-------- methods --------

	/**
	 *  Create options from properties
	 *  @param props The properties to read out.
	 *  @return The created ptions.
	 */
	public static GenerateOptions create(Properties props)
	{
		GenerateOptions ret = new GenerateOptions();
		
		String keys[] = (String[])props.keySet().toArray(new String[props.size()]);
		for(int i=0; i<keys.length; i++)
		{
			if(keys[i].equals(PREPEND+INCLUDESUBPACKAGES))
			{
				ret.includesubpackages = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+PACKAGES))
			{
				StringTokenizer stok = new StringTokenizer(props.getProperty(keys[i]), SEPARATOR);
				while(stok.hasMoreTokens())
				{
					ret.packages.add(stok.nextToken());
				}
			}
			else if(keys[i].equals(PREPEND+FILES))
			{
				StringTokenizer stok = new StringTokenizer(props.getProperty(keys[i]), SEPARATOR);
				while(stok.hasMoreTokens())
				{
					ret.files.add(stok.nextToken());
				}
			}
			else if(keys[i].equals(PREPEND+SPLITINDEX))
			{
				ret.splitindex = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+CREATEINDEX))
			{
				ret.createindex = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+CREATETREE))
			{
				ret.createtree = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+NONAVBAR))
			{
				ret.nonavbar = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+OVERVIEW))
			{
				ret.overview = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+OVERVIEWFILE))
			{
				ret.overviewfile = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+DESTDIRNAME))
			{
				ret.destdirname = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+CREATEDOCTITLE))
			{
				ret.createdoctitle = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+DOCTITLE))
			{
				ret.doctitle = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+EXTRAOPTIONSJAVADOC))
			{
				ret.extraoptionsjavadoc = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+EXTRAOPTIONSJADEXDOC))
			{
				ret.extraoptionsjadexdoc = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+JADEXDOC))
			{
				ret.jadexdoc = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+JAVADOC))
			{
				ret.javadoc = new Boolean(props.getProperty(keys[i])).booleanValue();
			}
			else if(keys[i].equals(PREPEND+JAVADOCLOC))
			{
				ret.javadocloc = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+JAVALOC))
			{
				ret.javaloc = props.getProperty(keys[i]);
			}
			else if(keys[i].equals(PREPEND+LINKS))
			{
				StringTokenizer stok = new StringTokenizer(props.getProperty(keys[i]), SEPARATOR);
				while(stok.hasMoreTokens())
				{
					ret.links.add(stok.nextToken());
				}
			}
			else if(keys[i].equals(PREPEND+SOURCEPATH))
			{
				StringTokenizer stok = new StringTokenizer(props.getProperty(keys[i]), SEPARATOR);
				while(stok.hasMoreTokens())
				{
					ret.sourcepath.add(stok.nextToken());
				}
			}
			else if(keys[i].equals(PREPEND+CLASSPATH))
			{
				StringTokenizer stok = new StringTokenizer(props.getProperty(keys[i]), SEPARATOR);
				while(stok.hasMoreTokens())
				{
					ret.classpath.add(stok.nextToken());
				}
			}
		}
		
		return ret;
	}
	
	/**
	 *  Create options from properties
	 *  @param javadoc Generate for Javadoc or Jadexdoc.
	 *  @return The created ptions.
	 */
	public Properties toProperties()
	{
		Properties ret = new Properties();
		
		ret.setProperty(PREPEND+INCLUDESUBPACKAGES, ""+includesubpackages);
		if(packages.size()>0)
		{
			String tmp = "";
			for(int i=0; i<packages.size(); i++)
			{
				tmp += packages.get(i);
				if(i+1<packages.size())
					tmp += SEPARATOR;
			}
			ret.setProperty(PREPEND+PACKAGES, tmp);
		}
		if(files.size()>0)
		{
			String tmp = "";
			for(int i=0; i<files.size(); i++)
			{
				tmp += files.get(i);
				if(i+1<files.size())
					tmp += SEPARATOR;
			}
			ret.setProperty(PREPEND+FILES, tmp);
		}
		ret.setProperty(PREPEND+SPLITINDEX, ""+splitindex);
		ret.setProperty(PREPEND+CREATEINDEX, ""+createindex);
		ret.setProperty(PREPEND+CREATETREE, ""+createtree);
		ret.setProperty(PREPEND+NONAVBAR, ""+nonavbar);
		ret.setProperty(PREPEND+OVERVIEW, ""+overview);
		if(overviewfile!=null)
			ret.setProperty(PREPEND+OVERVIEWFILE, overviewfile);
		if(destdirname!=null)
			ret.setProperty(PREPEND+DESTDIRNAME, destdirname);
		ret.setProperty(PREPEND+CREATEDOCTITLE, ""+createdoctitle);
		if(doctitle!=null)
			ret.setProperty(PREPEND+DOCTITLE, doctitle);
		if(extraoptionsjavadoc!=null)
			ret.setProperty(PREPEND+EXTRAOPTIONSJAVADOC, extraoptionsjavadoc);
		if(extraoptionsjadexdoc!=null)
			ret.setProperty(PREPEND+EXTRAOPTIONSJADEXDOC, extraoptionsjadexdoc);
		ret.setProperty(PREPEND+JADEXDOC, ""+jadexdoc);
		ret.setProperty(PREPEND+JAVADOC, ""+javadoc);
		if(javadocloc!=null)
			ret.setProperty(PREPEND+JAVADOCLOC, javadocloc);
		if(javaloc!=null)
			ret.setProperty(PREPEND+JAVALOC, javaloc);

		if(links.size()>0)
		{
			String tmp = "";
			for(int i=0; i<links.size(); i++)
			{
				tmp += links.get(i);
				if(i+1<links.size())
					tmp += SEPARATOR;
			}
			ret.setProperty(PREPEND+LINKS, tmp);
		}
		if(sourcepath.size()>0)
		{
			String tmp = "";
			for(int i=0; i<sourcepath.size(); i++)
			{
				tmp += sourcepath.get(i);
				if(i+1<sourcepath.size())
					tmp += SEPARATOR;
			}
			ret.setProperty(PREPEND+SOURCEPATH, tmp);
		}
		if(classpath.size()>0)
		{
			String tmp = "";
			for(int i=0; i<classpath.size(); i++)
			{
				tmp += classpath.get(i);
				if(i+1<classpath.size())
					tmp += SEPARATOR;
			}
			ret.setProperty(PREPEND+CLASSPATH, tmp);
		}
		
		return ret;
	}
	
	/**
	 *  Convert the options to command line options
	 *  that can be processed by Jadexdoc.
	 */
	public String[] toCommandLineString(boolean genjavadoc)
	{
		List ret = new ArrayList();
		
		// Add single files without options.
		for(int i=0; i<files.size(); i++)
		{
			if(!genjavadoc && SXML.isJadexFilename((String)files.get(i)))
				ret.add("\""+adaptRelativeEntry((String)files.get(i))+"\"");
			else if(genjavadoc && SUtil.isJavaSourceFilename((String)files.get(i)))
				ret.add("\""+adaptRelativeEntry((String)files.get(i))+"\"");
		}
		
		// Add package names as subpackages when specified
		// Otherwise without option
		if(includesubpackages && packages.size()>0)
			ret.add("-subpackages");
		for(int i=0; i<packages.size(); i++)
		{
			ret.add("\""+adaptRelativeEntry((String)packages.get(i))+"\"");
		}
		
		// Add -sourcepath option
		if(sourcepath.size()>0)
		{
			ret.add("-sourcepath");
			String sp = "";
			for(int i=0; i<sourcepath.size(); i++)
			{
				sp += "\""+adaptRelativeEntry((String)sourcepath.get(i))+"\"";
				if(i+1<sourcepath.size())
					sp += File.pathSeparator;
			}
			ret.add(sp);
		}
		
		// Add -classpath option
		if(classpath.size()>0)
		{
			ret.add("-classpath");
			String cp = "";
			for(int i=0; i<classpath.size(); i++)
			{
				cp += "\""+adaptRelativeEntry((String)classpath.get(i))+"\"";
				if(i+1<classpath.size())
					cp += File.pathSeparator;
			}
			ret.add(cp);
		}

		// Always current dir ist destdir
		/*if(destdirname!=null)
		{
			ret.add("-d");
			ret.add(destdirname);
		}*/

		if(overview)
		{
			ret.add("-overview");
			ret.add("\""+adaptRelativeEntry((String)overviewfile)+"\"");
		}

		if(createdoctitle)
		{
			ret.add("-doctitle");
			ret.add(doctitle);
		}

		if(!createtree)
			ret.add("-notree");

		if(nonavbar)
			ret.add("-nonavbar");

		if(createindex)
		{
			if(splitindex)
				ret.add("-splitindex");
		}
		else
		{
			ret.add("-noindex");
		}

		for(int i=0; i<links.size(); i++)
		{
			ret.add("-link");
			ret.add("\""+adaptRelativeEntry((String)links.get(i))+"\"");
		}

		if(extraoptionsjavadoc!=null && genjavadoc)
		{
			StringTokenizer stok = new StringTokenizer(extraoptionsjavadoc, " ");
			while(stok.hasMoreTokens())
			{
				ret.add(stok.nextToken());
			}
		}

		if(extraoptionsjadexdoc!=null && !genjavadoc)
		{
			StringTokenizer stok = new StringTokenizer(extraoptionsjadexdoc, " ");
			while(stok.hasMoreTokens())
			{
				ret.add(stok.nextToken());
			}
		}
		
		return (String[])ret.toArray(new String[ret.size()]);
	}
	
	/**
	 * @return the classpath
	 */
	public List getClasspath()
	{
		return classpath;
	}

	/**
	 * @param classpath the classpath to set
	 */
	public void setClasspath(List classpath)
	{
		this.classpath = classpath;
	}

	/**
	 * @return the createdoctitle
	 */
	public boolean isCreatedoctitle()
	{
		return createdoctitle;
	}

	/**
	 * @param createdoctitle the createdoctitle to set
	 */
	public void setCreatedoctitle(boolean createdoctitle)
	{
		this.createdoctitle = createdoctitle;
	}

	/**
	 * @return the createindex
	 */
	public boolean isCreateindex()
	{
		return createindex;
	}

	/**
	 * @param createindex the createindex to set
	 */
	public void setCreateindex(boolean createindex)
	{
		this.createindex = createindex;
	}

	/**
	 * @return the createtree
	 */
	public boolean isCreatetree()
	{
		return createtree;
	}

	/**
	 * @param createtree the createtree to set
	 */
	public void setCreatetree(boolean createtree)
	{
		this.createtree = createtree;
	}

	/**
	 * @return the destdirname
	 */
	public String getDestdirname()
	{
		return destdirname;
	}

	/**
	 * @param destdirname the destdirname to set
	 */
	public void setDestdirname(String destdirname)
	{
		this.destdirname = destdirname;
	}

	/**
	 * @return the doctitle
	 */
	public String getDoctitle()
	{
		return doctitle;
	}

	/**
	 * @param doctitle the doctitle to set
	 */
	public void setDoctitle(String doctitle)
	{
		this.doctitle = doctitle;
	}

	/**
	 * @return the extraoptions
	 */
	public String getExtraoptionsJavadoc()
	{
		return extraoptionsjavadoc;
	}

	/**
	 * @param extraoptions the extraoptions to set
	 */
	public void setExtraoptionsJavadoc(String extraoptionsjavadoc)
	{
		this.extraoptionsjavadoc = extraoptionsjavadoc;
	}
	
	/**
	 * @return the extraoptions
	 */
	public String getExtraoptionsJadexdoc()
	{
		return extraoptionsjadexdoc;
	}

	/**
	 * @param extraoptions the extraoptions to set
	 */
	public void setExtraoptionsJadexdoc(String extraoptionsJadexdoc)
	{
		this.extraoptionsjadexdoc = extraoptionsjadexdoc;
	}

	/**
	 * @return the files
	 */
	public List getFiles()
	{
		return files;
	}

	/**
	 * @param files the files to set
	 */
	public void setFiles(List files)
	{
		this.files = files;
	}

	/**
	 * @return the includesubpackages
	 */
	public boolean isIncludesubpackages()
	{
		return includesubpackages;
	}

	/**
	 * @param includesubpackages the includesubpackages to set
	 */
	public void setIncludesubpackages(boolean includesubpackages)
	{
		this.includesubpackages = includesubpackages;
	}

	/**
	 * @return the jadexdoc
	 */
	public boolean isJadexdoc()
	{
		return jadexdoc;
	}

	/**
	 * @param jadexdoc the jadexdoc to set
	 */
	public void setJadexdoc(boolean jadexdoc)
	{
		this.jadexdoc = jadexdoc;
	}

	/**
	 * @return the javadoc
	 */
	public boolean isJavadoc()
	{
		return javadoc;
	}

	/**
	 * @param javadoc the javadoc to set
	 */
	public void setJavadoc(boolean javadoc)
	{
		this.javadoc = javadoc;
	}

	/**
	 * @return the javadocloc
	 */
	public String getJavadocloc()
	{
		return javadocloc;
	}
	
	/**
	 * @param javadocloc the javadocloc to set
	 */
	public void setJavadocloc(String javadocloc)
	{
		this.javadocloc = javadocloc;
	}
	
	/**
	 *  @return The javaloc.
	 */
	public String getJavaloc()
	{
		return javaloc;
	}

	/**
	 *  @param javaloc The javaloc to set.
	 */
	public void setJavaloc(String javaloc)
	{
		this.javaloc = javaloc;
	}

	/**
	 * @return the links
	 */
	public List getLinks()
	{
		return links;
	}

	/**
	 * @param links the links to set
	 */
	public void setLinks(List links)
	{
		this.links = links;
	}

	/**
	 * @return the nonavbar
	 */
	public boolean isNonavbar()
	{
		return nonavbar;
	}

	/**
	 * @param nonavbar the nonavbar to set
	 */
	public void setNonavbar(boolean nonavbar)
	{
		this.nonavbar = nonavbar;
	}

	/**
	 * @return the overview
	 */
	public boolean isOverview()
	{
		return overview;
	}

	/**
	 * @param overview the overview to set
	 */
	public void setOverview(boolean overview)
	{
		this.overview = overview;
	}

	/**
	 * @return the overviewfile
	 */
	public String getOverviewfile()
	{
		return overviewfile;
	}

	/**
	 * @param overviewfile the overviewfile to set
	 */
	public void setOverviewfile(String overviewfile)
	{
		this.overviewfile = overviewfile;
	}

	/**
	 * @return the packages
	 */
	public List getPackages()
	{
		return packages;
	}

	/**
	 * @param packages the packages to set
	 */
	public void setPackages(List packages)
	{
		this.packages = packages;
	}

	/**
	 * @return the sourcepath
	 */
	public List getSourcepath()
	{
		return sourcepath;
	}

	/**
	 * @param sourcepath the sourcepath to set
	 */
	public void setSourcepath(List sourcepath)
	{
		this.sourcepath = sourcepath;
	}

	/**
	 * @return the splitindex
	 */
	public boolean isSplitindex()
	{
		return splitindex;
	}

	/**
	 * @param splitindex the splitindex to set
	 */
	public void setSplitindex(boolean splitindex)
	{
		this.splitindex = splitindex;
	}
	
	/**
	 *  Clone this object.
	 */
	public Object clone()
	{
		GenerateOptions ret = null;
		try
		{
			ret = (GenerateOptions)super.clone();
			ret.files = (ArrayList)((ArrayList)this.files).clone();
			ret.packages = (ArrayList)((ArrayList)this.packages).clone();
			ret.links = (ArrayList)((ArrayList)this.links).clone();
			ret.sourcepath = (ArrayList)((ArrayList)this.sourcepath).clone();
			ret.classpath = (ArrayList)((ArrayList)this.classpath).clone();
		}
		catch(CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 *  Adapt a relative path to a new current directory.
	 *  Computes the relativ path from the current directory to the
	 *  new currently directory and prepends this path to the entry.
	 *  @param entry The relative entry to adapt.
	 */
	public String adaptRelativeEntry(String entry)
	{
		return destdirname.equals(".")? entry: adaptRelativeEntry(entry, destdirname);
	}
	
	/**
	 *  Adapt a relative path to a new current directory.
	 *  Computes the relativ path from the current directory to the
	 *  new currently directory and prepends this path to the entry.
	 *  @param entry The relative entry to adapt.
	 *  @param destdir The destination directory which will be the new current directory.
	 */
	public static String adaptRelativeEntry(String entry, String destdir)
	{
		String ret = entry;
		File file = new File(entry);
		if(file.exists() && !file.isAbsolute())
		{
			File curdir = new File(".");
			File dest = new File(destdir);
			String rel = SUtil.getRelativePath(dest.getAbsolutePath(), curdir.getAbsolutePath());
			ret = rel+file.getPath();
			//System.out.println("Adapted file from: "+entry+" "+ret);
		}
		
		return ret;
	}
	
	/**
	 *  Main for testing.
	 * /
	public static void main(String[] args)
	{
		String destdir1 = "C:/projects/jadex/api";
		String destdir2 = "./api";
		String destdir3 = "./api/javaapi";
		
		String res1 = adaptRelativeEntry("./api/javaapi", destdir1);
		String res2 = adaptRelativeEntry("./api/javaapi", destdir2);
		String res3 = adaptRelativeEntry("./api/javaapi", destdir3);
	}*/
}