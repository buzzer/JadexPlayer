package jadex.runtime.impl;

import jadex.model.IMConfigElement;
import jadex.model.IMTypedElement;
import jadex.model.IMTypedElementSet;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.agenda.elementupdate.UpdateSetAction;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ObjectStreamException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


/**
 *  The typed element has (at runtime) a single value
 *  or a set of values. The values may be represented
 *  by expressions, which can be evaluated when needed.
 */
public abstract class RTypedElementSet extends RReferenceableElement implements PropertyChangeListener
{
	//-------- constants --------

	/** The argument types for property change listener adding/removal (cached for speed). */
	protected static Class[]	PCL	= new Class[]{PropertyChangeListener.class};	

	//-------- attributes --------

	/** The values. */
	protected transient ArrayList values;

	/** The values when stored for serialization. */
	protected ArrayList serialized_values;

	/** The initial values expression (can null). */
	protected IRExpression inivals;

	/** The flag indicating that the element is currently initing. */
	protected boolean	initing;

	/** Flag indicating that the parameter is modified. */
	private boolean	modified;

	//-------- constructor --------

	/**
	 *  Create a new typed element.
	 *  To evaluate the initial values, init() has to be called.
	 *  @param typedelement The typed element.
	 *  @param owner The owner.
	 */
	protected RTypedElementSet(String name, IMTypedElementSet typedelement,
			IMConfigElement state, RElement owner, RReferenceableElement creator)
	{
		super(name, typedelement, state, owner, creator, null);
	}

	/**
	 *  Initialize the initial values.
	 */
	protected void	init()
	{
		if(isInited())
			return;
		else if(initing)
			throw new RuntimeException("Recursive self initialization (probably cyclic dependency): "+this);
		this.initing	= true;

		setInitialValues();
		
		// When an update rate is specified,
		// create a self-renewing time-table entry.
		// The associated action updates the belief.
		if(getUpdateRate()>0)
			getScope().getAgent().addTimetableEntry(new TimetableData(getUpdateRate(),
				new UpdateSetAction(this, null)));


		removeConfiguration();
		this.initing	= false;
		super.init(); // Sets inited to true, therefore do at last!
		//this.modified = false; // necessary?
	}

	/**
	 *  Set the initial values.
	 *  May be default values (from model),
	 *  or initiual values provided on creation.
	 */
	protected abstract void	setInitialValues();

	/**
	 *  Set the initial values expression.
	 */
	protected void	setInitialValuesExpression(IRExpression inivals)
	{
		this.inivals = inivals;
		throwSystemEvent(createSystemEvent(SystemEvent.ESVALUES_CHANGED, null, -1));
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		super.cleanup();

		// Cleanup values.
		if(values!=null)
		{
			for(int i=0; i<values.size(); i++)
			{
				deregisterValue(values.get(i));
			}
			// Hack!!! Cannot remove reference to values,
			// because e.g. goal parameters are accessed after cleanup.
			// value	= null;
		}
	}

	//-------- methods --------

