package jadex.examples.hunterprey;

import jadex.adapter.fipa.AgentIdentifier;


/**
 *  Editable Java class for concept Prey of hunterprey ontology.
 */
public class Prey	extends PreyData
{
	//-------- constructors --------

	/**
	 *  Create a new Prey.
	 */
	public Prey()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new Prey.
	 */
	public Prey(String name, AgentIdentifier aid, Location location)
	{
		// Constructor using required slots (change if desired).
		setName(name);
		setAID(aid);
		setLocation(location);
	}

	//-------- custom code --------

	/**
	 *  Get a string representation of this Creature.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	buf	= new StringBuffer("Prey(");
		buf.append("location=");
		buf.append(getLocation());
		buf.append(", name=");
		buf.append(getName());
		buf.append(", points=");
		buf.append(getPoints());
		buf.append(", age=");
		buf.append(getAge());
		buf.append(", leaseticks=");
		buf.append(getLeaseticks());
		buf.append(")");
		return buf.toString();
	}
}

