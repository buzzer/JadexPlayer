package jadex.runtime;

import jadex.runtime.impl.IEncodable;
import jadex.util.SReflect;
import jadex.util.collection.IndexMap;
import jadex.util.collection.SCollection;

import java.io.Serializable;
import java.util.Map;

/**
 *  A filter checks if an object matches
 *  some given description.
 */
public interface IFilter
{
	//-------- constants --------

	/** A filter that never matches. */
	public static final IFilter NEVER = new ConstantFilter(false);

	/** A filter that always matches. */
	public static final IFilter ALWAYS = new ConstantFilter(true);

	//-------- methods --------

	/**
	 *  Match an object against the filter.
	 *  Exceptions are interpreted as non-match.
	 *  @param object The object.
	 *  @return True, if the filter matches.
	 */
	public boolean filter(Object object)	throws Exception;
}

//-------- helper classes --------

/**
 *  A constant filter always returns the same value,
 *  regardless of the object to match.
 */
class ConstantFilter	implements IFilter, IEncodable, Serializable
{
	//-------- attributes --------

	/** The constant value. */
	protected boolean	val;

	/** The representation. */
	protected IndexMap representation;

	//-------- constructors --------

	/** Create a constant filter. */
	public ConstantFilter(boolean val)
	{
		this.val	= val;
		this.representation = SCollection.createIndexMap();
		this.representation.add("isencodeablepresentation", "true"); // to distinguish this map from normal maps.
		this.representation.add("class", SReflect.getInnerClassName(getClass()));
		this.representation.add("value", new Boolean(val));
	}

	//-------- methods --------

	/** IFilter interface. */
	public boolean filter(Object o)
	{
		return val;
	}

	/** The IEncodable interface. */
	public Map getEncodableRepresentation()
	{
		return representation.getAsMap();
	}
}

