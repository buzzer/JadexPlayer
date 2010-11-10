package jadex.tools.jadexdoc.doclets;


import java.io.*;
import jadex.tools.jadexdoc.*;

/**
 *
 */
public class Util
{
	/**
	 * Copy a file in the resources directory to the destination
	 * directory (if it is not there already).  If
	 * <code>overwrite</code> is true and the destination file
	 * already exists, overwrite it.
	 * @param configuration Holds the destination directory and error message
	 * @param resourcefile The name of the resource file to copy
	 * @param overwrite A flag to indicate whether the file in the
	 * destination directory will be overwritten if
	 * it already exists.
	 */
	public static void copyResourceFile(Configuration configuration,
			String resourcefile, boolean overwrite)
	{
		String destdir = configuration.destdirname;
		String destresourcesdir = destdir+"resources";
		PathManager.createDirectory(configuration, destresourcesdir);
		File destfile = new File(destresourcesdir, resourcefile);
		if(destfile.exists() && (!overwrite))
			return;
		try
		{

			InputStream in = Configuration.class.getResourceAsStream("resources/"+resourcefile);

			if(in==null)
				return;

			OutputStream out = new FileOutputStream(destfile);
			byte[] buf = new byte[2048];
			int n;
			while((n = in.read(buf))>0)
				out.write(buf, 0, n);

			in.close();
			out.close();
		}
		catch(Throwable t)
		{
		}
	}

	/**
	 * Copy source file to destination file.
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void copyFile(File destfile, File srcfile) throws IOException
	{
		byte[] bytearr = new byte[512];
		int len = 0;
		FileInputStream input = new FileInputStream(srcfile);
		FileOutputStream output = new FileOutputStream(destfile);
		try
		{
			while((len = input.read(bytearr))!=-1)
			{
				output.write(bytearr, 0, len);
			}
		}
		catch(FileNotFoundException exc)
		{
		}
		catch(SecurityException exc)
		{
		}
		finally
		{
			input.close();
			output.close();
		}
	}


}

