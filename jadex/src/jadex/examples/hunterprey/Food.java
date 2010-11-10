package jadex.examples.hunterprey;


/**
 *  Editable Java class for concept Food of hunterprey ontology.
 */
public class Food	extends FoodData
{
	//-------- constructors --------

	/**
	 *  Create a new Food.
	 */
	public Food()
	{
		// Empty constructor required for JavaBeans (do not remove).
	}

	/**
	 *  Create a new Food.
	 */
	public Food(Location location)
	{
		// Constructor using required slots (change if desired).
		setLocation(location);
	}

	//-------- custom code --------
}

