package jadex.tools.common;

import jadex.adapter.fipa.SFipa;
import jadex.runtime.IMessageEvent;
import jadex.runtime.MessageEventFilter;
import jadex.runtime.Plan;
import jadex.tools.ontology.CurrentState;

import javax.swing.SwingUtilities;


/**
 *  Plan to manage a tool panel.
 */
public class ToolUpdatePlan extends Plan
{
	//-------- attributes --------

	/** The tool panel. */
	protected IToolPanel	tool;

	//-------- constructors --------
	
	/**
	 *  Create a new tool update plan.
	 */
	public ToolUpdatePlan()
	{
//		System.out.println("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		// Extract parameters
		this.tool	= (IToolPanel)getParameter("tool").getValue();
		
		// Add a waitqueue filter to avoid loosing messages.
		MessageEventFilter	filter	= new MessageEventFilter("tool_inform");
		filter.addValue(SFipa.CONVERSATION_ID, tool.getId());
		getWaitqueue().addFilter(filter);

		// Activate the tool.
		tool.activate();

		// Handle inform messages from observed agent
		while(tool.isActive())
		{
//			System.out.println("Waiting for update message with convid "+tool.getId());
			IMessageEvent	msg	= (IMessageEvent)waitFor(filter);
//			System.out.println("Update received: "+msg);
//			CurrentState	state	= (CurrentState)msg.getContent();
//			tool.update(state);
//			sendMessage(msg.createReply("tool_acknowledge"));

			// Convert to events and inform listener.
//			CurrentState	state	= new CurrentState();
////			System.err.println(msg.getMessage());
//			String info = ((String)msg.getContent()).trim();
//			List	eves	= new ArrayList();
//			info	= info.substring(1, info.length()-1);
//			ExpressionTokenizer	exto	= new ExpressionTokenizer(info,
//				" \t\r\n", new String[]{"\"\"", "()"});
//			while(exto.hasMoreTokens())
//			{
//				String	tok	= exto.nextToken();
//				eves.add(fromSLString(tok));
//			}
//			state.setSystemEvents((SystemEvent[])eves.toArray(new SystemEvent[eves.size()]));
			CurrentState	state	= (CurrentState)msg.getContent();
			tool.update(state);
			sendMessage(msg.createReply("tool_acknowledge"));
		}
	}

	/**
	 *  On abort force tool deactivation.
	 */
	public void	aborted()
	{
//		System.err.println("aborted: "+this);
//		SwingUtilities.invokeLater(new Runnable()
//		{
//			public void run()
//			{
//				System.err.println("aborted later: "+this);
				tool.deactivate();
//			}
//		});
	}

	//-------- helper methods --------

	/**
	 *  Create a system event from an sl string.
	 * /
	public static SystemEvent	fromSLString(String sl)
	{
//		System.out.println("Received: "+sl);
		Map	map	= (Map)SUtil.fromSLString(sl);
		String	type	= (String)map.get("type");
		Object	source	= map.get("source");
		Object	value	= map.get("value");
		int	index	= Integer.parseInt((String)map.get("index"));

		return new SystemEvent(type, source, value, index);
	}*/
}
