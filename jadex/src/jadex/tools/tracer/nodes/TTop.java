/*
 * TTop.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 18, 2004.  
 * Last revision $Revision: 3040 $ by:
 * $Author: braubach $ on $Date: 2005-08-02 15:13:37 +0000 (Tue, 02 Aug 2005) $.
 */
package jadex.tools.tracer.nodes;

import java.awt.Color;

import javax.swing.ImageIcon;

/** 
 * <code>TTop</code>
 * @since Nov 18, 2004
 */
public class TTop extends TNode
{

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getIcon()
   */
  public ImageIcon getIcon()
  {
    return null;
  }

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getColor()
   */
  public Color getColor()
  {
    return Color.WHITE;
  }

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getTraceType()
   */
  public String getTraceType()
  {
    return TOP;
  }

  /** 
   * @return false
   * @see com.touchgraph.graphlayout.Node#isVisible()
   */
  public boolean isVisible()
  {
    return false;
  }

  /** 
   * @return TOP
   * @see jadex.tools.tracer.nodes.TNode#getToolTip()
   */
  public String getToolTip()
  {
    return TOP;
  }

  /** 
   * @return false
   */
  public boolean isRoot()
  {
    return true;
  }

}

/*  
 * $Log$
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.1  2005/04/26 13:45:49  pokahr
 * *** empty log message ***
 *
 * Revision 1.6  2005/03/17 15:08:20  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.5  2005/02/05 22:03:42  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.4  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 * Revision 1.3  2005/01/03 15:38:00  9walczak
 * Communication events can be joined by an edge,
 * if they correspond to the same message.
 * The ACLMessages are pretty printed in tooltips.
 * Minor fixes.
 *
 * Revision 1.2  2004/12/22 18:25:43  9walczak
 * First time in main version.
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