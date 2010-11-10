package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.ICondition;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.AbstractAgendaAction;
import jadex.util.collection.SCollection;
import jadex.util.collection.WeakList;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


/**
 *  The container for all expressions.
 */
public class RExpressionbase extends RBase
{
	//-------- attributes --------
	
	/** The observed expressions, i.e. expressions that cause a system event. */
	protected Set	observed_expressions;

	/** The expressions (unmodifiable view). */
	protected Set	expview;
	
	/** The traced conditions. */
	protected Collection	traced_conditions;

	/** The conditions (unmodifiable view. */
	protected Collection	condview;
	
	//-------- constructors --------

	/**
	 *  Create a new planbase instance.
	 *  @param model	The expression base model.
	 *  @param owner	The owner of the instance.
	 */
	protected RExpressionbase(IMExpressionbase model, RElement owner)
	{
		super(null, model, owner);
		this.observed_expressions	= SCollection.createWeakSet();
//		this.traced_conditions	= SCollection.createWeakSet();
//		this.traced_conditions	= SCollection.createOrderedWeakSet();
//		this.traced_conditions	= SCollection.createWeakList();
		this.traced_conditions	= SCollection.concurrencyCheckingList(SCollection.createWeakList());
	}

//	/**
//	 *  Initialize the expression base.
//	 */
//	protected void	init(int level)
//	{
//		// todo: initial expressions?
//	}

	//-------- methods --------

	/**
	 *  Get an expression created from a predefined expression.
	 *  Always creates a new runtime expression.
	 *  @param name	The name of an expression defined in the ADF.
	 *  @return The expression object.
	 */
	public IRExpression	getExpression(String name)
	{
        IMReferenceableElement	exp	= ((IMExpressionbase)getModelElement()).getReferenceableElement(name);
        if(exp==null)
        	throw new RuntimeException("No such expression: "+name);
		return createExpression(exp, null);
	}

	/**
	 *  Create a precompiled expression.
	 *  @param query	The expression.
	 *  @param paramnames	The names of the parameters.
	 *  @param paramtypes	The types of the parameters.
	 *  @return The precompiled expression.
	 */
	public IRExpression	createExpression(String query, String[] paramnames, Class[] paramtypes)
	{
		if(paramnames==null && paramtypes!=null || paramnames!=null && paramtypes==null ||
			paramnames!=null && paramnames.length!=paramnames.length)
			throw new RuntimeException("Parameter invalid: "+paramnames);

		IRExpression ret = null;
		IMExpression mex = ((IMExpressionbase)getModelElement()).createExpression(null, query, null, IMReferenceableElement.EXPORTED_FALSE, paramnames, paramtypes);
		ret = createExpression(mex, null);
		ret.setTemporary(true);
		return ret;
	}

	/**
	 *  Create a condition, that is triggered whenever the expression
	 *  value changes to true.
	 *  @param expression	The condition expression.
	 *  @return The condition.
	 */
	public IRCondition	createCondition(String expression)
	{
		return createCondition(expression, ICondition.TRIGGER_CHANGES_TO_TRUE, null, null);
	}

	/**
	 *  Create a condition.
	 *  @param expression	The condition expression.
	 *  @param trigger	The condition trigger.
	 *  @return The condition.
	 * /
	public IRCondition	createCondition(String expression, String trigger)
	{
		IMCondition mcond = ((IMExpressionbase)getModelElement()).createCondition(null, expression, trigger, false, null, null);
		IRCondition ret = createCondition(mcond, null);
		ret.setTemporary(true);
		return ret;
	}*/

	/**
	 *  Create a precompiled expression.
	 *  @param query	The expression.
	 *  @param paramnames	The names of the parameters.
	 *  @param paramtypes	The types of the parameters.
	 *  @return The precompiled expression.
	 */
	public IRCondition	createCondition(String query, String trigger, String[] paramnames, Class[] paramtypes)
	{
		if(paramnames==null && paramtypes!=null || paramnames!=null && paramtypes==null ||
			paramnames!=null && paramnames.length!=paramnames.length)
			throw new RuntimeException("Parameter invalid: "+paramnames);

		IRCondition ret = null;
		IMCondition mcond = ((IMExpressionbase)getModelElement()).createCondition(null, query, trigger, IMReferenceableElement.EXPORTED_FALSE, paramnames, paramtypes);
		ret = createCondition(mcond, null);
		ret.setTemporary(true);
		return ret;
	}

	//-------- internal methods --------

