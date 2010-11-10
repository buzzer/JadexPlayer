package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.SCollection;

/**
 *  An config parameter element.
 */
public class MConfigParameterElement extends MConfigReferenceableElement implements IMConfigParameterElement
{
	//-------- xml attributes --------

	/** The parameters. */
	protected ArrayList parameters;

	/** The parameter sets. */
	protected ArrayList parametersets;

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
		if(parameters!=null)
			ret.addAll(parameters);
		if(parametersets!=null)
			ret.addAll(parametersets);
		return ret;
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if initial element with bindings has a name (not allowed).
		if(getName()!=null)
		{
			IMElement	orig	= getOriginalElement();
			MParameterElement	pe	= null;
			if(orig instanceof MParameterElementReference)
			{
				pe	= (MParameterElement)((MParameterElementReference)orig).getOriginalElement();
			}
			else if(orig instanceof MParameterElement)
			{
				pe	= (MParameterElement)orig;
			}
			
			if(pe!=null)
			{
				boolean hasbindings = false;
				IMParameter[] params = pe.getParameters();
				for(int i=0; i<params.length && !hasbindings; i++)
				{
					if(params[i].getBindingOptions()!=null)
						hasbindings = true;
				}
				if(hasbindings)
					report.addEntry(this, "Initial elements with bindings cannot have a predefined name.");
			}
		}
	}

	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., an initialbeliefsetreference can only be a reference to a beliefset(reference),
	 *  and not a belief(reference).
	 */
	protected boolean assignable(IMElement orig)
	{
		return orig instanceof IMParameterElement || orig instanceof IMParameterElementReference;
	}

	//-------- parameters --------

	/**
	 *  Get all initial parameters.
	 *  @return All initial parameters.
	 */
	public IMConfigParameter[]	getParameters()
	{
		if(parameters==null)
			return new IMConfigParameter[0];
		return (IMConfigParameter[])parameters.toArray(new IMConfigParameter[parameters.size()]);
	}

	/**
	 *  Create a new initial parameter.
	 *  @param ref	The name of the referenced parameter.
	 *  @param expression	The value expression.
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created initial parameter.
	 */
	public IMConfigParameter	createInitialParameter(String ref, String expression, String mode)
	{
		if(parameters==null)
			parameters = SCollection.createArrayList();

		MConfigParameter ret = new MConfigParameter();
		ret.setReference(ref);
		if(expression!=null)
			ret.createInitialValue(expression, mode);
		ret.setOwner(this);
		ret.init();
		parameters.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 */
	public void	deleteInitialParameter(IMConfigParameter parameter)
	{
		if(!parameters.remove(parameter))
			throw new RuntimeException("Initial parameter not found: "+parameter);
	}


	//-------- parameter sets --------

	/**
	 *  Get all initial parameter sets.
	 *  @return All initial parameter sets.
	 */
	public IMConfigParameterSet[]	getParameterSets()
	{
		if(parametersets==null)
			return new IMConfigParameterSet[0];
		return (IMConfigParameterSet[])parametersets
			.toArray(new IMConfigParameterSet[parametersets.size()]);
	}

	/**
	 *  Create a new parameter set.
	 *  @param ref	The name of the referenced parameter.
	 *  @param expression	The initial values expression (if any).
	 *  @return	The newly created parameter set.
	 */
	public IMConfigParameterSet	createInitialParameterSet(String ref, String expression)
	{
		if(parametersets==null)
			parametersets = SCollection.createArrayList();

		MConfigParameterSet ret = new MConfigParameterSet();
		ret.setReference(ref);
		if(expression!=null)
			ret.createInitialValue(expression);
		ret.setOwner(this);
		ret.init();
		parametersets.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter set.
	 *  @param parameterset	The parameter set to delete.
	 */
	public void	deleteInitialParameterSet(IMConfigParameterSet parameterset)
	{
		if(!parametersets.remove(parameterset))
			throw new RuntimeException("Initial parameter set not found: "+parameterset);
	}


	//-------- not xml related --------

	/**
	 *  Get a parameter by name.
	 *  @param elem The parameter.
	 *  @return The parameter expression.
	 */
	public IMConfigParameter	getParameter(IMReferenceableElement elem)
	{
		IMConfigParameter ret = null;
		for(int i=0; parameters!=null && i<parameters.size(); i++)
		{
			IMConfigParameter param = (IMConfigParameter)parameters.get(i);
			if(elem == param.getOriginalElement())
				ret = param;
		}
		return ret;
	}

	/**
	 *  Get a parameter by name.
	 *  @param elem The parameter.
	 *  @return The parameter expression.
	 */
	public IMConfigParameterSet	getParameterSet(IMReferenceableElement elem)
	{
		IMConfigParameterSet ret = null;
		for(int i=0; parametersets!=null && i<parametersets.size(); i++)
		{
			IMConfigParameterSet paramset = (IMConfigParameterSet)parametersets.get(i);
			if(elem == paramset.getOriginalElement())
				ret = paramset;
		}
		return ret;
	}

	//-------- jibx related --------

	/**
	 *  Add a parameter.
	 *  @param parameter The parameter.
	 */
	public void addParameter(MConfigParameter parameter)
	{
		if(parameters==null)
			parameters = SCollection.createArrayList();
		parameters.add(parameter);
	}

	/**
	 *  Get an iterator for all parameters.
	 *  @return The iterator.
	 */
	public Iterator iterParameters()
	{
		return parameters==null? Collections.EMPTY_LIST.iterator(): parameters.iterator();
	}

	/**
	 *  Add a parameter set.
	 *  @param parameterset The parameterset.
	 */
	public void addParameterSet(MConfigParameterSet parameterset)
	{
		if(parametersets==null)
			parametersets = SCollection.createArrayList();
		parametersets.add(parameterset);
	}

	/**
	 *  Get an iterator for all parametersets.
	 *  @return The iterator.
	 */
	public Iterator iterParameterSets()
	{
		return parametersets==null? Collections.EMPTY_LIST.iterator(): parametersets.iterator();
	}


	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MConfigParameterElement clone = (MConfigParameterElement)cl;
		if(parameters!=null)
		{
			clone.parameters = SCollection.createArrayList();
			for(int i=0; i<parameters.size(); i++)
				clone.parameters.add(((MElement)parameters.get(i)).clone());
		}
		if(parametersets!=null)
		{
			clone.parametersets = SCollection.createArrayList();
			for(int i=0; i<parametersets.size(); i++)
				clone.parametersets.add(((MElement)parametersets.get(i)).clone());
		}
	}
}
