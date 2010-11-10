package jadex.runtime.externalaccesswrapper;

import jadex.model.IMTypedElement;
import jadex.model.ISystemEventTypes;
import jadex.runtime.IBelief;
import jadex.runtime.IBeliefListener;
import jadex.runtime.IFilter;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.AsynchronousSystemEventListener;
import jadex.runtime.impl.IRBelief;
import jadex.runtime.impl.SystemEventFilter;
import jadex.util.Tuple;

/**
 *  The beliefbase wrapper accessible from within plans.
 */
public class BeliefWrapper	extends ElementWrapper	implements IBelief
{
	//-------- attributes --------

	/** The original goal base. */
	protected IRBelief belief;

	//-------- constructors --------

	/**
	 *  Create a new belief wrapper.
	 */
	public BeliefWrapper(IRBelief belief) // todo: make protected
	{
		super(belief.getScope().getAgent(), belief);
		this.belief = belief;
	}

	//-------- methods --------

	/**
	 *  Set a fact of a belief.
	 *  @param fact The new fact.
	 */
	public void setFact(final Object fact)
	{
		//checkThreadAccess();
		new AgentInvocation()
		{
			public void run()
			{
				belief.setFact(fact);
			}
		};
	}

	/**
	 *  Get the fact of a belief.
	 *  @return The fact.
	 */
	public Object	getFact()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= belief.getFact();
			}
		};
		return exe.object;
	}
	
	/**
	 *  Indicate that the fact of this belief was modified.
	 *  Calling this method causes an internal fact changed
	 *  event that might cause dependent actions.
	 */
	public void modified()
	{
		belief.getOriginalElement().throwSystemEvent(SystemEvent.FACT_CHANGED);
	}

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
				clazz = ((IMTypedElement)belief.getModelElement()).getClazz();
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
				bool	= belief.isAccessible();
			}
		};
		return exe.bool;
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a belief listener.
	 *  @param listener The belief listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addBeliefListener(final IBeliefListener userlistener, final boolean async)
	{
		new AgentInvocation()
		{
			public void run()
			{
				IFilter filter = new SystemEventFilter(new String[]{ISystemEventTypes.FACT_CHANGED}, unwrap());
				AsynchronousSystemEventListener listener = new AsynchronousSystemEventListener(userlistener, new Tuple(userlistener, belief));
				getAgent().addSystemEventListener(listener, filter, true, async);
			}
		};
	}
	
	/**
	 *  Remove a belief  listener.
	 *  @param listener The belief  listener.
	 */
	public void removeBeliefListener(final IBeliefListener userlistener)
	{
		new AgentInvocation()
		{
			public void run()
			{
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
		};
	}
}