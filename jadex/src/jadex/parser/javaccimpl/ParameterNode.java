package jadex.parser.javaccimpl;

import java.util.Map;


/**
 *  Parameter node representing a parameter.
 *  Parameter values are supplied at evaluation time.
 */
public class ParameterNode	extends ExpressionNode
{
	//-------- constructors --------

	/**
	 *  Create a node.
	 *  @param p	The parser.
	 *  @param id	The id.
	 */
	public ParameterNode(ParserImpl p, int id)
	{
		super(p, id);
	}

	//-------- evaluation --------

	/**
	 *  Evaluate the term.
	 *  @param params	The parameters (string, value).
	 *  @return	The value of the term.
	 */
	public Object	getValue(Map params)
	{
		if(params==null || !params.containsKey(getText()))
			throw new RuntimeException("Parameter not accessible: "+getText());
		
		return params.get(getText());
	}

	/**
	 *  Create a string representation of this node and its subnodes.
	 *  @return A string representation of this node.
	 */
	public String toPlainString()
	{
		return getText();			
	}

	/**
	 *  Get unbound parameter nodes.
	 *  @return The unbound parameter nodes.
	 */
	public ParameterNode[]	getUnboundParameterNodes()
	{
		return new ParameterNode[]{this};
	}
}
