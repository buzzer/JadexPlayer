package jadex.runtime;

import jadex.util.*;
import jadex.util.collection.SCollection;
import jadex.model.ISystemEventTypes;
import jadex.runtime.planwrapper.*;
import jadex.runtime.externalaccesswrapper.ElementWrapper.AgentInvocation;
import jadex.runtime.impl.*;

import java.util.*;
import java.util.logging.Logger;

/**
 *  The abstract plan is the abstract superclass
 *  for standard plans and mobile plans.
 */
public abstract class AbstractPlan implements java.io.Serializable
{
	//-------- attributes --------

	/** The runtime plan element. */
	private RPlan rplan;

	/** The plan plan-wrapper. */
	private PlanWrapper plan;

	/** The capability wrapper. */
	private CapabilityWrapper capability;

	/** The rootgoal wrapper. */
	private ProcessGoalWrapper rootgoal;

	/** The waitqueue. */
	private WaitqueueWrapper waitqueue;

	/** The exception. */
	private Exception exception;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public AbstractPlan()
	{
		String myadr	= ""+Thread.currentThread()+
			"_"+Thread.currentThread().hashCode();
		this.rplan	= (RPlan)planinit.get(myadr);
		if(rplan==null)
			throw new RuntimeException("Plan could not be inited: "+myadr+" - "+rplan);
		//this.rplan.setPlanInstance(this);
		this.plan = new PlanWrapper(rplan);

		this.capability = new CapabilityWrapper(rplan.getScope());
	}

	//-------- methods --------

	/**
	 *  Let a plan fail.
	 */
	public void fail()
	{
		capability.checkThreadAccess();
		throw new PlanFailureException();
		//rplan.getRootGoal().fail(new PlanFailureException()); // this would lead to invocation of aborted() method ?!
	}

	/**
	 *  Let a plan fail.
	 *  @param cause The cause.
	 */
	public void fail(Throwable cause)
	{
		capability.checkThreadAccess();
		throw new PlanFailureException(null, cause);
		//rplan.getRootGoal().fail(new PlanFailureException()); // this would lead to invocation of aborted() method ?!
	}

	/**
	 *  Let a plan fail.
	 *  @param message The message.
	 *  @param cause The cause.
	 */
	public void fail(String message, Throwable cause)
	{
		capability.checkThreadAccess();
		throw new PlanFailureException(message, cause);
		//rplan.getRootGoal().fail(new PlanFailureException()); // this would lead to invocation of aborted() method ?!
	}

	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public ICapability getScope()
	{
		capability.checkThreadAccess();
		return capability;
	}

	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger()
	{
		capability.checkThreadAccess();
		return rplan.getScope().getLogger();
	}

	/**
	 *  Start an atomic transaction.
	 *  All possible side-effects (i.e. triggered conditions)
	 *  of internal changes (e.g. belief changes)
	 *  will be delayed and evaluated after endAtomic() has been called.
	 *  @see #endAtomic()
	 */
	public void	startAtomic()
	{
		capability.checkThreadAccess();
		rplan.getScope().getAgent().startMonitorConsequences();
		rplan.getScope().getAgent().startAtomic();
	}

	/**
	 *  End an atomic transaction.
	 *  Side-effects (i.e. triggered conditions)
	 *  of all internal changes (e.g. belief changes)
	 *  performed after the last call to startAtomic()
	 *  will now be evaluated and performed.
	 *  @see #startAtomic()
	 */
	public void	endAtomic()
	{
		capability.checkThreadAccess();
		rplan.getScope().getAgent().endAtomic();
		rplan.getScope().getAgent().endMonitorConsequences();
	}

