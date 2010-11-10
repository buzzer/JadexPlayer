package jadex.model;

/**
 *  Interface for parameters.
 */
public interface IMParameter extends IMTypedElement
{
	//-------- constants --------

	/** Fixed values are not allowed to be changed and are used for matching messages. */
	public static final String DIRECTION_FIXED = "fixed";

	/** In parameters for parameter elements. */
	public static final String DIRECTION_IN = "in";

	/** Out parameters for parameter elements. */
	public static final String DIRECTION_OUT = "out";

	/** Inout parameters for parameter elements. */
	public static final String DIRECTION_INOUT = "inout";


	//-------- direction --------

	/**
	 *  Get the direction (in/out).
	 *  @return The direction of the parameter.
	 */
	public String	getDirection();

	/**
	 *  Set the direction (in/out).
	 *  @param direction	The direction of the parameter.
	 */
	public void	setDirection(String direction);


	//-------- optional --------

	/**
	 *  Get the optional flag.
	 *  @return True, if a value for this parameter is optional.
	 */
	public boolean	isOptional();

	/**
	 *  Set the optional flag.
	 *  @param optional	True, if a value for this parameter is optional.
	 */
	public void	setOptional(boolean optional);


	//-------- value --------

	/**
	 *  Get the default value of the parameter.
	 *  @return The default value expression (if any).
	 */
	public IMExpression	getDefaultValue();

	/**
	 *  Create the default value for the parameter.
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createDefaultValue(String expression, String mode);

	/**
	 *  Delete the default value of the parameter.
	 */
	public void	deleteDefaultValue();


	//-------- binding --------

	/**
	 *  Get the binding options of the parameter (i.e. a collection of possible values).
	 *  @return The binding options expression (if any).
	 */
	public IMExpression	getBindingOptions();

	/**
	 *  Create the binding options for the parameter (i.e. a collection of possible values).
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createBindingOptions(String expression, String mode);

	/**
	 *  Delete the binding options of the parameter (i.e. a collection of possible values).
	 */
	public void	deleteBindingOptions();
}
