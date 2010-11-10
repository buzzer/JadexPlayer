package jadex.tools.introspector.debugger;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import jadex.tools.common.GuiProperties;
import jadex.util.SGUI;
import jadex.util.jtable.ObjectTableModel;

/**
 *
 */
public class ClassicDebuggerPanel extends JPanel
{
	//-------- constants --------

	/** The lazy retrieval map. */
	protected static UIDefaults def = new UIDefaults(new Object[]
	{
		"right", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/arrowright.png"),
		"top", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/arrowtop.png"),
		"up", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/arrowup.png"),
		"down", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/arrowdown.png"),
		"bottom", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/arrowbottom.png"),
		"delete", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/delete.png"),
		"empty", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/empty.png"),
		"icon", SGUI.makeIcon(ClassicDebuggerPanel.class, "/jadex/tools/common/images/bug_small.png")
	});

	//-------- attributes --------

	/** The dispatcher mode. */
	protected JComboBox dispchoice;

	/** The dispatcher execution state. */
	protected JTextField dispexestate;

	/** The dispatcher step request. */
	protected JButton dispstep;

	/** The dispatcher step count. */
	protected JComboBox dispsteps;

	/** The dispatcher step count of the agent. */
	protected JTextField actdispsteps;

	/** The dispatcher selected event label. */
	protected JLabel seleventl;

	/** The dispatcher selected event. */
	protected JTextField selevent;

	/** The scheduler mode. */
	protected JComboBox schedchoice;


	/** The scheduler state. */
	protected JTextField schedstateview;

	/** The scheduler step request. */
	protected JButton schedstep;

	/** The actual scheduler step count. */
	protected JComboBox schedsteps;

	/** The actual scheduler step count of the agent. */
	protected JTextField actschedsteps;


	/** The event moving to the top button. */
	protected JButton eventtop;

	/** The event moving up button. */
	protected JButton eventup;

	/** The event moving down button. */
	protected JButton eventdown;

	/** The event moving to the bottom button. */
	protected JButton eventbottom;

	/** The event delete button. */
	protected JButton eventdelete;

	/** The rplan moving to the top button. */
	protected JButton readytop;

	/** The rplan moving up button. */
	protected JButton readyup;

	/** The rplan moving down button. */
	protected JButton readydown;

	/** The rplan moving to the bottom button. */
	protected JButton readybottom;

	/** The rplan delete button. */
	protected JButton readydelete;


	/** The event list. */
	protected JTable el;

	/** The ready list. */
	protected JTable rl;

	/** The applicable candidates list. */
	protected JTable al;

	/** The selected candidates list. */
	protected JTable sl;

	/** The event list model. */
	protected ObjectTableModel elm;

	/** The ready list model. */
	protected ObjectTableModel rlm;

	/** The applicables list model. */
	protected ObjectTableModel alm;

	/** The selected candidates list model. */
	protected ObjectTableModel slm;

	/** The content. */
	protected JSplitPane content;

	/** This map contains
	 *  rplanname -> changeevent
	 *  pairs. */
	protected HashMap listelems; //todo: remove.

	/* The gui listener. */
	protected GuiListener gl;


	//-------- constructors --------

	/**
	 *  Create a new debugger tab.
	 */
	public ClassicDebuggerPanel()
	{
		createGUI();
		createListener();
		this.listelems = new HashMap();
	}

	//-------- methods --------