	/**
	 *  Add a value to the typed element.
	 *  Only simple Java objects are allowed (no expressions).
	 *  @param value The new value.
	 */
	public void addValue(Object value)
	{
		// Can be called from init, therefore only init when not already initing.
		if(!isInited() && !initing) init();

		// When the element has dynamic initial values add and remove are prohibited.
		if(inivals!=null)
			throw new RuntimeException("Cannot change set with dynamic initial values: "+this);

		// Check the type.
		checkType(value);

		Class type = ((IMTypedElementSet)getModelElement()).getClazz();

		value = SReflect.convertWrappedValue(value, type);

		// Add new property listener
		registerValue(value);

		if(values==null)
			this.values = SCollection.createArrayList();
		values.add(value);

		if(!initing)
			this.modified = true;

		// Throw value added event.
		throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_ADDED, value, values.size()-1));
	}

	/**
	 *  Remove a value from a typed element
	 *  @param value The new value.
	 */
	public void removeValue(Object value)
	{
		if(!isInited()) init();

		// When the element has dynamic initial values add and remove are prohibited.
		if(inivals!=null)
			throw new RuntimeException("Cannot change set with dynamic initial values: "+this);
		
		int index	= indexOf(value);
		if(index!=-1)
		{
			// Remove value and old listener.
			deregisterValue(values.remove(index));

			if(!initing)
				this.modified = true;
			// Throw value removed event.
			//System.out.println("Removed value: "+getName()+" "+value);
			throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_REMOVED, value, index));
		}
		else
		{
			getScope().getAgent().getLogger().warning("No such value in element set: "
				+this+" "+value);
		}
	}

	/**
	 *  Get the index of the given value.
	 *  @return -1 when value is not found.
	 */
	protected int indexOf(Object value)
	{
		return values!=null ? values.indexOf(value) : -1;
	}

	/**
	 *  Set all values for a parameter set.
	 */
	public void addValues(Object[] values)
	{
		for(int i=0; values!=null && i<values.length; i++)
			addValue(values[i]);

		/*if(values==null)
			return;

		// Can be called from init, therefore only init when not already initing.
		if(!isInited() && !initing) init();

		// When the element has dynamic initial values add and remove are prohibited.
		if(inivals!=null)
			throw new RuntimeException("Cannot change set with dynamic initial values: "+this);

		for(int i=0; i<values.length; i++)
		{
			// Check the type.
			checkType(values[i]);

			Class type = ((IMTypedElementSet)getModelElement()).getClazz();

			Object value = SReflect.convertWrappedValue(values[i], type);

			// Add new property listener
			registerValue(value);

			this.values.add(value);

			if(!initing)
				this.modified = true;

			// Throw value added event.
			throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_ADDED, value, this.values.size()-1));
		}*/
	}

	/**
	 *  Remove all values from a typed element.
	 */
	public void removeValues()
	{
		// Can be called from init, therefore only init when not already initing.
		if(!isInited() && !initing) init();

		// When the element has dynamic initial values add and remove are prohibited.
		if(inivals!=null)
			throw new RuntimeException("Cannot change set with dynamic initial values: "+this);

		if(values!=null)
		{
			for(int i=values.size()-1; i>=0; i--)
			{
				removeValue(values.get(i));
			}
		}
		if(!initing)
			this.modified = true;
	}

	/**
	 *  Update a value to a new value. Searches the old
	 *  value with equals, removes it and stores the new fact.
	 *  @param newvalue The newvalue
	 */
	public void updateValue(Object newvalue)
	{
		if(!isInited()) init();

		// When the element has dynamic initial values add and remove are prohibited.
		if(inivals!=null)
			throw new RuntimeException("Cannot change set with dynamic initial values: "+this);

		int index	= indexOf(newvalue);
		if(index!=-1)
		{
			// Remove value and old listener.
			deregisterValue(values.remove(index));

			// Add value and listener.
			values.add(index, newvalue);
			registerValue(newvalue);

			if(!initing)
				this.modified = true;
			// Throw value removed event.
			throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_CHANGED, newvalue, index));
		}
		else
		{
			throw new RuntimeException("Value could not be updated, value not found: "+newvalue);
		}
	}

	/**
	 *  Update or add a value. When the value is already
	 *  contained it will be updated to the new value.
	 *  Otherwise the value will be added.
	 *  @param value The new or changed value
	 * /
	public void updateOrAddValue(Object value)
	{
		if(!inited) init();

		if(values.contains(value))
			updateValue(value);
		else
			addValue(value);
	}*/

	/**
	 *  Replace a value with another one.
	 *  @param oldval The old value.
	 *  @param newval The new value.
	 * /
	public void replaceValue(Object oldval, Object newval)
	{
		if(!inited) init();

		int index = values.indexOf(oldval);

		if(index != -1)
		{
			// Remove value and old listener.
			deregisterValue(values.remove(index));

			// Add new value and listener.
			values.add(index, newval);
			registerValue(newval);

			// Throw value removed event.
			throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_CHANGED, newval, index));
		}
		else
		{
			throw new RuntimeException("Value cannot be replaced, old value not found: "+oldval);
		}
	}*/

	/**
	 *  Get a value equal to the given object.
	 *  @param oldval The old value.
	 */
	public Object	getValue(Object oldval)
	{
		if(!isInited()) init();
		Object	newval	= null;
		boolean	found	= false;

		if(inivals!=null)
		{
			Iterator	it	= SReflect.getIterator(inivals.getValue());
			while(!found && it.hasNext())
			{
				Object	val	= it.next();
				found	= val==null && oldval==null || val!=null && val.equals(oldval);
				if(found)
					newval	= val;
			}
		}
		else
		{
			int index = indexOf(oldval);
	
			if(index != -1)
			{
				found	= true;
				newval	= values.get(index);
			}
		}

		if(!found)
			throw new RuntimeException("Value cannot be retrieved, old value not found: "+oldval);

		return newval;
	}

	/**
	 *  Test if a value is contained in a typed element.
	 *  @param value The value to test.
	 *  @return True, if value is contained.
	 */
	public boolean containsValue(Object value)
	{
		if(!isInited()) init();
		boolean contains	= false;

		if(inivals!=null)
		{
			Iterator	it	= SReflect.getIterator(inivals.getValue());
			while(!contains && it.hasNext())
			{
				Object	val	= it.next();
				contains	= val==null && value==null || val!=null && val.equals(value);
			}
		}
		else if(values!=null)
		{
			contains	=  values.contains(value);
		}

		return contains;
	}

	/**
	 *  Get the values of a typed element.
	 *  @return The values.
	 */
	public Object[]	getValues()
	{
		if(!isInited()) init();
		Object[] ret;

		if(inivals!=null)
		{
			ret = SUtil.iteratorToArray(SReflect.getIterator(inivals.getValue()),
				((IMTypedElementSet)getModelElement()).getClazz());
		}
		else if(values!=null)
		{
			ret	= values.toArray((Object[])Array.newInstance(SReflect.getWrappedType(((IMTypedElementSet)
				getModelElement()).getClazz()), values.size()));
		}
		else
		{
			ret	= (Object[])Array.newInstance(SReflect.getWrappedType(((IMTypedElementSet)
				getModelElement()).getClazz()), 0);
		}

		return ret;
	}

	/**
	 *  Get the number of values currently
	 *  contained in this set.
	 *  @return The values count.
	 */
	public int size()
	{
		if(!isInited()) init();
		int size;

		if(inivals!=null)
		{
			size	= 0;
			Iterator	it	= SReflect.getIterator(inivals.getValue());
			while(it.hasNext())
			{
				size++;
				it.next();
			}
		}
		else if(values!=null)
		{
			size	= values.size();
		}
		else
		{
			size	= 0;
		}

		return size;
	}

	/**
	 *  Get the update rate.
	 *  @return The update rate.
	 */
	public long	getUpdateRate()
	{
		// Can be called from init, therefore only init when not already initing.
		if(!isInited() && !initing) init();

		return ((IMTypedElementSet)getModelElement()).getUpdateRate();
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
		return ((IMTypedElementSet)getModelElement()).getClazz();
	}

	/**
	 *  This method gets called when a bound property is changed.
	 *  @param e A PropertyChangeEvent object describing the event source
	 *    and the property that has changed.
	 */
	public void propertyChange(final PropertyChangeEvent e)
	{
		//System.out.println("Received bean event: "+this);
		final int index = indexOf(e.getSource());

		if(index != -1)
		{
			// Throw value changed event depending on the thread
			// (e.g. plan/gui) that modified the bean.

			// Plan thread: The event has to be monitored for consequences (to interrupt plan)
			RPlan	plan	= getScope().getAgent().getCurrentPlan();
			//if(plan!=null && plan.getThread()==Thread.currentThread()) // should use checkThreadAccess()?
			if(getScope().getAgent().getInterpreter().isPlanThread())
			{
				getScope().getAgent().startMonitorConsequences();
				throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_CHANGED, e.getSource(), index));
				getScope().getAgent().endMonitorConsequences();
			}

			// Agent thread (e.g. custom invokeLater code): Just throw event.
			else if(getScope().getAgent().getInterpreter().isAgentThread())
			{
				throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_CHANGED, e.getSource(), index));
			}

			// Other thread: Event has to be thrown on the agent thread (using invokeLater).
			else
			{
				// Wait for agenda entries to be processed, before returning to original thread.
				/*getScope().getAgent().invokeSynchronized(new Runnable()
				{
					public void run()
					{
						throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_CHANGED, e.getSource(), index));
					}
				});*/

				// Avoid deadlocks when different operating on shared resources and using
				// the normal BeanPropertyChangeSupport.
				// Decouples property changes from outside (foreign thread).
				getScope().getAgent().getInterpreter().invokeLater(new Runnable()
				{
					public void run()
					{
						throwSystemEvent(createSystemEvent(SystemEvent.ESVALUE_CHANGED, e.getSource(), index));
					}
				});
			}
		}
		else
		{
			throw new RuntimeException("Value cannot be replaced, old value not found: "+e.getSource());
		}
	}

	/**
	 *  Create an added or removed event for a new/removed element.
	 *  @param type The event type (added or removed).
	 *  @param value The value.
	 *  @param index The index.
	 */
	public SystemEvent	createSystemEvent(String type, Object value, int index)
	{
		// Throw value added event.
		return new SystemEvent(type, this, value, index);
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(getName());
//		sb.append(", values=");
//		sb.append(SUtil.arrayToString(getValues()));
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map rep = super.getEncodableRepresentation();
		rep.put("valueclass", ((IMTypedElementSet)getModelElement()).getClazz().getName());
		rep.put("updaterate", ""+((IMTypedElementSet)getModelElement()).getUpdateRate());
//		String	values	= "n/a";
//		try
//		{
//			values = SUtil.arrayToString(getValues());
//		}
//		catch(Exception e){}
//		rep.put("values", values);
		return rep;
	}


	//-------- helper methods --------

	/**
	 *  Check the type of a value against the declared type.
	 *  @param value The value.
	 *  @throws RuntimeException when the check fails.
	 */
	protected void checkType(Object value)
	{
		Class testtype = null;
		Class type = ((IMTypedElementSet)getModelElement()).getClazz();

		if(value!=null)
		{
			testtype = value.getClass();
		}

		if(testtype!=null && !SReflect.isSupertype(type, testtype))
		{
			throw new RuntimeException("Cannot store value "+testtype+" in "+this);
		}
	}

	/**
	 *  Update the belief (evaluate the expressions).
	 * /
	protected void	update()
	{
		// create initial values for a multiple values element.
		if(inivals!=null)
		{
			removeValues();

			// Initial values specified by expression.
			Iterator	vals	= SReflect.getIterator(inivals.getValue());
			while(vals.hasNext())
			{
				addValue(vals.next());
			}
		}
	}*/

	/**
	 *  Register a value for observation.
	 *  If its an expression then add the action,
	 *  if its a bean then add the property listener.
	 */
	protected void	registerValue(Object value)
	{
		serialized_values	= null;
		if(value!=null && !(value instanceof RExpression))
		{
			try
			{
				// Do not use Class.getMethod (slow).
				Method	meth	= SReflect.getMethod(value.getClass(),
					"addPropertyChangeListener", PCL);
//				Method	meth	= value.getClass().getMethod(
//					"addPropertyChangeListener", PCL);
				if(meth!=null)
					meth.invoke(value, new Object[]{this});
			}
//			catch(NoSuchMethodException e){}
			catch(IllegalAccessException e){}
			catch(InvocationTargetException e){}
		}
	}

	/**
	 *  Deregister a value for observation.
	 *  If its an expression then clear the action,
	 *  if its a bean then remove the property listener.
	 */
	protected void	deregisterValue(Object value)
	{
		serialized_values	= null;
		if(value!=null)
		{
			// Stop listening for bean events.
			try
			{
				// Do not use Class.getMethod (slow).
				Method	meth	= SReflect.getMethod(value.getClass(),
					"removePropertyChangeListener", PCL);
//				Method	meth	= value.getClass().getMethod(
//					"removePropertyChangeListener", PCL);
				if(meth!=null)
					meth.invoke(value, new Object[]{this});
			}
//			catch(NoSuchMethodException e){}
			catch(IllegalAccessException e){}
			catch(InvocationTargetException e){}
		}
	}

	/**
	 *  Create an update action that updates
	 *  with respect to the update rate.
	 *  @return The update action.
	 * /
	protected IAgendaAction createUpdateAction()
	{
		return new UpdateSetAction(this, new DefaultPrecondition(this));
	}*/

	/**
	 *  Internal method to get the inivals.
	 */
	public IRExpression internalGetInivals()
	{
		// Can be called from init, therefore only init when not already initing.
		if(!isInited() && !initing) init();
		
		return inivals;
	}

	/**
	 *  Reevalaute the initial values expression and update
	 *  the values accordingly via update().
	 * /
	public class ReevaluateInitialValuesAction implements IAgendaAction, java.io.Serializable
	{
		public void execute()
		{
			IMExpression.IEvaluationModeData mode = ((IMExpression)inivals.getModelElement()).getEvaluationMode();
			if(IMExpression.IEvaluationModeData.ON_EVENT.equals(mode))
			{
				//System.out.println("Refreshing: "+getName());
				inivals.refresh();
			}
			update();
		}
	}*/

	//-------- serialization handling --------

	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	writeReplace() throws ObjectStreamException
	{
		// Save values before writing non transient element.
		if(!((IMTypedElementSet)getModelElement()).isTransient())
		{
			serialized_values	= values;
		}
		
		return super.writeReplace();
	}

	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	readResolve() throws ObjectStreamException
	{
		// Restore values after reading element.
		values	= serialized_values;
		serialized_values	= null;
		
		return super.readResolve();
	}
}

