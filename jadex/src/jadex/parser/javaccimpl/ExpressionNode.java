package jadex.parser.javaccimpl;

import jadex.parser.ITerm;
import jadex.util.SUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *  Base class of expression node hierarchy.
 */
public abstract class ExpressionNode	extends SimpleNode	implements ITerm
{
	//-------- attributes --------

	/** The token text (if any). */
	protected String	text;

	/** The imports (if any). */
	protected String[]	imports;

	/** The static type (if any). */
	protected Class	static_type;

	/** Is the node value constant
	    (independent of evaluation context and parameters)? */
	protected boolean	constant;

	/** The constant value (if any). */
	protected Object	constant_value;

	//-------- constructors --------

	/**
	 *  Create an expression node.
	 *  @param p	The parser.
	 *  @param id	The id.
	 */
	public ExpressionNode(ParserImpl p, int id)
	{
		super(p, id);
		this.imports	= p.getImports();
		//this.static_type	= Object.class; // not yet
	}

	//-------- attribute accessors --------

	/**
	 *  Set the token text.
	 *  @param text	The token text.
	 */
	public void	setText(String text)
	{
		this.text	= text;
	}

	/**
	 *  Append to the token text.
	 *  @param text	The text to append.
	 */
	public void	appendText(String text)
	{
		this.text	= this.text==null ? text : this.text+text;
	}

	/**
	 *  Get the token text.
	 *  @return The token text.
	 */
	public String	getText()
	{
		return this.text;
	}

	/**
	 *  Set the static type.
	 *  @param static_type	The static type.
	 */
	public void	setStaticType(Class static_type)
	{
		this.static_type	=  static_type;
	}

	/**
	 *  Get the static type (if any).
	 *  If no information about the return type of an expression
	 *  is available (e.g. because it depends on the evaluation context),
	 *  the static type is unknown (null).
	 *  @return The static type or null, if unknown.
	 */
	public Class	getStaticType()
	{
		return this.static_type;
	}

	/**
	 *  Set the constant value.
	 *  @param constant_value	The constant value.
	 */
	public void	setConstantValue(Object constant_value)
	{
		this.constant_value	=  constant_value;
	}

	/**
	 *  Get the constant value.
	 *  The constant value of a node may be known,
	 *  when it is independent of the evaluation context,
	 *  and the child nodes are constant, too.
	 *  @return The constant value.
	 */
	public Object	getConstantValue()
	{
		return this.constant_value;
	}

	/**
	 *  Set if the node is constant.
	 *  @param constant	The constant.
	 */
	public void	setConstant(boolean constant)
	{
		this.constant	=  constant;
	}

	/**
	 *  Get if the node is constant.
	 *  The node is constant, when it is independent
	 *  of the evaluation context, and the child nodes
	 *  are constant, too.
	 *  @return The constant flag.
	 */
	public boolean	isConstant()
	{
		return this.constant;
	}

	/**
	 *  Create a string representation of this node for dumping in a tree.
	 *  @return A string representation of this node.
	 */
	public String toString(String prefix)
	{
		return prefix + ParserImplTreeConstants.jjtNodeName[id]+"("+text+")";
	}

	/**
	 *  Create a string representation of this node for dumping in a tree.
	 *  @return A string representation of this node.
	 */
	public String toPlainString()
	{
		return super.toString();
	}

	/**
	 *  Create a string representation of this node for dumping in a tree.
	 *  @return A string representation of this node.
	 */
	public String toString()
	{
		return "<" + toPlainString() + ">";
	}

	/**
	 *  Create a string for a subnode.
	 *  Automatically adds braces if necessary.
	 *  @param subnode	The index of the subnode.
	 *  @return The string for the subnode.
	 */
	protected String	subnodeToString(int subnode)
	{
		Node	node	= jjtGetChild(subnode);
		if(node.jjtGetNumChildren()==0)
			return node.toPlainString();
		else
			return "(" + node.toPlainString() + ")";
	}

	//-------- ITerm methods --------

	/**
	 *  Evaluate the term.
	 *  @param params	The parameters (string, value).
	 *  @return	The value of the term.
	 * @throws Exception 
	 */
	public abstract Object	getValue(Map params) throws Exception;

