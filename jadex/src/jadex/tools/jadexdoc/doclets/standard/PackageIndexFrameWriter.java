package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import jadex.tools.jadexdoc.doclets.DocletAbortException;
import jadex.tools.jadexdoc.Comment;

/**
 * Generate the package index for the left-hand frame in the generated output.
 * A click on the package name in this frame will update the page in the bottom
 * left hand frame with the listing of contents of the clicked package.
 */
public class PackageIndexFrameWriter extends AbstractPackageIndexWriter
{

	/**
	 * Construct the PackageIndexFrameWriter object.
	 * @param filename Name of the package index file to be generated.
	 */
	public PackageIndexFrameWriter(StandardConfiguration configuration,
			String filename) throws IOException
	{
		super(configuration, filename);
	}

	/**
	 * Generate the package index file named "overview-frame.html".
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration)
	{
		PackageIndexFrameWriter packgen;
		String filename = "overview-frame.html";
		try
		{
			packgen = new PackageIndexFrameWriter(configuration, filename);
			packgen.generatePackageIndexFile(false);
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
	 * Print each package name on separate rows.
	 * @param pkgname The name of the package
	 */
	protected void printIndexRow(String pkgname, Comment comment)
	{
		fontStyle("FrameItemFont");
		if(pkgname.length()>0)
		{
			printTargetHyperLink(pathString(pkgname, "package-frame.html"),
					"packageFrame", pkgname);
			//printSummaryComment(comment);
		}
		else
		{
			printTargetHyperLink("package-frame.html",
					"packageFrame", "&lt;unnamed package>");
			//printSummaryComment(comment);
		}
		fontEnd();
		br();
	}

	/**
	 * Print the "-packagesheader" string in bold format, at top of the page,
	 * if it is not the empty string.  Otherwise print the "-header" string.
	 * Despite the name, there is actually no navigation bar for this page.
	 */
	protected void printNavigationBarHeader()
	{
		printTableHeader();
		fontSizeStyle("+1", "FrameTitleFont");
		if(configuration.packagesheader.length()>0)
		{
			bold(replaceDocRootDir(configuration.packagesheader));
		}
		else
		{
			bold(replaceDocRootDir(configuration.header));
		}
		fontEnd();
		printTableFooter();
	}

	/**
	 * Do nothing as there is no overview information in this page.
	 */
	protected void printOverviewHeader()
	{
	}

	/**
	 * Print Html "table" tag for the package index format.
	 * @param text Text string will not be used in this method.
	 */
	protected void printIndexHeader(String text)
	{
		printTableHeader();
	}

	/**
	 * Print Html closing "table" tag at the end of the package index.
	 */
	protected void printIndexFooter()
	{
		printTableFooter();
	}

	/**
	 * Print "All Agents" link at the top of the left-hand frame page.
	 */
	protected void printAllAgentsPackagesLink()
	{
		fontStyle("FrameItemFont");
		printTargetHyperLink("allagents-frame.html", "packageFrame",
				getText("doclet.All_Agents"));
		fontEnd();
		p();
		fontSizeStyle("+1", "FrameHeadingFont");
		printText("doclet.Packages");
		fontEnd();
		br();
	}

	/**
	 * Just print some space, since there is no navigation bar for this page.
	 */
	protected void printNavigationBarFooter()
	{
		p();
		space();
	}

	/**
	 * Print Html closing tags for the table for package index.
	 */
	protected void printTableFooter()
	{
		tdEnd();
		trEnd();
		tableEnd();
	}

	/**
	 * Print Html tags for the table for package index.
	 */
	protected void printTableHeader()
	{
		table();
		tr();
		tdNowrap();
	}
}