	/**
	 *  Dispatch a new subgoal.
	 *  @param subgoal The new subgoal.
	 *  @return The eventfilter for identifying the result event.
	 *  Note: plan step is interrupted after call.
	 */
	public IFilter dispatchSubgoal(IGoal subgoal)
	{
		capability.checkThreadAccess();

		rplan.getScope().getAgent().startMonitorConsequences();

		try
		{
			IRGoal original = (IRGoal)((GoalWrapper)subgoal).unwrap(); // unwrap!!!
			return rplan.getScope().getGoalbase().dispatchSubgoal(rplan.getRootGoal(), original);
		}
		catch(GoalFailureException gfe)
		{
			gfe.setGoal(subgoal);
			throw gfe;
		}
		finally
		{
			// Interrupts the plan step, if necessary.
			rplan.getScope().getAgent().endMonitorConsequences();
		}
	}

	/**
	 *  Get the name.
	 *  @return The name of the plan.
	 */
	public String getName()
	{
		//rplan.checkThreadAccess(); // make toString working
		return rplan.getName();
	}

	/**
	 *  todo: remove
	 *  Get the plans root goal.
	 *  @return The goal.
	 *  @deprecated
	 */
	public IProcessGoal getRootGoal()
	{
		capability.checkThreadAccess();
		if(rootgoal==null)
			rootgoal = new ProcessGoalWrapper(rplan.getRootGoal());
		return rootgoal;
	}

	/**
	 *  Get the waitqueue.
	 *  @return The waitqueue.
	 */
	public IWaitqueue getWaitqueue()
	{
		if(waitqueue==null)
			waitqueue = new WaitqueueWrapper(rplan.getWaitqueue());
		return waitqueue;
	}

	/**
	 *  Add some code to the agent's agenda,
	 *  that will be executed on the agent's thread.
	 *  This method can safely be called from any thread
	 *  (e.g. AWT event handlers).
	 *  todo: remove
	 * /
	public void	invokeLater(Runnable code)
	{
		rplan.getScope().getAgent().invokeLater(code);
	}*/

