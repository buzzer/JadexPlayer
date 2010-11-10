package jadex.tools.jadexdoc.doclets.standard;


import java.io.IOException;
import java.util.Arrays;
import jadex.tools.jadexdoc.doclets.DocletAbortException;

/**
 * Write out the package index.
 */
public class PackageListWriter extends HtmlStandardWriter
{

	/**
	 * Create a new package list writer.
	 */
	public PackageListWriter(StandardConfiguration configuration, String filename) throws IOException
	{
		super(configuration, filename);
	}

	/**
	 * Generate the package index.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration)
	{
		PackageListWriter packgen;
		String filename = "package-list";
		try
		{
			packgen = new PackageListWriter(configuration, filename);
			packgen.generatePackageListFile();
			packgen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	protected void generatePackageListFile()
	{
		String[] packages = configuration.specifiedPackages();
		String[] names = new String[packages.length];
		for(int i = 0; i<packages.length; i++)
		{
			names[i] = packages[i];
		}
		Arrays.sort(names);
		for(int i = 0; i<packages.length; i++)
		{
			println(names[i]);
		}
	}
}



