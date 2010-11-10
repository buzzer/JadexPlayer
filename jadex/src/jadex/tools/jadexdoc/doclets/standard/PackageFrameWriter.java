package jadex.tools.jadexdoc.doclets.standard;


import java.io.IOException;
import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.PathManager;
import jadex.tools.jadexdoc.doclets.*;

/**
 * Class to generate file for each package contents in the left-hand bottom
 * frame. This will list all the agent kinds in the package. A click on any
 * agent-kind will update the right-hand frame with the clicked agent-kind page.
 */
public class PackageFrameWriter extends AbstractPackageWriter
{

	/**
	 * Constructor to construct PackageFrameWriter object and to generate
	 * "package-frame.html" file in the respective package directory.
	 * For example for package "java.lang" this will generate file
	 * "package-frame.html" file in the "java/lang" directory. It will also
	 * create "java/lang" directory in the current or the destination directory
	 * if it doesen't exist.
	 * @param path Directories in this path will be created if they are not
	 * already there.
	 * @param filename Name of the package summary file to be generated,
	 * "package-frame.html".
	 * @param pkgname Package name under consideration.
	 * @throws java.io.IOException
	 * @throws DocletAbortException
	 */
	public PackageFrameWriter(StandardConfiguration configuration,
			String path, String filename,
			String pkgname)
			throws IOException
	{
		super(configuration, path, filename, pkgname);
	}

	/**
	 * Generate a package summary page for the left-hand bottom frame. Construct
	 * the PackageFrameWriter object and then uses it generate the file.
	 * @param pkgname The package for which "pacakge-frame.html" is to be generated.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration,
			String pkgname)
	{
		PackageFrameWriter packgen;
		String path = PathManager.getDirectoryPath(pkgname);
		String filename = "package-frame.html";
		try
		{
			packgen = new PackageFrameWriter(configuration, path,
					filename, pkgname);
			packgen.generatePackageFile(false);
			packgen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Generate agent listing for all the agents in this package. Divide agent
	 * listing as per the agent kind and generate separate listing for
	 * BDIAgents, Capabilities.
	 */
	protected void generateAgentListing()
	{
		IMCapability[] allagents = configuration.getSpecifiedAgents();
		List agents = new ArrayList();
		List capabilities = new ArrayList();
		for(int i = 0; i<allagents.length; i++)
		{
			IMCapability agent = allagents[i];
			String pkgname = agent.getPackage()==null? "": agent.getPackage();
			if(pkgname.equals(packagename))
			{
				if(agent instanceof IMBDIAgent)  // todo: hack
				{
					agents.add(agent);
				}
				else
				{
					capabilities.add(agent);
				}
			}
		}
		IMCapability[] carr = (IMCapability[])capabilities.toArray(new IMCapability[capabilities.size()]);
		IMCapability[] aarr = (IMCapability[])agents.toArray(new IMCapability[agents.size()]);

		generateAgentKindListing(carr, getText("doclet.Capabilities"));
		generateAgentKindListing(aarr, getText("doclet.Agents"));

	}

	/**
	 * Generate specific agent kind listing. Also add label to the listing.
	 * @param arr Array of specific agent kinds, namely BDIAgent or Capability.
	 * @param label Label for the listing
	 */
	protected void generateAgentKindListing(IMCapability[] arr, String label)
	{
		if(arr.length>0)
		{
			Arrays.sort(arr, new ElementNameComparator());
			printPackageTableHeader();
			fontSizeStyle("+1", "FrameHeadingFont");
			boolean printedHeader = false;
			for(int i = 0; i<arr.length; i++)
			{
				if(documentedAgents!=null && !documentedAgents.contains(arr[i]))
				{
					continue;
				}
				if(!printedHeader)
				{
					print(label);
					fontEnd();
					println("&nbsp;");
					fontStyle("FrameItemFont");
					printedHeader = true;
				}
				br();
				printTargetAgentLink(arr[i], "agentFrame");
			}
			fontEnd();
			printPackageTableFooter();
			println();
		}
	}

	/**
	 * Print the package link at the top of the agent kind listing. Clicking
	 * this link, package-summary page will appear in the right hand frame.
	 * @param heading Top Heading to be used for the agent kind listing.
	 */
	protected void printPackageHeader(String heading)
	{
		fontSizeStyle("+1", "FrameTitleFont");
		printTargetPackageLink(packagename, "agentFrame", heading);
		fontEnd();
	}

	/**
	 * The table for the agent kind listing.
	 */
	protected void printPackageTableHeader()
	{
		table();
		tr();
		tdNowrap();
	}

	/**
	 * Closing Html tags for table of agent kind listing.
	 */
	protected void printPackageTableFooter()
	{
		tdEnd();
		trEnd();
		tableEnd();
	}

	/**
	 * Do nothing. No footer is generated for this page.
	 */
	protected void printPackageFooter()
	{

	}

	/**
	 * Do nothing. Package Description is not generted in this page.
	 */
	protected void printPackageDescription() throws IOException
	{
	}
}



