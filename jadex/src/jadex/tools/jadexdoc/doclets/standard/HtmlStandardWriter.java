package jadex.tools.jadexdoc.doclets.standard;

import java.io.*;
import java.util.StringTokenizer;
import jadex.model.*;
import jadex.tools.jadexdoc.*;
import jadex.tools.jadexdoc.doclets.*;
import jadex.util.SUtil;

/**
 *
 */
public class HtmlStandardWriter extends HtmlDocWriter
{
	/**
	 * Relative path from the file getting generated to the destination
	 * directory. For example, if the file getting generated is
	 * "java/lang/Object.html", then the relative path string is "../../".
	 * This string can be empty if the file getting generated is in
	 * the destination directory.
	 */
	public String relativepath = "";

	/**
	 * Same as relativepath, but normalized to never be empty or
	 * end with a slash.
	 */
	public String relativepathNoSlash = "";

	/**
	 * Platform-dependent directory path from the current or the
	 * destination directory to the file getting generated.
	 * Used when creating the file.
	 * For example, if the file getting generated is
	 * "jadex/examples/Example.agent.html", then the path string is "jadex/examples".
	 */
	public String path = "";

	/**
	 * Name of the file getting generated. If the file getting generated is
	 * "jadex/examples/Example.agent.html", then the filename is "Example.agent.html".
	 */
	public String filename = "";

	/**
	 * Relative path from the destination directory to the current directory.
	 * For example if the destination directory is "core/api/docs", then the
	 * backpath string will be "../../".
	 */
	public final String backpath;

	/** The display length used for indentation while generating the class page. */
	public int displayLength = 0;

	/** The global configuration information for this run. */
	public StandardConfiguration configuration;

	/** The name of the doc files directory. */
	public static final String DOC_FILES_DIR_NAME = "doc-files";

	/**
	 * Constructor to construct the HtmlStandardWriter object.
	 * @param filename File to be generated.
	 */
	public HtmlStandardWriter(StandardConfiguration configuration, String filename) throws IOException
	{
		super(configuration, filename);
		this.configuration = configuration;
		this.backpath = PathManager.getBackPath(configuration.destdirname);
		this.filename = filename;
	}

	/**
	 * Constructor to construct the HtmlStandardWriter object.
	 * @param path Platform-dependent {@link #path} used when
	 * creating file.
	 * @param filename Name of file to be generated.
	 * @param relativepath Value for the variable {@link #relativepath}.
	 */
	public HtmlStandardWriter(StandardConfiguration configuration, String path, String filename,
			String relativepath) throws IOException
	{
		super(configuration, path, filename);
		this.configuration = configuration;
		this.backpath = PathManager.getBackPath(configuration.destdirname);
		this.path = path;
		this.relativepath = relativepath;
		this.relativepathNoSlash = PathManager.getPathNoTrailingSlash(this.relativepath);
		this.filename = filename;
	}

