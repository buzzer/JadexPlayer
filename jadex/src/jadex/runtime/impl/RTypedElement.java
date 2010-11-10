package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.*;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.*;
import jadex.runtime.impl.agenda.elementupdate.UpdateAction;
import java.util.*;
import java.beans.*;
import java.io.ObjectStreamException;
import java.lang.reflect.*;

/**
 *  The typed element has (at runtime) a single value
 *  or a set of values. The values may be represented
 *  by expressions, which can be evaluated when needed.
 */
public abstract class RTypedElement extends RReferenceableElement implements PropertyChangeListener
{
	//-------- constants --------

	/** The argument types for property change listener adding/removal (cached for speed). */
	protected static Class[]	PCL	= new Class[]{PropertyChangeListener.class};	

	//-------- attributes --------

	/** The value. */
	private transient Object value;

	/** The value when stored for serialization. */
	private Object serialized_value;

	/** The update rate (as Long or expression). */
	//protected Object update;

	/** The flag indicating that the element is currently initing. */
//	protected boolean	initing;

	/** Flag indicating that the parameter is modified. */
	private boolean	modified;

	//-------- constructor --------

	/**
	 *  Create a new typed element.
	 *  To evaluate the initial values, init() has to be called.
	 *  @param typedelement The typed element.
	 *  @param config The configuration.
	 *  @param owner The owner.
	 */
	protected RTypedElement(String name, IMTypedElement typedelement,
			IMConfigElement config, RElement owner, RReferenceableElement creator)
	{
		super(name, typedelement, config, owner, creator, null);
	}

	/**
	 *  Initialize the initial values.
	 */
	protected void	init()
	{
		// Hack!!! May have been called already from another element's init.
		if(!isInited())
		{
			super.init();
//			this.initing	= true;
			setInitialValue();
			
			// When an update rate is specified,
			// create a self-renewing time-table entry.
			// The associated action updates the typed element.
			if(getUpdateRate()>0)
				getScope().getAgent().addTimetableEntry(
					new TimetableData(getUpdateRate(), new UpdateAction(this, null)));
			
			removeConfiguration();
//			this.initing = false;	
		}
	}

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public abstract void	setInitialValue();
	
	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;
		
//		System.out.println("Cleanup: "+getName());
		super.cleanup();
		
