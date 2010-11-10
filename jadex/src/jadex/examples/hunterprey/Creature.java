package jadex.examples.hunterprey;

import java.util.*;

/**
 *  Editable Java class for concept Creature of hunterprey ontology.
 */
public abstract class Creature	extends CreatureData
{
    //-------- constants --------
    
    /** All possible directions. */
    public static final String[]	alldirs	= new String[]
    {
		RequestMove.DIRECTION_UP,
		RequestMove.DIRECTION_RIGHT,
		RequestMove.DIRECTION_DOWN,
		RequestMove.DIRECTION_LEFT
    };
    
	//-------- constructors --------

	/**
	 *  Create a new Creature.
	 */
	public Creature()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	//-------- custom code --------

	/**
	 *  Test if two creatures are equal.
	 */
	public boolean	equals(Object o)
	{
		return o instanceof Creature && ((Creature)o).getName().equals(getName());
	}

	/**
	 *  Get the hash code of the creature.
	 */
	public int	hashCode()
	{
		return getName().hashCode();
	}

	/**
	 *  Clone the creature.
	 */
	public Object clone()
	{
		Creature ret = null;
		try
		{
			ret = (Creature)getClass().newInstance();
			ret.setName(this.getName());
			ret.setAge(this.getAge());
			ret.setPoints(this.getPoints());
			ret.setAID(this.getAID());
			ret.setLocation(this.getLocation());
			ret.setLeaseticks(this.getLeaseticks());
		}
		catch(InstantiationException e){e.printStackTrace();}
		catch(IllegalAccessException e){e.printStackTrace();}
		return ret;
	}

	//-------- map helper methods --------
	
	/**
	 *  Create a location.
	 *  @param dir The direction.
	 *  @return The new location.
	 */
	public Location createLocation(String dir)
	{
		return createLocation(this.getLocation(), dir);
	}
	
	/**
	 *  Create a location.
	 *  @param loc The location.
	 *  @param dir The direction.
	 *  @return The new location.
	 */
	public Location createLocation(Location loc, String dir)
	{
		int x = loc.getX();
		int y = loc.getY();
		int width = getWorldWidth();
		int height = getWorldHeight();

		if(RequestMove.DIRECTION_UP.equals(dir))
		{
			y = (height+y-1)%height;
		}
		else if(RequestMove.DIRECTION_DOWN.equals(dir))
		{
			y = (y+1)%height;
		}
		else if(RequestMove.DIRECTION_LEFT.equals(dir))
		{
			x = (width+x-1)%width;
		}
		else if(RequestMove.DIRECTION_RIGHT.equals(dir))
		{
			x = (x+1)%width;
		}

		return new Location(x, y);
	}

	/**
	 *  Get the distance between me and an object.
	 *  @return The number of moves required to move between the objects
	 */
	public int	getDistance(WorldObject a)
	{
		return getDistance(getLocation(), a.getLocation());
	}

	/**
	 *  Get the distance between two objects
	 *  @return The number of moves required to move between the objects
	 */
	public int	getDistance(WorldObject a, WorldObject b)
	{
		return getDistance(a.getLocation(), b.getLocation());
	}

	/**
	 *  Get the distance between two locations.
	 *  @return The number of moves required to move between the locations.
	 */
	public int	getDistance(Location a, Location b)
	{
		int	dx	= Math.abs(a.getX()-b.getX());
		int	dy	= Math.abs(a.getY()-b.getY());
		if(dx>getWorldWidth()/2) dx = getWorldWidth()-dx;
		if(dy>getWorldHeight()/2) dy = getWorldHeight()-dy;
		return dx + dy;	// Assume no diagonal movement.
	}

	/**
	 *  Test if two locations are near
	 *  Range is in all direction (including diagonals).
	 */
	public boolean	isNear(Location a, Location b, int range)
	{
		int	dx	= Math.abs(a.getX()-b.getX());
		int	dy	= Math.abs(a.getY()-b.getY());
		if(dx>getWorldWidth()/2) dx = getWorldWidth()-dx;
		if(dy>getWorldHeight()/2) dy = getWorldHeight()-dy;
		return dx<=range && dy<=range;
	}

	/**
	 *  Test if a location is in my vision range.
	 */
	public boolean	isInVisionRange(Location a)
	{
		int	dx	= Math.abs(a.getX()-getLocation().getX());
		int	dy	= Math.abs(a.getY()-getLocation().getY());
		if(dx>getWorldWidth()/2) dx = getWorldWidth()-dx;
		if(dy>getWorldHeight()/2) dy = getWorldHeight()-dy;
		return dx<=getVisionRange() && dy<=getVisionRange();
	}

	/**
	 *  Get the directions between me and an object.
	 *  @return The possible directions to move nearer to the 2nd object.
	 */
	public String[]	getDirections(WorldObject a)
	{
		return getDirections(getLocation(), a.getLocation());
	}

	/**
	 *  Get the directions between two objects.
	 *  @return The possible directions to move nearer to the 2nd object.
	 */
	public String[]	getDirections(WorldObject a, WorldObject b)
	{
		return getDirections(a.getLocation(), b.getLocation());
	}

	/**
	 *  Get the directions between two locations.
	 *  @return The possible directions to move nearer to the 2nd location.
	 */
	public String[]	getDirections(Location a, Location b)
	{
		int	distance	= getDistance(a,b);
		ArrayList	directions	= new ArrayList();
		directions.add(RequestMove.DIRECTION_UP);
		directions.add(RequestMove.DIRECTION_DOWN);
		directions.add(RequestMove.DIRECTION_LEFT);
		directions.add(RequestMove.DIRECTION_RIGHT);
		for(Iterator i=directions.iterator(); i.hasNext(); )
		{
			// Remove, if direction is not towards second location.
			if(getDistance(createLocation(a, (String)i.next()), b) >= distance)
			{
				i.remove();
			}
		}
		return (String[])directions.toArray(new String[directions.size()]);
	}

	/**
	 *  Sort objects by distance.
	 */
	public void	sortByDistance(WorldObject[] objects)
	{
		sortByDistance(objects, getLocation());
	}
	/**
	 *  Sort objects by distance.
	 */
	public void	sortByDistance(WorldObject[] objects, final Location loc)
	{
		Arrays.sort(objects, new Comparator()
		{
			public int	compare(Object o1, Object o2)
			{
				return getDistance(loc, ((WorldObject)o1).getLocation())
					- getDistance(loc, ((WorldObject)o2).getLocation());
			}
		});
	}
	
	/**
	 *  Get a world object at a specified location.
	 *  @param loc The location.
	 *  @return The object at the location.
	 */
	public WorldObject getObject(Location loc, WorldObject[] objects)
	{
	    WorldObject ret = null;
	    for(int i=0; i<objects.length; i++)
	    {
	        if(objects[i].getLocation().equals(loc))
	            ret = objects[i];
	    }
	    return ret;
	}
	
	/**
	 *  Get all possible directions to move.
	 *  @param objects The objects near.
	 *  @return The objects one can move to.
	 */
	public String[] getPossibleDirections(WorldObject[] objects)
	{
	    List posdirs = new ArrayList();
	    for(int i=0; i<alldirs.length; i++)
	    {
	        if(!(getObject(createLocation(Creature.alldirs[i]), objects) instanceof Obstacle))
	            posdirs.add(alldirs[i]);
	    }
	    return (String[])posdirs.toArray(new String[posdirs.size()]);
	}

}

