package jadex.parser.javaccimpl;

import jadex.model.SourceLocation;
import jadex.parser.IParser;
import jadex.parser.IParserFactory;
import jadex.parser.ITerm;
import jadex.parser.ParserException;

import java.io.*;
import java.util.*;

/**
 *  The jadex parser parses all types of expressions in ADF and queries.
 */
public class Parser	implements IParser, IParserFactory, Serializable
{
	//-------- attributes --------

	/** The imports. */
	protected String[]	imports;

	//-------- constructors --------

	/**
	 *  Create a parser.
	 */
	public Parser()
	{
		this(null);
	}

	/**
	 *  Create a parser with package and imports.
	 *  @param imports	The imports.
	 */
	public Parser(String[] imports)
	{
		this.imports	= imports;
	}

	//-------- methods --------

	/**
	 *  Parse an expression string.
	 *  @param expression The expression string.
	 *  @param parameters Parameters declared in the expression (user parameters).
	 *  @return The parsed expression.
	 */
	public ITerm	parseExpression(String expression, List parameters)
	{
		// todo: use parameters for checking

		if(expression==null)
			throw new ParserException("String required for parsing: "+expression);
		// Init the parser.
		// Created every time, because JavaCC otherwise has memory leaks
		// and isn't thread safe.
		ParserImpl	parser	= new ParserImpl(new StringReader(expression));
		parser.setImports(imports);
		// todo: parser.setParameters(parameters);

		ExpressionNode	node;
		try
		{
			// Parse the expression.
			node	= parser.parseExpression();
			//node.dump("");

			// Check and precompile the expression.
			node.precompileTree();
		}
		catch(ParseException e)
		{
			SourceLocation loc = new SourceLocation(null, e.currentToken.next.beginLine, 
				e.currentToken.next.beginColumn); 
			throw new ParserException("Error parsing: "+expression+"\n"+e.getMessage(), e, loc);
		}
		catch(RuntimeException e)
		{
			throw e;
		}
		catch(Throwable e)
		{
			throw new ParserException("Error parsing: "+expression+"\n"+e, e, null);
		}

		// Now return that stuff.
		return  node;
	}

	/**
	 *  Parse a type expression.
	 *  @param expression	The type expression string.
	 *  @return The parsed type.
	 * /
	public Class	parseType(String expression)
	{
		if(expression==null)
			throw new ParseException("String required for parsing: "+expression);
		// Init the parser.
		// Created every time, because JavaCC otherwise has memory leaks
		// and isn't thread safe.
		ParserImpl	parser	= new ParserImpl(new StringReader(expression));
		parser.setImports(imports);

		Class	clazz;
		try
		{
			// Parse the type expression.
			clazz	= parser.parseType();
		}
		catch(ParseException e)
		{
//			if(e.specialConstructor)
				throw new ParseException("Error parsing: "+expression+"\n"+e.getMessage());
/*			else
				throw e;
* /		}
		catch(TokenMgrError e)
		{
			throw new ParseException("Error parsing: "+expression+"\n"+e.getMessage());
		}

		// Now return that stuff.
		return  clazz;
	}*/

	/**
	 *  Parse a value expression.
	 *  @param expression The type expression string.
	 *  @param params
	 *  @return The parsed value.
	 * /
	public Object	parseValue(String expression, List parameters, Map paramvalues)
	{
		return parseExpression(expression, parameters).getValue(paramvalues);
	}*/

//	/**
//	 *  Get the imports.
//	 */
//	public String[]	getImports()
//	{
//		return imports;
//	}

	/**
	 *  Parse a method body.
	 *  @param code The code.
	 *  @param base The base class.
	 *  @return An object of the compiled class.
	 */
	public Object parseClass(String code, Class base)
	{
		// todo: implement me
		throw new RuntimeException("ParseScript not supported by this parser impl: "+this);
	}

	//-------- IParserFactory methods --------

	/**
	 *  Create a new parser of defined type.
	 *  @param imports The imports.
	 *  @return The parser.
	 */
	public IParser createParser(String[] imports)
	{
		return new Parser(imports);
	}

	/**
	 *  Create a new parser of defined type.
	 *  @param imports The imports.
	 *  @param filename The filename of the model (used for expression caching).
	 *  @param lastmodified The date the model was last modified (used for expression caching).
	 *  @return The parser.
	 */
	public IParser createParser(String[] imports, String filename, long lastmodified)
	{
		// Todo?
		return new Parser(imports);
	}
}

