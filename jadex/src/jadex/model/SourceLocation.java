package jadex.model;

/**
 *  A location in a source file
 *  represented by line number and column number.
 */
public class SourceLocation
{
	//-------- attributes --------
	
	/** The filename. */
	protected String	filename;
	
	/** The line number. */
	protected int	linenumber;
	
	/** The column number. */
	protected int	columnnumber;
	
	//-------- constructors --------
	
	/**
	 *  Create a new source location.
	 */
	public SourceLocation(String filename, int linenumber, int columnnumber)
	{
		this.filename	= filename;
		this.linenumber	= linenumber;
		this.columnnumber	= columnnumber;
	}
	
	//-------- accessors --------
	
	/**
	 *  Get the file name.
	 */
	public String	getFilename()
	{
		return filename;
	}
	
	/**
	 *  Set the filename.
	 *  @param filename The filename to set.
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	/**
	 *  Get the line number.
	 */
	public int	getLineNumber()
	{
		return linenumber;
	}
	
	/**
	 *  Set the line number.
	 *  @param linenumber The linenumber to set.
	 */
	public void setLineNumber(int linenumber)
	{
		this.linenumber = linenumber;
	}

	/**
	 *  Get the column number.
	 */
	public int	getColumnNumber()
	{
		return columnnumber;
	}

	/**
	 *  Set the column number.
	 *  @param columnnumber The columnnumber to set.
	 */
	public void setColumnNumber(int columnnumber)
	{
		this.columnnumber = columnnumber;
	}
}
