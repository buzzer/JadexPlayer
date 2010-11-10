/*
 * NavigateUI.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics.
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 30, 2004.
 * Last revision $Revision: 3718 $ by:
 * $Author: braubach $ on $Date: 2005-11-30 16:22:03 +0000 (Wed, 30 Nov 2005) $.
 *
 * Parts based on TouchGraph
 * -----------------------------------------------------------------------
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
 */
package jadex.tools.tracer.ui;

import com.touchgraph.graphlayout.*;
import com.touchgraph.graphlayout.interaction.DragNodeUI;
import com.touchgraph.graphlayout.interaction.LocalityScroll;
import com.touchgraph.graphlayout.interaction.TGAbstractClickUI;
import com.touchgraph.graphlayout.interaction.TGAbstractDragUI;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import jadex.util.*;

/** GLNavigateUI. User interface for moving around the graph, as opposed
 * to editing.
 *   
 * @author   Alexander Shapiro                                        
 * @author   Murray Altheim (abstracted GLPanel to TGScrollPane interface)
 * @version  1.21  $Id: NavigateUI.java 3718 2005-11-30 16:22:03 +0000 (Wed, 30 Nov 2005) braubach $
 */
public class NavigateUI extends MouseAdapter
{
  /** <code>tgPanel</code>  */
  protected final GraphPanel gPanel;

  /** <code>tgPanel</code>  */
  protected final TGPanel    tgPanel;

  TGAbstractDragUI           hvDragUI;

  TGAbstractDragUI           rotateDragUI;

  //TGAbstractDragUI hvRotateDragUI;

  TGAbstractClickUI          hvScrollToCenterUI;

  DragNodeUI                 dragNodeUI;

  LocalityScroll             localityScroll;

  JPopupMenu                 nodePopup;

  JPopupMenu                 edgePopup;

  Node                       popupNode;

  Edge                       popupEdge;

  /** Init
   * Constructor: <code>NavigateUI</code>.
   * 
   * @param gPanel
   */
  public NavigateUI(GraphPanel gPanel)
  {
    this.gPanel = gPanel;
    this.tgPanel = gPanel.tgPanel;

    localityScroll = gPanel.localityScroll;
    hvDragUI = gPanel.hvScroll.getHVDragUI();
    rotateDragUI = gPanel.rotateScroll.getRotateDragUI();

    hvScrollToCenterUI = gPanel.hvScroll.getHVScrollToCenterUI();
    dragNodeUI = new DragNodeUI(tgPanel);

    setUpNodePopup();
    setUpEdgePopup();
  }

  /** 
   * @param e 
   * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      triggerPopup(e);
    }
    else
    {
      Node mouseOverN = tgPanel.getMouseOverN();
      if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
      {
        if (mouseOverN==null) hvDragUI.activate(e);
        else dragNodeUI.activate(e);
      }
    }
  }

  /** 
   * @param e 
   * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e)
  {
    Node mouseOverN = tgPanel.getMouseOverN();
    if (e.getModifiers()==MouseEvent.BUTTON1_MASK)
    {
      if (mouseOverN!=null)
      {
        tgPanel.setSelect(mouseOverN);
        try
        {
          tgPanel.setLocale(mouseOverN, localityScroll.getLocalityRadius());
        }
        catch(TGException ex)
        {
          //System.out.println("Error setting locale");
          //ex.printStackTrace();
			String failed = SUtil.wrapText("Error setting locale\n\n"+ex.getMessage());
			JOptionPane.showMessageDialog(SGUI.getWindowParent(tgPanel), failed, "Locale Error", JOptionPane.ERROR_MESSAGE);

        }
        //hvScrollToCenterUI.activate(e);                        
      }
    }
  }

  /** 
   * @param e 
   * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e)
  {
    if (e.isPopupTrigger())
    {
      triggerPopup(e);
    }
  }

  private void triggerPopup(MouseEvent e)
  {
    popupNode = tgPanel.getMouseOverN();
    popupEdge = tgPanel.getMouseOverE();
    if (popupNode!=null)
    {
      tgPanel.setMaintainMouseOver(true);
      tgPanel.setSelect(popupNode);
      nodePopup.show(e.getComponent(), e.getX(), e.getY());
    }
    else if (popupEdge!=null)
    {
      tgPanel.setMaintainMouseOver(true);
      edgePopup.show(e.getComponent(), e.getX(), e.getY());
    }
    else
    {
      gPanel.glPopup.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  private void setUpNodePopup()
  {
    JPopupMenu menu = new JPopupMenu();

    gPanel.fillPopupMenu(menu);
    menu.addPopupMenuListener(new PopupMenuListener()
    {
      public void popupMenuCanceled(PopupMenuEvent e)
      { /* NO OP */}

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
      {
        tgPanel.setMaintainMouseOver(false);
        tgPanel.setMouseOverN(null);
        tgPanel.repaint();
      }

      public void popupMenuWillBecomeVisible(PopupMenuEvent e)
      { /* NO OP */}
    });

    nodePopup = menu;
  }

  private void setUpEdgePopup()
  {
    edgePopup = new JPopupMenu();
    JMenuItem menuItem;

    menuItem = new JMenuItem("Hide Edge");
    ActionListener hideAction = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (popupEdge!=null)
        {
          tgPanel.hideEdge(popupEdge);
        }
      }
    };

    menuItem.addActionListener(hideAction);
    edgePopup.add(menuItem);

    edgePopup.addPopupMenuListener(new PopupMenuListener()
    {
      public void popupMenuCanceled(PopupMenuEvent e)
      { /* NO OP */}

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
      {
        tgPanel.setMaintainMouseOver(false);
        tgPanel.setMouseOverE(null);
        tgPanel.repaint();
      }

      public void popupMenuWillBecomeVisible(PopupMenuEvent e)
      { /* NO OP */}
    });
  }

} 