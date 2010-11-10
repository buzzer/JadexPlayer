package jadex.tools.jcc;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.tools.common.plugin.IAgentListListener;

import java.util.Enumeration;
import java.util.Vector;

/**
 *  Update the agent list.
 */
public class AgentList extends Vector
{
   private final Vector listeners = new Vector();

   /**
    *  Plan body.
    * @param agents
    */
   public void updateAgents(Object[] agents)
   {
      Vector c = (Vector)clone();
      for(int i=0; i<agents.length; i++)
      {
         AMSAgentDescription ad = (AMSAgentDescription)agents[i];
         if(ad!=null)
         {
        	 int idx = c.indexOf(ad);
        	 if(idx!=-1)
        	 {
        		 AMSAgentDescription old = (AMSAgentDescription)c.remove(idx);
        		 if(!old.getState().equals(ad.getState()))
        		 {
        			 remove(ad);
        			 add(ad.clone());
        			 fireAgentChanged(ad);
        		 }
        	 }
        	 else
        	 {
        		 add(ad.clone());
        		 fireNewAgentEvent(ad);
        	 }
         }
      }

      Enumeration e = c.elements();
      while(e.hasMoreElements())
      {
         AMSAgentDescription agent = (AMSAgentDescription)e.nextElement();
         remove(agent);
         fireAgentDied(agent);
      }
   }

   /**
    * 
    * @param al
    */
   void addListener(IAgentListListener al)
   {
      if(!listeners.contains(al))
      {
         listeners.add(al);
         for(int i=0; i<size(); i++)
            al.agentBorn((AMSAgentDescription) get(i));
      }
   }

   /** 
    * @param ad
    */
   protected void fireAgentDied(AMSAgentDescription ad)
   {
      if(ad!=null) 
      {
         Enumeration e = listeners.elements();
         while (e.hasMoreElements())
         {
            ((IAgentListListener) e.nextElement()).agentDied(ad);
         }
      }
   }

   /** 
    * @param ad
    */
   protected void fireNewAgentEvent(AMSAgentDescription ad)
   {
      Enumeration e = listeners.elements();
      while (e.hasMoreElements())
      {
         ((IAgentListListener)e.nextElement()).agentBorn(ad);
      }
   }
   
   /** 
    * @param ad
    */
   protected void fireAgentChanged(AMSAgentDescription ad)
   {
	  //System.out.println("The agent state changed: "+ad);
      Enumeration e = listeners.elements();
      while (e.hasMoreElements())
      {
         ((IAgentListListener)e.nextElement()).agentChanged(ad);
      }
   }

}