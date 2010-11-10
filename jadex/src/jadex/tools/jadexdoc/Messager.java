package jadex.tools.jadexdoc;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;


/**
 * Utility for integrating with jadexdoc tools.
 * Handle Resources. Access to error and warning counts.
 * Message formatting.
 */
public class Messager
{
	private static Messager instance;

	public PrintWriter errWriter;
	public PrintWriter warnWriter;
	public PrintWriter noticeWriter;

	/** The default writer for diagnostics. */
	//static final PrintWriter defaultErrWriter = new PrintWriter(System.err);
	//static final PrintWriter defaultWarnWriter = new PrintWriter(System.err);
	//static final PrintWriter defaultNoticeWriter = new PrintWriter(System.out);

	/**
	 * The maximum number of errors/warnings that are reported,
	 * can be reassigned from outside.
	 */
	private final int MaxErrors = 100;
	private final int MaxWarnings = 100;

	/** Switch: prompt user on each error. */
	private boolean promptOnError;

	/** Switch: emit warning messages. */
	private boolean emitWarnings;

	/** The number of errors encountered so far. */
	private int nerrors = 0;

	/** The number of warnings encountered so far. */
	private int nwarnings = 0;

	/**
	 * Get the current messager, which is also the compiler log.
	 */
	public static Messager getInstance()
	{
		if(instance==null)
		{
			//throw new InternalError("no messager instance!");
			instance = new Messager("default");
		}
		return instance;
	}

	/**
	 *
	 */
	public static void setInstance(Messager messager)
	{
		instance = messager;
	}


	private final String programName;
	private ResourceBundle messageRB = null;


	/**
	 * Constructor
	 * @param programName Name of the program (for error messages).
	 */
	protected Messager(String programName)
	{
		this(programName, null, null, null);//defaultErrWriter, defaultWarnWriter, defaultNoticeWriter);
	}

	/**
	 * Constructor
	 * @param programName Name of the program (for error messages).
	 * @param errWriter Stream for error messages
	 * @param warnWriter Stream for warnings
	 * @param noticeWriter Stream for other messages
	 */
	protected Messager(String programName, PrintWriter errWriter, PrintWriter warnWriter, PrintWriter noticeWriter)
	{
		this.errWriter = errWriter;
		this.warnWriter = warnWriter;
		this.noticeWriter = noticeWriter;

		//        Options options = Options.instance();
		//        this.promptOnError = options.get("-prompt") != null;
		//        this.emitWarnings = options.get("-nowarn") == null;
		//        this.MaxErrors = getIntOption(options, "-Xmaxerrs", 100);
		//        this.MaxWarnings = getIntOption(options, "-Xmaxwarns", 100);

		this.programName = programName;
		//instance = this;
	}


	/**
	 * Reset resource bundle, eg. locale has changed.
	 */
	public void reset()
	{
		messageRB = null;
	}

	/**
	 * Get string from ResourceBundle, initialize ResourceBundle
	 * if needed.
	 */
	private String getString(String key)
	{
		ResourceBundle messageRB = this.messageRB;
		if(messageRB==null)
		{
			try
			{
				this.messageRB = messageRB = ResourceBundle.getBundle("jadex.tools.jadexdoc.resources.jadexdoc");
			}
			catch(MissingResourceException e)
			{
				throw new Error("Fatal: Resource for jadexdoc is missing");
			}
		}
		return messageRB.getString(key);
	}


	/**
	 * get and format message string from resource
	 * @param key selects message from resource
	 */
	String getText(String key)
	{
		return getText(key, (String)null);
	}

	/**
	 * get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 first argument
	 */
	String getText(String key, String a1)
	{
		return getText(key, a1, null);
	}

	/**
	 * get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 */
	String getText(String key, String a1, String a2)
	{
		return getText(key, a1, a2, null);
	}

	/**
	 * get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 * @param a3 third argument
	 */
	String getText(String key, String a1, String a2, String a3)
	{
		return getText(key, a1, a2, a3, null);
	}

	/**
	 * get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 * @param a3 third argument
	 * @param a4 fourth argument
	 */
	String getText(String key, String a1, String a2, String a3, String a4)
	{
		try
		{
			String message = getString(key);
			String[] args = new String[4];
			args[0] = a1;
			args[1] = a2;
			args[2] = a3;
			args[3] = a4;
			return MessageFormat.format(message, (Object[])args);
		}
		catch(MissingResourceException e)
		{
			return "********** Resource for jadexdoc is broken. There is no "+
					key+" key in resource.";
		}
	}

	/**
	 * Print error message, increment error count.
	 * @param msg message to print
	 */
	public void printError(String msg)
	{
		if(errWriter!=null)
		{
			errWriter.println(programName+": "+msg);
			errWriter.flush();
		}
		else
		{
			System.err.println(programName+": "+msg);
		}
		prompt();
		nerrors++;
	}

	/**
	 * Print warning message, increment warning count.
	 * @param msg message to print
	 */
	public void printWarning(String msg)
	{
		if(warnWriter!=null)
		{
			warnWriter.println(programName+": warning - "+msg);
			warnWriter.flush();
		}
		else
		{
			System.err.println(programName+": warning - "+msg);
		}
		nwarnings++;
	}

	/**
	 * Print a message.
	 * @param msg message to print
	 */
	public void printNotice(String msg)
	{
		if(noticeWriter!=null)
		{
			noticeWriter.println(programName+": "+msg);
			noticeWriter.flush();
		}
		else
		{
			System.out.println(programName+": "+msg);
		}
	}
	
