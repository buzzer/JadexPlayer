package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import java.util.List;
import jadex.model.*;
import jadex.tools.jadexdoc.PathManager;
import jadex.tools.jadexdoc.doclets.AgentTree;

/**
 * Abstract class to print the agent hierarchy page for all the Agents. This
 * is sub-classed by {@link PackageTreeWriter} and {@link TreeWriter} to
 * generate the Package Tree and global Tree(for all the agents and packages)
 * pages.
 */

public abstract class AbstractTreeWriter extends HtmlStandardWriter
{

	/**
	 * The agent and capability tree built by using
	 * {@link AgentTree}
	 */
	protected final AgentTree agenttree;

	/**
	 * Constructor initilises agenttree variable. This constructor will be used
	 * while generating global tree file "overview-tree.html".
	 * @param filename File to be generated.
	 * @param agenttree Tree built by {@link AgentTree}
	 * @throws java.io.IOException
	 * @throws jadex.tools.jadexdoc.doclets.DocletAbortException
	 */
	protected AbstractTreeWriter(StandardConfiguration configuration,
			String filename, AgentTree agenttree)
			throws IOException
	{
		super(configuration, filename);
		this.agenttree = agenttree;
	}

	/**
	 * Create appropriate directory for the package and also initilise the
	 * relative path from this generated file to the current or
	 * the destination directory. This constructor will be used while
	 * generating "package tree" file.
	 * @param path Directories in this path will be created if they are not
	 * already there.
	 * @param filename Name of the package tree file to be generated.
	 * @param agenttree The tree built using {@link AgentTree}
	 * for the package pkg.
	 * @param pkg package for which tree file will be generated.
	 * @throws java.io.IOException
	 * @throws com.sun.tools.doclets.DocletAbortException
	 */
	protected AbstractTreeWriter(StandardConfiguration configuration,
			String path, String filename,
			AgentTree agenttree, String pkg)
			throws IOException
	{
		super(configuration,
				path, filename, PathManager.getRelativePath(pkg));
		this.agenttree = agenttree;
	}

	/**
	 * Generate each level of the agent tree. For each containing
	 * capability indents the next level information.
	 * Recurses itself to generate containing capability info.
	 * @param parent the sub-capability or super-supercapability of the list.
	 * @param list list of the sub-capabilities at this level.
	 */
	protected void generateLevelInfo(IMCapability parent, List list, boolean agentlist)
	{
		if(list.size()>0)
		{
			ul();
			for(int i = 0; i<list.size(); i++)
			{
				IMCapability local = (IMCapability)list.get(i);
				printPartialInfo(local);
				List newlist = agentlist? agenttree.subcapabilities(local): agenttree.supercapabilities(local);
				generateLevelInfo(local, newlist, agentlist);   // Recurse
			}
			ulEnd();
		}
	}

	/**
	 * Generate the heading for the tree depending upon tree type if it's a
	 * Agent Tree or Capability tree and also print the tree.
	 * @param list List of capability which are at the most base level, all the
	 * other capabilities in this run will derive from these capability.
	 * @param heading Heading for the tree.
	 */
	protected void generateTree(List list, String heading)
	{
		if(list.size()>0)
		{
			IMCapability cd = (IMCapability)list.get(0);
			printTreeHeading(heading);
//            generateLevelInfo((cd instanceof IMBDIAgent)? (IMCapability)list.get(0): null, list);
			generateLevelInfo(cd, list, cd instanceof IMBDIAgent);
		}
	}


	/**
	 * Print information about the agent kind, if it's a "agent" or "capability".
	 * @param cap IMCapability.
	 */
	protected void printPartialInfo(IMCapability cap)
	{
		li("circle");
		print((cap instanceof IMBDIAgent)? "agent ": "capability ");
		printPreQualifiedBoldAgentLink(cap);
	}

	/**
	 * Print the heading for the tree.
	 * @param heading Heading for the tree.
	 */
	protected void printTreeHeading(String heading)
	{
		h2();
		println(getText(heading));
		h2End();
	}

	/**
	 * Highlight "Tree" word in the navigation bar, since this is the tree page.
	 */
	protected void navLinkTree()
	{
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Tree");
		fontEnd();
		navCellEnd();
	}
}
