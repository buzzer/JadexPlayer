package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import java.util.*;
import jadex.model.IMCapability;
import jadex.tools.jadexdoc.PathManager;

/**
 * Abstract class to generate file for each package contents. Sub-classed to
 * generate specific formats Frame and Non-Frame Output by
 * {@link PackageIndexFrameWriter} and {@link PackageIndexFrameWriter}
 * respectively.
 */
public abstract class AbstractPackageWriter extends HtmlStandardWriter
{

	/** The agents to be documented.  Use this to filter out agents that will not be documented. */
	protected Set documentedAgents;

	/** The package under consideration. */
	String packagename;

	/**
	 * Create appropriate directory for the package and also initilise the
	 * relative path from this generated file to the current or
	 * the destination directory.
	 * @param path Directories in this path will be created if they are not
	 * already there.
	 * @param filename Name of the package summary file to be generated.
	 * @param packagedoc PackageDoc under consideration.
	 * @throws com.sun.tools.doclets.DocletAbortException
	 */
	public AbstractPackageWriter(StandardConfiguration configuration,
			String path, String filename,
			String packagedoc)
			throws IOException
	{
		super(configuration, path, filename,
				PathManager.getRelativePath(packagedoc));
		this.packagename = packagedoc;
		if(configuration.specifiedPackages().length==0)
		{
			IMCapability[] classes = configuration.getSpecifiedAgents();
			documentedAgents = new HashSet();
			for(int i = 0; i<classes.length; i++)
			{
				documentedAgents.add(classes[i]);
			}
		}
	}

	/**
	 *
	 */
	protected abstract void generateAgentListing();

	/**
	 *
	 * @throws IOException
	 */
	protected abstract void printPackageDescription() throws IOException;

	/**
	 *
	 * @param head
	 */
	protected abstract void printPackageHeader(String head);

	/**
	 *
	 */
	protected abstract void printPackageFooter();

	/**
	 * Generate Individual Package File with Agent and Capability
	 * Listing with the appropriate links. Calls the methods from the
	 * sub-classes to generate the file contents.
	 * @param includeScript boolean true when including windowtitle script
	 */
	protected void generatePackageFile(boolean includeScript) throws IOException
	{
		String pkgName = packagename;
		String[] metakeywords = {pkgName+" "+"package"};

		printHtmlHeader(pkgName, metakeywords, includeScript);
		printPackageHeader(pkgName);

		generateAgentListing();
		printPackageDescription();

		printPackageFooter();
		printBodyHtmlEnd();
	}

	/**
	 * Highlight "Package" in the navigation bar, as this is the package page.
	 */
	protected void navLinkPackage()
	{
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Package");
		fontEnd();
		navCellEnd();
	}
}



