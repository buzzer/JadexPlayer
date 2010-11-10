/*
 * GraphPanel.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 24, 2004.  
 * Last revision $Revision: 5024 $ by:
 * $Author: pokahr $ on $Date: 2007-03-15 17:34:04 +0100 (Thu, 15 Mar 2007) $.
 * 
 * This file is based on GLPanel from TouchGraph GraphLayout package:
 ************************************************************************* 
 * 
 * TouchGraph LLC. Apache-Style Software License
 *
 *
 * Copyright (c) 2001-2002 Alexander Shapiro. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by 
 *        TouchGraph LLC (http://www.touchgraph.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "TouchGraph" or "TouchGraph LLC" must not be used to endorse 
 *    or promote products derived from this software without prior written 
 *    permission.  For written permission, please contact 
 *    alex@touchgraph.com
 *
 * 5. Products derived from this software may not be called "TouchGraph",
 *    nor may "TouchGraph" appear in their name, without prior written
 *    permission of alex@touchgraph.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL TOUCHGRAPH OR ITS CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR 
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 */

package jadex.tools.tracer.ui;

import jadex.tools.common.SelectAction;
import jadex.tools.tracer.nodes.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.ToolTipManager;

import com.touchgraph.graphlayout.Edge;
import com.touchgraph.graphlayout.GraphListener;
import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGException;
import com.touchgraph.graphlayout.TGLensSet;
import com.touchgraph.graphlayout.TGPanel;
import com.touchgraph.graphlayout.graphelements.TGForEachEdge;
import com.touchgraph.graphlayout.graphelements.TGForEachNode;
import com.touchgraph.graphlayout.interaction.HVScroll;
import com.touchgraph.graphlayout.interaction.LocalityScroll;
import com.touchgraph.graphlayout.interaction.RotateScroll;
import com.touchgraph.graphlayout.interaction.ZoomScroll;

/** GLPanel contains code for adding scrollbars and interfaces to the TGPanel
 * The "GL" prefix indicates that this class is GraphLayout specific, and
 * will probably need to be rewritten for other applications.
 *
 * @author   Alexander Shapiro
 * @version  1.21  $Id: GraphPanel.java 5024 2007-03-15 16:34:04Z pokahr $
 */
public class GraphPanel extends JPanel implements MouseWheelListener, TNodeListener
{
  /** <code>zoomLabel</code> label for zoom menu item */
  public static final String zoomLabel     = "Zoom";    // 

  /** <code>rotateLabel</code> label for rotate menu item */
  public static final String rotateLabel   = "Rotate";  // 

  /** <code>localityLabel</code> label for locality menu item */
  public static final String localityLabel = "Locality"; // 

  /** <code>hvScroll</code> */
  public HVScroll            hvScroll;

  /** <code>zoomScroll</code>  */
  public ZoomScroll          zoomScroll;

  /** <code>rotateScroll</code>  */
  public RotateScroll        rotateScroll;

  /** <code>localityScroll</code>  */
  public LocalityScroll      localityScroll;

  /** <code>currentSB</code>  */
  protected JScrollBar       currentSB;

  /** <code>glPopup</code> */
  public JPopupMenu          glPopup;

  /** <code>scrollBarHash</code> */
  public Map                 scrollBarHash;

  /** <code>tgPanel</code>  */
  protected TGPanel          tgPanel;

  /** <code>tgLensSet</code> */
  protected TGLensSet        tgLensSet;

  /** <code>ui</code> uplink */
  protected final TracerUI   ui;

