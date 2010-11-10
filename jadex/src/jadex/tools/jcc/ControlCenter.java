package jadex.tools.jcc;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.config.Configuration;
import jadex.runtime.*;
import jadex.tools.common.GuiProperties;
import jadex.tools.common.RememberOptionMessage;
import jadex.tools.common.plugin.IAgentListListener;
import jadex.tools.common.plugin.IControlCenter;
import jadex.tools.common.plugin.IControlCenterPlugin;
import jadex.tools.common.plugin.IMessageListener;
import jadex.util.DynamicURLClassLoader;
import jadex.util.SGUI;
import jadex.util.SUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.*;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * The Jadex control center.
 */
public class ControlCenter implements IControlCenter
{
	//-------- constants --------

	/** The initially included plugins. */
	public static final String JCC_PLUGINS = "jcc.plugins";

	/** The last used project */
	public static final String JCC_PROJECT = "jcc.project";

	//-------- attributes --------

	/** The plugins (plugin->panel). */
	protected Map	plugins;

	/** The control center window. */
	protected ControlCenterWindow	window;

	/** The current project. */
	protected File	project;

	/** The external access. */
	protected IExternalAccess	agent;

	/** The listeners for messages. */
	protected List	msglisteners;

	/** The agent list. */
	// Todo: remove (use belief listener).
	protected AgentList	agentlist;

	/** Flag indicating if exit was initiated. */
	protected boolean	killed;

	//-------- constructors --------

