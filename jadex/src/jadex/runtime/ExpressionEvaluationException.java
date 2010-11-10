package jadex.runtime;


/**
 *  A problem occurred during the evaluation of an expression.
 */
public class ExpressionEvaluationException	extends RuntimeException
{
	//-------- constructors --------

	/**
	 *  Create a new expression evaluation exception.
	 *  @param message The message.
	 *  @param cause The parent exception (if any).
	 */
	public ExpressionEvaluationException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
