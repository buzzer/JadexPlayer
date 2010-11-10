package jadex.tools.introspector;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.*;
import jadex.tools.common.ElementPanel;
import jadex.tools.common.IToolPanel;
import jadex.tools.common.LocalToolRequestPlan;
import jadex.tools.common.ShortcutToolReply;
import jadex.tools.introspector.bdiviewer.BeliefbasePanel;
import jadex.tools.introspector.bdiviewer.GoalbasePanel;
import jadex.tools.introspector.bdiviewer.PlanbasePanel;
import jadex.tools.introspector.debugger.DebuggerTab;
import jadex.tools.ontology.*;
import jadex.util.SGUI;
import jadex.util.SReflect;
import jadex.util.SUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;

import javax.swing.*;

/**
 *  A generic tool panel for easy integration into the introspector.
 */
public class ToolPanel	extends JPanel	implements IToolPanel
{
	//-------- constants --------

	/** The text when nothing is selected in the details view. */
	public static final String NOTHING = "No element selected for detailed view.\nUse double-click to view element.";

	//-------- attributes --------

	/** The agent access. */
	protected IExternalAccess	agent;
	
	/** The agent to observe. */
	protected AgentIdentifier	observed;
	
	/** The content pane. */
	protected JSplitPane	content;

	/** The details panel. */
	protected ElementPanel	details;

	/** The currently registered listeners (listener->types). */
	protected Map	listeners;

	/** The current set of event types that we have registered our interest in. */
	protected Set	typeset;

	/** The tab panel. */
	protected JTabbedPane tabs;

	/** The tool components. */
	protected ToolTab[]	tools;

	/** Is the tool active. */
	protected boolean	isactive;

	//-------- constructors --------

	/**
	 *  Create a new tool panel for a remote agent.
	 *  @param agent	The agent access.
	 *  @param active	Flags indicating which tools should be active.
	 */
	public ToolPanel(IExternalAccess agent, AgentIdentifier observed, final boolean[] active)
	{
		this.agent	= agent;
		this.observed	= observed;
		this.typeset	= new HashSet();
		this.listeners	= new HashMap();
		this.details	= new ElementPanel("Details", NOTHING);
		this.tabs = new JTabbedPane();
//		tabs.addMouseListener(new MouseAdapter()
//		{
//			// Is only mouseReleased a popup trigger???
//			public void	mousePressed(MouseEvent e)	{doPopup(e);}
//			public void	mouseReleased(MouseEvent e)	{doPopup(e);}
//			public void	mouseClicked(MouseEvent e)	{doPopup(e);}
//		
//			protected void doPopup(MouseEvent e)
//			{
//				if(e.isPopupTrigger())
//				{
//					final int	i	= tabs.indexAtLocation(e.getX(), e.getY());
//					if(i!=-1)
//					{
//						String	name	= "Activate "+tabs.getTitleAt(i);
//						Icon	icon	= tabs.getIconAt(i);
//						final boolean	isenabled	= tabs.isEnabledAt(i);
//
//						// Show menu.
//						JPopupMenu	menu	= new JPopupMenu("Manage Tabs");
//						JCheckBoxMenuItem	item	= new JCheckBoxMenuItem(new AbstractAction(name, icon)
//						{
//							public void actionPerformed(ActionEvent e)
//							{
//								Component	comp	= tabs.getComponentAt(i);
//								tabs.setEnabledAt(i, !isenabled);
//								comp.setEnabled(!isenabled);
//								if(!isenabled)
//								{
//									tabs.setSelectedIndex(i);
//								}
//							}
//						});
//						item.setSelected(isenabled);
//						menu.add(item);
//						menu.show(tabs, e.getX(), e.getY());
//					}
//				}
//			}
//		});

        // Hack!?!?!
        this.tools	= new ToolTab[]{new BeliefbasePanel(this), new GoalbasePanel(this),
        	new PlanbasePanel(this), new DebuggerTab(this)};
        
        boolean selected	= false;
        for(int i=0; i<tools.length; i++)
		{
			this.tabs.addTab(tools[i].getName(), tools[i].getIcon(), tools[i]);

			// Select first active tab.
			if(!selected && active[i])
			{
				this.tabs.setSelectedIndex(i);
				selected	= true;
			}
		}

		this.content	= new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, details);
		content.setOneTouchExpandable(true);
		content.setResizeWeight(1.0);
		content.setDividerLocation(65535);	// Proportional (1.0) doesn't work.
		this.setLayout(new BorderLayout());
		this.add("Center", content);
		
