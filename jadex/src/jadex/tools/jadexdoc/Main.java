package jadex.tools.jadexdoc;

import java.io.*;
import java.util.*;
import jadex.config.Configuration;
import jadex.tools.jadexdoc.doclets.MessageRetriever;

/**
 *  Main program of Jadexdoc.
 */
public class Main
{
	//-------- constants --------

	protected static final String JADEXDOC_NAME = "jadexdoc";
	protected static final String STANDARD_DOCLET_CLASSNAME = "jadex.tools.jadexdoc.doclets.standard.Standard";
	protected static final String RESOURCE_LOCATION = "jadex.tools.jadexdoc.resources.jadexdoc";

	//-------- attributes --------

	/** The default doclet class name. */
	protected final String defaultDocletClassName;

	/** The list of options. */
	protected List options = new ArrayList();

	/** The messager. */
	protected Messager msg;

	/** The message retriever. */
	protected MessageRetriever messager;

	//protected boolean breakiterator = false;
	//protected boolean verbose = false;
	//protected String encoding = null;

	/** The doclet invoker. */
	protected DocletInvoker docletInvoker;

	/** The Jadexdoc tool. */
	protected JadexdocTool jadexdocTool;

	/** Option. */
	protected boolean rejectWarnings = false;

	//-------- constructors --------

	/**
	 *  Create a new main.
	 *  @param programName
	 *  @param errWriter
	 *  @param warnWriter
	 *  @param noticeWriter
	 *  @param defaultDocletClassName
	 */
	Main(String programName, PrintWriter errWriter, PrintWriter warnWriter, PrintWriter noticeWriter, String defaultDocletClassName)
	{
		this.msg = new Messager(programName, errWriter, warnWriter, noticeWriter);
		Messager.setInstance(msg);
		this.messager = new MessageRetriever(msg, RESOURCE_LOCATION);
		this.defaultDocletClassName = defaultDocletClassName;
	}

	/**
	 *  Create a new main.
	 *  @param programName
	 *  @param defaultDocletClassName
	 */
	Main(String programName, String defaultDocletClassName)
	{
		this.msg = new Messager(programName);
		Messager.setInstance(msg);
		this.messager = new MessageRetriever(msg, RESOURCE_LOCATION);
		this.defaultDocletClassName = defaultDocletClassName;
	}

	/**
	 *  Create a new main.
	 *  @param programName
	 */
	Main(String programName)
	{
		this(programName, STANDARD_DOCLET_CLASSNAME);
	}

	/**
	 *  Create a new main.
	 */
	Main()
	{
		this(JADEXDOC_NAME);
	}

	//-------- static invocation methods --------

	/**
	 * Command line interface.
	 * @param args The command line parameters.
	 */
	public static void main(String[] args)
	{
		System.exit(execute(args));
	}

	/**
	 * Programmatic interface.
	 * @param args The command line parameters.
	 * @return The return code.
	 */
	public static int execute(String[] args)
	{
		Configuration.setFallbackConfiguration("jadex/config/batch_conf.properties");
		Main jdoc = new Main();
		return jdoc.begin(args);
	}

	//-------- methods --------

	/**
	 *  Main program - external wrapper.
	 */
	public int begin(String[] argv)
	{
		boolean failed = false;
		try
		{
			failed = !parseAndExecute(argv);
		}
		catch(ExitJadexdoc exc)
		{
		}
		catch(OutOfMemoryError ee)
		{
			messager.error("main.out.of.memory");
			failed = true;
		}
		catch(Error ee)
		{
			ee.printStackTrace();
			messager.error("main.fatal.error");
			failed = true;
		}
		catch(Exception ee)
		{
			ee.printStackTrace();
			messager.error("main.fatal.exception");
			failed = true;
		}
		finally
		{
			messager.exitNotice();
			messager.flush();
		}
		failed |= messager.nerrors()>0;
		failed |= rejectWarnings && messager.nwarnings()>0;
		return failed? 1: 0;
	}


