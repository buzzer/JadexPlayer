package jadex.tools.common;

import java.util.*;
import java.io.*;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.*;
import jadex.util.SReflect;

/**
 *  Create a html string for the element.
 */
public class ElementHTMLGenerator implements IRepresentationConverter
{
	//-------- attributes --------

	/** The template. */
	protected Template template;

	/** The context. */
	protected VelocityContext context;

	//-------- constructors ---------

	/**
	 *  Create a new generator.
	 */
	public ElementHTMLGenerator()
	{
		try
		{
			Properties p = new Properties();
			p.setProperty("resource.loader", "file, class");
			p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
			p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			//p.setProperty("file.resource.loader.path", "./jadex/tools/pdt2adf/helpers/templates");
			Velocity.init(p);

			this.context = new VelocityContext();
			context.put("sreflect", new SReflect());

			this.template = Velocity.getTemplate("jadex/tools/common/element.vm");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//-------- methods ---------

	/**
	 *  Convert the map of attributes to a formatted string.
	 */
	public String convert(Map element)
	{
		String ret = null;

		try
		{
			context.put("element", element);
			context.put("recursion", new HashMap());
			StringWriter sw = new StringWriter();
			template.merge(context, sw);
			ret = sw.toString();
/*			FileWriter fw = new FileWriter("test.html");
			fw.write(ret);
			fw.close();
*/		}
		catch(Exception e)
		{
			System.out.println(e);
		}

		return ret;
	}

	/**
	 *  Main for testing.
	 */
	public static void main(String[] args)
	{
		HashMap element = new HashMap();
		HashMap ielement = new HashMap();
		HashMap iielement = new HashMap();

		iielement.put("ii1", "ii1_val");
		iielement.put("isencodeablepresentation", "true");

		ielement.put("inner", iielement);
		ielement.put("name", "innername_val");
		ielement.put("class", "innerclass_val");
		ielement.put("ia", "ia_val");
		ielement.put("isencodeablepresentation", "true");

		element.put("inner", ielement);
		element.put("name", "name_val");
		element.put("class", "class_val");
		element.put("a", "a_val");

		ElementHTMLGenerator gen = new ElementHTMLGenerator();

		gen.convert(element);
	}

}
