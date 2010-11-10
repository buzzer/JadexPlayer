package jadex.runtime.planwrapper;

import jadex.runtime.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;
import jadex.util.Tuple;
import jadex.model.IMTypedElement;
import jadex.model.ISystemEventTypes;

/**
 *  The beliefbase wrapper accessible from within plans.
 */
public class BeliefWrapper extends ElementWrapper implements IBelief
{
	//-------- attributes --------

	/** The original goal base. */
	protected IRBelief belief;

	//-------- constructors --------

	/**
	 *  Create a new belief wrapper.
	 */
	protected BeliefWrapper(IRBelief belief)
	{
		super(belief);
		this.belief	= belief;
	}

	//-------- methods --------

	/**
	 *  Set a fact of a belief.
	 *  @param fact The new fact.
	 */
	public void setFact(Object fact)
	{
		checkThreadAccess();
		getCapability().getAgent().startMonitorConsequences();
		try
		{
      		belief.setFact(fact);
    	}
		finally{getCapability().getAgent().endMonitorConsequences();}
	}

	/**
	 *  Get the fact of a belief.
	 *  @return The fact.
	 */
	public Object	getFact()
	{
		checkThreadAccess();
	    return belief.getFact();
	}
	
	/**
	 *  Indicate that the fact of this belief was modified.
	 *  Calling this method causes an internal fact changed
	 *  event that might cause dependent actions.
	 */
	public void modified()
	{
		checkThreadAccess();
		belief.getOriginalElement().throwSystemEvent(SystemEvent.FACT_CHANGED);
	}

	/**
	 *  Get the value class.
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		checkThreadAccess();
		return belief.getClazz();
	}

	/**
	 *  Is this belief accessible.
	 *  @return False, if the belief cannot be accessed.
	 */
	public boolean isAccessible()
	{
		checkThreadAccess();
		return belief.isAccessible();
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a belief listener.
	 *  @param listener The belief listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addBeliefListener(IBeliefListener userlistener, boolean async)
	{
		checkThreadAccess();
		IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.FACT_CHANGED}, unwrap());
		WaitAbstraction wa = new WaitAbstraction(getCapability());
		AsynchronousSystemEventListener listener = new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, belief));
		getAgent().addSystemEventListener(listener, filter, true, async);
	}
	
	/**
	 *  Remove a belief  listener.
	 *  @param listener The belief  listener.
	 */
	public void removeBeliefListener(IBeliefListener userlistener)
	{
		checkThreadAccess();
		Object	identifier	= new Tuple(userlistener, belief);
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