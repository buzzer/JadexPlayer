package jadex.model;

import java.io.*;
import jadex.util.*;

/**
 *  Model loader have to implement this interface used by SXML.
 */
public interface IModelLoader
{
	/**
	 *  Load an agent model.
	 *  @param name The agent name.
	 *  @param imports	The imports (if any).
	 */
	public IMBDIAgent	loadAgentModel(String name, String[] imports)	throws IOException;

	/**
	 *  Load a capability model.
	 *  @param name The capability name.
	 *  @param imports	The imports (if any).
	 *  @param owner The owner.
	 */
	public IMCapability	loadCapabilityModel(String name, String[] imports, IMElement owner)	throws IOException;

	/**
	 *  Load a properties model.
	 *  @param name The properties name.
	 *  @param imports	The imports (if any).
	 *  @param owner The owner.
	 */
	public IMPropertybase	loadPropertyModel(String name, String[] imports, IMElement owner)	throws IOException;

	/**
	 *  Loads any xml Jadex model (e.g. agent, capability, or propertybase).
	 *  Used from Jadexdoc.
	 *  @return The loaded model.
	 */
	public IMElement	loadModel(ResourceInfo rinfo)	throws IOException;

	/**
	 *  Get the model cache.
	 *  @return filename The filename to clear, null for all.
	 */
	public ObjectCache getModelCache();
}