  /** Default constructor.
   * @param ui
   */
  public GraphPanel(TracerUI ui)
  {
    this.ui = ui;
    scrollBarHash = new Hashtable();
    tgLensSet = new TGLensSet();
    tgPanel = new TTGPanel();
    hvScroll = new HVScroll(tgPanel, tgLensSet);
    zoomScroll = new ZoomScroll(tgPanel);
    rotateScroll = new RotateScroll(tgPanel);
    localityScroll = new LocalityScroll(tgPanel);

    localityScroll.setLocalityRadius(10);

    ToolTipManager.sharedInstance().registerComponent(tgPanel);
    buildPanel();

    tgLensSet.addLens(hvScroll.getLens());
    tgLensSet.addLens(zoomScroll.getLens());
    tgLensSet.addLens(rotateScroll.getLens());
    tgLensSet.addLens(tgPanel.getAdjustOriginLens());

    tgPanel.setLensSet(tgLensSet);

    tgPanel.addMouseListener(new NavigateUI(this));

    tgPanel.addMouseWheelListener(this);

    setVisible(true);
  }

  /**
   *  
   * @see com.touchgraph.graphlayout.GLPanel#buildPanel()
   */
  public void buildPanel()
  {
    final JScrollBar horizontalSB = hvScroll.getHorizontalSB();
    final JScrollBar verticalSB = hvScroll.getVerticalSB();
    final JScrollBar zoomSB = zoomScroll.getZoomSB();
    final JScrollBar rotateSB = rotateScroll.getRotateSB();
    final JScrollBar localitySB = localityScroll.getLocalitySB();

    setLayout(new BorderLayout());

    final JPanel scrollPanel = new JPanel();
    scrollPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    final JPanel topPanel = new JPanel();
    topPanel.setLayout(new GridBagLayout());

    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(0, 0, 0, 0);
    c.gridx = 1;
    c.weightx = 1;

    scrollBarHash.put(zoomLabel, zoomSB);
    scrollBarHash.put(rotateLabel, rotateSB);
    scrollBarHash.put(localityLabel, localitySB);

    JPanel scrollselect = scrollSelectPanel(new String[] {zoomLabel, rotateLabel, localityLabel});
    topPanel.add(scrollselect, c);

    add(topPanel, BorderLayout.NORTH);

    c.fill = GridBagConstraints.BOTH;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 1;
    c.weighty = 1;
    scrollPanel.add(tgPanel, c);

    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0;
    c.weighty = 0;
    scrollPanel.add(verticalSB, c);

    c.gridx = 0;
    c.gridy = 2;
    scrollPanel.add(horizontalSB, c);

    add(scrollPanel, BorderLayout.CENTER);

    fillPopupMenu(glPopup = new JPopupMenu());

    glPopup.addSeparator();
    glPopup.add(new JMenuItem(new AbstractAction("Toggle Controls")
    {
      boolean controlsVisible = true;

      public void actionPerformed(ActionEvent e)
      {
        controlsVisible = !controlsVisible;
        horizontalSB.setVisible(controlsVisible);
        verticalSB.setVisible(controlsVisible);
        topPanel.setVisible(controlsVisible);
      }
    }));

  }

