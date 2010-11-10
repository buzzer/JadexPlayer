package jadex.parser.javaccimpl;

import jadex.config.Configuration;
import jadex.runtime.AbstractPlan;
import jadex.runtime.impl.IRParameterElement;
import jadex.runtime.impl.RBeliefbase;
import jadex.util.DynamicURLClassLoader;
import jadex.util.SReflect;
import jadex.util.SUtil;

import java.lang.reflect.*;
import java.util.*;


/**
 *  A node for a constructor or method invocation or field access.
 */
// Todo: Allow conversions between basic number types.
// Todo: Remove dependencies to jadex.runtime ???
public class ReflectNode	extends ExpressionNode
{
	//-------- constants --------

	/** The constructor type. */
	public static final int	CONSTRUCTOR	= 1;

	/** The static method type. */
	public static final int	STATIC_METHOD	= 2;

	/** The static field type. */
	public static final int	STATIC_FIELD	= 3;

	/** The method type. */
	public static final int	METHOD	= 4;

	/** The field type. */
	public static final int	FIELD	= 5;

	//-------- attributes --------

	/** The reflect node type. */
	protected int type;

	//-------- precomputed values --------

	/** The clazz. */
	protected Class	clazz;

	/** The argument types (for constructors and methods). */
	protected transient Class[]	argtypes;

	/** The argument values (for constructors and methods). */
	protected transient Object[]	args;

	/** The possible constructors (for constructor nodes). */
	protected transient Constructor[]	constructors;

	/** The dynamically reloaded class (currently only for constructor nodes). */
	protected transient Class	reloadedclass;
	
	/** The possible methods (for static and nonstatic methods). */
	protected transient Method[]	methods;

//	/** The field accessor method (for static and nonstatic fields). */
//	protected transient Method	accessor;

	/** The field (for static and nonstatic fields). */
	protected transient Field	field;

	/** Flag indicating that this node is a candidate dynamic class reloading
	 *  (currently only supported for plan constructors). */
	protected boolean	reloadable;

	//-------- constructors --------

	/**
	 *  Create an expression node.
	 *  @param p	The parser.
	 *  @param id	The id.
	 */
	public ReflectNode(ParserImpl p, int id)
	{
		super(p, id);
	}

	//-------- attribute accessors --------

	/**
	 *  Set the constructor type.
	 *  @param type	The constrcutor type.
	 */
	public void	setType(int type)
	{
		this.type	= type;
	}

	/**
	 *  Get the constructor type.
	 *  @return The constructor type.
	 */
	public int	getType()
	{
		return this.type;
	}

	//-------- evaluation --------

