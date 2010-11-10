package jadex.runtime.impl;

import java.util.List;
import java.util.Map;
import jadex.model.IMBindingCondition;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.conditions.ConditionDefaultAction;

/**
 *  A condition that is triggered for each binding of a bindanle element.
 */
public class RBindingCondition extends RCondition
{
	//-------- attributes --------

	/** The bindings. */
	protected BindingHelper	bindings;

	/** The current binding (only valid during doTrace). */
	protected Map	binding;
	
	//-------- constructors --------

	/**
	 *  Create a new RBindingCondition.
	 *  @param model	The model element.
	 *  @param owner	The owner.
	 *  @param action	The action.
	 *  @param bindings	The bindings.
	 */
	protected RBindingCondition(IMBindingCondition model, RElement owner
		, AbstractAgendaAction action, BindingHelper bindings)
	{
		super(model, owner, null, action);
		this.bindings	= bindings;
	}

	//-------- methods --------

	// flag used for debugging.
	boolean tracing;

	/**
	 *  Get the condition delegate.
	 */
	protected InterpreterCondition getDelegate(IAgendaAction action)
	{
		return new InterpreterCondition(action==null? new ConditionDefaultAction(this): action, getScope())
		{
			public Object getValue()
			{
				return RBindingCondition.this.getValue();
			}

			public boolean isAffected(SystemEvent event)
			{
				return RBindingCondition.this.isAffected(event);
			}

			public boolean isTriggered()
			{
				return RBindingCondition.this.isTriggered();
			}

			protected void doTrace(SystemEvent cause)
			{
				RBindingCondition.this.doTrace(cause);
			}
		};
	}

	/**
	 *  Overridden to test and trigger for each binding.
	 *  @see jadex.runtime.impl.RCondition#doTrace(SystemEvent cause)
	 */
	protected void doTrace(SystemEvent cause)
	{
		assert !tracing : this;
		tracing	= true;
		if(bindings!=null)
		{
			List	bs	= bindings.calculateBindings(null, null, null);
			for(int i=0; i< bs.size(); i++)
			{
				this.binding	= (Map)bs.get(i);
				// Hack!!! Should remember lastvalue for each binding!?

				if(isTriggered())
				{
					if(getAction()==null)
					{
						throw new RuntimeException("Error, condition has no action: "+this.getName()+getOwner());
					}
					getScope().getAgent().getInterpreter().addAgendaEntry(
						((BindingAction)getAction()).createBindingInstance(binding), cause);
				}
			}
			this.binding	= null;
		}
		else
		{
			// Hack !!! createBindingAction only called because of
			// hack in GoalCreationAction.
			if(isTriggered())
			{
				if(getAction()==null)
				{
					throw new RuntimeException("Error, condition has no action: "+this.getName()+getOwner());
				}
				getScope().getAgent().getInterpreter().addAgendaEntry(((BindingAction)getAction()).createBindingInstance(null), cause);
			}
		}
		tracing	= false;
	}

	/**
	 *  Overriden to use parameters from current binding (if any).
	 *  @see jadex.runtime.impl.RExpression#getValue()
	 */
	public Object getValue()
	{
		return super.getValue(binding);
	}

	/**
	 *  Set the action of the condition.
	 * /
	// Hack!!! Todo: remove.
	protected void setAction(BindingAction action)
	{
		this.action	= action;
	}*/

	//-------- helper classes --------

}
