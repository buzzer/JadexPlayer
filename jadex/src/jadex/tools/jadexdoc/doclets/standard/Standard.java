package jadex.tools.jadexdoc.doclets.standard;

import java.io.*;
import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.*;
import jadex.tools.jadexdoc.doclets.*;
import jadex.util.SUtil;

/**
 *
 */
public class Standard implements IJadexDoclet
{

	/** The build version number. */
	public static final String BUILD_VERSION = getJadexReleaseNumber();

	/** The global configuration information for this run. */
	private StandardConfiguration configuration;// = (StandardConfiguration)createConfiguration(); //= new StandardConfiguration();


	/**
	 * The "start" method as required by Jadexdoc.
	 * @return boolean
	 */
	public boolean start()
	{
//                try {
		startGeneration();
//                } catch (RuntimeException exc) {
//                exc.printStackTrace();
//                    return false;
//                }
		return true;
	}

	/**
	 * Create the configuration instance.
	 * Override this method to use a different
	 * configuration.
	 * /
	public Configuration createConfiguration()
	{
		return new StandardConfiguration();
	}*/

	/**
	 * Get current configuration instance.
	 * @return Configuration The current configuration instance.
	 */
	public Configuration getConfiguration()
	{
		if(configuration==null)
			configuration = new StandardConfiguration();
		return configuration;
	}


	/**
	 * Start the generation of files. Call generate methods in the individual
	 * writers, which will in turn genrate the documentation files.
	 */
	public void startGeneration()
	{
		if(configuration.getSpecifiedAgents().length==0)
		{
			configuration.standardmessage.
					error("doclet.No_Agents_To_Document");
			return;
		}
		if(!configuration.quiet)
		{
			configuration.standardmessage.notice("stddoclet.version", BUILD_VERSION);
			configuration.printedVersion = true;
		}

		String configdestdir = configuration.destdirname;
		String confighelpfile = configuration.helpfile;
		String configstylefile = configuration.stylesheetfile;
		performCopy(configdestdir, confighelpfile);
		performCopy(configdestdir, configstylefile);
		Util.copyResourceFile(configuration, "inherit.png", false);

		AgentTree agenttree = new AgentTree(configuration);
		// do early to reduce memory footprint
		/**if(configuration.classuse)
		{
            AgentUseMapper.generate(configuration, classtree);
		}*/

		IndexWriter.generate(configuration);

		if(configuration.createtree)
		{
			TreeWriter.generate(configuration, agenttree);
		}
		if(configuration.createindex)
		{
			IndexBuilder indexbuilder = new IndexBuilder(configuration);
			if(configuration.splitindex)
			{
				SplitIndexWriter.generate(configuration, indexbuilder);
			}
			else
			{
				SingleIndexWriter.generate(configuration, indexbuilder);
			}
		}

		AllAgentsFrameWriter.generate(configuration, new IndexBuilder(configuration, true));

		if(configuration.createoverview)
		{
			PackageIndexWriter.generate(configuration);
		}

		String[] inclPackages = configuration.specifiedPackages();

		if(inclPackages.length>1)
		{
			PackageIndexFrameWriter.generate(configuration);
		}

		if(!(inclPackages.length==1 && inclPackages[0].equals("")))
		{
			String prev = null, next;
			for(int i = 0; i<inclPackages.length; i++)
			{
				PackageFrameWriter.generate(configuration, inclPackages[i]);
				//Don't generate package page for unnamed package
				if(inclPackages[i].length()==0)
				{
					continue;
				}
				next = (i+1<inclPackages.length && inclPackages[i+1].length()>0)?
						inclPackages[i+1]: null;
				//If the next package is unnamed package, skip 2 ahead if possible
				next = (i+2<inclPackages.length && next==null)?
						inclPackages[i+2]: next;
				PackageWriter.generate(configuration, inclPackages[i], prev, next);
				if(configuration.createtree)
				{
					PackageTreeWriter.generate(configuration, inclPackages[i], prev, next);
				}
				prev = inclPackages[i];
			}
		}

		generateAgentFiles(agenttree);

		if(configuration.sourcepath!=null && configuration.sourcepath.length()>0)
		{
			StringTokenizer pathTokens = new StringTokenizer(configuration.sourcepath, ":");
			boolean first = true;
			while(pathTokens.hasMoreTokens())
			{
				HtmlStandardWriter.copyDocFiles(configuration, pathTokens.nextToken()
						+File.separator, HtmlStandardWriter.DOC_FILES_DIR_NAME, first);
				first = false;
			}
		}


		PackageListWriter.generate(configuration);
		if(configuration.helpfile.length()==0 && !configuration.nohelp)
		{
			HelpWriter.generate(configuration);
		}

		if(configuration.stylesheetfile.length()==0)
		{
			StylesheetWriter.generate(configuration);
		}
	}