	/**
	 *  Precompute the set of matching constructors if possible.
	 */
	public void precompile()
	{
		// Check number of children.
		if(	(type==CONSTRUCTOR || type==STATIC_METHOD || type==METHOD)
			&& !(jjtGetNumChildren()==2)
		||	(type==STATIC_FIELD || type==FIELD)
			&& !(jjtGetNumChildren()==1))
		{
			throw new ParseException("Wrong number of child nodes: "+this);
		}
		else if(type!=CONSTRUCTOR && type!=STATIC_METHOD && type!=METHOD
			&& type!=STATIC_FIELD && type!=FIELD)
		{
			throw new ParseException("Unknown node type "+type+": "+this);
		}

		// Get child nodes.
		int	child=0;
		ExpressionNode	type_or_value	=  (ExpressionNode)jjtGetChild(child++);
		ExpressionNode	argsnode	= jjtGetNumChildren()==child ? null
			: (ExpressionNode)jjtGetChild(child);

		// Determine class and reference value for nonstatic members.
		if(type==METHOD || type==FIELD)
		{
			clazz	= type_or_value.getStaticType();
		}

		// Determine class for static members.
		else if(type_or_value!=null && type_or_value.isConstant())
		{
			try
			{
				clazz	= (Class)type_or_value.getValue(null);
			}
			catch(Exception e)
			{
			}
		}

		if(clazz!=null && (clazz.getModifiers() & Modifier.PUBLIC)==0)
		{
			throw new ParseException("Cannot access member of nonpublic class: "+clazz);
		}
		
		// Precompute argument types and values.
		if(type==CONSTRUCTOR || type==STATIC_METHOD || type==METHOD)
		{
			this.argtypes	= new Class[argsnode.jjtGetNumChildren()];
			this.args	= new Object[argtypes.length];
			for(int i=0; i<argtypes.length; i++)
			{
				ExpressionNode	node	= (ExpressionNode)argsnode.jjtGetChild(i);
				argtypes[i]	= node.getStaticType();
				if(node.isConstant())
				{
					try
					{
						args[i]	= node.getValue(null);
					}
					catch(Exception e)
					{
					}
				}
			}

			// Find available constructors
			if(type==CONSTRUCTOR && clazz!=null)
			{
				this.setStaticType(clazz);
				this.constructors	= findConstructors(clazz, argtypes);
				if(constructors.length==0)
				{
					throw new ParseException("No constructor found for: "+clazz
						+SUtil.arrayToString(argtypes));
				}
				
				// Set node to reloadable if this is a plan constructor.
				if(SReflect.isSupertype(AbstractPlan.class, clazz))
				{
					this.reloadable	= true;
				}
			}

			// Find available methods
			else if( (type==STATIC_METHOD || type==METHOD) && clazz!=null)
			{
				this.methods	= findMethods(clazz, argtypes);
				if(methods.length==0)
				{
					throw new ParseException("No "+getText()+" method found for: "+clazz
						+SUtil.arrayToString(argtypes));
				}
				else
				{					
					// Determine return type, if unique.
					Class	retype	= null;
					for(int i=0; i<methods.length; i++)
					{
						if(i==0)
						{
							retype	= methods[i].getReturnType();
						}
						else if(retype!=methods[i].getReturnType())
						{
							retype	= null;
						}
					}
					if(retype!=null)
						setStaticType(SReflect.getWrappedType(retype));
				}
			}
		}

		// Find field
		else if( (type==STATIC_FIELD || type==FIELD) && clazz!=null)
		{
			// Find field. Handle ".class" specially.
			if(type==STATIC_FIELD && getText().equals("class"))
			{
				this.setStaticType(Class.class);
				this.setConstantValue(clazz);
				this.setConstant(true);
			}
			// Handle ".length" of arrays specially (Java Bug???).
			else if(type==FIELD && clazz.isArray() && getText().equals("length"))
			{
				this.setStaticType(int.class);
				if(type_or_value.isConstant())
				{
					try
					{
						Object	array	= type_or_value.getValue(null);
						this.setConstantValue(new Integer(Array.getLength(array)));
						this.setConstant(true);
					}
					catch(Exception e)
					{
					}
				}
			}
			// Ignore beliefbase and parameter elements.
			else if(SReflect.isSupertype(RBeliefbase.class, clazz)
				|| SReflect.isSupertype(IRParameterElement.class, clazz))
			{
				// Do nothing.
			}
			else
			{
				try
				{
					this.field	= SReflect.getCachedField(clazz, getText());
					//this.field	= clazz.getField(getText());
					this.setStaticType(SReflect.getWrappedType(field.getType()));

					// Check if static modifier matches.
					if(type==STATIC_FIELD &&!Modifier.isStatic(field.getModifiers()))
					{
						throw new ParseException("Static reference to nonstatic field :"+this);
					}

					// For final fields precompute value.
					if(Modifier.isFinal(field.getModifiers()))
					{
						try
						{
							if(Modifier.isStatic(field.getModifiers()))
							{
								this.setConstantValue(field.get(null));
								this.setConstant(true);
							}
							else if(type_or_value.isConstant())
							{
								try
								{
									Object value	= type_or_value.getValue(null);
									if(value!=null)
									{
										this.setConstantValue(field.get(value));
										this.setConstant(true);
									}
									else
									{
										throw new ParseException("Cannot reference nonstatic field of null value: "+this);
									}
								}
								catch(ParseException e)
								{
									throw e;
								}
								catch(Exception e)
								{
								}
							}
						}
						catch(IllegalAccessException e)
						{
							throw new ParseException("Nonpublic field cannot be accessed: "+this);
						}
					}
				}
				catch(NoSuchFieldException e)
				{
//					// Try bean helper.
//					Class type = BeanHelper.getPropertyClass(clazz, getText());
//
//					if(type!=null && type_or_value.isConstant())
//					{
//						Object value	= type_or_value.getValue(null);
//						if(value!=null)
//						{
//							try
//							{
//								this.setConstantValue(BeanHelper.getPropertyValue(value, getText()));
//								this.setConstant(true);
//							}
//							catch(IntrospectionException ie)
//							{
//								throwParseException(ie);
//							}
//						}
//						else
//						{
//							throw new ParseException("Cannot reference nonstatic field of null value: "+this);
//						}
//					}

					// Try map accessor.
					/*if(accessor==null && SReflect.isSupertype(Map.class, clazz))
					{
						try
						{
							accessor	= clazz.getMethod("get", new Class[]{Object.class});
							args	= new Object[]{getText()};
						}
						catch(NoSuchMethodException e2)
						{
						}
						catch(SecurityException e2)
						{
						}
					}*/

					// If not found, throw original exception.
//					if(type==null)
					{
						throwParseException(e);
					}
//					else
//					{
//						this.setStaticType(SReflect.getWrappedType(type));
//					}
				}
			}
		}
	}