        // Hack!!! Activate tools after constructor has returned
        // (allows to create manage tool goal before registering listener)
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				for(int i=0; i<tools.length; i++)
				{
					tools[i].setActive(active[i]);
				}
			}
		});
	}
	
	//-------- methods --------

	/**
	 *  Show element details.
	 */
	public void showElementDetails(Map element)
	{
        // todo: grab icon
		this.details.addElement(element, null);
		// Hack? to show detail panel.
		if(content.getDividerLocation()>content.getMaximumDividerLocation())
			content.setDividerLocation(content.getLastDividerLocation());
	}

	/**
	 *  Dispatch a goal and display errors (if any).
	 *  @param goal	The goal to dispatch.
	 *  @param errortitle	The title to use for an error dialog.
	 *  @param errormessage	An optional error message displayed before the exception.
	 */
	// Todo: remove as copy of ControlCenter.dispatchGoal().
	public void dispatchGoal(IGoal goal, final String errortitle, final String errormessage)
	{
//		System.out.println("dispatching goal ("+System.currentTimeMillis()/1000+"): "+goal+(goal.hasParameter("request")?", "+goal.getParameter("request").getValue():""));
		
		goal.addGoalListener(new IGoalListener()
		{
			public void goalFinished(AgentEvent ae)
			{
				IGoal	goal	= (IGoal)ae.getSource();
				if(!goal.isSucceeded())
				{
//					System.out.println("goal failed ("+System.currentTimeMillis()/1000+"): "+goal+(goal.hasParameter("request")?", "+goal.getParameter("request").getValue():""));
					String	text;
					if(errormessage==null && goal.getException()==null)
					{
						text	= errortitle;
					}
					else if(errormessage!=null && goal.getException()==null)
					{
						text	= errormessage;
					}
					else if(errormessage==null && goal.getException()!=null)
					{
						text	= "" + goal.getExcludeMode();
					}
					else //if(errormessage!=null && goal.getException()!=null)
					{
						text	= errormessage + "\n" + goal.getExcludeMode();
					}
					JOptionPane.showMessageDialog(SGUI.getWindowParent(ToolPanel.this), SUtil.wrapText(text), errortitle, JOptionPane.ERROR_MESSAGE);
				}
				else
				{
//					System.out.println("goal succeeded ("+System.currentTimeMillis()/1000+"): "+goal+(goal.hasParameter("request")?", "+goal.getParameter("request").getValue():""));
				}
				goal.removeGoalListener(this);
			}
			
			public void goalAdded(AgentEvent ae)
			{
			}
		}, false);
		agent.dispatchTopLevelGoal(goal);
	}

	//-------- tool methods --------

	/**
	 *  Get the name of the observed agent.
	 */
	public String	getAgentName()
	{
		return observed.getName();
	}

	/**
	 *  Refresh the tool representation, by getting a fresh state.
	 *  The tool should clear its GUI prior to calling this method.
	 *  @param listener	The listener that wants a refresh (has to be registered).
	 */
	public void	refresh(final ISystemEventListener listener)
	{
//		System.out.println("refreshing");
		String[]	types	= (String[])listeners.get(listener);
		if(types==null)
			throw new IllegalArgumentException("Listener has to be registered.");

		CurrentState	state	= new CurrentState(IntrospectorAdapter.TOOL_INTROSPECTOR);
			state.setEventTypes(types);

		if(observed.equals(agent.getAgentIdentifier()))
		{
			ShortcutToolReply reply = new ShortcutToolReply(this, null);
			LocalToolRequestPlan.handleLocalToolRequest(agent, state, reply);
			state	= (CurrentState)reply.getResult();
			if(state!=null)
			{
				//System.out.println("toolstate: "+SUtil.arrayToString(state.getSystemEvents()));
				listener.systemEventsOccurred(state.getSystemEvents());
			}
		}
		else
		{
			IGoal	getstate	= agent.getGoalbase().createGoal("tool_request");
			getstate.getParameter("tool").setValue(this);
			getstate.getParameter("agent").setValue(observed);
			getstate.getParameter("request").setValue(state);
			getstate.addGoalListener(new IGoalListener()
			{
				public void goalAdded(AgentEvent ae) {}
				
				public void goalFinished(AgentEvent ae)
				{
					IGoal	goal	= (IGoal)ae.getSource();
					if(goal.isSucceeded())
					{
						final CurrentState	state	= (CurrentState)goal.getParameter("result").getValue();
						if(state!=null)
						{
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									listener.systemEventsOccurred(state.getSystemEvents());
								}
							});
						}
					}
				}
			}, false);
			dispatchGoal(getstate, "Tool Problem", "Tool request failed");			
		}
	}


	/**
	 *  Issue an action request to the agent.
	 *  @param action The action to be performed.
	 */
	public void	performToolAction(ToolAction action)
	{
		if(observed.equals(agent.getAgentIdentifier()))
		{
			ShortcutToolReply reply = new ShortcutToolReply(this, null);
			LocalToolRequestPlan.handleLocalToolRequest(agent, action, reply);
		}
		else
		{
			// Hack???
			if(action.getToolType()==null)
				action.setToolType("introspector");

			IGoal	perform	= agent.getGoalbase().createGoal("tool_request");
			perform.getParameter("tool").setValue(this);
			perform.getParameter("agent").setValue(observed);
			perform.getParameter("request").setValue(action);
			dispatchGoal(perform, "Error while performing action", createErrorMessage(action));
		}
	}

	/**
	 *  Register for event propagation.
	 *  @param listener	The listener.
	 *  @param types	The system event types.
	 */
	public void	addChangeListener(ISystemEventListener listener, String[] types)
	{
		if(types==null)
			throw new NullPointerException("Types may not be null.");

		listeners.put(listener, types);
		updateRegistration();
		refresh(listener);	// Hack??? Provides initial state.
	}

	/**
	 *  Deregister event propagation.
	 *  @param listener	The listener.
	 */
	public void	removeChangeListener(ISystemEventListener listener)
	{
		listeners.remove(listener);
		updateRegistration();
	}


	//-------- helper methods --------

	/**
	 *  Create a useful error message
	 *  including the original command.
	 */
	// Only needed, because generated ontology beans have no toString()!!!
	protected String	createErrorMessage(ToolAction action)
	{
		String	msg;
		if(action instanceof ExecuteCommand)
		{
			msg	= "Action: Execute command "+((ExecuteCommand)action).getCommand();
		}
		else if(action instanceof ChangeAttribute)
		{
			ChangeAttribute	ca	= (ChangeAttribute)action;
			msg	= "Action: Change "+ca.getAttributeName()+" of "+ca.getElementName()
				+" to "+ca.getValue();
		}
		else
		{
			// Unknown action type.
			msg	= "Action: "+SReflect.getInnerClassName(action.getClass());
		}
		return SUtil.wrapText(msg);
	}

	/**
	 *  Check if registration has to be done/changed after listeners
	 *  have been added/removed.
	 */
	protected void	updateRegistration()
	{
		// Determine new type set.
		Set	typeset2	= new HashSet();
		for(Iterator i=listeners.values().iterator(); i.hasNext(); )
		{
			String[]	types	= (String[])i.next();
			for(int j=0; j<types.length; j++)
				typeset2.add(types[j]);
		}

		// Does the subscription has to be changed?
		if(!typeset2.equals(typeset))
		{
			// Remember new type set.
			typeset	= typeset2;
			String[]	types	= (String[])typeset.toArray(new String[typeset.size()]);

			final Register	reg	= new Register(IntrospectorAdapter.TOOL_INTROSPECTOR);
			reg.setEventTypes(types);

			if(observed.equals(agent.getAgentIdentifier()))
			{
				LocalToolRequestPlan.handleLocalToolRequest(agent, reg, new ShortcutToolReply(this, null));
			}
			else
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						IGoal	reggoal	= agent.getGoalbase().createGoal("tool_request");
						reggoal.getParameter("tool").setValue(ToolPanel.this);
						reggoal.getParameter("agent").setValue(observed);
						reggoal.getParameter("request").setValue(reg);
						try
						{
							agent.dispatchTopLevelGoalAndWait(reggoal);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

	/**
	 *  Deregister with the agent.
	 */
	protected void cleanupRegistration()
	{
		Deregister	dereg	= new Deregister(IntrospectorAdapter.TOOL_INTROSPECTOR);
		IGoal	dereggoal	= agent.getGoalbase().createGoal("tool_request");
		dereggoal.getParameter("tool").setValue(this);
		dereggoal.getParameter("agent").setValue(observed);
		dereggoal.getParameter("request").setValue(dereg);
//		System.err.println("deregistering tool for: "+observed);
		dispatchGoal(dereggoal, "Error Closing Tool", "Problem while deregistering tool");
	}

	//-------- IToolPanel interface --------
	
	/**
	 *  The globally unique tool id is used to route messages to
	 *  the corresponding tools. 
	 */
	public String getId()
	{
		return getAgentName()+"_toolpanel@"+hashCode();
	}

	/**
	 *  Activate the tool.
	 */
	public void activate()
	{
		this.isactive	= true;
	}

	/**
	 *  The observed agent has changed and the tool needs to be updated.
	 */
	public void update(final CurrentState state)
	{
//		System.out.println("updating");
		SwingUtilities.invokeLater(new Runnable()
		{
			public void	run()
			{
				SystemEvent[]	ces	= state.getSystemEvents();
//				System.out.println(SUtil.arrayToString(ces));
				ISystemEventListener[] cls = (ISystemEventListener[])listeners.keySet()
					.toArray(new ISystemEventListener[listeners.size()]);
				for(int i=0; i<cls.length; i++)
				{
					// Hack!!! May receive events it is not interested in!
//					System.out.println("updating_call");
					try
					{
						cls[i].systemEventsOccurred(ces);
					}
					catch(Exception e)
					{
//						System.err.println("Exception of "+cls[i]+" during handling of "+SUtil.arrayToString(ces));
//						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 *  Used to check if management of the tool is still required.
	 */
	public boolean isActive()
	{
		return isactive;
	}

	/**
	 *  Called when the tool should be deactivated (e.g. when the agent dies).
	 */
	public void deactivate()
	{
		try
		{
			cleanupRegistration();
			isactive	= false;
		}
		catch(AgentDeathException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *  Get the component.
	 *  @return The component.
	 */
	public Component getComponent()
	{
		return this;
	}

	/**
	 *
	 * /
	public static boolean check(AgentIdentifier aid1, AgentIdentifier aid2)
	{
		return !aid1.equals(aid2);
	}*/
}

