package jadex.tools.starter;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.model.*;
import jadex.runtime.GoalFailureException;
import jadex.tools.common.*;
import jadex.tools.common.jtreetable.DefaultTreeTableNode;
import jadex.tools.common.jtreetable.TreeTableNodeType;
import jadex.tools.common.modeltree.FileNode;
import jadex.tools.common.modeltree.INodeAction;
import jadex.tools.common.modeltree.ModelExplorer;
import jadex.tools.common.modeltree.RootNode;
import jadex.tools.common.plugin.IAgentListListener;
import jadex.tools.jcc.AbstractJCCPlugin;
import jadex.util.SGUI;
import jadex.util.SUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

/**
 *  The starter plugin.
 */
public class StarterPlugin extends AbstractJCCPlugin implements  IAgentListListener
{
	//-------- constants --------

	/**
	 * The image icons.
	 */
	protected static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"resume_agent", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_agent_big.png"),
		"suspend_agent", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_agent_szzz_big.png"),
		"kill_agent", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_killagent.png"),
		"kill_platform", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_killplatform.png"),
		"starter", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_starter.png"),
		"starter_sel", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_starter_sel.png"),
//		"scanning_on",	SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_agent_testcheck.gif")
		"scanning_on",	SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_refresh_anim.gif"),
//		"scanning_off",	SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_refresh_anim00.png")
		//"jadexdoc",	SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_jadexdoc.png"),
		"start_agent",	SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/start.png"),
		"agent_suspended", SGUI.makeIcon(AgentTreeTable.class, "/jadex/tools/common/images/new_agent_szzz.png")
	});

	protected static final java.io.FileFilter ADF_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || SXML.isJadexFilename(pathname.getName());
		}
	};
	
	protected static final java.io.FileFilter AGENT_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || SXML.isAgentFilename(pathname.getName());
		}
	};
	
	protected static final java.io.FileFilter CAPABILITY_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || SXML.isCapabilityFilename(pathname.getName());
		}
	};

	//-------- attributes --------

	/** The starter panel. */
	private StarterPanel spanel;

	/** The panel showing the classpath models. */
	private ModelExplorer mpanel;

	/** The agent instances in a tree. */
	private AgentTreeTable agents;

	/** A split panel. */
	private JSplitPane lsplit;

	/** A split panel. */
    private JSplitPane csplit;

	//-------- methods --------
	
	/**
	 *  Get the name.
	 *  @return The name.
	 *  @see jadex.tools.common.plugin.IControlCenterPlugin#getName()
	 */
	public String getName()
	{
		return "Starter";
	}

	/**
	 *  Create tool bar.
	 *  @return The tool bar.
	 */
	public JComponent[] createToolBar()
	{
		JComponent[] ret = new JComponent[6];
		JButton b;

		b = new JButton(mpanel.ADD_PATH);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[0] = b;
		
		b = new JButton(mpanel.REMOVE_PATH);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[1] = b;
		
		b = new JButton(mpanel.REFRESH_ALL);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[2] = b;
		
		JSeparator	separator	= new JToolBar.Separator();
		separator.setOrientation(JSeparator.VERTICAL);
		ret[3] = separator;
		
		/*b = new JButton(GENERATE_JADEXDOC);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		bar.add(b);
		
		separator = new JToolBar.Separator();
		separator.setOrientation(JSeparator.VERTICAL);
		bar.add(separator);*/
		
		b = new JButton(KILL_AGENT);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[4] = b;
		
		b = new JButton(KILL_PLATFORM);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[5] = b;

		return ret;
	}
	
	/**
	 *  Create menu bar.
	 *  @return The menu bar.
	 */
	public JMenu[] createMenuBar()
	{
		return mpanel.createMenuBar();
	}
	
	/**
	 *  Create main panel.
	 *  @return The main panel.
	 */
	public JComponent createView()
	{
		csplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		csplit.setOneTouchExpandable(true);

		lsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		lsplit.setOneTouchExpandable(true);
		lsplit.setResizeWeight(0.7);

		JLabel	refreshcomp	= new JLabel(icons.getIcon("scanning_on"));
		refreshcomp.setToolTipText("Loading/checking agent models.");
		mpanel = new ModelExplorer(getJCC(), new RootNode(ADF_FILTER, 
			new RootNodeFunctionality()), refreshcomp, null, new String[]{"ADFs", "Agents", "Capabilities"}, 
			new java.io.FileFilter[]{ADF_FILTER, AGENT_FILTER, CAPABILITY_FILTER}
		);
		mpanel.setAction(FileNode.class, new INodeAction()
		{
			public void validStateChanged(TreeNode node, boolean valid)
			{
				String file1 = ((FileNode)node).getFile().getAbsolutePath();
				String file2 = spanel.getFilename();
				//System.out.println(file1+" "+file2);
				if(file1!=null && file1.equals(file2))
				{
					spanel.reloadModel(file1);
				}
			}
		});
		mpanel.setPopupBuilder(new PopupBuilder(new Object[]{new StartAgentMenuItemConstructor(), mpanel.ADD_PATH,
			mpanel.REMOVE_PATH, mpanel.REFRESH, mpanel.REFRESH_ALL}));
		mpanel.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				Object	node = mpanel.getLastSelectedPathComponent();
				if(node instanceof FileNode)
				{
					// Models have to be loaded with absolute path.
					// An example to facilitate understanding:
					// root
					//  +-classes1
					//  |  +- MyAgent.agent.xml
					//  +-classes2
					//  |  +- MyAgent.agent.xml

					String model = ((FileNode)node).getFile().getAbsolutePath();
					if(SXML.isJadexFilename(model))
					{
						loadModel(model);
					}
				}
			}
		});
		MouseListener ml = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				int row = mpanel.getRowForLocation(e.getX(), e.getY());
				if(row != -1)
				{
					if(e.getClickCount() == 2)
					{
						Object	node = mpanel.getLastSelectedPathComponent();
						if(node instanceof FileNode)
						{
							mpanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							String type = ((FileNode)node).getFile().getAbsolutePath();
							if(SXML.isAgentFilename(type))
								getJCC().createAgent(type, null, null, null);
							mpanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
      		}
  		};
  		mpanel.addMouseListener(ml);


		lsplit.add(new JScrollPane(mpanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		agents = new AgentTreeTable();
		agents.setMinimumSize(new Dimension(0, 0));
		agents.getTreetable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// Change agent node type to enable suspended icon for agents. 
		agents.addNodeType(new TreeTableNodeType(AgentTreeTable.NODE_AGENT, 
			new Icon[0], new String[]{"name", "address"}, new String[]{"Name", "Address"})
		{
			public Icon selectIcon(Object value)
			{
				Icon	ret;
				AMSAgentDescription ad = (AMSAgentDescription)((DefaultTreeTableNode)value).getUserObject();
				if(AMSAgentDescription.STATE_SUSPENDED.equals(ad.getState()))
				{
					ret = StarterPlugin.icons.getIcon("agent_suspended");
				}
				else
				{
					ret	= AgentTreeTable.icons.getIcon(AgentTreeTable.NODE_AGENT);
				}
				//System.out.println(value+" "+ad.getState());
				return ret;
			}
		});
		agents.getNodeType(AgentTreeTable.NODE_AGENT).addPopupAction(KILL_AGENT);
		agents.getNodeType(AgentTreeTable.NODE_AGENT).addPopupAction(SUSPEND_AGENT);
		agents.getNodeType(AgentTreeTable.NODE_AGENT).addPopupAction(RESUME_AGENT);
		agents.getNodeType(AgentTreeTable.NODE_PLATFORM).addPopupAction(KILL_PLATFORM);
		agents.getTreetable().getSelectionModel().setSelectionInterval(0, 0);

		lsplit.add(agents);
		lsplit.setDividerLocation(300);

		csplit.add(lsplit);
		spanel = new StarterPanel(this);
		csplit.add(spanel);
		csplit.setDividerLocation(180);
            			
		jcc.addAgentListListener(this);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.adjustColumnWidths();
			}
		});

		return csplit;
	}
	
	/**
	 * @return the starter icon
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#getToolIcon()
	 */
	public Icon getToolIcon(boolean selected)
	{
		return selected? icons.getIcon("starter_sel"): icons.getIcon("starter");
	}

	/**
	 * Load the properties.
	 * @param props
	 */
	public void setProperties(Properties props)
	{
		// try to load arguments from properties.

		mpanel.setProperties(props);
		spanel.setProperties(props);

		try
		{
			int lsdl = Integer.parseInt(props.getProperty("leftsplit.location"));
			lsplit.setDividerLocation(lsdl);
		}
		catch(Exception e)
		{ 
		}
		try
		{
			int msdl = Integer.parseInt(props.getProperty("mainsplit.location"));
			csplit.setDividerLocation(msdl);
		}
		catch(Exception e)
		{
		}
	}

	/**
	 * Save the properties.
	 * @param props
	 */
	public void getProperties(Properties props)
	{
		mpanel.getProperties(props);
		spanel.getProperties(props);

		//props.setProperty("details", ""+detailson);
		props.put("leftsplit.location", ""+lsplit.getDividerLocation());
		props.put("mainsplit.location", ""+csplit.getDividerLocation());
	}
	
	/**
	 *  Load a model.
	 *  @param model The model name.
	 */
	protected void loadModel(final String model)
	{
		getCpanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		getStarterPanel().loadModel(model);
		getCpanel().setCursor(Cursor.getDefaultCursor());
	}

	/**
	 *  Get the central panel.
	 *  @return cpanel The panel.
	 */
	protected Component getCpanel()
	{
		return csplit;
	}

	/**
	 *  Get the starter panel.
	 * @return the starter panel
	 */
	protected StarterPanel getStarterPanel()
	{
		return spanel;
	}

	/**
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#reset()
	 */
	public void reset()
	{
		try
		{
			mpanel.reset();
			spanel.reset();
		}
		catch(Exception e)
		{
		}
	}

	/**
	 *  Called when the agent is closed.
	 */
	public void	shutdown()
	{
		mpanel.close();
	}

	/**
	 *  Action for suspending an agent.
	 */
	final AbstractAction SUSPEND_AGENT = new AbstractAction("Suspend agent", icons.getIcon("suspend_agent"))
	{
		public void actionPerformed(ActionEvent e)
		{
			TreePath[] paths = agents.getTreetable().getTree().getSelectionPaths();
			for(int i=0; paths!=null && i<paths.length; i++) 
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)paths[i].getLastPathComponent();
				if(node!=null && node.getUserObject() instanceof AMSAgentDescription)
				{
					jcc.suspendAgent(((AMSAgentDescription)node.getUserObject()).getName());
				}
			}
		}

		public boolean isEnabled()
		{
			TreePath[] paths = agents.getTreetable().getTree().getSelectionPaths();
			boolean ret = paths!=null;
			for(int i=0; ret && paths!=null && i<paths.length; i++) 
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)paths[i].getLastPathComponent();
				if(node!=null && node.getUserObject() instanceof AMSAgentDescription)
				{
					ret &= AMSAgentDescription.STATE_ACTIVE.equals(
						((AMSAgentDescription)node.getUserObject()).getState());
				}
			}
			return ret;
		}
	};
	
	/**
	 *  Action for resuming an agent.
	 */
	final AbstractAction RESUME_AGENT = new AbstractAction("Resume agent", icons.getIcon("resume_agent"))
	{
		public void actionPerformed(ActionEvent e)
		{
			TreePath[] paths = agents.getTreetable().getTree().getSelectionPaths();
			for(int i=0; paths!=null && i<paths.length; i++)
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)paths[i].getLastPathComponent();
				if(node!=null && node.getUserObject() instanceof AMSAgentDescription)
				{
					jcc.resumeAgent(((AMSAgentDescription)node.getUserObject()).getName());
				}
			}
		}
		
		public boolean isEnabled()
		{
			TreePath[] paths = agents.getTreetable().getTree().getSelectionPaths();
			boolean ret = paths!=null;
			for(int i=0; ret && paths!=null && i<paths.length; i++) 
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)paths[i].getLastPathComponent();
				if(node!=null && node.getUserObject() instanceof AMSAgentDescription)
				{
					ret &= AMSAgentDescription.STATE_SUSPENDED.equals(
						((AMSAgentDescription)node.getUserObject()).getState());
				}
			}
			return ret;
		}
	};
	
	/**
	 *  Action for killing an agent.
	 */
	final AbstractAction KILL_AGENT = new AbstractAction("Kill agent", icons.getIcon("kill_agent"))
	{
		public void actionPerformed(ActionEvent e)
		{
			TreePath[] paths = agents.getTreetable().getTree().getSelectionPaths();
			for(int i=0; paths!=null && i<paths.length; i++) 
			{
				DefaultTreeTableNode node = (DefaultTreeTableNode)paths[i].getLastPathComponent();
				if(node!=null && node.getUserObject() instanceof AMSAgentDescription)
				{
					jcc.killAgent(((AMSAgentDescription)node.getUserObject()).getName());
				}
			}
		}
	};

	/**
	 *  Action for killing the platform.
	 */
	final AbstractAction KILL_PLATFORM = new AbstractAction("Kill platform", icons.getIcon("kill_platform"))
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				jcc.getAgent().dispatchTopLevelGoal(jcc.getAgent().createGoal("ams_shutdown_platform"));
			}
			catch(GoalFailureException ex)
			{
				String text = SUtil.wrapText("Could not kill platform: "+ex.getMessage());
				JOptionPane.showMessageDialog(SGUI.getWindowParent(spanel), text, "Platform Shutdown Problem", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	};

	/**
	 *  Called when an agent has died.
	 *  @param ad The agent description.
	 */
	public void agentDied(final AMSAgentDescription ad)
	{
		// Update components on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.removeAgent(ad);
			}
		});
	}

	/**
	 *  Called when an agent is born.
	 *  @param ad the agent description.
	 */
	public void agentBorn(final AMSAgentDescription ad)
	{
		// Update components on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.addAgent(ad);
			}
		});
	}
	
	/**
	 *  Called when an agent changed.
	 *  @param ad the agent description.
	 */
	public void agentChanged(final AMSAgentDescription ad)
	{
		// Update components on awt thread.
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				agents.updateAgent(ad);
			}
		});
	}

	/** 
	 * @return the help id of the perspective
	 * @see jadex.tools.jcc.AbstractJCCPlugin#getHelpID()
	 */
	public String getHelpID()
	{
		return "tools.starter";
	}

	//-------- starter specific tree actions --------

	/**
	 *  Dynamically create a new menu item structure for starting agents.
	 */
	class StartAgentMenuItemConstructor implements IMenuItemConstructor
	{
		/**
		 *  Get or create a new menu item (struture).
		 *  @return The menu item (structure).
		 */
		public JMenuItem getMenuItem()
		{
			JMenuItem ret = null;

			if(isEnabled())
			{
				TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
				if(node instanceof FileNode)
				{
					final String type = ((FileNode)node).getFile().getAbsolutePath();
					if(SXML.isAgentFilename(type) && ((FileNode)node).isValid())
					{
						try
						{
							IMBDIAgent model	= SXML.loadAgentModel(type, null);
							final IMConfiguration[] inistates = model.getConfigurationbase().getConfigurations();
							if(inistates.length>1)
							{
								JMenu re = new JMenu("Start Agent");
								re.setIcon(icons.getIcon("start_agent"));
								for(int i=0; i<inistates.length; i++)
								{
									final String config = inistates[i].getName();
									JMenuItem me = new JMenuItem(config);
									re.add(me);
									me.addActionListener(new ActionListener()
									{
										public void actionPerformed(ActionEvent e)
										{
											getJCC().createAgent(type, null, config, null);
										}
									});
									me.setToolTipText("Start in configuration: "+config);

								}
								ret = re;
								ret.setToolTipText("Start agent in selectable configuration");
							}
							else
							{
								if(inistates.length==1)
								{
									ret = new JMenuItem("Start Agent ("+inistates[0].getName()+")");
									ret.setToolTipText("Start agent in configuration:"+inistates[0].getName());
								}
								else
								{
									ret = new JMenuItem("Start Agent");
									ret.setToolTipText("Start agent without explicit initial state");
								}
								ret.setIcon(icons.getIcon("start_agent"));
								ret.addActionListener(new ActionListener()
								{
									public void actionPerformed(ActionEvent e)
									{
										getJCC().createAgent(type, null, null, null);
									}
								});
							}
						}
						catch(IOException e)
						{
							// NOP
						}
					}
				}
			}

			return ret;
		}

		/**
		 *  Test if action is available in current context.
		 *  @return True, if available.
		 */
		public boolean isEnabled()
		{
			boolean ret = false;
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				String type = ((FileNode)node).getFile().getAbsolutePath();
				if(SXML.isAgentFilename(type))
					ret = true;
			}
			return ret;
		}
	}
}

