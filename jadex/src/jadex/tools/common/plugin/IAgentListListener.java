package jadex.tools.common.plugin;

import jadex.adapter.fipa.AMSAgentDescription;

/**
 *  Interface for plugins to be informed about agent list changes.
 */
public interface IAgentListListener
{

   /** 
    * @param ad
    */
   void agentDied(AMSAgentDescription ad);

   /** 
    * @param ad
    */
   void agentBorn(AMSAgentDescription ad);
   
   /** 
    * @param ad
    */
   void agentChanged(AMSAgentDescription ad);

}
