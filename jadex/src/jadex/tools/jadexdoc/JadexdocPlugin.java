package jadex.tools.jadexdoc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIDefaults;
import javax.swing.tree.TreeNode;

import jadex.model.SXML;
import jadex.tools.common.PopupBuilder;
import jadex.tools.common.ToolTipAction;
import jadex.tools.common.modeltree.DirNode;
import jadex.tools.common.modeltree.FileNode;
import jadex.tools.common.modeltree.ModelExplorer;
import jadex.tools.common.modeltree.RootNode;
import jadex.tools.common.plugin.IControlCenter;
import jadex.tools.jcc.AbstractJCCPlugin;
import jadex.tools.starter.RootNodeFunctionality;
import jadex.tools.starter.StarterPlugin;
import jadex.util.SGUI;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

/**
 *  The Jadexdoc plugin.
 *  // todo: make ModelExplorer and file selctors allowing also to add/remove .java files.
 */
public class JadexdocPlugin extends AbstractJCCPlugin
{
	//-------- constants --------

	/**
	 * The image icons.
	 */
	protected static final UIDefaults icons = new UIDefaults(new Object[]
	{
		"scanning_on",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_refresh_anim.gif"),
		"jadexdoc",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_jadexdoc.png"),
		"jadexdoc_sel", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_jadexdoc_sel.png"),
		"add_package",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_add_package.png"),
		"remove_package",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_remove_package.png"),
		"add_all_packages",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_add_all_packages.png"),
		"remove_all_packages",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_remove_all_packages.png"),
		"add_file",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_add_agent_big.png"),
		"remove_file",	SGUI.makeIcon(JadexdocPlugin.class, "/jadex/tools/common/images/new_remove_agent_big.png"),
	});
	
