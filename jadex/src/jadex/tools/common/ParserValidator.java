package jadex.tools.common;

import jadex.parser.SParser;

/**
 *  A validator that tries to parse the text of a textfield.
 */
public class ParserValidator implements IValidator
{
	//-------- attributes --------
	
	/** Flag indicating the last valid state. */
	protected boolean	lastvalid;

	/** The text corresponding to the last valid state. */
	protected String	lasttext;
	
	//-------- constructors --------

	/**
	 *  Create a parser validator.
	 */
	public ParserValidator()
	{
		this.lastvalid	= true;
		this.lasttext	= null;
	}
	
	//-------- IValidator interface --------

	/**
	 *  Return true when the given object is valid.
	 */
	public boolean isValid(Object object)
	{
		if(object instanceof String)
		{
			String	text	= (String) object;
			if(lasttext==null || !lasttext.equals(text))
			{
				lasttext	= text;
				if(text.length()!=0)
				{
					try
					{
						SParser.evaluateExpression(text, null, null);
						lastvalid	= true;
					}
					catch(Exception e)
					{
						lastvalid	= false;
					}
				}
				else
				{
					lastvalid	= true;
				}
			}
		}
		else
		{
			lastvalid	= false;
			lasttext	= null;
		}

		return lastvalid;
	}
}