	/**
	 * Print error message, increment error count.
	 * @param msg message to print
	 * /
	public void printError(String msg)
	{
		getErrWriter().println(programName+": "+msg);
		getErrWriter().flush();
		prompt();
		nerrors++;
	}


	/**
	 * Print warning message, increment warning count.
	 * @param msg message to print
	 * /
	public void printWarning(String msg)
	{
		getWarnWriter().println(programName+": warning - "+msg);
		getWarnWriter().flush();
		nwarnings++;
	}

	/**
	 * Print a message.
	 * @param msg message to print
	 * /
	public void printNotice(String msg)
	{
		getNoticeWriter().println(msg);
		getNoticeWriter().flush();
	}


	/**
	 * Print error message, increment error count.
	 * @param key selects message from resource
	 */
	public void error(String key)
	{
		printError(getText(key));
	}

	/**
	 * Print error message, increment error count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 */
	public void error(String key, String a1)
	{
		printError(getText(key, a1));
	}

	/**
	 * Print error message, increment error count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 */
	public void error(String key, String a1, String a2)
	{
		printError(getText(key, a1, a2));
	}

	/**
	 * Print error message, increment error count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 * @param a3 third argument
	 */
	public void error(String key, String a1, String a2, String a3)
	{
		printError(getText(key, a1, a2, a3));
	}

	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 */
	public void warning(String key)
	{
		printWarning(getText(key));
	}

	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 */
	public void warning(String key, String a1)
	{
		printWarning(getText(key, a1));
	}

	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 */
	public void warning(String key, String a1, String a2)
	{
		printWarning(getText(key, a1, a2));
	}

	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 * @param a3 third argument
	 */
	public void warning(String key, String a1, String a2,
			String a3)
	{
		printWarning(getText(key, a1, a2, a3));
	}

	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 * @param a3 third argument
	 */
	public void warning(String key, String a1, String a2,
			String a3, String a4)
	{
		printWarning(getText(key, a1, a2, a3, a4));
	}

	/**
	 * Print a message.
	 * @param key selects message from resource
	 */
	public void notice(String key)
	{
		printNotice(getText(key));
	}

	/**
	 * Print a message.
	 * @param key selects message from resource
	 * @param a1 first argument
	 */
	public void notice(String key, String a1)
	{
		printNotice(getText(key, a1));
	}

	/**
	 * Print a message.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 */
	public void notice(String key, String a1, String a2)
	{
		printNotice(getText(key, a1, a2));
	}

	/**
	 * Print a message.
	 * @param key selects message from resource
	 * @param a1 first argument
	 * @param a2 second argument
	 * @param a3 third argument
	 */
	public void notice(String key, String a1, String a2, String a3)
	{
		printNotice(getText(key, a1, a2, a3));
	}

	/**
	 * Return total number of errors, including those recorded
	 * in the compilation log.
	 */
	public int nerrors()
	{
		return nerrors;
	}

	/**
	 * Return total number of warnings, including those recorded
	 * in the compilation log.
	 */
	public int nwarnings()
	{
		return nwarnings;
	}

	/**
	 * Print exit message.
	 */
	public void exitNotice()
	{
		if(nerrors>0)
		{
			notice((nerrors>1)? "main.errors": "main.error", ""+nerrors);
		}
		if(nwarnings>0)
		{
			notice((nwarnings>1)? "main.warnings": "main.warning",
					""+nwarnings);
		}
	}

	/**
	 * Flush the logs
	 */
	public void flush()
	{
		if(errWriter!=null) errWriter.flush();
		if(warnWriter!=null) warnWriter.flush();
		if(noticeWriter!=null) noticeWriter.flush();
	}
	
	/**
	 * Flush the logs
	 * /
	public void flush()
	{
		getErrWriter().flush();
		getWarnWriter().flush();
		getNoticeWriter().flush();
	}*/

	/**
	 * Prompt user after an error.
	 */
	public void prompt()
	{
		if(isPromptOnError())
		{
			System.err.println(getText("jadexdoc.resume.abort"));
			try
			{
				while(true)
				{
					switch(System.in.read())
					{
						case 'a':

						case 'A':
							System.exit(-1);
							return;

						case 'r':

						case 'R':
							return;

						case 'x':

						case 'X':
							throw new AssertionError("user abort");

						default:

					}
				}
			}
			catch(IOException e)
			{
			}
		}
	}

	public boolean isPromptOnError()
	{
		return promptOnError;
	}

	public void setPromptOnError(boolean promptOnError)
	{
		this.promptOnError = promptOnError;
	}

	public boolean isEmitWarnings()
	{
		return emitWarnings;
	}

	public void setEmitWarnings(boolean emitWarnings)
	{
		this.emitWarnings = emitWarnings;
	}

	public int getNerrors()
	{
		return nerrors;
	}

	public void setNerrors(int nerrors)
	{
		this.nerrors = nerrors;
	}

	public int getNwarnings()
	{
		return nwarnings;
	}

	public void setNwarnings(int nwarnings)
	{
		this.nwarnings = nwarnings;
	}

	/**
	 * @return the errWriter
	 * /
	public PrintWriter getErrWriter()
	{
		//return errWriter==null? new PrintWriter(System.err): errWriter;
		return errWriter==null? defaultErrWriter: errWriter;
	}*/

	/**
	 * @return the noticeWriter
	 * /
	public PrintWriter getNoticeWriter()
	{
		//return noticeWriter==null? new PrintWriter(System.out): noticeWriter;
		return noticeWriter==null? defaultNoticeWriter: noticeWriter;
	}*/

	/**
	 * @return the warnWriter
	 * /
	public PrintWriter getWarnWriter()
	{
		//return warnWriter==null? new PrintWriter(System.err): warnWriter;
		return warnWriter==null? defaultWarnWriter: warnWriter;
	}*/
}