	/**
	 *  Add some code to the agent's agenda,
	 *  and wait until it has been executed on the agent's thread.
	 *  This method can safely be called from any thread
	 *  (e.g. AWT event handlers).
	 *  todo: remove
	 * /
	public void	invokeAndWait(Runnable code)
	{
		rplan.getScope().getAgent().invokeAndWait(code);
	}*/


	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(SReflect.getInnerClassName(this.getClass()));
		buf.append("(name=");
		buf.append(getName());
		buf.append(")");
		return buf.toString();
	}

	/**
	 *  Get the agent name.
	 *  @return The agent name.
	 */
	public String getAgentName()
	{
		return capability.getAgentName();
	}
	
	/**
	 * Get the agent identifier.
	 * @return The agent identifier.
	 */
	public BasicAgentIdentifier	getAgentIdentifier()
	{
		return capability.getAgentIdentifier();
	}

	/**
	 *  Check if the corresponding plan was aborted because the
	 *  proprietary goal succeeded during the plan was running.
	 *  @return True, if the goal was aborted on success of the proprietary goal.
	 */
	public boolean isAbortedOnSuccess()
	{
		return getRootGoal().isAbortedOnSuccess();
	}

	/**
	 *  Get the uncatched exception that occurred in the body (if any).
	 *  Method should be called when in failed() method.
	 *  @return The exception.
	 */
	public Exception getException()
	{
		return rplan.getException();
	}

	/**
	 *  Kill this agent.
	 */
	public void killAgent()
	{
		capability.killAgent();
	}

	//-------- capability shortcut methods --------
	
	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IBeliefbase getBeliefbase()
	{
		return capability.getBeliefbase();
	}

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IGoalbase getGoalbase()
	{
		return capability.getGoalbase();
	}

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IPlanbase getPlanbase()
	{
		return capability.getPlanbase();
	}

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IEventbase getEventbase()
	{
		return capability.getEventbase();
	}

	/**
	 * Get the expression base.
	 * @return The expression base.
	 */
	public IExpressionbase getExpressionbase()
	{
		return capability.getExpressionbase();
	}

	/**
	 * Get the property base.
	 * @return The property base.
	 */
	public IPropertybase getPropertybase()
	{
		return capability.getPropertybase();
	}

	//-------- goalbase shortcut methods --------
	
	/**
	 *  Dispatch a new top-level goal.
	 *  @param goal The new goal.
	 *  Note: plan step is interrupted after call.
	 */
	public void dispatchTopLevelGoal(IGoal goal)
	{
		capability.getGoalbase().dispatchTopLevelGoal(goal);
	}

	/**
	 *  Create a goal from a template goal.
	 *  To be processed, the goal has to be dispatched as subgoal
	 *  or adopted as top-level goal.
	 *  @param type	The template goal name as specified in the ADF.
	 *  @return The created goal.
	 */
	public IGoal createGoal(String type)
	{
		return capability.getGoalbase().createGoal(type);
	}

	//-------- eventbase shortcut methods --------
	
	/**
	 *  Send a message after some delay.
	 *  @param me	The message event.
	 *  @return The filter to wait for an answer.
	 */
	public IFilter	sendMessage(IMessageEvent me)
	{
		return capability.getEventbase().sendMessage(me);
	}

	/**
	 *  Dispatch an internal event.
	 *  @param event The event.
	 *  Note: plan step is interrupted after call.
	 */
	public void dispatchInternalEvent(IInternalEvent event)
	{
		capability.getEventbase().dispatchInternalEvent(event);
	}

	/**
	 *  Create a new message event.
	 *  @return The new message event.
	 */
	public IMessageEvent createMessageEvent(String type)
	{
		return capability.getEventbase().createMessageEvent(type);
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 */
	public IInternalEvent createInternalEvent(String type)
	{
		return capability.getEventbase().createInternalEvent(type);
	}

	/**
	 *  Create a new intenal event.
	 *  @return The new intenal event.
	 *  @deprecated Convenience method for easy conversion to new explicit internal events.
	 *  Will be removed in later releases.
	 */
	public IInternalEvent createInternalEvent(String type, Object content)
	{
		return capability.getEventbase().createInternalEvent(type, content);
	}

	//-------- gui methods --------

	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public IExternalAccess getExternalAccess()
	{
		//externalaccesswrapper.checkThreadAccess();
		return capability.getExternalAccess();
	}

	//-------- expressionbase shortcut methods --------
	// Hack!!! Not really shortcuts, because expressions/conditions are remembered for cleanup.
	
	/**
	 *  Get a query created from a predefined expression.
	 *  @param name	The name of an expression defined in the ADF.
	 *  @return The query object.
	 *  @deprecated	Use @link{#getExpression(String)} instead.
	 */
	public IExpression	getQuery(String name)
	{
		return	getExpression(name);
	}

	/**
	 *  Get an instance of a predefined expression.
	 *  @param name	The name of an expression defined in the ADF.
	 *  @return The expression instance.
	 */
	public IExpression	getExpression(String name)
	{
		return capability.getExpressionbase().getExpression(name);
	}

	/**
	 *  Get a condition predefined in the ADF.
	 *  Note that a new condition instance is returned each time this method is called.
	 *  @param name	The name of a condition defined in the ADF.
	 *  @return The condition object.
	 */
	public ICondition	getCondition(String name)
	{
		return capability.getExpressionbase().getCondition(name);
	}

	/**
	 *  Create a precompiled query.
	 *  @param query	The query string.
	 *  @return The precompiled query.
	 *  @deprecated	Use @link{#createExpression(String)} instead.
	 */
	public IExpression	createQuery(String query)
	{
		return createExpression(query);
	}

	/**
	 *  Create a precompiled expression.
	 *  @param expression	The expression string.
	 *  @return The precompiled expression.
	 */
	public IExpression	createExpression(String expression)
	{
		return capability.getExpressionbase().createExpression(expression);
	}

	/**
	 *  Create a precompiled expression.
	 *  @param expression	The expression string.
	 *  @return The precompiled expression.
	 */
	public IExpression	createExpression(String expression, String[] paramnames, Class[] paramtypes)
	{
		return capability.getExpressionbase().createExpression(expression, paramnames, paramtypes);
	}

	/**
	 *  Create a condition, that is triggered whenever the expression
	 *  value changes to true.
	 *  @param expression	The condition expression.
	 *  @return The condition.
	 */
	public ICondition	createCondition(String expression)
	{
		return createCondition(expression, ICondition.TRIGGER_CHANGES_TO_TRUE, null, null);
	}

	/**
	 *  Create a condition.
	 *  @param expression	The condition expression.
	 *  @param trigger	The condition trigger.
	 *  @return The condition.
	 */
	public ICondition	createCondition(String expression, String trigger, String[] paramnames, Class[] paramtypes)
	{
		return capability.getExpressionbase().createCondition(expression, trigger, paramnames, paramtypes);
	}

	//-------- parameter handling --------

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IParameter[]	getParameters()
	{
		return plan.getParameters();
	}

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IParameterSet[]	getParameterSets()
	{
		return plan.getParameterSets();
	}


	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter.
	 */
	public IParameter getParameter(String name)
	{
		return plan.getParameter(name);
	}

	/**
	 *  Get a parameter.
	 *  @param name The name.
	 *  @return The parameter set.
	 */
	public IParameterSet getParameterSet(String name)
	{
		return plan.getParameterSet(name);
	}

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(String name)
	{
		return plan.hasParameter(name);
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(String name)
	{
		return plan.hasParameterSet(name);
	}

	//-------- internal methods --------

	/**
	 *  This method is called after the plan has been terminated.
	 *  It can be overriden to perform any custom cleanup code
	 *  but this implementation should be called also, because
	 *  it performs cleanup concerning expressions and conditions.
	 * /
	// Replaced by passed(), failed(), aborted().
	protected void cleanup()
	{
		// Cleanup expressions / conditions.
		/*for(int i=0; i<expressions.size(); i++)
		{
			// Resolve references to cleanup original expression.
			IRElement	exp	= (IRElement)expressions.get(i);
			while(exp instanceof RElementReference)
				exp	= ((RElementReference)exp).getReferencedElement();

			exp.cleanup();
		}* /
	}*/

	/**
	 *  Get the plan instance info.
	 *  @return The plan instance info.
	 */
	// todo: make package access
	public RPlan getRPlan()
	{
		return rplan;
	}

	/**
	 *  Get the capability.
	 *  @return The capability.
	 */
	protected RCapability getRCapability()
	{
		return rplan.getScope();
	}

	/**
	 *  Get the capability.
	 *  @return The capability.
	 */
	protected CapabilityWrapper getCapability()
	{
		return capability;
	}
	
	//-------- listeners --------
	
	/**
	 *  Add a plan listener.
	 *  @param listener The plan listener.
	 *  @param async True, if the notification should be done on a separate thread.
	 */
	public void addPlanListener(IPlanListener listener, boolean async)
	{
		IFilter filter = new SystemEventFilter(
			new String[]{ISystemEventTypes.PLAN_ADDED, ISystemEventTypes.PLAN_REMOVED}, getRPlan());
		final AsynchronousSystemEventListener systemlistener 
			= new AsynchronousSystemEventListener(listener, new Tuple(listener, this));
		getRCapability().getAgent().addSystemEventListener(systemlistener, filter, true, async);
	}
	
	/**
	 *  Remove a plan listener.
	 *  @param listener The plan listener.
	 */
	public void removePlanListener(final IPlanListener listener)
	{
		Object	identifier	= new Tuple(listener, this);
		ISystemEventListener[] listeners = getRCapability().getAgent().getSystemEventListeners();
		for(int i=0; i<listeners.length; i++)
		{
			if((listeners[i] instanceof AsynchronousSystemEventListener) 
				&& ((AsynchronousSystemEventListener)listeners[i]).getIdentifier().equals(identifier))
			{
				getRCapability().getAgent().removeSystemEventListener(listeners[i]);
				break;
			}
		}
	}

	//-------- static part -------

	/** The hashtable containing plan init values (hack???). */
	// Needed for passing the rplan to the abstract plan instance.
	// Must be thread safe as more than one agent could use the table
	// at the same time.
	static Map planinit	= SCollection.createHashtable();
}
