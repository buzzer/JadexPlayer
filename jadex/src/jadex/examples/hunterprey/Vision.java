package jadex.examples.hunterprey;

import java.util.*;


/**
 *  Editable Java class for concept Vision of hunterprey ontology.
 */
public class Vision	extends VisionData
{
	//-------- constructors --------

	/**
	 *  Create a new Vision.
	 */
	public Vision()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	//-------- custom code --------

	/**
	 *  Get the creatures in the vision.
	 */
	public Creature[]	getCreatures()
	{
		ArrayList	ret	= new ArrayList();
		for(Iterator i=objects.iterator(); i.hasNext(); )
		{
			Object	obj	= i.next();
			if(obj instanceof Creature)
				ret.add(obj);
		}
		return (Creature[])ret.toArray(new Creature[ret.size()]);
	}

	/**
	 *  Get an object as represented in this vision.
	 * /
	public WorldObject	findObject(WorldObject template)
	{
		WorldObject	ret	= null;
		for(Iterator i=objects.iterator(); ret==null && i.hasNext(); )
		{
			Object	next	= i.next();
			if(next.equals(template))
				ret	= (WorldObject)next;
		}
		return ret;
	}*/

	/**
	 *  Test if an object is currently seen.
	 *  @return True, if seen.
	 */
	public boolean contains(WorldObject object)
	{
		return objects.contains(object);
	}
}