	/**
	 *  Get relevant event types.
	 *  @return The event types.
	 */
	public List getRelevantEventtypes()
	{
		// Determine semantic dependencies.
		List ret = new ArrayList();

		ParameterNode[]	params	= getUnboundParameterNodes();

		for(int i=0; i<params.length; i++)
		{
			// Only expressions like "$<var>.ref/meth()" are of relevance
			if(params[i].jjtGetParent() instanceof ReflectNode)
			{
				ReflectNode	ref	= (ReflectNode)params[i].jjtGetParent();
				// Beliefs
				if(params[i].getText().equals("$beliefbase"))
				{
					// $beliefbase.<belief(set)>
					if(ref.getType()==ReflectNode.FIELD)
					{
						ret.add(new String[]{"$beliefbase", ref.getText()});
					}

					// $beliefbase.getBelief()
					else if(ref.getType()==ReflectNode.METHOD
						&& ref.getText().equals("getBelief"))
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($beliefbase is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1)
							.jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic belief -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$beliefbase", (String)arg.getConstantValue()});
						}
					}

					// $beliefbase.getBeliefSet()
					else if(ref.getType()==ReflectNode.METHOD
						&& ref.getText().equals("getBeliefSet"))
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($beliefbase is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1)
							.jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic belief set -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$beliefbase", (String)arg.getConstantValue()});
						}
					}
				}

				// Goalbase access
				else if(params[i].getText().equals("$goalbase"))
				{
					// $goalbase.getGoals("<name>")
					if(ref.getType()==ReflectNode.METHOD
						&& (ref.getText().equals("getGoals"))
						&& ref.jjtGetChild(1).jjtGetNumChildren()>0)	// args not empty.
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($goal is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1).jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic ´goal -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$goalbase", (String)arg.getConstantValue()});
						}
					}

					// $goalbase.getGoals()
					else if(ref.getType()==ReflectNode.METHOD
						&& (ref.getText().equals("getGoals")))
					{
						ret.add(new String[]{"$goalbase", null}); // affected from any goal
					}
				}

				// Goal parameter (owner is parameter or condition, owned by goal)
				else if(params[i].getText().equals("$goal"))
				{
					// $goal.<parameter>
					if(ref.getType()==ReflectNode.FIELD)
					{
						ret.add(new String[]{"$goal", ref.getText()});
					}

					// $goal.getParameter()
					else if(ref.getType()==ReflectNode.METHOD
						&& ref.getText().equals("getParameter"))
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($goal is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1)
							.jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic parameter -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$goal", (String)arg.getConstantValue()});
						}
					}

					// $goal.getParameterSet()
					else if(ref.getType()==ReflectNode.METHOD
						&& ref.getText().equals("getParameterSet"))
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($goal is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1)
							.jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic parameter set -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$goal", (String)arg.getConstantValue()});
						}
					}
				}

					// Goal parameter (owner is parameter or condition, owned by goal)
				else if(params[i].getText().equals("$plan"))
				{
					// $goal.<parameter>
					if(ref.getType()==ReflectNode.FIELD)
					{
						ret.add(new String[]{"$plan", ref.getText()});
					}

					// $goal.getParameter()
					else if(ref.getType()==ReflectNode.METHOD
						&& ref.getText().equals("getParameter"))
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($goal is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1)
							.jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic parameter -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$plan", (String)arg.getConstantValue()});
						}
					}

					// $goal.getParameterSet()
					else if(ref.getType()==ReflectNode.METHOD
						&& ref.getText().equals("getParameterSet"))
					{
						// Arguments are a stored in separate node
						// which is 2nd child ($goal is 1st arg).
						ExpressionNode	arg	= (ExpressionNode)ref.jjtGetChild(1)
							.jjtGetChild(0);
						if(!arg.isConstant())
						{
							System.out.println("Warning: Reference to dynamic parameter set -"
								+" affecting events cannot be determined: "+this);
							System.out.println(arg);
						}
						else
						{
							ret.add(new String[]{"$plan", (String)arg.getConstantValue()});
						}
					}
				}

				// todo: planbase ?
			}
		}
		return ret;

		// Determine semantic dependencies.
		/*if(this instanceof IMBindingCondition)
		{
			IMParameter[]	bindings;
			IMElement	owner	= getOwner();
			while(!(owner instanceof IMParameterElement) && owner!=null)
				owner	= owner.getOwner();
			assert owner!=null : this+", "+this.getOwner();
			bindings = ((IMParameterElement)owner).getBindingParameters();

			if(bindings.length>0)
			{
				for(int i=0; i<bindings.length; i++)
				{
					//addEventType(null, bindings[i].getName(), BINDING_REF);
					addEventType(null, bindings[i].getName(), PARAMETER_REF);
				}
			}
		}*/
	}

	/**
	 *  Get unbound parameter nodes.
	 *  @return The unbound parameter nodes.
	 */
	protected ParameterNode[]	getUnboundParameterNodes()
	{
		// Default: Return unbound parameters of subnodes.
		ParameterNode[]	ret	= new ParameterNode[0];
		for(int i=0; i<jjtGetNumChildren(); i++)
		{
			ret	= (ParameterNode[])SUtil.joinArrays(ret,
				((ExpressionNode)jjtGetChild(i)).getUnboundParameterNodes());
		}
		return ret;
	}

	//-------- expression methods --------

	/**
	 *  This method should be overridden to perform
	 *  all possible checks and precompute all values
	 *  (e.g. the static_type), which are independent
	 *  of the evaluation context and parameters.
	 */
	public void precompile()
	{
	}

	/**
	 *  Precompile this node and all subnodes.
	 */
	public void precompileTree()
	{
		// Precompile subtree first !
		for(int i=0; i<jjtGetNumChildren(); i++)
		{
			((ExpressionNode)jjtGetChild(i)).precompileTree();
		}

		// Now precompile this node.
		precompile();
	}

	/**
	 *  (Re)throw an exception that occured during parsing
	 *  and add a useful error message.
	 *  @param ex	The exception to be rethrown (if any).
	 */
	protected void	throwParseException(Throwable ex)	throws ParseException
	{
		Node	root	= this;
		while(root.jjtGetParent()!=null)
			root	= root.jjtGetParent();

		StringWriter	sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		throw new ParseException("Exception while parsing expression: "
				+root.toPlainString()+"\n"+sw);
	}

	/**
	 *  (Re)throw an exception that occured during evaluation
	 *  and add a useful error message.
	 *  @param ex	The exception to be rethrown (if any).
	 */
	protected void	throwEvaluationException(Throwable ex)	throws RuntimeException
	{
		Node	root	= this;
		while(root.jjtGetParent()!=null)
			root	= root.jjtGetParent();

		StringWriter	sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		throw new RuntimeException("Exception while evaluating expression: "
				+root.toPlainString()+"\n"+sw);
	}
}

