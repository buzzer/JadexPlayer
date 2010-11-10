package jadex.tools.common;

import jadex.adapter.IToolAdapter.IToolReply;
import jadex.runtime.IProcessGoal;
import jadex.util.SGUI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *  In the local case, no messages are sent. Only the goal result is set.
 */
public class ShortcutToolReply implements IToolReply
{
	/** The tool. */
	protected IToolPanel tool;

	/** The goal. */
	protected IProcessGoal goal;

	/** The result. */
	protected Object result;

	/**
	 *
	 */
	public ShortcutToolReply(IToolPanel tool, IProcessGoal goal)
	{
		this.tool = tool;
		this.goal = goal;
	}

	/**
	 * Send an inform message.
	 * @param content The message content.
	 * @param sync If true, wait for a reception aknowledgement of the recipient.
	 */
	public void sendInform(Object content, boolean sync)
	{
		if(goal!=null)
		{
			try
			{
				// Hack! todo: cannot determine if called from plan or from agent systemEventOccurred
				goal.setResult(content);
				return;
			}
			catch(Exception e)
			{
			}
		}

//		if(content instanceof String)
//		{
//			CurrentState	state	= new CurrentState();
//	//		System.err.println(msg.getMessage());
//			String info = (String)content;
//			List	eves	= new ArrayList();
//			info	= info.substring(1, info.length()-1);
//			ExpressionTokenizer	exto	= new ExpressionTokenizer(info,
//				" \t\r\n", new String[]{"\"\"", "()"});
//			while(exto.hasMoreTokens())
//			{
//				String	tok	= exto.nextToken();
//				eves.add(ToolUpdatePlan.fromSLString(tok));
//			}
//			state.setSystemEvents((SystemEvent[])eves.toArray(new SystemEvent[eves.size()]));
//			tool.update(state);
//		}
//		else
//		{
			result = content;
//		}
	}

	/**
	 * Send a failure message.
	 * @param content The message content.
	 * @param sync If true, wait for a reception aknowledgement of the recipient.
	 */
	public void sendFailure(final Object content, boolean sync)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				String txt = ""+content;
				JOptionPane.showMessageDialog(SGUI.getWindowParent(tool.getComponent())
					, txt, "Tool Error", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	/**
	 *  Get the result.
	 *  @return The result.
	 */
	public Object getResult()
	{
		return result;
	}

	public void cleanup()
	{
		// sync not used!?
	}
}

