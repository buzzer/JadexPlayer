package jadex.model.jibximpl;

import jadex.model.*;
import jadex.model.MessageType.ParameterSpecification;
import jadex.parser.ITerm;
import jadex.parser.ParserException;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.MultiCollection;
import jadex.util.collection.SCollection;

import java.lang.reflect.Array;
import java.util.*;

/**
 *  Represents a term that can be evaluated.
 */
public class MExpression extends MReferenceableElement implements IMExpression
{
	//-------- constants --------

	/** The belief reference type. */
	protected static final String	BELIEF_REF = "beliefref";

	/** The belief set reference type. */
	protected static final String	BELIEFSET_REF = "beliefsetref";

	/** The goal reference type. */
	protected static final String	GOAL_REF = "goalref";

	/** The parameter reference type. */
	protected static final String	PARAMETER_REF = "parameterref";

	/** The parameter set reference type. */
	protected static final String	PARAMETERSET_REF = "parametersetref";

	/** Empty expression array. */
	protected static final IMExpression[] EMPTY_EXPRESSION_SET = new IMExpression[0];

	//-------- xml attributes --------

	/** The evaluation mode. */
	protected String evaluationmode;

	/** The classname. */
	protected String classname;

	/** The expression text. */
	protected String exptext;

	/** The list of expression parameters. */
	protected ArrayList expressionparameters;

	/** The list of relevant beliefs. */
	protected ArrayList relevantbeliefs;

	/** The list of relevant beliefsets. */
	protected ArrayList relevantbeliefsets;

	/** The list of relevant goals. */
	protected ArrayList relevantgoals;

	/** The list of relevant parameters. */
	protected ArrayList relevantparameters;

	/** The list of relevant parameter sets. */
	protected ArrayList relevantparametersets;

	//-------- attributes --------

	/** The constructed term (which is the connection to the parser) */
	protected ITerm term;

	/** The relevant events (model element->event types). */
	protected MultiCollection	relevant;

	/** The expected class of the expression. */
	protected Class clazz;

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(expressionparameters!=null)
			ret.addAll(expressionparameters);
		if(relevantbeliefs!=null)
			ret.addAll(relevantbeliefs);
		if(relevantbeliefsets!=null)
			ret.addAll(relevantbeliefsets);
		if(relevantgoals!=null)
			ret.addAll(relevantgoals);
		if(relevantparameters!=null)
			ret.addAll(relevantparameters);
		if(relevantparametersets!=null)
			ret.addAll(relevantparametersets);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if class can be found.
		if(getClassname()!=null && SReflect.findClass0(getClassname(), getScope().getFullImports())==null)
			report.addEntry(this, "Unknown type: "+getClassname());

		// Check if expression can be parsed.
		try
		{
			term	= getTerm();
		}
		catch(RuntimeException e)
		{
			report.addEntry(this, e.toString());
		}
		
		// Check names of expression parameters.
		MBase.checkNameUniqueness(this, report, getExpressionParameters());

