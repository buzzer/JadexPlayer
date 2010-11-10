package jadex.examples.cleanerworld.multi;


/**
 *  Editable Java class for concept Chargingstation of cleaner-generated ontology.
 */
public class Chargingstation	extends ChargingstationData
{
	/** The instance counter. */
	protected static int instancecnt = 0;

	//-------- constructors --------

	/**
	 *  Create a new Chargingstation.
	 */
	public Chargingstation()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new charging station.
	 */
	public Chargingstation(Location location)
	{
		this("Chargingstation #"+instancecnt++, location);
	}

	/**
	 *  Create a new Chargingstation.
	 */
	public Chargingstation(String name, Location location)
	{
		setId(name);
		setName(name);
		setLocation(location);
	}

	//-------- custom code --------
}

