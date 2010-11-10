package jadex.runtime.impl;

import java.util.Map;
import jadex.util.*;
import jadex.util.collection.*;
import jadex.runtime.*;

/**
 *  A candidate info denotes a plan candidate that
 *  can be scheduled for execution.
 */
public abstract class CandidateInfo implements ICandidateInfo, IEncodable, java.io.Serializable
{
	//-------- attributes --------

	/** The associated event. */
	protected IREvent revent;

	/** The associated plan. */
	protected RPlan	rplan;

	/** The property representation (cached, may not be up to date). */
	protected IndexMap	representation;

	//-------- constructors --------

	/**
	 *  Create candidate info.
	 *  @param revent	The event.
	 */
	public CandidateInfo(IREvent revent, RPlan rplan)
	{
		//assert revent!=null;

		this.revent	= revent;
		this.rplan = rplan;
	}
	
	//-------- interface methods --------

	/**
	 *  Get the plan instance.
	 *  @return	The plan instance.
	 */
	public IPlan getPlan()
	{
		assert getPlanInstance()!=null;

		return new jadex.runtime.planwrapper.PlanWrapper(getPlanInstance());
	}

	/**
	 *  Get the associated event.
	 *  @return	The associated event.
	 */
	public IEvent	getEvent()
	{
		assert getEventInstance()!=null;

		return jadex.runtime.planwrapper.EventbaseWrapper.wrap(getEventInstance());
	}

	//-------- additional methods --------

	/**
	 *  Get the plan instance info.
	 *  @return	The plan instance info.
	 */
	public RPlan	getPlanInstance()
	{
		return this.rplan;
	}

	/**
	 *  Get the event instance.
	 *  @return	The event instance.
	 */
	public IREvent	getEventInstance()
	{
		return this.revent;
	}

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		Map	representation = SCollection.createHashMap();
		representation.put("isencodeablepresentation", "true"); // to distinguish this map from normal maps.
		representation.put("class", SReflect.getInnerClassName(this.getClass()));
		return representation;
	}
}

