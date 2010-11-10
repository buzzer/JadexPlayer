package jadex.model;

/**
 *  The parameter set behaviour interface.
 */
public interface IMParameterSet extends IMTypedElementSet
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
	 *  @return The direction of the parameter set.
	 */
	public String	getDirection();

	/**
	 *  Set the direction (in/out).
	 *  @param direction	The direction of the parameter set.
	 */
	public void	setDirection(String direction);


	//-------- optional --------

	/**
	 *  Get the optional flag.
	 *  @return True, if values for this parameter set are optional.
	 */
	public boolean	isOptional();

	/**
	 *  Set the optional flag.
	 *  @param optional	True, if values for this parameter set are optional.
	 */
	public void	setOptional(boolean optional);


	//-------- values --------

	/**
	 *  Get the default values of the parameter.
	 *  @return The default values.
	 */
	public IMExpression[]	getDefaultValues();

	/**
	 *  Create a default values for the parameter.
	 *  @param expression	The expression string.
	 *  @return The new value expression.
	 */
	public IMExpression	createDefaultValue(String expression);

	/**
	 *  Delete the default values of the parameter.
	 *  @param value	The value expression.
	 */
	public void	deleteDefaultValue(IMExpression value);


	//-------- default values expression --------

	/**
	 *  Get the default values expression of the parameter (returning a collection of default values).
	 *  @return The default values expression expression (if any).
	 */
	public IMExpression	getDefaultValuesExpression();

	/**
	 *  Create the default values expression for the parameter (returning a collection of default values).
	 *  @param expression	The expression string.
	 *  @param mode	The evaluation mode.
	 *  @return The new value expression.
	 */
	public IMExpression	createDefaultValuesExpression(String expression, String mode);

	/**
	 *  Delete the default values expression of the parameter (returning a collection of default values).
	 */
	public void	deleteDefaultValuesExpression();
}
