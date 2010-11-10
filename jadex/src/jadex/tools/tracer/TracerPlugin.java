/*
 * TracerPlugin.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by walczak on Nov 2, 2005.  
 * Last revision $Revision: 5230 $ by:
 * $Author: braubach $ on $Date: 2007-06-08 10:24:22 +0200 (Fri, 08 Jun 2007) $.
 */
package jadex.tools.tracer;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.IMessageEvent;
import jadex.tools.common.plugin.*;
import jadex.tools.jcc.AbstractJCCPlugin;
import jadex.tools.ontology.OTrace;
import jadex.tools.starter.StarterPlugin;
import jadex.tools.tracer.ui.TracerUI;
import jadex.util.SGUI;

import java.util.Properties;

import javax.swing.*;

/**
 * 
 */
public class TracerPlugin extends AbstractJCCPlugin implements IAgentListListener, IMessageListener
{

	//-------- static part --------

   /** The image icons. */
   protected static final UIDefaults icons = new UIDefaults(new Object[]
   {
		"tracer",      SGUI.makeIcon(TracerPlugin.class, "/jadex/tools/common/images/new_tracer.png"),
		"tracer_sel", SGUI.makeIcon(StarterPlugin.class, "/jadex/tools/common/images/new_tracer_sel.png"),
   });

   static final String PROPS_FILE="tracer.properties";
   
   private TracerController     ctrl;
   
   private TracerUI             ui;

   /**
    * @param mdi
    */
   public void init(IControlCenter mdi)
   {
	  this.ctrl = new TracerController(mdi.getAgent());    
      super.init(mdi);
   }

   /** 
    * @return the name of this plugin:"TracerPlugin"
    * @see jadex.tools.common.plugin.IControlCenterPlugin#getName()
    */
   public String getName()
   {
      return "Tracer";
   }

	/**
	 *  Create menu bar.
	 *  @return The menu bar.
	 */
	public JMenu[] createMenuBar()
	{
		 return ui.createMenuBar();
	}
	
	/**
	 *  Create main panel.
	 *  @return The main panel.
	 */
	public JComponent createView()
	{
		 this.ui = new TracerUI(ctrl);
	      ctrl.setUI(ui);
	      jcc.addAgentListListener(this);
	      jcc.addMessageListener(this);
	      return ui;
	}

   /** 
    * @return the tracer icon
    * @see jadex.tools.common.plugin.IControlCenterPlugin#getToolIcon()
    */
   public Icon getToolIcon(boolean selected)
   {
	  return selected? icons.getIcon("tracer_sel"): icons.getIcon("tracer");
      //return new ImageIcon(TracerPlugin.class.getResource("ui/icons/tracer.png"), "TracerPlugin");
   }

   /** 
    * @param ad
    */
   public void agentDied(final AMSAgentDescription ad)
   {
      ctrl.agentsDied(ad);
   }

   /** 
    * @param ad
    */
   public void agentBorn(final AMSAgentDescription ad)
   {             
      ctrl.agentBorn(ad);
   }
   
   /** 
    * @param ad
    */
   public void agentChanged(final AMSAgentDescription ad)
   {    
	   // nop?
   }

	/** 
	 * @param message
	 * @return true if processed
	 */
	public boolean processMessage(IMessageEvent message)
	{
		boolean	processed	= false;
		Object content = message.getContent();
		if (content instanceof OTrace)
		{
			Object	obj	 = message.getParameter("sender").getValue();
			AgentIdentifier	aid	= (AgentIdentifier)obj;
			ctrl.add(aid, (OTrace)content);
			processed	= true;
		}
		return processed;
	}

	/** 
	 * @param ps
	 * @see jadex.tools.jcc.AbstractJCCPlugin#getProperties(java.util.Properties)
	 */
	public void getProperties(Properties props)
	{
		ui.getProperties(props);
		ctrl.getProperties(props);
	}

	/** 
	 * @param ps
	 * @see jadex.tools.jcc.AbstractJCCPlugin#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties props)
	{
		ctrl.setProperties(props);
		ui.setProperties(props);
	}

	/** 
	 * @return the help id of the perspective
	 * @see jadex.tools.jcc.AbstractJCCPlugin#getHelpID()
	 */
	public String getHelpID()
	{
		return "tools.tracer";
	}
	
	/**
	 *  Reset the plugin.
	 */
	public void	reset()
	{
		ctrl.resetPrototype();
		ui.resetDefaultFilter();
		// Todo: disable tracing of current agents???
		// but tracing not saved in project!?
	}
}