		// Cleanup value.
		if(value!=null)
		{
			deregisterValue(value);
			
			// Hack!!! Cannot remove reference to value,
			// because e.g. goal parameters are accessed after cleanup.
			// value	= null;
		}
	}

	//-------- methods --------

	/**
	 *  Set a value of a typed element.
	 *  @param value The new value.
	 */
	public void setValue(Object value)
	{
		// Lazy init (setValue must not be called from init).
		if(!isInited()) initStructure();

		if(value!=null)
		{
			// Check the type.
			checkType(value);

			// Convert value, if necessary.
			// Todo: Disallow expressions to be set from the outside???
			if(!(value instanceof RExpression))
			{
				Class type = ((IMTypedElement)getModelElement()).getClazz();
				value = SReflect.convertWrappedValue(value, type);
			}
		}

		if(!SUtil.equals(this.value, value))
		{
			// Remove old listener.
			deregisterValue(this.value);

			// Add new listener.
			registerValue(value);

			this.value = value;
			//System.out.println(getName()+" value set to: "+value);

			this.modified = true;
			throwSystemEvent(createSystemEvent(SystemEvent.VALUE_CHANGED));
		}
	}

	/**
	 *  Set the initial value of a typed element.
	 *  @param value The new value.
	 */
	protected void setInitialValue(Object value)
	{
		if(value!=null)
		{
			// Check the type.
			checkType(value);

			// Convert value, if necessary.
			// Todo: Disallow expressions to be set from the outside???
			if(!(value instanceof RExpression))
			{
				Class type = ((IMTypedElement)getModelElement()).getClazz();
				value = SReflect.convertWrappedValue(value, type);
			}

			// Add new listener.
			registerValue(value);

			this.value = value;

			throwSystemEvent(createSystemEvent(SystemEvent.VALUE_CHANGED));
		}
	}

	/**
	 *  Get the value of a typed element.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		if(!isInited()) initStructure();
		Object ret = value;
		if(ret instanceof IRExpression
			&& !SReflect.isSupertype(IRExpression.class, ((IMTypedElement)getModelElement()).getClazz())
			&& !SReflect.isSupertype(IInterpreterCondition.class, ((IMTypedElement)getModelElement()).getClazz()))
		{
			// Only evaluate when value is expression and return type is different from expression.
			//ret = SReflect.convertWrappedValue(((RExpression)ret).getValue(),
			//	((IMTypedElement)getModelElement()).getClazz());
 			ret = ((RExpression)ret).getValue();
		}
		/*else
		{
			if(SReflect.isSupertype(RExpression.class, ((IMTypedElement)getModelElement()).getClazz()))
				System.out.println("!!!! "+getName()+" "+((IMTypedElement)getModelElement()).getClazz());
		}*/
 		return ret;
	}

	/**
	 *  Refresh the value of the element.
	 *  @throws UnsupportedOperationException	when the value is not an expression.
	 */
	public void	refresh()
	{
		if(!isInited()) initStructure();
		if(value instanceof RExpression)
		{
			((RExpression)value).refresh();
 		}
 		else
 		{
 			throw new UnsupportedOperationException("Cannot refresh non-expression value: "+this);
 		}
	}

	/**
	 *  Get the update rate.
	 *  @return The update rate.
	 */
	public long	getUpdateRate()
	{
		// Can be called from constructor, therefore only init when not already initing.
//		if(!isInited() && !initing) initStructure();
		if(!isInited()) initStructure();
		return ((IMTypedElement)getModelElement()).getUpdateRate();
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
		return ((IMTypedElement)getModelElement()).getClazz();
	}

	/**
	 *  This method gets called when a bound property is changed.
	 *  @param e A PropertyChangeEvent object describing the event source
	 *    and the property that has changed.
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		//System.out.println("PropChange "+getName()+" in "+getScope().getPlatformAgent().getLocalName()+" "+e);

		// Throw value changed event depending on the thread that modified the bean.

		// Plan thread: The event has to be monitored for consequences (to interrupt plan)
		RPlan	plan	= getScope().getAgent().getCurrentPlan();
		//if(plan!=null && plan.getThread()==Thread.currentThread()) // should use checkThreadAccess()?
		if(getScope().getAgent().getInterpreter().isPlanThread())
		{
			getScope().getAgent().startMonitorConsequences();
			throwSystemEvent(createSystemEvent(SystemEvent.VALUE_CHANGED));
			getScope().getAgent().endMonitorConsequences();
		}

		// Agent thread (e.g. custom invokeLater code): Just throw event.
		//else if(getScope().getPlatformAgent().getAgentThread()==Thread.currentThread())
		else if(getScope().getAgent().getInterpreter().isAgentThread())
		{
			throwSystemEvent(createSystemEvent(SystemEvent.VALUE_CHANGED));
		}

		// Other thread (e.g. awt-thread): Event has to be thrown on the agent thread (using invokeLater).
		else
		{
			// Wait for agenda entries to be processed, before returning to original thread.
			/*getScope().getAgent().invokeSynchronized(new Runnable()
			{
				public void run()
				{
					throwSystemEvent(createSystemEvent(SystemEvent.VALUE_CHANGED));
				}
			});*/

			// Avoid deadlocks when different operating on shared resources and using
			// the normal BeanPropertyChangeSupport.
			// Decouples property changes from outside (foreign thread).
			getScope().getAgent().getInterpreter().invokeLater(new Runnable()
			{
				public void run()
				{
					throwSystemEvent(createSystemEvent(SystemEvent.VALUE_CHANGED));
				}
			});
		}
	}

	/**
	 *  Direct access to the value.
	 */
	public Object internalGetValue()
	{
		return value;
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
		sb.append(", value=");
		//sb.append(getValue());
		sb.append(value);
		sb.append(", class=");
		sb.append(((IMTypedElement)getModelElement()).getClazz());
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
		rep.put("valueclass", ((IMTypedElement)getModelElement()).getClazz().getName());
		rep.put("updaterate", ""+((IMTypedElement)getModelElement()).getUpdateRate());
		String	value;
		if(this.value instanceof RExpression)
		{
			try
			{
				value = ""+((RExpression)this.value).getValue();
			}
			catch(Exception e)
			{
				value = "n/a";
			}
		}
		else
		{
			value	= ""+this.value;
		}
		rep.put("value", value);
		return rep;
	}

	//-------- helper methods --------

	/**
	 *  Create a system event.
	 *  @param type The event type.
	 */
	public SystemEvent	createSystemEvent(String type)
	{
		// Throw value change event.
		// Hack! To allow initially invalid facts (e.g. special "dynamic" facts)
		Object	value	= this.value;
		if(value instanceof RExpression)
		{
			Class ctype = ((IMTypedElement)getModelElement()).getClazz();
			if(!SReflect.isSupertype(RExpression.class, ctype))
			{
				try
				{
					value = ((RExpression)value).getValue();
				}
				catch(Exception e)
				{
					value = "n/a";
				}
			}
		}
		return new SystemEvent(type, this, value);
	}

	/**
	 *  Check the type of a value against the declared type.
	 *  @param value The value.
	 *  @throws RuntimeException when the check fails.
	 */
	protected void checkType(Object value)
	{
		Class testtype = null;
		Class type = ((IMTypedElement)getModelElement()).getClazz();

		if(value instanceof RExpression)
		{
			IMExpression mex = (IMExpression)((RExpression)value).getModelElement();

			if(SReflect.isSupertype(RExpression.class, type))
			{
				testtype = value.getClass();
			}
			else if(mex.getClazz()!=null)
			{
				testtype = mex.getClazz();
			}
			/*else
			{
				testtype = mex.getStaticType();
			}*/
		}
		else if(value!=null)
		{
			testtype = value.getClass();
		}

		if(testtype!=null && !SReflect.isSupertype(type, testtype))
		{
			throw new RuntimeException("Cannot store value "+testtype+" in "+this+" "+getOwner());
		}
	}

	/**
	 *  Register a value for observation.
	 *  If its an expression then add the action,
	 *  if its a bean then add the property listener.
	 */
	protected void	registerValue(Object value)
	{
		serialized_value	= null;
		// todo: set SystemEvent ?
		if(value!=null)
		{
//			System.out.println("register: "+value);
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
		serialized_value	= null;
		if(value instanceof RExpression)
		{
			Class type = ((IMTypedElement)getModelElement()).getClazz();
			if(!SReflect.isSupertype(RExpression.class, type))
			{
				// Stop listening for change events.
				((RExpression)value).cleanup();
			}
		}
		else if(value!=null)
		{
//			System.out.println("deregister: "+value);
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
	 */
	protected IAgendaAction createUpdateAction()
	{
		return new UpdateAction(this, null);
	}

	//-------- serialization handling --------

	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	writeReplace() throws ObjectStreamException
	{
		// Save value before writing nontransient element.
		if(!((IMTypedElement)getModelElement()).isTransient())
		{
			serialized_value	= value;
		}
		return super.writeReplace();
	}

	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	readResolve() throws ObjectStreamException
	{
		// Restore value after reading element.
		value	= serialized_value;
		
		return super.readResolve();
	}
}

