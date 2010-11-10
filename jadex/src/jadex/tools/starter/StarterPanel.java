package jadex.tools.starter;

import jadex.config.Configuration;
import jadex.model.*;
import jadex.parser.SParser;
import jadex.tools.common.*;
import jadex.util.*;
import jadex.util.collection.SCollection;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.help.HelpBroker;
import javax.help.CSH;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * The starter gui allows for starting agents platform independently.
 */
public class StarterPanel extends JPanel
{
	//-------- static part --------

	/** The image icons. */
	protected static UIDefaults	icons	= new UIDefaults(new Object[]
	{
		"Browse", SGUI.makeIcon(StarterPanel.class,	"/jadex/tools/common/images/dots_small.png"),
	});

	//-------- attributes --------

	/** The model. */
	protected IMCapability model;

	/** The last loaded filename. */
	protected String lastfile;

	//-------- gui widgets --------

	/** The filename. */
	protected JTextField filename;

	/** The file chooser. */
	protected JFileChooser filechooser;

	/** The configuration. */
	protected JComboBox config;

	/** The agent type. */
	protected JTextField agentname;

	/** The agent name generator flag. */
	protected JCheckBox genagentname;

	/** The agent type. */
	protected JPanel arguments;
	protected List argelems;

	/** The start button. */
	protected JButton start;

	/** The description panel. */
	protected ElementPanel modeldesc;

	/** The agent specific panel. */
	protected JPanel ap;

	//-------- constructors --------

