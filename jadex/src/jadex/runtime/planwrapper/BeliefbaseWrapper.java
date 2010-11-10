package jadex.runtime.planwrapper;

import java.util.Map;
import java.util.WeakHashMap;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.model.*;


/**
 *  The beliefbase wrapper accessible from within plans.
 */
public class BeliefbaseWrapper extends ElementWrapper implements IBeliefbase
{
	//-------- attributes --------

	/** The original belief base. */
	protected RBeliefbase beliefbase;

	/** The belief/set wrappers (cached). */
	protected transient Map	wrappers;
	
	//-------- constructors --------

	/**
	 *  Create a new beliefbase wrapper.
	 */
	protected BeliefbaseWrapper(RBeliefbase beliefbase)
	{
		super(beliefbase);
		this.beliefbase = beliefbase;
	}

	//-------- methods --------
	
    /**
	 *  Get a belief for a name.
	 *  @param name	The belief name.
	 */
	public IBelief getBelief(String name)
	{
		checkThreadAccess();
		IRBelief	rbelief	= beliefbase.getBelief(name);
		if(wrappers==null)
			wrappers	= new WeakHashMap();
		IBelief	belief	= (IBelief)wrappers.get(rbelief);
		if(belief==null)
		{
			belief	= new BeliefWrapper(rbelief);
			wrappers.put(rbelief, belief);
		}
		return belief;
	}

	/**
	 *  Get a belief set for a name.
	 *  @param name	The belief set name.
	 */
	public IBeliefSet getBeliefSet(String name)
	{
		checkThreadAccess();
		IRBeliefSet	rbeliefset	= beliefbase.getBeliefSet(name);
		if(wrappers==null)
			wrappers	= new WeakHashMap();
		IBeliefSet	beliefset	= (IBeliefSet)wrappers.get(rbeliefset);
		if(beliefset==null)
		{
			beliefset	= new BeliefSetWrapper(rbeliefset);
			wrappers.put(rbeliefset, beliefset);
		}
		return beliefset;
	}

	/**
	 *  Returns <tt>true</tt> if this beliefsetbase contains a belief with the
	 *  specified name.
	 *  @param name the name of a belief.
	 *  @return <code>true</code> if contained, <code>false</code> is not contained, or
	 *          the specified name refer to a belief set.
	 *  @see #containsBeliefSet(java.lang.String)
	 */
	public boolean containsBelief(String name)
	{
		checkThreadAccess();
		return beliefbase.containsBelief(name);
	}

	/**
	 *  Returns <tt>true</tt> if this beliefbase contains a belief set with the
	 *  specified name.
	 *  @param name the name of a belief set.
	 *  @return <code>true</code> if contained, <code>false</code> is not contained, or
	 *          the specified name refer to a belief.
	 *  @see #containsBelief(java.lang.String)
	 */
	public boolean containsBeliefSet(String name)
	{
		checkThreadAccess();
		return beliefbase.containsBeliefSet(name);
	}

	/**
	 *  Returns the names of all beliefs.
	 *  @return The names of all beliefs.
	 */
	public String[] getBeliefNames()
	{
		checkThreadAccess();
		return beliefbase.getBeliefNames();
	}

	/**
	 *  Returns the names of all belief sets.
	 *  @return The names of all belief sets.
	 */
	public String[] getBeliefSetNames()
	{
		checkThreadAccess();
		return beliefbase.getBeliefSetNames();
	}

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param clazz The class.
	 *  @deprecated
	 */
	public void createBelief(String key, Class clazz, int update)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.createBelief(key, clazz, update);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param clazz The class.
	 *  @deprecated
	 */
	public void createBeliefSet(String key, Class clazz, int update)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.createBeliefSet(key, clazz, update);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBelief(String key)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.deleteBelief(key);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBeliefSet(String key)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.deleteBeliefSet(key);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

		/**
	 *  Register a new belief.
	 *  @param mbelief The belief model.
	 */
	public void registerBelief(IMBelief mbelief)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.registerBelief(mbelief);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Register a new beliefset model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void registerBeliefSet(IMBeliefSet mbeliefset)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.registerBeliefSet(mbeliefset);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Register a new belief reference.
	 *  @param mbeliefref The belief reference model.
	 */
	public void registerBeliefReference(IMBeliefReference mbeliefref)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.registerBeliefReference(mbeliefref);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Register a new beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void registerBeliefSetReference(IMBeliefSetReference mbeliefsetref)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.registerBeliefSetReference(mbeliefsetref);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Deregister a belief model.
	 *  @param mbelief The belief model.
	 */
	public void deregisterBelief(IMBelief mbelief)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.deregisterBelief(mbelief);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Deregister a beliefset model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void deregisterBeliefSet(IMBeliefSet mbeliefset)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.deregisterBeliefSet(mbeliefset);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}
	
	/**
	 *  Deregister a belief reference model.
	 *  @param mbeliefref The belief reference model.
	 */
	public void deregisterBeliefReference(IMBeliefReference mbeliefref)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.deregisterBeliefReference(mbeliefref);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Deregister a beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void deregisterBeliefSetReference(IMBeliefSetReference mbeliefsetref)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefbase.deregisterBeliefSetReference(mbeliefsetref);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}
}
