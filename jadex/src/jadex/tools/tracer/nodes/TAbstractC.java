/*
 * TAbstractC.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Jan 3, 2005.  
 * Last revision $Revision: 5005 $ by:
 * $Author: pokahr $ on $Date: 2007-03-14 17:50:46 +0100 (Wed, 14 Mar 2007) $.
 */
package jadex.tools.tracer.nodes;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.ontology.OTrace;
import jadex.tools.tracer.ui.LookAndFeel;

import java.util.StringTokenizer;

/**
 * <code>TAbstractC</code>. An abstract superclass for all communication nodes.
 * @since Jan 3, 2005
 */
public abstract class TAbstractC extends TNode
{

  /** Init
   * Constructor: <code>TAbstractC</code>.
   * Constructs a node with a label cut to MAX_LABEL_LENGTH from L&F
   * @param aid
   * @param trace
   */
  public TAbstractC(AgentIdentifier aid, OTrace trace)
  {
    super(aid, trace);
    setLabel(trace.getValue());
  }

  /**
   * @return true
   */
  public boolean isCommunication()
  {
    return true;
  }

  /** 
   * @return 
   * @see java.lang.Object#toString()
   */
  public String getToolTip()
  {
    if (tooltip==null)
    {
      StringBuffer buf = new StringBuffer();

      buf.append("<HTML><TABLE>");

      appendLine(buf, getTraceType().toUpperCase(), name);

      appendLisp(buf, "Value", trace.getValue(), LookAndFeel.VALUE_MAX_LINES);

      appendRest(buf);

      buf.append("</TABLE></HTML>");

      return buf.toString();
    }

    return tooltip;
  }

  /** appends a line of lisp to the tooltip
   * @param buf
   * @param name
   * @param lisp
   * @param lines
   */
  protected void appendLisp(StringBuffer buf, String name, String lisp, int lines)
  {
    buf.append("<TR valign=\"top\"><TD><B>");
    buf.append(name);
    buf.append("</B></TD><TD>=</TD><TD>");
    appendLispSub(buf, lisp, lines);
    buf.append("</TD></TR>");
  }

  /**  A preatty printer for LISP
   * @param buf
   * @param lisp
   * @param lines
   */
  protected void appendLispSub(StringBuffer buf, String lisp, int lines)
  {
    StringTokenizer tok = new StringTokenizer(lisp, " (:)\"", true);
    int i = 0; // the number of right parenthesis
    boolean rp = true;
    boolean sp = false;
    boolean qt = false;
    while(tok.hasMoreTokens()&&lines>0)
    {
      String t = tok.nextToken();
      if ("(".equals(t))
      {
        i++;
        if ( !rp)
        {
          buf.append("<br>");
          lines--;
          appendWS(buf, i*3);
          rp = true;
        }
      }
      else if (")".equals(t))
      {
        i--;
      }
      else if (":".equals(t)&&sp)
      {
        if (tok.hasMoreTokens())
        {
          t = tok.nextToken();
          buf.append("<br>");
          lines--;
          appendWS(buf, i*3);
          buf.append(':');
          buf.append("<i><b>");
          buf.append(t);
          buf.append("</b></i>");
          rp = true;
          continue; // dont
        }
      }
      else
      {
        sp = " ".equals(t);
        qt ^= "\"".equals(t); 
        if ( !sp)
        {
          rp = false;
          
        }
      }
      escape(buf, t, qt);
    }
  }

  /** Append white spaces to buffer 
   * @param buf
   * @param i
   */
  protected void appendWS(StringBuffer buf, int i)
  {
    //System.out.println("Stub: TAbstractC.appendWS");
    while(i-->0)
    {
      buf.append("&nbsp;");
    }
  }

  /** 
   * @return a pretty printed version of the message
   */
  public String getMsg()
  {
    // System.out.println("Stub: TAbstractC.getMsg");
    StringBuffer buf = new StringBuffer("<HTML>");
    appendLispSub(buf, trace.getValue(), LookAndFeel.VALUE_MAX_LINES+10);
    buf.append("</HTML>");
    return buf.toString();
  }

}

/*  
 * $Log$
 * Revision 1.1  2005/08/02 15:13:11  braubach
 * alpha version of new platform independent tracer
 *
 * Revision 1.2  2005/05/26 15:41:18  9walczak
 * Fixed a deathlock between AWT and TTGPanel.fireAfterMove().
 *
 * Revision 1.1  2005/04/26 13:45:49  pokahr
 * *** empty log message ***
 *
 * Revision 1.7  2005/03/17 15:08:20  9walczak
 * Tested for multithreading. Major fixes.
 *
 * Revision 1.6  2005/02/05 22:03:42  9walczak
 * Created new Ontology. Removed sync methods.
 * Made all filters positive. User may specify filter from the gui.
 * Many fixes.
 *
 * Revision 1.5  2005/01/28 14:34:11  braubach
 * no message
 *
 * Revision 1.4  2005/01/27 09:24:17  9walczak
 * minor fixes
 *
 * Revision 1.3  2005/01/26 13:47:29  9walczak
 * Beta release. Many fixes.
 *
 * Revision 1.2  2005/01/11 18:29:54  9walczak
 * Addopted IntrospectorPreprocessor approach
 * to communicate with agents to be traced.
 * Replaced many tracer hooks by SystemEvents.
 *
 * Revision 1.1  2005/01/03 15:38:00  9walczak
 * Communication events can be joined by an edge,
 * if they correspond to the same message.
 * The ACLMessages are pretty printed in tooltips.
 * Minor fixes.
 *
 */