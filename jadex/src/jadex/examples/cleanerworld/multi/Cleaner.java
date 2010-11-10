package jadex.examples.cleanerworld.multi;


/**
 *  Editable Java class for concept Cleaner of cleaner-generated ontology.
 */
public class Cleaner	extends CleanerData
{
	//-------- constructors --------

	/**
	 *  Create a new Cleaner.
	 */
	public Cleaner()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new Cleaner.
	 */
	public Cleaner(Location location, String name, Waste carriedwaste, double vision, double chargestate)
	{
		setLocation(location);
		setId(name);
		setName(name);
		setCarriedWaste(carriedwaste);
		setVisionRange(vision);
		setChargestate(chargestate);
	}

	//-------- custom code --------

	/**
	 *  Clone the object.
	 */
	public Object	clone()
	{
		Cleaner	clone	= (Cleaner)super.clone();
		if(getCarriedWaste()!=null)
			clone.setCarriedWaste((Waste)getCarriedWaste().clone());
		return clone;
	}
}

