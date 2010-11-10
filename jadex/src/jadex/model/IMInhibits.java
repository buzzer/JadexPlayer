package jadex.model;


/**
 *  The interface containing information about inhibiting another goal instance.
 */
public interface IMInhibits extends IMExpression
{
	//-------- constants --------

	/** The "when_active" inhibition type. */
	public static final String	WHEN_ACTIVE = "when_active";

	/** The "when_in_process" inhibition type. */
	public static final String	WHEN_IN_PROCESS = "when_in_process";

	//-------- inhibit --------

	/**
	 *  Get the inhibition type, i.e. an identifier for the goal's
	 *  lifecycle state in which the inhibition link is active.
	 *  @return	The inhibition type.	
	 */
	public String	getInhibit();
	
	/**
	 *  Set the inhibition type, i.e. an identifier for the goal's
	 *  lifecycle state in which the inhibition link is active.
	 *  @param type	The inhibition type.
	 */
	public void	setInhibit(String type);
	
	
	//-------- reference --------

	/**
	 *  Get the reference to the inhibited goal type.
	 *  @return	The inhibited goal type.
	 */
	public String	getReference();
	
	/**
	 *  Set the reference to the inhibited goal type.
	 *  @param ref	The inhibited goal type.
	 */
	public void	setReference(String ref);
	

	//-------- not xml related --------
	
	/**
	 *  Get the inhibited goals.
	 *  @return The inhibited goal.
	 */
	public IMReferenceableElement	getInhibitedGoal();

	/**
	 *  Get the inhibiting expression.
	 *  @return The inhibiting expression.
	 */
	public IMExpression	getInhibitingExpression();
}
