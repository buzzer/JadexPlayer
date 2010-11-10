package jadex.examples.hunterprey.creature.hunters.elephunter;

import java.util.*;
import jadex.examples.hunterprey.*;
import jadex.runtime.*;
import jadex.util.SUtil;


/**
 *  Plan to move around in the environment and try to catch prey.
 */
public class ELEPHunterPlan	extends Plan
{
	//-------- attributes --------

	/** Random number generator. */
	protected Random	rand;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public ELEPHunterPlan()
	{
		this.rand	= new Random(hashCode());
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		waitForBeliefChange("vision");
	    //waitForCondition(getCondition("has_vision"));
		//int	dir	= 0;

		while(true)
		{
			// Look whats around.
			Creature me = ((Creature)getBeliefbase().getBelief("my_self").getFact());
			Vision vision = ((Vision)getBeliefbase().getBelief("vision").getFact());
	
			WorldObject[]	objects	= vision.getObjects();
			me.sortByDistance(objects);
	    	String[] posdirs = me.getPossibleDirections(objects);
	    	Map	values	= new HashMap();
			for(int i=0; i<posdirs.length; i++)
			{
				values.put(posdirs[i], new Integer(0));
			}
			WorldObject	captured	= null;

			// Evaluate possible directions.
			for(int i=0; i<objects.length; i++)
			{
				if(objects[i] instanceof Hunter || objects[i] instanceof Prey)
				{
					// Desired distance to other hunters==3, to prey==0. 
					int	good_distance	= objects[i] instanceof Hunter ? 3 : 0;
					int	dist	= me.getDistance(objects[i]);
					if(dist==0 && objects[i] instanceof Prey)
					{
						captured	= objects[i];
						break;
					}
					
					
					// Object is too far away, increase value for directions.
					if(dist>good_distance)
					{
						String[] dirs	= me.getDirections(objects[i]);
						for(int j=0; j<dirs.length; j++)
						{
							if(values.containsKey(dirs[j]))
							{
								int value	= ((Integer)values.get(dirs[j])).intValue();
								values.put(dirs[j], new Integer(value+1));
							}
						}
					}

					// Object is too close, decrease value for directions.
					else if(dist<good_distance)
					{
						String[] dirs	= me.getDirections(objects[i]);
						for(int j=0; j<dirs.length; j++)
						{
							if(values.containsKey(dirs[j]))
							{
								int value	= ((Integer)values.get(dirs[j])).intValue();
								values.put(dirs[j], new Integer(value-1));
							}
						}
					}
					
				}
			}

			// Eat captured prey, if any.
			if(captured!=null)
			{
				eat(captured);
			}
			
			// Otherwise calculate best move.
			else
			{
				List	best_moves	= new ArrayList();
				int	best_value	= Integer.MIN_VALUE;
				for(Iterator i=values.keySet().iterator(); i.hasNext(); )
				{
					String	move	= (String)i.next();
					int	value	= ((Integer)values.get(move)).intValue();
					if(value>best_value)
					{
						best_value	= value;
						best_moves.clear();
						best_moves.add(move);
					}
					else if(value==best_value)
					{
						best_moves.add(move);
					}
				}
	
				// Take best move.
				if(best_moves.size()>0)
				{
					// If more than one best move, choose randomly.
					move((String)best_moves.get(rand.nextInt(best_moves.size())));
				}
				
				// No move possible.
				else
				{
					// Just sit an wait.
					waitForBeliefChange("vision");
				}
			}
		}
	}

	/**
	 *  Move towards a direction.
	 */
	protected void move(String direction)
	{
		try
		{
			//System.out.println(getAgentName()+" wants to move: "+direction);
			IGoal move = createGoal("move");
			move.getParameter("direction").setValue(direction);
			dispatchSubgoalAndWait(move);
		}
		catch(GoalFailureException e)
		{
			getLogger().warning("Move goal failed");
		}
	}

	/**
	 *  Eat an object.
	 */
	protected void eat(WorldObject object)
	{
		try
		{
			IGoal eat = createGoal("eat");
			eat.getParameter("object").setValue(object);
			dispatchSubgoalAndWait(eat);
		}
		catch(GoalFailureException e)
		{
			getLogger().warning("Move goal failed");
		}
	}

}


