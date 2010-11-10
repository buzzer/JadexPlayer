package jadex.tools.jadexdoc;


import java.io.File;
import java.util.*;
import jadex.model.IMCapability;
import jadex.tools.jadexdoc.doclets.MessageRetriever;

/**
 * Configure the output based on the options. Doclets should sub-class
 * Configuration, to configure and add their own options.
 */
public abstract class Configuration
{

	/**
	 * The default amount of space between tab stops.
	 */
	public static final int DEFAULT_TAB_STOP_LENGTH = 8;

	/**
	 * The source output directory name
	 */
	public static final String SOURCE_OUTPUT_DIR_NAME = "src-html/";

	/**
	 * The specified amount of space between tab stops.
	 */
	public int linksourcetab = DEFAULT_TAB_STOP_LENGTH;

	/**
	 * True if we should generate browsable sources.
	 */
	public boolean genSrc = false;

	/**
	 * True if command line option "-nosince" is used. Default value is
	 * false.
	 */
	public boolean nosince = false;

	/**
	 * True if we should recursively copy the doc-file subdirectories
	 */
	public boolean copydocfilesubdirs = false;

	/**
	 * The META charset tag used for cross-platform viewing.
	 */
	public String charset = "";

	/**
	 * The list of doc-file subdirectories to exclude
	 */
	protected Set excludedDocFileDirs;

	/**
	 * The list of qualifiers to exclude
	 */
	protected Set excludedQualifiers;


	/**
	 * Destination directory name, in which doclet will generate the entire
	 * documentation. Default is current directory.
	 */
	public String destdirname = "";

	/**
	 * Encoding for this document. Default is default encoding for this
	 * platform.
	 */
	public String docencoding = null;

	/**
	 * Encoding for this document. Default is default encoding for this
	 * platform.
	 */
	public String encoding = null;

	/**
	 * Generate author specific information for all the classes if @author
	 * tag is used in the doc comment and if -author option is used.
	 * <code>showauthor</code> is set to true if -author option is used.
	 * Default is don't show author information.
	 */
	public boolean showauthor = false;

	/**
	 * Generate version specific information for the all the classes
	 * if @version tag is used in the doc comment and if -version option is
	 * used. <code>showversion</code> is set to true if -version option is
	 * used.Default is don't show version information.
	 */
	public boolean showversion = false;

	/**
	 * Don't generate the date in the generated documentation, if -nodate
	 * option is used. <code>nodate</code> is set to true if -nodate
	 * option is used. Default is don't show date.
	 */
	public boolean nodate = false;

	/**
	 * Sourcepath from where to read the source files. Default is classpath.
	 */
	public String sourcepath = "";

	/**
	 * Don't generate deprecated API information at all, if -nodeprecated
	 * option is used. <code>nodepracted</code> is set to true if
	 * -nodeprecated option is used. Default is generate deprected API
	 * information.
	 */
	public boolean nodeprecated = false;


	/**
	 * True if only exported members are shown
	 */
	public boolean exported = false;


	/**
	 * True if expressions are included
	 */
	public boolean expressions = true;

	/**
	 * True if expressions are included
	 */
	public boolean events = true;

	/**
	 * True if standard members are included
	 */
	public boolean standardmembers = false;


	/**
	 * list of classes specified on the command line.
	 */
	public List cmdLineAgents;

	/**
	 * list of packages specified on the command line.
	 */
	public List cmdLinePackages;

	/**
	 * a collection of all options.
	 */
	public List options;


	/**
	 * Message Retriever for the doclet, to retrieve message from the resource
	 * file for this Configuration.
	 */
	public MessageRetriever message = null;
	private static final String DOCLET_RESOURCE_LOCATION = "jadex.tools.jadexdoc.doclets.resources.doclets";

	/**
	 * This method should be defined in all those doclets
	 * which want to inherit from this Configuration. This method
	 * should return the number of arguments to the command line option.
	 * This method is called by the method {@link #getDocletOptionLength(String)}.
	 * @param option Command line option under consideration.
	 * @return number of arguments to option. Zero return means
	 *         option not known.  Negative value means error occurred.
	 * @see #getDocletOptionLength(String)
	 */
	public abstract int getDocletOptionLength(String option);

