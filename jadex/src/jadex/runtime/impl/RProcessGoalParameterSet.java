package jadex.runtime.impl;

import jadex.model.IMReferenceableElement;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 *  A process goal parameter set redirects accessor calls
 *  to the original goal parameter set until it is explicitly modified.
 */
public class RProcessGoalParameterSet extends RReferenceableElement implements IRParameterSet
{
	//-------- attributes --------

	/** The parameter set of the original goal. */
	protected IRParameterSet	paramset;
	
	/** Flag indicating that the parameter is modified. */
	protected boolean	modified;
	
	/** The cached values. */
	protected List	values;
	
	//-------- constructors --------
	
	/**
	 *  Create a new RProcessGoalParameterSet.
	 *  @param paramset The parameter set.
	 *  @param owner The owner
	 */
	protected RProcessGoalParameterSet(IRParameterSet paramset, RElement owner)
	{
		super(paramset.getName(), (IMReferenceableElement)paramset.getModelElement(), null, owner, null, null);

		this.paramset	= paramset;
		this.modified	= false;
	}

	//-------- IRParameterSet interface --------
	
	/**
	 *  Add a value to the typed element.
	 *  @param value The new value.
	 */
	public void addValue(Object value)
	{
		modified();
		values.add(value);
	}

	/**
	 *  Remove a value from a typed element
	 *  @param value The new value.
	 */
	public void removeValue(Object value)
	{
		modified();
		values.remove(value);
	}

	/**
	 *  Add values to a parameter set.
	 */
	public void addValues(Object[] values)
	{
		modified();
		for(int i=0; i<values.length; i++)
			this.values.add(values[i]);
	}

	/**
	 *  Remove all values from a typed element.
	 */
	public void removeValues()
	{
		modified();
		values.clear();
	}

	/**
	 *  Update a value to a new value. Searches the old
	 *  value with equals, removes it and stores the new value.
	 *  @param newvalue The new value.
	 */
	public void updateValue(Object newvalue)
	{
		modified();
		if(values.remove(newvalue))
		{
			values.add(newvalue);
		}
		else
		{
			throw new RuntimeException("Value could not be updated, value not found: "+newvalue);			
		}
	}

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(Object oldval)
	{
		if(modified)
		{
			int	index	= values.indexOf(oldval);
			if(index!=-1)
				return values.get(index);
			else
				throw new RuntimeException("Value cannot be retrieved, old value not found: "+oldval);
		}
		else
			return paramset.getValue(oldval);
	}

	/**
	 *  Test if a value is contained in a parameter.
	 *  @param value The value to test.
	 *  @return True, if value is contained.
	 */
	public boolean containsValue(Object value)
	{
		if(modified)
			return values.contains(value);
		else
			return paramset.containsValue(value);
	}

	/**
	 *  Get the values of a parameterset.
	 *  @return The values.
	 */
	public Object[]	getValues()
	{
		if(modified)
			return values.toArray((Object[])Array.newInstance(SReflect.getWrappedType(getClazz()), values.size()));
		else
			return paramset.getValues();
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		if(modified)
			return values.size();
		else
			return paramset.size();
	}

	/**
	 *  Was the typed element modified by setting a value.
	 *  @return True, if modified.
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 *  Get the value class.
	 *  Shortcut for getModelElement().getClazz().
	 *  @return The value class.
	 */
	public Class	getClazz()
	{
		return paramset.getClazz();
	}

	//-------- parameter protection methods --------
	
	/**
	 *  Check if this paramter can be accessed for read access.
	 */
	public void checkReadAccess()
	{
		// todo: ?		
	}

	/**
	 *  Check if this paramter can be accessed for write access.
	 */
	public void checkWriteAccess()
	{
		// todo: ?		
	}

	/**
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	public void	setInitialValues()
	{
		// Do nothing, as initial values are already stored in proprietary goal.
	}
	
	//-------- helper methods --------

	/**
	 *  Called when the parameter set is modified.
	 *  Sets the modified flag and copies original value, if necessary.
	 */
	protected void	modified()
	{
		if(!modified)
		{
			modified	= true;
			// todo: deep copy
			// todo: what about registered values? (beans/expressions etc.)
			// todo: Hack!!! Remove direct field access.
			ArrayList	ovals	= ((RParameterSet)paramset.getOriginalElement()).values;
			if(ovals!=null)
			{
				this.values = (ArrayList)ovals.clone();
			}
			else
			{
				this.values	= SCollection.createArrayList();
			}
		}
	}

	/**
	 *  Copy back the state to the original parameter.
	 */
	protected void copyContent()
	{
		if(modified)
		{
			// Remove original values and add new (hack???).
			paramset.removeValues();
			Object[]	values	= getValues();
			for(int i=0; i<values.length; i++)
			{
				paramset.addValue(values[i]);
			}
		}
	}
}
