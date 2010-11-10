package jadex.parser;

import jadex.config.Configuration;
import jadex.util.SReflect;
import jadex.util.SUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *  Static class for creating parsers (factory) and testing it.
 */
public class SParser
{
	//-------- constants --------

	// todo: move constans to parsers, use reflection for creating parsers?!

	/** The jbind factory name. */
	public static final String PARSER_JAVACC = "jadex.parser.javaccimpl.Parser";

	/** The jibx factory name. */
	public static final String PARSER_JANINO = "jadex.parser.janinoimpl.Parser";

	//-------- attributes --------

	/** The current parser factory in use. */
	protected static IParserFactory	factory;
	
	/** The own parser. */
	protected static IParser parser;

	//-------- initializers --------

	/**
	 *  Create a new parser of defined type.
	 *  @param imports The imports.
	 *  @return The parser.
	 */
	public static IParser	createParser(String[] imports)
	{
		return getFactory().createParser(imports);
	}

	/**
	 *  Create a new parser of defined type.
	 *  @param imports The imports.
	 *  @return The parser.
	 */
	public static IParser	createParser(String[] imports, String filename, long lastmodified)
	{
		return getFactory().createParser(imports, filename, lastmodified);
	}

	/**
	 *  Get the factory.
	 */
	protected static IParserFactory	getFactory()
	{
		String	factoryname	= Configuration.getConfiguration().getParserName();
		if(factory==null || !factory.getClass().getName().equals(factoryname))
		{
			try
			{
				factory	= (IParserFactory)SReflect.findClass(factoryname, null).newInstance();
			}
			catch(Exception e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new RuntimeException(sw.toString());
			}
		}
		return factory;
	}
	
	/**
	 *  Evaluate an expression directly.
	 *  @param exp The expression text.
	 *  @param imports The imports.
	 *  @param params The parameters.
	 *  @return The value.
	 *  @throws Exception on parse or evaluation error.
	 */
	public static Object evaluateExpression(String exp, String[] imports, Map params) throws Exception
	{
		if(parser==null)
			parser = SParser.createParser(imports);
		ITerm term = parser.parseExpression(exp, 
			params!=null? SUtil.arrayToList(params.keySet().toArray()): null);
		return term.getValue(params);
	}
	
	//-------- main for testing --------

