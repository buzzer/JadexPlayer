package jadex.tools.jcc;

import jadex.runtime.Plan;

/**
 *  Update the agent list.
 */
public class AgentListUpdatePlan extends Plan
{
   /**
    *  Plan body.
    */
   public void body()
   {
      ControlCenter ctrl = (ControlCenter) getBeliefbase().getBelief("jcc").getFact();
      Object[] agents = getBeliefbase().getBeliefSet("agents").getFacts();
      if(ctrl != null && agents != null)
      {
         ctrl.agentlist.updateAgents(agents);
      }
   }
}