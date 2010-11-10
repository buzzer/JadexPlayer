package jadex.tools.tracer.nodes;

import java.awt.Color;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.ontology.OGoal;
import jadex.tools.ontology.OTrace;
import jadex.tools.tracer.ui.LookAndFeel;

import javax.swing.ImageIcon;

/** 
 * <code>TGoal</code>
 * @since Nov 10, 2004
 */
public class TGoal extends TNode
{

  /**
   *  Default Constructor: <code>TGoal</code>.
   * 
   *  @param aid
   *  @param trace
   */
  public TGoal(AgentIdentifier aid, OTrace trace)
  {
    super(aid, trace);
  }

  /** 
   * @return the icon
   * @see jadex.tools.tracer.nodes.TNode#getIcon()
   */
  public ImageIcon getIcon()
  {
    return OGoal.GOALKIND_ACHIEVE.equals(((OGoal)trace).getGoalKind()) ? LookAndFeel.ACHIEVEGOAL_ICON
    	: OGoal.GOALKIND_PERFORM.equals(((OGoal)trace).getGoalKind()) ? LookAndFeel.PERFORMGOAL_ICON
    	: OGoal.GOALKIND_QUERY.equals(((OGoal)trace).getGoalKind()) ? LookAndFeel.QUERYGOAL_ICON
    	: OGoal.GOALKIND_MAINTAIN.equals(((OGoal)trace).getGoalKind()) ? LookAndFeel.MAINTAINGOAL_ICON
    	: OGoal.GOALKIND_META.equals(((OGoal)trace).getGoalKind()) ? LookAndFeel.METAGOAL_ICON
    	: LookAndFeel.GOAL_ICON;
  }

  /** 
   * @return the color of this node
   * @see jadex.tools.tracer.nodes.TNode#getColor()
   */
  public Color getColor()
  {
    return LookAndFeel.GOAL_COLOR;
  }

  /** 
   * @return 
   * @see jadex.tools.tracer.nodes.TNode#getTraceType()
   */
  public String getTraceType()
  {
    return "Goal";
  }

}