	/**
	 *  Evaluate the term.
	 *  @param params	The parameters (string, value).
	 *  @return	The value of the term.
	 */
	public Object	getValue(Map params)	throws Exception
	{
		// Return constant value if available.
		if(isConstant())
			return getConstantValue();

		// Get child nodes.
		int	child=0;
		ExpressionNode	type_or_value	= (ExpressionNode)jjtGetChild(child++);
		ExpressionNode	argsnode	= jjtGetNumChildren()==child ? null
			: (ExpressionNode)jjtGetChild(child);
		Object	value	= null;

		if(type==CONSTRUCTOR || type==STATIC_METHOD || type==METHOD)
		{
			// Instantiate arguments (make copy of precomputed values).
			Object[]	args	= new Object[argsnode.jjtGetNumChildren()];
			if(this.args!=null)
				System.arraycopy(this.args, 0, args, 0, args.length);
			for(int i=0; i<args.length; i++)
			{
				if(args[i]==null)
				{
					args[i]	= ((ExpressionNode)argsnode.jjtGetChild(i)).getValue(params);
				}
			}

			// Determine argument types (make copy of precomputed types).
			Class[]	argtypes	= new Class[argsnode.jjtGetNumChildren()];
			if(this.argtypes!=null)
				System.arraycopy(this.argtypes, 0, argtypes, 0, argtypes.length);
			for(int i=0; i<argtypes.length; i++)
			{
				if(argtypes[i]==null && args[i]!=null)
				{
					argtypes[i]	= args[i].getClass();
				}
			}

			// Handle constructor nodes.
			if(type==CONSTRUCTOR)
			{
				value	= invokeConstructor((Class)type_or_value
					.getValue(params), argtypes, args);
			}

			// Handle method nodes.
			else if(type==METHOD || type==STATIC_METHOD)
			{
				Object	ref	= null;
				if(type==STATIC_METHOD && clazz==null)
				{
					clazz	= (Class)type_or_value.getValue(params);
					// todo: find public superclass.
				}
				else
				{
					ref	= type_or_value.getValue(params);
					if(ref==null)
					{
						throw new RuntimeException("Cannot invoke nonstatic method on null value: "+this);
					}
					else if(clazz==null)
					{
						clazz	= ref.getClass();
						// todo: find public superclass.
					}
				}
				value	= invokeMethod(ref, clazz, argtypes, args);
			}
		}
		else if(type==FIELD || type==STATIC_FIELD)
		{
			// Get object to get the field from (if any).
			Object	ref	= type==STATIC_FIELD ? null
				: type_or_value.getValue(params);

			// Handle ".class" specially.
			if(type==STATIC_FIELD && getText().equals("class"))
			{
				value	= type_or_value.getValue(params);
			}

			// Handle ".length" of arrays specially (Java Bug???).
			else if(type==FIELD && ref!=null && ref.getClass().isArray() && getText().equals("length"))
			{
				value	= new Integer(Array.getLength(ref));
 			}

			// Handle normal fields.
			else
			{
				// Determine class.
				if(type!=STATIC_FIELD && ref==null)
				{
					throw new RuntimeException("Cannot reference nonstatic field of null value: "+this+", "+type_or_value.getValue(params));
				}
				else if(clazz==null)
				{
					clazz	= type==STATIC_FIELD ? (Class)type_or_value.getValue(params) : ref.getClass();
					// todo: find public superclass.
				}

				value	= accessField(ref, clazz);
			}
		}

		return value;
	}

