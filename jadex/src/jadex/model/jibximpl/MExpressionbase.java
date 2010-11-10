package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;

/**
 *  The model of an expression base containing expressions.
 */
public class MExpressionbase extends MBase implements IMExpressionbase
{
	//-------- xml attributes --------

	/** The expressions. */
	protected ArrayList expressions;

	/** The conditions. */
	protected ArrayList conditions;

	/** The expression references. */
	protected ArrayList expressionrefs;

	/** The condition references. */
	protected ArrayList conditionrefs;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		// The expressions in the expression base are dynamic expressions
		// Therefore set the evaluation mode to dynamic when nothing else
		// is specified.
		for(int i=0; expressions!=null && i<expressions.size(); i++)
		{
			IMExpression exp = (IMExpression)expressions.get(i);
			if(exp.getEvaluationMode()==null)
				exp.setEvaluationMode(IMExpression.MODE_DYNAMIC);
		}

		// Is this a good default for predefined conditions?!
		for(int i=0; conditions!=null && i<conditions.size(); i++)
		{
			IMCondition con = (IMCondition)conditions.get(i);
			if(con.getTrigger()==null)
				con.setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		}

		// create standard reference
		createExpressionReference(STANDARD_EXPRESSION_REFERENCE, IMReferenceableElement.EXPORTED_FALSE, null);
	}
	

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// As all expressions are checked from the outside,
		// check the expressions as part of the base doCheck().
		for(int i=0; expressions!=null && i<expressions.size(); i++)
		{
			MExpression	exp	= (MExpression)expressions.get(i);
			exp.checkClass(exp.getClazz(), report);
		}
	}
	
	//-------- xml methods --------

	/**
	 *  Geneal add method for unmarshalling.
	 *  Necessary to support unordered collections :-(
	 *  @param elem The element to add.
	 */
	public void addElement(IMReferenceableElement elem)
	{
		assert elem instanceof IMCondition || elem instanceof IMConditionReference
			|| elem instanceof IMExpression || elem instanceof IMExpressionReference;


		if(elem instanceof IMCondition)
		{
			if(conditions==null)
				conditions = SCollection.createArrayList();
			conditions.add(elem);
		}
		else if(elem instanceof IMConditionReference)
		{
			if(conditionrefs==null)
				conditionrefs = SCollection.createArrayList();
			conditionrefs.add(elem);
		}
		else if(elem instanceof IMExpression)
		{
			if(expressions==null)
				expressions = SCollection.createArrayList();
			expressions.add(elem);
		}
		else //if(elem instanceof IMBeliefSetReference)
		{
			if(expressionrefs==null)
				expressionrefs = SCollection.createArrayList();
			expressionrefs.add(elem);
		}
	}

	/**
	 *  Geneal add method for marshalling.
	 *  @return Iterator with all elements.
	 */
	public Iterator iterElements()
	{
		return SReflect.getIterator(getReferenceableElements());
	}

	//-------- methods --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		return "expressionbase";
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public IMBase	getCorrespondingBase(IMCapability scope)
	{
		return scope.getExpressionbase();
	}

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public IMReferenceableElement[]	getReferenceableElements()
	{
		Object	ret	= new IMReferenceableElement[0];
		ret	= SUtil.joinArrays(ret, getExpressions());
		ret	= SUtil.joinArrays(ret, getExpressionReferences());
		ret	= SUtil.joinArrays(ret, getConditions());
		ret	= SUtil.joinArrays(ret, getConditionReferences());
		return (IMReferenceableElement[])ret;
	}

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem)
	{
		assert elem!=null;

		if(elem instanceof IMCondition)
			deleteCondition((IMCondition)elem);
		else if(elem instanceof IMConditionReference)
			deleteConditionReference((IMConditionReference)elem);
		else if(elem instanceof IMExpression)
			deleteExpression((IMExpression)elem);
		else if(elem instanceof IMExpressionReference)
			deleteExpressionReference((IMExpressionReference)elem);
		else
			throw new RuntimeException("Element not expression/condition: "+elem);
	}

	//-------- expressions --------

	/**
	 *  Get all defined expressions.
	 *  @return The expressions.
	 */
	public IMExpression[] getExpressions()
	{
		if(expressions==null)
			return MExpression.EMPTY_EXPRESSION_SET;
		return (IMExpression[])expressions.toArray(new IMExpression[expressions.size()]);
	}

	/**
	 *  Get a expression by name.
	 *  @param name	The expression name.
	 *  @return The expression with that name (if any).
	 */
	public IMExpression	getExpression(String name)
	{
		assert name!=null;

		IMExpression ret = null;
		for(int i=0; expressions!=null && i<expressions.size() && ret==null; i++)
		{
			IMExpression test = (IMExpression)expressions.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a new expression.
	 *  @param name	The expression name.
	 *  @param expression	The expression string.
	 *  @param clazz	The expected type for values.
	 *  @param exported	Flag indicating if this expression may be referenced from outside capabilities.
	 *  @param paramnames	The names of the parameters.
	 *  @param paramtypes	The types of the parameters.
	 *  @return The modelelement of the expression.
	 */
	public IMExpression	createExpression(String name, String expression, Class clazz, String exported, String[] paramnames, Class[] paramtypes)
	{
		assert expression!=null : this;
		if(expressions==null)
			expressions = SCollection.createArrayList();

		MExpression ret = new MExpression();
		ret.setName(name);
		ret.setExpressionText(expression);
		ret.setClazz(clazz);
		ret.setEvaluationMode(IMExpression.MODE_DYNAMIC);
		ret.setExported(exported);
		ret.setOwner(this);
		if(paramnames!=null)
		{
			if(paramtypes==null || paramtypes.length!=paramnames.length)
				throw new RuntimeException("Invalid expression parameter settings.");

			for(int i=0; i<paramnames.length; i++)
			{
				ret.addExpressionParameter(new ExpressionParameterInfo(paramnames[i], null, paramtypes[i]));
			}
		}
		ret.init();
		expressions.add(ret);
		return ret;
	}

	/**
	 *  Delete an expression.
	 *  @param expression	The expression.
	 */
	public void	deleteExpression(IMExpression expression)
	{
		if(!expressions.remove(expression))
			throw new RuntimeException("Expression not found: "+expression);
	}


	//-------- expression references --------

	/**
	 *  Get all expression references.
	 *  @return The expression references.
	 */
	public IMExpressionReference[] getExpressionReferences()
	{
		if(expressionrefs==null)
			return new IMExpressionReference[0];
		return (IMExpressionReference[])expressionrefs
			.toArray(new IMExpressionReference[expressionrefs.size()]);
	}

	/**
	 *  Get an expression reference.
	 *  @param name The name.
	 *  @return The expression reference.
	 */
	public IMExpressionReference getExpressionReference(String name)
	{
		assert name!=null;

		IMExpressionReference ret = null;
		for(int i=0; expressionrefs!=null && i<expressionrefs.size() && ret==null; i++)
		{
			IMExpressionReference test = (IMExpressionReference)expressionrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a new expression reference.
	 *  @param name The name.
	 *  @param exported	Flag indicating if this element may be referenced from outside capabilities.
	 *  @param ref	The referenced expression (or null for abstract).
	 *  @return The modelelement of the expression reference.
	 */
	public IMExpressionReference	createExpressionReference(String name, String exported, String ref)
	{
		if(expressionrefs==null)
			expressionrefs = SCollection.createArrayList();

		MExpressionReference ret = new MExpressionReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		expressionrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete an expression reference.
	 *  @param reference	The expression reference.
	 */
	public void	deleteExpressionReference(IMExpressionReference reference)
	{
		if(!expressionrefs.remove(reference))
			throw new RuntimeException("Expression reference not found: "+reference);
	}


	//-------- conditions --------

	/**
	 *  Get all defined conditions.
	 *  @return The conditions.
	 */
	public IMCondition[] getConditions()
	{
		if(conditions==null)
			return new IMCondition[0];
		return (IMCondition[])conditions.toArray(new IMCondition[conditions.size()]);
	}

	/**
	 *  Get a condition by name.
	 *  @param name	The condition name.
	 *  @return The condition with that name (if any).
	 */
	public IMCondition	getCondition(String name)
	{
		assert name!=null;

		IMCondition ret = null;
		for(int i=0; conditions!=null && i<conditions.size() && ret==null; i++)
		{
			IMCondition test = (IMCondition)conditions.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a new condition.
	 *  @param name The name.
	 *  @param expression The expression string.
	 *  @param trigger The trigger type.
	 *  @param exported	Flag indicating if this event reference may be referenced from outside capabilities.
	 *  @return The new condition model element.
	 */
	public IMCondition createCondition(String name, String expression, String trigger, String exported, String[] paramnames, Class[] paramtypes)
	{
		if(conditions==null)
			conditions = SCollection.createArrayList();

		MCondition ret = new MCondition();
		ret.setName(name);
		ret.setExpressionText(expression);
		if(trigger!=null)
			ret.setTrigger(trigger);
		else
			ret.setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		ret.setExported(exported);
		ret.setOwner(this);
		if(paramnames!=null)
		{
			if(paramtypes==null || paramtypes.length!=paramnames.length)
				throw new RuntimeException("Invalid expression parameter settings.");

			for(int i=0; i<paramnames.length; i++)
			{
				ret.addExpressionParameter(new ExpressionParameterInfo(paramnames[i], null, paramtypes[i]));
			}
		}
		ret.init();
		conditions.add(ret);
		return ret;
	}

	/**
	 *  Delete a condition.
	 *  @param condition	The condition.
	 */
	public void	deleteCondition(IMCondition condition)
	{
		if(!conditions.remove(condition))
			throw new RuntimeException("Condition not found: "+condition);
	}

	//-------- condition references --------

	/**
	 *  Get all condition references.
	 *  @return The condition references.
	 */
	public IMConditionReference[] getConditionReferences()
	{
		if(conditionrefs==null)
			return new IMConditionReference[0];
		return (IMConditionReference[])conditionrefs
			.toArray(new IMConditionReference[conditionrefs.size()]);
	}

	/**
	 *  Get a condition reference.
	 *  @param name The name.
	 *  @return The condition reference.
	 */
	public IMConditionReference getConditionReference(String name)
	{
		assert name!=null;

		IMConditionReference ret = null;
		for(int i=0; conditionrefs!=null && i<conditionrefs.size() && ret==null; i++)
		{
			IMConditionReference test = (IMConditionReference)conditionrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a new condition reference.
	 *  @param name The name.
	 *  @param exported	Flag indicating if this element may be referenced from outside capabilities.
	 *  @param ref	The referenced condition (or null for abstract).
	 *  @return The modelelement of the condition reference.
	 */
	public IMConditionReference	createConditionReference(String name, String exported, String ref)
	{
		if(conditionrefs!=null)
			conditionrefs = SCollection.createArrayList();

		MConditionReference ret = new MConditionReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		conditionrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete an condition reference.
	 *  @param reference	The condition reference.
	 */
	public void	deleteConditionReference(IMConditionReference reference)
	{
		if(!conditionrefs.remove(reference))
			throw new RuntimeException("Condition reference not found: "+reference);
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MExpressionbase clone = (MExpressionbase)cl;
		if(expressions!=null)
		{
			clone.expressions = SCollection.createArrayList();
			for(int i=0; i<expressions.size(); i++)
				clone.expressions.add(((MElement)expressions.get(i)).clone());
		}
		if(expressionrefs!=null)
		{
			clone.expressionrefs = SCollection.createArrayList();
			for(int i=0; i<expressionrefs.size(); i++)
				clone.expressionrefs.add(((MElement)expressionrefs.get(i)).clone());
		}
		if(conditions!=null)
		{
			clone.conditions = SCollection.createArrayList();
			for(int i=0; i<conditions.size(); i++)
				clone.conditions.add(((MElement)conditions.get(i)).clone());
		}
		if(conditionrefs!=null)
		{
			clone.conditionrefs = SCollection.createArrayList();
			for(int i=0; i<conditionrefs.size(); i++)
				clone.conditionrefs.add(((MElement)conditionrefs.get(i)).clone());
		}
	}
}
