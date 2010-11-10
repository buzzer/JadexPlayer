package jadex.tools.jadexdoc.doclets.standard;

import java.io.File;
import java.util.*;
import jadex.model.IMCapability;
import jadex.tools.jadexdoc.*;
import jadex.tools.jadexdoc.doclets.*;


/**
 * Configure the output based on the command line options.
 */
public class StandardConfiguration extends Configuration
{

	/** The name of the constant values file. */
	public static final String CONSTANTS_FILE_NAME = "constant-values.html";

	/** True if user wants to suppress descriptions and tags. */
	public boolean nocomment = false;

	/** True if user wants to suppress time stamp in output. */
	public boolean notimestamp = false;

	/** Argument for command line option "-header". */
	public String header = "";

	/** Argument for command line option "-packagesheader". */
	public String packagesheader = "";

	/** Argument for command line option "-footer". */
	public String footer = "";

	/** Argument for command line option "-doctitle". */
	public String doctitle = "";

	/** Argument for command line option "-windowtitle". */
	public String windowtitle = "";

	/** Argument for command line option "-bottom". */
	public String bottom = "";

	/** Argument for command line option "-helpfile". */
	public String helpfile = "";

	/** Argument for command line option "-stylesheetfile". */
	public String stylesheetfile = "";

	/** True if command line option "-nohelp" is used. Default value is false. */
	public boolean nohelp = false;

	/** True if command line option "-splitindex" is used. Default value is false. */
	public boolean splitindex = false;

	/** False if command line option "-noindex" is used. Default value is true. */
	public boolean createindex = true;

	/** True if command line option "-use" is used. Default value is false. */
	public boolean classuse = false;

	/** False if command line option "-notree" is used. Default value is true. */
	public boolean createtree = true;

	/** True if command line option "-nonavbar" is used. Default value is false. */
	public boolean nonavbar = false;

	/** True if command line option "-nooverview" is used. Default value is false. */
	public boolean nooverview = false;

	/** True if command line option "-overview" is used. Default value is false. */
	public boolean overview = false;

	/** Name of the overview file and null if it does not exist. */
	public String overviewfile = null;

	/** This is true if option "-overview" is used or option "-overview" is not
	 	used and number of packages is more than one. */
	public boolean createoverview = false;

	/** The tracker of external package links (sole-instance). */
	public final Extern extern = new Extern(this);

	/** The package grouping sole-instance. */
	public final Group group = new Group(this);

	/** True if running in quiet mode. */
	public boolean quiet = false;

	/** True if version id has been printed. */
	public boolean printedVersion = false;

	/** Unique Resource Handler for this package. */
	public final MessageRetriever standardmessage;

	/** The currently processed element (capability or agent). */
	public IMCapability currentelement;

	/** The standard resource location. */
	private static final String STANDARD_RESOURCE_LOCATION = "jadex.tools.jadexdoc.doclets.standard.resources.standard";


	/**
	 * Constructor.
	 */
	public StandardConfiguration()
	{
		standardmessage = new MessageRetriever(Messager.getInstance(), STANDARD_RESOURCE_LOCATION);
	}

	/**
	 * Depending upon the command line options provided by the user, set
	 * configure the output generation environment.
	 * @param optlist Used to retrieve used comand line options.
	 */
	public void setDocletOptions(List optlist)
	{

		String[][] options = (String[][])optlist.toArray(new String[optlist.size()][]);

		LinkedHashSet customTagStrs = new LinkedHashSet();
		for(int oi = 0; oi<options.length; ++oi)
		{
			String[] os = options[oi];
			String opt = os[0].toLowerCase();
			if(opt.equals("-footer"))
			{
				footer = os[1];
			}
			else if(opt.equals("-header"))
			{
				header = os[1];
			}
			else if(opt.equals("-packagesheader"))
			{
				packagesheader = os[1];
			}
			else if(opt.equals("-doctitle"))
			{
				doctitle = os[1];
			}
			else if(opt.equals("-windowtitle"))
			{
				windowtitle = os[1];
			}
			else if(opt.equals("-bottom"))
			{
				bottom = os[1];
			}
			else if(opt.equals("-helpfile"))
			{
				helpfile = os[1];
			}
			else if(opt.equals("-stylesheetfile"))
			{
				stylesheetfile = os[1];
			}
			else if(opt.equals("-charset"))
			{
				charset = os[1];
			}
			else if(opt.equals("-nohelp"))
			{
				nohelp = true;
			}
			else if(opt.equals("-splitindex"))
			{
				splitindex = true;
			}
			else if(opt.equals("-noindex"))
			{
				createindex = false;
			}
			else if(opt.equals("-use"))
			{
				classuse = true;
			}
			else if(opt.equals("-notree"))
			{
				createtree = false;
			}
			else if(opt.equals("-nocomment"))
			{
				nocomment = true;
			}
			else if(opt.equals("-notimestamp"))
			{
				notimestamp = true;
			}
			else if(opt.equals("-nosince"))
			{
				nosince = true;
			}
			else if(opt.equals("-nonavbar"))
			{
				nonavbar = true;
			}
			else if(opt.equals("-nooverview"))
			{
				nooverview = true;
			}
			else if(opt.equals("-overview"))
			{
				overview = true;
				overviewfile = os[1];
			}
			else if(opt.equals("-group"))
			{
				group.checkPackageGroups(os[1], os[2]);
			}
			else if(opt.equals("-link"))
			{
				String url = os[1];
				extern.url(url, url, false);
			}
			else if(opt.equals("-linkoffline"))
			{
				String url = os[1];
				String pkglisturl = os[2];
				extern.url(url, pkglisturl, true);
			}
			else if(opt.equals("-quiet"))
			{
				message.setQuiet();
				standardmessage.setQuiet();
				quiet = true;
			}
		}
	}