	/**
	 *  Create a string representation of this node and its subnodes.
	 *  @return A string representation of this node.
	 */
	public String toPlainString()
	{
		if(type==CONSTRUCTOR)
			return "new " + jjtGetChild(0).toPlainString() + jjtGetChild(1).toPlainString();
		else if(type==METHOD || type==STATIC_METHOD)
			return jjtGetChild(0).toPlainString() + "." + getText() + jjtGetChild(1).toPlainString();
		else //if(type==FIELD || type==STATIC_FIELD)
			return jjtGetChild(0).toPlainString() + "." + getText();
	}

	//-------- helper methods --------

	/**
	 *  Find all matching constructors of a given class.
	 *  @param clazz	The class.
	 *  @param argtypes	The argument types.
	 *  @return	The matched constructors.
	 */
	protected Constructor[]	findConstructors(Class clazz, Class[] argtypes)
	{
		// Find matching signatures from available options.
		Constructor[] cs	= clazz.getConstructors();
		Class[][]	paramtypes	= new Class[cs.length][];
		for(int i=0; i<cs.length; i++)
		{
			paramtypes[i]	= cs[i].getParameterTypes();
		}
		int[]	matches	= SReflect.matchArgumentTypes(argtypes, paramtypes);

		// Store matched constructors.
		Constructor[]	constructors	= new Constructor[matches.length];
		for(int i=0; i<matches.length; i++)
		{
			constructors[i]	= cs[matches[i]];
		}

		return constructors;
	}

	/**
	 *  Find all matching methods of a given class.
	 *  @param clazz	The class.
	 *  @param argtypes	The argument types.
	 *  @return	The matched methods.
	 */
	protected Method[]	findMethods(Class clazz, Class[] argtypes)
	{
		// Find named methods.
		Method[] ms	= SReflect.getMethods(clazz, getText());
		ArrayList ames	= new ArrayList();
		for(int i=0; i<ms.length; i++)
		{
			// Matches static modifier.
			if(type!=STATIC_METHOD || Modifier.isStatic(ms[i].getModifiers()))
			{
				ames.add(ms[i]);
			}
		}
		ms	= (Method[])ames.toArray(new Method[ames.size()]);

		// Find matching signatures from available options.
		Class[][]	paramtypes	= new Class[ms.length][];
		for(int i=0; i<ms.length; i++)
		{
			paramtypes[i]	= ms[i].getParameterTypes();
		}
		int[]	matches	= SReflect.matchArgumentTypes(argtypes, paramtypes);

		// Return matched methods.
		Method[]	methods	= new Method[matches.length];
		for(int i=0; i<matches.length; i++)
		{
			methods[i]	= ms[matches[i]];
		}
		return methods;
	}

