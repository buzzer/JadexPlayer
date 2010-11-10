package jadex.tools.jadexdoc.doclets.standard;

import java.io.*;
import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.*;
import jadex.tools.jadexdoc.doclets.*;
import jadex.util.SUtil;

/**
 * Class to generate file for each package contents in the right-hand
 * frame. This will list all the agent kinds in the package..
 */
public class PackageWriter extends AbstractPackageWriter
{

	public static final String PACKAGE_FILE_NAME = "package.html";

	/** Used for package documentation. */
	private Comment comment;

	/** The prev package name in the alpha-order list. */
	protected String prev;

	/** The next package name in the alpha-order list. */
	protected String next;

	/**
	 * Constructor to construct PackageWriter object and to generate
	 * "package-summary.html" file in the respective package directory.
	 * For example for package "java.lang" this will generate file
	 * "package-summary.html" file in the "java/lang" directory. It will also
	 * create "java/lang" directory in the current or the destination directory
	 * if it doesen't exist.
	 * @param path Directories in this path will be created if they are not
	 * already there.
	 * @param filename Name of the package summary file to be generated,
	 * "package-frame.html".
	 * @param packagedoc PackageDoc under consideration.
	 * @param prev Previous package in the sorted array.
	 * @param next Next package in the sorted array.
	 * @throws java.io.IOException
	 * @throws DocletAbortException
	 */
	public PackageWriter(StandardConfiguration configuration, String path, String filename,
			String packagedoc, String prev, String next) throws IOException
	{
		super(configuration, path, filename, packagedoc);
		this.prev = prev;
		this.next = next;

		comment = comment(path+File.separatorChar+PACKAGE_FILE_NAME, configuration);
	}

	/**
	 * Generate a package summary page for the right-hand frame. Construct
	 * the PackageFrameWriter object and then uses it generate the file.
	 * @param pkg The package for which "pacakge-summary.html" is to be
	 * generated.
	 * @param prev Previous package in the sorted array.
	 * @param next Next package in the sorted array.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration,
			String pkg, String prev,
			String next)
	{
		PackageWriter packgen;
		String path = PathManager.getDirectoryPath(pkg);
		String filename = "package-summary.html";
		try
		{
			packgen = new PackageWriter(configuration, path, filename, pkg, prev, next);
			packgen.generatePackageFile(true);
			packgen.close();
			packgen.copyDocFiles(configuration, getSourcePath(configuration, pkg), path+fileseparator
					+DOC_FILES_DIR_NAME, true);
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
	 * BDIAgents and Capabilities.
	 */
	protected void generateAgentListing()
	{
		IMCapability[] allagents = configuration.getSpecifiedAgents();
		List agents = new ArrayList();
		List capabilities = new ArrayList();
		for(int i = 0; i<allagents.length; i++)
		{
			IMCapability agent = allagents[i];
			if(agent.getPackage()!=null && agent.getPackage().equals(packagename))
			{
				if(agent instanceof IMBDIAgent)
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

		generateClassKindListing(carr, getText("doclet.Capabilities"));
		generateClassKindListing(aarr, getText("doclet.Agents"));

	}

	/**
	 * Generate specific agent kind listing. Also add label to the listing.
	 * @param arr Array of specific agent kinds, namely BDIAgent or Capability.
	 * @param label Label for the listing
	 */
	protected void generateClassKindListing(IMCapability[] arr, String label)
	{
		if(arr.length>0)
		{
			Arrays.sort(arr, new ElementNameComparator());
			tableIndexSummary();
			boolean printedHeading = false;
			for(int i = 0; i<arr.length; i++)
			{
				if(documentedAgents!=null && !documentedAgents.contains(arr[i]))
				{
					continue;
				}
				if(!printedHeading)
				{
					printFirstRow(label);
					printedHeading = true;
				}
				trBgcolorStyle("white", "TableRowColor");
				summaryRow(15);
				bold();
				print(getAgentLink(arr[i]));
				boldEnd();
				summaryRowEnd();
				summaryRow(0);

				printSummaryComment(new Comment(arr[i]));

				summaryRowEnd();
				trEnd();
			}
			tableEnd();
			println("&nbsp;");
			p();
		}
	}

	/**
	 * Print the table heading for the agent-listing.
	 * @param label Label for the agent kind listing.
	 */
	protected void printFirstRow(String label)
	{
		tableHeaderStart("#CCCCFF");
		bold(label);
		tableHeaderEnd();
	}

	/**
	 * Print the package comment as specified in the "packages.html" file in
	 * the source package directory.
	 */
	protected void printPackageComment()
	{
		if(configuration.nocomment)
		{
			return;
		}
		if(comment!=null)
		{
			anchor("package_description");
			h2(getText("doclet.Package_Description", packagename));
			p();
			printInlineComment(comment);
			p();
		}
	}

	/**
	 * Print the package description and the tag information from the
	 * "packages.html" file.
	 */
	protected void printPackageDescription() throws IOException
	{
		if(configuration.nocomment)
		{
			return;
		}
		printPackageComment();
		//        generateTagInfo(packagedoc);
	}

	/**
	 * Print one line summary comment for the package at the top of the page and
	 * add link to the description which is generated at the end of the page.
	 * @param heading Package name.
	 */
	protected void printPackageHeader(String heading)
	{
		navLinks(true);
		hr();
		h2(getText("doclet.Package")+" "+heading);
		if(comment!=null && !configuration.nocomment)
		{
			printSummaryComment(comment);
			p();
			bold(getText("doclet.See"));
			br();
			printNbsps();
			printHyperLink("", "package_description",
					getText("doclet.Description"), true);
			p();
		}
	}

	/**
	 * Print the navigation bar links at the bottom also print the "-bottom"
	 * if specified on the command line.
	 */
	protected void printPackageFooter()
	{
		hr();
		navLinks(false);
		printBottom();
	}

	/**
	 * Print "Use" link for this pacakge in the navigation bar.
	 */
	protected void navLinkAgentUse()
	{
		navCellStart();
		printHyperLink("package-use.html", "", getText("doclet.navAgentUse"),
				true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print "PREV PACKAGE" link in the navigation bar.
	 */
	protected void navLinkPrevious()
	{
		if(prev==null)
		{
			printText("doclet.Prev_Package");
		}
		else
		{
			String path = PathManager.getRelativePath(packagename,
					prev);
			printHyperLink(path+"package-summary.html", "",
					getText("doclet.Prev_Package"), true);
		}
	}

	/**
	 * Print "NEXT PACKAGE" link in the navigation bar.
	 */
	protected void navLinkNext()
	{
		if(next==null)
		{
			printText("doclet.Next_Package");
		}
		else
		{
			String path = PathManager.getRelativePath(packagename,
					next);
			printHyperLink(path+"package-summary.html", "",
					getText("doclet.Next_Package"), true);
		}
	}

	/**
	 * Print "Tree" link in the navigation bar. This will be link to the package
	 * tree file.
	 */
	protected void navLinkTree()
	{
		navCellStart();
		printHyperLink("package-tree.html", "", getText("doclet.Tree"),
				true, "NavBarFont1");
		navCellEnd();
	}
}




