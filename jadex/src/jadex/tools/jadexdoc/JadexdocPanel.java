package jadex.tools.jadexdoc;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.help.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;

import jadex.tools.common.*;
import jadex.util.*;
import jadex.model.SXML;

/**
 *  The JadexDoc GUI for fast documentation generation.
 *  The tool needs to access JavaDoc either via executing the
 *  .exe or via the jdk/lib/tools.jar.
 */
public class JadexdocPanel extends JPanel
{
	//-------- static part --------

	/** The image icons. */
	protected static UIDefaults	icons	= new UIDefaults(new Object[]
	{
		"browse", SGUI.makeIcon(JadexdocPanel.class, "/jadex/tools/common/images/dots_small.png"),
		"running_javadoc", SGUI.makeIcon(JadexdocPanel.class, "/jadex/tools/common/images/new_javadoc_anim.gif"),
		"running_jadexdoc", SGUI.makeIcon(JadexdocPanel.class, "/jadex/tools/common/images/new_jadexdoc_anim.gif"),
	});

	//-------- attributes --------

	/** The Jadexdoc plugin. */
	protected JadexdocPlugin plugin;
	
	/** The generate options. */
	protected GenerateOptions options;

	/** The textfield for the output directory. */
	protected JTextField output_dir;

	/** The include subcapabilities button. */
	protected JCheckBox include_subs;

	/** The create index checkbox. */
	protected JCheckBox gentreecb;

	/** The create index checkbox. */
	protected JCheckBox gennavcb;

	/** The create index checkbox. */
	protected JCheckBox genindexcb;

	/** The create split index checkbox. */
	protected JCheckBox gensplitindexcb;

	/** Generate overview. */
	protected JCheckBox overviewcb;

	/** The overview textfield. */
	protected JTextField overviewtf;

	/** Flag for document title. */
	protected JCheckBox doctitlecb;

	/** Document title. */
	protected JTextField doctitletf;

	/** The generate Javadoc flag. */
	protected JCheckBox generatejavadoc;

	/** The generate Jadexdoc flag. */
	protected JCheckBox generatejadexdoc;
	
	/** The javadoc program location. */
	protected JTextField javadocloctf;

	/** The java program location. */
	protected JTextField javaloctf;

	/** The extra command line options for javadoc. */
	protected JTextField extraoptionsjavadoctf;
	
	/** The extra command line options for jadexdoc. */
	protected JTextField extraoptionsjadexdoctf;

	/** The ok button. */
	protected JButton startbut;

	/** The sourcepaths. */
	protected EditableList sourcepaths;
	
	/** The classpaths. */
	protected EditableList classpaths;
	
	/** The packages to document. */
	protected EditableList packages;

	/** The links. */
	protected EditableList links;
	
	/** The split pane. */
	protected JSplitPane center;

	/** The tabbed pane. */
	protected JTabbedPane lists;
	
	/** Boolean indicating that the generation is running. */
	protected boolean running;
	
	/** Boolean indicating if user aborted the generation (that possibly is running). */
	protected boolean abort;
	

	//-------- constructors --------