	/**
	 *  The file filter for the model tree.
	 */
	protected static final java.io.FileFilter ADF_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || SXML.isJadexFilename(pathname.getName());
		}
	};
	
	/**
	 *  The file filter for the model tree.
	 */
	protected static final java.io.FileFilter ADF_AND_JAVA_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || SXML.isJadexFilename(pathname.getName())
				|| pathname.getName().endsWith(".java");
		}
	};
	
	/**
	 *  The file filter for the model tree.
	 */
	protected static final java.io.FileFilter JAVA_FILTER = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory() || pathname.getName().endsWith(".java");
		}
	};

	//-------- attributes --------

	/** The jadexdoc panel. */
	protected JadexdocPanel jpanel;

	/** The panel showing the classpath models. */
	protected ModelExplorer mpanel;

	/** A split panel. */
    protected JSplitPane csplit;

    //-------- constructors --------
	
	/** 
	 *  Initialize the plugin.
	 */
	public void init(IControlCenter jcc)
	{
		super.init(jcc);
	}
	
	//-------- methods --------
	
	/**
	 *  Get the name.
	 *  @return The name.
	 *  @see jadex.tools.common.plugin.IControlCenterPlugin#getName()
	 */
	public String getName()
	{
		return "Jadexdoc";
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

		b = new JButton(mpanel.REFRESH);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[2] = b;
		
		JSeparator	separator	= new JToolBar.Separator();
		separator.setOrientation(JSeparator.VERTICAL);
		ret[3] = separator;
		
		b = new JButton(ADD_ALL_PACKAGES);
		b.setBorder(null);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setEnabled(true);
		ret[4] = b;
		
		b = new JButton(REMOVE_ALL_PACKAGES);
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

		JLabel	refreshcomp	= new JLabel(icons.getIcon("scanning_on"));
		refreshcomp.setToolTipText("Loading/checking agent models.");
		mpanel = new ModelExplorer(getJCC(), new RootNode(ADF_FILTER, new RootNodeFunctionality()), 
			refreshcomp, null, new String[]{"ADFs", "ADFs and Javas", "Javas"}, 
			new java.io.FileFilter[]{ADF_FILTER, ADF_AND_JAVA_FILTER, JAVA_FILTER});
		
		mpanel.setPopupBuilder(new PopupBuilder(new Object[]{mpanel.ADD_PATH,
			mpanel.REMOVE_PATH, mpanel.REFRESH, mpanel.REFRESH_ALL, 
			ADD_FILE, REMOVE_FILE, ADD_PACKAGE, REMOVE_PACKAGE, ADD_ALL_PACKAGES, REMOVE_ALL_PACKAGES}));
		
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
							List all = findRecursivePackages((FileNode)node);
							for(int i=0; i<all.size(); i++)
								jpanel.addEntry((String)all.get(i));
						}
					}
				}
      		}
  		};
  		mpanel.addMouseListener(ml);

		csplit.add(new JScrollPane(mpanel));
		jpanel = new JadexdocPanel(this, null);
		csplit.add(jpanel);
		csplit.setDividerLocation(180);
            			
		return csplit;
	}
	
	/**
	 *  Find all packages recursively starting with
	 *  the given node.
	 *  @param node The start node.
	 */
	protected List findRecursivePackages(FileNode node)
	{
		List ret = SCollection.createArrayList();
		
		// Do not allow adding the root dir (e.g. classes or src)
		if(node.getParent()!=node.getRootNode())
		{
			FileNode tmp = (FileNode)node;
			FileNode[] nodes = getAllChildren(tmp);
			ret.add(getEntryName(tmp));
			for(int i=0; i<nodes.length; i++)
			{
				// Only add packages recursively
				if(nodes[i] instanceof DirNode)
				{
					ret.add(getEntryName(nodes[i]));
				}
			}
		}
		
		return ret;
	}
	
	/**
	 *  Get the package or file name for an entry.
	 *  Package names are in the form: package1.package2.(...)
	 *  File names are in the form: C:/folder1/filename1
	 */
	protected String getEntryName(FileNode node)
	{
		String name = ((FileNode)node).getFile().getAbsolutePath();
		// Agent or capability artefacts can be added directly
		
		if(!SXML.isJadexFilename(name) && !SUtil.isJavaSourceFilename(name))
		{
			// Determine the package or file name 
			// relativ to the package base
			FileNode tmp = (FileNode)node;
			name = tmp.getFile().getName();
			while(tmp.getParent() instanceof FileNode
				&& tmp.getParent().getParent()!=tmp.getRootNode())
			{
				tmp = (FileNode)tmp.getParent();
				name = tmp.getFile().getName()+ "." +name;
			}
		}
		return name;
	}
	
	/**
	 *  Get all chilren for a specific node.
	 *  @param node The node.
	 *  @return All children (also indirect) of the node.
	 */
	protected FileNode[] getAllChildren(FileNode node)
	{
		List todo = SCollection.createArrayList();
		todo.add(node);
		for(int i=0; i<todo.size(); i++)
		{
			FileNode tmp = (FileNode)todo.get(i);
			for(int j=0; j<tmp.getChildCount(); j++)
			{
				todo.add(tmp.getChildAt(j));
			}
		}
		todo.remove(node);
		return (FileNode[])todo.toArray(new FileNode[todo.size()]);
	}

	/**
	 * @return the starter icon
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#getToolIcon()
	 */
	public Icon getToolIcon(boolean selected)
	{
		return selected? icons.getIcon("jadexdoc_sel"): icons.getIcon("jadexdoc");
	}

	
	/**
	 * Load the properties.
	 * @param props
	 */
	public void setProperties(Properties props)
	{
		// try to load arguments from properties.

		mpanel.setProperties(props);
		jpanel.setProperties(props);

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
		jpanel.getProperties(props);

		props.put("mainsplit.location", Integer.toString(csplit.getDividerLocation()));
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
	 *  @return the starter panel
	 */
	protected JadexdocPanel getJadexdocPanel()
	{
		return jpanel;
	}

	/**
	 * @see jadex.tools.common.plugin.IControlCenterPlugin#reset()
	 */
	public void reset()
	{
		mpanel.reset();
		//spanel.reset();
	}

	/**
	 *  Called when the agent is closed.
	 */
	public void	shutdown()
	{
		mpanel.close();
	}

	/** 
	 * @return the help id of the perspective
	 * @see jadex.tools.jcc.AbstractJCCPlugin#getHelpID()
	 */
	public String getHelpID()
	{
		return "tools.jadexdoc";
	}
	
	/**
	 *  Add a package recursively.
	 */
	public final Action ADD_ALL_PACKAGES = new ToolTipAction("Add All Packages", 
		icons.getIcon("add_all_packages"), "Add all packages recursively starting with this package")
	{
		public void actionPerformed(ActionEvent e)
		{
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				List all = findRecursivePackages((FileNode)node);
				for(int i=0; i<all.size(); i++)
					jpanel.addEntry((String)all.get(i));	
			}
		}

		/**
		 *  Test if action is available in current context.
		 *  @return True, if available.
		 */
		public boolean isEnabled()
		{
			boolean ret = false;
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof DirNode)
			{
				DirNode dn = (DirNode)node;
				
				// Need at least one subpackage
				for(int i=0; !ret && i<dn.getChildCount(); i++)
				{
					if(dn.getChildAt(i) instanceof DirNode)
						ret = true;
				}
				
				if(ret)
				{
					// Need at least one missing entry
					ret = false;
					List all = findRecursivePackages(dn);
					all.remove(getEntryName(dn));
				
					for(int i=0; !ret && i<all.size(); i++)
					{
						if(!jpanel.containsEntry((String)all.get(i)))
							ret = true;
					}
				}
			}
			return ret;
		}
	};
	
	/**
	 *  Remove all packages recursively.
	 */
	public final Action REMOVE_ALL_PACKAGES = new ToolTipAction("Remove All Packages", 
		icons.getIcon("remove_all_packages"), "Remove all packages recursively starting with this package")
	{
		public void actionPerformed(ActionEvent e)
		{
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				List all = findRecursivePackages((FileNode)node);
				for(int i=0; i<all.size(); i++)
					jpanel.removeEntry((String)all.get(i));	
			}
		}

		/**
		 *  Test if action is available in current context.
		 *  @return True, if available.
		 */
		public boolean isEnabled()
		{
			boolean ret = false;
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof DirNode)
			{
				DirNode dn = (DirNode)node;
				
				// Need at least one subpackage
				for(int i=0; !ret && i<dn.getChildCount(); i++)
				{
					if(dn.getChildAt(i) instanceof DirNode)
						ret = true;
				}
				
				if(ret)
				{
					// Need at least one existing entry
					ret = false;
					List all = findRecursivePackages(dn);
					all.remove(getEntryName(dn));
				
					for(int i=0; !ret && i<all.size(); i++)
					{
						if(jpanel.containsEntry((String)all.get(i)))
							ret = true;
					}
				}
			}
			return ret;
		}
	};

	/**
	 *  Add a package.
	 */
	public final Action ADD_PACKAGE = new ToolTipAction("Add Package", 
		icons.getIcon("add_package"), "Add a package")
	{
		public void actionPerformed(ActionEvent e)
		{
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				jpanel.addEntry(getEntryName((FileNode)node));
			}
		}

		/**
		 *  Test if action is available in current context.
		 *  @return True, if available.
		 */
		public boolean isEnabled()
		{
			boolean ret = false;
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof DirNode)
			{
				DirNode dn = (DirNode)node;
				ret = !jpanel.containsEntry(getEntryName(dn));
			}
			return ret;
		}
	};
	
	/**
	 *  Remove a package.
	 */
	public final Action REMOVE_PACKAGE = new ToolTipAction("Remove Package", 
		icons.getIcon("remove_package"), "Remove a package")
	{
		public void actionPerformed(ActionEvent e)
		{
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				jpanel.removeEntry(getEntryName((FileNode)node));
			}
		}

		/**
		 *  Test if action is available in current context.
		 *  @return True, if available.
		 */
		public boolean isEnabled()
		{
			boolean ret = false;
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof DirNode)
			{
				DirNode dn = (DirNode)node;
				ret = jpanel.containsEntry(getEntryName(dn));
			}
			return ret;
		}
	};
	
	/**
	 *  Add a file.
	 */
	public final Action ADD_FILE = new ToolTipAction("Add File", 
		icons.getIcon("add_file"), "Add a file")
	{
		public void actionPerformed(ActionEvent e)
		{
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				jpanel.addEntry(getEntryName((FileNode)node));
			}
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
				FileNode fn = (FileNode)node;
				ret = (SXML.isJadexFilename(fn.getFile().getName())
					|| SUtil.isJavaSourceFilename(fn.getFile().getName()))
					&& !jpanel.containsEntry(getEntryName(fn));
			}
			return ret;
		}
	};
	
	/**
	 *  Remove a file.
	 */
	public final Action REMOVE_FILE = new ToolTipAction("Remove File", 
		icons.getIcon("remove_file"), "Remove a file")
	{
		public void actionPerformed(ActionEvent e)
		{
			TreeNode node = (TreeNode)mpanel.getLastSelectedPathComponent();
			if(node instanceof FileNode)
			{
				jpanel.removeEntry(getEntryName((FileNode)node));
			}
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
				FileNode fn = (FileNode)node;
				ret = (SXML.isJadexFilename(fn.getFile().getName()) 
					|| SUtil.isJavaSourceFilename(fn.getFile().getName()))
					&& jpanel.containsEntry(getEntryName(fn));
			}
			return ret;
		}
	};
}

