package jadex.model.jibximpl;

import jadex.model.IMBase;
import jadex.model.IMCapability;
import jadex.model.IMElement;
import jadex.model.IMExpression;
import jadex.model.IMPropertybase;
import jadex.model.IMReferenceableElement;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *  The container of properties.
 */
public class MPropertybase extends MBase implements IMPropertybase
{
	//-------- attributes --------

	/** The properties. */
	protected ArrayList properties = SCollection.createArrayList();

	/** The owner, when loaded from external property file. */
	// Hack???
	protected IMElement	extowner;

	/** Additional property bases (ordered). */
	protected List	externalbases;

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
		IMExpression[] props = getProperties();
		for(int i=0; i<props.length; i++)
			ret.add(props[i]);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		// Check if names of properties are unique.
		// Properties are not referenceable elements, and thus not checked by MBase. 
		MBase.checkNameUniqueness(this, report, (IMElement[])properties.toArray(new IMElement[properties.size()]));

		// As all expressions are checked from the outside,
		// check the expressions as part of the base doCheck().
		for(int i=0; properties!=null && i<properties.size(); i++)
		{
			MExpression	exp	= (MExpression)properties.get(i);
			exp.checkClass(exp.getClazz(), report);
		}
	}

	//-------- methods --------

	/**
	 *  Get all properties.
	 *  @return An array of the properties.
	 */
	public IMExpression[]	getProperties()
	{
		List al = SCollection.createArrayList();
		Set	names	= SCollection.createHashSet();	// Add names to check for duplicates.
		for(Iterator it=properties.iterator(); it.hasNext(); )
		{
			IMExpression p = (IMExpression)it.next();
			al.add(p);
			names.add(p.getName());
		}

		if(externalbases!=null)
		{
			for(int i=0; i<externalbases.size(); i++)
			{
				IMExpression[]	exprops	= ((IMPropertybase)externalbases.get(i)).getProperties();
				for(int j=0; j<exprops.length; j++ )
				{
					if(!names.contains(exprops[j].getName()))
					{
						al.add(exprops[j]);
						names.add(exprops[j].getName());
					}
				}
			}
		}

		return (IMExpression[])al.toArray(new IMExpression[al.size()]);
	}

	/**
	 *  Get a property.
	 *  @param name The property name.
	 *  @return The property expression.
	 */
	public IMExpression	getProperty(String name)
	{
		assert name!=null;

		IMExpression ret = null;
		for(int i=0; i<properties.size(); i++)
		{
			IMExpression test = (IMExpression)properties.get(i);
			if(test.getName().equals(name))
				ret = test;
		}

		if(ret==null)
		{
			if(externalbases!=null)
			{
				for(int i=0; ret==null && i<externalbases.size(); i++)
					ret	= ((IMPropertybase)externalbases.get(i)).getProperty(name);
			}
		}

		return ret;
	}

	/**
	 *  Create a property.
	 *  @param name The property name.
	 *  @param expression	The expression string.
	 *  @return The new property.
	 */
	public IMExpression	createProperty(String name, String expression)
	{
		assert expression!=null : this;
		MExpression ret = new MExpression();
		ret.setName(name);
		ret.setExpressionText(expression);
		ret.setOwner(this);
		ret.init();
		properties.add(ret);
		return ret;
	}

	/**
	 *  Delete the property.
	 */
	public void	deleteProperty(IMExpression property)
	{
		if(!properties.remove(property))
			throw new RuntimeException("Property not found: "+property);
	}


	//-------- not xml related --------

	/**
	 *  Add an external property file.
	 *  @param external The external property file.
	 */
	public void addPropertyBase(IMPropertybase external)
	{
		if(externalbases==null)
			externalbases	= SCollection.createArrayList();
		externalbases.add(external);
	}

	//-------- methods --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		return "propertybase";
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
		return scope.getPropertybase();
	}

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem)
	{
		assert elem!=null;

		deleteProperty((IMExpression)elem);

		// todo: what about properties in other scopes?
	}

	/**
	 *  Get the owner.
	 *  @return The owner.
	 */
	public IMElement	getOwner()
	{
		if(extowner!=null)
		{
			return extowner;
		}
		else
		{
			return super.getOwner();
		}
	}

	/**
	 *  Set the owner.
	 */
	// Hack??? Needed, when loading properties from external file.
	protected void	setOwner(IMElement owner)
	{
		this.extowner	= owner;
	}

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	// Hack!!! Properties are not referenceable?
	public IMReferenceableElement[]	getReferenceableElements()
	{
		return new IMReferenceableElement[0];
	}

	/**
	 *  Add a property.
	 *  @param property The property.
	 */
	public void addProperty(MExpression property)
	{
		if(properties==null)
			properties = SCollection.createArrayList();
		properties.add(property);
	}

	/**
	 *  Get an iterator for all properties.
	 *  @return The iterator.
	 */
	public Iterator iterProperties()
	{
		return properties==null? Collections.EMPTY_LIST.iterator(): properties.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MPropertybase clone = (MPropertybase)cl;
		if(extowner!=null)
			clone.extowner = (IMElement)((MElement)extowner).clone();
		if(properties!=null)
		{
			clone.properties = SCollection.createArrayList();
			for(int i=0; i<properties.size(); i++)
				clone.properties.add(((MElement)properties.get(i)).clone());
		}
		if(externalbases!=null)
		{
			clone.externalbases = SCollection.createArrayList();
			for(int i=0; i<externalbases.size(); i++)
				clone.externalbases.add(((MElement)externalbases.get(i)).clone());
		}
	}
}