	/**
	 *  Factory method for expression creation.
	 *  @param model The expression model.
	 *  @return The new expression.
	 */
	protected IRExpression	createExpression(IMReferenceableElement model, RReferenceableElement creator)
	{
		IRExpression ret = internalCreateExpression(model, creator);
		ret.getOriginalElement().initStructure();
		return ret;
	}

	/**
	 *  Factory method for expression creation.
	 *  @param model The expression model.
	 *  @return The new expression.
	 */
	protected IRExpression	internalCreateExpression(IMReferenceableElement model, RReferenceableElement creator)
	{
		assert creator==null || creator instanceof IRExpression: creator;

		IRExpression ret = null;

		if(model.getScope()!=getScope().getModelElement())
			throw new RuntimeException("Creation of elements only allowed in definition scope! "+model.getName());

		if(model instanceof IMExpression)
		{
			// todo: make custom exps reusable (would also require system event)
			ret = new RExpression((IMExpression)model, this, creator, null);
		}
		else if(model instanceof IMExpressionReference)
		{
			// Do not add referenced expressions (otherwise would be affected in wrong scope).
			ret = new RExpressionReference((IMExpressionReference)model, this, creator);
		}
		else
		{
			throw new RuntimeException("Element not an expression: "+model);
		}

		return ret;
	}

	/**
	 *  Factory method for condition creation.
	 *  @param model The condition model.
	 *  @return The new condition.
	 */
	public IRCondition	createCondition(IMReferenceableElement model,
			RReferenceableElement creator)
	{
		assert creator==null || creator instanceof IRCondition : creator;

		IRCondition ret = null;

		if(model.getScope()!=getScope().getModelElement())
			throw new RuntimeException("Creation of elements only allowed in definition scope! "+model.getName());

		if(model instanceof IMCondition)
		{
			ret = new RCondition((IMCondition)model, this, creator, null);
			//conditions.add(ret);	// Only add "original" conditions.
		}
		else if(model instanceof IMConditionReference)
		{
			// Do not add referenced conditions (otherwise would be traced in wrong scope).
			RConditionReference rcr = new RConditionReference((IMConditionReference)model, this, creator);
			rcr.init();
			ret	= rcr;
		}
		else
		{
			throw new RuntimeException("Element not an expression: "+model);
		}

		return ret;
	}

	/**
	 *  Get a condition per name.
	 *  Always creates new conditions.
	 */
	public IRCondition getCondition(String name)
	{
        IMReferenceableElement mcond = ((IMExpressionbase)getModelElement()).getReferenceableElement(name);
        if(mcond==null)
        	throw new RuntimeException("No such condition: "+name);
		return createCondition(mcond, null);
	}

	/**
	 *  Evaluate an expression without creating an RExpression object,
	 *  but considering local scope expression parameters.
	 *  Internal expressions are not stored in the expression base.
	 *  @param model The model element.
	 *  @return The expression value.
	 */
	public Object	evaluateInternalExpression(IMExpression model, RElement owner)
	{
		assert model!=null: "Expression must not be null.";
		assert model.getScope()==getScope().getModelElement()
			:"Creation of elements only allowed in definition scope! "+model.getName();

		return RExpression.evaluateExpression(model, owner.getExpressionParameters());
	}

	/**
	 *  Create a new expression, internal to the given owner.
	 *  @param model The model element.
	 *  @param owner The owner.
	 *  @param systemevent The system event.
	 *  @return The new expression.
	 */
	public RExpression createInternalExpression(IMExpression model, RElement owner, SystemEvent systemevent)
	{
		if(model.getScope()!=owner.getScope().getModelElement())
			throw new RuntimeException("Creation of elements only allowed in definition scope! "+model.getName());

		return new RExpression(model, owner, null, systemevent);
	}

	/**
	 *  Create a new condition, internal to the given owner.
	 *  Internal condition are not stored in the expression base.
	 *  @param model The model element.
	 *  @param owner The owner.
	 *  @param action The action.
	 *  @param bindings The bindings (for binding conditions).
	 *  @return The new condition.
	 */
	protected RCondition createInternalCondition(IMCondition model, RElement owner
		, AbstractAgendaAction action, BindingHelper bindings)
	{
		if(model.getScope()!=owner.getScope().getModelElement())
			throw new RuntimeException("Creation of elements only allowed in definition scope! "+model.getName());

		RCondition	ret;
		if(model instanceof IMBindingCondition)
		{
			ret	= new RBindingCondition((IMBindingCondition)model, owner, action, bindings);
		}
		else
		{
			ret	= new RCondition(model, owner, null, action);
		}
		return ret;
	}

