package jadex.tools.jadexdoc;

import java.io.*;
import java.util.*;


/**
 * Various utility methods for processing jadexdoc command line arguments.
 */
public class CommandLine
{

	public CommandLine()
	{
		super();
	}

	/**
	 * Process Win32-style command files for the specified command line
	 * arguments and return the resulting arguments. A command file argument
	 * is of the form '@file' where 'file' is the name of the file whose
	 * contents are to be parsed for additional arguments. The contents of
	 * the command file are parsed using StreamTokenizer and the original
	 * '@file' argument replaced with the resulting tokens. Recursive command
	 * files are not supported. The '@' character itself can be quoted with
	 * the sequence '@@'.
	 */
	public static String[] parse(String[] args) throws IOException
	{
		List newArgs = new ArrayList();
		for(int i = 0; i<args.length; i++)
		{
			String arg = args[i];
			if(arg.length()>1 && arg.charAt(0)=='@')
			{
				arg = arg.substring(1);
				if(arg.charAt(0)=='@')
				{
					newArgs.add(arg);
				}
				else
				{
					loadCmdFile(arg, newArgs);
				}
			}
			else
			{
				newArgs.add(arg);
			}
		}
		return (String[])newArgs.toArray(new String[newArgs.size()]);
	}

	/**
	 * 
	 * @param name
	 * @param args
	 * @throws IOException
	 */
	private static void loadCmdFile(String name, List args) throws IOException
	{
		Reader r = new BufferedReader(new FileReader(name));
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();
		st.wordChars(' ', 255);
		st.whitespaceChars(0, ' ');
		st.commentChar('#');
		st.quoteChar('\"');
		st.quoteChar('\'');
		while(st.nextToken()!=st.TT_EOF)
		{
			args.add(st.sval);
		}
		r.close();
	}
}
