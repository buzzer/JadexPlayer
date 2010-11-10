package jadex.parser.javaccimpl;

import jadex.util.SReflect;

import java.util.Map;


/**
 *  Node representing a type.
 *  The value will be the class object of the type.
 */
public class TypeNode	extends ExpressionNode
{
	//-------- constructors --------

	/**
	 *  Create an expression node.
	 *  @param p	The parser.
	 *  @param id	The id.
	 */
	public TypeNode(ParserImpl p, int id)
	{
		super(p, id);
		setStaticType(Class.class);
	}

	//-------- evaluation --------

	/**
	 *  Append to the token text.
	 *  @param text	The text to append.
	 */
	public void	appendText(String text)
	{
		super.appendText(text);
		if(text.equals("[]"))
		{
			// Hack ??? Update constant value to array type.
			precompile();
		}
	}

	/**
	 *  Precompute the type.
	 */
	public void precompile()
	{
		String	name	= getText();

		// Get class object.
		Class	clazz	= SReflect.findClass0(getText(), imports);

		if(clazz==null)
		{
			// Shouldn't happen...?
			throw new ParseException("Class not found: "+name);
		}

		setConstantValue(clazz);
		setConstant(true);
	}

	/**
	 *  Evaluate the term.
	 *  @param params	The parameters (string, value).
	 *  @return	The value of the term.
	 */
	public Object	getValue(Map params)
	{
		if(getConstantValue()==null)
			precompile();
		return getConstantValue();
	}

	/**
	 *  Create a string representation of this node and its subnodes.
	 *  @return A string representation of this node.
	 */
	public String toPlainString()
	{
		return getText();			
	}
}

