package jadex.runtime.impl;

import jadex.model.IMReferenceableElement;
import jadex.model.IMTypedElement;
import jadex.util.SReflect;

/**
 *  A process goal parameter redirects getValue() calls
 *  to the original goal parameter until setValue() is explicitly called.
 */
public class RProcessGoalParameter extends RReferenceableElement implements IRParameter
{
	//-------- attributes --------

	/** The parameter of the original goal. */
	protected IRParameter	parameter;

	/** The new value. */
	protected Object value;

	/** Flag indicating that the parameter is modified. */
	private boolean	modified;
	
	//-------- constructors --------
	/**
	 *  Create a new RProcessGoalParameter.
	 */
	protected RProcessGoalParameter(IRParameter parameter, RElement owner)
	{
		super(parameter.getName(), (IMReferenceableElement)parameter.getModelElement(), null, owner, null, null);
		// todo: hack wrong modelelement
		//super((IMParameter)parameter.getOriginalElement().getModelElement(), null, owner);

		this.parameter	= parameter;
		this.modified	= false;
		//this.inited	= true;	// Hack???

		// Usually done in int(), but init will not be called, to avoid
		// expressions to be created twice.
		// todo: needed for process parameters?
//		if(getUpdateRate()>0)
//			getScope().getAgent().addTimetableEntry(
//				new TimetableData(getUpdateRate(), new AgendaEntry(new UpdateAction(), this)));
	}

	//-------- methods --------

	/**
	 *  Get the value of a typed element.
	 *  @return The value.
	 */
	public Object	getValue()
	{
		if(modified)
		{
			return value;
		}
		else
		{
			return parameter.getValue();
		}
	}

	/**
	 *  Set a value of a typed element.
	 *  @param value The new value.
	 */
	public void setValue(Object value)
	{
		modified	= true;
		this.value= value;
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
		//sb.append(", value=");
		//sb.append(getValue());
		sb.append(", modified=");
		sb.append(modified);
		sb.append(", class=");
		sb.append(((IMTypedElement)getModelElement()).getClazz());
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Set the initial value.
	 *  May be a default value (from model),
	 *  or an initiual value provided on creation.
	 */
	public void	setInitialValue()
	{
		// todo: ?
	}

	/**
	 *
	 * @return
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 *
	 * @return
	 */
	public Class getClazz()
	{
		return parameter.getClazz();
	}

	/**
	 *
	 */
	public void checkReadAccess()
	{
		// todo: ?
	}

	/**
	 *
	 */
	public void checkWriteAccess()
	{
		// todo: ?
	}

	//-------- helper methods --------

	/**
	 *  Copy back the state to the original parameter.
	 */
	protected void copyContent()
	{
		if(modified)
		{
			parameter.setValue(this.getValue());
		}
	}
}
