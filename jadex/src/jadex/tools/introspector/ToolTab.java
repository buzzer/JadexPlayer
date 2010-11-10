package jadex.tools.introspector;

import java.awt.event.ActionEvent;

import javax.swing.*;
import jadex.runtime.*;
import jadex.util.SGUI;

/**
 *  Base class for tool components.
 */
public abstract class ToolTab	extends JPanel	implements ISystemEventListener
{
	//-------- static part --------

	/** The image icons. */
	protected static UIDefaults	icons	= new UIDefaults(new Object[]
	{
		// todo: rename icon?
		"start",	SGUI.makeIcon(ToolTab.class, "/jadex/tools/common/images/start.png"),
		"stop",	SGUI.makeIcon(ToolTab.class, "/jadex/tools/common/images/stop.png")
	});

	//-------- attributes --------

	/** The tool panel (for communication with the observed agent). */
	protected ToolPanel	tool;

	/** The name of this tool component. */
	protected String	name;

	/** The icon of this tool component. */
	protected Icon	icon;

	/** The system event types to register for. */
	protected String[]	events;
	
	/** Flag indicating if observation is active. */
	protected boolean	active;

	/** The toolbar actions. */
	protected Action[]	actions;

	/** The state of toolbar actions (saved when tool is inactivated). */
	protected boolean[]	actionstates;

	//-------- constructors --------

	/**
	 *  Create a new tool component.
	 *  @param tool	The tool panel (for communication with the observed agent).
	 *  @param name	The name of the tool component.
	 *  @param icon	The icon of the tool component.
	 *  @param events	The system event types to register for.
	 */
	public ToolTab(ToolPanel tool, String name, Icon icon, String[] events)
	{
		this.tool	= tool;
		this.name	= name;
		this.icon	= icon;
		this.events	= events;
	}

	//-------- methods --------

	/**
	 *  Get the tool panel of this component.
	 */
	public ToolPanel	getToolPanel()
	{
		return this.tool;
	}

	/**
	 *  Get the name this component.
	 */
	public String	getName()
	{
		return this.name;
	}

	/**
	 *  Get the icon of this component.
	 */
	public Icon	getIcon()
	{
		return this.icon;
	}

	/**
	 *  (De-)Activate agent observation.
	 */
	public void	setActive(boolean active)
	{
		// Change state.
		if(active!=this.active)
		{
			if(active)
			{
				clear();
				tool.addChangeListener(this, events);
			}
			else
			{
				tool.removeChangeListener(this);
			}
			this.active	= active;
		}

		// Set action settings regardless of change, as setActive() is also called initially.
		if(active)
		{
			STARTSTOP_ACTION.putValue(Action.SHORT_DESCRIPTION, "Stop Observation");
			STARTSTOP_ACTION.putValue(Action.SMALL_ICON, icons.getIcon("stop"));
		}
		else
		{
			STARTSTOP_ACTION.putValue(Action.SHORT_DESCRIPTION, "Start Observation");
			STARTSTOP_ACTION.putValue(Action.SMALL_ICON, icons.getIcon("start"));
		}


		// Restore action states.
		if(active)
		{
			if(actionstates!=null)
			{
				Action[]	actions	= getActions();
				for(int i=1; i<actions.length; i++)
				{
					if(actions[i]!=null && actions[i]!=STARTSTOP_ACTION)
					{
						actions[i].setEnabled(actionstates[i]);
					}
				}
				actionstates	= null;
			}
		}
		
		// Disable actions and save states.
		else 
		{
			assert actionstates==null;
			Action[]	actions	= getActions();
			actionstates	= new boolean[actions.length];
			for(int i=0; i<actions.length; i++)
			{
				if(actions[i]!=null && actions[i]!=STARTSTOP_ACTION)
				{
					actionstates[i]	= actions[i].isEnabled();
					actions[i].setEnabled(false);
				}
			}
		}
	}
	
	/**
	 *  Get the (menu/toolbar) actions of the bdi viewer.
	 */
	public Action[]	getActions()
	{
		if(this.actions==null)
		{
			this.actions	= new Action[1];
			
			this.actions[0]	= STARTSTOP_ACTION;
		}

		return this.actions;
	}

	//-------- template methods --------

	/**
	 *  Update the view, as a change event occured.
	 *  @param events	The change event.
	 */
	public abstract void	systemEventsOccurred(SystemEvent[] events);

	/**
	 *  Clear the view when refreshing.
	 *  To be overriden to perform custom cleanup.
	 */
	protected abstract void	clear();
	
	//-------- toolbar actions --------

	// Todo: why name in constructor for tool tip,but short desc later?
	public final AbstractAction	STARTSTOP_ACTION	= new AbstractAction("Start Observation", icons.getIcon("start"))
	{
		public void actionPerformed(ActionEvent e)
		{
			setActive(!active);
		}
	};
}

