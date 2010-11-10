package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import java.util.*;
import jadex.tools.jadexdoc.doclets.*;


/**
 * Generate Agent Hierarchy page for all the agents in this run.
 */

public class TreeWriter extends AbstractTreeWriter
{

	/**
	 * Packages in this run.
	 */
	private List packages;

	/**
	 * True if there are no packages specified on the command line,
	 * False otherwise.
	 */
	private boolean agentsonly;

	/**
	 * Constructor to construct TreeWriter object.
	 */
	public TreeWriter(StandardConfiguration configuration,
			String filename, AgentTree agenttree)
			throws IOException
	{
		super(configuration, filename, agenttree);
		packages = configuration.cmdLinePackages;
		agentsonly = packages.size()==0;
	}

	/**
	 * Create a TreeWriter object and use it to generate the
	 * "overview-tree.html" file.
	 */
	public static void generate(StandardConfiguration configuration,
			AgentTree agenttree)
	{
		TreeWriter treegen;
		String filename = "overview-tree.html";
		try
		{
			treegen = new TreeWriter(configuration, filename, agenttree);
			treegen.generateTreeFile();
			treegen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Print the agent hierarchy and capability hierarchy in the file.
	 */
	public void generateTreeFile() throws IOException
	{
		printHtmlHeader(getText("doclet.Window_Agent_Hierarchy"));

		printTreeHeader();

		printPageHeading();

		printPackageTreeLinks();

		generateTree(agenttree.baseagents(), "doclet.Agent_Hierarchy");
		generateTree(agenttree.basecapabilities(), "doclet.Capability_Hierarchy");

		printTreeFooter();
	}

	/**
	 * Generate the links to all the package tree files.
	 */
	protected void printPackageTreeLinks()
	{
		//Do nothing if only unnamed package is used
		if(packages.size()==1 && ((String)packages.get(0)).length()==0)
		{
			return;
		}
		if(!agentsonly)
		{
			dl();
			dt();
			boldText("doclet.Package_Hierarchies");
			dd();
			Collections.sort(packages);
			for(int i = 0; i<packages.size(); i++)
			{
				if(((String)packages.get(i)).length()==0)
				{
					continue;
				}
				String filename = pathString((String)packages.get(i), "package-tree.html");
				printHyperLink(filename, "", (String)packages.get(i));
				if(i<packages.size()-1)
				{
					print(", ");
				}
			}
			ddEnd();
			dtEnd();
			dlEnd();
			hr();
		}
	}

	/**
	 * Print the navigation bar links at the top.
	 */
	protected void printTreeHeader()
	{
		navLinks(true);
		hr();
	}

	/**
	 * Print the navigation bar links at the bottom.
	 */
	protected void printTreeFooter()
	{
		hr();
		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * Print the page title "Hierarchy For All Packages" at the top of the tree
	 * page.
	 */
	protected void printPageHeading()
	{
		center();
		h2();
		printText("doclet.Hierarchy_For_All_Packages");
		h2End();
		centerEnd();
	}
}