	/**
	 * Check for doclet added options here.
	 * @return number of arguments + 1 for a option. Zero return means
	 *         option not known.  Negative value means error occurred.
	 */
	public int getDocletOptionLength(String option)
	{

		option = option.toLowerCase();
		if(option.equals("-nocomment") ||
				option.equals("-notimestamp") ||
				option.equals("-keywords") ||
				option.equals("-nodeprecatedlist") ||
				option.equals("-noindex") ||
				option.equals("-notree") ||
				option.equals("-nohelp") ||
				option.equals("-nosince") ||
				option.equals("-quiet") ||
				option.equals("-splitindex") ||
				option.equals("-use") ||
				option.equals("-nonavbar") ||
				option.equals("-serialwarn") ||
				option.equals("-nooverview"))
		{
			return 1;
		}
		else if(option.equals("-help"))
		{
			standardmessage.notice("doclet.usage");
			return 1;
		}
		else if(option.equals("-x"))
		{
			standardmessage.notice("doclet.xusage");
			return -1; // so run will end
		}
		else if(option.equals("-footer") ||
				option.equals("-header") ||
				option.equals("-packagesheader") ||
				option.equals("-doctitle") ||
				option.equals("-windowtitle") ||
				option.equals("-bottom") ||
				option.equals("-helpfile") ||
				option.equals("-stylesheetfile") ||
				option.equals("-link") ||
				option.equals("-linksourcetab") ||
				option.equals("-charset") ||
				option.equals("-overview") ||
				option.equals("-tag") ||
				option.equals("-tagletpath") ||
				option.equals("-taglet"))
		{
			return 2;
		}
		else if(option.equals("-group") ||
				option.equals("-linkoffline"))
		{
			return 3;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * After parsing the available options using
	 * {@link #getDocletOptionLength(String)},
	 * Jadexdoc invokes this method with an array of options-arrays, where
	 * the first item in any array is the option, and subsequent items in
	 * that array are its arguments.
	 * @param optlist Options used on the command line.
	 * @return true if all the options are valid.
	 */
	public boolean validDocletOptions(List optlist)
	{
		boolean helpfile = false;
		boolean nohelp = false;
		boolean overview = false;
		boolean nooverview = false;
		boolean splitindex = false;
		boolean noindex = false;

		Group testgroup = new Group(this);

		String[][] options = (String[][])optlist.toArray(new String[optlist.size()][]);

		//  look at our options
		for(int oi = 0; oi<options.length; ++oi)
		{
			String[] os = options[oi];
			String opt = os[0].toLowerCase();
			if(opt.equals("-helpfile"))
			{
				if(nohelp==true)
				{
					standardmessage.error("doclet.Option_conflict", "-helpfile", "-nohelp");
					return false;
				}
				if(helpfile==true)
				{
					standardmessage.error("doclet.Option_reuse", "-helpfile");
					return false;
				}
				File help = new File(os[1]);
				if(!help.exists())
				{
					standardmessage.error("doclet.File_not_found", os[1]);
					return false;
				}
				helpfile = true;
			}
			else if(opt.equals("-nohelp"))
			{
				if(helpfile==true)
				{
					standardmessage.error("doclet.Option_conflict", "-nohelp", "-helpfile");
					return false;
				}
				nohelp = true;
			}
			else if(opt.equals("-overview"))
			{
				if(nooverview==true)
				{
					standardmessage.error("doclet.Option_conflict", "-overview", "-nooverview");
					return false;
				}
				if(overview==true)
				{
					standardmessage.error("doclet.Option_reuse", "-overview");
					return false;
				}
				overview = true;
			}
			else if(opt.equals("-nooverview"))
			{
				if(overview==true)
				{
					standardmessage.error("doclet.Option_conflict", "-nooverview", "-overview");
					return false;
				}
				nooverview = true;
			}
			else if(opt.equals("-splitindex"))
			{
				if(noindex==true)
				{
					standardmessage.error("doclet.Option_conflict", "-splitindex", "-noindex");
					return false;
				}
				splitindex = true;
			}
			else if(opt.equals("-noindex"))
			{
				if(splitindex==true)
				{
					standardmessage.error("doclet.Option_conflict", "-noindex", "-splitindex");
					return false;
				}
				noindex = true;
			}
			else if(opt.equals("-group"))
			{
				if(!testgroup.checkPackageGroups(os[1], os[2]))
				{
					return false;
				}
			}
		}
		return true;
	}


}


