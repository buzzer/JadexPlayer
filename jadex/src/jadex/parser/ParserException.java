package jadex.parser;

import jadex.model.SourceLocation;

/**
 *  Indication of a parser exception.
 */
public class ParserException extends RuntimeException
{
	//-------- attributes --------
	
	/** The source location. */
	protected SourceLocation loc;
	
	//-------- constructors --------
	
	/**
	 *  Create a new parser exception.
	 *  @param message The exception message.
	 */
	public ParserException(String message)
	{
		super(message);
	}
	
	/**
	 *  Create a new parser exception.
	 *  @param message The exception message.
	 *  @param cause The cause for this exception.
	 */
	public ParserException(String message, Throwable cause, SourceLocation loc)
	{
		super(message, cause);
		this.loc = loc;
	}
	
	//-------- methods --------
	
	/**
	 *  Get the parsing error source location. 
	 *  @return The source location of the parsing problem.
	 */
	public SourceLocation getSourceLocation()
	{
		return loc;
	}
}
