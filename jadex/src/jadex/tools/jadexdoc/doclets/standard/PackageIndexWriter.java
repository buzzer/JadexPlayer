package jadex.tools.jadexdoc.doclets.standard;

import java.io.*;
import java.util.*;
import jadex.tools.jadexdoc.Comment;
import jadex.tools.jadexdoc.doclets.DocletAbortException;
import jadex.util.SUtil;

/**
 * Generate the package index page "overview-summary.html" for the right-hand
 * frame. A click on the package name on this page will update the same frame
 * with the "pacakge-summary.html" file for the clicked package.
 */
public class PackageIndexWriter extends AbstractPackageIndexWriter
{

	/** Used for "overview" documentation. */
	private Comment comment;

	/** Map representing the group of packages as specified on the command line.
		@see jadex.tools.jadexdoc.doclets.Group */
	private Map groupPackageMap;

	/** List to store the order groups as specified on the command line. */
	private List groupList;

	/**
	 * Construct the PackageIndexWriter. Also constructs the grouping
	 * information as provided on the command line by "-group" option. Stores
	 * the order of groups specified by the user.
	 * @see jadex.tools.jadexdoc.doclets.Group
	 */
	public PackageIndexWriter(StandardConfiguration configuration,
			String filename)
			throws IOException
	{
		super(configuration, filename);

		groupPackageMap = configuration.group.groupPackages(packages);
		groupList = configuration.group.getGroupList();

		comment = configuration.overviewfile!=null? comment(configuration.overviewfile, configuration): null;
	}

	/**
	 * Generate the package index page for the right-hand frame.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration)
	{
		PackageIndexWriter packgen;
		String filename = "overview-summary.html";
		try
		{
			packgen = new PackageIndexWriter(configuration, filename);
			packgen.generatePackageIndexFile(true);
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
	 * Print each package in separate rows in the index table. Generate link
	 * to each package.
	 * @param pkgname Package to which link is to be generated.
	 */
	protected void printIndexRow(String pkgname, Comment packagedoc)
	{
		if(pkgname.length()>0)
		{
			trBgcolorStyle("white", "TableRowColor");
			summaryRow(20);
			bold();
			printPackageLink(pkgname);
			boldEnd();
			summaryRowEnd();
			summaryRow(80);
			printSummaryComment(packagedoc);
			summaryRowEnd();
			trEnd();
		}
	}

	/**
	 * Depending upon the grouping information and their titles, generate
	 * separate table indices for each package group.
	 */
	protected void generateIndex()
	{
		for(int i = 0; i<groupList.size(); i++)
		{
			String groupname = (String)groupList.get(i);
			List list = (List)groupPackageMap.get(groupname);
			if(list!=null && list.size()>0)
			{
				printIndexContents((String[])list.
						toArray(new String[list.size()]),
						groupname);
			}
		}
	}

	/**
	 * Print the overview summary comment for this documentation. Print one line
	 * summary at the top of the page and generate a link to the description,
	 * which is generated at the end of this page.
	 */
	protected void printOverviewHeader()
	{
		if(comment!=null)
		{
			printSummaryComment(comment);
			p();
			bold(getText("doclet.See"));
			br();
			printNbsps();
			printHyperLink("", "overview_description",
					getText("doclet.Description"), true);
			p();
		}
	}

	/**
	 * Print Html tags for the table for this package index.
	 */
	protected void printIndexHeader(String text)
	{
		tableIndexSummary();
		tableHeaderStart("#CCCCFF");
		bold(text);
		tableHeaderEnd();
	}

	/**
	 * Print Html closing tags for the table for this package index.
	 */
	protected void printIndexFooter()
	{
		tableEnd();
		p();
		space();
	}

	/**
	 * Print the overview comment as provided in the file specified by the
	 * "-overview" option on the command line.
	 */
	protected void printOverview()
	{
		if(comment!=null)
		{
			anchor("overview_description");
			p();
			printInlineComment(comment);
			p();
		}
	}

	/**
	 * Print the header for navigation bar. Also print the "-title" specified
	 * on command line, at the top of page.
	 */
	protected void printNavigationBarHeader()
	{
		navLinks(true);
		hr();
		printConfigurationTitle();
	}

	/**
	 * Print the footer fornavigation bar. Also print the "-bottom" specified
	 * on command line, at the top of page.
	 */
	protected void printNavigationBarFooter()
	{
		hr();
		navLinks(false);
		printBottom();
	}
}



