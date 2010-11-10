package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  The agent model element is the java representation
 *  of a agent description (e.g. from the xml definition).
 */
public class MBDIAgent extends MCapability implements IMBDIAgent
{
	//-------- xml attributes --------

	/** The property file name. */
	protected String propertyfile	= "jadex.config.runtime";
	
	/** The arguments. */
	protected ArrayList arguments = SCollection.createArrayList();


	//-------- constructors --------
	
	/**
	 *  Called when the element is setup.
	 *  Overwritten in elements that need a setup.
	 */
	protected void init()
	{
		super.init();
		
		if(propertyfile!=null)
		{
			try
			{
				((MPropertybase)getPropertybase()).addPropertyBase(SXML.loadPropertiesModel(propertyfile, getFullImports(), this));
			}
			catch(IOException e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				throw new RuntimeException(sw.toString());
			}
		}
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		if(propertyfile!=null)
		{
			try
			{
				SXML.loadPropertiesModel(propertyfile, getFullImports(), this);
			}
			catch(Exception e)
			{
				report.addEntry(this, "Could not load property file '"+propertyfile+"'.");
			}
		}
		
		if(isAbstract())
		{
			report.addEntry(this, "Agents can't be abstract.");
		}
	}

	/**
	 *  The extension for files of this type (e.g. ".agent.xml").
	 */
	protected String	getFileExtension()
	{
		return SXML.FILE_EXTENSION_AGENT;
	}

	//-------- xml methods --------

	/**
	 *  Get the property file name.
	 *  @return The property file name.
	 */
	public String getPropertyFile()
	{
		return this.propertyfile;
	}

	/**
	 *  Set the property file name.
	 *  @param propertyfile The property file.
	 */
	public void setPropertyFile(String propertyfile)
	{
		this.propertyfile = propertyfile;
	}

	/**
	 *  Get the expression parameters.
	 */
	public List	getSystemExpressionParameters()
	{
		// Avoid connection to owner.
		List copy = super.getSystemExpressionParameters();
		copy.add(0, new ExpressionParameterInfo("$agent", this, "jadex.runtime.impl.RBDIAgent"));
		//copy.add(0, new ExpressionParameterInfo("$args", null, Object[].class));
		return copy;
	}
	
	/**
	 *  Get all argument names.
	 *  @return An array of the arguments.
	 */
	public String[]	getArgumentNames()
	{
		List al = SCollection.createArrayList();
		
		IMBelief[] bels = getBeliefbase().getBeliefs();
		for(int i=0; i<bels.length; i++)
		{
			if(bels[i].getExported().equals(IMTypedElement.EXPORTED_TRUE))
			{
				al.add(bels[i].getName());
			}
		}
		IMBeliefReference[] belrefs = getBeliefbase().getBeliefReferences();
		for(int i=0; i<belrefs.length; i++)
		{
			if(belrefs[i].getExported().equals(IMTypedElement.EXPORTED_TRUE))
			{
				al.add(belrefs[i].getName());
			}
		}
		
		return (String[])al.toArray(new String[al.size()]);
	}
	
	/**
	 *  Check if the given arguments match the beliefs in the model. 
	 */
	public void checkArguments(Map arguments)
	{
		if(arguments.size()>0)
		{
			for(Iterator it=arguments.keySet().iterator(); it.hasNext(); )
			{
				String	arg	= (String)it.next();
				if(getBeliefbase().getBelief(arg)!=null)
				{
					IMBelief	bel	= getBeliefbase().getBelief(arg);
					if(IMReferenceableElement.EXPORTED_TRUE.equals(bel.getExported()))
					{
						if(bel.getClazz()!=null && arguments.get(arg)!=null)
						{
							if(!SReflect.isSupertype(bel.getClazz(), arguments.get(arg).getClass()))
							{
								throw new RuntimeException("Belief for argument '"+arg+"' does not match type ("+bel.getClazz().getName()+" vs. "+arguments.get(arg).getClass().getName()+").");
							}
						}
					}
					else
					{
						throw new RuntimeException("Belief for argument '"+arg+"' is not exported.");
					}
				}
				else if(getBeliefbase().getBeliefReference(arg)!=null)
				{
					IMBeliefReference	bel	= getBeliefbase().getBeliefReference(arg);
					if(IMReferenceableElement.EXPORTED_TRUE.equals(bel.getExported()))
					{
						if(bel.getClazz()!=null && arguments.get(arg)!=null)
						{
							if(!SReflect.isSupertype(bel.getClazz(), arguments.get(arg).getClass()))
							{
								throw new RuntimeException("Belief for argument '"+arg+"' does not match type ("+bel.getClazz().getName()+" vs. "+arguments.get(arg).getClass().getName()+").");
							}
						}
					}
					else
					{
						throw new RuntimeException("Belief for argument '"+arg+"' is not exported.");
					}
				}
				else
				{
					throw new RuntimeException("No such belief for argument '"+arg+"'.");
				}
			}
		}
	}
}
