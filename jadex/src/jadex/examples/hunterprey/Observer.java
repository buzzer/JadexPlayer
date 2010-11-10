package jadex.examples.hunterprey;

import jadex.adapter.fipa.AgentIdentifier;


/**
 *  Editable Java class for concept Observer of hunterprey ontology.
 */
public class Observer	extends ObserverData
{
	//-------- constructors --------

	/**
	 *  Create a new Observer.
	 */
	public Observer()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new Observer.
	 */
	public Observer(String name, AgentIdentifier aid, Location location)
	{
		// Constructor using required slots (change if desired).
		setName(name);
		setAID(aid);
		setLocation(location);
	}

	//-------- custom code --------
}