	/**
	 *  Handle a change occured event.
	 *  @param ce The change occured event.
	 * /
	public void changeOccurred(final ChangeEvent ce)
	{
		//System.out.println("Introspector event: "+ce.toSLString());
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// Handle info messages from the target.
				//System.out.println("Received a message from the target: "+info);
				//Map values = SUtil.parseSLToMap(info);

				String what = ce.getType();
				//System.out.println("ce: "+ce);

				// lists

				if(ISteppable.EVENT_LIST_ELEMENT_ADDED.equals(what))
				{
					Map event = (Map)ce.getAttributeValue("event");
					String name = ""+event.get("name");
					Icon icon = SGUI.getElementIcon(event.get("class"));
					int pos = Integer.parseInt(""+ce.getAttributeValue("pos"));
					elm.insertRow(pos, new Object[]{icon, name}, event);
					//System.out.println("tool: "+elm.getRowCount());
				}
				else if(ISteppable.EVENT_LIST_ELEMENT_REMOVED.equals(what))
				{
					elm.removeRow(ce.getAttributeValue("event"));
					//System.out.println("tool: "+elm.getRowCount());
				}
				else if(ISteppable.READY_LIST_ELEMENT_ADDED.equals(what))
				{
					Map rplan = (Map)ce.getAttributeValue("rplan");
					String rplanname = (String)rplan.get("name");
					Icon icon = SGUI.getElementIcon(rplan.get("class"));
					int pos = Integer.parseInt(""+ce.getAttributeValue("pos"));
					rlm.insertRow(pos, new Object[]{icon, rplanname}, rplanname);
					listelems.put(rplanname, rplan);
				}
				else if(ISteppable.READY_LIST_ELEMENT_REMOVED.equals(what))
				{
					Map rplan = (Map)ce.getAttributeValue("rplan");
					String rplanname = (String)rplan.get("name");
					rlm.removeRow(rplanname);
					listelems.remove(rplanname);
				}

				// dispatcher

				else if(ISteppable.DISPATCHER_STEP_DONE.equals(what))
				{
					String state = ""+ce.getAttributeValue("state");
					if(!state.equals("null"))
					{
						//System.out.println("* * * * * Disp step done: "+ce.getAttributeValue("steps"));

						dispexestate.setText(state);
						actdispsteps.setText(""+ce.getAttributeValue("steps"));

						if(DispatcherBehaviour.STATE_EVENTSELECTED.equals(state))
						{
							Map event = (Map)ce.getAttributeValue("event");
							//System.out.println("+++ "+event);
							Icon eventclass = SGUI.getElementIcon(event.get("class"));
							seleventl.setIcon(eventclass);
							selevent.setText(""+event.get("name"));
							selevent.setCaretPosition(0);
							listelems.put("selevent", event);
						}
						else if(DispatcherBehaviour.STATE_APPLICABLES.equals(state))
						{
							List aps = (List)ce.getAttributeValue("applicables");
							//System.out.println("apps: "+aps);
							for(int i=0; aps!=null && i<aps.size(); i++) // Hack?
							{
								Map cand = (Map)aps.get(i);
								if(cand==null)
									throw new RuntimeException("hהההה! "+aps);
								Icon icon = SGUI.getElementIcon(cand.get("class"));
								alm.addRow(new Object[]{icon, cand.get("name")}, cand);
							}
						}
						else if(DispatcherBehaviour.STATE_MLREASONING.equals(state))
						{
							List schs = (List)ce.getAttributeValue("candidates");
							for(int i=0; schs!=null&& i<schs.size(); i++) // Hack?
							{
								Map cand = (Map)schs.get(i);
								Icon icon = SGUI.getElementIcon(cand.get("class"));
								slm.addRow(new Object[]{icon, cand.get("name")}, cand);
								//listelems.put(cand, ce);
							}
						}
						else if(DispatcherBehaviour.STATE_INITIAL.equals(state))
						{
							dispexestate.setText("");
							seleventl.setIcon(def.getIcon("empty"));
							selevent.setText("");
							listelems.remove("selevent");
							alm.removeAllRows();
							slm.removeAllRows();
						}
					}
				}
				else if(ISteppable.DISPATCHER_MODE_CHANGED.equals(what))
				{
					actdispsteps.setText(""+ce.getAttributeValue("steps"));
					String mode = ""+ce.getAttributeValue("mode");

					dispchoice.removeActionListener(gl);
					dispchoice.setSelectedItem(mode);
					if(!SUtil.toList(dispchoice.getActionListeners()).contains(gl))
						dispchoice.addActionListener(gl);

					if("step".equals(mode) || "cycle".equals(mode))
					{
						dispstep.setEnabled(true);
						dispsteps.setEnabled(true);
						actdispsteps.setEnabled(true);
						if("cycle".equals(mode))
							dispexestate.setText("");
					}
					else
					{
						selevent.setText("");
						listelems.remove("selevent");
						seleventl.setIcon(def.getIcon("empty"));
						dispexestate.setText("");
						dispstep.setEnabled(false);
						dispsteps.setEnabled(false);
						actdispsteps.setEnabled(false);
						alm.removeAllRows();
						slm.removeAllRows();
					}
				}
				else if(ISteppable.DISPATCHER_STEPS_CHANGED.equals(what))
				{
					actdispsteps.setText(""+ce.getAttributeValue("steps"));
				}

				// Scheduler

				else if(ISteppable.SCHEDULER_STEP_DONE.equals(what))
				{
					schedstateview.setText("step finished");
					actschedsteps.setText(""+ce.getAttributeValue("steps"));
				}
				else if(ISteppable.SCHEDULER_MODE_CHANGED.equals(what))
				{
					String mode = (String)ce.getAttributeValue("mode");

					schedchoice.removeActionListener(gl);
					schedchoice.setSelectedItem(mode);
					if(!SUtil.toList(schedchoice.getActionListeners()).contains(gl))
						schedchoice.addActionListener(gl);

					actschedsteps.setText(""+ce.getAttributeValue("steps"));

					if("step".equals(mode))
					{
						schedstep.setEnabled(true);
						schedsteps.setEnabled(true);
						actschedsteps.setEnabled(true);
					}
					else
					{
						schedstep.setEnabled(false);
						schedsteps.setEnabled(false);
						actschedsteps.setEnabled(false);
						for(int i=0; i<rlm.getRowCount(); i++)
						{
							Object id = rlm.getObjectForRow(0);
							rlm.removeRow(0);
							if(listelems.remove(id)==null)
								throw new RuntimeException("Inconsistent ready list.");
						}
					}
				}
				else if(ISteppable.SCHEDULER_STEPS_CHANGED.equals(what))
				{
					actschedsteps.setText(""+ce.getAttributeValue("steps"));
				}
			}
		});
	}*/