	/**
	 *  Main program - internal
	 */
	protected boolean parseAndExecute(String[] argv)
	{
		long tm = System.currentTimeMillis();

		try
		{
			argv = CommandLine.parse(argv);
		}
		catch(FileNotFoundException e)
		{
			messager.error("File_Read_Error", e.getMessage());
			exit();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			exit();
		}

		setDocletInvoker(argv);

		List jadexNames = new ArrayList();
		List subPackages = new ArrayList();
		List excludedPackages = new ArrayList();

		Map compOpts = new HashMap();

		for(int i = 0; i<argv.length; i++)
		{
			String arg = argv[i];
			if(arg.equals("-subpackages"))
			{
				oneArg(argv, i++);
				addToList(subPackages, argv[i]);
			}
			else if(arg.equals("-exclude"))
			{
				oneArg(argv, i++);
				addToList(excludedPackages, argv[i]);
			}
			else if(arg.equals("-verbose"))
			{
				//verbose = true;
				setOption(arg);
				compOpts.put("-verbose", "");
			}
			else if(arg.equals("-encoding"))
			{
				oneArg(argv, i++);
				//encoding = argv[i];
				compOpts.put("-encoding", argv[i]);
			}
			else if(arg.equals("-breakiterator"))
			{
				//breakiterator = true;
				setOption("-breakiterator");
			}
			else if(arg.equals("-help"))
			{
				usage();
				exit();
			}
			else if(arg.equals("-prompt"))
			{
				compOpts.put("-prompt", "-prompt");
				messager.setPromptOnError(true);
			}
			else if(arg.equals("-overview"))
			{
				oneArg(argv, i++);
			}
			else if(arg.equals("-doclet"))
			{
				i++;
			}
			else if(arg.equals("-docletpath"))
			{
				i++;
			}
			else if(arg.startsWith("-"))
			{
				int optionLength;
				optionLength = docletInvoker.optionLength(arg);

				if(optionLength<0)
				{
					exit();
				}
				else if(optionLength==0)
				{
					usageError("main.invalid_flag", arg);
				}
				else
				{
					if((i+optionLength)>argv.length)
					{
						usageError("main.requires_argument", arg);
					}
					List args = new ArrayList();
					for(int j = 0; j<optionLength-1; ++j)
					{
						args.add(argv[++i]);
					}
					setOption(arg, args);
				}
			}
			else
			{
				jadexNames.add(arg);
			}
		}
		if(jadexNames.isEmpty() && subPackages.isEmpty())
		{
			usageError("main.No_packages_or_agents_specified");
		}

		if(!docletInvoker.validOptions(options))
		{
			exit();
		}

		docletInvoker.setOptions(options);

		jadexdocTool = new JadexdocTool(messager, options);
		jadexdocTool.setJadexNames(jadexNames, subPackages, excludedPackages);

		docletInvoker.setAgents(jadexdocTool.getAgents());
		docletInvoker.setPackages(jadexdocTool.getPackages());

		boolean ok = docletInvoker.start();

		if(compOpts.get("-verbose")!=null)
		{
			tm = System.currentTimeMillis()-tm;
			messager.notice("main.done_in", Long.toString(tm));
		}

		return ok;
	}

	/**
	 *
	 * @param argv
	 */
	protected void setDocletInvoker(String[] argv)
	{
		String docletClassName = null;
		String docletPath = null;
		for(int i = 0; i<argv.length; i++)
		{
			String arg = argv[i];
			if(arg.equals("-doclet"))
			{
				oneArg(argv, i++);
				if(docletClassName!=null)
				{
					usageError("main.more_than_one_doclet_specified_0_and_1", docletClassName, argv[i]);
				}
				docletClassName = argv[i];
			}
			else if(arg.equals("-docletpath"))
			{
				oneArg(argv, i++);
				if(docletPath==null)
				{
					docletPath = argv[i];
				}
				else
				{
					docletPath += File.pathSeparator+argv[i];
				}
			}
		}
		if(docletClassName==null)
		{
			docletClassName = defaultDocletClassName;
		}

		try
		{
			docletInvoker = new DocletInvoker(messager, docletClassName, docletPath);
		}
		catch(ClassNotFoundException e)
		{
			messager.error("main.doclet_class_not_found", docletClassName);
			exit();
		}
		catch(IllegalAccessException e)
		{
			messager.error("main.exception_thrown", docletClassName, e.toString());
			exit();
		}
		catch(InstantiationException e)
		{
			messager.error("main.exception_thrown", docletClassName, e.toString());
			exit();
		}
	}

	/**
	 * Set one arg option.
	 * Error and exit if one argument is not provided.
	 */
	protected void oneArg(String[] args, int index)
	{
		if((index+1)<args.length)
		{
			setOption(args[index], args[index+1]);
		}
		else
		{
			usageError("main.requires_argument", args[index]);
		}
	}

	/**
	 *
	 * @param list
	 * @param str
	 */
	protected void addToList(List list, String str)
	{
		StringTokenizer st = new StringTokenizer(str, ":");
		String current;
		while(st.hasMoreTokens())
		{
			current = st.nextToken();
			list.add(current);
		}
	}

	/**
	 *
	 * @param key
	 */
	protected void usageError(String key)
	{
		messager.error(key);
		usage();
		exit();
	}

	/**
	 *
	 * @param key
	 * @param a1
	 */
	protected void usageError(String key, String a1)
	{
		messager.error(key, a1);
		usage();
		exit();
	}

	/**
	 *
	 * @param key
	 * @param a1
	 * @param a2
	 */
	protected void usageError(String key, String a1, String a2)
	{
		messager.error(key, a1, a2);
		usage();
		exit();
	}

	/**
	 * indicate an option with no arguments was given.
	 */
	protected void setOption(String opt)
	{
		String[] option = {opt};
		options.add(option);
	}

	/**
	 * indicate an option with one argument was given.
	 */
	protected void setOption(String opt, String argument)
	{
		String[] option = {opt, argument};
		options.add(option);
	}

	/**
	 * indicate an option with the specified list of arguments was given.
	 */
	protected void setOption(String opt, List arguments)
	{
		String[] args = new String[arguments.size()+1];
		int k = 0;
		args[k++] = opt;
		for(int i = 0; i<arguments.size(); i++)
		{
			args[k++] = (String)arguments.get(i);
		}
		options.add(args);
	}

	/**
	 * Usage
	 */
	protected void usage()
	{
		messager.notice("main.usage");
		if(docletInvoker!=null)
		{
			docletInvoker.optionLength("-help");
		}
	}

	/**
	 * Exit Jadexdoc.
	 */
	protected void exit()
	{
		throw new ExitJadexdoc();
	}

	/**
	 *  The Jadexdoc error.
	 */
	protected class ExitJadexdoc extends Error
	{
		/**
		 *  Create a new error.
		 */
		public ExitJadexdoc()
		{
			super();
		}
	}
}
