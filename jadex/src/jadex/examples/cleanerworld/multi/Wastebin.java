package jadex.examples.cleanerworld.multi;

import java.util.ArrayList;


/**
 *  Editable Java class for concept Wastebin of cleaner-generated ontology.
 */
public class Wastebin	extends WastebinData
{
	/** The instance counter. */
	protected static int instancecnt = 0;

	//-------- constructors --------

	/**
	 *  Create a new Wastebin.
	 */
	public Wastebin()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new wastebin.
	 */
	public Wastebin(Location location, int capacity)
	{
		this("Wastebin #"+instancecnt++, location, capacity);
	}

	/**
	 *  Create a new Wastebin.
	 */
	public Wastebin(String name, Location location, int capacity)
	{
		setId(name);
		setName(name);
		setLocation(location);
		setCapacity(capacity);
	}

	//-------- custom code --------

	/**
	 *  Test is the wastebin is full.
	 *  @return True, when wastebin is full.
	 */
	public boolean isFull()
	{
		return wastes.size()>=capacity;
	}

	/**
	 *  Empty the waste bin.
	 */
	public void empty()
	{
		wastes.clear();
	}

	/**
	 *  Fill the waste bin.
	 */
	public void fill()
	{
		// Fill with imaginary waste ;-)
		while(!isFull())
			wastes.add(new Waste(new Location(-1, -1)));
	}


	/**
	 *  Test is the waste is in the waste bin.
	 *  @return True, when wastebin contains the waste.
	 */
	public boolean contains(Waste waste)
	{
		return wastes.contains(waste);
	}

	/**
	 *  Clone the object.
	 */
	public Object	clone()
	{
		Wastebin	clone	= (Wastebin)super.clone();
		clone.wastes	= new ArrayList();
		for(int i=0; i<wastes.size(); i++)
			clone.wastes.add(((Waste)wastes.get(i)).clone());
		return clone;
	}
}