	/**
	 *  Add an expression.
	 */
	protected void addExpression(RExpression expression)
	{
		if(!observed_expressions.add(expression))
			throw new RuntimeException("Expression already contained: "+expression);
	}

	/**
	 *  Remove an expression, when it is no longer used.
	 */
	protected void removeExpression(RExpression expression)
	{
		observed_expressions.remove(expression);
		//if(getScope().blocked)
		//	System.out.println("shiTttt");
		//if(!observed_expressions.remove(expression))
		//	throw new RuntimeException("No such expression: "+expression);
	}

	/**
	 *  Add a traced condition.
	 */
	protected void addCondition(IInterpreterCondition condition)
	{
		if(!traced_conditions.add(condition))
			throw new RuntimeException("Condition already contained: "+condition);
	}

	/**
	 *  Remove a condition, when it is no longer used.
	 */
	protected void removeCondition(IInterpreterCondition condition)
	{
		traced_conditions.remove(condition);
	}

	/**
	 *  Check if the given condition is contained in the expression base.
	 *  @param condition	The condition.
	 * /
	protected boolean containsCondition(RCondition condition)
	{
		return conditions.contains(condition);
	}*/

	/**
	 *  Get all (original) expressions.
	 *  References are currently not stored.
	 */
	protected Set	getExpressions()
	{
		return expview!=null ? expview
			: (expview=Collections.unmodifiableSet(observed_expressions));
	}

	/**
	 *  Get all (original) conditions.
	 *  References are currently not stored.
	 */
	public Collection	getConditions()
	{
		return condview!=null ? condview
			: (condview=Collections.unmodifiableCollection(traced_conditions));
	}

	/**
	 *  Register a new expression model.
	 *  @param mexpression The expression model.
	 */
	public void registerExpression(IMExpression mexpression)
	{
		// todo: NOP?
	}

	/**
	 *  Register a new expression reference model.
	 *  @param mexpressionref The expression reference model.
	 */
	public void registerExpressionReference(IMExpressionReference mexpressionref)
	{
		// todo: NOP?
	}

	/**
	 *  Register a new condition model.
	 *  @param mcondition The condition model.
	 */
	public void registerCondition(IMCondition mcondition)
	{
		// todo: NOP?
	}

	/**
	 *  Register a new condition reference model.
	 *  @param mconditionref The condition reference model.
	 */
	public void registerConditionReference(IMConditionReference mconditionref)
	{
		// todo: NOP?
	}

	/**
	 *  Deregister an expression model.
	 *  @param mexpression The expression model.
	 */
	public void deregisterExpression(IMExpression mexpression)
	{
		// todo: cleanup instances?
	}

	/**
	 *  Deregister an expression reference model.
	 *  @param mexpressionref The expression reference model.
	 */
	public void deregisterExpressionReference(IMExpressionReference mexpressionref)
	{
		// todo: cleanup instances?
	}

	/**
	 *  Deregister an condition model.
	 *  @param mcondition The condition model.
	 */
	public void deregisterCondition(IMCondition mcondition)
	{
		// todo: cleanup instances?
	}

	/**
	 *  Deregister an condition reference model.
	 *  @param mconditionref The condition reference model.
	 */
	public void deregisterConditionReference(IMConditionReference mconditionref)
	{
		// todo: cleanup instances?
	}

	//-------- RBase abstract methods --------

	/**
	 *  Get the runtime element for a model element.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param melement	The model of the element to be retrieved.
	 *  @param creator	The creator of the element (e.g. a reference).
	 */
	protected RReferenceableElement	getElementInstance(
			IMReferenceableElement melement, RReferenceableElement creator)
	{
		RCapability	scope	= getScope().getAgent().lookupCapability(melement.getScope());
		if(melement instanceof IMCondition || melement instanceof IMConditionReference)
			return (RReferenceableElement)scope.getExpressionbase().createCondition(melement, creator);
		else if(melement instanceof IMExpression || melement instanceof IMExpressionReference)
			return (RReferenceableElement)scope.getExpressionbase().internalCreateExpression(melement, creator);
		else
			throw new RuntimeException("Cannot create element (neither expression nor condition): "+melement);
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected RBase getCorrespondingBase(RCapability scope)
	{
		return scope.getExpressionbase();
	}

}