	/**
	 *  Create a new panel.
	 */
	public JadexdocPanel(JadexdocPlugin plugin, GenerateOptions options)
	{
		this.plugin = plugin;
		this.options = options;
		if(options==null)
			this.options = new GenerateOptions();
		
		// The settings panel
		int cols = 5;
		JPanel settings = new JPanel(new GridBagLayout());
		settings.setBorder(new TitledBorder(new EtchedBorder(), "Settings"));
	
		include_subs	= new JCheckBox("Include subpackages");
		int y=0;
		settings.add(include_subs, new GridBagConstraints(0, y, 3, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0));
		JLabel outlab = new JLabel("Output directory:");
		output_dir = new JTextField(cols);
		Dimension d = new Dimension(10, output_dir.getMinimumSize().height);
		//output_dir.setMinimumSize(d);
		
		JButton browse_outdir	= new JButton(icons.getIcon("browse"));
		final JFileChooser outchooser = new JFileChooser(".");
		outchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		browse_outdir.setMargin(new Insets(0,0,0,0));
		browse_outdir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(outchooser.showDialog(JadexdocPanel.this, "Select")==JFileChooser.APPROVE_OPTION)
				{
					output_dir.setText(outchooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		y++;
		settings.add(outlab, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(output_dir, new GridBagConstraints(1, y, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(browse_outdir, new GridBagConstraints(2, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));

		overviewcb = new JCheckBox("Overview page:");
		final JFileChooser overviewchooser = new JFileChooser(".");
		overviewchooser.setAcceptAllFileFilterUsed(true);
		javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter()
		{
			public String getDescription()
			{
				return "HTMLs";
			}

			public boolean accept(File f)
			{
				String name = f.getName();
				return f.isDirectory() || name.endsWith(".html");
			}
		};
		overviewchooser.addChoosableFileFilter(filter);

		overviewtf = new JTextField(cols);
		//overviewtf.setMinimumSize(d);
		overviewtf.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e){}
			public void insertUpdate(DocumentEvent e)
			{
				if(!"".equals(overviewtf.getText()) && !overviewcb.isSelected())
					overviewcb.setSelected(true);
			}
			public void removeUpdate(DocumentEvent e)
			{
				if("".equals(overviewtf.getText()) && overviewcb.isSelected())
					overviewcb.setSelected(false);
			}
		});
		JButton browse_overview = new JButton(icons.getIcon("browse"));
		browse_overview.setMargin(new Insets(0,0,0,0));
		browse_overview.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(overviewchooser.showDialog(JadexdocPanel.this, "Load")==JFileChooser.APPROVE_OPTION)
				{
					overviewtf.setText(overviewchooser.getSelectedFile().getAbsolutePath());
					overviewcb.setSelected(true);
				}
			}
		});
		y++;
		settings.add(overviewcb, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(overviewtf, new GridBagConstraints(1, y, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(browse_overview, new GridBagConstraints(2, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));

		doctitlecb = new JCheckBox("Document title:");
		doctitletf = new JTextField(cols);
		//doctitletf.setMinimumSize(d);
		doctitletf.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e){}
			public void insertUpdate(DocumentEvent e)
			{
				if(!"".equals(doctitletf.getText()) && !doctitlecb.isSelected())
					doctitlecb.setSelected(true);
			}
			public void removeUpdate(DocumentEvent e)
			{
				if("".equals(doctitletf.getText()) && doctitlecb.isSelected())
					doctitlecb.setSelected(false);
			}
		});
		y++;
		settings.add(doctitlecb, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(doctitletf, new GridBagConstraints(1, y, 2, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));

		JPanel basicoptions = new JPanel(new GridBagLayout());
		basicoptions.setBorder(new TitledBorder(new EtchedBorder(), "Basic options"));
		gentreecb = new JCheckBox("Generate hierarchy tree");
		gennavcb = new JCheckBox("Generate navigation bar");
		genindexcb = new JCheckBox("Generate index");
		gensplitindexcb = new JCheckBox("Generate index per letter");
		basicoptions.add(gentreecb, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0));
		basicoptions.add(gennavcb, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0));
		basicoptions.add(genindexcb, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0));
		basicoptions.add(gensplitindexcb, new GridBagConstraints(0, 3, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(2,12,2,2), 0, 0));
		y++;
		settings.add(basicoptions, new GridBagConstraints(0, y, 3, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));

		generatejavadoc = new JCheckBox("Generate Javadoc");
		y++;
		settings.add(generatejavadoc, new GridBagConstraints(0, y, 3, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));

		JLabel cmdlinelab = new JLabel("Extra options Javadoc:");
		extraoptionsjavadoctf = new JTextField(cols);
		//extraoptionsjavadoctf.setMinimumSize(d);
		
			y++;
		settings.add(cmdlinelab, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(extraoptionsjavadoctf, new GridBagConstraints(1, y, 2, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
				
		javadocloctf = new JTextField(cols);
		//javadocloctf.setMinimumSize(d);
		
		final JFileChooser javadocfc = new JFileChooser(".");
		JButton browse_javadoc = new JButton(icons.getIcon("browse"));
		browse_javadoc.setMargin(new Insets(0,0,0,0));
		browse_javadoc.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(javadocfc.showDialog(JadexdocPanel.this, "Locate Javadoc")==JFileChooser.APPROVE_OPTION)
				{
					javadocloctf.setText(javadocfc.getSelectedFile().getAbsolutePath());
					generatejavadoc.setSelected(true);
				}
			}
		});
		
		y++;
		settings.add(new JLabel("Javadoc program location: "), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(javadocloctf, new GridBagConstraints(1, y, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(browse_javadoc, new GridBagConstraints(2, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
	
		generatejadexdoc = new JCheckBox("Generate Jadexdoc");
		y++;
		settings.add(generatejadexdoc, new GridBagConstraints(0, y, 3, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
	
		JLabel cmdlinelabjadex = new JLabel("Extra options Jadexdoc:");
		extraoptionsjadexdoctf = new JTextField(cols);
		//extraoptionsjadexdoctf.setMinimumSize(d);
		
			y++;
		settings.add(cmdlinelabjadex, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(extraoptionsjadexdoctf, new GridBagConstraints(1, y, 2, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		
		javaloctf = new JTextField(cols);
		//javaloctf.setMinimumSize(d);
		
		final JFileChooser javalocfc = new JFileChooser(".");
		JButton browse_java = new JButton(icons.getIcon("browse"));
		browse_java.setMargin(new Insets(0,0,0,0));
		browse_java.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(javalocfc.showDialog(JadexdocPanel.this, "Locate Java")==JFileChooser.APPROVE_OPTION)
				{
					javaloctf.setText(javalocfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		y++;
		settings.add(new JLabel("Java program location for Jadexdoc: "), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(javaloctf, new GridBagConstraints(1, y, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		settings.add(browse_java, new GridBagConstraints(2, y, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
	
		
		startbut = new JButton("Abort");
		startbut.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		
		JPanel setbuts = new JPanel(new GridBagLayout());
		startbut.setText("Start");
		startbut.setToolTipText("Start the documentation generation");
		final JButton helpbut = new JButton("Help");
		helpbut.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		helpbut.setToolTipText("Invoke the JavaHelp for Jadexdoc");
		
		JButton openbrowser = new JButton("Show");
		openbrowser.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		openbrowser.setToolTipText("Open documentation from output dir in browser");
		y++;
		settings.add(openbrowser, new GridBagConstraints(0, y, 3, 1, 1, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		//settings.add(openbrowser, new GridBagConstraints(0, y, 3, 1, 1, 0, GridBagConstraints.NORTHWEST,
		//	GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		openbrowser.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				updateModel();
				openDocInBrowser(JadexdocPanel.this.options);
			}
		});
	
		setbuts.add(startbut, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2,4,4,2), 0, 0));
		setbuts.add(openbrowser, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2,4,4,2), 0, 0));
		setbuts.add(helpbut, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2,4,4,2), 0, 0));
		y++;
		settings.add(setbuts, new GridBagConstraints(0, y, 3, 1, 1, 1, GridBagConstraints.SOUTHEAST,
			GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0));
		
		startbut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Start the generation only when not running currently.
				if(!running)
				{
					startbut.setText("Abort");
					startbut.setToolTipText("Abort the documentation generation");
					running = true;
					generate();
				}
				// Abort the generation if running and not currently aborting.
				else if(!abort)
				{
					abort = true;
					startbut.setText("Start");
					startbut.setToolTipText("Start the documentation generation");
				}
			}
		});
				
		// The tabbed panel
		lists = new JTabbedPane();
		
		// Create packages view
		JPanel packview = new JPanel(new BorderLayout());
		this.packages = new EditableList("Packages to document", true);
		JScrollPane scroll = new JScrollPane(packages);
		packages.setPreferredScrollableViewportSize(new Dimension(400, 200)); 
		JPanel buts = new JPanel(new GridBagLayout());
		JButton clear = new JButton("Clear");
		clear.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		buts.add(clear, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		clear.setToolTipText("Clear the package list");
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				packages.removeEntries();
			}
		});
		packview.add("Center", scroll);
		packview.add("South", buts);
		lists.add("Packages", packview);
		
		// Create source paths view
		JPanel sourceview = new JPanel(new BorderLayout());
		this.sourcepaths = new EditableList("Source Paths", true);
		scroll = new JScrollPane(sourcepaths);
		sourcepaths.setPreferredScrollableViewportSize(new Dimension(400, 200));
		buts = new JPanel(new GridBagLayout());
		JButton add = new JButton("Add");
		add.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		clear = new JButton("Clear");		
		clear.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		buts.add(add, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		buts.add(clear, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		add.setToolTipText("Add a source path entry");
		clear.setToolTipText("Clear the source path list");
		final JFileChooser chooser = new JFileChooser(".");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		add.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(chooser.showDialog(SGUI.getWindowParent(JadexdocPanel.this)
					, "Load")==JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					sourcepaths.addEntry(""+file);
				}
			}
		});
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sourcepaths.removeEntries();
			}
		});
		sourceview.add("Center", scroll);
		sourceview.add("South", buts);
		lists.add("Source Paths", sourceview);
		
		// Create class paths view
		JPanel classview = new JPanel(new BorderLayout());
		this.classpaths = new EditableList("Class Paths", true);
		scroll = new JScrollPane(classpaths);
		classpaths.setPreferredScrollableViewportSize(new Dimension(400, 200));
		buts = new JPanel(new GridBagLayout());
		add = new JButton("Add");
		add.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		JButton fetch = new JButton("Fetch");		
		fetch.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		clear = new JButton("Clear");
		clear.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		add.setToolTipText("Add a class path entry");
		clear.setToolTipText("Clear the class path list");
		fetch.setToolTipText("Fetch all entries from current class path");
		buts.add(add, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		buts.add(clear, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		buts.add(fetch, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		final JFileChooser cchooser = new JFileChooser(".");
		cchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		add.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(cchooser.showDialog(SGUI.getWindowParent(JadexdocPanel.this)
					, "Load")==JFileChooser.APPROVE_OPTION)
				{
					File file = cchooser.getSelectedFile();
					classpaths.addEntry(""+file);
				}
			}
		});
		fetch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				classpaths.removeEntries();
				java.util.List entries = fetchClasspath();				
				for(int i=0; i<entries.size(); i++)
				{
					classpaths.addEntry((String)entries.get(i));
				}
			}
		});
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				classpaths.removeEntries();
			}
		});
		classview.add("Center", scroll);
		classview.add("South", buts);
		lists.add("Class Paths", classview);
	
		// Create class paths view
		JPanel linksview = new JPanel(new BorderLayout());
		this.links = new EditableList("URL links", true);
		scroll = new JScrollPane(links);
		links.setPreferredScrollableViewportSize(new Dimension(400, 200));
		buts = new JPanel(new GridBagLayout());
		add = new JButton("Add");
		add.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		fetch = new JButton("Fetch");		
		fetch.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		clear = new JButton("Clear");	
		clear.putClientProperty(SGUI.AUTO_ADJUST, Boolean.TRUE);
		add.setToolTipText("Add a file URL");
		clear.setToolTipText("Clear the links list");
		fetch.setToolTipText("Fetch the J2SE link");
		buts.add(add, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		buts.add(clear, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		buts.add(fetch, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(2, 4, 4, 2), 0, 0));
		final JFileChooser lchooser = new JFileChooser(".");
		lchooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		add.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(lchooser.showDialog(SGUI.getWindowParent(JadexdocPanel.this)
					, "Load")==JFileChooser.APPROVE_OPTION)
				{
					File file = lchooser.getSelectedFile();
					if(file!=null)
					{
						try
						{
							links.addEntry(""+file.toURI().toURL());
						}
						catch(MalformedURLException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		});
		fetch.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				links.addEntry("http://java.sun.com/javase/6/docs/api/");
				//links.addEntry("http://vsis-www.informatik.uni-hamburg.de/projects/jadex/jadex-0.96x/kernel/index.html");
			}
		});
		clear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				links.removeEntries();
			}
		});
		linksview.add("Center", scroll);
		linksview.add("South", buts);
		lists.add("URL Links", linksview);
		
		this.setLayout(new BorderLayout());
		
		center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		center.setOneTouchExpandable(true);
		center.setDividerLocation(200);
		//lists.setMinimumSize(new Dimension(0,0));
		//settings.setMinimumSize(new Dimension(0,0));
		center.add(lists);
		JScrollPane	scrollx	= new JScrollPane(settings);
		scrollx.setBorder(null);
		center.add(scrollx);
			
		this.add("Center", center);
		
		SGUI.adjustComponentSizes(this);
		
		updateView();

		HelpBroker hb = GuiProperties.setupHelp(this, "tools.jadexdoc");
		if(hb!=null)
			helpbut.addActionListener(new CSH.DisplayHelpFromSource(hb));
	}

	/**
	 *  Update the view from the model.
	 */
	protected void updateView()
	{
		include_subs.setSelected(options.includesubpackages);
		
		packages.removeEntries();
		for(int i=0; i<options.files.size(); i++)
			packages.addEntry((String)options.files.get(i));
		for(int i=0; i<options.packages.size(); i++)
			packages.addEntry((String)options.packages.get(i));

		sourcepaths.removeEntries();
		for(int i=0; i<options.sourcepath.size(); i++)
			sourcepaths.addEntry((String)options.sourcepath.get(i));
	
		classpaths.removeEntries();
		for(int i=0; i<options.classpath.size(); i++)
			classpaths.addEntry((String)options.classpath.get(i));
		
		links.removeEntries();
		for(int i=0; i<options.links.size(); i++)
			links.addEntry((String)options.links.get(i));
		
		output_dir.setText(options.destdirname);

		gentreecb.setSelected(options.createtree);
		gennavcb.setSelected(!options.nonavbar);
		genindexcb.setSelected(options.createindex);
		gensplitindexcb.setSelected(options.splitindex);

		output_dir.setText(options.destdirname);
		overviewcb.setSelected(options.overview);
		overviewtf.setText(options.overviewfile);

		doctitlecb.setSelected(options.createdoctitle);
		doctitletf.setText(options.doctitle);

		generatejavadoc.setSelected(options.javadoc);
		generatejadexdoc.setSelected(options.jadexdoc);
		
		javadocloctf.setText(options.javadocloc);
		javaloctf.setText(options.javaloc);
		extraoptionsjavadoctf.setText(options.extraoptionsjavadoc);
		extraoptionsjadexdoctf.setText(options.extraoptionsjadexdoc);

		if(genindexcb.isSelected())
			gensplitindexcb.setEnabled(true);
		else
			gensplitindexcb.setEnabled(false);
	}

	/**
	 *  Update the model from the view.
	 */
	protected void updateModel()
	{
		// todo: The convert method does not work when extra classpath settings are given in the extra options
		/*String conv = SUtil.convertPathToPackage(sel_name.getText());
		if(conv!=null)
			options.selectedpackage = conv;
		else
			options.selectedpackage = sel_name.getText();*/
		
		options.packages.clear();
		options.files.clear();
		String[] paths = packages.getEntries();
		for(int i=0; i<paths.length; i++)
		{
			if(SXML.isJadexFilename(paths[i]) || paths[i].toLowerCase().endsWith(".java"))
				options.files.add(paths[i]);
			else
				options.packages.add(paths[i]);
		}
		
		options.includesubpackages = include_subs.isSelected();
		options.destdirname = output_dir.getText();
		options.overview = overviewcb.isSelected();
		options.overviewfile = overviewtf.getText();
		options.createdoctitle = doctitlecb.isSelected();
		options.doctitle = doctitletf.getText();

		options.createtree = gentreecb.isSelected();
		options.nonavbar = !gennavcb.isSelected();
		options.createindex = genindexcb.isSelected();
		options.splitindex = gensplitindexcb.isSelected();
		options.extraoptionsjavadoc = extraoptionsjavadoctf.getText();
		options.extraoptionsjadexdoc = extraoptionsjadexdoctf.getText();
		options.javadoc = generatejavadoc.isSelected();
		options.jadexdoc = generatejadexdoc.isSelected();
		options.javadocloc = javadocloctf.getText();
		options.javaloc = javaloctf.getText();
		
		options.sourcepath.clear();
		String[] sps = sourcepaths.getEntries();
		for(int i=0; i<sps.length; i++)
			options.sourcepath.add(sps[i]);
		
		options.classpath.clear();
		String[] cps = classpaths.getEntries();
		for(int i=0; i<cps.length; i++)
			options.classpath.add(cps[i]);
		
		options.links.clear();
		String[] lks = links.getEntries();
		for(int i=0; i<lks.length; i++)
			options.links.add(lks[i]);
	}

	/**
	 *  Generate JadexDoc from supplied options from gui.
	 */
	public void generate()
	{
		updateModel();
		Thread gen = new Thread(new Runnable()
		{
			public void run()
			{
				boolean success = true;
				
				boolean toshow = true;
				boolean toactivate = true;
				if(plugin!=null)
				{
					toshow = !plugin.getJCC().isConsoleShown();
					if(toshow)
						plugin.getJCC().showConsole(true);
					toactivate = !plugin.getJCC().isConsoleEnabled();
					if(toactivate)
						plugin.getJCC().setConsoleEnabled(true);
				}
				GenerateOptions javaoptions = (GenerateOptions)options.clone();
				GenerateOptions jadexoptions = (GenerateOptions)options.clone();
				javaoptions.destdirname = javaoptions.destdirname+File.separatorChar+"javaapi";
				String javadest;
				try
				{
					javadest = ""+new File(javaoptions.destdirname).toURI().toURL();
					if(jadexoptions.javadoc)
						jadexoptions.links.add(""+javadest);
				}
				catch(MalformedURLException e)
				{
				}
				
				//System.out.println("Generation started");
				try
				{
					if(!abort && options.javadoc)
						generateJavadoc(javaoptions);
					//System.out.println("Javadoc finished");
				}
				catch(Exception e)
				{
					success = false;
					// Show non-modal dialog.
					SGUI.showMessageDialog(SGUI.getWindowParent(JadexdocPanel.this),
						SUtil.wrapText(e.getMessage()), "Javadoc Error", JOptionPane.INFORMATION_MESSAGE);
					//JOptionPane.showMessageDialog(GenerateJadexdocPanel.this, e.getMessage(),
					//	"Documentation Error", JOptionPane.INFORMATION_MESSAGE);
				}
				
				try
				{
					if(!abort && options.jadexdoc)
						generateJadexdoc(jadexoptions);
					//System.out.println("Jadexdoc finished");
				}
				catch(Exception e)
				{
					success = false;
					// Show non-modal dialog.
					SGUI.showMessageDialog(SGUI.getWindowParent(JadexdocPanel.this),
						SUtil.wrapText(e.getMessage()), "Jadexdoc Error", JOptionPane.INFORMATION_MESSAGE);
					//JOptionPane.showMessageDialog(GenerateJadexdocPanel.this, e.getMessage(),
					//	"Documentation Error", JOptionPane.INFORMATION_MESSAGE);
				}
				
				running = false;
				abort = false;
				startbut.setText("Start");
				startbut.setToolTipText("Start the documentation generation");
				//System.out.println("Generation ended");
				
				if(plugin!=null)
				{
					if(success && toshow)
						plugin.getJCC().showConsole(false);
					if(toactivate)
						plugin.getJCC().setConsoleEnabled(false);
				}
			}
		});
		gen.start();
	}
	
	/**
	 *  Set the options.
	 *  @param options The options.
	 */
	public void setOptions(GenerateOptions options)
	{
		this.options = options;
		updateView();
	}
	
	/**
	 *  Get the options.
	 *  @return options The options.
	 */
	public GenerateOptions getOptions()
	{
		updateModel();
		return options;
	}
	
	/**
	 *  Add a file or package to the
	 *  packages to document.
	 *  @param entry The entry.
	 */
	public void addEntry(String entry)
	{
		// Select the package view.
		lists.setSelectedIndex(0);
		packages.addEntry(entry);
	}
	
	/**
	 *  Remove a file or package to the
	 *  packages to document.
	 *  @param entry The entry.
	 */
	public void removeEntry(String entry)
	{
		packages.removeEntry(entry);
	}
	
	/**
	 *  Test if a file or package is
	 *  contained in the list.
	 *  @param entry The entry.
	 */
	public boolean containsEntry(String entry)
	{
		return packages.containsEntry(entry);
	}
	
	/**
	 *  Update tool from given properties.
	 */
	public void setProperties(final Properties props)
	{
		setOptions(GenerateOptions.create(props));

		if(props.getProperty("listssplit.location")!=null)
		{
			int msdl = Integer.parseInt(props.getProperty("listssplit.location"));
			center.setDividerLocation(msdl);
		}
	}
	
	/**
	 *  Write current state into properties.
	 */
	public void getProperties(Properties props)
	{
		props.putAll(getOptions().toProperties());
		props.put("listssplit.location", Integer.toString(center.getDividerLocation()));
	}
	
	//-------- static methods --------
	/**
	 *  Generate Javadoc using the options.
	 *  @param options The options.
	 */
	public void generateJavadoc(final GenerateOptions options)
	{
		// Throw exception when no packages and files to document
		if(options.packages.size()==0 && options.files.size()==0) 
			throw new RuntimeException("No sources selected for documentation, documentation impossible");

		JLabel run = new JLabel(icons.getIcon("running_javadoc"));
		plugin.getJCC().addStatusComponent(this, run);
		
		/*Class javadoc;
		String failed = null;
		try
		{
			// Try invoking the JavaDoc class via reflection
			// Needs the jdk/lib/tools.jar in classpath
			//System.out.println(SUtil.arrayToString(options.toCommandLineString()));
			
			javadoc = SReflect.findClass("com.sun.tools.javadoc.Main", null);
			final Method exe = javadoc.getMethod("execute", new Class[]{String[].class});
			MethodInvocation mi = new MethodInvocation(exe, new Object[]{options.toCommandLineString(true)});
			Thread jd = new Thread(mi);
			jd.start();
			
			while(mi.success==null && !abort)
			{
				Thread.sleep(300);
				if(abort)
					jd.stop();
			}
			if(mi.success==Boolean.FALSE)
				throw new RuntimeException("Javadoc returned an error.");

		}
		catch(Exception e)
		{*/
			//System.out.println("Tools.jar for Javadoc not found, trying cmd-line invocation");
			// Try invoking the JavaDoc class via command-line
			try
			{
				String cmd = options.javadocloc==null || options.javadocloc.length()==0?
					"javadoc": options.javadocloc;
				
				String[] cmds = options.toCommandLineString(true);
				String cmdline = "";
				for(int i=0; i<cmds.length; i++)
					cmdline += " "+cmds[i];
				
				System.out.println(cmd+" "+cmdline);
				
				// Can be called in another directory
				File newcurdir = new File(options.destdirname);
				newcurdir.mkdirs();
				Process proc = Runtime.getRuntime().exec(cmd+" "+cmdline, null, newcurdir);
				new Thread(new StreamCopy(proc.getInputStream(), System.out)).start();
				new Thread(new StreamCopy(proc.getErrorStream(), System.out)).start();
				
				boolean finished = false;
				while(!finished && !abort)
				{
					try
					{
						Thread.sleep(300);
						if(proc.exitValue()!=0)
							throw new RuntimeException("Javadoc returned an error or could not be invoked.");
						finished = true;
					}
					catch(IllegalThreadStateException ie)
					{
						if(abort)
							proc.destroy();
					}
				}
					
				/*Process proc = Runtime.getRuntime().exec(cmd+" "+cmdline);
				new Thread(new StreamCopy(proc.getInputStream(), System.out)).start();
				new Thread(new StreamCopy(proc.getErrorStream(), System.out)).start();
					
				if(proc.waitFor()!=0)
				// todo: does not work because process output stream is not read :-(
				//if(Runtime.getRuntime().exec(cmd+" -classpath "+cp+cmdline).waitFor()!=0)
				{
					throw new RuntimeException("Javadoc returned an error or could not be invoked.");
				}*/
			}
			catch(Exception ex)
			{
				throw new RuntimeException("Could not process Javadoc. Reason: "+ex.getMessage());
				//ex.printStackTrace();
				//failed = "Could not process Javadoc. Reason: "+ex.getMessage();
			}
			
			plugin.getJCC().removeStatusComponent(this);
			
		//}
		//if(failed!=null)
		//	throw new RuntimeException(failed);
	}
	
	/**
	 *  Generate Jadexdoc for the specified options.
	 *  @params options The options.
	 */
	public void generateJadexdoc(GenerateOptions options)
	{
		JLabel run = new JLabel(icons.getIcon("running_jadexdoc"));
		plugin.getJCC().addStatusComponent(this, run);
		
		try
		{
			String cmd = options.javaloc==null || options.javaloc.length()==0?
					"java": options.javaloc;
			java.util.List cps = fetchClasspath();
			for(int i=0; i<cps.size(); i++)
			{
				if(i==0)
					cmd += " -cp ";
				else
					cmd += File.pathSeparator;
				cmd += "\""+cps.get(i)+"\"";
			}
			cmd +=  " jadex.tools.jadexdoc.Main";
			
			String[] cmds = options.toCommandLineString(false);
			String cmdline = "";
			for(int i=0; i<cmds.length; i++)
				cmdline += " "+cmds[i];
			
			//System.out.println(cmd+" "+cmdline);
			
			// Can be called in another directory
			File newcurdir = new File(options.destdirname);
			newcurdir.mkdirs();
			Process proc = Runtime.getRuntime().exec(cmd+" "+cmdline, null, newcurdir);
			new Thread(new StreamCopy(proc.getInputStream(), System.out)).start();
			new Thread(new StreamCopy(proc.getErrorStream(), System.out)).start();
			
			boolean finished = false;
			while(!finished && !abort)
			{
				try
				{
					Thread.sleep(300);
					if(proc.exitValue()!=0)
						throw new RuntimeException("Jadexdoc returned an error or could not be invoked.");
					finished = true;
				}
				catch(IllegalThreadStateException ie)
				{
					if(abort)
						proc.destroy();
				}
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException("Could not process Jadexdoc. Reason: "+e.getMessage());
		}
		
		// Cannot change current dir in same VM :-( 
		// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4045688
		/*MethodInvocation mi;
		try
		{
			Class jadexdoc = SReflect.findClass("jadex.tools.jadexdoc.Main", null);
			
			Method exe = jadexdoc.getMethod("execute", new Class[]{String[].class});
			mi = new MethodInvocation(exe, new Object[]{options.toCommandLineString(false)});
			Thread jd = new Thread(mi);
			jd.start();
			
			while(mi.success==null && !abort)
			{
				Thread.sleep(300);
				if(abort)
					jd.stop();
			}
		}
		catch(Exception e)
		{
			throw new RuntimeException("Could not process Jadexdoc. Reason: "+e.getMessage());
		}
		if(mi!=null && mi.success==Boolean.FALSE)
			throw new RuntimeException("Jadexdoc returned an error.");*/
		
		// Uninterruptable
		/*if(Main.execute(options.toCommandLineString())!=0) 
			throw new RuntimeException("Jadexdoc returned an error.");*/
		
		plugin.getJCC().removeStatusComponent(this);
	}
	
	/**
	 *  Fetch the current classpath
	 *  @return classpath entries as a list of strings.
	 */
	protected java.util.List fetchClasspath()
	{
		java.util.List	entries	= new ArrayList();
		ClassLoader cl = DynamicURLClassLoader.getInstance();
		if(!(cl instanceof DynamicURLClassLoader))
			throw new RuntimeException("No URLClassLoader found");
		java.util.List cps = ((DynamicURLClassLoader)cl).getAllClasspathURLs();
		for(int i=0; i<cps.size(); i++)
		{
			URL	url	= (URL)cps.get(i);
			String file = url.getFile();
			File f = new File(file);
			
			// Hack!!! Above code doesnt handle relative url paths. 
			if(!f.exists())
			{
				File	newfile	= new File(new File("."), file);
				if(newfile.exists())
				{
					f	= newfile;
				}
			}
			entries.add(f.getAbsolutePath());
		}
		return entries;
	}

	/**
	 *  Open some generated docs in the browser.
	 *  @param options The options.
	 */
	public static void openDocInBrowser(GenerateOptions options)
	{
		try
		{
			String path = new File(options.destdirname).getCanonicalPath();
			BrowserLauncher.openURL("file:///"+path+"/index.html");
		}
		catch(IOException e)
		{
			//throw new RuntimeException("Could not open browser. Reason: "+e.getMessage());
		}	
	}

	/**
	 *  Class for invoking a method on a decoupled thread.
	 */
	static class MethodInvocation implements Runnable
	{
		protected Method exe;
		protected Object[] args;
		public Boolean success;

		public MethodInvocation(Method exe, Object[] args)
		{
			this.exe = exe;
			this.args = args;
		}
		
		public void run()
		{
			try
			{
				if(((Integer)exe.invoke(null, args)).intValue()!=0)
					success = Boolean.FALSE;
				else
					success = Boolean.TRUE;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	};
	
	/**
	 *  Main for testing.
	 *  @param args
	 */
	public static void main(String[] args)
	{
		GenerateOptions go = new GenerateOptions();
		go.destdirname = "c:/projects/test4711";
		//go.packages.add("jadex.examples.blackjack.dealer");
		go.packages.add("jadex.examples.blackjack");
		
		final String file = args.length==0? "./jadexdoc.properties": args[0];
		
		try
		{
			FileInputStream fis = new FileInputStream(file);
			Properties props = new Properties();
			props.load(fis);
			fis.close();
			go = GenerateOptions.create(props);
		}
		catch(Exception e)
		{
			System.out.println("Failed to load properties from: "+file);
			//e.printStackTrace();
		}
			
		final JadexdocPanel jp = new JadexdocPanel(null, go);
		final JFrame f = new JFrame();
		f.add("Center", jp);
		f.pack();
		f.setVisible(true);
		
		f.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent we)
			{
				//System.out.println("Window closed: "+we);
				try
				{
					FileOutputStream fos = new FileOutputStream(file);
					Properties props = jp.getOptions().toProperties();
					props.store(fos, null);
					fos.close();
					System.out.println("Saved properties in: "+file);
				}
				catch(Exception e)
				{
					System.out.println("Failed to save properties in: "+file);
					//e.printStackTrace();
				}
				f.dispose();
			}
		});
	}
	
}