	/**
	 *  Find and invoke a constructor.
	 *  @param clazz	The class to instantiate.
	 *  @param argtypes	The actual argument types.
	 *  @param args	The actual argument values.
	 *  @return	The instantiated object.
	 * @throws Exception 
	 */
	protected Object	invokeConstructor(Class clazz, Class[] argtypes, Object[] args) throws Exception
	{
		// Reload, when class is reloadable.
		if(reloadable && Configuration.getConfiguration().isJavaCCPlanReloading())
		{
			// Remember reloaded class for next access (otherwise original class would be used next time).
			// Hack!!! Should be remembered by class loader?
			// Should update type node ???
			reloadedclass	= DynamicURLClassLoader.loadModifiedClassWithInstance(
				reloadedclass!=null ? reloadedclass : clazz);

			// On change, remove constructor cache.
			if(reloadedclass!=clazz)
			{
				clazz	= reloadedclass;
				constructors	= null;
			}
		}
		
		// Find matching signature from available options.
		Constructor	con	= null;
		if(constructors==null)
		{
			Constructor[]	constructors	= findConstructors(clazz, argtypes);
			if(constructors.length>0)
			{
				con	= constructors[0];
			}
			else
			{
				throw new ParseException("No constructor found for: "+clazz
					+SUtil.arrayToString(argtypes));
			}
		}
		else
		{
			// Match precomputed options against actual argument types.
			Class[][]	paramtypes	= new Class[constructors.length][];
			for(int i=0; i<constructors.length; i++)
			{
				paramtypes[i]	= constructors[i].getParameterTypes();
			}
			int[]	matches	= SReflect.matchArgumentTypes(argtypes, paramtypes);

			if(matches.length>0)
			{
				con	= constructors[matches[0]];
			}
			else
			{
				throw new RuntimeException("No constructor found for "+clazz
					+SUtil.arrayToString(argtypes));
			}
		}

		// Try to invoke constructor.
		Object	ret	= null;
		try
		{
			ret	= con.newInstance(args);
		}
		catch(InvocationTargetException e)
		{
			if(e.getTargetException() instanceof Exception)
				throw (Exception)e.getTargetException();
			else
				throw e;
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 *  Find and invoke a method.
	 *  @param ref	The object on which to invoke (may be null for static methods).
	 *  @param clazz	The class to instantiate.
	 *  @param argtypes	The actual argument types.
	 *  @param args	The actual argument values.
	 *  @return	The return value.
	 * @throws Exception 
	 */
	protected Object	invokeMethod(Object ref, Class clazz,
		Class[] argtypes, Object[] args) throws Exception
	{
		// Find matching signature from available options.
		Method	method	= null;
		if(methods==null)
		{
			Method[]	methods	= findMethods(clazz, argtypes);
			if(methods.length>0)
			{
				method	= methods[0];
			}
			else
			{
				throw new ParseException("No method found for term "+this+": " + clazz
					+ " " + getText() + SUtil.arrayToString(argtypes));
			}
		}
		else
		{
			// Match precomputed options against actual argument types.
			Class[][]	paramtypes	= new Class[methods.length][];
			for(int i=0; i<methods.length; i++)
			{
				paramtypes[i]	= methods[i].getParameterTypes();
			}
			int[]	matches	= SReflect.matchArgumentTypes(argtypes, paramtypes);

			if(matches.length>0)
			{
				method	= methods[matches[0]];
			}
			else
			{
				throw new ParseException("No method found for: " + clazz
					+ " " + getText() + SUtil.arrayToString(argtypes));
			}
		}

		Method	invmeth	= getMethodForMethod(method);
		if(invmeth==null)
		{
			throw new ParseException("Method '"+method.getName()+"' declared on nonpublic class.");
		}
		
		// Try to invoke method.
		Object	ret	= null;
		try
		{
			ret	= invmeth.invoke(ref, args);
		}
		catch(InvocationTargetException e)
		{
			if(e.getTargetException() instanceof Exception)
				throw (Exception)e.getTargetException();
			else
				throw e;
		}
		return ret;
	}

	/**
	 *  Access a field.
	 *  Also tries bean property accessor methods.
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 */
	protected Object	accessField(Object ref, Class clazz) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException
	{
		boolean	found	= false;
		Object val = null;

		// Special access for beliefbase and parameter elements.
		if(ref instanceof RBeliefbase)
		{
			RBeliefbase	bb	= (RBeliefbase)ref;
			if(bb.containsBelief(getText()))
			{
				val	= bb.getBelief(getText()).getFact();
				found	= true;
			}
			else if(bb.containsBeliefSet(getText()))
			{
				val	= bb.getBeliefSet(getText()).getFacts();
				found	= true;
			}
		}
		else if(ref instanceof IRParameterElement)
		{
			IRParameterElement	pe	= (IRParameterElement)ref;
			if(pe.hasParameter(getText()))
			{
				val	= pe.getParameter(getText()).getValue();
				found	= true;
			}
			else if(pe.hasParameterSet(getText()))
			{
				val	= pe.getParameterSet(getText()).getValues();
				found	= true;
			}
		}

		// Find field if not precomputed.
		Field	field0	= this.field;
		if(!found && field0==null)
		{
//			try
//			{
				//field0	= clazz.getField(getText());
				field0	= SReflect.getCachedField(clazz, getText());

				// Check if static modifier matches.
				if(type==STATIC_FIELD &&!Modifier.isStatic(field0.getModifiers()))
				{
					throw new RuntimeException("Static reference to nonstatic field :"+this);
				}
//			}
//			catch(NoSuchFieldException e)
//			{
//				throwEvaluationException(e);				
//			}
		}

		// Try to access field (if found).
		if(!found && field0!=null)
		{
//			try
//			{
				val	= field0.get(ref);
				found	= true;
//			}
//			catch(IllegalAccessException e)
//			{
//				throwEvaluationException(e);
//			}
		}

		assert found;
		
		// Try map interface.
		/*else if(SReflect.isSupertype(Map.class, clazz))
		{
			try
			{
				accessor0	= clazz.getMethod("get", new Class[]{Object.class});
				args0	= new Object[]{getText()};
			}
			catch(NoSuchMethodException e){}	// Shouldn't happen
			catch(SecurityException e){}	// Shouldn't happen
		}*/


		// Try bean property
//		else
//		{
//			try
//			{
//				val = BeanHelper.getPropertyValue(ref, getText());
//			}
//			catch(Exception e)
//			{
//				// Throw original NoSuchFieldException.
//				throwEvaluationException(e);
//			}
//		}
		return val;
	}


		// Try to find accessor method.
		/*if(field0==null && accessor0==null)
		{
			try
			{
				String	name	= "get" + getText().substring(0,1).toUpperCase()
					+ ((getText().length()>1) ? getText().substring(1) : "");
				accessor0	= clazz.getMethod(name, new Class[0]);
				args0	= new Object[0];
			}
			catch(NoSuchMethodException e){}
			catch(SecurityException e){}
		}*/


		// If not found, throw original exception.
		/*if(field0==null && accessor0==null)
		{
			StringWriter	sw = new StringWriter();
			nosuchfield.printStackTrace(new PrintWriter(sw));
			throw new RuntimeException(""+sw);
		}

		// Read field.
		Object value	= null;
		try
		{
			if(accessor0==null)
			{
				value	= field0.get(ref);
			}
			else
			{
				value	= accessor0.invoke(ref, args0);
			}
		}
		catch(IllegalAccessException e)
		{
			StringWriter	sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new RuntimeException(""+sw);
		}
		catch(IllegalArgumentException e)
		{
			StringWriter	sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new RuntimeException(""+sw);
		}
		catch(InvocationTargetException e)
		{
			if(e.getTargetException() instanceof RuntimeException)
			{
				throw (RuntimeException)e.getTargetException();
			}
			else
			{
				StringWriter	sw = new StringWriter();
				e.getTargetException().printStackTrace(new PrintWriter(sw));
				throw new RuntimeException(""+sw);
			}
		}
		return value;
	}*/
	
	/**
	 *  Find method declared in public class for a given method. 
	 */
	protected Method	getMethodForMethod(Method method)
	{
		Class	clazz	= method.getDeclaringClass();
		if((clazz.getModifiers() & Modifier.PUBLIC)==0)
		{
			List	classes	= new ArrayList();
			if(clazz.getSuperclass()!=null)
				classes.add(clazz.getSuperclass());
			classes.addAll(Arrays.asList(clazz.getInterfaces()));
			Method	meth	= null;
			while(meth==null && classes.size()>0)
			{
				Class	testclass	= (Class)classes.remove(0);
				try
				{
					if((testclass.getModifiers() & Modifier.PUBLIC)!=0)
					{
						meth	= testclass.getMethod(method.getName(), method.getParameterTypes());
					}
				}
				catch(Exception e)
				{
				}
				
				if(meth==null)
				{
					if(testclass.getSuperclass()!=null)
						classes.add(testclass.getSuperclass());
					classes.addAll(Arrays.asList(testclass.getInterfaces()));
				}
			}
			
			method	= meth;
		}
		return method;
	}
	
	//-------- deserialization handling --------

	/**
	 *  After deserialization do a precompile, because
	 *  the reflect objects (method, constructor etc.)
	 *  created in the precompile process are not serializable.
	 * /
	private void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		// Read this object from stream.
		in.defaultReadObject();

		// Precompile node to initialize reflection objects.
		precompile();
	}*/
}