	/**
	 * After parsing the available options using
	 * {@link #getDocletOptionLength(String)},
	 * JavaDoc invokes this method with an array of options-arrays, where
	 * the first item in any array is the option, and subsequent items in
	 * that array are its arguments. So, if -print is an option that takes
	 * no arguments, and -copies is an option that takes 1 argument, then
	 * <pre>
	 *     -print -copies 3
	 * </pre>
	 * produces an array of arrays that looks like:
	 * <pre>
	 *      option[0][0] = -print
	 *      option[1][0] = -copies
	 *      option[1][1] = 3
	 * </pre>
	 * (By convention, command line switches start with a "-", but
	 * they don't have to.)
	 * This method is not required to be written by sub-classes and will
	 * default gracefully (to true) if absent.
	 * <P>
	 * Printing option related error messages (using the provided
	 * DocErrorReporter) is the responsibility of this method.
	 * <P>
	 * Note: This is invoked on a temporary config, no side-effect
	 * settings will persist.
	 * @param optlist Options used on the command line.
	 * @return true if all the options are valid.
	 */
	public abstract boolean validDocletOptions(List optlist);

	/**
	 * Depending upon the command line options provided by the user, set
	 * configure the output generation environment.
	 * @param optlist Used to retrieve used comand line options.
	 */
	public abstract void setDocletOptions(List optlist);

	/**
	 * Constructor. Constructs the message retriever with resource file.
	 */
	public Configuration()
	{
		message = new MessageRetriever(Messager.getInstance(), DOCLET_RESOURCE_LOCATION);
		excludedDocFileDirs = new HashSet();
		excludedQualifiers = new HashSet();
	}

	/**
	 * Set the command line options supported by this configuration.
	 * @param optlist Root of the Program Structure generated by this Javadoc run.
	 * @throws jadex.tools.jadexdoc.doclets.DocletAbortException
	 */
	public void setOptions(List optlist)
	{
		String[][] options = (String[][])optlist.toArray(new String[optlist.size()][]);
		for(int oi = 0; oi<options.length; ++oi)
		{
			String[] os = options[oi];
			String opt = os[0].toLowerCase();
			if(opt.equals("-d"))
			{
				destdirname = addTrailingFileSep(os[1]);
			}
			else if(opt.equals("-docfilessubdirs"))
			{
				copydocfilesubdirs = true;
			}
			else if(opt.equals("-docencoding"))
			{
				docencoding = os[1];
			}
			else if(opt.equals("-encoding"))
			{
				encoding = os[1];
			}
			else if(opt.equals("-author"))
			{
				showauthor = true;
			}
			else if(opt.equals("-version"))
			{
				showversion = true;
			}
			else if(opt.equals("-exported"))
			{
				exported = true;
			}
			else if(opt.equals("-expressions"))
			{
				expressions = true;
			}
			else if(opt.equals("-events"))
			{
				events = true;
			}
			else if(opt.equals("-standardmembers"))
			{
				standardmembers = true;
			}
			else if(opt.equals("-nodeprecated"))
			{
				nodeprecated = true;
			}
			else if(opt.equals("-xnodate"))
			{
				nodate = true;
			}
			else if(opt.equals("-sourcepath"))
			{
				sourcepath = os[1];
			}
			else if(opt.equals("-classpath") &&
					sourcepath.length()==0)
			{
				sourcepath = os[1];
			}
			else if(opt.equals("-excludedocfilessubdir"))
			{
				addToSet(excludedDocFileDirs, os[1]);
			}
			else if(opt.equals("-noqualifier"))
			{
				addToSet(excludedQualifiers, os[1]);
			}
			else if(opt.equals("-linksource"))
			{
				genSrc = true;
			}
			else if(opt.equals("-linksourcetab"))
			{
				genSrc = true;
				try
				{
					linksourcetab = Integer.parseInt(os[1]);
				}
				catch(NumberFormatException e)
				{
					//Set to -1 so that warning will be printed
					//to indicate what is valid argument.
					linksourcetab = -1;
				}
				if(linksourcetab<=0)
				{
					message.warning("doclet.linksourcetab_warning");
					linksourcetab = DEFAULT_TAB_STOP_LENGTH;
				}
			}
		}
		if(sourcepath.length()==0)
		{
			sourcepath = System.getProperty("env.class.path");
		}
		if(docencoding==null)
		{
			docencoding = encoding;
		}

		setDocletOptions(optlist);
	}

