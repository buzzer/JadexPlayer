package jadex.runtime.externalaccesswrapper;

import jadex.model.IMTypedElementSet;
import jadex.model.ISystemEventTypes;
import jadex.runtime.IBeliefSet;
import jadex.runtime.IBeliefSetListener;
import jadex.runtime.IFilter;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.IRBeliefSet;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;

/**
 *  The belief set wrapper.
 */
public class BeliefSetWrapper	extends ElementWrapper	implements IBeliefSet
{
	//-------- attributes --------

	/** The original belief set. */
	protected IRBeliefSet beliefset;

	//-------- constructors --------

	/**
	 *  Create a new belief set wrapper.
	 */
	//protected BeliefSetWrapper(IRBeliefSet beliefset)	
	public BeliefSetWrapper(IRBeliefSet beliefset) // todo: reduce visibility
	{
		super(beliefset.getScope().getAgent(), beliefset);
		this.beliefset = beliefset;
	}

	//-------- methods --------

	/**
	 *  Add a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void addFact(final Object fact)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.addFact(fact);
			}
		};
	}

	/**
	 *  Remove a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void removeFact(final Object fact)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.removeFact(fact);
			}
		};
	}

	/**
	 *  Add facts to a belief set.
	 */
	public void addFacts(final Object[] facts)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.addFacts(facts);
			}
		};
	}

	/**
	 *  Remove all facts from a belief.
	 */
	public void removeFacts()
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.removeFacts();
			}
		};
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getFact(final Object oldval)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= beliefset.getFact(oldval);
			}
		};
		return exe.object;
	}

	/**
	 *  Test if a fact is contained in a belief.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsFact(final Object fact)
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= beliefset.containsFact(fact);
			}
		};
		return exe.bool;
	}

	/**
	 *  Get the facts of a beliefset.
	 *  @return The facts.
	 */
	public Object[]	getFacts()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				oarray	= beliefset.getFacts();
			}
		};
		return exe.oarray;
	}

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 */
	public void updateFact(final Object newfact)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.updateFact(newfact);
			}
		};
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				integer	= beliefset.size();
			}
		};
		return exe.integer;
	}
	
	/**
	 *  Indicate that the fact of this belief was modified.
	 *  Calling this method causes an internal fact changed
	 *  event that might cause dependent actions.
	 */
	public void modified()
	{
		//checkThreadAccess();
		beliefset.getOriginalElement().throwSystemEvent(SystemEvent.BSFACTS_CHANGED);
	}

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(final Object fact)
	{
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.updateOrAddFact(fact);
			}
		};
	}*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(final Object oldfact, final Object newfact)
	{
		new AgentInvocation()
		{
			public void run()
			{
				beliefset.replaceFact(oldfact, newfact);
			}
		};
	}*/

	/**
	 *  Get the value class.
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		//checkThreadAccess();
		AgentInvocation exe = new AgentInvocation()
		{
			public void run()
			{
				clazz = ((IMTypedElementSet)beliefset.getModelElement()).getClazz();
			}
		};
		return exe.clazz;
	}

	/**
	 *  Is this belief accessible.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				bool	= beliefset.isAccessible();
			}
		};
		return exe.bool;
	}

	//-------- listeners --------
	
	/**
	 *  Add a belief set listener.
	 *  @param listener The belief set listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addBeliefSetListener(final IBeliefSetListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.BSFACT_ADDED,
					ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.BSFACTS_CHANGED, ISystemEventTypes.BSFACT_CHANGED}, unwrap());
				AsynchronousSystemEventListener listener 
					= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, beliefset));
				getAgent().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a belief set listener.
	 *  @param listener The belief set listener.
	 */
	public void removeBeliefSetListener(final IBeliefSetListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
				Object	identifier	= new Tuple(userlistener, beliefset);
				ISystemEventListener[] listeners = getAgent().getSystemEventListeners();
				for(int i=0; i<listeners.length; i++)
				{
					if((listeners[i] instanceof AsynchronousSystemEventListener) 
						&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
					{
						getAgent().removeSystemEventListener(listeners[i]);
						break;
					}
				}
			}
		};
	}
	
}