	/**
	 * Copy the given directory contents from the source package directory
	 * to the generated documentation directory. For example for a package
	 * java.lang this method find out the source location of the package using
	 * SourcePath and if given directory is found in the source
	 * directory structure, copy the entire directory, to the generated
	 * documentation hierarchy.
	 * @param srcDirName The original directory to copy from.
	 * @param path The relative path to the directory to be copied.
	 * @param overwrite Overwrite files if true.
	 * @throws jadex.tools.jadexdoc.doclets.DocletAbortException
	 */
	public static void copyDocFiles(StandardConfiguration configuration,
			String srcDirName, String path, boolean overwrite)
	{
		String destname = configuration.destdirname;
		File sourcePath, destPath = new File(destname);
		StringTokenizer pathTokens = new StringTokenizer(configuration.sourcepath==null || configuration.sourcepath.length()==0?
				".": configuration.sourcepath, ":");
		try
		{
			while(pathTokens.hasMoreTokens())
			{
				sourcePath = new File(pathTokens.nextToken());
				if(destPath.getCanonicalPath().equals(sourcePath.getCanonicalPath()))
				{
					return;
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		File srcdir = new File(srcDirName+path);
		if(!srcdir.exists())
		{
			return;
		}
		if(destname.length()>0 && !destname.endsWith(fileseparator))
		{
			destname += fileseparator;
		}
		String dest = destname+path;
		try
		{
			File destdir = new File(dest);
			PathManager.createDirectory(configuration, dest);
			String[] files = srcdir.list();
			for(int i = 0; i<files.length; i++)
			{
				File srcfile = new File(srcdir, files[i]);
				File destfile = new File(destdir, files[i]);
				if(srcfile.isFile())
				{
					if(destfile.exists() && !overwrite)
					{
						configuration.standardmessage.warning("doclet.Copy_Overwrite_warning",
								srcfile.toString(), destdir.toString());
					}
					else
					{
						configuration.standardmessage.notice("doclet.Copying_File_0_To_Dir_1",
								srcfile.toString(), destdir.toString());
						Util.copyFile(destfile, srcfile);
					}
				}
				else if(srcfile.isDirectory())
				{
					if(configuration.copydocfilesubdirs
							&& !configuration.shouldExcludeDocFileDir(srcfile.getName()))
					{
						copyDocFiles(configuration, srcDirName,
								path+fileseparator+srcfile.getName(), overwrite);
					}
				}
			}
		}
		catch(SecurityException exc)
		{
			throw new DocletAbortException();
		}
		catch(IOException exc)
		{
			throw new DocletAbortException();
		}
	}

	/**
	 * Given a PackageDoc, return the source path for that package.
	 * @param pkgname The package to seach the path for.
	 * @return A string representing the path to the given package.
	 */
	protected static String getSourcePath(Configuration configuration, String pkgname)
	{
		try
		{
			String pkgPath = PathManager.getDirectoryPath(pkgname);
			String completePath = new SourcePath(configuration.sourcepath).getDirectory(pkgPath)
					+fileseparator;
			return completePath.substring(0, completePath.indexOf(pkgPath));
		}
		catch(Exception e)
		{
			return "";
		}
	}


	/**
	 * Replace {&#064;docRoot} tag used in options that accept HTML text, such
	 * as -header, -footer and -bottom, and when converting a relative
	 * HREF where commentTagsToString inserts a {&#064;docRoot} where one was
	 * missing.  (Also see DocRootTaglet for {&#064;docRoot} tags in doc
	 * comments.)
	 * <p/>
	 * Replace {&#064;docRoot} tag in htmlstr with the relative path to the
	 * destination directory from the directory where the file is being
	 * written, looping to handle all such tags in htmlstr.
	 * <p/>
	 * For example, for "-d docs" and -header containing {&#064;docRoot}, when
	 * the HTML page for source file p/C1.java is being generated, the
	 * {&#064;docRoot} tag would be inserted into the header as "../",
	 * the relative path from docs/p/ to docs/ (the document root).
	 * <p/>
	 * Note: This doc comment was written with '&amp;#064;' representing '@'
	 * to prevent the inline tag from being interpreted.
	 */
	public String replaceDocRootDir(String htmlstr)
	{
		// Return if no inline tags exist
		int index = htmlstr.indexOf("{@");
		if(index<0)
		{
			return htmlstr;
		}
		String lowerHtml = htmlstr.toLowerCase();
		// Return index of first occurrence of {@docroot}
		// Note: {@docRoot} is not case sensitive when passed in w/command line option
		index = lowerHtml.indexOf("{@docroot}", index);
		if(index<0)
		{
			return htmlstr;
		}
		StringBuffer buf = new StringBuffer();
		int previndex = 0;
		while(true)
		{
			// Search for lowercase version of {@docRoot}
			index = lowerHtml.indexOf("{@docroot}", previndex);
			// If next {@docRoot} tag not found, append rest of htmlstr and exit loop
			if(index<0)
			{
				buf.append(htmlstr.substring(previndex));
				break;
			}
			// If next {@docroot} tag found, append htmlstr up to start of tag
			buf.append(htmlstr.substring(previndex, index));
			previndex = index+10;  // length for {@docroot} string
			// Insert relative path where {@docRoot} was located
			buf.append(relativepathNoSlash);
			// Append slash if next character is not a slash
			if(previndex<htmlstr.length() && htmlstr.charAt(previndex)!='/')
			{
				buf.append(PathManager.urlfileseparator);
			}
		}
		return buf.toString();
	}

	/**
	 * Print Html Hyper Link, with target frame.
	 * @param link String name of the file.
	 * @param where Position in the file
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 * @param bold Whether the label should be bold or not?
	 */
	public void printTargetHyperLink(String link, String where,
			String target, String label,
			boolean bold, String title)
	{
		print(getHyperLink(link, where, label, bold, "", title, target));
	}

	/**
	 * Get Html Hyper Link, with target frame.  This
	 * link will only appear if page is not in a frame.
	 * @param link String name of the file.
	 * @param where Position in the file
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 * @param bold Whether the label should be bold or not?
	 */
	public void printNoFramesTargetHyperLink(String link, String where,
			String target, String label,
			boolean bold)
	{
		script();
		println("  <!--");
		println("  if(window==top) {");
		println("    document.writeln('"+getHyperLink(link, where, label, bold, "", "", target)+"');");
		println("  }");
		println("  //-->");
		scriptEnd();
		println("<NOSCRIPT>");
		println("  "+getHyperLink(link, where, label, bold, "", "", target));
		println("</NOSCRIPT>\n");
	}

	/**
	 * Print Html Hyper Link, with target frame.
	 * @param link String name of the file.
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 * @param bold Whether the label should be bold or not?
	 */
	public void printTargetHyperLink(String link, String target,
			String label, boolean bold)
	{
		printTargetHyperLink(link, "", target, label, bold, "");
	}


	/**
	 * Print bold Html Hyper Link, with target frame. The label will be bold.
	 * @param link String name of the file.
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 */
	public void printBoldTargetHyperLink(String link, String target,
			String label)
	{
		printTargetHyperLink(link, target, label, true);
	}

	/**
	 * Print bold Html Hyper Link, with target frame. The label will be bold
	 * and the link will only show up if the page is not in a frame.
	 * @param link String name of the file.
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 */
	public void printNoFramesBoldTargetHyperLink(String link, String target,
			String label)
	{
		printNoFramesTargetHyperLink(link, "", target, label, true);
	}


	/**
	 * Print Html Hyper Link, with target frame.
	 * @param link String name of the file.
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 */
	public void printTargetHyperLink(String link, String target,
			String label)
	{
		printTargetHyperLink(link, "", target, label, false, "");
	}

	/**
	 * Print Agent link, with target frame.
	 * @param cd The Agent or Capability to which link is.
	 * @param target Name of the target frame.
	 */
	public void printTargetAgentLink(IMCapability cd, String target)
	{
		String title = getText(cd instanceof IMBDIAgent?
			"doclet.Href_Agent_Title": "doclet.Href_Capability_Title" , cd.getPackage());
		printTargetHyperLink(CapabilityWriter.getLocalFilename(cd), "", target,
			cd instanceof IMBDIAgent? Standard.getMemberName(cd): italicsText(Standard.getMemberName(cd)), false, title);
	}

	/**
	 * Print Package link, with target frame.
	 * @param pd The link will be to the "package-summary.html" page for this
	 * package.
	 * @param target Name of the target frame.
	 * @param label Tag for the link.
	 */
	public void printTargetPackageLink(String pd, String target, String label)
	{
		printTargetHyperLink(pathString(pd, "package-summary.html"), target, label);
	}


	/**
	 * Print the html file header using no keywords for the META tag.
	 * @param title String title for the generated html file.
	 */
	public void printHtmlHeader(String title)
	{
		printHtmlHeader(title, null, true);
	}

	/**
	 * Print the html file header. Also print Html page title and stylesheet
	 * default properties.
	 * @param title String window title to go in the &lt;TITLE&gt; tag
	 * @param metakeywords Array of String keywords for META tag.  Each element
	 * of the array is assigned to a separate META tag.
	 * Pass in null for no array.
	 */
	public void printHtmlHeader(String title, String[] metakeywords)
	{
		printHtmlHeader(title, metakeywords, true);
	}

	/**
	 * Print the html file header. Also print Html page title and stylesheet
	 * default properties.
	 * @param title String window title to go in the &lt;TITLE&gt; tag
	 * @param metakeywords Array of String keywords for META tag.  Each element
	 * of the array is assigned to a separate META tag.
	 * Pass in null for no array.
	 * @param includeScript boolean true if printing windowtitle script.
	 * False for files that appear in the left-hand frames.
	 */
	public void printHtmlHeader(String title, String[] metakeywords, boolean includeScript)
	{
		println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 "+
				"Transitional//EN\" "+
				"\"http://www.w3.org/TR/html4/loose.dtd\">");
		println("<!--NewPage-->");
		html();
		head();
		if(!configuration.notimestamp)
		{
			print("<!-- Generated by jadexdoc (build "+Standard.BUILD_VERSION+") on ");
			print(today());
			println(" -->");
		}
		if(configuration.charset.length()>0)
		{
			println("<META http-equiv=\"Content-Type\" content=\"text/html; "
					+"charset="+configuration.charset+"\">");
		}
		if(configuration.windowtitle.length()>0)
		{
			title += " ("+configuration.windowtitle+")";
		}
		title(title);
		println(title);
		titleEnd();
		println("");
		if(metakeywords!=null)
		{
			for(int i = 0; i<metakeywords.length; i++)
			{
				println("<META NAME=\"keywords\" "
						+"CONTENT=\""+metakeywords[i]+"\">");
			}
		}
		println("");
		printStyleSheetProperties();
		println("");
		// Don't print windowtitle script for overview-frame, allagents-frame
		// and package-frame
		if(includeScript)
		{
			printWinTitleScript(title);
		}
		println("");
		headEnd();
		println("");
		body("white", includeScript);
	}

	/**
	 * Print user specified header and the footer.
	 * @param header if true print the user provided header else print the
	 * user provided footer.
	 */
	public void printUserHeaderFooter(boolean header)
	{
		em();
		if(header)
		{
			print(replaceDocRootDir(configuration.header));
		}
		else
		{
			if(configuration.footer.length()!=0)
			{
				print(replaceDocRootDir(configuration.footer));
			}
			else
			{
				print(replaceDocRootDir(configuration.header));
			}
		}
		emEnd();
	}

	/**
	 * Print the user specified bottom.
	 */
	public void printBottom()
	{
		hr();
		print(replaceDocRootDir(configuration.bottom));
	}

	/**
	 * Print the navigation bar for the Html page at the top and and the bottom.
	 * @param header If true print navigation bar at the top of the page else
	 * print the nevigation bar at the bottom.
	 */
	protected void navLinks(boolean header)
	{
		println("");
		if(!configuration.nonavbar)
		{
			if(header)
			{
				println("\n<!-- ========= START OF TOP NAVBAR ======= -->");
				anchor("navbar_top");
				print("\n");
				print(getHyperLink("", "skip-navbar_top", "", false, "",
						getText("doclet.Skip_navigation_links"), ""));
			}
			else
			{
				println("\n<!-- ======= START OF BOTTOM NAVBAR ====== -->");
				anchor("navbar_bottom");
				print("\n");
				print(getHyperLink("", "skip-navbar_bottom", "", false, "",
						getText("doclet.Skip_navigation_links"), ""));
			}
			table(0, "100%", 1, 0);
			tr();
			tdColspanBgcolorStyle(3, "#EEEEFF", "NavBarCell1");
			println("");
			if(header)
			{
				anchor("navbar_top_firstrow");
			}
			else
			{
				anchor("navbar_bottom_firstrow");
			}
			table(0, 0, 3);
			print("  ");
			trAlignVAlign("center", "top");

			if(configuration.createoverview)
			{
				navLinkContents();
			}

			if(configuration.specifiedPackages().length>0)
			{
				navLinkPackage();
			}

			navLinkAgent();

			if(configuration.classuse)
			{
				navLinkAgentUse();
			}
			if(configuration.createtree)
			{
				navLinkTree();
			}
			if(configuration.createindex)
			{
				navLinkIndex();
			}
			if(!configuration.nohelp)
			{
				navLinkHelp();
			}
			print("  ");
			trEnd();
			tableEnd();
			tdEnd();

			tdAlignVAlignRowspan("right", "top", 3);

			printUserHeaderFooter(header);
			tdEnd();
			trEnd();
			println("");

			tr();
			tdBgcolorStyle("white", "NavBarCell2");
			font("-2");
			space();
			navLinkPrevious();
			space();
			println("");
			space();
			navLinkNext();
			fontEnd();
			tdEnd();

			tdBgcolorStyle("white", "NavBarCell2");
			font("-2");
			print("  ");
			navShowLists();
			print("  ");
			space();
			println("");
			space();
			navHideLists();
			print("  ");
			space();
			println("");
			space();
			navLinkClassIndex();
			fontEnd();
			tdEnd();

			trEnd();

			printSummaryDetailLinks();

			tableEnd();
			if(header)
			{
				aName("skip-navbar_top");
				aEnd();
				println("\n<!-- ========= END OF TOP NAVBAR ========= -->");
			}
			else
			{
				aName("skip-navbar_bottom");
				aEnd();
				println("\n<!-- ======== END OF BOTTOM NAVBAR ======= -->");
			}
			println("");
		}
	}

	/**
	 * Do nothing. This is the default method.
	 */
	protected void printSummaryDetailLinks()
	{
	}

	/**
	 * Print link to the "overview-summary.html" page.
	 */
	protected void navLinkContents()
	{
		navCellStart();
		printHyperLink(relativepath+"overview-summary.html", "",
				getText("doclet.Overview"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Description for a cell in the navigation bar.
	 */
	protected void navCellStart()
	{
		print("  ");
		tdBgcolorStyle("#EEEEFF", "NavBarCell1");
		print("    ");
	}

	/**
	 * Description for a cell in the navigation bar, but with reverse
	 * high-light effect.
	 */
	protected void navCellRevStart()
	{
		print("  ");
		tdBgcolorStyle("#FFFFFF", "NavBarCell1Rev");
		print(" ");
		space();
	}

	/**
	 * Closing tag for navigation bar cell.
	 */
	protected void navCellEnd()
	{
		space();
		tdEnd();
	}

	/**
	 * Print the word "Package" in the navigation bar cell, to indicate that
	 * link is not available here.
	 */
	protected void navLinkPackage()
	{
		navCellStart();
		fontStyle("NavBarFont1");
		printText("doclet.Package");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Print the word "Use" in the navigation bar cell, to indicate that link
	 * is not available.
	 */
	protected void navLinkAgentUse()
	{
		navCellStart();
		fontStyle("NavBarFont1");
		printText("doclet.navAgentUse");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Print link for previous file.
	 * @param prev File name for the prev link.
	 */
	public void navLinkPrevious(String prev)
	{
		String tag = getText("doclet.Prev");
		if(prev!=null)
		{
			printHyperLink(prev, "", tag, true);
		}
		else
		{
			print(tag);
		}
	}

	/**
	 * Print the word "PREV" to indicate that no link is available.
	 */
	protected void navLinkPrevious()
	{
		navLinkPrevious(null);
	}

	/**
	 * Print link for next file.
	 * @param next File name for the next link.
	 */
	public void navLinkNext(String next)
	{
		String tag = getText("doclet.Next");
		if(next!=null)
		{
			printHyperLink(next, "", tag, true);
		}
		else
		{
			print(tag);
		}
	}

	/**
	 * Print the word "NEXT" to indicate that no link is available.
	 */
	protected void navLinkNext()
	{
		navLinkNext(null);
	}

	/**
	 * Print "FRAMES" link, to switch to the frame version of the output.
	 * @param link File to be linked, "index.html".
	 */
	protected void navShowLists(String link)
	{
		printBoldTargetHyperLink(link, "_top", getText("doclet.FRAMES"));
	}

	/**
	 * Print "FRAMES" link, to switch to the frame version of the output.
	 */
	protected void navShowLists()
	{
		navShowLists(relativepath+"index.html");
	}

	/**
	 * Print "NO FRAMES" link, to switch to the non-frame version of the output.
	 * @param link File to be linked.
	 */
	protected void navHideLists(String link)
	{
		printBoldTargetHyperLink(link, "_top", getText("doclet.NO_FRAMES"));
	}

	/**
	 * Print "NO FRAMES" link, to switch to the non-frame version of the output.
	 */
	protected void navHideLists()
	{
		navHideLists(filename);
	}

	/**
	 * Print "Tree" link in the navigation bar. If there is only one package
	 * specified on the command line, then the "Tree" link will be to the
	 * only "package-tree.html" file otherwise it will be to the
	 * "overview-tree.html" file.
	 */
	protected void navLinkTree()
	{
		navCellStart();
		String[] packages = configuration.specifiedPackages();
		if(packages.length==1 && configuration.getSpecifiedAgents().length==0)
		{
			printHyperLink(pathString(packages[0], "package-tree.html"), "",
					getText("doclet.Tree"), true, "NavBarFont1");
		}
		else
		{
			printHyperLink(relativepath+"overview-tree.html", "",
					getText("doclet.Tree"), true, "NavBarFont1");
		}
		navCellEnd();
	}

	/**
	 * Print "Tree" link to the "overview-tree.html" file.
	 */
	protected void navLinkMainTree(String label)
	{
		printHyperLink(relativepath+"overview-tree.html", label);
	}

	/**
	 * Print the word "Class" in the navigation bar cell, to indicate that
	 * class link is not available.
	 */
	protected void navLinkAgent()
	{
		navCellStart();
		fontStyle("NavBarFont1");
		printText("doclet.Agent");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Print "Deprecated" API link in the navigation bar.
	 */
	protected void navLinkDeprecated()
	{
		navCellStart();
		printHyperLink(relativepath+"deprecated-list.html", "",
				getText("doclet.navDeprecated"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print link for generated index. If the user has used "-splitindex"
	 * command line option, then link to file "index-files/index-1.html" is
	 * generated otherwise link to file "index-all.html" is generated.
	 */
	protected void navLinkClassIndex()
	{
		printNoFramesBoldTargetHyperLink(relativepath+"allagents-noframe.html",
				"", getText("doclet.All_Agents"));
	}

	/**
	 * Print link for generated class index.
	 */
	protected void navLinkIndex()
	{
		navCellStart();
		printHyperLink(relativepath+
				(configuration.splitindex?
				PathManager.getPath("index-files")+
				fileseparator: "")+
				(configuration.splitindex?
				"index-1.html": "index-all.html"), "",
				getText("doclet.Index"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print help file link. If user has provided a help file, then generate a
	 * link to the user given file, which is already copied to current or
	 * destination directory.
	 */
	protected void navLinkHelp()
	{
		String helpfilenm = configuration.helpfile;
		if(helpfilenm.equals(""))
		{
			helpfilenm = "help-doc.html";
		}
		else
		{
			int lastsep;
			if((lastsep = helpfilenm.lastIndexOf(File.separatorChar))!=-1)
			{
				helpfilenm = helpfilenm.substring(lastsep+1);
			}
		}
		navCellStart();
		printHyperLink(relativepath+helpfilenm, "",
				getText("doclet.Help"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print the word "Detail" in the navigation bar. No link is available.
	 */
	protected void navDetail()
	{
		printText("doclet.Detail");
	}

	/**
	 * Print the word "Summary" in the navigation bar. No link is available.
	 */
	protected void navSummary()
	{
		printText("doclet.Summary");
	}

	/**
	 * Print the Html table tag for the index summary tables. The table tag
	 * printed is
	 * &lt;TABLE BORDER="1" CELLPADDING="3" CELLSPACING="0" WIDTH="100%">
	 */
	public void tableIndexSummary()
	{
		table(1, "100%", 3, 0);
	}

	/**
	 * Same as {@link #tableIndexSummary()}.
	 */
	public void tableIndexDetail()
	{
		table(1, "100%", 3, 0);
	}

	/**
	 * Print Html tag for table elements. The tag printed is
	 * &lt;TD ALIGN="right" VALIGN="top" WIDTH="1%"&gt;.
	 */
	public void tdIndex()
	{
		print("<TD ALIGN=\"right\" VALIGN=\"top\" WIDTH=\"1%\">");
	}

	/**
	 * Prine table header information about color, column span and the font.
	 * @param color Background color.
	 * @param span Column span.
	 */
	public void tableHeaderStart(String color, int span)
	{
		trBgcolorStyle(color, "TableHeadingColor");
		tdColspan(span);
		font("+2");
	}

	/**
	 * Print table header for the inherited members summary tables. Print the
	 * background color information.
	 * @param color Background color.
	 */
	public void tableInheritedHeaderStart(String color)
	{
		trBgcolorStyle(color, "TableSubHeadingColor");
		td();
	}

	/**
	 * Print "Use" table header. Print the background color and the column span.
	 * @param color Background color.
	 */
	public void tableUseInfoHeaderStart(String color)
	{
		trBgcolorStyle(color, "TableSubHeadingColor");
		tdColspan(2);
	}

	/**
	 * Print table header with the background color with default column span 2.
	 * @param color Background color.
	 */
	public void tableHeaderStart(String color)
	{
		tableHeaderStart(color, 2);
	}

	/**
	 * Print table header with the column span, with the default color #CCCCFF.
	 * @param span Column span.
	 */
	public void tableHeaderStart(int span)
	{
		tableHeaderStart("#CCCCFF", span);
	}

	/**
	 * Print table header with default column span 2 and default color #CCCCFF.
	 */
	public void tableHeaderStart()
	{
		tableHeaderStart(2);
	}

	/**
	 * Print table header end tags for font, column and row.
	 */
	public void tableHeaderEnd()
	{
		fontEnd();
		tdEnd();
		trEnd();
	}

	/**
	 * Print table header end tags in inherited tables for column and row.
	 */
	public void tableInheritedHeaderEnd()
	{
		tdEnd();
		trEnd();
	}

	/**
	 * Print the summary table row cell attribute width.
	 * @param width Width of the table cell.
	 */
	public void summaryRow(int width)
	{
		if(width!=0)
		{
			tdWidth(width+"%");
		}
		else
		{
			td();
		}
	}

	/**
	 * Print the summary table row cell end tag.
	 */
	public void summaryRowEnd()
	{
		tdEnd();
	}

	/**
	 * Print the heading in Html &lt;H2> format.
	 * @param str The Header string.
	 */
	public void printIndexHeading(String str)
	{
		h2();
		print(str);
		h2End();
	}

	/**
	 * Print Html tag &lt;FRAMESET=arg&gt;.
	 * @param arg Argument for the tag.
	 */
	public void frameSet(String arg)
	{
		println("<FRAMESET "+arg+">");
	}

	/**
	 * Print Html closing tag &lt;/FRAMESET&gt;.
	 */
	public void frameSetEnd()
	{
		println("</FRAMESET>");
	}

	/**
	 * Print Html tag &lt;FRAME=arg&gt;.
	 * @param arg Argument for the tag.
	 */
	public void frame(String arg)
	{
		println("<FRAME "+arg+">");
	}

	/**
	 * Print Html closing tag &lt;/FRAME&gt;.
	 */
	public void frameEnd()
	{
		println("</FRAME>");
	}

	/**
	 * Return path to the class page for a element. For example, the class
	 * name is "java.lang.Object" and if the current file getting generated is
	 * "java/io/File.html", then the path string to the class, returned is
	 * "../../java/lang/Object.html".
	 * @param cd Class to which the path is requested.
	 */
	protected String pathToClass(Class cd)
	{
		StringBuffer buf = new StringBuffer(relativepath);
		buf.append(PathManager.getPath(cd.getName())+".html");
		return buf.toString();
	}

	/**
	 * Return path to the class page for a element. For example, the class
	 * name is "java.lang.Object" and if the current file getting generated is
	 * "java/io/File.html", then the path string to the class, returned is
	 * "../../java/lang.Object.html".
	 * @param cd Class to which the path is requested.
	 */
	protected String pathToAgent(IMCapability cd)
	{
		return pathString(cd, CapabilityWriter.getLocalFilename(cd));
	}

	/**
	 * Return path to the given file name in the given package name. So if the name
	 * passed is "Object.html" and the name of the package is "java.lang", and
	 * if the relative path is "../.." then returned string will be
	 * "../../java/lang/Object.html"
	 * @param pkgname Name of package in which the file name is assumed to be.
	 * @param name File name, to which path string is.
	 */
	protected String pathString(String pkgname, String name)
	{
		StringBuffer buf = new StringBuffer(relativepath);
		String pathstr = PathManager.getPath(pkgname);
		if(pathstr.length()>0)
		{
			buf.append(pathstr);
			buf.append("/");
		}
		buf.append(name);
		return buf.toString();
	}

	/**
	 * Return the path to the class page for a element. Works same as
	 * {@link #pathToClass(Class)}.
	 * @param cd Class to which the path is requested.
	 * @param name Name of the file(doesn't include path).
	 */
	protected String pathString(Class cd, String name)
	{
		return pathString(cd.getPackage(), name);
	}

	/**
	 * Return path to the given file name in the given package. So if the name
	 * passed is "Object.html" and the name of the package is "java.lang", and
	 * if the relative path is "../.." then returned string will be
	 * "../../java/lang/Object.html"
	 * @param pd Package in which the file name is assumed to be.
	 * @param name File name, to which path string is.
	 */
	protected String pathString(Package pd, String name)
	{
		StringBuffer buf = new StringBuffer(relativepath);
		buf.append(PathManager.getPathToPackage(pd, name));
		return buf.toString();
	}

	/**
	 * Return path to the given file name in the given package. So if the name
	 * passed is "Object.html" and the name of the package is "java.lang", and
	 * if the relative path is "../.." then returned string will be
	 * "../../java/lang/Object.html"
	 * @param pd Package in which the file name is assumed to be.
	 * @param name File name, to which path string is.
	 */
	protected String pathString(IMElement pd, String name)
	{
		StringBuffer buf = new StringBuffer(relativepath);
		buf.append(PathManager.getPathToAgent((IMCapability)pd, name));

		return buf.toString();
	}

	/**
	 * Print link to the "pacakge-summary.html" file, depending upon the
	 * package name.
	 */
	public void printPackageLink(String pkg)
	{
		print(getPackageLink(pkg));
	}

	/**
	 *
	 * @param pkg
	 * @param bold
	 */
	public void printPackageLink(String pkg, boolean bold)
	{
		print(getPackageLink(pkg, bold));
	}

	/**
	 *
	 * @param pkg
	 * @param linklabel
	 */
	public void printPackageLink(String pkg, String linklabel)
	{
		print(getPackageLink(pkg, linklabel, false));
	}

	/**
	 * Get link for individual package file.
	 */
	public String getPackageLink(String pkg)
	{
		return getPackageLink(pkg, pkg, false);
	}

	/**
	 *
	 * @param pkg
	 * @param bold
	 * @return
	 */
	public String getPackageLink(String pkg, boolean bold)
	{
		return getPackageLink(pkg, pkg, bold);
	}

	/**
	 *
	 * @param pkg
	 * @param label
	 * @return
	 */
	public String getPackageLink(String pkg, String label)
	{
		return getPackageLink(pkg, label, false);
	}

	/**
	 *
	 * @param pkg
	 * @param linklabel
	 * @param bold
	 * @return
	 */
	public String getPackageLink(String pkg, String linklabel,
			boolean bold)
	{
		boolean included = false;
		String[] packages = configuration.specifiedPackages();
		for(int i = 0; i<packages.length; i++)
		{
			if(packages[i].equals(pkg))
			{
				included = true;
				break;
			}
		}

		if(included)
		{
			return getHyperLink(pathString(pkg, "package-summary.html"),
					"", linklabel, bold);
		}
		else
		{
			String crossPkgLink = getCrossPackageLink(pkg);
			if(crossPkgLink!=null)
			{
				return getHyperLink(crossPkgLink, "", linklabel, bold);
			}
			else
			{
				return linklabel;
			}
		}
	}

	/**
	 *
	 * @param comment
	 */
	public void printSummaryComment(Comment comment)
	{
		if(comment!=null)
		{
			String text = comment.getFirstSentence();
			if(text!=null && text.length()>0)
			{
				print(text);
			}
			else
			{
				space();
			}
		}
	}

	/**
	 *
	 * @param element
	 */
	public void printIndexComment(IMElement element)
	{
		Comment comment = new Comment(element);
		String text = comment.getFirstSentence();
		if(text!=null && text.length()>0)
		{
			print(text);
		}
	}

	/**
	 *
	 * @param comment
	 */
	public void printInlineComment(Comment comment)
	{
		String text = comment.getCommentText();
		if(text!=null && text.length()>0)
		{
			print(text);
			p();
		}
	}

	/**
	 *
	 * @param el
	 */
	public void printInlineComment(IMElement el)
	{
		if(el.getDescription()!=null)
		{
			print(el.getDescription());
			p();
		}
	}

	/**
	 *
	 * @param cd
	 */
	public void printClassLink(Class cd)
	{
		print(getClassLink(cd, false));
	}

	/**
	 *
	 * @param cd
	 */
	public void printAgentLink(IMCapability cd)
	{
		print(getAgentLink(cd, false));
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public String getClassLink(Class cd)
	{
		return getClassLink(cd, false);
	}

	/**
	 *
	 * @param cd
	 * @param label
	 */
	public void printClassLink(Class cd, String label)
	{
		print(getClassLink(cd, "", label, false));
	}

	/**
	 *
	 * @param cd
	 * @param label
	 * @return
	 */
	public String getClassLink(Class cd, String label)
	{
		return getClassLink(cd, "", label, false);
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 */
	public void printClassLink(Class cd, String where, String label)
	{
		print(getClassLink(cd, where, label, false));
	}

	/**
	 *
	 * @param cd
	 * @param label
	 * @param bold
	 */
	public void printClassLink(Class cd, String label, boolean bold)
	{
		print(getClassLink(cd, "", label, bold));
	}

	/**
	 *
	 * @param cd
	 * @param bold
	 */
	public void printAgentLink(IMCapability cd, boolean bold)
	{
		print(getAgentLink(cd, bold));
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 */
	public void printAgentLink(IMCapability cd, String where, String label)
	{
		print(getAgentLink(cd, where, label, false));
	}

	/**
	 *
	 * @param cd
	 * @param label
	 * @param bold
	 */
	public void printAgentLink(IMCapability cd, String label, boolean bold)
	{
		print(getAgentLink(cd, "", label, bold));
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @param bold
	 */
	public void printAgentLink(IMCapability cd, String where,
			String label, boolean bold)
	{
		print(getAgentLink(cd, where, label, bold));
	}

	/**
	 *
	 * @param cd
	 * @param el
	 * @param bold
	 */
	public void printMemberLink(IMCapability cd, IMElement el, boolean bold)
	{
		if(el!=null && cd!=null)
		{
			String where = getMemberAnchor(el);
			String label = Standard.getMemberName(el);
			print(getAgentLink(cd, where, label, bold));
		}
	}


	/**
	 *
	 * @param cd
	 * @param el
	 * @param bold
	 * @return
	 */
	public String getMemberLink(IMCapability cd, IMElement el, boolean bold)
	{
		String where = getMemberAnchor(el);
		String label = Standard.getMemberName(el);
		return getAgentLink(cd, where, label, bold);
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @param bold
	 * @param color
	 */
	public void printClassLink(Class cd, String where, String label,
			boolean bold, String color)
	{
		print(getClassLink(cd, where, label, bold, color, ""));
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @return
	 */
	public String getClassLink(Class cd, String where, String label)
	{
		return getClassLink(cd, where, label, false);
	}

	/**
	 *
	 * @param cd
	 * @param bold
	 */
	public void printClassLink(Class cd, boolean bold)
	{
		print(getClassLink(cd, bold));
	}

	/**
	 *
	 * @param cd
	 * @param bold
	 * @return
	 */
	public String getClassLink(Class cd, boolean bold)
	{
		return getClassLink(cd, "", "", bold);
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @return
	 */
	public String getAgentLink(IMCapability cd, String where, String label)
	{
		return getAgentLink(cd, where, label, false);
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public String getAgentLink(IMCapability cd)
	{
		return getAgentLink(cd, false);
	}

	/**
	 *
	 * @param cd
	 * @param label
	 * @return
	 */
	public String getAgentLink(IMCapability cd, String label)
	{
		return getAgentLink(cd, "", label, false);
	}

	/**
	 *
	 * @param cd
	 * @param bold
	 * @return
	 */
	public String getAgentLink(IMCapability cd, boolean bold)
	{
		return getAgentLink(cd, "", "", bold);
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @param bold
	 */
	public void printClassLink(Class cd, String where,
			String label, boolean bold)
	{
		print(getClassLink(cd, where, label, bold));
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @param bold
	 * @param color
	 * @param target
	 * @return
	 */
	public String getClassLink(Class cd, String where, String label, boolean bold, String color, String target)
	{
		boolean nameUnspecified = label.length()==0;
		if(nameUnspecified)
		{
			label = cd.getName();
			if(label.lastIndexOf('.')!=-1)
			{
				label = label.substring(label.lastIndexOf('.')+1, label.length());
			}
		}
		displayLength += label.length();

		//Create a tool tip if we are linking to a class or interface.  Don't
		//create one if we are linking to a member.
		if(!cd.isPrimitive())
		{
			String title = where==null || where.length()==0? (
					getText(cd.isInterface()?
					"doclet.Href_Interface_Title":
					"doclet.Href_Class_Title", cd.getPackage().getName())):
					"";

			if(isIncluded(cd))
			{
				String filename = pathToClass(cd);
				return getHyperLink(filename, where, label, bold, color, title, target);
			}
			else
			{
				String crosslink = getCrossClassLink(cd, where, label, bold, color, true);
				if(crosslink!=null)
				{
					return crosslink;
				}
			}
		}
		if(nameUnspecified)
		{
			displayLength -= label.length();
			label = configuration.getClassName(cd);
			displayLength += label.length();
		}
		return label;
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @param bold
	 * @return
	 */
	public String getClassLink(Class cd, String where,
			String label, boolean bold)
	{
		return getClassLink(cd, where, label, bold, "", "");
	}

	/**
	 *
	 * @param cd
	 * @param where
	 * @param label
	 * @param bold
	 * @return
	 */
	public String getAgentLink(IMCapability cd, String where,
			String label, boolean bold)
	{
		return getAgentLink(cd, where, label, bold, "", "");
	}

	/**
	 *
	 * @param agent
	 * @param where
	 * @param label
	 * @param bold
	 * @param color
	 * @param target
	 * @return
	 */
	public String getAgentLink(IMCapability agent, String where,
			String label, boolean bold, String color,
			String target)
	{
		boolean nameUnspecified = label.length()==0;
		if(nameUnspecified)
		{
			label = Standard.getMemberName(agent);
		}
		displayLength += label.length();

		//Create a tool tip if we are linking to a class or interface.  Don't
		//create one if we are linking to a member.
		// todo: remove Hack
		String title = where==null || where.length()==0? (
				getText(agent instanceof IMBDIAgent?
				"doclet.Href_Agent_Title":
				"doclet.Href_Capability_Title", agent.getPackage())):
				"";

		if(isIncluded(agent))
		{
			String filename = pathToAgent(agent);
			return getHyperLink(filename, where, label, bold, color, title, target);

		}
//        else {
//            String crosslink = getCrossAgentLink(agent, where,label, bold, color, true);
//            if (crosslink != null) {
//                return crosslink;
//            }
//        }
		if(nameUnspecified)
		{
			displayLength -= label.length();
			label = configuration.getAgentName(agent);
//            label = agent.getPackage() + "." + agent.getName();
			displayLength += label.length();
		}
		return label;
	}


	/**
	 * **********************************************************
	 * Return a class cross link to external class documentation.
	 * The name must be fully qualified to determine which package
	 * the class is in.  The -link option does not allow users to
	 * link to external classes in the "default" package.
	 * @param cd the external class.
	 * @param refMemName the name of the member being referenced.  This should
	 * be null or empty string if no member is being referenced.
	 * @param label the label for the external link.
	 * @param bold true if the link should be bold.
	 * @param style the style of the link.
	 * @param code true if the label should be code font.
	 */
	public String getCrossClassLink(Class cd, String refMemName,
			String label, boolean bold, String style,
			boolean code)
	{

		String className = cd.getName();
		String packageName = (cd.getPackage()!=null)? cd.getPackage().getName(): "";
		className = (packageName.length()>0)? className.substring(packageName.length()+1): className;
		String defaultLabel = code? getCode()+className+getCodeEnd(): className;

		if(getCrossPackageLink(packageName)!=null)
		{
			//The package exists in external documentation, so link to the external
			//class (assuming that it exists).  This is definitely a limitation of
			//the -link option.  There are ways to determine if an external package
			//exists, but no way to determine if the external class exists.  We just
			//have to assume that it does.
			String link = configuration.extern.getExternalLink(packageName, relativepath, className+".html");
			String where = refMemName==null? "": refMemName;
			label = (label==null || label.length()==0)? defaultLabel: label;
			String title = getText("doclet.Href_Class_Or_Interface_Title", packageName);
			String hyperlink = getHyperLink(link, where, label, bold, style, title, "");
			return hyperlink;
		}
		
		return null;
	}

	/**
	 *
	 * @param cd
	 * @param refMemName
	 * @param label
	 * @param bold
	 * @param style
	 * @param code
	 * @return
	 */
	public String getCrossAgentLink(IMCapability cd, String refMemName,
		String label, boolean bold, String style, boolean code)
	{

		String className = Standard.getMemberName(cd);
		String packageName = (cd.getPackage()!=null)? cd.getPackage(): "";
		className = packageName.substring(packageName.length()-1);
		String defaultLabel = code? getCode()+className+getCodeEnd(): className;

		if(getCrossPackageLink(packageName)!=null)
		{
			//The package exists in external documentation, so link to the external
			//class (assuming that it exists).  This is definitely a limitation of
			//the -link option.  There are ways to determine if an external package
			//exists, but no way to determine if the external class exists.  We just
			//have to assume that it does.
			String link = configuration.extern.getExternalLink(packageName, relativepath, className+".html");
			String where = refMemName==null? "": refMemName;
			label = (label==null || label.length()==0)? defaultLabel: label;
			String title = getText("doclet.Href_Class_Or_Interface_Title", packageName);
			String hyperlink = getHyperLink(link, where, label, bold, style, title, "");
			return hyperlink;
		}

		return null;
	}

	/**
	 *
	 * @param pkgName
	 * @return
	 */
	public String getCrossPackageLink(String pkgName)
	{
		return configuration.extern.getExternalLink(pkgName, relativepath,
				"package-summary.html");
	}

	/**
	 *
	 * @param cd
	 */
	public void printQualifiedClassLink(Class cd)
	{
		printClassLink(cd, "", cd.getName());
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public String getQualifiedClassLink(Class cd)
	{
		return getClassLink(cd, "", cd.getName());
	}

	/**
	 *
	 * @param cd
	 */
	public void printQualifiedAgentLink(IMCapability cd)
	{
		printAgentLink(cd, "", cd.getPackage()==null? Standard.getMemberName(cd): cd.getPackage()+"."+Standard.getMemberName(cd));
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public String getQualifiedAgentLink(IMCapability cd)
	{
		return getAgentLink(cd, "", cd.getPackage()==null? Standard.getMemberName(cd): cd.getPackage()+"."+Standard.getMemberName(cd));
	}


	/**
	 * Print Class link, with only class name as the link and prefixing
	 * plain package name.
	 */
	public void printPreQualifiedClassLink(Class cd)
	{
		print(getPreQualifiedClassLink(cd, false));
	}

	/**
	 *
	 * @param cd
	 */
	public void printPreQualifiedBoldClassLink(Class cd)
	{
		print(getPreQualifiedClassLink(cd, true));
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public String getPreQualifiedClassLink(Class cd)
	{
		return getPreQualifiedClassLink(cd, false);
	}

	/**
	 *
	 * @param cd
	 * @param bold
	 * @return
	 */
	public String getPreQualifiedClassLink(Class cd, boolean bold)
	{
		String classlink = "";
		Package pd = cd.getPackage();
		if(pd!=null)
		{
			classlink = pd.getName();
		}
		classlink += getClassLink(cd, "", cd.getName(), bold);
		return classlink;
	}

	/**
	 * Print Class link, with only class name as the link and prefixing
	 * plain package name.
	 */
	public void printPreQualifiedAgentLink(IMCapability cd)
	{
		print(getPreQualifiedAgentLink(cd, false));
	}

	/**
	 *
	 * @param cd
	 */
	public void printPreQualifiedBoldAgentLink(IMCapability cd)
	{
		print(getPreQualifiedAgentLink(cd, true));
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public String getPreQualifiedAgentLink(IMCapability cd)
	{
		return getPreQualifiedAgentLink(cd, false);
	}

	/**
	 *
	 * @param cd
	 * @param bold
	 * @return
	 */
	public String getPreQualifiedAgentLink(IMCapability cd, boolean bold)
	{
		String classlink = "";
		String pkgName = cd.getPackage()==null? "": cd.getPackage();
		if(!configuration.shouldExcludeQualifier(pkgName))
		{
			classlink = (pkgName.length()>0)? pkgName+".": "";
		}
		classlink += getAgentLink(cd, "", Standard.getMemberName(cd), bold);
		return classlink;
	}

	/**
	 *
	 * @param key
	 */
	public void printText(String key)
	{
		print(getText(key));
	}

	/**
	 *
	 * @param key
	 * @param a1
	 */
	public void printText(String key, String a1)
	{
		print(getText(key, a1));
	}

	/**
	 *
	 * @param key
	 * @param a1
	 * @param a2
	 */
	public void printText(String key, String a1, String a2)
	{
		print(getText(key, a1, a2));
	}

	/**
	 *
	 * @param key
	 */
	public void boldText(String key)
	{
		bold(getText(key));
	}

	/**
	 *
	 * @param key
	 * @param a1
	 */
	public void boldText(String key, String a1)
	{
		bold(getText(key, a1));
	}

	/**
	 *
	 * @param key
	 * @param a1
	 * @param a2
	 */
	public void boldText(String key, String a1, String a2)
	{
		bold(getText(key, a1, a2));
	}

	/**
	 *
	 * @param key
	 * @return
	 */
	public String getText(String key)
	{
		return msg(false).getText(key);
	}

	/**
	 *
	 * @param key
	 * @param a1
	 * @return
	 */
	public String getText(String key, String a1)
	{
		return msg(false).getText(key, a1);
	}

	/**
	 *
	 * @param key
	 * @param a1
	 * @param a2
	 * @return
	 */
	public String getText(String key, String a1, String a2)
	{
		return msg(false).getText(key, a1, a2);
	}

	/**
	 *
	 * @param key
	 * @param a1
	 * @param a2
	 * @param a3
	 * @return
	 */
	public String getText(String key, String a1, String a2, String a3)
	{
		return msg(false).getText(key, a1,
				a2, a3);
	}

	/**
	 * Return true if this class is included in the active set.
	 * A ClassDoc is included iff either it is specified on the
	 * commandline, or if it's containing package is specified
	 * on the command line, or if it is a member class of an
	 * included class.
	 */
	public boolean isIncluded(Class cd)
	{
		return false;
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public boolean isIncluded(IMCapability cd)
	{
		return configuration.isIncluded(cd);
//        return true;
		//        return cd == configuration.currentcd;
	}

	/**
	 *
	 * @param text
	 * @return
	 */
	public static String removeNonInlineHtmlTags(String text)
	{
		if(text.indexOf('<')<0)
		{
			return text;
		}
		String noninlinetags[] = {"<ul>", "</ul>", "<ol>", "</ol>",
								  "<dl>", "</dl>", "<table>", "</table>",
								  "<tr>", "</tr>", "<td>", "</td>",
								  "<th>", "</th>", "<p>", "</p>",
								  "<li>", "</li>", "<dd>", "</dd>",
								  "<dir>", "</dir>", "<dt>", "</dt>",
								  "<h1>", "</h1>", "<h2>", "</h2>",
								  "<h3>", "</h3>", "<h4>", "</h4>",
								  "<h5>", "</h5>", "<h6>", "</h6>",
								  "<pre>", "</pre>", "<menu>", "</menu>",
								  "<listing>", "</listing>", "<hr>",
								  "<blockquote>", "</blockquote>",
								  "<center>", "</center>",
								  "<UL>", "</UL>", "<OL>", "</OL>",
								  "<DL>", "</DL>", "<TABLE>", "</TABLE>",
								  "<TR>", "</TR>", "<TD>", "</TD>",
								  "<TH>", "</TH>", "<P>", "</P>",
								  "<LI>", "</LI>", "<DD>", "</DD>",
								  "<DIR>", "</DIR>", "<DT>", "</DT>",
								  "<H1>", "</H1>", "<H2>", "</H2>",
								  "<H3>", "</H3>", "<H4>", "</H4>",
								  "<H5>", "</H5>", "<H6>", "</H6>",
								  "<PRE>", "</PRE>", "<MENU>", "</MENU>",
								  "<LISTING>", "</LISTING>", "<HR>",
								  "<BLOCKQUOTE>", "</BLOCKQUOTE>",
								  "<CENTER>", "</CENTER>"
		};
		for(int i = 0; i<noninlinetags.length; i++)
		{
			text = replace(text, noninlinetags[i], "");
		}
		return text;
	}

	/**
	 *
	 * @param text
	 * @param tobe
	 * @param by
	 * @return
	 */
	public static String replace(String text, String tobe, String by)
	{
		while(true)
		{
			int startindex = text.indexOf(tobe);
			if(startindex<0)
			{
				return text;
			}
			int endindex = startindex+tobe.length();
			StringBuffer replaced = new StringBuffer();
			if(startindex>0)
			{
				replaced.append(text.substring(0, startindex));
			}
			replaced.append(by);
			if(text.length()>endindex)
			{
				replaced.append(text.substring(endindex));
			}
			text = replaced.toString();
		}
	}

	/**
	 *
	 */
	public void printStyleSheetProperties()
	{
		String filename = configuration.stylesheetfile;
		if(filename.length()>0)
		{
			File stylefile = new File(filename);
			String parent = stylefile.getParent();
			filename = (parent==null)?
					filename:
					filename.substring(parent.length()+1);
		}
		else
		{
			filename = "stylesheet.css";
		}
		filename = relativepath+filename;
		link("REL =\"stylesheet\" TYPE=\"text/css\" HREF=\""+
				filename+"\" "+"TITLE=\"Style\"");
	}

	/**
	 *
	 * @return
	 */
	public StandardConfiguration configuration()
	{
		return configuration;
	}

	/**
	 * If checkVersion is true, print the version number before return
	 * the MessageRetriever.
	 */
	public MessageRetriever msg(boolean checkVersion)
	{
		if(checkVersion && !configuration.printedVersion)
		{
			configuration.standardmessage.notice("stddoclet.version", Standard.BUILD_VERSION);
			configuration.printedVersion = true;
		}
		return configuration.standardmessage;
	}

	/**
	 *
	 * @param member
	 * @return
	 */
	public String getMemberAnchor(IMElement member)
	{
		String prefix = null;
		if(member.getOwner() instanceof IMBeliefbase)
		{
			prefix = "(belief)";
		}
		else if(member.getOwner() instanceof IMGoalbase)
		{
			prefix = "(goal)";
		}
		else if(member.getOwner() instanceof IMPlanbase)
		{
			prefix = "(plan)";
		}
		else if(member.getOwner() instanceof IMEventbase)
		{
			prefix = "(event)";
		}
		else if(member.getOwner() instanceof IMExpressionbase)
		{
			prefix = "(expression)";
		}
		prefix += Standard.getMemberName(member);
		return prefix;
	}

	/*public static boolean isBDIAgent(IMElement element)
	{
		return element instanceof IMBDIAgent;
	}

	public static boolean isCapability(IMElement element)
	{
		return !isBDIAgent(element);
	}*/

	/**
	 * Utility for subclasses which read HTML documentation files.
	 */
	public static String readHTMLDocumentation(InputStream input, String filename,
		Configuration configuration) throws IOException
	{
		int filesize = input.available();
		byte[] filecontents = new byte[filesize];
		input.read(filecontents, 0, filesize);
		input.close();
		String rawDoc = new String(filecontents);
		String upper = null;
		int bodyIdx = rawDoc.indexOf("<body");
		if(bodyIdx==-1)
		{
			bodyIdx = rawDoc.indexOf("<BODY");
			if(bodyIdx==-1)
			{
				upper = rawDoc.toUpperCase();
				bodyIdx = upper.indexOf("<BODY");
				if(bodyIdx==-1)
				{
					configuration.message.error("javadoc.Body_missing_from_html_file", filename);
					return "";
				}
			}
		}
		bodyIdx = rawDoc.indexOf('>', bodyIdx);
		if(bodyIdx==-1)
		{
			configuration.message.error("javadoc.Body_missing_from_html_file", filename);
			return "";
		}
		++bodyIdx;
		int endIdx = rawDoc.indexOf("</body", bodyIdx);
		if(endIdx==-1)
		{
			endIdx = rawDoc.indexOf("</BODY", bodyIdx);
			if(endIdx==-1)
			{
				if(upper==null)
				{
					upper = rawDoc.toUpperCase();
				}
				endIdx = upper.indexOf("</BODY", bodyIdx);
				if(endIdx==-1)
				{
					configuration.message.error("javadoc.End_body_missing_from_html_file", filename);
					return "";
				}
			}
		}
		return rawDoc.substring(bodyIdx, endIdx);
	}

	/**
	 * Do lazy initialization of "documentation" string.
	 */
	public static Comment comment(String filename, Configuration configuration)
	{
		String ret = null;
		File	file	= new File(filename);
		
		if(!file.exists() && configuration.sourcepath!=null)
		{
			StringTokenizer	stok	= new StringTokenizer(configuration.sourcepath, File.pathSeparator);
			while(!file.exists() && stok.hasMoreTokens())
			{
				file	= new File(stok.nextToken(), filename);
			}
		}

		if(file.exists())
		{
			try
			{
				ret = readHTMLDocumentation(new FileInputStream(file), filename, configuration);
			}
			catch(IOException exc)
			{
				configuration.message.error("javadoc.File_Read_Error", filename);
			}
		}

		return ret!=null? new Comment(ret): null;
	}
}


