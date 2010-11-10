package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  The parameter element reference.
 */
public abstract class MParameterElementReference extends MElementReference implements IMParameterElementReference
{
	//-------- xml attributes --------

	/** The parameter references. */
	private ArrayList parameterrefs;

	/** The parameter set references. */
	private ArrayList parametersetrefs;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		// todo: Hack!!! Create parameter references in correspondence
		// to the defined parameters of the original parameter element.
		if(!isAbstract())
		{
			IMReferenceableElement orig = getReferencedElement();
			assert orig instanceof IMParameterElement || orig instanceof IMParameterElementReference : this+", "+orig;
	
			if(orig instanceof IMParameterElement)
			{
				IMParameter[]	params = ((IMParameterElement)orig).getParameters();
				for(int i=0; i<params.length; i++)
				{
					createParameterReference(params[i].getName(), params[i].getClazz());
				}
	
				IMParameterSet[]	paramsets = ((IMParameterElement)orig).getParameterSets();
				for(int i=0; i<paramsets.length; i++)
				{
					createParameterSetReference(paramsets[i].getName(), paramsets[i].getClazz());
				}
			}
			else	// orig instanceof IMParameterElementReference
			{
				IMParameterReference[]	params = ((IMParameterElementReference)orig).getParameterReferences();
				for(int i=0; i<params.length; i++)
				{
					createParameterReference(params[i].getName(), params[i].getClazz());
				}
	
				IMParameterSetReference[]	paramsets = ((IMParameterElementReference)orig).getParameterSetReferences();
				for(int i=0; i<paramsets.length; i++)
				{
					createParameterSetReference(paramsets[i].getName(), paramsets[i].getClazz());
				}
			}
		}
	}

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(parameterrefs!=null)
			ret.addAll(parameterrefs);
		if(parametersetrefs!=null)
			ret.addAll(parametersetrefs);
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
		if(parameterrefs!=null && parametersetrefs!=null)
		{
			// Have to manually create array of type IMElement[].
			elements	= (IMElement[])SUtil.joinArrays(parameterrefs.toArray(new IMElement[parameterrefs.size()]), getParameterSetReferences());
		}
		else if(parameterrefs!=null)
		{
			elements	= getParameterReferences();
		}
		else if(parametersetrefs!=null)
		{
			elements	= getParameterSetReferences();
		}
		
		if(elements!=null)
		{
			MBase.checkNameUniqueness(this, report, elements);
		}
	}

	//-------- parameter references --------

	/**
	 *  Get all parameter references.
	 *  @return All parameter references.
	 */
	public IMParameterReference[]	getParameterReferences()
	{
		if(parameterrefs==null)
			return new IMParameterReference[0];
		return (IMParameterReference[])parameterrefs
			.toArray(new IMParameterReference[parameterrefs.size()]);
	}

	/**
	 *  Get a parameter reference by name.
	 *  @param name The parameter reference name.
	 *  @return The parameter reference.
	 */
	// Todo: replace by getReference(original)
	public IMParameterReference	getParameterReference(String name)
	{
		IMParameterReference ret = null;
		for(int i=0; parameterrefs!=null && i<parameterrefs.size() && ret==null; i++)
		{
			IMParameterReference tmp = (IMParameterReference)parameterrefs.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new parameter reference.
	 *  @param ref	The name of the referenced parameter.
	 *  @param clazz	The class for values.
	 *  @return	The newly created parameter.
	 */
	public IMParameterReference	createParameterReference(String ref, Class clazz)
	{
		if(parameterrefs==null)
			parameterrefs = SCollection.createArrayList();

		MParameterReference ret = new MParameterReference();
		ret.setName(ref);	// Hack?
		ret.setReference(ref);
		ret.setClazz(clazz);
		ret.setOwner(this);
		ret.init();
		parameterrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter reference.
	 *  @param parameter	The parameter reference to delete.
	 */
	public void	deleteParameterReference(IMParameterReference parameter)
	{
		if(!parameterrefs.remove(parameter))
			throw new RuntimeException("Parameter reference not found: "+parameter);
	}

	//-------- parameter set references --------

	/**
	 *  Get all parameter set references.
	 *  @return All parameter set references.
	 */
	public IMParameterSetReference[]	getParameterSetReferences()
	{
		if(parametersetrefs==null)
			parametersetrefs = SCollection.createArrayList();

		return (IMParameterSetReference[])parametersetrefs
			.toArray(new IMParameterSetReference[parametersetrefs.size()]);
	}

	/**
	 *  Get a parameter by name.
	 *  @param name The parameter name.
	 *  @return The parameter expression.
	 */
	// Todo: replace by getReference(original)
	public IMParameterSetReference	getParameterSetReference(String name)
	{
		IMParameterSetReference ret = null;
		for(int i=0; parametersetrefs!=null && i<parametersetrefs.size() && ret==null; i++)
		{
			IMParameterSetReference tmp = (IMParameterSetReference)parametersetrefs.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new parameter set reference.
	 *  @param ref	The name of the referenced parameter set.
	 *  @param clazz	The class for values.
	 *  @return	The newly created parameter set reference.
	 */
	public IMParameterSetReference	createParameterSetReference(String ref, Class clazz)
	{
		if(parametersetrefs==null)
			parametersetrefs = SCollection.createArrayList();

		MParameterSetReference ret = new MParameterSetReference();
		ret.setName(ref);	// Hack?
		ret.setReference(ref);
		ret.setClazz(clazz);
		ret.setOwner(this);
		ret.init();
		parametersetrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter set reference.
	 *  @param ref	The parameter set reference to delete.
	 */
	public void	deleteParameterSetReference(IMParameterSetReference ref)
	{
		if(!parametersetrefs.remove(ref))
			throw new RuntimeException("Parameterset reference not found: "+ref);
	}

	//-------- jibx related --------

	/**
	 *  Add a parameterref.
	 *  @param parameterref The parameterrefs.
	 */
	public void addParameterReference(MParameterReference parameterref)
	{
		if(parameterrefs==null)
			parameterrefs = SCollection.createArrayList();
		parameterrefs.add(parameterref);
	}

	/**
	 *  Get an iterator for all parameterrefss.
	 *  @return The iterator.
	 */
	public Iterator iterParameterReferences()
	{
		return parameterrefs==null? Collections.EMPTY_LIST.iterator(): parameterrefs.iterator();
	}

	/**
	 *  Add a parametersetref.
	 *  @param parametersetref The parametersetrefs.
	 */
	public void addParameterSetReference(MParameterSetReference parametersetref)
	{
		if(parametersetrefs==null)
			parametersetrefs = SCollection.createArrayList();
		parametersetrefs.add(parametersetref);
	}

	/**
	 *  Get an iterator for all parametersetrefss.
	 *  @return The iterator.
	 */
	public Iterator iterParameterSetReferences()
	{
		return parametersetrefs==null? Collections.EMPTY_LIST.iterator(): parametersetrefs.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MParameterElementReference clone = (MParameterElementReference)cl;
		if(parameterrefs!=null)
		{
			clone.parameterrefs = SCollection.createArrayList();
			for(int i=0; i<parameterrefs.size(); i++)
				clone.parameterrefs.add(((MElement)parameterrefs.get(i)).clone());
		}
		if(parametersetrefs!=null)
		{
			clone.parametersetrefs = SCollection.createArrayList();
			for(int i=0; i<parametersetrefs.size(); i++)
				clone.parametersetrefs.add(((MElement)parametersetrefs.get(i)).clone());
		}
	}
}
