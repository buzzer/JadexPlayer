package jadex.model.jibximpl;

import jadex.model.*;

/**
 *  The condition model element is the java representation
 *  of a condition description (e.g. from the xml definition).
 */
public class MCondition extends MExpression implements IMCondition
{
	//-------- xml attributes --------

	/** The trigger mode. */
	protected String trigger;

	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// Needed because xml schema does not allow subtypes to overwrite default values :-(
		if(getEvaluationMode()==null)
			setEvaluationMode(IMExpression.MODE_DYNAMIC);
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		checkClass(Boolean.class, report);
	}

	//-------- xml methods --------

	/**
	 *  Get the trigger.
	 *  @return The trigger.
	 */
	public String getTrigger()
	{
		return this.trigger;
	}

	/**
	 *  Set the trigger.
	 *  @param trigger The trigger.
	 */
	public void setTrigger(String trigger)
	{
		this.trigger = trigger;
	}
}