	/**
	 *  Update the gui with the entries contained in the agenda.
	 */
	public void updateGui(List entries)
	{
		//System.out.println("Update called!");
		elm.removeAllRows();
		rlm.removeAllRows();
		for(int i=0; i<entries.size(); i++)
		{
			if(entries.get(i) instanceof AgendaEntryInfo)
			{
				AgendaEntryInfo entry = (AgendaEntryInfo)entries.get(i);
				//AgendaActionInfo aai = (AgendaActionInfo)entry.getAction();
				if(entry.getActionClassName().equals("ProcessEventAction") && !entry.isProcessed())
				{
					elm.addRow(new Object[]{GuiProperties.getElementIcon(entry.getActionClassName()), "?"}, entry);
				}
				else if(entry.getActionClassName().equals("ExecutePlanStepAction") && !entry.isProcessed())
				{
					rlm.addRow(new Object[]{GuiProperties.getElementIcon(entry.getActionClassName()), "?"}, entry);
				}
			}
		}
	}

	/**
	 *  Clear the view when refreshing.
	 *  To be overriden to perform custom cleanup.
	 */
	protected void	clear()
	{
		// Clear tables.
		elm.removeAllRows();
		rlm.removeAllRows();
		alm.removeAllRows();
		slm.removeAllRows();
	}

	//-------- helper methods --------