	/**
	 *
	 * @param agenttree
	 */
	protected void generateAgentFiles(AgentTree agenttree)
	{
		String[] packages = configuration.specifiedPackages();
		for(int i = 0; i<packages.length; i++)
		{
			String pkgname = packages[i];
			generateAgentCycle(pkgname, agenttree);
		}
	}


	/**
	 * Instantiate AgentWriter for each agent within the package
	 * passed to it and generate Documentation for that.
	 */
	protected void generateAgentCycle(String pkgname, AgentTree agenttree)
	{
		IMCapability[] arr = configuration.getSpecifiedAgents();
		List agents = new ArrayList();

		for(int i = 0; i<arr.length; i++)
		{
			IMCapability agent = arr[i];
			String agentpackage = agent.getPackage();
			if(SUtil.equals(agentpackage, pkgname))
			{
				agents.add(agent);
			}
		}
		IMCapability[] pkgarr = (IMCapability[])agents.toArray(new IMCapability[agents.size()]);
		generateAgentCycle(pkgarr, agenttree);
	}

	/**
	 *
	 * @param arr
	 * @param agenttree
	 */
	protected void generateAgentCycle(IMCapability[] arr, AgentTree agenttree)
	{
		Arrays.sort(arr, new ElementNameComparator());
		for(int i = 0; i<arr.length; i++)
		{
			IMCapability prev = (i==0)? null: arr[i-1];
			IMCapability curr = arr[i];
			IMCapability next = (i+1==arr.length)? null: arr[i+1];

			generate(configuration, curr, prev, next, agenttree);
		}
	}

	/**
	 *
	 * @param configdestdir
	 * @param filename
	 */
	protected void performCopy(String configdestdir, String filename)
	{
		try
		{
			String destdir = (configdestdir.length()>0)?
					configdestdir+File.separatorChar: "";
			if(filename.length()>0)
			{
				File helpstylefile = new File(filename);
				String parent = helpstylefile.getParent();
				String helpstylefilename = (parent==null)?
						filename:
						filename.substring(parent.length()+1);
				File desthelpfile = new File(destdir+helpstylefilename);
				if(!desthelpfile.getCanonicalPath().equals(helpstylefile.getCanonicalPath()))
				{
					Util.copyFile(desthelpfile, helpstylefile);
				}
			}
		}
		catch(IOException exc)
		{

		}
	}

	/**
	 *
	 * @return
	 */
	private static String getJadexReleaseNumber()
	{

		try
		{
			ResourceBundle versionProperties = ResourceBundle.getBundle("jadex.config.version");
			return versionProperties.getString("jadex.release.number");
		}
		catch(MissingResourceException e)
		{
			return "missing release number";
		}
	}

	protected static Set containingPackagesSeen = new HashSet();


	/**
	 * Generate a agent page.
	 * @param prev the previous agent generated, or null if no previous.
	 * @param capa the capa to generate.
	 * @param next the next capa to be generated, or null if no next.
	 */
	public static void generate(StandardConfiguration configuration, IMCapability capa, IMCapability prev,
			IMCapability next, AgentTree agenttree)
	{
		//        String pkgpath =  PathManager.getDirectoryPath(agent.containingPackage());
		String pkgpath = PathManager.getDirectoryPath(capa.getPackage());
		try
		{
			CapabilityWriter capgen;
			if(capa instanceof IMBDIAgent)
			{
				capgen = new AgentWriter(configuration, pkgpath, (IMBDIAgent)capa, prev, next, agenttree);
			}
			else
			{
				capgen = new CapabilityWriter(configuration, pkgpath, capa, prev, next, agenttree);
			}
			capgen.generateDocumentationFile();
			String cp = capa.getPackage();

			if((configuration.cmdLinePackages==null || !configuration.cmdLinePackages.contains(cp)) &&
				!containingPackagesSeen.contains(cp))
			{

				//Only copy doc files dir if the containing package is not documented
				//AND if we have not documented a class from the same package already.
				//Otherwise, we are making duplicate copies.
				HtmlStandardWriter.copyDocFiles(configuration, HtmlStandardWriter.getSourcePath(configuration, capa.getPackage()),
					pkgpath+HtmlWriter.fileseparator+HtmlStandardWriter.DOC_FILES_DIR_NAME, true);
				containingPackagesSeen.add(cp);
			}
			capgen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), CapabilityWriter.getLocalFilename(capa));
			throw new DocletAbortException();
		}
	}

	/**
	 *  Return the member name or "?" if unknown.
	 */
	public static String getMemberName(IMElement elem)
	{
		return elem.getName()==null? "?": elem.getName();
	}

}