	/**
	 *  Create a control center.
	 */
	public ControlCenter(final IExternalAccess agent)
	{
		this.agent = agent;
		this.plugins	= new LinkedHashMap();
		this.msglisteners	= new ArrayList();
		this.agentlist	= new AgentList();

		Set plugin_set = new HashSet();
		msglisteners = new Vector();

		window = new ControlCenterWindow(ControlCenter.this);
		
		agent.addAgentListener(new IAgentListener()
		{
			public void	agentTerminating(AgentEvent ae)
			{
				if(!killed)
				{
					closeProject();
					closePlugins();
					killed = true;
				}
				window.setVisible(false);
				window.dispose();
			}
		}, false);

		
		// Load plugins.
		String	plugins_prop	= Configuration.getConfiguration().getProperty(JCC_PLUGINS);
		if(plugins_prop!=null)
		{
			StringTokenizer tokenizer = new StringTokenizer(plugins_prop, ", ");
			while(tokenizer.hasMoreTokens())
			{
				Class plugin_class=null;
				try
				{
					plugin_class = DynamicURLClassLoader.getInstance().loadClass(tokenizer.nextToken().trim());
					if(!plugin_set.contains(plugin_class))
					{
						IControlCenterPlugin p = (IControlCenterPlugin)plugin_class.newInstance();
						plugins.put(p, null);
						plugin_set.add(plugin_class);
						setStatusText("Plugin loaded successfully: "+p.getName());
					}
				}
				catch(Exception e)
				{
					//e.printStackTrace();
					String text = SUtil.wrapText("Plugin("+plugin_class+") could not be loaded: "+e.getMessage());
					//JOptionPane.showMessageDialog(window, text, "Plugin Error", JOptionPane.INFORMATION_MESSAGE);
					System.out.println(text);
				}
			}
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(!plugins.isEmpty())
				{
					// load project
					String proj = Configuration.getConfiguration().getProperty(JCC_PROJECT);
					if(proj!=null)
					{
						try
						{
							File project = new File(proj);
							openProject(project);//, false);
							window.filechooser.setCurrentDirectory(project.getParentFile());
							window.filechooser.setSelectedFile(project);
							window.setVisible(true);
						}
						catch(Exception e)
						{
							proj = null;
						}
					}
					
					if(proj==null)
					{
						// Use default title, location and plugin
						setCurrentProject(null);
						Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
						window.setSize(new Dimension((int)(dim.width*0.6), ((int)(dim.height*0.6))));
						window.setLocation(SGUI.calculateMiddlePosition(window));
						activatePlugin((IControlCenterPlugin)plugins.keySet().iterator().next());
						window.setVisible(true);
						window.setCenterSplit(-1);
					}
					
					// Print out startup time (for testing purposes).
					if(Configuration.getConfiguration().getProperty(Configuration.STARTTIME)!=null)
					{
						// Use invokeLater to make sure time is calculated after window is initialized.
						SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								agent.getLogger().info("Platform + JCC total start time: "+(System.currentTimeMillis() - Long.parseLong(Configuration.getConfiguration().getProperty(Configuration.STARTTIME)))+"ms.");
							}
						});
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null,
						"No plugins found. The Jadex IDE cannot start. Consult file: "+Configuration.getConfiguration().getFilename()+".",
						"Cannot start Jadex IDE", JOptionPane.ERROR_MESSAGE);
					agent.killAgent();
				}
			}
		});
	}

	//-------- project management --------

	/**
	 *  Close the current project.
	 */
	protected void closeProject()
	{
		// Unconditionally save project (saves platform settings even when no project is open).
		saveProject();

		resetPlugins();

		setCurrentProject(null);
	}
	
	/**
	 *  Set the title with respect to the actual project.
	 *  @param file The project file or null for no project.
	 */
	protected void setCurrentProject(File file)
	{
		this.project = file;
		if(file!=null)
		{
			String fname = file.getName();
			int i = fname.lastIndexOf(".prj");
			if(i>0) fname = fname.substring(0, i);
			setTitle("Project "+fname);
		}
		else
		{
			setTitle("Unnamed project");
		}
	}

	/**
	 *  Open a given project.
	 *  @param close	Close the previous project before opening
	 */
	//protected void openProject(File pd, boolean close)
	protected void openProject(File pd) throws Exception
	{
		/*if(pd==null || !pd.getName().toLowerCase().endsWith(".prj"))
		{
			String failed = SUtil.wrapText("Cannot load project. The project file: \n"+pd.getAbsolutePath()
				+"\n cannot be read");
			JOptionPane.showMessageDialog(window, failed, "Project Error", JOptionPane.ERROR_MESSAGE);
			return;
		}*/
		
		//if(close)
		//	closeProject();

		// Read project properties
		Properties props = new Properties();
		try
		{
			FileInputStream	ps	= new FileInputStream(pd);
			props.load(ps);
			ps.close();
		}
		catch(Exception e)
		{
			String failed = SUtil.wrapText("Could not open project\n\n"+e.getMessage());
			JOptionPane.showMessageDialog(window, failed, "Project Error", JOptionPane.ERROR_MESSAGE);
			throw e;
			//e.printStackTrace();
			//return;
		}

		try
		{
			int w = Integer.parseInt(props.getProperty("wnd0.width"));
			int h = Integer.parseInt(props.getProperty("wnd0.height"));
			int x = Integer.parseInt(props.getProperty("wnd0.x"));
			int y = Integer.parseInt(props.getProperty("wnd0.y"));
			window.setBounds(x, y, w, h);
		}
		catch(Exception e)
		{
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			window.setSize(new Dimension((int)(dim.width*0.6), ((int)(dim.height*0.6))));
			String failed = SUtil.wrapText("Corrupt data in project file\n\n"+e.getMessage());
			JOptionPane.showMessageDialog(window, failed, "Project Error", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}

		try
		{
			window.setVisible(true); // otherwise it will not be extended (jdk5)
			int es = Integer.parseInt(props.getProperty("wnd0.extendedState"));
			window.setExtendedState(es);
		}
		catch(Exception e)
		{
			String failed = SUtil.wrapText("Corrupt data in project file\n\n"+e.getMessage());
			JOptionPane.showMessageDialog(window, failed, "Project Error", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}
		
		// Load the console heights.
		Map chs = window.getConsoleHeights();
		IControlCenterPlugin[] pls = (IControlCenterPlugin[])plugins.keySet()
			.toArray(new IControlCenterPlugin[plugins.size()]);
		for(int i=0; i<pls.length; i++)
		{
			try
			{
				int ch = Integer.parseInt(props.getProperty(pls[i].getName()+".console.height"));
				chs.put(pls[i].getName()+".console.height", new Integer(ch));
			}
			catch(Exception e)
			{
			}
		}
			
		setCurrentProject(pd);

		loadPluginProperties();

		IControlCenterPlugin	plugin	= null;
		String pwnd = props.getProperty("wnd0.perspective");
		if(pwnd!=null)
		{
			plugin	= getPluginForName(pwnd);
		}
		if(plugin==null)
		{
			plugin	= (IControlCenterPlugin)plugins.keySet().iterator().next();
		}
		activatePlugin(plugin);
		
		String splitpos = props.getProperty("wnd0.centersplit");
		if(splitpos!=null)
		{
			window.setCenterSplit(Integer.parseInt(splitpos));
		}
		else
		{
			// -1 for max split
			window.setCenterSplit(-1);
		}
		
		String conon = props.getProperty("wnd0.console_on");
		if(conon!=null)
		{
			window.setConsoleEnabled(new Boolean(conon).booleanValue());
		}
		
		setStatusText("Project opened successfully: "+pd.getName());
	}

	/**
	 *  Save settings of JCC and all plugins in current project.
	 */
	protected void saveProject()
	{		
		if(project!=null)
		{
			// write project properties
			Properties	props	= new Properties();
			props.setProperty("wnd0.perspective", window.getPerspective().getName());
			props.setProperty("wnd0.width", Integer.toString(window.getWidth()));
			props.setProperty("wnd0.height", Integer.toString(window.getHeight()));
			props.setProperty("wnd0.x", Integer.toString(window.getX()));
			props.setProperty("wnd0.y", Integer.toString(window.getY()));
			props.setProperty("wnd0.extendedState", Integer.toString(window.getExtendedState()));
			props.setProperty("wnd0.centersplit", Integer.toString(window.getCenterSplit()));
			props.setProperty("wnd0.console_on", Boolean.toString(window.isConsoleEnabled()));
			
			// Save the console heights.
			Map chs = window.getConsoleHeights();
			String[] keys = (String[])chs.keySet().toArray(new String[chs.size()]);
			for(int i=0; i<keys.length; i++)
			{
				props.setProperty(keys[i], ""+chs.get(keys[i]));
			}
			
			try
			{
				FileOutputStream os = new FileOutputStream(project);
				props.store(os, "Jadex Project Properties");
				os.close();
				setStatusText("Project saved successfully: "+project.getAbsolutePath());
			}
			catch(Exception e)
			{
				String failed = SUtil.wrapText("Could not save data in properties file\n\n"+e.getMessage());
				JOptionPane.showMessageDialog(window, failed, "Properties Error", JOptionPane.ERROR_MESSAGE);
				//e2.printStackTrace();
			}
			
			savePluginProperties();
		}

		try
		{
			Configuration	config	= Configuration.getConfiguration();
			if(project!=null)
			{
				config.setProperty(JCC_PROJECT, project.getAbsolutePath());
			}
			else
			{
				config.setProperty(JCC_PROJECT, null);
			}
			config.persist();
//			if(project==null)
//			{
//				System.err.println("Saved last project: "+project);
//				Thread.dumpStack();
//			}
		}
		catch(IOException e)
		{
			String failed = SUtil.wrapText("Could not save platform settings\n\n"+e);
			JOptionPane.showMessageDialog(window, failed, "Settings Error", JOptionPane.ERROR_MESSAGE);
			//e1.printStackTrace();
		}
	}
	
	//-------- plugin handling --------

	/**
	 *  Reset all active plugins.
	 *  Called when the project is closed.
	 */
	protected void resetPlugins()
	{
		// Reset all plugins, which have a panel associated. 
		for(Iterator it=plugins.keySet().iterator(); it.hasNext(); )
		{
			IControlCenterPlugin plugin = (IControlCenterPlugin)it.next();
			if(plugins.get(plugin)!=null)
			{
				try
				{
					plugin.reset();
				}
				catch(Exception e)
				{
					System.err.println("Exception during reset of JCC-Plug-In "+plugin.getName());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 *  Close all active plugins.
	 *  Called when the JCC exits.
	 */
	protected void closePlugins()
	{
		// Close all plugins, which have a panel associated. 
		for(Iterator it=plugins.keySet().iterator(); it.hasNext(); )
		{
			IControlCenterPlugin plugin = (IControlCenterPlugin)it.next();
			if(plugins.get(plugin)!=null)
			{
				try
				{
					plugin.shutdown();
				}
				catch(Exception e)
				{
					System.err.println("Exception while closing JCC-Plug-In "+plugin.getName());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 *  Load the properties for all active plugins.
	 *  Called when a project has been opened.
	 */
	protected void	loadPluginProperties()
	{
		// Load properties of all plugins, which have a panel associated. 
		for(Iterator it=plugins.keySet().iterator(); it.hasNext(); )
		{
			IControlCenterPlugin plugin = (IControlCenterPlugin)it.next();
			if(plugins.get(plugin)!=null)
			{
				loadPluginProperties(plugin);
			}
		}
	}

	/**
	 *  Load properties for a given plugin.
	 */
	protected void loadPluginProperties(IControlCenterPlugin plugin)
	{
		File parent = project.getParentFile();
		Properties	props	= new Properties();
		File pluginFile=new File(parent, project.getName()+"."+plugin.getName()+".properties");
		if(pluginFile.canRead())
		{
			try
			{
				FileInputStream is = new FileInputStream(pluginFile);
				props.clear();
				props.load(is);
				is.close();
				plugin.setProperties(props);
			}
			catch(Exception e)
			{
				//e.printStackTrace();
				plugin.reset();
				String failed = SUtil.wrapText("Error during loading settings of plugin "+plugin.getName()+"\n\n"+e.getMessage());
				JOptionPane.showMessageDialog(window, failed, "Plugin Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{
			plugin.reset();
		}
	}

	/**
	 *  Save the properties for all active plugins.
	 *  Called when a project has been opened.
	 */
	protected void	savePluginProperties()
	{
		File parent = project.getParentFile();
		Properties	props	= new Properties();
		// Load properties of all plugins, which have a panel associated. 
		for(Iterator it=plugins.keySet().iterator(); it.hasNext(); )
		{
			IControlCenterPlugin plugin = (IControlCenterPlugin)it.next();
			if(plugins.get(plugin)!=null)
			{
				try
				{
					props.clear();
					plugin.getProperties(props);
					File pluginFile=new File(parent, project.getName()+"."+plugin.getName()+".properties");
					pluginFile.createNewFile();
					FileOutputStream os = new FileOutputStream(pluginFile);
					props.store(os, "Plugin " + plugin.getName()+" Properties");
					os.close();
				}
				catch(Exception e)
				{
					String failed = SUtil.wrapText("Error during saving settings of plugin "+plugin.getName()+"\n\n"+e.getMessage());
					JOptionPane.showMessageDialog(window, failed, "Plugin Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 *  Find a plugin by name.
	 *  @return null, when plugin is not found.
	 */
	protected IControlCenterPlugin getPluginForName(String name)
	{
		for(Iterator it=plugins.keySet().iterator(); it.hasNext(); )
		{
			IControlCenterPlugin plugin = (IControlCenterPlugin)it.next();
			if(name.equals(plugin.getName()))
				return plugin;
		}
		return null;
	}

	/**
	 *  Activate a plugin.
	 */
	protected void	activatePlugin(IControlCenterPlugin plugin)
	{
		window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
		// Init the plugin, when not yet inited.
		if(plugins.get(plugin)==null)
		{
			try
			{
				plugin.init(this);
				JComponent	comp	= plugin.getView();
	            plugins.put(plugin, comp);
	            
				// Project may be null, when activating default project	
	        	if(project!=null)
					loadPluginProperties(plugin);
	            
	            // Todo: move this code to controlcenterwindow!?
				if(plugin.getHelpID()!=null)
	            	GuiProperties.setupHelp(comp, plugin.getHelpID());

				window.content.add(comp, plugin.getName());
	            window.setPerspective(plugin);
				
	        	setStatusText("Plugin activated successfully: "+plugin.getName());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				String failed = SUtil.wrapText("Error during init of plugin "+plugin.getName()+"\n\n"+e);
				JOptionPane.showMessageDialog(window, failed, "Plugin Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{
			window.setPerspective(plugin);
		}

		window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	//-------- IControlCenter interface --------

	/**
	 *  Listen for changes to the list of known agents.
	 */
	public void addAgentListListener(IAgentListListener listener)
	{
		agentlist.addListener(listener);
	}

	/**
	 *  Listen for incoming messages.
	 */
	public void addMessageListener(IMessageListener listener)
	{
		msglisteners.add(listener);
	}

	/**
	 *  Create a new agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void createAgent(String type, String name, String configname, Map arguments)
	{
		try
		{
			final IGoal start = agent.createGoal("ams_create_agent");
			start.getParameter("type").setValue(type);
			start.getParameter("name").setValue(name);
			if(configname!=null && configname.length()!=0)
			{
				start.getParameter("configuration").setValue(configname);
			}
			if(arguments!=null)
			{
				start.getParameter("arguments").setValue(arguments);
			}
			start.addGoalListener(new IGoalListener()
			{
				public void goalAdded(AgentEvent ae)
				{
				}
				
				public void goalFinished(AgentEvent ae)
				{
					if(start.isSucceeded())
					{
						AgentIdentifier aid = (AgentIdentifier)start.getParameter("agentidentifier").getValue();
						setStatusText("Started agent: "+aid.getLocalName());		
					};
				}
			}, false);
			
			dispatchGoal(start, "Problem Starting Agent", "Agent could not be started.");
		}
		catch(AgentDeathException ex)
		{
			// Thrown when killing myself -> ignore.
		}
	}

	/**
	 *  Kill an agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void killAgent(final AgentIdentifier name)
	{
		try
		{
			final IGoal kill = agent.getGoalbase().createGoal("ams_destroy_agent");
			kill.getParameter("agentidentifier").setValue(name);
			kill.addGoalListener(new IGoalListener()
			{
				public void goalAdded(AgentEvent ae)
				{
				}
				
				public void goalFinished(AgentEvent ae)
				{
					if(kill.isSucceeded())
						setStatusText("Killed agent: "+name.getLocalName());		
				}
			}, false);
			if(name.equals(agent.getAgentIdentifier()))
			{
				// Do not display error message when killing self.
				agent.dispatchTopLevelGoal(kill);
			}
			else
			{
				dispatchGoal(kill, "Problem Killing Agent", "Agent could not be killed.");
			}
		}
		catch(AgentDeathException ex)
		{
			// Thrown when killing myself -> ignore.
		}
	}
	
	/**
	 *  Suspend an agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void suspendAgent(AgentIdentifier name)
	{
		try
		{
			final IGoal suspend = agent.getGoalbase().createGoal("ams_suspend_agent");
			suspend.getParameter("agentidentifier").setValue(name);
			suspend.addGoalListener(new IGoalListener()
			{
				public void goalAdded(AgentEvent ae)
				{
				}
				
				public void goalFinished(AgentEvent ae)
				{
					if(suspend.isSucceeded())
						setStatusText("Suspended agent: "+agent.getAgentName());		
				}
			}, false);
			dispatchGoal(suspend, "Problem Suspending Agent", "Agent could not be suspended.");
		}
		catch(AgentDeathException ex)
		{
			// Thrown when killing myself -> ignore.
		}
	}
	
	/**
	 *  Resume an agent on the platform.
	 *  Any errors will be displayed in a dialog to the user.
	 */
	public void resumeAgent(AgentIdentifier name)
	{
		try
		{
			final IGoal resume = agent.getGoalbase().createGoal("ams_resume_agent");
			resume.getParameter("agentidentifier").setValue(name);
			resume.addGoalListener(new IGoalListener()
			{
				public void goalAdded(AgentEvent ae)
				{
				}
				
				public void goalFinished(AgentEvent ae)
				{
					if(resume.isSucceeded())
						setStatusText("Resumed agent: "+agent.getAgentName());		
				}
			}, false);
			dispatchGoal(resume, "Problem Resuming Agent", "Agent could not be resumed.");
		}
		catch(AgentDeathException ex)
		{
			// Thrown when killing myself -> ignore.
		}
	}

	/**
	 *  Get the external access interface.
	 *  @return the external agent access.
	 */
	public IExternalAccess getAgent()
	{
		return agent;
	}

	/**
	 *  Set a text to be displayed in the status bar.
	 *  The text will be removed automatically after
	 *  some delay (or replaced by some other text).
	 */
	public void setStatusText(String text)
	{
		window.getStatusBar().setText(text);
	}
	
	/**
	 *  Add a component to the status bar.
	 *  @param id	An id for later reference.
	 *  @param comp	An id for later reference.
	 */
	public void	addStatusComponent(Object id, JComponent comp)
	{
		window.getStatusBar().addStatusComponent(id, comp);
	}

	/**
	 *  Remove a previously added component from the status bar.
	 *  @param id	The id used for adding the component.
	 */
	public void	removeStatusComponent(Object id)
	{
		window.getStatusBar().removeStatusComponent(id);
	}
	
	/**
	 *  Show the console.
	 *  @param show True, if console should be shown.
	 */
	public void showConsole(boolean show)
	{
		window.showConsole(show);
	}
	
	/**
	 *  Test if console is shown.
	 *  @return True, if shown.
	 */
	public boolean isConsoleShown()
	{
		return window.isConsoleShown();
	}
	
	/**
	 *  Set the console height.
	 *  @param height The console height.
	 */
	public void setConsoleHeight(int height)
	{
		window.setConsoleHeight(height);
	}
	
	/**
	 *  Get the console height.
	 *  @return The console height.
	 */
	public int getConsoleHeight()
	{
		return window.getConsoleHeight();
	}
	
	/**
	 *  Set the console enable state.
	 *  @param enabled The enabled state.
	 */
	public void setConsoleEnabled(boolean enabled)
	{
		window.setConsoleEnabled(enabled);
	}
	
	/**
	 *  Test if the console is enabled.
	 *  @return True, if enabled.
	 */
	public boolean isConsoleEnabled()
	{
		return window.isConsoleEnabled();
	}
	
	//-------- helper methods --------

	/**
	 *  Dispatch a goal and display errors (if any).
	 *  @param goal	The goal to dispatch.
	 *  @param errortitle	The title to use for an error dialog.
	 *  @param errormessage	An optional error message displayed before the exception.
	 */
	protected void dispatchGoal(IGoal goal, final String errortitle, final String errormessage)
	{
		goal.addGoalListener(new IGoalListener()
		{
			public void goalFinished(AgentEvent ae)
			{
				IGoal	goal	= (IGoal)ae.getSource();
				if(!goal.isSucceeded())
				{
					String	text;
					String exmsg = goal.getException()==null? null: goal.getException().getMessage();
					if(errormessage==null && exmsg==null)
					{
						text	= errortitle;
					}
					else if(errormessage!=null && exmsg==null)
					{
						text	= errormessage;
					}
					else if(errormessage==null && exmsg!=null)
					{
						text	= "" + goal.getException().getMessage();
					}
					else //if(errormessage!=null && goal.getException()!=null)
					{
						text	= errormessage + "\n" + exmsg;
					}
					JOptionPane.showMessageDialog(window, SUtil.wrapText(text), errortitle, JOptionPane.ERROR_MESSAGE);
				}
				goal.removeGoalListener(this);
			}
			
			public void goalAdded(AgentEvent ae)
			{
			}
		}, false);
		agent.dispatchTopLevelGoal(goal);
	}

	/**
	 *  Process a received message.
	 *  Called by MailPlan.
	 */
	protected void processMessage(IMessageEvent me)
	{
		for(int i=0; i<msglisteners.size(); i++)
		{
			IMessageListener msgl = (IMessageListener)msglisteners.get(i);
			if(msgl.processMessage(me)) 
				break;
		}
	}

	/**
	 * Set the title of the window.
	 * @param t The title.
	 */
	protected void setTitle(String t)
	{
		String release = Configuration.getConfiguration().getReleaseNumber();
		String date = Configuration.getConfiguration().getReleaseDate();
		window.setTitle("Jadex Control Center "+release+" ("+date+"): "+t);
	}

	/**
	 *  Informs the window if it should dispose its resources.
	 *  @return true if the agent has been killed.
	 */
	protected boolean exit()
	{
		if(killed)
			return true;

		// When no project is open, ask to save project settings.
		if(project==null)
		{
			String	msg	= SUtil.wrapText("Do you want to save the current settings as a new project?");
			
			int o = JOptionPane.showConfirmDialog(window, msg,
				"Save Settings", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if(JOptionPane.YES_OPTION==o)
			{
				window.saveProjectAs();
			}
		}
		
		String	jccexit	= Configuration.getConfiguration().getProperty(Configuration.JCC_EXIT);
		
		int choice;
		RememberOptionMessage msg = null;
		if(jccexit==null || jccexit.equals(Configuration.JCC_EXIT_ASK))
		{
			msg = new RememberOptionMessage("You requested to close the Jadex GUI.\n " +
				"Do you also want to shutdown the local platform?\n");
			choice = JOptionPane.showConfirmDialog(window, msg, "Exit Question", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		}
		else if(jccexit.equals(Configuration.JCC_EXIT_KEEP))
		{
			choice	= JOptionPane.NO_OPTION;
		}
		else //if(jccexit.equals(Configuration.JCC_EXIT_SHUTDOWN))
		{
			choice	= JOptionPane.YES_OPTION;
		}

		if(JOptionPane.YES_OPTION==choice)
		{
			// Save settings if wanted
			if(msg!=null && msg.isRemember())
				Configuration.getConfiguration().setProperty(Configuration.JCC_EXIT, 
					Configuration.JCC_EXIT_SHUTDOWN);

			// todo: persist needs to much disk space per model?!
			/*try
			{
				SXML.persistModelCache();
			}
			catch(IOException e)
			{
				String text = SUtil.wrapText("Could not save model cache: "+e.getMessage());
				JOptionPane.showMessageDialog(window, text, "Cache problem", JOptionPane.ERROR_MESSAGE);
			}*/
			closeProject();
			closePlugins();
			killed = true;
			try
			{
				agent.dispatchTopLevelGoal(agent.createGoal("ams_shutdown_platform"));
			}
			catch(GoalFailureException ex)
			{
				String text = SUtil.wrapText("Could not kill platform: "+ex.getMessage());
				JOptionPane.showMessageDialog(window, text, "Platform Shutdown Problem", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if(JOptionPane.NO_OPTION==choice)
		{
			// Save settings if wanted
			if(msg!=null && msg.isRemember())
				Configuration.getConfiguration().setProperty(Configuration.JCC_EXIT, 
					Configuration.JCC_EXIT_KEEP);

			closeProject();
			closePlugins();
			killed = true;
			agent.killAgent();
		}
		// else CANCEL

		return killed;
	}
	
	/**
	 *  Get the JCC window.
	 */
	public JFrame	getWindow()
	{
		return window;
	}

	/**
	 *  Check if a project is active.
	 */
	protected boolean hasProject()
	{
		return project!=null;
	}

	/**
	 *  Get all plugins.
	 *  As the plugins may not be inited only safe methodes
	 *  such as getName() and getIcon() may be called.
	 */
	public IControlCenterPlugin[] getPlugins()
	{
		return (IControlCenterPlugin[])plugins.keySet().toArray(new IControlCenterPlugin[plugins.size()]);
	}	
}