	/**
	 * Open the GUI.
	 * @param starter The starter.
	 */
	StarterPanel(final StarterPlugin starter)
	{
		super(new BorderLayout());

		JPanel content = new JPanel(new GridBagLayout());

	   	// The browse button.
		//final JButton browse = new JButton("browse...");
		final JButton browse = new JButton(icons.getIcon("Browse"));
		browse.setToolTipText("Browse via file requester to locate a model");
		browse.setMargin(new Insets(0,0,0,0));
		// Create the filechooser.
		// Hack!!! might trhow exception in applet / webstart
		try
		{
			filechooser = new JFileChooser(".");
			filechooser.setAcceptAllFileFilterUsed(true);
			javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter()
			{
				public String getDescription()
				{
					return "ADFs";
				}

				public boolean accept(File f)
				{
					String name = f.getName();
					return f.isDirectory() || name.endsWith(SXML.FILE_EXTENSION_AGENT) || name.endsWith(SXML.FILE_EXTENSION_CAPABILITY);
				}
			};
			filechooser.addChoosableFileFilter(filter);
		}
		catch(SecurityException e)
		{
			browse.setEnabled(false);
		}
		browse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(filechooser.showDialog(SGUI.getWindowParent(StarterPanel.this)
					, "Load")==JFileChooser.APPROVE_OPTION)
				{
					File file = filechooser.getSelectedFile();
					String	model	= file!=null ? ""+file : null;

//					if(file!=null && file.getName().endsWith(".jar"))
//					{
//						// Start looking into the jar-file for description-files
//						try
//						{
//							DynamicURLClassLoader.addURLToInstance(new URL("file", "", file.toString()));
//
//							JarFile jarFile = new JarFile(file);
//							Enumeration e = jarFile.entries();
//							java.util.List	models	= new ArrayList();
//							while (e.hasMoreElements())
//							{
//								ZipEntry jarFileEntry = (ZipEntry) e.nextElement();
//								if(SXML.isJadexFilename(jarFileEntry.getName()))
//								{
//									models.add(jarFileEntry.getName());
//								}
//							}
//							jarFile.close();
//
//							if(models.size()>1)
//							{
//								Object[]	choices	= models.toArray(new String[models.size()]);
//								JTreeDialog td = new JTreeDialog(
//									null,
////									(Frame)StarterGui.this.getParent(),
//									"Select Model", true,
//									"Select an model to load:",
//									(String[])choices, (String)choices[0]);
//								td.setVisible(true);
//								model = td.getResult();
//							}
//							else if(models.size()==1)
//							{
//								model	= (String)models.get(0);
//							}
//							else
//							{
//								model	= null;
//							}
//						}
//						catch(Exception e)
//						{
//							//e.printStackTrace();
//						}
//					}

					//System.out.println("... load model: "+model);
//					lastfile	= model;
					loadModel(model);
				}
			}
		});

		// Create the filename combo box.
		filename = new JTextField();
		filename.setEditable(true);
		ActionListener filelistener = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				loadModel(filename.getText());
			}
		};
		filename.addActionListener(filelistener);

		// The configuration.
		config = new JComboBox();
		config.setToolTipText("Choose the configuration to start with");
		config.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				refreshArguments();
			}
		});

		// The agent name.
		agentname = new JTextField();

		// The generate flag for the agentname;
		genagentname = new JCheckBox("Auto generate", false);
		genagentname.setToolTipText("Auto generate the agent instance name");
		genagentname.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				agentname.setEditable(!genagentname.isSelected());
			}
		});

		// The arguments.
		arguments = new JPanel(new GridBagLayout());

		// The reload button.
		final JButton reload = new JButton("Reload");
		reload.setToolTipText("Reload the current model");
		reload.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				reloadModel(lastfile);
			}
		});

		int mw = (int)reload.getMinimumSize().getWidth();
		int pw = (int)reload.getPreferredSize().getWidth();
		int mh = (int)reload.getMinimumSize().getHeight();
		int ph = (int)reload.getPreferredSize().getHeight();

		// The start button.
		this.start = new JButton("Start");
		start.setToolTipText("Start this agent");
		start.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(model!=null)
				{
					String configname = (String)config.getModel().getSelectedItem();
					String an = genagentname.isSelected()?  null: agentname.getText();
					Map args = SCollection.createHashMap();
					String errortext = null;
					for(int i=0; i<argelems.size(); i++)
					{
						String argname = ((JLabel)arguments.getComponent(i*4+1)).getText();
						String argval = ((JTextField)arguments.getComponent(i*4+3)).getText();
						if(argval.length()>0)
						{
							Object arg = null;
							try
							{
								arg = SParser.evaluateExpression(argval, null, null);
							}
							catch(Exception e)
							{
								if(errortext==null)
									errortext = "Error within argument expressions:\n";
								errortext += argname+" "+e.getMessage()+"\n";
							}
							args.put(argname, arg);
						}
					}
					if(errortext!=null)
					{
						JOptionPane.showMessageDialog(SGUI.getWindowParent(StarterPanel.this), errortext, 
							"Display Problem", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
						starter.getJCC().createAgent(filename.getText(), an, configname, args);
					}
				}
			}
		});
		start.setMinimumSize(new Dimension(mw, mh));
		start.setPreferredSize(new Dimension(pw, ph));

		// The reset button.
		final JButton reset = new JButton("Reset");
		reset.setToolTipText("Reset all fields");
		reset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				reset();
			}
		});
		reset.setMinimumSize(new Dimension(mw, mh));
		reset.setPreferredSize(new Dimension(pw, ph));

		// The description panel.
		modeldesc = new ElementPanel("Description", null);
		ChangeListener desclistener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent ce)
			{
				Object id = modeldesc.getId(modeldesc.getSelectedComponent());
				if(id instanceof String)
				{
					//System.out.println("SystemEvent: "+id);
					loadModel((String)id);
					updateGuiForNewModel((String)id);
				}
			}
		};
		modeldesc.addChangeListener(desclistener);
		modeldesc.setMinimumSize(new Dimension(200, 150));
		modeldesc.setPreferredSize(new Dimension(400, 150));

		// Avoid panel being not resizeable when long filename is displayed
		filename.setMinimumSize(filename.getMinimumSize());

		int y = 0;
	
		ap = new JPanel(new GridBagLayout());
		
		JLabel agentnamel = new JLabel("Agent name");
		ap.add(agentnamel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(0, 0, 0, 2), 0, 0));
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.add(agentname, "Center");
		tmp.add(genagentname, "East");
		//content.add(agentname, new GridBagConstraints(1, y, 2, 1, 1, 0, GridBagConstraints.WEST,
		//			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		ap.add(tmp, new GridBagConstraints(1, 0, 4, 1, 1, 0, GridBagConstraints.EAST,
			GridBagConstraints.BOTH, new Insets(0, 2, 0, 2), 0, 0));
		
		JPanel upper = new JPanel(new GridBagLayout());
		upper.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), " Settings "));
		upper.add(new JLabel("Filename"), new GridBagConstraints(0, y, 1, 1, 0, 0,
			GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		upper.add(filename, new GridBagConstraints(1, y, 3, 1, 1, 0,
			GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		upper.add(browse, new GridBagConstraints(4, y, 1, 1, 0, 0,
			GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0, 0));
		y++;
		JLabel conf = new JLabel("Configuration");
		upper.add(conf, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		upper.add(config, new GridBagConstraints(1, y, 4, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		y++;
		upper.add(ap, new GridBagConstraints(0, y, 5, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		y = 0;
		content.add(upper, new GridBagConstraints(0, y, 5, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		y++;
		content.add(arguments, new GridBagConstraints(0, y, 5, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		agentnamel.setMinimumSize(conf.getMinimumSize());
		agentnamel.setPreferredSize(conf.getPreferredSize());

		/*y++;
		agentnamel = new JLabel("Agent name");
		content.add(agentnamel, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.add(agentname, "Center");
		tmp.add(genagentname, "East");
		//content.add(agentname, new GridBagConstraints(1, y, 2, 1, 1, 0, GridBagConstraints.WEST,
		//			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		content.add(tmp, new GridBagConstraints(1, y, 4, 1, 0, 0, GridBagConstraints.EAST,
					GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		y++;
		argumentsl = new JLabel("Arguments");
		content.add(argumentsl, new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		content.add(arguments, new GridBagConstraints(1, y, 4, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));*/

		/*y++;
		content.add(new JButton("1"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		content.add(new JButton("2"), new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		content.add(new JButton("3"), new GridBagConstraints(2, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		content.add(new JButton("4"), new GridBagConstraints(3, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		content.add(new JButton("5"), new GridBagConstraints(4, y, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
*/
		JPanel buts = new JPanel(new GridBagLayout());
		buts.add(start, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		buts.add(reload, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		buts.add(reset, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));

		HelpBroker hb = GuiProperties.setupHelp(this, "tools.starter");
		if(hb!=null)
		{
			JButton help = new JButton("Help");
			help.setToolTipText("Activate JavaHelp system");
			help.addActionListener(new CSH.DisplayHelpFromSource(hb));
			help.setMinimumSize(new Dimension(mw, mh));
			help.setPreferredSize(new Dimension(pw, ph));
			buts.add(help, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(2, 2, 2, 2), 0, 0));
		}

		//content.add(prodmode, new GridBagConstraints(3, 4, 1, 1, 1, 0,
		//	GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0));
		y++;
		content.add(buts, new GridBagConstraints(0, y, 5, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(2, 2, 2, 2), 0, 0));

		y++;
		content.add(modeldesc, new GridBagConstraints(0, y, 5, 1, 1, 1, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.add("Center", content);
	}

	/**
	 *  Reload the model.
	 *  @param adf The adf.
	 */
	public void reloadModel(String adf)
	{
		if(lastfile==null)
			return;
		String cachename = lastfile.substring(0, lastfile.length()-3)+"cam";
		SXML.clearModelCache(cachename);
		String toload = lastfile;
		lastfile = null;
		loadModel(toload);
	}
	
	/**
	 *  Load an agent model.
	 *  @param adf The adf to load.
	 */
	public void loadModel(final String adf)
	{
		// Don't load same model again (only on reload).
		if(adf!=null && adf.equals(lastfile))
			return;
		
		//System.out.println("loadModel: "+adf+" "+modelname.getActionListeners().length+" "+SUtil.arrayToString(modelname.getActionListeners()));

		try
		{
			if(SXML.isAgentFilename(adf))
			{
				model = SXML.loadAgentModel(adf, null);
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						updateGuiForNewModel(adf);
					}
				});
				createArguments();
				arguments.setVisible(true);
				ap.setVisible(true);
				start.setVisible(true);
			}
			else if(SXML.isCapabilityFilename(adf))
			{
				model = SXML.loadCapabilityModel(adf, null, null);
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						updateGuiForNewModel(adf);
					}
				});
				arguments.setVisible(false);
				ap.setVisible(false);
				start.setVisible(false);
			}
			else
			{
				model = null;
				modeldesc.addTextContent("Model", null, "No model loaded.", "model");
				// Without model no agent can be started.
				start.setEnabled(false);
			}
			lastfile = adf;
		}
		catch(Exception e)
		{
			// With broken model no agent can be started.
			start.setEnabled(false);
			//e.printStackTrace();
			model = null;
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			modeldesc.addTextContent("Error", null, "No model loaded:\n"+sw, "error");
			agentname.setText("");
			config.removeAllItems();
			clearArguments();
			setAgentName("");
			filename.setText("");
		}
	}

	/**
	 *  Update the GUI for a new model.
	 *  @param adf The adf.
	 */
	void updateGuiForNewModel(String adf)
	{
//		System.out.println("updategui");
		if(model==null)
			return;
		
		filename.setText(adf);

		if(model.getName()!=null && SXML.isAgentFilename(adf))
			agentname.setText(model.getName());
		else
			agentname.setText("");

		lastfile = model.getFilename();

		config.removeAllItems();
		// Add all known agent configuration names to the config chooser.
		IMConfiguration[] states = model.getConfigurationbase().getConfigurations();
		for(int i = 0; i<states.length; i++)
		{
			((DefaultComboBoxModel)config.getModel()).addElement(states[i].getName());
		}
		IMConfiguration defstate = model.getConfigurationbase().getDefaultConfiguration();
		if(defstate!=null)
		{
			config.getModel().setSelectedItem(defstate.getName());
		}

		//		if(modeldesc.getSelectedComponent()==null
		//			|| !modeldesc.getId(modeldesc.getSelectedComponent()).equals(adf))
		{
			String clazz = SReflect.getInnerClassName(model.getClass());
			if(clazz.endsWith("Data")) clazz = clazz.substring(0, clazz.length()-4);

			IReport report = Configuration.getConfiguration().isModelChecking()? model.getReport(): null;
			if(report!=null && !report.isEmpty())
			{
				Icon icon = GuiProperties.getElementIcon(clazz+"_broken");
				try
				{
					modeldesc.addHTMLContent(model.getName(), icon, report.toHTMLString(), adf, report.getDocuments());
				}
				catch(Exception e)
				{
					//e.printStackTrace();
					String text = SUtil.wrapText("Could not display HTML content: "+e.getMessage());
					JOptionPane.showMessageDialog(SGUI.getWindowParent(this), text, "Display Problem", JOptionPane.INFORMATION_MESSAGE);
					modeldesc.addTextContent(model.getName(), icon, report.toString(), adf);
				}
			}
			else
			{
				Icon icon = GuiProperties.getElementIcon(clazz);
				try
				{
					modeldesc.addHTMLContent(model.getName(), icon, getDescription(model), adf, null);
				}
				catch(Exception e)
				{
					String text = SUtil.wrapText("Could not display HTML content: "+e.getMessage());
					JOptionPane.showMessageDialog(SGUI.getWindowParent(this), text, "Display Problem", JOptionPane.INFORMATION_MESSAGE);
					modeldesc.addTextContent(model.getName(), icon, getDescription(model), adf);
				}
			}

			// Adjust state of start button depending on model checking state.
			start.setEnabled(SXML.isAgentFilename(adf) && (report==null || report.isEmpty()));
		}
	}

	/**
	 * Get the description for a model.
	 * Only required because JBind doesn't allow access to XML comments.
	 * @param model The model.
	 * @return The description. 
	 */
	protected String getDescription(IMElement model)
	{
		String ret = null;
		if(model instanceof IMCapability)
		{
			try
			{
				// Try to extract first comment from file.
				InputStream is = SUtil.getResource(((IMCapability)model).getFilename());
				int read;
				while((read = is.read())!=-1)
				{
					if(read=='<')
					{
						read = is.read();
						if(Character.isLetter((char)read))
						{
							// Found first tag, use whatever comment found up to now.
							break;
						}
						else if(read=='!' && is.read()=='-' && is.read()=='-')
						{
							// Found comment.
							StringBuffer comment = new StringBuffer();
							while((read = is.read())!=-1)
							{
								if(read=='-')
								{
									if((read = is.read())=='-')
									{
										if((read = is.read())=='>')
										{
											// Finished reading <!-- ... --> statement
											ret = comment.toString();
											break;
										}
										comment.append("--");
										comment.append((char)read);
									}
									else
									{
										comment.append('-');
										comment.append((char)read);
									}
								}
								else
								{
									comment.append((char)read);
								}
							}
						}
					}
				}
				is.close();
			}
			catch(Exception e)
			{
				ret = "No description available: "+e;
			}
		}
		else
		{
			ret = "No description available.";
		}
		return ret;
	}

	/**
	 *  Get the properties.
	 *  @param props The properties.
	 */
	public void getProperties(Properties props)
	{
		String m = filename.getText();
		if(m!=null) props.setProperty("model", m);

		String c = (String)config.getSelectedItem();
		if(c!=null) 
			props.setProperty("config", c);

		props.setProperty("autogenerate", ""+genagentname.isSelected());
		
		props.setProperty("name", agentname.getText());
		for(int i=0; argelems!=null && i<argelems.size(); i++)
		{
			JTextField valt = (JTextField)arguments.getComponent(i*4+3);
			props.setProperty("arguments_"+i, valt.getText());
		}
	}

	/**
	 *  Set the properties.
	 *  @param props The propoerties.
	 */
	protected void setProperties(Properties props)
	{
		String mo = props.getProperty("model");
		if(mo!=null)
		{
			loadModel(mo);
			selectConfiguration(props.getProperty("config"));
		}
		if(argelems!=null && argelems.size()>0)
		{
			String[] argvals = new String[argelems.size()];
			for(int i=0; i<argvals.length; i++)
			{
				argvals[i] = props.getProperty("arguments_"+i);
			}
			setArguments(argvals);
		}
		setAgentName(props.getProperty("name"));
		setAutoGenerate(props.getProperty("autogenerate"));
		
	}

	/**
	 *  Reset the gui.
	 */
	public void reset()
	{
		filename.setText("");
		modeldesc.removeAll();
		loadModel(null);
		config.removeAllItems();
		clearArguments();
		setAgentName("");
		//model = null;
		//start.setEnabled(false);
	}

	/**
	 *  Select a configuration.
	 *  @param conf The configuration.
	 */
	protected void selectConfiguration(final String conf)
	{
		if(conf!=null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					//System.out.println("selecting: "+conf+" "+config.getModel().getSize());
					config.getModel().setSelectedItem(conf);
				}
			});
		}
	}

	/**
	 *  Set the arguments.
	 *  @param args The arguments.
	 */
	protected void setArguments(final String[] args)
	{
		if(args!=null && args.length>0)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					for(int i=0; i<args.length; i++)
					{
						JTextField valt = (JTextField)arguments.getComponent(i*4+3);
						valt.setText(args[i]);
					}
				}
			});
		}
	}
	
	/**
	 *  Refresh the argument values.
	 */
	protected void refreshArguments()
	{
		// Assert that all argument components are there.
		if(arguments==null || argelems==null || arguments.getComponentCount()!=4*argelems.size())
			return;
		
		for(int i=0; argelems!=null && i<argelems.size(); i++)
		{
			JTextField valt = (JTextField)arguments.getComponent(i*4+2);
			String val  = findValue((IMReferenceableElement)argelems.get(i), (String)config.getSelectedItem());
			valt.setText(val);
			//valt.setMinimumSize(new Dimension(valt.getPreferredSize().width/4, valt.getPreferredSize().height/4));
		}
	}
	
	/**
	 *  Refresh the argument values.
	 */
	protected void clearArguments()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Assert that all argument components are there.
				if(arguments==null || argelems==null || arguments.getComponentCount()!=4*argelems.size())
					return;
				
				for(int i=0; i<argelems.size(); i++)
				{
					JTextField valt = (JTextField)arguments.getComponent(i*4+3);
					valt.setText("");
				}
			}
		});
	}
	
	/**
	 *  Create the arguments panel.
	 */
	protected void createArguments()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				argelems = SCollection.createArrayList();
				arguments.removeAll();
				arguments.setBorder(null);
				
				if(model instanceof IMBDIAgent)
				{
					int y=0;
					IMBDIAgent magent = (IMBDIAgent)model;
					IMBelief[] args = magent.getBeliefbase().getBeliefs();
					for(int i=0; i<args.length; i++)
					{
						if(args[i].getExported().equals(IMTypedElement.EXPORTED_TRUE))
						{
							argelems.add(args[i]);
							createArgumentGui(args[i], y++);
						}
					}
					IMBeliefReference[] argrefs = magent.getBeliefbase().getBeliefReferences();
					for(int i=0; i<argrefs.length; i++)
					{
						if(argrefs[i].getExported().equals(IMTypedElement.EXPORTED_TRUE))
						{
							argelems.add(argrefs[i]);
							createArgumentGui(argrefs[i], y++);
						}
					}
					if(y>0)
						arguments.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), " Arguments "));
				}
			}
		});
	}
	
	/**
	 *  Create the gui for one argument. 
	 *  @param elem The belief or belief reference.
	 *  @param y The row number where to add.
	 */
	protected void createArgumentGui(final IMReferenceableElement elem, int y)
	{
		JLabel namel = new JLabel(elem.getName());
		final JValidatorTextField valt = new JValidatorTextField(15);
		valt.setValidator(new ParserValidator());
		JTextField mvalt = new JTextField(findValue(elem, (String)config.getModel().getSelectedItem()));
		// Java JTextField bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4247013
		//mvalt.setMinimumSize(new Dimension(mvalt.getPreferredSize().width/4, mvalt.getPreferredSize().height/4));
		mvalt.setEditable(false);
		
		Class	clazz = null;
		String	description	= null;
		IMReferenceableElement	myelem	= elem;
		while((clazz==null || description==null) && myelem instanceof IMBeliefReference)
		{
			IMBeliefReference	mbelref	= (IMBeliefReference)myelem;
			clazz	= clazz!=null ? clazz : mbelref.getClazz();
			description	= description!=null ? description : mbelref.getDescription();
			myelem	= ((IMBeliefReference)myelem).getReferencedElement();
		}
		if((clazz==null || description==null) && myelem instanceof IMBelief)
		{
			IMBelief	mbel	= (IMBelief)myelem;
			clazz	= clazz!=null ? clazz : mbel.getClazz();
			description	= description!=null ? description : mbel.getDescription();
		}
		JLabel typel = new JLabel(clazz!=null ? SReflect.getInnerClassName(clazz) : "undefined");
		
		if(description!=null)
		{
			namel.setToolTipText(description);
			valt.setToolTipText(description);
			mvalt.setToolTipText(description);
//			typel.setToolTipText(description);
		}
		
		int x = 0;
		arguments.add(typel, new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		arguments.add(namel, new GridBagConstraints(x++, y, 1, 1, 0, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		arguments.add(mvalt, new GridBagConstraints(x++, y, 1, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		arguments.add(valt, new GridBagConstraints(x++, y, 1, 1, 1, 0, GridBagConstraints.WEST,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
		y++;
	}
	
	/**
	 *  Find the model value (initial or default) for an argument.
	 *  @param elem The element.
	 *  @param configname The configname.
	 *  @return The expression string.
	 */
	protected String findValue(IMReferenceableElement elem, String configname)
	{
		String ret = null;
		IMCapability scope = elem.getScope();
		if(elem instanceof IMBelief)
		{
			IMBelief bel = (IMBelief)elem;
			IMConfiguration inis = configname!=null? scope.getConfigurationbase().getConfiguration(configname)
				: scope.getConfigurationbase().getDefaultConfiguration();
			IMConfigBelief inib = null;
			if(inis!=null && inis.getBeliefbase()!=null)
				inib = inis.getBeliefbase().getInitialBelief(bel);
			if(inib!=null)
			{
				ret = inib.getInitialFact().getExpressionText();
			}
			else
			{
				IMExpression dv = bel.getDefaultFact();
				if(dv!=null)
					ret = dv.getExpressionText();
			}
		}
		else if(elem instanceof IMBeliefReference)
		{
			IMBeliefReference belref = (IMBeliefReference)elem;
			IMConfiguration inis = configname!=null? scope.getConfigurationbase().getConfiguration(configname)
				: scope.getConfigurationbase().getDefaultConfiguration();
			IMConfigBelief inib = null;
			if(inis!=null && inis.getBeliefbase()!=null)
				inib = inis.getBeliefbase().getInitialBelief(belref);
			if(inib!=null)
			{
				ret = inib.getInitialFact().getExpressionText();
			}
			else if(inis!=null)
			{
				// Find the initial state name of the referenced capability
				// It is possibly defined in the initial state
				String inistatename = null;
				String capaname = belref.getReference().substring(0, belref.getReference().indexOf("."));
				IMConfigCapability[] incaps= inis.getCapabilitybase().getCapabilityConfigurations();
				for(int i=0; i<incaps.length; i++)
				{
					if(incaps[i].getReference().equals(capaname))
					{
						inistatename = incaps[i].getConfiguration();
					}
				}
		
				// Fetch the referenced element.
				ret = findValue(belref.getReferencedElement(), inistatename);
			}
		}
		return ret;
	}		

	/**
	 *  Set the agent name.
	 *  @param name The name.
	 */
	protected void setAgentName(final String name)
	{
		if(name!=null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					agentname.setText(name);
				}
			});
		}
	}

	/**
	 *  Set the auto generate in gui.
	 *  @param autogen The autogen property.
	 */
	protected void setAutoGenerate(final String autogen)
	{
		if(autogen!=null)
		{
			final boolean state = new Boolean(autogen).booleanValue();
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					genagentname.setSelected(state);
					agentname.setEditable(!genagentname.isSelected());
				}
			});
		}
	}
	
	/**
	 *  Get the last loaded filename.
	 *  @return The filename.
	 */
	public String getFilename()
	{
		return lastfile;
	}

	/**
	 *  Main for testing only.
	 *  @param args The arguments.
	 */
	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.getContentPane().add(new StarterPanel(null));
		f.pack();
		f.setVisible(true);
	}
}