package jadex.examples.hunterprey.environment;

import jadex.adapter.fipa.Done;
import jadex.examples.hunterprey.Environment;
import jadex.examples.hunterprey.RequestWorldSize;
import jadex.runtime.Plan;

/**
 *  The dispatch world size plan sends back the size of the world.
 */
public class DispatchWorldSizePlan extends Plan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public DispatchWorldSizePlan()
	{
		getLogger().info("Created: "+this);
	}

	//------ methods -------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		/*IMessageEvent req = (IMessageEvent)getInitialEvent();
		RequestWorldSize rws = (RequestWorldSize)req.getContent();
		Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
		rws.setWidth(env.getWidth());
		rws.setHeight(env.getHeight());
		Done done = new Done();
		done.setAction(rws);
		sendMessage(req.createReply("inform_done", done));*/

		RequestWorldSize rws = (RequestWorldSize)getParameter("action").getValue();
		Environment env = (Environment)getBeliefbase().getBelief("environment").getFact();
		rws.setWidth(env.getWidth());
		rws.setHeight(env.getHeight());
		Done done = new Done();
		done.setAction(rws);
		getParameter("result").setValue(done);
	}
}
