/*
 * LookAndFeel.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 10, 2004.  
 * Last revision $Revision: 5234 $ by:
 * $Author: 8bade $ on $Date: 2007-06-08 10:40:07 +0200 (Fri, 08 Jun 2007) $.
 */
package jadex.tools.tracer.ui;

import java.awt.Color;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;

/** This class preloades icons for the tracer gui
 * <code>LookAndFeel</code>
 * @since Nov 10, 2004
 */
public class LookAndFeel
{
  /** <code>AGENT_ICON</code> the icon for an agent */
  public static final ImageIcon  AGENT_ICON          = getIcon("/jadex/tools/common/images/new_agent.png");

  /** <code>AGENT_COLOR</code> the color of an agent node */
  public static final Color      AGENT_COLOR         = new Color(160, 160, 250);

  /** <code>AGENT_ICON</code> the icon for an agent */
  public static final ImageIcon  WATCHED_AGENT_ICON  = getIcon("/jadex/tools/common/images/new_agent_introspected.png");

  /** <code>AGENT_COLOR</code> the color of an agent node */
  public static final Color      WATCHED_AGENT_COLOR = new Color(175, 175, 255);

  /** <code>AGENT_ICON</code> the icon for an agent */
  public static final ImageIcon  IGNORED_AGENT_ICON  = getIcon("/jadex/tools/common/images/new_agent.png");

  /** <code>AGENT_COLOR</code> the color of an agent node */
  public static final Color      IGNORED_AGENT_COLOR = new Color(160, 160, 200);

  /** <code>AGENT_ICON</code> the icon for a death agent */
  public static final ImageIcon  DAGENT_ICON         = getIcon("/jadex/tools/common/images/agent_dead.png");

  /** <code>AGENT_COLOR</code> the color of a death agent node */
  public static final Color      DAGENT_COLOR        = new Color(150, 150, 160);

  
  // Todo: icons for actions...
  
  /** <code>ACTION_ICON</code> the icon for action */
  public static final ImageIcon  ACTION_ICON         = getIcon("icons/Action.png");

  /** <code>ACTION_COLOR</code>  the color for action */
  public static final Color      ACTION_COLOR        = new Color(255, 150, 150);

  
  
  /** <code>BELIEF_ICON</code> the icon for belief */
  public static final ImageIcon  BELIEF_ICON         = getIcon("/jadex/tools/common/images/bulb2.png");

  /** <code>BELIEF_COLOR</code>  */
  public static final Color      BELIEF_COLOR        = new Color(150, 180, 255);

  /** <code>BELIEF_TOOLTIP_SIZE</code> the number of events schown in the belief tooltip */
  public static final int        BELIEF_TOOLTIP_SIZE = 10;


   // Todo: other icons for belief reads/writes???

  /** <code>BELIEF_ICON</code> the icon for belief */
  public static final ImageIcon  READ_ICON           = getIcon("/jadex/tools/common/images/bulb2.png");

  /** <code>BELIEF_COLOR</code>  */
  public static final Color      READ_COLOR          = BELIEF_COLOR;

  /** <code>BELIEF_ICON</code> the icon for belief */
  public static final ImageIcon  WRITE_ICON          = getIcon("/jadex/tools/common/images/bulb1.png");

  /** <code>BELIEF_COLOR</code>  */
  public static final Color      WRITE_COLOR         = BELIEF_COLOR;

  
  
  /** <code>PLAN_ICON</code> */
  public static final ImageIcon  PLAN_ICON           = getIcon("/jadex/tools/common/images/plan2.png");

  /** <code>PLAN_COLOR</code>  */
  public static final Color      PLAN_COLOR          = new Color(255, 150, 230);

  /** <code>GOAL_ICON</code>  */
  public static final ImageIcon  GOAL_ICON           = getIcon("/jadex/tools/common/images/cloud2.png");

  /** <code>ACHIEVEGOAL_ICON</code>  */
  public static final ImageIcon  ACHIEVEGOAL_ICON           = getIcon("/jadex/tools/common/images/cloud2a.png");

  /** <code>PERFORMGOAL_ICON</code>  */
  public static final ImageIcon  PERFORMGOAL_ICON           = getIcon("/jadex/tools/common/images/cloud2p.png");

  /** <code>QUERYGOAL_ICON</code>  */
  public static final ImageIcon  QUERYGOAL_ICON           = getIcon("/jadex/tools/common/images/cloud2q.png");

  /** <code>MAINTAINGOAL_ICON</code>  */
  public static final ImageIcon  MAINTAINGOAL_ICON           = getIcon("/jadex/tools/common/images/cloud2m.png");

  /** <code>METAGOAL_ICON</code>  */
  public static final ImageIcon  METAGOAL_ICON       = getIcon("/jadex/tools/common/images/cloud2meta.png");

