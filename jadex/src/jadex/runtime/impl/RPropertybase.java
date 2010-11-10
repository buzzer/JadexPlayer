package jadex.runtime.impl;

import java.util.*;
import jadex.model.*;
import jadex.util.collection.*;

/**
 *  The base for properties.
 */
public class RPropertybase extends RBase
{
	//-------- attributes --------

	/** The properties. */
	protected IndexMap props;

	//-------- constructors --------

	/**
	 *  Create a new property base instance.
	 *  @param model	The property base model.
	 *  @param owner	The owner of the instance.
	 */
	protected RPropertybase(IMPropertybase model, RElement owner)
	{
		super(null, model, owner);
		this.props	= SCollection.createIndexMap();
	}

	/**
	 *  Initialize the property base.
	 */
	//protected void	init(int level)
	protected void	constructorInit()
	{
		// On constructor initialization, instantiate properties.
		//if(level==0)
		{
			IMExpression[]	exps	= ((IMPropertybase)getModelElement()).getProperties();
			for(int i=0; i<exps.length; i++)
			{
				Object prop = null;
				try
				{
					prop = getScope().getExpressionbase()
						.evaluateInternalExpression(exps[i], this);
					props.add(exps[i].getName(), prop);
				}
				catch(Exception e)
				{
//					StringWriter	sw	= new StringWriter();
//					e.printStackTrace(new PrintWriter(sw));
//					RCapability.getScope(this).getLogger().severe(RCapability.getScope(this).getAgent().getName()
//						+ ": Agent property could not be parsed: "+names[i]+"\n"+sw);
					//e.printStackTrace();	// Hack!!!
//					getScope().getLogger().warning("Could not evaluate property: "+exps[i]);
					// todo: should use logger but logger not available here
					//e.printStackTrace();
					System.out.println(getScope().getAgentName()+" could not evaluate property: "+exps[i]);
				}
				catch(Error e)
				{
//					StringWriter	sw	= new StringWriter();
//					e.printStackTrace(new PrintWriter(sw));
//					RCapability.getScope(this).getLogger().severe(RCapability.getScope(this).getAgent().getName()
//						+ ": Agent property could not be parsed: "+names[i]+"\n"+sw);
					//e.printStackTrace();	// Hack!!!
//					getScope().getLogger().warning("Could not evaluate property: "+exps[i]);
					// todo: should use logger but logger not available here
					System.out.println(getScope().getAgentName()+" could not evaluate property: "+exps[i]);
				}
			}
		}
	}

	//-------- methods --------

	/**
	 *  Get a property.
	 *  @param name The property name.
	 *  @return The property value.
	 */
	public Object	getProperty(String name)
	{
		Object	value	= props.get(name);
		if(value==null && getScope().getParent()!=null)
			value	= getScope().getParent().getPropertybase().getProperty(name);
		return value;
	}

	/**
	 *  Get all properties that start with a start string.
	 *  Also parent properties are available.
	 *  @param start The start string (null for all).
	 *  @return An array of the matching property names.
	 */
	public String[] getPropertyNames(String start)
	{
		ArrayList ret = SCollection.createArrayList();
		String[] keys = (String[])props.getKeys(String.class);
		for(int i=0; i<keys.length; i++)
		{
			if(start==null || keys[i].startsWith(start))
			{
				ret.add(keys[i]);
			}
		}

		// Possibly add all parents properties.
		String[] pps = null;
		if(getScope().getParent()!=null)
		{
			pps = getScope().getParent().getPropertybase().getPropertyNames(start);
			for(int i=0; i<pps.length; i++)
				ret.add(pps[i]);
		}

		return (String[])ret.toArray(new String[ret.size()]);
	}

	/**
	 *
	 * /
	protected void printAllPropertyNames()
	{
		RPropertybase cur = this;
		while(cur!=null)
		{
			System.out.println(this.getName()+": "+getPropertyNames(null));
			cur = RCapability.getScope(this).getParent().getPropertybase();
		}
	}*/

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
		throw new RuntimeException("Base does not support referenceable elements!: "+getName());
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected RBase	getCorrespondingBase(RCapability scope)
	{
		return scope.getPropertybase();
	}
}
