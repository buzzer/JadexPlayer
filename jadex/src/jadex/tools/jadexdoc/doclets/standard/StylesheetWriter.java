package jadex.tools.jadexdoc.doclets.standard;

import java.io.*;
import jadex.tools.jadexdoc.Configuration;
import jadex.tools.jadexdoc.doclets.DocletAbortException;

/**
 *
 */
public class StylesheetWriter extends HtmlStandardWriter
{
	/** The stylesheet name. */
	private static final String CSS_FILE_NAME = "stylesheet.css";

	/**
	 * Constructor.
	 */
	public StylesheetWriter(StandardConfiguration configuration, String filename) throws IOException
	{
		super(configuration, filename);
	}

	/**
	 * Generate the style file contents.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration)
	{
		StylesheetWriter stylegen;
		try
		{
			stylegen = new StylesheetWriter(configuration, CSS_FILE_NAME);
			stylegen.generateStyleFile();
			stylegen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered", exc.toString(), CSS_FILE_NAME);
			throw new DocletAbortException();
		}
	}

	/**
	 * Generate the style file contents.
	 */
	protected void generateStyleFile()
	{

		String filename = "resources/"+CSS_FILE_NAME;
		InputStream input = Configuration.class.getResourceAsStream(filename);
		if(input!=null)
		{
			try
			{
				int filesize = input.available();
				byte[] filecontents = new byte[filesize];
				input.read(filecontents, 0, filesize);
				input.close();
				print(new String(filecontents));
			}
			catch(IOException exc)
			{
				configuration.message.error("javadoc.File_Read_Error", filename);
			}

		}
	}

}



