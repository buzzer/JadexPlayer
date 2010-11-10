package jadex.benchmarks;

import java.util.Map;

import jadex.adapter.fipa.AgentIdentifier;
import jadex.runtime.*;
import jadex.util.collection.SCollection;

/**
 *  Start another peer agent.
 */
public class StartPeerPlan extends Plan
{
	/**
	 * The body method is called on the
	 * instatiated plan instance from the scheduler.
	 */
	public void body()
	{
		long delay = ((Integer)getBeliefbase().getBelief("delay").getFact()).intValue();
		if(delay>0)
			waitFor(delay);

		int num = ((Integer)getBeliefbase().getBelief("num").getFact()).intValue();
		int max = ((Integer)getBeliefbase().getBelief("max").getFact()).intValue();
		Long starttime = (Long)getBeliefbase().getBelief("starttime").getFact();
		Long startmem = (Long)getBeliefbase().getBelief("startmem").getFact();

		// Create new peer.
		if(num<max)
		{
			num++;
			IGoal sp = createGoal("ams_create_agent");
			sp.getParameter("type").setValue("jadex.benchmarks.AgentCreation");
			// todo: Hack! Assumes there is no capability
			sp.getParameter("configuration").setValue(getScope().getConfigurationName());
			sp.getParameter("name").setValue(createPeerName(num));
			Map args = SCollection.createHashMap();
			args.put("max", new Integer(max));
			args.put("num", new Integer(num));
			args.put("starttime", starttime);
			args.put("startmem", startmem);
			sp.getParameter("arguments").setValue(args);
			dispatchSubgoalAndWait(sp);
			System.out.println("Successfully created peer: "+sp.getParameter("agentidentifier").getValue());
		}
		
		// Print results.
		else
		{
			long used = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			long omem = (used-startmem.longValue())/1024;
			long upera = (used-startmem.longValue())/max/1024;
			System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");

			long end = System.currentTimeMillis();
			System.out.println("Last peer created. "+max+" agents started.");
			double dur = ((double)end-starttime.longValue())/1000.0;
			double pera = dur/max;
			System.out.println("Needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");

			// Delete prior agents.
			long	killstarttime	= System.currentTimeMillis();
			while((--num)>0)
			{
				String	name	= createPeerName(num);
				IGoal sp = createGoal("ams_destroy_agent");
				sp.getParameter("agentidentifier").setValue(new AgentIdentifier(name, true));
				dispatchSubgoalAndWait(sp);
				System.out.println("Successfully destroyed peer: "+name);
			}
			long killend = System.currentTimeMillis();
			System.out.println("Last peer destroyed. "+(max-1)+" agents killed.");
			double killdur = ((double)killend-killstarttime)/1000.0;
			double killpera = killdur/(max-1);
			
			System.out.println("\nCumulated results:");
			System.out.println("Creation needed: "+dur+" secs. Per agent: "+pera+" sec. Corresponds to "+(1/pera)+" agents per sec.");
			System.out.println("Killing needed:  "+killdur+" secs. Per agent: "+killpera+" sec. Corresponds to "+(1/killpera)+" agents per sec.");
			System.out.println("Overall memory usage: "+omem+"kB. Per agent: "+upera+" kB.");

			killAgent();
		}
	}

	/**
	 *  Create a name for a peer with a given number.
	 */
	protected String createPeerName(int num)
	{
		String	name	= getScope().getAgentName();
		int	index	= name.indexOf("Peer_#");
		if(index!=-1)
		{
			name	= name.substring(0, index);
		}
		if(num!=1)
		{
			name	+= "Peer_#"+num;
		}
		return name;
	}
}