		// Check if relevant elements are found.
		IMRelevantElement[]	rel	= getRelevantBeliefs();
		for(int i=0; i<rel.length; i++)
		{
			if(findRelevantElement(rel[i].getReference(), BELIEF_REF)==null)
			{
				report.addEntry(this, "Relevant belief '"+rel[i].getReference()+"' not found.");
			}
		}
		rel	= getRelevantBeliefSets();
		for(int i=0; i<rel.length; i++)
		{
			if(findRelevantElement(rel[i].getReference(), BELIEFSET_REF)==null)
			{
				report.addEntry(this, "Relevant belief set '"+rel[i].getReference()+"' not found.");
			}
		}
		rel	= getRelevantGoals();
		for(int i=0; i<rel.length; i++)
		{
			if(findRelevantElement(rel[i].getReference(), GOAL_REF)==null)
			{
				report.addEntry(this, "Relevant goal '"+rel[i].getReference()+"' not found.");
			}
		}
		rel	= getRelevantParameters();
		for(int i=0; i<rel.length; i++)
		{
			if(findRelevantElement(rel[i].getReference(), PARAMETER_REF)==null)
			{
				report.addEntry(this, "Relevant parameter '"+rel[i].getReference()+"' not found.");
			}
		}
		rel	= getRelevantParameterSets();
		for(int i=0; i<rel.length; i++)
		{
			if(findRelevantElement(rel[i].getReference(), PARAMETERSET_REF)==null)
			{
				report.addEntry(this, "Relevant parameter set '"+rel[i].getReference()+"' not found.");
			}
		}
	}
	
	/**
	 *  Check the type of the expression.
	 *  This method should be called by MElements that
	 *  contain expressions from their doCheck() methods.
	 *  For convenience, the check is only performed if clazz is not null.
	 */
	protected void checkClass(Class clazz, Report report)
	{
		if(clazz!=null)
		{
			ITerm	term	= null;
			try
			{
				term	= getTerm();
			}
			catch(RuntimeException e){}
			
			if(term!=null && term.getStaticType()!=null)
			{
				if(!SReflect.isSupertype(clazz, term.getStaticType()))
				{
					report.addEntry(this, "Type "+term.getStaticType().getName()+" of expression incompatible with required type "+clazz.getName()+".");				
				}
			}
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected boolean assignable(IMElementReference ref)
	{
		return ref instanceof IMExpressionReference && !(ref instanceof IMConditionReference);
	}

	//-------- evaluation mode --------

	/**
	 *  Get the evaluation mode.
	 *  @return	The evaluation mode.
	 */
	public String	getEvaluationMode()
	{
		return evaluationmode;
	}

	/**
	 *  Set the evaluation mode.
	 *  @param eva	The evaluation mode.
	 */
	public void setEvaluationMode(String eva)
	{
		this.evaluationmode = eva;
	}

	//-------- classname --------

	/**
	 *  Get the class name.
	 *  @return	The class name.
	 */
	public String	getClassname()
	{
		return classname;
	}

	/**
	 *  Set the class name.
	 *  @param classname The classname.
	 */
	public void setClassname(String classname)
	{
		this.classname = classname;
	}

	//-------- expression text --------

	/**
	 *  Get the expression text.
	 *  @return The expression text.
	 */
	public String getExpressionText()
	{
		// Removed asserts as arguments might be pure declarations without value 
		//assert exptext!=null : this;
		//assert exptext.length()>0: this;
		return exptext;
	}

	/**
	 *  Set the expression text.
	 *  @param expression	The expression text.
	 */
	public void setExpressionText(String expression)
	{
		// Removed asserts as arguments might be pure declarations without value 
		//assert expression!=null: this;
		//assert expression.length()>0: this;
		this.exptext = expression;
	}

	//-------- expression parameters --------

	/**
	 *  Get all expression parameters.
	 *  @return The expression parameters.
	 */
	public IMExpressionParameter[] getExpressionParameters()
	{
		if(expressionparameters==null)
			return new IMExpressionParameter[0];
		return (IMExpressionParameter[])expressionparameters
			.toArray(new IMExpressionParameter[expressionparameters.size()]);
	}

	/**
	 *  Create an expression parameter.
	 *  @param name The name.
	 *  @param clazz The clazz.
	 */
	public IMExpressionParameter createExpressionParameter(String name, Class clazz)
	{
		if(expressionparameters==null)
			expressionparameters = SCollection.createArrayList();

		MExpressionParameter ret = new MExpressionParameter();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setOwner(this);
		ret.init();
		expressionparameters.add(ret);
		return ret;
	}

	/**
	 *  Delete an expression parameter.
	 *  @param param The expression parameter.
	 */
	public void deleteExpressionParameter(IMExpressionParameter param)
	{
		if(!expressionparameters.remove(param))
			throw new RuntimeException("Could not find element to remove: "+param);
	}

	//-------- relevant elements --------

	/**
	 *  Get the relevant beliefs.
	 *  @return The relevant beliefs.
	 */
	public IMRelevantElement[]	getRelevantBeliefs()
	{
		if(relevantbeliefs==null)
			return EMPTY_RELSET;
		return (IMRelevantElement[])relevantbeliefs
			.toArray(new IMRelevantElement[relevantbeliefs.size()]);
	}

	/**
	 *  Add a relevant belief.
	 *  @param ref	The referenced belief.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement createRelevantBelief(String ref, String event)
	{
		if(relevantbeliefs==null)
			relevantbeliefs = SCollection.createArrayList();

		MRelevantElement ret = new MRelevantElement();
		ret.setReference(ref);
		ret.setEventType(event);
		ret.setOwner(this);
		ret.init();
		relevantbeliefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a relevant belief.
	 *  @param ref	The referenced belief.
	 */
	public void	deleteRelevantBelief(IMRelevantElement ref)
	{
		if(!relevantbeliefs.remove(ref))
			throw new RuntimeException("Element not found: "+ref);
	}


	/**
	 *  Get the relevant belief sets.
	 *  @return The relevant belief sets.
	 */
	public IMRelevantElement[]	getRelevantBeliefSets()
	{
		if(relevantbeliefsets==null)
			return EMPTY_RELSET;
		return (IMRelevantElement[])relevantbeliefsets
			.toArray(new IMRelevantElement[relevantbeliefsets.size()]);
	}

	/**
	 *  Add a relevant belief set.
	 *  @param ref	The referenced belief set.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement createRelevantBeliefSet(String ref, String event)
	{
		if(relevantbeliefsets==null)
			relevantbeliefsets = SCollection.createArrayList();

		MRelevantElement ret = new MRelevantElement();
		ret.setReference(ref);
		ret.setEventType(event);
		ret.setOwner(this);
		ret.init();
		relevantbeliefsets.add(ret);
		return ret;
	}

	/**
	 *  Delete a relevant belief set.
	 *  @param ref	The referenced belief set.
	 */
	public void	deleteRelevantBeliefSet(IMRelevantElement ref)
	{
		if(!relevantbeliefsets.remove(ref))
			throw new RuntimeException("Element not found: "+ref);
	}

	/**
	 *  Get the relevant goals.
	 *  @return The relevant goals.
	 */
	public IMRelevantElement[]	getRelevantGoals()
	{
		if(relevantgoals==null)
			return EMPTY_RELSET;
		return (IMRelevantElement[])relevantgoals
			.toArray(new IMRelevantElement[relevantgoals.size()]);
	}

	/**
	 *  Add a relevant goal.
	 *  @param ref	The referenced goal.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement createRelevantGoal(String ref, String event)
	{
		if(relevantgoals==null)
			relevantgoals = SCollection.createArrayList();

		MRelevantElement ret = new MRelevantElement();
		ret.setReference(ref);
		ret.setEventType(event);
		ret.setOwner(this);
		ret.init();
		relevantgoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a relevant goal.
	 *  @param ref	The referenced goal.
	 */
	public void	deleteRelevantGoal(IMRelevantElement ref)
	{
		if(!relevantgoals.remove(ref))
			throw new RuntimeException("Element not found: "+ref);
	}

	/**
	 *  Get the relevant parameters.
	 *  @return The relevant parameters.
	 */
	public IMRelevantElement[]	getRelevantParameters()
	{
		if(relevantparameters==null)
			return EMPTY_RELSET;
		else
			return (IMRelevantElement[])relevantparameters
				.toArray(new IMRelevantElement[relevantparameters.size()]);
	}

	/**
	 *  Add a relevant parameter.
	 *  @param ref	The referenced parameter.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement createRelevantParameter(String ref, String event)
	{
		if(relevantparameters==null)
			relevantparameters = SCollection.createArrayList();

		MRelevantElement ret = new MRelevantElement();
		ret.setReference(ref);
		ret.setEventType(event);
		ret.setOwner(this);
		ret.init();
		relevantparameters.add(ret);
		return ret;
	}

	/**
	 *  Delete a relevant parameter.
	 *  @param ref	The referenced parameter.
	 */
	public void	deleteRelevantParameter(IMRelevantElement ref)
	{
		if(!relevantparameters.remove(ref))
			throw new RuntimeException("Element not found: "+ref);
	}

	/**
	 *  Get the relevant parameter sets.
	 *  @return The relevant parameter sets.
	 */
	public IMRelevantElement[]	getRelevantParameterSets()
	{
		if(relevantparametersets==null)
			return EMPTY_RELSET;
		return (IMRelevantElement[])relevantparametersets
			.toArray(new IMRelevantElement[relevantparametersets.size()]);
	}

	/**
	 *  Add a relevant parameter set.
	 *  @param ref	The referenced parameter set.
	 *  @param event	The relevant system event (or null for default).
	 */
	public IMRelevantElement createRelevantParameterSet(String ref, String event)
	{
		if(relevantparametersets==null)
			relevantparametersets = SCollection.createArrayList();

		MRelevantElement ret = new MRelevantElement();
		ret.setReference(ref);
		ret.setEventType(event);
		ret.setOwner(this);
		ret.init();
		relevantparametersets.add(ret);
		return ret;
	}

	/**
	 *  Delete a relevant parameter set.
	 *  @param ref	The referenced parameter set.
	 */
	public void	deleteRelevantParameterSet(IMRelevantElement ref)
	{
		if(!relevantparametersets.remove(ref))
			throw new RuntimeException("Element not found: "+ref);
	}

	//-------- non xml-relevant methods --------

	/** The name counter. */
	private static int mcnt;

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		// Names of internal expressions may null and are generated when required
		if(name==null)
		{
			// Name generation not allowed for checking.
			assert !isChecking();
			String	name	= SReflect.getInnerClassName(this.getClass())+"#"+(++mcnt);
			setName(name);
		}
		return super.getName();
	}

	/**
	 *  Get the expression parameters.
	 *  @return The expression parameters.
	 */
	public List getSystemExpressionParameters()
	{
		List	exparams	= super.getSystemExpressionParameters();

		// Make binding parameters available in plan precondition!. (Hack!!!)
		IMPlan plan = null;
		if(getOwner() instanceof IMPlan && ((IMPlan)getOwner()).getPrecondition()==this)
		{
			plan = (IMPlan)getOwner();
			IMParameter[] bps = plan.getBindingParameters();
			for(int i=0; i<bps.length; i++)
			{
				exparams.add(new ExpressionParameterInfo(bps[i].getName(), null, bps[i].getClazz()));
			}
		}

		// Make available $goal, $event for plan parameters and match expression
		if(getOwner() instanceof IMPlanParameter)
			plan = (IMPlan)getOwner().getOwner();
		if(getOwner() instanceof IMReference && (getOwner().getOwner().getOwner() instanceof IMPlan))
			plan = (IMPlan)getOwner().getOwner().getOwner();

		if(plan!=null && plan.getTrigger()!=null)
		{
			IMReference[] grefs = plan.getTrigger().getGoals();
			if(grefs.length>0)
			{
				// Get first trigger goal (all trigger goals must support all params)
				IMReferenceableElement gref = getScope().getGoalbase().getReferenceableElement(grefs[0].getReference());
				if(gref!=null)
					exparams.add(new ExpressionParameterInfo("$goal", gref, "jadex.runtime.impl.IRGoal"));
			}

			// Get first trigger event (all trigger event must support all params)
			IMReference[] ierefs = plan.getTrigger().getInternalEvents();
			if(ierefs.length>0)
			{
				IMReferenceableElement ieref = getScope().getEventbase().getReferenceableElement(ierefs[0].getReference());
				if(ieref!=null)
					exparams.add(new ExpressionParameterInfo("$event", ieref, "jadex.runtime.impl.IREvent"));
			}
			else
			{
				IMReference[] merefs = plan.getTrigger().getMessageEvents();
				if(merefs.length>0)
				{
					IMReferenceableElement meref = getScope().getEventbase().getReferenceableElement(merefs[0].getReference());
					if(meref!=null)
						exparams.add(new ExpressionParameterInfo("$event", meref, "jadex.runtime.impl.IREvent"));
				}
			}
		}

		// Make message parameters available in the message event match expression
		if(getOwner() instanceof IMMessageEvent && ((IMMessageEvent)getOwner()).getMatchExpression()==this)
		{
			IMMessageEvent mevent = (IMMessageEvent)getOwner();
			IMParameter[] params = mevent.getParameters();
			HashSet allps = SCollection.createHashSet();
			for(int i=0; i<params.length; i++)
			{
				// Hack! converts "-" to "_" because variable names must not contain "-" in Java
				allps.add(params[i].getName());
				String paramname = "$"+SUtil.replace(params[i].getName(), "-", "_");
				exparams.add(new ExpressionParameterInfo(paramname, params[i], params[i].getClazz()));
			}
			IMParameterSet[] paramsets = mevent.getParameterSets();
			for(int i=0; i<paramsets.length; i++)
			{
				// Hack! converts "-" to "_" because variable names must not contain "-" in Java
				allps.add(paramsets[i].getName());
				String paramsetname = "$"+SUtil.replace(paramsets[i].getName(), "-", "_");
				exparams.add(new ExpressionParameterInfo(paramsetname, paramsets[i],
					Array.newInstance(paramsets[i].getClazz(), new int[1]).getClass()));
			}
			exparams.add(new ExpressionParameterInfo("$messagemap", null, Map.class));

			if(isChecking())
			{
				// Check is performed before init on MessageEvent can add default params
				MessageType mt = mevent.getMessageType();
				ParameterSpecification ps[] = mt.getParameters();
				for(int i=0; i<ps.length; i++)
				{
					if(!allps.contains(ps[i].getName()))
					{
						String paramname = "$"+SUtil.replace(ps[i].getName(), "-", "_");
						exparams.add(new ExpressionParameterInfo(paramname, null, ps[i].getClazz()));
					}
				}
				ps = mt.getParameterSets();
				for(int i=0; i<ps.length; i++)
				{
					if(!allps.contains(ps[i].getName()))
					{
						String paramsetname = "$"+SUtil.replace(ps[i].getName(), "-", "_");
						exparams.add(new ExpressionParameterInfo(paramsetname, null, Array.newInstance(ps[i].getClazz(), new int[1]).getClass()));
					}
				}
			}
		}

		return exparams;
	}

	/**
	 *  Get the relevant list.
	 *  Hack!!! todo: remove or change?!
	 */
	public MultiCollection getRelevantList()
	{
		if(this.relevant==null)
		{
			// Determine affecting events and elements from expression node.
			// Calls addEventType for relevant event types.
			// Use HashSet for fast contains().
			this.relevant = new MultiCollection(SCollection.createHashMap(), HashSet.class);

			IMRelevantElement[]	rels	= getRelevantBeliefs();
			for(int i=0; i<rels.length; i++)
			{
				String	ref	= rels[i].getReference();
				String	type	= rels[i].getEventType();
				addEventType(type, ref, BELIEF_REF);
			}

			rels	= getRelevantBeliefSets();
			for(int i=0; i<rels.length; i++)
			{
				String	ref	= rels[i].getReference();
				String	type	= rels[i].getEventType();
				addEventType(type, ref, BELIEFSET_REF);
			}

			rels	= getRelevantGoals();
			for(int i=0; i<rels.length; i++)
			{
				String	ref	= rels[i].getReference();
				String	type	= rels[i].getEventType();
				addEventType(type, ref, GOAL_REF);
			}

			rels	= getRelevantParameters();
			for(int i=0; i<rels.length; i++)
			{
				String	ref	= rels[i].getReference();
				String	type	= rels[i].getEventType();
				addEventType(type, ref, PARAMETER_REF);
			}

			rels	= getRelevantParameterSets();
			for(int i=0; i<rels.length; i++)
			{
				String	ref	= rels[i].getReference();
				String	type	= rels[i].getEventType();
				addEventType(type, ref, PARAMETERSET_REF);
			}

			// Determine affecting events and elements from expression node.
			// Calls addEventType for relevant event types.
			determineEvents();
		}
		return relevant;
	}

	/**
	 *  Get the term.
	 *  @return The term.
	 */
	public ITerm	getTerm()
	{
		if(term==null && exptext!=null && exptext.length()>0)
		{
			List exparams = getSystemExpressionParameters();
			IMExpressionParameter[] exps = getExpressionParameters();
			for(int i=0; i<exps.length; i++)
			{
				exparams.add(new ExpressionParameterInfo(exps[i].getName(), null, exps[i].getClazz()));
			}
			try
			{
				term = getScope().getParser().parseExpression(getExpressionText(), exparams);
			}
			catch(ParserException e)
			{
				// Calculate error location.
				SourceLocation loc = e.getSourceLocation();
				SourceLocation myloc = getSourceLocation();
				if(loc!=null && myloc!=null)
				{
					int ln = myloc.getLineNumber()+loc.getLineNumber();
					int cn = myloc.getColumnNumber()+loc.getColumnNumber();
					loc.setLineNumber(ln);
					loc.setColumnNumber(cn);
				}
				throw e;
			}
			
			if(isChecking())
			{
				// Hack!!! Reparses term on every access.
				return term;
			}
			else
			{
				this.term	= term;
			}
		}
		return term;
	}

	/**
	 *  Get the value, if possible to evaluate in model.
	 *  @param params The parameters.
	 *  @return The value.
	 * /
	public Object	getValue(Map params)	throws Exception
	{
		return getTerm().getValue(params);
	}*/

	/**
	 *  Get the static type.
	 *  If no information about the return type of an expression
	 *  is available (e.g. because it depends on the evaluation context),
	 *  the static type is Object.
	 *  @return The static type.
	 * /
	public Class	getStaticType()
	{
		return getTerm().getStaticType();
	}*/

	//-------- class --------

	/**
	 *  Get the class of the values.
	 *  @return	The class of the values.
	 */
	public Class	getClazz()
	{
		if(clazz==null && getClassname()!=null)
		{
			//clazz = getScope().getParser().parseType(getClassname());
			clazz = SReflect.findClass0(getClassname(), getScope().getFullImports());
		}
		return clazz;
	}

	/**
	 *  Set the class.
	 *  @param clazz The clazz.
	 */
	public void setClazz(Class clazz)
	{
		this.clazz = clazz;
		setClassname(getClassname()!=null ? clazz.getName() : null);
	}

	//-------- other methods --------

	/**
	 *  Create a string representation.
	 *  @return	The string representation.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(expression = ");
		sb.append(exptext);
		//sb.append(", affected = ");
		//sb.append(getRelevantList());
		sb.append(")");
		return sb.toString();
	}

	//-------- helper methods for jibx (avoiding creation of empty ArrayLists)--------

	/**
	 *  Add an expression parameter.
	 *  @param exp The expression parameter.
	 */
	public void addExpressionParameter(IMExpressionParameter exp)
	{
		if(expressionparameters==null)
			expressionparameters = SCollection.createArrayList();
		expressionparameters.add(exp);
	}

	/**
	 *  Get an iterator for relevant beliefs.
	 *  @return An iterator for relevant beliefs.
	 */
	public Iterator iterExpressionParameters()
	{
		return expressionparameters==null? Collections.EMPTY_LIST.iterator(): expressionparameters.iterator();
	}

	/** Static constant that is returned when no elements. */
	protected static IMRelevantElement[] EMPTY_RELSET = new IMRelevantElement[0];

	/**
	 *  Add a relevant beliefs.
	 *  @param rel The relevant beliefs.
	 */
	public void addRelevantBelief(IMRelevantElement rel)
	{
		if(relevantbeliefs==null)
			relevantbeliefs = SCollection.createArrayList();
		relevantbeliefs.add(rel);
	}

	/**
	 *  Get an iterator for relevant beliefs.
	 *  @return An iterator for relevant beliefs.
	 */
	public Iterator iterRelevantBeliefs()
	{
		return relevantbeliefs==null? Collections.EMPTY_LIST.iterator(): relevantbeliefs.iterator();
	}


	/**
	 *  Add a relevant belief sets.
	 *  @param rel The relevant belief sets.
	 */
	public void addRelevantBeliefSet(IMRelevantElement rel)
	{
		if(relevantbeliefsets==null)
			relevantbeliefsets = SCollection.createArrayList();
		relevantbeliefsets.add(rel);
	}

	/**
	 *  Get an iterator for relevant belief sets.
	 *  @return An iterator for relevant belief sets.
	 */
	public Iterator iterRelevantBeliefSets()
	{
		return relevantbeliefsets==null? Collections.EMPTY_LIST.iterator(): relevantbeliefsets.iterator();
	}


	/**
	 *  Add a relevant goal.
	 *  @param rel The relevant goal.
	 */
	public void addRelevantGoal(IMRelevantElement rel)
	{
		if(relevantgoals==null)
			relevantgoals = SCollection.createArrayList();
		relevantgoals.add(rel);
	}

	/**
	 *  Get an iterator for relevant goal .
	 *  @return An iterator for relevant goal .
	 */
	public Iterator iterRelevantGoals()
	{
		return relevantgoals==null? Collections.EMPTY_LIST.iterator(): relevantgoals.iterator();
	}


	/**
	 *  Add a relevant parameter.
	 *  @param rel The relevant parameter.
	 */
	public void addRelevantParameter(IMRelevantElement rel)
	{
		if(relevantparameters==null)
			relevantparameters = SCollection.createArrayList();
		relevantparameters.add(rel);
	}

	/**
	 *  Get an iterator for relevant parameter .
	 *  @return An iterator for relevant parameter .
	 */
	public Iterator iterRelevantParameters()
	{
		return relevantparameters==null? Collections.EMPTY_LIST.iterator(): relevantparameters.iterator();
	}


	/**
	 *  Add a relevant parameter sets.
	 *  @param rel The relevant parameter sets.
	 */
	public void addRelevantParameterSet(IMRelevantElement rel)
	{
		if(relevantparametersets==null)
			relevantparametersets = SCollection.createArrayList();
		relevantparametersets.add(rel);
	}

	/**
	 *  Get an iterator for relevant parameter sets.
	 *  @return An iterator for relevant parameter sets.
	 */
	public Iterator iterRelevantParameterSets()
	{
		return relevantparametersets==null? Collections.EMPTY_LIST.iterator(): relevantparametersets.iterator();
	}

	//-------- helper methods --------

	/**
	 *  Init the assign to elements.
	 *  Internal expressions cannot be referenced.
	 */
	protected void initAssignToElements()
	{
		assert getOwner()!=null: getExpressionText();

		// Hack!!! Internal expressions are not owned by a base.
		if(getOwner() instanceof IMBase)
		{
			super.initAssignToElements();
		}
	}

	/**
	 *  Auto-determine the events and elements affecting this expression.
	 */
	protected void	determineEvents()
	{
		// Retrieve the relevant event types from the parsed expression.
		List rels = getTerm().getRelevantEventtypes();

		for(int i=0; i<rels.size(); i++)
		{
			String[] tmp = (String[])rels.get(i);
			if(tmp[0].equals("$beliefbase"))
			{
				IMReferenceableElement elm = getScope().getBeliefbase().getReferenceableElement(tmp[1]);
				if(elm instanceof IMBelief || elm instanceof IMBeliefReference)
				{
					addEventType(null, elm.getName(), BELIEF_REF);
				}
				else if(elm!=null)
				{
					addEventType(null, elm.getName(), BELIEFSET_REF);
				}
				else
				{
					assert false : tmp[0]+" "+tmp[1];
				}
			}
			else if(tmp[0].equals("$goalbase"))
			{
				if(tmp[1]==null)
				{
					addEventType(ISystemEventTypes.GOAL_EVENT, ANY_ELEMENT, null);
				}
				else
				{
					addEventType(null, tmp[1], GOAL_REF);
				}
			}
			else if(tmp[0].equals("$goal"))
			{
				if(getOwner() instanceof IMGoal)
				{
					if(((IMGoal)getOwner()).getParameter(tmp[1])!=null)
					{
						addEventType(null, tmp[1], PARAMETER_REF);
					}
					else if(((IMGoal)getOwner()).getParameterSet(tmp[1])!=null)
					{
						addEventType(null, tmp[1], PARAMETERSET_REF);
					}
					else
					{
						assert false : tmp[0]+" "+tmp[1];
					}
				}
				else if(getOwner() instanceof IMGoalReference)
				{
					if(((IMGoalReference)getOwner()).getParameterReference(tmp[1])!=null)
					{
						addEventType(null, tmp[1], PARAMETER_REF);
					}
					else if(((IMGoalReference)getOwner()).getParameterSetReference(tmp[1])!=null)
					{
						addEventType(null, tmp[1], PARAMETERSET_REF);
					}
					else
					{
						assert false : tmp[0]+" "+tmp[1];
					}
				}
			}
			else if(tmp[0].equals("$plan"))
			{
				if(getOwner() instanceof IMPlan)
				{
					if(((IMPlan)getOwner()).getParameter(tmp[1])!=null)
					{
						addEventType(null, tmp[1], PARAMETER_REF);
					}
					else if(((IMPlan)getOwner()).getParameterSet(tmp[1])!=null)
					{
						addEventType(null, tmp[1], PARAMETERSET_REF);
					}
					else
					{
						assert false : tmp[0]+" "+tmp[1];
					}
				}
			}
		}

		// Determine semantic dependencies.
		if(this instanceof IMBindingCondition)
		{
			IMParameter[]	bindings;
			IMElement	owner	= getOwner();
			while(!(owner instanceof IMParameterElement) && owner!=null)
				owner	= owner.getOwner();
			assert owner!=null : this+", "+this.getOwner();
			bindings = ((IMParameterElement)owner).getBindingParameters();

			if(bindings.length>0)
			{
				for(int i=0; i<bindings.length; i++)
				{
					//addEventType(null, bindings[i].getName(), BINDING_REF);
					addEventType(ISystemEventTypes.BINDING_EVENT, bindings[i].getName(), PARAMETER_REF);
				}
			}
		}
	}

	/**
	 *  Add a name, eventtype event combination to the list of relevant events.
	 *  @param eventtype	The change event eventtype (expression).
	 *  @param modelref	The model element name (or null for any).
	 *  @param reftype The reference type e.g. BELIEF_REF or GOAL_REF.
	 */
	protected void	addEventType(String eventtype, String modelref, String reftype)
	{
		Object rel = findRelevantElement(modelref, reftype);
		if(rel==null)
			rel	= ANY_ELEMENT;

		if(BELIEF_REF.equals(reftype))
		{
			if(eventtype==null)
				eventtype	= ISystemEventTypes.FACT_CHANGED;
		}
		else if(BELIEFSET_REF.equals(reftype))
		{
			if(eventtype==null)
				eventtype	= ISystemEventTypes.BSFACTS_CHANGED;
		}
		else if(GOAL_REF.equals(reftype))
		{
			if(eventtype==null)
				eventtype	= ISystemEventTypes.GOAL_EVENT;
		}
		else if(PARAMETER_REF.equals(reftype))
		{
			if(eventtype==null)
				eventtype	= ISystemEventTypes.VALUE_CHANGED;
		}
		else if(PARAMETERSET_REF.equals(reftype))
		{
			if(eventtype==null)
				eventtype	= ISystemEventTypes.ESVALUES_CHANGED;
		}

		//System.out.println("MEx: "+getExpressionText()+" "+rel+" "+eventtype);
		Collection	types	= ISystemEventTypes.Subtypes.getSubtypes(eventtype);
		for(Iterator itypes=types.iterator(); itypes.hasNext(); )
			this.relevant.put(rel, itypes.next());
	}

	/**
	 *  Find a relvant element.
	 *  @param modelref	The model element name (or null for any).
	 *  @param reftype The reference type e.g. BELIEF_REF or GOAL_REF.
	 */
	protected IMReferenceableElement	findRelevantElement(String modelref, String reftype)
	{
		IMReferenceableElement	refelem	= null;

		if(BELIEF_REF.equals(reftype))
		{
			refelem	= getScope().getBeliefbase().getReferenceableElement(modelref);
			if(!(refelem instanceof IMBelief || refelem instanceof IMBeliefReference))
				refelem	= null;
		}
		else if(BELIEFSET_REF.equals(reftype))
		{
			refelem	= getScope().getBeliefbase().getReferenceableElement(modelref);
			if(!(refelem instanceof IMBeliefSet || refelem instanceof IMBeliefSetReference))
				refelem	= null;
		}
		else if(GOAL_REF.equals(reftype))
		{
			refelem = getScope().getGoalbase().getReferenceableElement(modelref);
		}
		else if(PARAMETER_REF.equals(reftype) || PARAMETERSET_REF.equals(reftype))
		{
			IMElement	pe = null;
			String paramref = modelref;
			if(modelref.indexOf(".")!=-1)
			{
				String peref = modelref.substring(0, modelref.lastIndexOf("."));
				paramref = modelref.substring(modelref.lastIndexOf(".")+1);
				//System.out.println("Searching: "+peref+" "+paramref);
				pe = getScope().getGoalbase().getReferenceableElement(peref);
				if(pe==null)
					pe = getScope().getPlanbase().getReferenceableElement(peref);
				if(pe==null)
					pe = getScope().getEventbase().getReferenceableElement(peref);
				// todo: check for uniqueness
			}
			else
			{
				if(getOwner() instanceof IMParameterElement || getOwner() instanceof IMParameterElementReference)
				{
					pe = getOwner();
				}
				else if(getOwner() instanceof IMPlanTrigger)
				{
					pe = getOwner().getOwner();
				}
			}

			if(pe!=null)
			{
				if(PARAMETER_REF.equals(reftype))
				{
					refelem	= pe instanceof IMParameterElement
						? (IMReferenceableElement)((IMParameterElement)pe).getParameter(paramref)
						: (IMReferenceableElement)((IMParameterElementReference)pe).getParameterReference(paramref);
				}
				else //if(PARAMETERSET_REF.equals(reftype))
				{
					refelem	= pe instanceof IMParameterElement
						? (IMReferenceableElement)((IMParameterElement)pe).getParameterSet(paramref)
						: (IMReferenceableElement)((IMParameterElementReference)pe).getParameterSetReference(paramref);
				}
			}
		}

		return refelem;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MExpression clone = (MExpression)cl;

		this.relevant = null;
		if(expressionparameters!=null)
		{
			clone.expressionparameters = SCollection.createArrayList();
			for(int i=0; i<expressionparameters.size(); i++)
				clone.expressionparameters.add(((MElement)expressionparameters.get(i)).clone());
		}
		if(relevantbeliefs!=null)
		{
			clone.relevantbeliefs = SCollection.createArrayList();
			for(int i=0; i<relevantbeliefs.size(); i++)
				clone.relevantbeliefs.add(((MElement)relevantbeliefs.get(i)).clone());
		}
		if(relevantbeliefsets!=null)
		{
			clone.relevantbeliefsets = SCollection.createArrayList();
			for(int i=0; i<relevantbeliefsets.size(); i++)
				clone.relevantbeliefsets.add(((MElement)relevantbeliefsets.get(i)).clone());
		}
		if(relevantgoals!=null)
		{
			clone.relevantgoals = SCollection.createArrayList();
			for(int i=0; i<relevantgoals.size(); i++)
				clone.relevantgoals.add(((MElement)relevantgoals.get(i)).clone());
		}
		if(relevantparameters!=null)
		{
			clone.relevantparameters = SCollection.createArrayList();
			for(int i=0; i<relevantparameters.size(); i++)
				clone.relevantparameters.add(((MElement)relevantparameters.get(i)).clone());
		}
		if(relevantparametersets!=null)
		{
			clone.relevantparametersets = SCollection.createArrayList();
			for(int i=0; i<relevantparametersets.size(); i++)
				clone.relevantparametersets.add(((MElement)relevantparametersets.get(i)).clone());
		}

		// ITerm not cloned, assume all implementations are immutable.
	}
}
