package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
import jadex.util.Tuple;
import jadex.model.*;

/**
 *  The belief set wrapper.
 */
public class BeliefSetWrapper extends ElementWrapper implements IBeliefSet
{
	//-------- attributes --------

	/** The original belief set. */
	protected IRBeliefSet beliefset;

	//-------- constructors --------

	/**
	 *  Create a new belief set wrapper.
	 */
	protected BeliefSetWrapper(IRBeliefSet beliefset)
	{
		super(beliefset);
		this.beliefset = beliefset;
	}

	//-------- methods --------

	/**
	 *  Add a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void addFact(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
     		 beliefset.addFact(fact);
    	}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Remove a fact to a belief.
	 *  @param fact The new fact.
	 */
	public void removeFact(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
      		beliefset.removeFact(fact);
    	}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Add facts to a belief set.
	 */
	public void addFacts(final Object[] facts)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
     		beliefset.addFacts(facts);
    	}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Remove all facts from a belief.
	 */
	public void removeFacts()
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
     		beliefset.removeFacts();
    	}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getFact(Object oldval)
	{
		checkThreadAccess();
    	Object value=beliefset.getFact(oldval);

		return value;
	}

	/**
	 *  Test if a fact is contained in a belief.
	 *  @param fact The fact to test.
	 *  @return True, if fact is contained.
	 */
	public boolean containsFact(Object fact)
	{
		checkThreadAccess();
		return beliefset.containsFact(fact);
	}

	/**
	 *  Get the facts of a beliefset.
	 *  @return The facts.
	 */
	public Object[]	getFacts()
	{
		checkThreadAccess();
		Object[] facts=beliefset.getFacts();
		return facts;
	}

	/**
	 *  Update a fact to a new fact. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newfact The new fact.
	 */
	public void updateFact(Object newfact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
      		beliefset.updateFact(newfact);
    	}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		checkThreadAccess();
		return beliefset.size();
	}

	/**
	 *  Get the value class.
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		checkThreadAccess();
		return beliefset.getClazz();
	}
	
	/**
	 *  Indicate that the fact of this belief was modified.
	 *  Calling this method causes an internal fact changed
	 *  event that might cause dependent actions.
	 */
	public void modified()
	{
		checkThreadAccess();
		beliefset.getOriginalElement().throwSystemEvent(SystemEvent.BSFACTS_CHANGED);
	}

	/**
	 *  Update or add a fact. When the fact is already
	 *  contained it will be updated to the new fact.
	 *  Otherwise the value will be added.
	 *  @param fact The new or changed fact.
	 * /
	public void updateOrAddFact(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefset.updateOrAddFact(fact);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}*/

	/**
	 *  Replace a fact with another one.
	 *  @param oldfact The old fact.
	 *  @param newfact The new fact.
	 * /
	public void replaceFact(Object oldfact, Object newfact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try{beliefset.replaceFact(oldfact, newfact);}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}*/

	/**
	 *  Is this belief accessible.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible()
	{
		checkThreadAccess();
		return beliefset.isAccessible();
	}
	
	//-------- listeners --------
	
	
	/**
	 *  Add a belief set listener.
	 *  @param listener The belief set listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addBeliefSetListener(IBeliefSetListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.BSFACT_ADDED,
			ISystemEventTypes.BSFACT_REMOVED, ISystemEventTypes.BSFACTS_CHANGED});
		AsynchronousSystemEventListener listener 
			= new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, beliefset));
		getAgent().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a belief set listener.
	 *  @param listener The belief set listener.
	 */
	public void removeBeliefSetListener(IBeliefSetListener userlistener)
	{
		checkThreadAccess();
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
}
