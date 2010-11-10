package jadex.runtime.externalaccesswrapper;

import jadex.model.IMBelief;
import jadex.model.IMBeliefReference;
import jadex.model.IMBeliefSet;
import jadex.model.IMBeliefSetReference;
import jadex.runtime.IBelief;
import jadex.runtime.IBeliefSet;
import jadex.runtime.IBeliefbase;
import jadex.runtime.impl.IRBelief;
import jadex.runtime.impl.IRBeliefSet;
import jadex.runtime.impl.RBeliefbase;

import java.util.Map;
import java.util.WeakHashMap;


/**
 *  The beliefbase wrapper accessible from other threads (e.g. gui).
 */
public class BeliefbaseWrapper	extends ElementWrapper	implements IBeliefbase
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
		super(beliefbase.getScope().getAgent(), beliefbase);
		this.beliefbase = beliefbase;
	}

	//-------- methods --------

    /**
	 *  Get a belief for a name.
	 *  @param name	The belief name.
	 */
	public IBelief getBelief(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= beliefbase.getBelief(name);
			}
		};
		if(wrappers==null)
			wrappers	= new WeakHashMap();
		IBelief	belief	= (IBelief)wrappers.get(exe.object);
		if(belief==null)
		{
			IRBelief	rbelief	= (IRBelief)exe.object;
			belief	= new BeliefWrapper(rbelief);
			wrappers.put(rbelief, belief);
		}
		return belief;
	}

	/**
	 *  Get a belief set for a name.
	 *  @param name	The belief set name.
	 */
	public IBeliefSet getBeliefSet(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= beliefbase.getBeliefSet(name);
			}
		};
		if(wrappers==null)
			wrappers	= new WeakHashMap();
		IBeliefSet	beliefset	= (IBeliefSet)wrappers.get(exe.object);
		if(beliefset==null)
		{
			IRBeliefSet	rbeliefset	= (IRBeliefSet)exe.object;
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
	 *  @see #containsBeliefSet(String)
	 */
	public boolean containsBelief(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= beliefbase.containsBelief(name);
			}
		};
		return exe.bool;
	}

	/**
	 *  Returns <tt>true</tt> if this beliefbase contains a belief set with the
	 *  specified name.
	 *  @param name the name of a belief set.
	 *  @return <code>true</code> if contained, <code>false</code> is not contained, or
	 *          the specified name refer to a belief.
	 *  @see #containsBelief(String)
	 */
	public boolean containsBeliefSet(final String name)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= beliefbase.containsBeliefSet(name);
			}
		};
		return exe.bool;
	}

	/**
	 *  Returns the names of all beliefs.
	 *  @return The names of all beliefs.
	 */
	public String[] getBeliefNames()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				sarray	= beliefbase.getBeliefNames();
			}
		};
		return exe.sarray;
	}

	/**
	 *  Returns the names of all belief sets.
	 *  @return The names of all belief sets.
	 */
	public String[] getBeliefSetNames()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				sarray	= beliefbase.getBeliefSetNames();
			}
		};
		return exe.sarray;
	}

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param bclazz The belief class.
	 *  @deprecated
	 */
	public void createBelief(final String key, final Class bclazz, final int update)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.createBelief(key, bclazz, update);
			}
		};
	}

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param bclazz The belief class.
	 *  @deprecated
	 */
	public void createBeliefSet(final String key, final Class bclazz, final int update)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.createBeliefSet(key, bclazz, update);
			}
		};
	}

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBelief(final String key)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.deleteBelief(key);
			}
		};
	}

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBeliefSet(final String key)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.deleteBeliefSet(key);
			}
		};
	}

	/**
	 *  Register a new belief.
	 *  @param mbelief The belief model.
	 */
	public void registerBelief(final IMBelief mbelief)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.registerBelief(mbelief);
			}
		};
	}

	/**
	 *  Register a new goal model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void registerBeliefSet(final IMBeliefSet mbeliefset)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.registerBeliefSet(mbeliefset);
			}
		};
	}

	/**
	 *  Register a new belief reference.
	 *  @param mbeliefref The belief reference model.
	 */
	public void registerBeliefReference(final IMBeliefReference mbeliefref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.registerBeliefReference(mbeliefref);
			}
		};
	}

	/**
	 *  Register a new beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void registerBeliefSetReference(final IMBeliefSetReference mbeliefsetref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.registerBeliefSetReference(mbeliefsetref);
			}
		};
	}

	/**
	 *  Deregister a goal model.
	 *  @param mbelief The belief model.
	 */
	public void deregisterBelief(final IMBelief mbelief)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.deregisterBelief(mbelief);
			}
		};
	}

	/**
	 *  Deregister a goal model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void deregisterBeliefSet(final IMBeliefSet mbeliefset)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.deregisterBeliefSet(mbeliefset);
			}
		};
	}
	
	/**
	 *  Deregister a belief reference model.
	 *  @param mbeliefref The belief reference model.
	 */
	public void deregisterBeliefReference(final IMBeliefReference mbeliefref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.deregisterBeliefReference(mbeliefref);
			}
		};
	}

	/**
	 *  Deregister a beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void deregisterBeliefSetReference(final IMBeliefSetReference mbeliefsetref)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefbase.deregisterBeliefSetReference(mbeliefsetref);
			}
		};
	}

}
