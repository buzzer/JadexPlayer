package jadex.examples.cleanerworld.multi.cleanermobile;

import java.util.List;
import jadex.examples.cleanerworld.multi.*;
import jadex.runtime.*;
import jadex.util.SUtil;

/**
 *  Move to a point.
 */
public class MoveToLocationPlan extends MobilePlan
{
	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public MoveToLocationPlan()
	{
		getLogger().info("Created: "+this);
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		// Move a little.
		if(event instanceof IGoalEvent && !((IGoalEvent)event).isInfo()
			|| event instanceof IInternalEvent && event.getType().equals(IInternalEvent.TYPE_TIMEOUT))
		{
			//long	time	= getRootGoal().getExecutionTime();
			Location target = (Location)getParameter("location").getValue();
			Location myloc = (Location)getBeliefbase().getBelief("my_location").getFact();
			if(!myloc.isNear(target))
			{
				//pause(atm);
				// calculate the new position offset.
				//long	newtime	= System.currentTimeMillis();
				double speed = ((Double)getBeliefbase().getBelief("my_speed").getFact()).doubleValue();
				double d = myloc.getDistance(target);
				double r = speed*0.00004*100;//(newtime-time);
				double dx = target.getX()-myloc.getX();
				double dy = target.getY()-myloc.getY();
				//time	= newtime;
	
				// When radius greater than distance, just move a step.
				double rx = r<d? r*dx/d: dx;
				double ry = r<d? r*dy/d: dy;
				getBeliefbase().getBelief("my_location").setFact(new Location(myloc.getX()+rx, myloc.getY()+ry));
	
				// Alter the charge state
				double	charge	= ((Double)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue();
				charge	-= r*0.075;
				getBeliefbase().getBelief("my_chargestate").setFact(new Double(charge));

				
				// Create a representation of myself and get vision.
				Cleaner cl = new Cleaner((Location)getBeliefbase().getBelief("my_location").getFact(),
					getAgentName(),
					(Waste)getBeliefbase().getBelief("carriedwaste").getFact(),
					((Number)getBeliefbase().getBelief("my_vision").getFact()).doubleValue(),
					((Number)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue());
				//IEnvironment env = (IEnvironment)getBeliefbase().getBelief("environment").getFact();
				//Vision vi = env.getVision(cl);
				IGoal dg = createGoal("get_vision_action");
				dispatchSubgoalAndWait(dg);
			}
		}
		
		// Update vision after moving.
		else
		{
			Vision vi = (Vision)((IGoalEvent)event).getGoal().getParameter("vision").getValue();
			updateVision(vi);
			waitFor(100); // wait for 0.01 seconds for next move.
		}
	}

	//-------- helper methods --------

	/**
	 *  Update the vision, when having moved.
	 */
	protected void	updateVision(Vision vi)
	{
		if(vi!=null)
		{
			// Create a representation of myself.
			Cleaner cl = new Cleaner((Location)getBeliefbase().getBelief("my_location").getFact(),
				getAgentName(),
				(Waste)getBeliefbase().getBelief("carriedwaste").getFact(),
				((Number)getBeliefbase().getBelief("my_vision").getFact()).doubleValue(),
				((Number)getBeliefbase().getBelief("my_chargestate").getFact()).doubleValue());

			getBeliefbase().getBelief("daytime").setFact(new Boolean(vi.isDaytime()));
			Waste[] ws = vi.getWastes();
			Wastebin[] wbs = vi.getWastebins();
			Chargingstation[] cs = vi.getStations();
			Cleaner[] cls = vi.getCleaners();

			// When an object is not seen any longer (not
			// in actualvision, but in (near) beliefs), remove it.
			List known = (List)getExpression("query_in_vision_objects").execute();
			for(int i=0; i<known.size(); i++)
			{
				Object object = known.get(i);
				if(object instanceof Waste)
				{
					List tmp = SUtil.arrayToList(ws);
					if(!tmp.contains(object))
						getBeliefbase().getBeliefSet("wastes").removeFact(object);
				}
				else if(object instanceof Wastebin)
				{
					List tmp = SUtil.arrayToList(wbs);
					if(!tmp.contains(object))
						getBeliefbase().getBeliefSet("wastebins").removeFact(object);
				}
				else if(object instanceof Chargingstation)
				{
					List tmp = SUtil.arrayToList(cs);
					if(!tmp.contains(object))
						getBeliefbase().getBeliefSet("chargingstations").removeFact(object);
				}
				else if(object instanceof Cleaner)
				{
					List tmp = SUtil.arrayToList(cls);
					if(!tmp.contains(object))
						getBeliefbase().getBeliefSet("cleaners").removeFact(object);
				}
			}

			// Add new or changed objects to beliefs.
			for(int i=0; i<ws.length; i++)
			{
				if(!getBeliefbase().getBeliefSet("wastes").containsFact(ws[i]))
					getBeliefbase().getBeliefSet("wastes").addFact(ws[i]);
			}
			for(int i=0; i<wbs.length; i++)
			{
				// Remove contained wastes from knowledge.
				// Otherwise the agent might think that the waste is still
				// somewhere (outside its vision) and then it creates lots of
				// cleanup goals, that are instantly achieved because the
				// target condition (waste in wastebin) holds.
				Waste[]	wastes	= wbs[i].getWastes();
				for(int j=0; j<wastes.length; j++)
				{
					if(getBeliefbase().getBeliefSet("wastes").containsFact(wastes[j]))
						getBeliefbase().getBeliefSet("wastes").removeFact(wastes[j]);
				}

				// Now its safe to add wastebin to beliefs.
				IBeliefSet bs = getBeliefbase().getBeliefSet("wastebins");
				if(bs.containsFact(wbs[i]))
					bs.updateFact(wbs[i]);
				else
					bs.addFact(wbs[i]);
				//getBeliefbase().getBeliefSet("wastebins").updateOrAddFact(wbs[i]);
			}
			for(int i=0; i<cs.length; i++)
			{
				if(cs[i].getName()==null)
				{
					throw new RuntimeException("xxx "+cs[i]);
				}
				IBeliefSet bs = getBeliefbase().getBeliefSet("chargingstations");
				if(bs.containsFact(cs[i]))
					bs.updateFact(cs[i]);
				else
					bs.addFact(cs[i]);
				//getBeliefbase().getBeliefSet("chargingstations").updateOrAddFact(cs[i]);
			}
			for(int i=0; i<cls.length; i++)
			{
				if(!cls[i].equals(cl))
				{
					IBeliefSet bs = getBeliefbase().getBeliefSet("cleaners");
					if(bs.containsFact(cls[i]))
						bs.updateFact(cls[i]);
					else
						bs.addFact(cls[i]);
					//getBeliefbase().getBeliefSet("cleaners").updateOrAddFact(cls[i]);
				}
			}

			//getBeliefbase().getBelief("???").setFact("allowed_to_move", new Boolean(true));
		}
		else
		{
			//System.out.println("Error when updating vision! "+event.getGoal());
			System.out.println(getAgentName()+" Error when updating vision! ");
		}
	}
}