	protected void addToSet(Set s, String str)
	{
		StringTokenizer st = new StringTokenizer(str, ":");
		String current;
		while(st.hasMoreTokens())
		{
			current = st.nextToken();
			s.add(current);
		}
	}

	/**
	 * Add a traliling file separator, if not found or strip off extra trailing
	 * file separators if any.
	 * @param path Path under consideration.
	 * @return String Properly constructed path string.
	 */
	String addTrailingFileSep(String path)
	{
		String fs = System.getProperty("file.separator");
		String dblfs = fs+fs;
		int indexDblfs;
		while((indexDblfs = path.indexOf(dblfs))>= 0)
		{
			path = path.substring(0, indexDblfs)+
					path.substring(indexDblfs+fs.length());
		}
		if(!path.endsWith(fs))
			path += fs;
		return path;
	}

	/**
	 * Check for doclet added options here. This
	 * will return the length of the options which are shared
	 * by our doclets.
	 * @param option Option whose length is requested.
	 * @return number of arguments to option. Zero return means
	 *         option not known.  Negative value means error occurred.
	 */
	public int getOptionLength(String option)
	{
		option = option.toLowerCase();
		if(option.equals("-docfilessubdirs") ||
				option.equals("-linksource") ||
				option.equals("-version") ||
				option.equals("-nodeprecated") ||
				option.equals("-exported") ||
				option.equals("-expressions") ||
				option.equals("-events") ||
				option.equals("-standardmembers") ||
				option.equals("-author") ||
				option.equals("-xnodate"))
		{
			return 1;
		}
		else if(option.equals("-docencoding") ||
				option.equals("-encoding") ||
				option.equals("-excludedocfilessubdir") ||
				option.equals("-noqualifier") ||
				option.equals("-sourcepath") ||
				option.equals("-classpath") ||
				option.equals("-d"))
		{
			return 2;
		}
		else
		{
			return getDocletOptionLength(option);
		}
	}

	/**
	 * This will validate the options which are shared
	 * by our doclets. For example, this method will flag an error
	 * if user has used "-nohelp" and "-helpfile" option
	 * together.
	 * @param optlist Options used on the command line.
	 * @return true if all the options are valid.
	 */
	public boolean validOptions(List optlist)
	{

		String[][] options = (String[][])optlist.toArray(new String[optlist.size()][]);

		for(int oi = 0; oi<options.length; oi++)
		{
			String[] os = options[oi];
			String opt = os[0].toLowerCase();
			if(opt.equals("-d"))
			{
				String destdirname = addTrailingFileSep(os[1]);
				File destDir = new File(destdirname);
				if(!destDir.exists())
				{
					//Create the output directory (in case it doesn't exist yet)
					(new File(destdirname)).mkdirs();
				}
				else if(!destDir.isDirectory())
				{
					message.error("doclet.destination_directory_not_directory_0", destDir.getPath());
					return false;
				}
				else if(!destDir.canWrite())
				{
					message.error("doclet.destination_directory_not_writable_0", destDir.getPath());
					return false;
				}
			}
		}
		return validDocletOptions(optlist);
	}


