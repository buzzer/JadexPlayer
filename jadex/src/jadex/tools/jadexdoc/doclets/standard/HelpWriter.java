package jadex.tools.jadexdoc.doclets.standard;


import java.io.*;
import jadex.tools.jadexdoc.Configuration;
import jadex.tools.jadexdoc.doclets.DocletAbortException;

/**
 * Generate the Help File for the generated API documentation. The help file
 * contents are helpful for browsing the generated documentation.
 * @author Atul M Dambalkar
 */
public class HelpWriter extends HtmlStandardWriter
{

	private static final String HELP_FILE_NAME = "help-doc.html";

	/**
	 * Constructor to construct HelpWriter object.
	 * @param filename File to be generated.
	 */
	public HelpWriter(StandardConfiguration configuration,
			String filename) throws IOException
	{
		super(configuration, filename);
	}

	/**
	 * Construct the HelpWriter object and then use it to generate the help
	 * file. The name of the generated file is "help-doc.html". The help file
	 * will get generated if and only if "-helpfile" and "-nohelp" is not used
	 * on the command line.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration)
	{
		HelpWriter helpgen;
		try
		{
			helpgen = new HelpWriter(configuration, HELP_FILE_NAME);
			helpgen.generateHelpFile();
			helpgen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), HELP_FILE_NAME);
			throw new DocletAbortException();
		}
	}

	/**
	 * Generate the help file contents.
	 */
	protected void generateHelpFile()
	{
		printHtmlHeader(getText("doclet.Window_Help_title"));
		navLinks(true);
		hr();

		printHelpFileContents();

		hr();
		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * Print the help file contents from the resource file. While generating the
	 * help file contents it also keeps track of user options. If "-notree"
	 * is used, then the "overview-tree.html" will not get generated and hence
	 * help information also will not get generated.
	 */
	protected void printHelpFileContents()
	{

		String filename = "resources/"+HELP_FILE_NAME;
		InputStream in = Configuration.class.getResourceAsStream(filename);
		if(in!=null)
		{
			try
			{
				print(readHTMLDocumentation(in, filename, configuration));
			}
			catch(IOException exc)
			{
				configuration.message.error("javadoc.File_Read_Error", filename);
			}

		}
	}

	/**
	 * Highlight the word "Help" in the navigation bar as this is the help file.
	 */
	protected void navLinkHelp()
	{
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Help");
		fontEnd();
		navCellEnd();
	}
}



