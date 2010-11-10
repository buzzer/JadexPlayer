package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import jadex.tools.jadexdoc.PathManager;
import jadex.tools.jadexdoc.doclets.*;


/**
 * Class to generate Tree page for a package. The name of the file generated is
 * "package-tree.html" and it is generated in the respective package directory.
 */

public class PackageTreeWriter extends AbstractTreeWriter
{

	/** Package for which tree is to be generated. */
	protected String packagename;

	/** The previous package name in the alpha-order list. */
	protected String prev;

	/** The next package name in the alpha-order list. */
	protected String next;

	/**
	 * Constructor.
	 * @throws java.io.IOException
	 * @throws DocletAbortException
	 */
	public PackageTreeWriter(StandardConfiguration configuration,
			String path, String filename,
			String pkg,
			String prev, String next)
			throws IOException
	{
		super(configuration, path, filename,
				new AgentTree(configuration.getAgentsForPackage(pkg)), pkg);
		this.packagename = pkg;
		this.prev = prev;
		this.next = next;
	}

	/**
	 * Construct a PackageTreeWriter object and then use it to generate the
	 * package tree page.
	 * @param pkg Package for which tree file is to be generated.
	 * @param prev Previous package in the alpha-ordered list.
	 * @param next Next package in the alpha-ordered list.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration,
			String pkg, String prev,
			String next)
	{
		PackageTreeWriter packgen;
		String path = PathManager.getDirectoryPath(pkg);
		String filename = "package-tree.html";
		try
		{
			packgen = new PackageTreeWriter(configuration, path, filename, pkg,
					prev, next);
			packgen.generatePackageTreeFile();
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
	 * Generate a separate tree file for each package.
	 */
	protected void generatePackageTreeFile() throws IOException
	{
		printHtmlHeader(packagename+" "+getText("doclet.Window_Agent_Hierarchy"));

		printPackageTreeHeader();

		if(configuration.cmdLinePackages.size()>1)
		{
			printLinkToMainTree();
		}

		generateTree(agenttree.baseagents(), "doclet.Agent_Hierarchy");
		generateTree(agenttree.basecapabilities(), "doclet.Capability_Hierarchy");

		printPackageTreeFooter();
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * Print the navigation bar header for the package tree file.
	 */
	protected void printPackageTreeHeader()
	{
		navLinks(true);
		hr();
		center();
		h2(getText("doclet.Hierarchy_For_Package", packagename));
		centerEnd();
	}

	/**
	 * Generate a link to the tree for all the packages.
	 */
	protected void printLinkToMainTree()
	{
		dl();
		dt();
		boldText("doclet.Package_Hierarchies");
		dd();
		navLinkMainTree(getText("doclet.All_Packages"));
		dlEnd();
		hr();
		ddEnd();
		dtEnd();
		dlEnd();
	}

	/**
	 * Print the navigation bar footer for the package tree file.
	 */
	protected void printPackageTreeFooter()
	{
		hr();
		navLinks(false);
	}

	/**
	 * Link for the previous package tree file.
	 */
	protected void navLinkPrevious()
	{
		if(prev==null)
		{
			navLinkPrevious(null);
		}
		else
		{
			String path = PathManager.getRelativePath(packagename,
					prev);
			navLinkPrevious(path+"package-tree.html");
		}
	}

	/**
	 * Link for the next package tree file.
	 */
	protected void navLinkNext()
	{
		if(next==null)
		{
			navLinkNext(null);
		}
		else
		{
			String path = PathManager.getRelativePath(packagename,
					next);
			navLinkNext(path+"package-tree.html");
		}
	}

	/**
	 * Link to the package summary page for the package of this tree.
	 */
	protected void navLinkPackage()
	{
		navCellStart();
		printHyperLink("package-summary.html", "", getText("doclet.Package"),
				true, "NavBarFont1");
		navCellEnd();
	}
}
