package jadex.tools.jadexdoc.doclets;


import java.text.MessageFormat;
import java.util.*;
import jadex.tools.jadexdoc.Messager;

/**
 * Retrieve and format messages stored in a resource.
 */
public class MessageRetriever
{
	/**
	 * The global messager for this run.
	 */
	private final Messager messager;

	/**
	 * The location from which to lazily fetch the resource..
	 */
	private final String resourcelocation;

	/**
	 * The lazily fetched resource..
	 */
	private ResourceBundle messageRB;

	/**
	 * True when running in quiet mode
	 */
	private boolean isQuiet = false;

	/**
	 * Initilize the ResourceBundle with the given resource.
	 * @param messager The messager
	 * @param rb Resource.
	 */
	public MessageRetriever(Messager messager, ResourceBundle rb)
	{
		this.messager = messager;
		this.messageRB = rb;
		this.resourcelocation = null;
	}

	/**
	 * Initilize the ResourceBundle with the given resource.
	 * @param messager the messager
	 * @param resourcelocation Resource.
	 */
	public MessageRetriever(Messager messager, String resourcelocation)
	{
		this.messager = messager;
		this.resourcelocation = resourcelocation;
	}

	/**
	 * get and format message string from resource
	 * @param key selects message from resource
	 */
	public String getText(String key)
	{
		return getText(key, (String)null);
	}

	/**
	 * Get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 Argument, to be repalced in the message.
	 */
	public String getText(String key, String a1)
	{
		return getText(key, a1, null);
	}

	/**
	 * Get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 */
	public String getText(String key, String a1, String a2)
	{
		return getText(key, a1, a2, null);
	}

	/**
	 * Get and format message string from resource
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 * @param a3 third argument to be replaced in the message.
	 */
	public String getText(String key, String a1, String a2, String a3)
	{
		if(messageRB==null)
		{
			try
			{
				messageRB = ResourceBundle.getBundle(resourcelocation);
			}
			catch(MissingResourceException e)
			{
				throw new Error("Fatal: Resource ("+resourcelocation+
						") for jadexdoc doclets is missing.");
			}
		}
		try
		{
			String message = messageRB.getString(key);
			String[] args = new String[3];
			args[0] = a1;
			args[1] = a2;
			args[2] = a3;
			return MessageFormat.format(message, (Object[])args);
		}
		catch(MissingResourceException e)
		{
			throw new Error("Fatal: Resource ("+resourcelocation+
					") for jadexdoc is broken. There is no '"+
					key+"' key in resource.");
		}
	}


	/**
	 * Print error message, increment error count.
	 * @param msg message to print
	 */
	void printError(String msg)
	{
		if(messager!=null)
		{
			messager.printError(msg);
		}
		else
		{
			System.err.println(/* Main.program + ": " + */ msg);
		}
	}

	/**
	 * Print warning message, increment warning count.
	 * @param msg message to print
	 */
	void printWarning(String msg)
	{
		if(messager!=null)
		{
			messager.printWarning(msg);
		}
		else
		{
			System.err.println(/* Main.program +  ": warning - " + */ "Warning: "+msg);
		}
	}

	/**
	 * Print a message.
	 * @param msg message to print
	 */
	void printNotice(String msg)
	{

		if(!isQuiet)
		{
			if(messager!=null)
			{
				messager.printNotice(msg);
			}
			else
			{
				System.out.println(msg);
			}
		}
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
	 * @param a1 first argument to be replaced in the message.
	 */
	public void error(String key, String a1)
	{
		printError(getText(key, a1));
	}


	/**
	 * Print error message, increment error count.
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 */
	public void error(String key, String a1, String a2)
	{
		printError(getText(key, a1, a2));
	}


	/**
	 * Print error message, increment error count.
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 * @param a3 third argument to be replaced in the message.
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
	 * @param a1 first argument to be replaced in the message.
	 */
	public void warning(String key, String a1)
	{
		printWarning(getText(key, a1));
	}


	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 */
	public void warning(String key, String a1, String a2)
	{
		printWarning(getText(key, a1, a2));
	}


	/**
	 * Print warning message, increment warning count.
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 * @param a3 third argument to be replaced in the message.
	 */
	public void warning(String key, String a1, String a2, String a3)
	{
		printWarning(getText(key, a1, a2, a3));
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
	 * @param a1 first argument to be replaced in the message.
	 */
	public void notice(String key, String a1)
	{
		printNotice(getText(key, a1));
	}


	/**
	 * Print a message.
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 */
	public void notice(String key, String a1, String a2)
	{
		printNotice(getText(key, a1, a2));
	}


	/**
	 * Print a message.
	 * @param key selects message from resource
	 * @param a1 first argument to be replaced in the message.
	 * @param a2 second argument to be replaced in the message.
	 * @param a3 third argument to be replaced in the message.
	 */
	public void notice(String key, String a1, String a2, String a3)
	{
		printNotice(getText(key, a1, a2, a3));
	}

	/**
	 * Set to quiet mode.
	 */
	public void setQuiet()
	{
		isQuiet = true;
	}

	public int nerrors()
	{
		return messager.nerrors();
	}

	public int nwarnings()
	{
		return messager.nwarnings();
	}

	public void exitNotice()
	{
		messager.exitNotice();
	}

	public void flush()
	{
		messager.flush();
	}

	public void setPromptOnError(boolean promptOnError)
	{
		messager.setPromptOnError(promptOnError);
	}

}
