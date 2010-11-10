package jadex.model;


/**
 *  The plan body provides access to the executable code of a plan.
 */
public interface IMPlanBody extends IMExpression
{
	//-------- constants --------

	/** The standard plan body type. */
	public static final String BODY_STANDARD = "standard";

	/** The mobile plan body type. */
	public static final String BODY_MOBILE = "mobile";


	//-------- type --------

	/**
	 *  Get the type (e.g. "mobile").
	 *  @return The type of the plan body.
	 */
	public String	getType();

	/**
	 *  Set the type (e.g. "mobile").
	 *  @param type	The type of the plan body.
	 */
	public void	setType(String type);


	//-------- inline --------

	/**
	 *  Is this an inline plan body.
	 *  @return True, if inline.
	 */
	public boolean	isInline();

	/**
	 *  Set if inline plan body.
	 *  @param inline The inline state.
	 */
	public void	setInline(boolean inline);


	//-------- passed --------

	/**
	 *  Get the passed code.
	 *  @return The passed code.
	 */
	public String	getPassedCode();

	/**
	 *  Set the passed code.
	 *  @param passed The passed code.
	 */
	public void	setPassedCode(String passed);


	//-------- failed --------

	/**
	 *  Get the failed code.
	 *  @return The failed code.
	 */
	public String	getFailedCode();

	/**
	 *  Set the failed code.
	 *  @param failed The failed code.
	 */
	public void	setFailedCode(String failed);

	
	//-------- aborted --------

	/**
	 *  Get the aborted code.
	 *  @return The aborted code.
	 */
	public String	getAbortedCode();

	/**
	 *  Set the aborted code.
	 *  @param aborted The aborted code.
	 */
	public void	setAbortedCode(String aborted);
}