	/**
	 *  Main method for testing parsers.
	 *  Invoke with expressions to parse and evaluate.
	 *  When invoked with no arguments runs tests specified
	 *  in TestExpressions.properties.
	 */
	public static void main(String[] args)	throws Exception
	{
		Configuration.setFallbackConfiguration("jadex/config/batch_conf.properties");
		
		IParser	parser	= SParser.createParser(null);

		//Object o =	parser.parseExpression("new java.lang.String[]{\"a\", \"b\"}", null);

		if(args.length>0)
		{
			for(int i=0; i<args.length; i++)
			{
				System.out.println("\n"+args[i]);
				Map	params	= new HashMap();
				params.put("$param", String.class);
				//ITerm term = parser.parseExpression(args[i], params, null);
				ITerm term = parser.parseExpression(args[i], null);
				//term.dump("", System.out);
				Object	retval	= term.getValue(null);
				System.out.println("Result: "+retval);
			}
		}
		else
		{
			// Load tests from properties.
			Properties props	= new Properties();
			props.load(SParser.class.getResourceAsStream("TestExpressions.properties"));

			String imports	= props.getProperty("imports");
			if(imports!=null)
			{
				props.remove("imports");
				ArrayList	list	= new ArrayList();
				StringTokenizer	stok	= new StringTokenizer(imports, ", ");
				while(stok.hasMoreTokens())
					list.add(stok.nextToken().trim());
				parser	= SParser.createParser((String[])list.toArray(new String[list.size()]));
			}

			String parameters	= props.getProperty("parameters");
			Map	params	= new HashMap();
			Map	paramtypes	= new HashMap();
			if(parameters!=null)
			{
				props.remove("parameters");
				StringTokenizer	stok	= new StringTokenizer(parameters, ",");
				while(stok.hasMoreTokens())
				{
					String	param	= stok.nextToken().trim();
					String	value	= props.getProperty(param);
					params.put(param, parser.parseExpression(value, null).getValue(params));
					if(params.get(param)!=null)
						paramtypes.put(param, params.get(param).getClass());
					props.remove(param);
				}
			}
			int passed	= 0;
			int failed	= 0;

			for(Enumeration	keys=props.keys(); keys.hasMoreElements(); )
			{
				// Read expression from properties.
				String	exp	= (String)keys.nextElement();
				/*System.out.println("--->"+exp);
				if(exp.startsWith("$_false||"))
					System.out.println("here");*/

				//System.out.println(exp);
				String	result	= props.getProperty(exp);
				int	sep	= result.lastIndexOf(":");
				String	value	= result.substring(0, sep).trim();
				String	type	= result.substring(sep+1).trim();

				// Constant node expected.
				boolean	constant	= false;
				if(type.startsWith("c"))
				{
					constant	= true;
					type	= type.substring(1).trim();
				}
				// Parse exception expected.
				boolean	parsex	= false;
				if(type.startsWith("p"))
				{
					parsex	= true;
					type	= type.substring(1).trim();
				}
				// Evaluation exception expected.
				boolean	evalex	= false;
				if(type.startsWith("e"))
				{
					evalex	= true;
					type	= type.substring(1).trim();
				}

				// Try to parse.
				boolean fail	= false;
				ITerm node	= null;
				try
				{
					//node = parser.parseExpression(exp, paramtypes, null);
					node = parser.parseExpression(exp, null);
					System.out.println(node);
					if(parsex)
					{
						System.err.println(node);
						System.err.println("Parse: Fail (expected exception: "+type+")");
						fail	= true;
					}
					/*else if(constant && !node.isConstant())
					{
						System.err.println(node);
						System.err.println("Parse: Fail (expected constant node)");
						fail	= true;
					}
					else if(!constant && node.isConstant())
					{
						System.err.println(node);
						System.err.println("Parse: Fail (unexpected constant node)");
						fail	= true;
					}*/
				}
				catch(Throwable e)
				{
					if(!parsex)
					{
						System.err.println("Parse: Fail (unexpected exception)");
						e.printStackTrace();
						fail	= true;
					}
					else if(!e.toString().startsWith(type))
					{
						System.err.println("Parse: Fail (expected exception: "+type+", but was "+e+")");
						e.printStackTrace();
						fail	= true;
					}
				}

				// Try to evaluate.
				Object	retval	= null;
				if(!fail && !parsex)
				{
					try
					{
						retval	= node.getValue(params);
						if(evalex)
						{
							System.err.println(node);
							System.err.println("Eval: Fail (expected exception: "+type+")");
							fail	= true;
						}
					}
					catch(Exception e)
					{
						if(!evalex)
						{
							System.err.println(node);
							System.err.println("Eval: Fail (unexpected exception)");
							e.printStackTrace();
							fail	= true;
						}
						else if(!e.toString().startsWith(type))
						{
							System.err.println(node);
							System.err.println("Eval: Fail (expected exception: "+type+", but was "+e+")");
							e.printStackTrace();
							fail	= true;
						}
					}
				}

				// Match return value.
				if(!fail && !parsex && !evalex)
				{
					String	rets	= ""+retval;
					if(!rets.startsWith(value))
					{
						System.err.println(node);
						System.err.println("Value: Fail (was "+rets+", should be "+value+")");
						fail	= true;
					}
					/*Class	clazz	= node.getStaticType();
					if(clazz==null && retval!=null)
					{
						clazz	= retval.getClass();
					}
					if(clazz==null && !type.equals("null")
						|| clazz!=null && !clazz.getName().equals(type))
					{
						System.err.println(node);
						System.err.println("Type: Fail (was "
							+ clazz	+ ", should be "+type+")");
						fail	= true;
					}*/
				}

				if(fail)
				{
					failed++;
					System.err.println("Failed expression: "+exp);
					/*if(node!=null)
					{
						node.dump("", System.err);
					}*/
					System.err.println("\n");
				}
				else
				{
					passed++;
				}
			}

			if(failed==0)
			{
				System.out.println("\nAll "+passed+" tests completed.");
			}
			else
			{
				System.out.println("\nFailed "+failed+" tests of "+(passed+failed)+".");
			}
		}
	}
}
