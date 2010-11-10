package jadex.model;


/**
 *  The condition model element.
 */
public interface IMCondition extends IMExpression
{

	//-------- constants --------

	/** The "always" trigger mode. */
	public static final String	TRIGGER_ALWAYS = "always";
	/** The "changes" trigger mode. */
	public static final String	TRIGGER_CHANGES = "changes";
	/** The "changes_to_false" trigger mode. */
	public static final String	TRIGGER_CHANGES_TO_FALSE = "changes_to_false";
	/** The "changes_to_true" trigger mode. */
	public static final String	TRIGGER_CHANGES_TO_TRUE = "changes_to_true";
	/** The "is_false" trigger mode. */
	public static final String	TRIGGER_IS_FALSE = "is_false";
	/** The "is_true" trigger mode. */
	public static final String	TRIGGER_IS_TRUE = "is_true";

	
	//-------- trigger --------

	/**
	 *  Get the trigger type.
	 *  @return	The trigger type.
	 */
	public String	getTrigger();

	/**
	 *  Set the trigger type.
	 *  @param trigger	The trigger type.
	 */
	public void setTrigger(String trigger);

}
