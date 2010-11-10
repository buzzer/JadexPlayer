package jadex.examples.cleanerworld.multi;

import java.util.List;


/**
 *  Editable Java class for concept Vision of cleaner-generated ontology.
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

	/**
	 *  Create a new vision.
	 */
	public Vision(List wastes, List wastebins, List stations, List cleaners, boolean daytime)
	{
		this.wastes = wastes;
		this.wastebins = wastebins;
		this.stations = stations;
		this.cleaners = cleaners;
		this.daytime = daytime;
	}

	//-------- custom code --------
}