	/**
	 * Return true if the given doc-file subdirectory should be excluded and
	 * false otherwise.
	 * @param docfilesubdir the doc-files subdirectory to check.
	 */
	public boolean shouldExcludeDocFileDir(String docfilesubdir)
	{
		if(excludedDocFileDirs.contains(docfilesubdir))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Return true if the given qualifier should be excluded and false otherwise.
	 * @param qualifier the qualifier to check.
	 */
	public boolean shouldExcludeQualifier(String qualifier)
	{
		if(excludedQualifiers.contains("all") || excludedQualifiers.contains(qualifier))
		{
			return true;
		}
		else
		{
			int index = -1;
			while((index = qualifier.indexOf(".", index+1))!=-1)
			{
				if(excludedQualifiers.contains(qualifier.substring(0, index+1)+"*"))
				{
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Command line options.
	 * <p/>
	 * <pre>
	 * For example, given:
	 *     javadoc -foo this that -bar other ...
	 * <p/>
	 * This method will return:
	 *      options()[0][0] = "-foo"
	 *      options()[0][1] = "this"
	 *      options()[0][2] = "that"
	 *      options()[1][0] = "-bar"
	 *      options()[1][1] = "other"
	 * </pre>
	 * @return an array of arrays of String.
	 */
	public String[][] options()
	{
		return (String[][])options.toArray(new String[options.size()][]);
	}

	/**
	 * Initialize packages information. Those packages are input from
	 * command line.
	 */
	public void setPackages(List packages)
	{
		cmdLinePackages = packages;
	}


	/**
	 * Packages specified on the command line.
	 */
	public String[] specifiedPackages()
	{
		return (String[])cmdLinePackages.toArray(new String[cmdLinePackages.size()]);
	}

	/**
	 * Initialize agents information. Those agents are input from
	 * command line.
	 */
	public void setAgents(List agents)
	{
		cmdLineAgents = agents;
	}

	/**
	 * Get all agents specified on the command line.
	 */
	public IMCapability[] getSpecifiedAgents()
	{
		return (IMCapability[])cmdLineAgents.toArray(new IMCapability[cmdLineAgents.size()]);
	}


	public List getAgentsForPackage(String pkgname)
	{
		IMCapability[] arr = getSpecifiedAgents();
		List agents = new ArrayList();

		for(int i = 0; i<arr.length; i++)
		{
			IMCapability agent = arr[i];
			String agentpackage = agent.getPackage()==null? "": agent.getPackage();
			if(agentpackage.equals(pkgname))
			{
				agents.add(agent);
			}
		}
		return agents;

	}

	public boolean isIncluded(IMCapability cd)
	{
		boolean isIncluded = false;
		IMCapability[] arr = getSpecifiedAgents();
		for(int i = 0; i<arr.length; i++)
		{
			IMCapability capability = arr[i];
			if(getQualifiedAgentName(capability).equals(getQualifiedAgentName(cd)))
			{
				isIncluded = true;
			}
		}
		return isIncluded;
	}

	/**
	 * Return the qualified name of the <code>IMCapability</code> if it's qualifier is not excluded.  Otherwise,
	 * return the unqualified <code>IMCapability</code> name.
	 * @param agent the <code>IMCapability</code> to check.
	 */
	public String getAgentName(IMCapability agent)
	{
		String pd = agent.getPackage();
		if(pd!=null && shouldExcludeQualifier(pd))
		{
			return agent.getName();
		}
		else
		{
			return getQualifiedAgentName(agent);
		}
	}

	/**
	 * 
	 * @param agent
	 * @return
	 */
	public String getQualifiedAgentName(IMCapability agent)
	{
		String pck = agent.getPackage();
		if(pck!=null && pck.length()>0)
		{
			return agent.getPackage()+"."+agent.getName();
		}
		else
		{
			return agent.getName();
		}

	}

	/**
	 * Return the qualified name of the <code>ClassDoc</code> if it's qualifier is not excluded.  Otherwise,
	 * return the unqualified <code>ClassDoc</code> name.
	 * @param cd the <code>ClassDoc</code> to check.
	 */
	public String getClassName(Class cd)
	{
		if(cd==null)
		{
			return "";
		}
		Package pd = cd.getPackage();
		String cl = cd.getName();
		if(cl.lastIndexOf('.')!=-1)
		{
			cl = cl.substring(cl.lastIndexOf('.')+1, cl.length());
		}
		if(pd!=null && shouldExcludeQualifier(pd.getName()))
		{
			return cl;
		}
		else
		{
			return cd.getName();
		}

	}
}