  /** <code>GOAL_COLOR</code>  */
  public static final Color      GOAL_COLOR          = new Color(140, 255, 170);

  /** <code>RECEIVE_ICON</code>  */
  public static final ImageIcon  RECEIVE_ICON        = getIcon("/jadex/tools/common/images/new_received_message.png");

  /** <code>RECEIVE_COLOR</code>  */
  public static final Color      RECEIVE_COLOR       = new Color(60, 190, 255);

  /** <code>COMMUNICATION_ICON</code>  */
  public static final ImageIcon  SEND_ICON           = getIcon("/jadex/tools/common/images/new_sent_message.png");

  /** <code>SEND_COLOR</code>  */
  public static final Color      SEND_COLOR          = new Color(60, 190, 255);

  // Todo: new icon for internal event...
  
  /** <code>EVENT_ICON</code> */
  public static final ImageIcon  EVENT_ICON          = getIcon("icons/Event.png");

  /** <code>EVENT_COLOR</code>  */
  public static final Color      EVENT_COLOR         = new Color(255, 230, 150);

  /** <code>SELECT_COLOR</code>  */
  public static final Color      SELECT_COLOR        = new Color(250, 230, 230);
  
  /** <code>EDGE_COLOR</code> the color of edges */
  public static final Color      EDGE_COLOR          = new Color(160, 160, 160);
  
  /** <code>EDGE_SELECT_COLOR</code> the color of selected edges */
  public static final Color      EDGE_SELECT_COLOR   = new Color(160, 160, 160);

  /** <code>TIME_FORMAT</code>  formats a date to show the hour and minutes */
  public static final DateFormat TIME                = new SimpleDateFormat("ss.mm.HH");

  /** <code>GRAPH_COLOR</code> the default color for the Graph Layout panel */
  public static final Color      GRAPH_COLOR         = Color.WHITE;

  /** <code>DRAG_COLOR</code> Color to be used when draging the node */
  public static final Color      DRAG_COLOR          = Color.BLACK;

  /** <code>DRAG_COLOR</code> Color to be used when draging the node */
  public static final Color      HIGHLIGHT           = Color.RED;

  /** <code>MAX_LABEL_LENGTH</code>:  the length of a label int tree and table */
  public static final int        MAX_LABEL_LENGTH    = 100;

  /** <code>STACK_DEPTH</code>: is the number of lines used to print the stack in tooltips */
  public static final int        STACK_DEPTH         = 10;

  /** <code>VALUE_MAX_LINES</code>: maximum number of lines  */
  public static final int        VALUE_MAX_LINES     = 30;

  static
  {
    ToolTipManager ttm = ToolTipManager.sharedInstance();
    ttm.setDismissDelay(20000);
    ttm.setReshowDelay(0);
    ttm.setInitialDelay(100);
  }

  /** 
   * @param fileName
   * @return an icon from this directory;
   */
  protected final static ImageIcon getIcon(String fileName)
  {
    URL url = LookAndFeel.class.getResource(fileName);
    return new ImageIcon(url);
  }

}

/*  
 * $Log$
 * Revision 1.2  2005/11/24 12:45:45  walczak
 * *** empty log message ***
 *
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.1  2005/04/26 13:45:50  pokahr
 * *** empty log message ***
 *
 * Revision 1.8  2005/03/17 15:08:19  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.7  2005/02/07 17:54:56  9walczak
 * LookAndFeel.
 *
 * Revision 1.6  2005/02/07 11:37:09  9walczak
 * Changed the look and feel a bit.
 *
 * Revision 1.5  2005/02/05 22:03:41  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.4  2005/01/28 14:34:11  braubach
 * no message
 *
 * Revision 1.3  2005/01/11 18:29:54  9walczak
 * Addopted IntrospectorPreprocessor approach
 * to communicate with agents to be traced.
 * Replaced many tracer hooks by SystemEvents.
 *
 * Revision 1.2  2004/12/22 18:25:43  9walczak
 * First time in main version.
 *
 * Revision 1.1.2.5  2004/12/22 17:36:39  9walczak
 * *** empty log message ***
 *
 * Revision 1.1.2.4  2004/12/06 15:52:03  9walczak
 * Added a menu to the tracer gui.
 *
 * Revision 1.1.2.3  2004/11/30 12:37:04  9walczak
 * Added tooltips to the graph.
 * Added selection highlight.
 * Added mousewheel zoom.
 * Added death agent icon.
 *
 * Revision 1.1.2.2  2004/11/24 18:02:50  9walczak
 * Added Graph View  to the tracer GUI.
 * Belief traces are differentiated in Writes and Reads.
 *
 * Revision 1.1.2.1  2004/11/18 18:03:28  9walczak
 * Createt new version of tracer with:
 *    -proxy (java 2 jade),
 *    -tracerAgent (jade 2 gui),
 *    -gui (tree, table, [graph]).
 * Tested on blocksworld.
 *
 */