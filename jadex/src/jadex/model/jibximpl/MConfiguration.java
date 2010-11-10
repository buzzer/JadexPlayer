package jadex.model.jibximpl;

import jadex.model.*;

import java.util.List;

/**
 *  The configuration of a capability.
 */
public class MConfiguration extends MElement implements IMConfiguration
{
	//-------- xml attributes --------

	/** The capability base. */
	protected MConfigCapabilitybase capabilitybase;

	/** The beliefbase. */
	protected MConfigBeliefbase beliefbase;

	/** The goalbase. */
	protected MConfigGoalbase goalbase;

	/** The planbase. */
	protected MConfigPlanbase planbase;

	/** The eventbase. */
	protected MConfigEventbase eventbase;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(capabilitybase!=null)
			ret.add(capabilitybase);
		if(beliefbase!=null)
			ret.add(beliefbase);
		if(goalbase!=null)
			ret.add(goalbase);
		if(planbase!=null)
			ret.add(planbase);
		if(eventbase!=null)
			ret.add(eventbase);
		return ret;
	}

	//-------- bases --------

	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IMConfigBeliefbase getBeliefbase()
	{
		if(beliefbase==null)
		{
			beliefbase = new MConfigBeliefbase();
			beliefbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				beliefbase.init();
		}
		return beliefbase;
	}

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IMConfigGoalbase getGoalbase()
	{
		if(goalbase==null)
		{
			goalbase = new MConfigGoalbase();
			goalbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				goalbase.init();
		}
		return goalbase;
	}

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IMConfigPlanbase getPlanbase()
	{
		if(planbase==null)
		{
			planbase = new MConfigPlanbase();
			planbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				planbase.init();
		}
		return planbase;
	}

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IMConfigEventbase getEventbase()
	{
		if(eventbase==null)
		{
			eventbase = new MConfigEventbase();
			eventbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				eventbase.init();
		}
		return eventbase;
	}

	/**
	 *  Get the capability base.
	 *  @return The capability base.
	 */
	public IMConfigCapabilitybase getCapabilitybase()
	{
		if(capabilitybase==null)
		{
			capabilitybase = new MConfigCapabilitybase();
			capabilitybase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				capabilitybase.init();
		}
		return capabilitybase;
	}


	//-------- not xml related --------

	/**
	 *  Get the initial capability reference of the given
	 *  capability reference.
	 */
	public IMConfigCapability	getInitialCapability(IMCapabilityReference cap)
	{
		IMConfigCapability	ini	= null;
		IMConfigCapability[] capas = getCapabilitybase().getCapabilityConfigurations();
		for(int i=0; i<capas.length && ini==null; i++)
		{
			if(capas[i].getOriginalElement()==cap)
				ini	= capas[i];
		}
		return ini;
	}

	//-------- helper methods --------

	/**
	 *  Generate and sets a pseudo name for an element.
	 *  Can be overrideen, if not allowed or other naming scheme desired.
	 */
	protected String generateName()
	{
		throw new RuntimeException("Configurations require a name.");
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfiguration clone = (MConfiguration)cl;
		if(capabilitybase!=null)
			clone.capabilitybase = (MConfigCapabilitybase)capabilitybase.clone();
		if(beliefbase!=null)
			clone.beliefbase = (MConfigBeliefbase)beliefbase.clone();
		if(goalbase!=null)
			clone.goalbase = (MConfigGoalbase)goalbase.clone();
		if(planbase!=null)
			clone.planbase = (MConfigPlanbase)planbase.clone();
		if(eventbase!=null)
			clone.eventbase = (MConfigEventbase)eventbase.clone();
	}
}
