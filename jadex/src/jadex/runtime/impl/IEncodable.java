package jadex.runtime.impl;

import java.util.Map;


/**
 *  Interface for encodable objects.
 *  Encodable objects have to provide their relevant properties as map.
 */
public interface IEncodable
{
	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation();
}

