package jadex.parser;

import java.util.*;

/**
 *  Interface for parsers. Must support the parsing of Java expressions into
 *  evaluable objects (ITerm).
 */
public interface IParser
{
	/**
	 *  Parse an expression string.
	 *  @param expression The expression string.
	 *  @param parameters Parameters declared in the expression (user parameters).
	 *  @return The parsed expression.
	 */
	public ITerm	parseExpression(String expression, List parameters);
	
	/**
	 *  Parse an expression string.
	 *  @param expression The expression.
	 *  @param parameters Parameters declared in the expression (user parameters).
	 *  @return The parsed expression.
	 */
	//public ITerm	parseExpression(IMExpression expression, List parameters);

//	/**
//	 *  Parse a script string.
//	 *  @param script The script string.
//	 *  @param parameters Parameters declared in the expression (user parameters).
//	 *  @return The parsed expression.
//	 */
//	public IScript	parseScript(String script, List parameters);

	/**
	 *  Parse a method body (optional operation).
	 *  @param code The code.
	 *  @param base The base class.
	 *  @return An object of the compiled class.
	 */
	public Object parseClass(String code, Class base);
}
