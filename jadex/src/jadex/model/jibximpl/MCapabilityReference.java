package jadex.model.jibximpl;

import java.io.*;

import jadex.model.*;
import jadex.config.Configuration;

/**
 *  A capability reference stores the link to the referenced capability,
 *  its local name and the belief bindings.
 */
public class MCapabilityReference extends MElement implements IMCapabilityReference
{
	//-------- xml attributes --------

	/** The file name. */
	protected String file;

	//-------- attributes --------

	/** The referenced contained capability. */
	protected IMCapability	inner;

	/** The referenced contained capability loaded for checking. */
	protected IMCapability	check;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 *  Overwritten in elements that need a setup.
	 */
	protected void init()
	{
		super.init();
		
		// Test to see if check-cleanup works.
		assert check==null : this;
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		if(getFile()==null)
		{
			report.addEntry(this, "A 'file' has to be specified.");
		}
		else
		{
			IMCapability	cap	= getCapability();
			if(cap==null)
			{
				report.addEntry(this, "Cannot load capability file '"+getFile()+"'.");
			}
			else
			{
				IReport	rep2	= cap.getReport();
				if(!rep2.isEmpty())
				{
					report.addEntry(this, "Loaded capability <a href=\"#"+cap.getFullName()+"\">"+cap.getFullName()+"</a> has errors.");
					report.addDocument(cap.getFullName(), rep2.toHTMLString());
					// hack???
					report.getDocuments().putAll(rep2.getDocuments());
				}
			}
		}
	}
	
	/**
	 *  Perform cleanup after checking this element.
	 */
	protected void doCheckCleanup()
	{
		super.doCheckCleanup();
		
		this.check	= null;
	}

	//-------- xml methods --------

	/**
	 *  Get the file name.
	 *  @return The file name.
	 */
	public String getFile()
	{
		return this.file;
	}

	/**
	 *  Set the file name.
	 *  @param file The file name.
	 */
	public void setFile(String file)
	{
		this.file = file;
	}

	//-------- methods --------

	/**
	 *  Get the referenced capability.
	 *  @return The referenced capability.
	 */
	public IMCapability getCapability()
	{
		if(isChecking())
		{
			if(check==null)
			{
				check	= loadCapability();
			}
		}
		else
		{
			if(inner==null)
			{
				inner	= loadCapability();
				// Todo: report error when inner==null (checking might be disabled).
			}
		}
		return isChecking() ? check : inner;
	}

	/**
	 *  Load the specified capability.
	 */
	protected IMCapability	loadCapability()
	{
		// Search the capability in the properties.
		String	capaname	= "capability."+getFile();

		String	ref = Configuration.getConfiguration().getProperty(capaname);
		if(ref==null)
		{
			// Use file name directly.
			// Also allows to load agents/capabilities without platform (e.g. for jadexdoc).
			ref	= getFile();
		}

		IMCapability ret	= null;
		try
		{
			ret = SXML.loadCapabilityModel(ref, getScope().getFullImports(), getScope());
		}
		catch(IOException e)
		{
			//e.printStackTrace();
			// Ignore, will be reported by doCheck().
		}
		return ret;
	}

	//-------- helper methods --------

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MCapabilityReference clone = (MCapabilityReference)cl;
		if(inner!=null)
			clone.inner = (IMCapability)((MCapability)inner).clone();
	}
}
