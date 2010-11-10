package jadex.parser;

/**
 *  A factory for creating parsers.
 *  Used to shield the kernel from different parser implementations.
 */
public interface IParserFactory
{
	/**
	 *  Create a new parser of defined type.
	 *  @param imports The imports.
	 *  @return The parser.
	 */
	public IParser	createParser(String[] imports);

	/**
	 *  Create a new parser of defined type.
	 *  @param imports The imports.
	 *  @param filename The filename of the model (used for expression caching).
	 *  @param lastmodified The date the model was last modified (used for expression caching).
	 *  @return The parser.
	 */
	public IParser	createParser(String[] imports, String filename, long lastmodified);
}