  /** 
   * creates a popup menu for this element
   * @param menu
   */
  public void fillPopupMenu(JPopupMenu menu)
  {
    JMenu submenu = new JMenu("Show");
    submenu.add(new JMenuItem(new AbstractAction("Actions")
    {
      public void actionPerformed(ActionEvent e)
      {
        showActions(true);
      }
    }));
    submenu.add(new JMenuItem(new AbstractAction("Beliefs")
    {
      public void actionPerformed(ActionEvent e)
      {
        showBeliefs(true);
      }
    }));
    submenu.add(new JMenuItem(new AbstractAction("Messages")
    {
      public void actionPerformed(ActionEvent e)
      {
        showMessages(true);
      }
    }));
    submenu.addSeparator();
    submenu.add(new JMenuItem(new AbstractAction("Trace causes")
    {
      public void actionPerformed(ActionEvent e)
      {
        showCauses(true);
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));
    submenu.add(new JMenuItem(new AbstractAction("Trace effects")
    {
      public void actionPerformed(ActionEvent e)
      {
        showEffects(true);
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }

    }));
    submenu.addMenuListener(TracerUI.MENU_ACTIVATOR);
    menu.add(submenu);

    submenu = new JMenu("Hide");
    submenu.add(new JMenuItem(new AbstractAction("Actions")
    {
      public void actionPerformed(ActionEvent e)
      {
        showActions(false);
      }
    }));
    submenu.add(new JMenuItem(new AbstractAction("Beliefs")
    {
      public void actionPerformed(ActionEvent e)
      {
        showBeliefs(false);
      }
    }));
    submenu.add(new JMenuItem(new AbstractAction("Send Events")
    {
      public void actionPerformed(ActionEvent e)
      {
        showMessages(false);
      }
    }));
    submenu.addSeparator();
    submenu.add(new JMenuItem(new AbstractAction("Trace")
    {
      public void actionPerformed(ActionEvent e)
      {
        TNode n = getSelected();
        showEffects(n, false);
        showTrace(n, false);
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));
    submenu.add(new JMenuItem(new AbstractAction("Trace causes")
    {
      public void actionPerformed(ActionEvent e)
      {
        showCauses(false);
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));

    submenu.add(new JMenuItem(new AbstractAction("Trace effects")
    {
      public void actionPerformed(ActionEvent e)
      {
        showEffects(false);
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));

    submenu.addMenuListener(TracerUI.MENU_ACTIVATOR);
    menu.add(submenu);

    menu.addSeparator();
    menu.add(new JMenuItem(new AbstractAction("Expand")
    {
      public void actionPerformed(ActionEvent e)
      {
        TNode n = getSelected();
        if (n!=null)
        {
          tgPanel.expandNode(n);
        }
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));

    menu.add(new JMenuItem(new AbstractAction("Collapse")
    {
      public void actionPerformed(ActionEvent e)
      {
        TNode n = getSelected();
        if (n!=null)
        {
          tgPanel.collapseNode(n);
        }
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));
    menu.add(new JMenuItem(new AbstractAction("Delete")
    {
      public void actionPerformed(ActionEvent e)
      {
        TNode n = getSelected();
        if (n!=null)
        {
          ui.ctrl.removeTrace(n);
        }
      }

      public boolean isEnabled()
      {
        return getSelected()!=null;
      }
    }));

    menu.addSeparator();
    menu.add(JOIN_BELIEFS.getCBItem());

    menu.add(JOIN_MESSAGES.getCBItem());
    menu.addSeparator();
    menu.add(LABEL_VIEW.getCBItem());

    menu.addPopupMenuListener(TracerUI.PMENU_ACTIVATOR);
  }

  /** 
   * @param scrollBarNames
   * @return a panel with many scrolls
   */
  protected JPanel scrollSelectPanel(String [] scrollBarNames)
  {
    final JComboBox scrollCombo = new JComboBox(scrollBarNames);
    scrollCombo.setPreferredSize(new Dimension(80, 20));
    scrollCombo.setSelectedIndex(0);
    currentSB = (JScrollBar)scrollBarHash.get(scrollBarNames[0]);
    scrollCombo.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        JScrollBar selectedSB = (JScrollBar)scrollBarHash.get(scrollCombo.getSelectedItem());
        if (currentSB!=null) currentSB.setVisible(false);
        if (selectedSB!=null) selectedSB.setVisible(true);
        currentSB = selectedSB;
      }
    });

    final JPanel sbp = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    sbp.add(scrollCombo, c);
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 1;
    c.insets = new Insets(0, 10, 0, 17);
    c.fill = GridBagConstraints.HORIZONTAL;
    for(int i = 0; i<scrollBarNames.length; i++)
    {
      JScrollBar sb = (JScrollBar)scrollBarHash.get(scrollBarNames[i]);
      if (sb==null) continue;
      if (i!=0) sb.setVisible(false);
      sbp.add(sb, c);
    }
    return sbp;
  }

  /** Zoom the graph
   * @param e 
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    currentSB.setValue(currentSB.getValue()-e.getWheelRotation());
  }

  /** 
   * @return the selected node or null
   */
  public TNode getSelected()
  {
    return (TNode)tgPanel.getSelect();
  }

  /** 
   * @param n
   */
  private void addNode(TNode n)
  {
    if (JOIN_BELIEFS.isSelected())
    {
      if (n instanceof TBeliefAccess)
      { // show the corresponding belief
        TBeliefAccess ba = (TBeliefAccess)n;
        addNode(ba.getBeliefNode());
        return;
      }
    }
    else
    { // split beliefs
      if (n instanceof TBelief)
      {
        showTraces(n.getChildren());
        return;
      }
    }

    try
    {
      tgPanel.addNode(n);
      Iterator it = n.parents();
      while(it.hasNext())
      {
        TNode parent = (TNode)it.next();
        if (parent.isVisible()&& !(parent instanceof TBelief))
        {
          TEdge e = new TEdge(parent, n);
          tgPanel.addEdge(e);
        }
      }

      if ( !(n instanceof TBelief))
      { // TBelief will not join with children
        Enumeration elems = n.children();
        while(elems.hasMoreElements())
        {
          TNode child = (TNode)elems.nextElement();
          if (child.isVisible())
          {
            TEdge e = new TEdge(n, child);
            tgPanel.addEdge(e);
          }
        }
      }
      n.addListener(this);

      if (JOIN_MESSAGES.isSelected()&&n.isCommunication())
      {
        List al = ui.ctrl.getComms(n.getName());
        if (al!=null&&al.size()>0)
        {
          if (n instanceof TSend)
          {
            int i = al.size();
            while(i-->1)
            {
              TNode rcv = (TNode)al.get(i);
              if (n!=rcv&&rcv.isVisible())
              { // sanity
                tgPanel.addEdge(new TEdge(n, rcv, TEdge.DEFAULT_LENGTH*3));
              }
            }
          }
          else
          { // instanceof TReceive
            TNode tn = (TNode)al.get(0);
            if (tn instanceof TSend&&tn.isVisible())
            {
              tgPanel.addEdge(new TEdge(tn, n, TEdge.DEFAULT_LENGTH*3));
            }
          }
        }
      }
      else

      if (JOIN_BELIEFS.isSelected()&&n instanceof TBelief)
      {
        TBelief bel = (TBelief)n;
        TNode[] bs = bel.getChildren();
        int bi=bs.length;
        while(bi-->0)
        {
          TNode[] ps = bs[bi].getParents();
          int pi=ps.length;
          while(pi-->0)
          {
            TNode p = ps[pi];
            if (p.isVisible())
            {
              TEdge edge = new TEdge(p, bel);
              tgPanel.addEdge(edge);
            }
          }
        }
      }
    }
    catch(TGException e)
    {
    	// Todo
//      e.printStackTrace();
    }
  }

  //  -------------- trace graph ------------------------
  
  // ------------------ node manipulation interface ---------------    

  /** Adds a node to this graph if any of its parents is visible 
   * @param tn
   */
  public void showTrace(TNode tn)
  {
    if (tn.hasVisibleParents())
    {
      addNode(tn);
    }
  }

  /** 
   * @param nodes
   */
  private void showTraces(TNode[] ns)
  {
    int i=ns.length;
    while(i-->0)
    {
      showTrace(ns[i]);
    }
  }

  /** Shows or hides the given trace in the graph
   * @param node
   * @param b
   */
  public void showTrace(TNode node, boolean b)
  {
    // System.out.println("Stub: GraphPanel.showTrace");
    if (node==null||node.getName()==TNode.TOP) return;
    if (b)
    {
      addNode(node);
    }
    else
    {
      node.removeListener(this);
      node.setVisible(false);
      tgPanel.deleteNode(node);
    }
  }

  /** Shows selected traces in the trace graph
   * @param nodes
   * @param b 
   */
  protected void showTraces(TNode[] nodes, boolean b)
  {
    //  System.out.println("Stub: GraphPanel.showTraces");
    int i=nodes.length;
    while(i-->0)
    {
      showTrace(nodes[i], b);
    }
  }

  /** Shows or hides effects of a trace
   * @param tn
   * @param b
   */
  protected void showEffects(TNode tn, boolean b)
  {
    if (tn==null) return;
    //  System.out.println("Stub: GraphPanel.showEffects");
    TNode[] cs = tn.getChildren();
    
    int ci=cs.length;
    while(ci-->0)
    {
      TNode n = cs[ci];
      if (!b || JOIN_BELIEFS.isSelected() || !(n instanceof TBelief)) { 
    	  showTrace(n, b);
    	  showEffects(n, b);
      }
    }
  }

  /** Shows or hides the effects of selected node int the trace graph
   * Only effects from the agent.
   * @param b
   */
  protected void showEffects(boolean b)
  {
    showEffects((TNode)tgPanel.getSelect(), b);
  }

  /** Shows or hides causes of a trace 
   * @param tn
   * @param b
   */
  protected void showCauses(TNode tn, boolean b)
  {
    if (tn==null) return;
    TNode[] ps = tn.getParents();

    // System.out.println("Stub: GraphPanel.showCauses");
    int pi = ps.length;
    while(pi-->0)
    {
      TNode n = ps[pi];
      showTrace(n, b);
      if (b)
      {
        showCauses(n, true);
      }
      else
      {
        showTraces(n.getChildren(), false);
      }
    }
  }

  /** Shows or hides the causes of selected node int the trace graph
   * Only causes from the agent.
   * @param b
   */
  protected void showCauses(boolean b)
  {
    showCauses((TNode)tgPanel.getSelect(), b);
  }

  /** <code>JOIN_BELIEFS</code>: a checkbox to join beliefs */
  protected SelectAction JOIN_BELIEFS = 
    new SelectAction("Join Beliefs")
    {
      public void flagChanged(boolean flag)
      {
        join_beliefs(flag);
      }
    };

  /** 
   * @param flag
   */
  protected void join_beliefs(boolean flag)
  {
    Iterator it = tgPanel.getAllNodes();
    if (it!=null) {
	    ArrayList nodes = new ArrayList(tgPanel.getNodeCount());
	    while(it.hasNext())
	    {
	      Object o = it.next();
	      if (flag&&o instanceof TBeliefAccess|| !flag&&o instanceof TBelief)
	      {
	        nodes.add(o);
	      }
	    }
	
	    int i = nodes.size();
	
	    while(i-->0)
	    {
	      Object o = nodes.get(i);
	      if (flag)
	      {
	        TBeliefAccess ba = (TBeliefAccess)o;
	        showTrace(ba.getBeliefNode(), true);
	        showTrace(ba, false);
	      }
	      else
	      {
	        TBelief bel = (TBelief)o;
	        showTrace(bel, false);
	        showTraces(bel.getChildren());
	      }
	
	    }
    }
  }

  /** <code>JOIN_MESSAGES</code>: a checkbox to join communications */
  protected SelectAction JOIN_MESSAGES = 
    new SelectAction("Join Messages")
    {
       public void flagChanged(boolean flag)
       {
         join_messages(flag);
       }
     };

  /** a method used to join and split messages shown
   * @param flag
   */
  protected void join_messages(boolean flag)
  {
    String[] cms = ui.ctrl.getComms();
    int i = cms.length;
    while(i-->0)
    {
      Object[] c = ui.ctrl.getComms(cms[i]).toArray();
      if (c!=null&&c.length>0)
      {
        TNode n = (TNode)c[0];
        if (n.isVisible()&&n instanceof TSend)
        {
          int j = c.length;
          while(j-->1)
          {
            TNode rcv = (TNode)c[j];
            if (rcv.isVisible())
            {
              if (flag)
              {
                tgPanel.addEdge(new TEdge(n, rcv, TEdge.DEFAULT_LENGTH*3));
              }
              else
              {
                tgPanel.deleteEdge(new TEdge(n, rcv));
              }
            }
          }
        }
      }
    }
  }

  /** <code>ICON_VIEW</code>: a checkbox to show labels instead of icons */
  protected SelectAction LABEL_VIEW = new SelectAction("Labels") {
    public void flagChanged(boolean flag)
    {
      tgPanel.repaint();
    }
  };

  /** 
   * @param bel
   * @param causes
   */
  protected void connectCauses(TBelief bel, List causes)
  {
    if ( !bel.isVisible()) return;
    Iterator it = causes.iterator();
    while(it.hasNext())
    {
      TNode n = (TNode)it.next();
      if (n.isVisible())
      {
        tgPanel.addEdge(new TEdge(n, bel, TEdge.DEFAULT_LENGTH*3));
      }
    }
  }

  /** 
   * @param bel
   * @param effects
   */
  protected void connectEffects(TBelief bel, List effects)
  {
    if ( !bel.isVisible()) return;
    Iterator it = effects.iterator();
    while(it.hasNext())
    {
      TNode n = (TNode)it.next();
      if (n.isVisible())
      {
        tgPanel.addEdge(new TEdge(bel, n, TEdge.DEFAULT_LENGTH*3));
      }
    }
  }

  /** no op
   * @param caller
   * @param node 
   */
  public void childAdded(TNode caller, TNode node)
  {
    // System.out.println("Stub: GraphPanel.childAdded");
    if (caller.isVisible()&&node.isVisible())
    {
      tgPanel.addEdge(new TEdge(caller, node));
    }
  }

  /** no op
   * @param caller
   * @param node 
   */
  public void parentAdded(TNode caller, TNode node)
  {
    // System.out.println("Stub: GraphPanel.parentAdded");
    if (caller.isVisible()&&node.isVisible())
    {
      tgPanel.addEdge(new TEdge(node, caller));
    }
  }

  /** removes an edge
   * @param caller
   * @param node
   * @param index 
   */
  public void childRemoved(TNode caller, TNode node, int index)
  {
    tgPanel.deleteEdge(new TEdge(caller, node));
  }

  /** removes an edge
   * @param caller
   * @param node
   * @param index 
   */
  public void parentRemoved(TNode caller, TNode node, int index)
  {
    tgPanel.deleteEdge(new TEdge(node, caller));
  }

  /** no op
   * @param caller 
   */
  public void nodeChanged(TNode caller)
  {
  // System.out.println("Stub: GraphPanel.nodeChanged");
  }

  /** removes the edge from graph
   * @param caller 
   */
  public void nodeDeleted(TNode caller)
  {
    showTrace(caller, false);
  }

  /** Shows all actions connected to 
   * @param b
   */
  public void showActions(boolean b)
  {
    if (b)
    {
      showNodes(TAction.class);
    }
    else
    {
      hideNodes(TAction.class);
    }
  }

  /** 
   * @param b
   */
  public void showBeliefs(boolean b)
  {
    if (b)
    {
      if (JOIN_BELIEFS.isSelected())
      {
        showNodes(TBelief.class);
      }
      else
      {
        showNodes(TBeliefAccess.class);
      }
    }
    else
    {
      hideNodes(TBeliefAccess.class);
      hideNodes(TBelief.class);
    }
  }

  /** 
   * @param b
   */
  public void showMessages(boolean b)
  {
    if (b)
    {
      showNodes(TAbstractC.class);
    }
    else
    {
      hideNodes(TSend.class);
    }
  }

  /** Shows nodes of specified class if they are connected to a visible
   * child or parent. 
   * @param clazz
   */
  private void showNodes(Class clazz)
  {
    Iterator it = tgPanel.getAllNodes();
    if (it!=null) {
	    ArrayList nodes = new ArrayList(tgPanel.getNodeCount());
	    while(it.hasNext())
	    {
	      Object o = it.next();
	      nodes.add(o);
	    }
	
	    int i = nodes.size();
	    while(i-->0)
	    {
	      TNode n = (TNode)nodes.get(i);
	      // parents
	      TNode[] ps = n.getParents();
	      int pi = ps.length;
	      while(pi-->0)
	      {
	        TNode p = ps[pi];
	        if ( !p.isVisible()&&clazz.isInstance(p))
	        {
	          showTrace(p, true);
	        }
	      }
	
	      // children
	      TNode[] cs = n.getChildren();
	      int ci = cs.length;
	      while(ci-->0)
	      {
	        TNode c = cs[ci];
	        if ( !c.isVisible()&&clazz.isInstance(c))
	        {
	          showTrace(c, true);
	        }
	      }
	    }
    }
  }

  /** hides nodes of specified class
   * @param clazz
   */
  private void hideNodes(Class clazz)
  {
    Iterator it = tgPanel.getAllNodes();
    if (it!=null) {
	    ArrayList nodes = new ArrayList(tgPanel.getNodeCount());
	    while(it.hasNext())
	    {
	      Object o = it.next();
	      if (clazz.isInstance(o))  nodes.add(o);
	    }
	
	    int i = nodes.size();
	    while(i-->0)
	    {
	      showTrace((TNode)nodes.get(i), false);
	    }
    }
  }

  class TTGPanel extends TGPanel
  {
    
    /** <code>graphListeners</code>: */
    protected final Vector graphListeners=new Vector();
  
    /** <code>offgraphics</code> */
    protected Graphics  offgraphics;

    /** <code>offscreen</code>  */
    protected Image     offscreen;

    /** <code>offscreensize</code>  */
    protected Dimension offscreensize;

    /** <code>paintNodes</code>  */
    protected final TGForEachNode paintNodes = 
    new TGForEachNode() 
    {
      public void forEachNode(Node n)
      {
        if (LABEL_VIEW.isSelected())
        {
          n.paint(offgraphics, TTGPanel.this);
        }
        else
        {
          ((TNode)n).paintIcon(offgraphics);
        }
      }
    };
    
    /** <code>paintEdges</code> */
    protected final TGForEachEdge paintEdges = 
    new TGForEachEdge()
    {
      public void forEachEdge(Edge edge)
      {
        edge.paint(offgraphics, TTGPanel.this);
      }
    };
  
    
    /** 
     * @param event
     * @return tooltip for the node the mouse is over
     * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
     */
    public String getToolTipText(MouseEvent event)
    {
      TNode mouseOver = (TNode)getMouseOverN();
      if (mouseOver!=null) { return mouseOver.getToolTip(); }
      return super.getToolTipText(event);
    }
    
    /** 
     * @param g 
     * @see com.touchgraph.graphlayout.TGPanel#update(java.awt.Graphics)
     */
    public synchronized void update(Graphics g)
    {
      Dimension d = getSize();
      if ((offscreen==null)||(d.width!=offscreensize.width)||(d.height!=offscreensize.height))
      {
        offscreen = createImage(d.width, d.height);
        offscreensize = d;
        offgraphics = offscreen.getGraphics();

        processGraphMove();
        findMouseOver();
      }

      offgraphics.setColor(BACK_COLOR);
      offgraphics.fillRect(0, 0, d.width, d.height);

      getGES().forAllEdges(paintEdges);

      getGES().forAllNodes(paintNodes);

      if (mouseOverE!=null)
      { //Make the edge the mouse is over appear on top.
        mouseOverE.paint(offgraphics, this);
        paintNodes.forEachNode(mouseOverE.from);
        paintNodes.forEachNode(mouseOverE.to);
      }

      if (select!=null)
      { //Make the selected node appear on top.
        select.paint(offgraphics, this);
      }

      if (mouseOverN!=null && mouseOverN!=select)
      { //Make the node the mouse is over appear on top.
        paintNodes.forEachNode(mouseOverN);
      }

      paintComponents(offgraphics); //Paint any components that have been added to this panel
      g.drawImage(offscreen, 0, 0, null);

    }
    
    /** 
     *  
     * @see com.touchgraph.graphlayout.TGPanel#repaintAfterMove()
     */
    public void repaintAfterMove()
    {
      GraphListener ls[]=null;
      synchronized(this) {
        processGraphMove();
        findMouseOver();

        if (graphListeners!=null) {
          ls = (GraphListener[])graphListeners.toArray(new GraphListener[graphListeners.size()]);
        }
      }
      
      if (ls!=null) for(int i=0; i<ls.length; i++) {ls[i].graphMoved();}
      repaint();
    }
    

    /** Overriden from touchgraph
     *  
     * @see com.touchgraph.graphlayout.TGPanel#fireResetEvent()
     */
    public void fireResetEvent() {
      GraphListener ls[]=null;
      synchronized(this) {
        if (graphListeners!=null) {
          ls = (GraphListener[])graphListeners.toArray(new GraphListener[graphListeners.size()]);
        }
      }
      if (ls!=null) for(int i=0; i<ls.length; i++) {ls[i].graphReset();}
    }
  
    /** Overriden from touchgraph
     * @param gl 
     * @see com.touchgraph.graphlayout.TGPanel#addGraphListener(com.touchgraph.graphlayout.GraphListener)
     */
    public void addGraphListener(GraphListener gl){
        graphListeners.addElement(gl);
        super.addGraphListener(gl);
    }
  
  
    /** Overriden from the touchgraph
     * @param gl 
     * @see com.touchgraph.graphlayout.TGPanel#removeGraphListener(com.touchgraph.graphlayout.GraphListener)
     */
    public void removeGraphListener(GraphListener gl){
        graphListeners.removeElement(gl);
        super.removeGraphListener(gl);
    }
  }

}

/*  
 * $Log$
 * Revision 1.7  2006/03/14 11:58:00  walczak
 * finished the DF Browser
 *
 * Revision 1.6  2005/11/25 19:42:18  pokahr
 * *** empty log message ***
 *
 * Revision 1.5  2005/11/24 14:56:12  walczak
 * *** empty log message ***
 *
 * Revision 1.4  2005/11/24 08:33:38  walczak
 * fixed bugs. added multiple select to agent table in starter
 *
 * Revision 1.3  2005/11/23 15:16:11  walczak
 * Polished the agent filter menu/dialog.
 *
 * Revision 1.2  2005/11/22 11:00:51  walczak
 * Removed unconnected beliefs from poping out
 *
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.2  2005/05/26 15:41:18  9walczak
 * Fixed a deathlock between AWT and TTGPanel.fireAfterMove().
 *
 * Revision 1.1  2005/04/26 13:45:50  pokahr
 * *** empty log message ***
 *
 * Revision 1.14  2005/03/17 15:08:19  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.13  2005/02/07 17:54:56  9walczak
 * LookAndFeel.
 *
 * Revision 1.12  2005/02/07 15:52:49  9walczak
 * fixed paint code
 *
 * Revision 1.10  2005/02/07 14:59:12  9walczak
 * Added: show icons feature  to the graph view.
 *
 * Revision 1.9  2005/02/05 22:03:41  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.8  2005/01/27 12:29:42  9walczak
 * Added a negative filter in the tracer agent.
 * Made the filter at TracerPreprocessor positive.
 *
 * Revision 1.7  2005/01/27 09:22:14  9walczak
 * minor fix
 *
 * Revision 1.6  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 * Revision 1.4  2005/01/06 15:07:20  braubach
 * no message
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
 * Revision 1.1.2.5  2004/12/22 17:36:39  9walczak
 * *** empty log message ***
 *
 * Revision 1.1.2.4  2004/12/13 17:48:23  9walczak
 * Bound the menu items and added collapse beliefs option.
 *
 * Revision 1.1.2.3  2004/12/06 15:52:03  9walczak
 * Added a menu to the tracer gui.
 *
 * Revision 1.1.2.2  2004/11/30 12:37:04  9walczak
 * Added tooltips to the graph.
 * Added selection highlight.
 * Added mousewheel zoom.
 * Added death agent icon.
 *
 * Revision 1.1.2.1  2004/11/24 18:02:50  9walczak
 * Added Graph View  to the tracer GUI.
 * Belief traces are differentiated in Writes and Reads.
 *
 */