	/**
	 *  Create the gui elements.
	 */
	protected void createGUI()
	{
		Insets insets = new Insets(2, 6, 4, 4);
		Insets noi = new Insets(2, 0, 2, 4);
		//Insets noi = new Insets(0, 0, 0, 0);

		// REvent list - Panel elp
		this.elm = new ObjectTableModel(new String[]{"Type","Event"});
		elm.setColumnClass(Icon.class, 0);
		this.eventtop = new JButton();
		this.eventup = new JButton();
		this.eventdown = new JButton();
		this.eventbottom = new JButton();
		this.eventdelete = new JButton();
		eventtop.setIcon(def.getIcon("top"));
		eventup.setIcon(def.getIcon("up"));
		eventdown.setIcon(def.getIcon("down"));
		eventbottom.setIcon(def.getIcon("bottom"));
		eventdelete.setIcon(def.getIcon("delete"));
		this.el = new JTable(elm);
		el.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane elsp = new JScrollPane(el);
		elsp.getViewport().setBackground(Color.WHITE);
		JPanel elp = new JPanel(new GridBagLayout());
		elp.add(elsp, new GridBagConstraints(0, 0, 1, 5, 1, 1,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, noi, 0, 0));
		elp.add(eventtop,  new GridBagConstraints(1, 0, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		elp.add(eventup, new GridBagConstraints(1, 1, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		elp.add(eventdown, new GridBagConstraints(1, 2, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		elp.add(eventbottom, new GridBagConstraints(1, 3, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		elp.add(eventdelete, new GridBagConstraints(1, 4, 1, 1, 0, 1,
			GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));

		// Ready list - Panel rlp
		this.rlm = new ObjectTableModel(new String[]{"Type", "Plan Instance"});
		rlm.setColumnClass(Icon.class, 0);
		this.readytop = new JButton();
		this.readyup = new JButton();
		this.readydown = new JButton();
		this.readybottom = new JButton();
		this.readydelete = new JButton();
		readytop.setIcon(def.getIcon("top"));
		readyup.setIcon(def.getIcon("up"));
		readydown.setIcon(def.getIcon("down"));
		readybottom.setIcon(def.getIcon("bottom"));
		readydelete.setIcon(def.getIcon("delete"));
		this.rl = new JTable(rlm);
		JScrollPane rlsp = new JScrollPane(rl);
		rlsp.getViewport().setBackground(Color.WHITE);
		JPanel rlp = new JPanel(new GridBagLayout());
		rlp.add(rlsp, new GridBagConstraints(0, 0, 1, 5, 1, 0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, noi, 0, 0));
		rlp.add(readytop,  new GridBagConstraints(1, 0, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		rlp.add(readyup, new GridBagConstraints(1, 1, 1, 1, 0, 0,
			GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		rlp.add(readydown, new GridBagConstraints(1, 2, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		rlp.add(readybottom, new GridBagConstraints(1, 3, 1, 1, 0, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));
		rlp.add(readydelete, new GridBagConstraints(1, 4, 1, 1, 0, 1,
			GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, noi, 0, 0));

		// Dispatcher

		// Applicables list
		this.alm = new ObjectTableModel(new String[]{"Type","Candidates"});
		alm.setColumnClass(Icon.class, 0);
		this.al = new JTable(alm);
		JScrollPane alsp = new JScrollPane(al);
		alsp.getViewport().setBackground(Color.WHITE);

		// Scheduled candidates list
		this.slm = new ObjectTableModel(new String[]{"Type", "Scheduled Candidates"});
		slm.setColumnClass(Icon.class, 0);
		this.sl = new JTable(slm);
		JScrollPane slsp = new JScrollPane(sl);
		slsp.getViewport().setBackground(Color.WHITE);

		// Execution control
		this.dispchoice = new JComboBox(new String[]{"normal", "cycle", "step"});
		this.dispstep = new JButton();
		this.dispstep.setIcon(def.getIcon("right"));
		this.dispsteps = new JComboBox(new String[]{"1","2","3","4","5","10","100"});
		this.dispsteps.setEditable(true);
		this.actdispsteps = new JTextField(3);
		this.actdispsteps.setEditable(false);
		this.dispexestate = new JTextField();
		this.dispexestate.setEditable(false);
		JPanel exe = new JPanel(new GridBagLayout());
		//JToolBar exe = new JToolBar("Dispatcher Control");
		//exe.setLayout(new GridBagLayout());
		exe.add(new JLabel("Mode"), new GridBagConstraints(0, 0, 1, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(dispchoice, new GridBagConstraints(1, 0, 3, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(new JLabel("Execute"), new GridBagConstraints(0, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(dispsteps, new GridBagConstraints(1, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(dispstep, new GridBagConstraints(2, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(actdispsteps, new GridBagConstraints(3, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(new JLabel("State"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(dispexestate,  new GridBagConstraints(1, 2, 3, 1, 0, 0,
			GridBagConstraints.CENTER,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		exe.add(new JPanel(), new GridBagConstraints(0, 3, 2, 1, 1, 1,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,  new Insets(0,0,0,0), 0, 0));
		JPanel disp = new JPanel(new BorderLayout());
		disp.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Dispatcher Control"));
		disp.add("Center", exe);

		// Panel with selected event
		JPanel selep = new JPanel(new GridBagLayout());
		this.selevent = new JTextField();
		this.selevent.setEditable(false);
		this.selevent.setHorizontalAlignment(SwingConstants.LEFT);
		this.seleventl = new JLabel("Event");
		seleventl.setIcon(def.getIcon("empty"));
		seleventl.setHorizontalTextPosition(JLabel.LEFT);
		selep.add(seleventl,  new GridBagConstraints(0, 0, 1, 1, 0, 1,
			GridBagConstraints.NORTH,  GridBagConstraints.HORIZONTAL, insets, 0, 0));
		selep.add(selevent,  new GridBagConstraints(1, 0, 2, 1, 1, 1,
			GridBagConstraints.NORTH,  GridBagConstraints.HORIZONTAL, insets, 0, 0));

		// Scheduler
		this.schedstateview = new JTextField("running");
		schedstateview.setEditable(false);
		this.schedchoice = new JComboBox(new String[]{"normal", "step"});
		this.schedstep = new JButton();
		this.schedstep.setIcon(def.getIcon("right"));
		this.schedsteps = new JComboBox(new String[]{"1","2","3","4","5","10","100"});
		this.schedsteps.setEditable(true);
		this.actschedsteps = new JTextField(3);
		this.actschedsteps.setEditable(false);
		JPanel sched = new JPanel(new GridBagLayout());
		Border schedbord = new TitledBorder(new EtchedBorder(
			EtchedBorder.LOWERED), "Scheduler Control");
		sched.setBorder(schedbord);
		sched.add(new JLabel("Mode"), new GridBagConstraints(0, 0, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(schedchoice, new GridBagConstraints(1, 0, 3, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(new JLabel("Execute"), new GridBagConstraints(0, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(schedsteps, new GridBagConstraints(1, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(schedstep, new GridBagConstraints(2, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(actschedsteps, new GridBagConstraints(3, 1, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(new JLabel("State"), new GridBagConstraints(0, 2, 1, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(schedstateview, new GridBagConstraints(1, 2, 3, 1, 0, 0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		sched.add(new JPanel(), new GridBagConstraints(0, 3, 4, 1, 1, 1,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,  new Insets(0,0,0,0), 0, 0));

		// Set minimum and preferred sizes (Hack?)
		eventtop.setMinimumSize(new Dimension(20, 20));
		eventup.setMinimumSize(new Dimension(20, 20));
		eventdown.setMinimumSize(new Dimension(20, 20));
		eventbottom.setMinimumSize(new Dimension(20, 20));
		eventdelete.setMinimumSize(new Dimension(20, 20));
		eventtop.setPreferredSize(new Dimension(20, 20));
		eventup.setPreferredSize(new Dimension(20, 20));
		eventdown.setPreferredSize(new Dimension(20, 20));
		eventbottom.setPreferredSize(new Dimension(20, 20));
		eventdelete.setPreferredSize(new Dimension(20, 20));
		readytop.setMinimumSize(new Dimension(20, 20));
		readyup.setMinimumSize(new Dimension(20, 20));
		readydown.setMinimumSize(new Dimension(20, 20));
		readybottom.setMinimumSize(new Dimension(20, 20));
		readydelete.setMinimumSize(new Dimension(20, 20));
		readytop.setPreferredSize(new Dimension(20, 20));
		readyup.setPreferredSize(new Dimension(20, 20));
		readydown.setPreferredSize(new Dimension(20, 20));
		readybottom.setPreferredSize(new Dimension(20, 20));
		readydelete.setPreferredSize(new Dimension(20, 20));
		readydelete.setPreferredSize(new Dimension(20, 20));
		dispstep.setMinimumSize(new Dimension(30, (int)dispstep.getMinimumSize().getHeight()));
		dispstep.setPreferredSize(new Dimension(30, (int)dispstep.getPreferredSize().getHeight()));
		dispsteps.setMinimumSize(new Dimension(60, (int)dispsteps.getMinimumSize().getHeight()));
		dispsteps.setPreferredSize(new Dimension(60, (int)dispsteps.getPreferredSize().getHeight()));
		actdispsteps.setMinimumSize(actdispsteps.getPreferredSize());
		schedstep.setMinimumSize(new Dimension(30, (int)schedstep.getMinimumSize().getHeight()));
		schedstep.setPreferredSize(new Dimension(30, (int)schedstep.getPreferredSize().getHeight()));
		schedsteps.setMinimumSize(new Dimension(60, (int)schedsteps.getMinimumSize().getHeight()));
		schedsteps.setPreferredSize(new Dimension(60, (int)schedsteps.getPreferredSize().getHeight()));
		actschedsteps.setMinimumSize(actschedsteps.getPreferredSize());
		disp.setMinimumSize(disp.getPreferredSize());
		elp.setMinimumSize(new Dimension(130, (int)disp.getMinimumSize().getHeight()));
		elp.setPreferredSize(new Dimension(200, (int)disp.getPreferredSize().getHeight()));
		//alsp.setMinimumSize(new Dimension(200, ((int)disp.getMinimumSize().getHeight()-(int)selep.getMinimumSize().getHeight()-2*(insets.bottom+insets.top))/2));
		alsp.setPreferredSize(new Dimension(200, ((int)disp.getPreferredSize().getHeight()-(int)selep.getMinimumSize().getHeight()-2*(insets.bottom+insets.top))/2));
		//slsp.setMinimumSize(new Dimension(200, ((int)disp.getMinimumSize().getHeight()-(int)selep.getMinimumSize().getHeight()-2*(insets.bottom+insets.top))/2));
		slsp.setPreferredSize(new Dimension(200, ((int)disp.getPreferredSize().getHeight()-(int)selep.getMinimumSize().getHeight()-2*(insets.bottom+insets.top))/2));
		//el.getColumnModel().getColumn(0).setMinWidth(40);
		el.getColumnModel().getColumn(0).setMaxWidth(40);
		//al.getColumnModel().getColumn(0).setMinWidth(40);
		al.getColumnModel().getColumn(0).setMaxWidth(40);
		//sl.getColumnModel().getColumn(0).setMinWidth(40);
		sl.getColumnModel().getColumn(0).setMaxWidth(40);
		//rl.getColumnModel().getColumn(0).setMinWidth(40);
		rl.getColumnModel().getColumn(0).setMaxWidth(40);

		// Construct the complete view
		JPanel top = new JPanel(new GridBagLayout());
		top.add(disp, new GridBagConstraints(0, 0, 1, 1, 0, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		JSplitPane right = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		right.add(elp);
		right.setOneTouchExpandable(true);
		JPanel r1 = new JPanel(new GridBagLayout());
		r1.add(selep, new GridBagConstraints(0, 0, 1, 1, 1, 0,
			GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		r1.add(alsp, new GridBagConstraints(0, 1, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		r1.add(slsp, new GridBagConstraints(0, 2, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		right.add(r1);
		top.add(right, new GridBagConstraints(1, 0, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));

		rlp.setMinimumSize(new Dimension(200, (int)sched.getMinimumSize().getHeight()));
		rlp.setPreferredSize(new Dimension(200, (int)sched.getPreferredSize().getHeight()));
		JPanel middle = new JPanel(new GridBagLayout());
		middle.add(sched, new GridBagConstraints(0, 0, 1, 1, 0, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));
		middle.add(rlp, new GridBagConstraints(1, 0, 1, 1, 1, 1,
			GridBagConstraints.NORTH, GridBagConstraints.BOTH, insets, 0, 0));

		this.content = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		this.content.setOneTouchExpandable(true);
		this.content.setResizeWeight(0.5);
		this.content.add(top);
		this.content.add(middle);

		this.setLayout(new BorderLayout());
		this.add("Center", content);
	}

	/**
	 *  Create local / remote listeners.
	 */
	protected void createListener()
	{
		this.gl = new GuiListener();
		eventtop.addActionListener(gl);
		eventup.addActionListener(gl);
		eventdown.addActionListener(gl);
		eventbottom.addActionListener(gl);
		eventdelete.addActionListener(gl);
		readytop.addActionListener(gl);
		readyup.addActionListener(gl);
		readydown.addActionListener(gl);
		readydelete.addActionListener(gl);
		dispchoice.addActionListener(gl);
		dispstep.addActionListener(gl);
		//dispsteps.addActionListener(gl);
		schedchoice.addActionListener(gl);
		schedstep.addActionListener(gl);
		//schedsteps.addActionListener(gl);
		selevent.addMouseListener(gl);
		el.addMouseListener(gl);
		rl.addMouseListener(gl);
		al.addMouseListener(gl);
		sl.addMouseListener(gl);
	}

	//-------- inner classes --------

	/**
	 *  The listener.
	 */
	protected class GuiListener extends MouseAdapter implements ActionListener
	{
		/**
		 *  Called when a gui action occurred.
		 */
		public void actionPerformed(ActionEvent ae)
		{
			Object source = ae.getSource();

			// REvent list

			/*if(source==eventtop)
			{
				int oldpos = el.getSelectedRow();
				if(oldpos>0) // oldpos!=-1 && oldpos!=0
				{
					String eventid = (String)((Map)elm.getObjectForRow(oldpos)).get("name");
					String cmd = "moveEvent "+eventid+" "+0;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}
			}
			else if(source==eventup)
			{
				int oldpos = el.getSelectedRow();
				if(oldpos>0) // oldpos!=-1 && oldpos!=0
				{
					String eventid = (String)((Map)elm.getObjectForRow(oldpos)).get("name");
					String cmd = "moveEvent "+eventid+" "+(oldpos-1);
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}
			}
			else if(source==eventdown)
			{
				int oldpos = el.getSelectedRow();
				if(oldpos!=-1 && oldpos<elm.getRowCount()-1)
				{
					String eventid = (String)((Map)elm.getObjectForRow(oldpos)).get("name");
					String cmd = "moveEvent "+eventid+" "+(oldpos+1);
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}
			}
			else if(source==eventbottom)
			{
				int oldpos = el.getSelectedRow();
				int newpos = elm.getRowCount()-1;
				if(oldpos!=-1 && oldpos<newpos)
				{
					String eventid = (String)((Map)elm.getObjectForRow(oldpos)).get("name");
					String cmd = "moveEvent "+eventid+" "+newpos;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}
			}
			else if(source==eventdelete)
			{
				int oldpos = el.getSelectedRow();
				if(oldpos!=-1)
				{
					String eventid = (String)((Map)elm.getObjectForRow(oldpos)).get("name");
					String cmd = "deleteEvent "+eventid;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("No element selected.");
				}
			}


			// Ready list

			else if(source==readytop)
			{
				int oldpos = rl.getSelectedRow();
				if(oldpos>0) // oldpos!=-1 && oldpos!=0
				{
					String rplanname = (String)rlm.getObjectForRow(oldpos);
					String cmd = "moveReady "+rplanname+" "+0;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}

			}
			else if(source==readyup)
			{
				int oldpos = rl.getSelectedRow();
				if(oldpos>0) // oldpos!=-1 && oldpos!=0
				{
					String rplanname = (String)rlm.getObjectForRow(oldpos);
					String cmd = "moveReady "+rplanname+" "+(oldpos-1);
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}

			}
			else if(source==readydown)
			{
				int oldpos = rl.getSelectedRow();
				if(oldpos!=-1 && oldpos<rlm.getRowCount()-1)
				{
					String rplanname = (String)rlm.getObjectForRow(oldpos);
					String cmd = "moveReady "+rplanname+" "+(oldpos+1);
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}
			}
			else if(source==readybottom)
			{
				int oldpos = rl.getSelectedRow();
				int newpos = rlm.getRowCount()-1;
				if(oldpos!=-1 && oldpos<newpos)
				{
					String rplanname = (String)rlm.getObjectForRow(oldpos);
					String cmd = "moveReady "+rplanname+" "+newpos;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("Element cannot be moved.");
				}
			}
			else if(source==readydelete)
			{
				int oldpos = rl.getSelectedRow();
				if(oldpos!=-1)
				{
					String rplanname = (String)rlm.getObjectForRow(oldpos);
					String cmd = "deleteReady "+rplanname;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
				else
				{
					System.out.println("No element selected.");
				}
			}

			// Dispatcher

			else if(source==dispchoice)
			{
				//System.out.println("Requesting disp step: "+dispchoice.getSelectedItem());
				String cmd = "setDispatcherExecutionMode "
						+dispchoice.getSelectedItem();
				ExecuteCommand com = new ExecuteCommand();
				com.setCommand(cmd);
				tool.performToolAction(com);
			}
			else if(source==dispstep)
			{
				int steps = -1;
				try{steps = Integer.parseInt((String)dispsteps.getSelectedItem());}
				catch(NumberFormatException e){}
				if(steps>-1)
				{
					String cmd = "setDispatcherSteps "+steps;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
			}

			// Scheduler

			else if(source==schedchoice)
			{
				//System.out.println("Requesting sched step: "+schedchoice.getSelectedItem());
				String state = (String)schedchoice.getSelectedItem();
				if(state.equals("normal"))
					schedstateview.setText("running");
				else if(state.equals("step"))
					schedstateview.setText("step finished");
				String cmd = "setSchedulerExecutionMode "+state;
				ExecuteCommand com = new ExecuteCommand();
				com.setCommand(cmd);
				tool.performToolAction(com);
			}
			else if(source==schedstep)
			{
				schedstateview.setText("step requested");
				int steps = -1;
				try{steps = Integer.parseInt((String)schedsteps.getSelectedItem());}
				catch(NumberFormatException e){}
				if(steps>-1)
				{
					String cmd = "setSchedulerSteps "+steps;
					ExecuteCommand com = new ExecuteCommand();
					com.setCommand(cmd);
					tool.performToolAction(com);
				}
			}*/
		}

		/**
		 *  Invoked when a component gains the keyboard focus.
		 */
		public void mouseClicked(MouseEvent me)
		{
			Object source = me.getSource();
			//System.out.println("me: "+me);
			/*if(me.getClickCount()==2)
			{
				if(source==selevent)
				{
					tool.showElementDetails((Map)listelems.get("selevent"));
				}
				else if(source==el)
				{
					int msi = el.getSelectionModel().getMinSelectionIndex();
					if(msi!=-1)
					{
						Map event = (Map)elm.getObjectForRow(el.getSelectionModel().getMinSelectionIndex());
						tool.showElementDetails(event);
					}
				}
				else if(source==rl)
				{
					int msi = rl.getSelectionModel().getMinSelectionIndex();
					if(msi!=-1)
					{
						String rplanname = (String)rlm.getObjectForRow(rl.getSelectionModel().getMinSelectionIndex());
						tool.showElementDetails((Map)listelems.get(rplanname));
					}

				}
				else if(source==al)
				{
					int msi = al.getSelectionModel().getMinSelectionIndex();
					if(msi!=-1)
					{
						Map cand = (Map)alm.getObjectForRow(al.getSelectionModel().getMinSelectionIndex());
						tool.showElementDetails(cand);
					}

				}
				else if(source==sl)
				{
					int msi = sl.getSelectionModel().getMinSelectionIndex();
					if(msi!=-1)
					{
						Map cand = (Map)slm.getObjectForRow(sl.getSelectionModel().getMinSelectionIndex());
						tool.showElementDetails(cand);
					}

				}
			}*/
		}
	}

	/**
	 *  Main for testing.
	 */
	public static void main(String[] args)
	{
		ClassicDebuggerPanel cdp = new ClassicDebuggerPanel();
		JFrame f = new JFrame();
		f.getContentPane().add("Center", cdp);
		f.pack();
		f.setVisible(true);
	}
}

