package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  The abstract base class for all elements with parameters.
 */
public abstract class MParameterElement extends MReferenceableElement implements IMParameterElement
{
	//-------- xml attributes --------

	/** The parameters. */
	private ArrayList parameters;

	/** The parameter sets. */
	private ArrayList parametersets;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 * /
	protected void init()
	{
		super.init();

		// todo: Hack!!! Create parameter references in correspondence
		// to the defined parameters of this parameter element.
		IMParameter[]	params	= getParameters();
		IMParameterSet[]	paramsets	= getParameterSets();
		IMElementReference[] refs = getAssignToElements();

		for(int r=0; r<refs.length; r++)
		{
			IMParameterElementReference	ref	= (IMParameterElementReference)refs[r];

			for(int i=0; i<params.length; i++)
			{
				ref.createParameterReference(params[i].getName(), params[i].getClazz());
			}

			for(int i=0; i<paramsets.length; i++)
			{
				ref.createParameterSetReference(paramsets[i].getName(), paramsets[i].getClazz());
			}
		}
	}*/

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
		
		// Check if names of parameter(set)s are unique.
		IMElement[]	elements	= null;
		if(parameters!=null && parametersets!=null)
		{
			// Have to manually create array of type IMElement[].
			elements	= (IMElement[])SUtil.joinArrays(parameters.toArray(new IMElement[parameters.size()]), getParameterSets());
		}
		else if(parameters!=null)
		{
			elements	= getParameters();
		}
		else if(parametersets!=null)
		{
			elements	= getParameterSets();
		}
		
		if(elements!=null)
		{
			MBase.checkNameUniqueness(this, report, elements);
		}
	}

	//-------- parameters --------

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IMParameter[]	getParameters()
	{
		if(parameters==null)
			return new IMParameter[0];
		return (IMParameter[])parameters.toArray(new IMParameter[parameters.size()]);
	}

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter expression.
	 */
	public IMParameter	getParameter(String name)
	{
		IMParameter ret = null;
		for(int i=0; parameters!=null && i<parameters.size(); i++)
		{
			IMParameter tmp = (IMParameter)parameters.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new parameter.
	 *  @param name	The name of the parameter.
	 *  @param clazz	The class for values.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default value expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created parameter.
	 */
	public IMParameter	createParameter(String name, Class clazz, String direction, long updaterate, String expression, String mode)
	{
		if(parameters==null)
			parameters = SCollection.createArrayList();

		MParameter ret = new MParameter();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setDirection(direction==null? IMParameter.DIRECTION_IN: direction);
		ret.setUpdateRate(updaterate);
		if(expression!=null)
			ret.createDefaultValue(expression, mode);
		ret.setOwner(this);
		ret.init();
		parameters.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 */
	public void	deleteParameter(IMParameter parameter)
	{
		if(!parameters.remove(parameter))
			throw new RuntimeException("Parameter not found: "+parameter);
	}


	//-------- parameter sets --------

	/**
	 *  Get all parameter sets.
	 *  @return An array of the parameter sets.
	 */
	public IMParameterSet[]	getParameterSets()
	{
		if(parametersets==null)
			return new IMParameterSet[0];
		return (IMParameterSet[])parametersets.toArray(new IMParameterSet[parametersets.size()]);
	}

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter expression.
	 */
	public IMParameterSet	getParameterSet(String name)
	{
		IMParameterSet ret = null;
		for(int i=0; parametersets!=null && i<parametersets.size(); i++)
		{
			IMParameterSet tmp = (IMParameterSet)parametersets.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new parameter set.
	 *  @param name	The name of the  parameter set.
	 *  @param clazz	The class for values.
	 *  @param direction	The direction (in/out).
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param expression	The default values expression (if any).
	 *  @param mode	The evaluation mode.
	 *  @return	The newly created parameter set.
	 */
	public IMParameterSet	createParameterSet(String name, Class clazz, String direction, long updaterate, String expression, String mode)
	{
		if(parametersets==null)
			parametersets = SCollection.createArrayList();

		MParameterSet ret = new MParameterSet();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setDirection(direction==null? IMParameterSet.DIRECTION_IN: direction);
		ret.setUpdateRate(updaterate);
		if(expression!=null)
			ret.createDefaultValuesExpression(expression, mode);
		ret.setOwner(this);
		ret.init();
		parametersets.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter set.
	 *  @param parameterset	The parameter set to delete.
	 */
	public void	deleteParameterSet(IMParameterSet parameterset)
	{
		if(!parametersets.remove(parameterset))
			throw new RuntimeException("Parameter set could not be found: "+parameterset);
	}

	//-------- jibx helper --------

	/**
	 *  Add a parameter element.
	 *  @param elem The parameter element.
	 */
	public void addParameter(IMParameter elem)
	{
		if(parameters==null)
			parameters = SCollection.createArrayList();
		parameters.add(elem);
	}

	/**
	 *  Get an iterator for assign to names.
	 *  @return An iterator for all assign to names.
	 */
	public Iterator iterParameters()
	{
		return parameters==null? Collections.EMPTY_LIST.iterator(): parameters.iterator();
	}

	/**
	 *  Add a parameter set.
	 *  @param elem The parameter set.
	 */
	public void addParameterSet(IMParameterSet elem)
	{
		if(parametersets==null)
			parametersets = SCollection.createArrayList();
		parametersets.add(elem);
	}

	/**
	 *  Get an iterator for parameter sets.
	 *  @return An iterator for all parameter sets.
	 */
	public Iterator iterParameterSets()
	{
		return parametersets==null? Collections.EMPTY_LIST.iterator(): parametersets.iterator();
	}

	//-------- not xml related --------

	/**
	 *  Get the bindings.
	 *  @return The binding parameters.
	 */
	public IMParameter[] getBindingParameters()
	{
		List ret = SCollection.createArrayList();
		for(int i=0; parameters!=null && i<parameters.size(); i++)
		{
			IMParameter param = (IMParameter)parameters.get(i);
			if(param.getBindingOptions()!=null)
				ret.add(param);
		}
		return (IMParameter[])ret.toArray(new IMParameter[ret.size()]);
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MParameterElement clone = (MParameterElement)cl;
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
