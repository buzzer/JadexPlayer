package jadex.model.jibximpl;

import jadex.model.*;
import jadex.parser.IParser;
import jadex.parser.SParser;
import jadex.util.DynamicURLClassLoader;
import jadex.util.collection.SCollection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  The capability model element is the java representation
 *  of a capability description (e.g. from the xml definition).
 */
public class MCapability extends MElement implements IMCapability
{
	//-------- xml attributes --------

	/** The package name. */
	protected String packagename;

	/** The abstract flag. */
	protected boolean isabstract	= false;

	/** The beliefbase. */
	protected MBeliefbase beliefbase;

	/** The goalbase. */
	protected MGoalbase goalbase;

	/** The planbase. */
	protected MPlanbase planbase;

	/** The eventbase. */
	protected MEventbase eventbase;

	/** The expressionbase. */
	protected MExpressionbase expressionbase;

	/** The propertybase. */
	protected MPropertybase propertybase;

	/** The configurationbase. */
	protected MConfigurationbase configurationbase;

	/** The included imports. */
	protected ArrayList imports;

	/** The included capabilities. */
	protected ArrayList capabilityrefs;

	//-------- attributes --------

	/** The parser. */
	protected transient IParser parser;

	/** The filename. */
	protected String filename;

	/** The last modified date. */
	protected long lastmodified;

	//--------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(capabilityrefs!=null)
			ret.addAll(capabilityrefs);	// Have to be inited first!!!

		// Todo: should bases be always present?
		if(beliefbase!=null)
			ret.add(beliefbase);
		if(goalbase!=null)
			ret.add(goalbase);
		if(planbase!=null)
			ret.add(planbase);
		if(eventbase!=null)
			ret.add(eventbase);
		if(expressionbase!=null)
			ret.add(expressionbase);
		if(propertybase!=null)
			ret.add(propertybase);
		if(configurationbase!=null)
			ret.add(configurationbase);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if name has been set.
		if(getName()==null)
		{
			report.addEntry(this, "Element requires a name.");
		}
		// Check if capability filename equals model name.
		else
		{
			String	expected	= getName()+getFileExtension();
			boolean	match	= filename.endsWith(expected);
	
			// Also detect "Copy of My.agent.xml"
			if(match && filename.length()>expected.length())
			{
				// Check if character before name is separator char.
				// Also check for '/' as filenames may contain platform independent string (e.g. URLs).
				char	prefix	= filename.charAt(filename.length()-expected.length()-1);
				match	= prefix=='/' || prefix==File.separatorChar;
			}
			
			if(!match)
				report.addEntry(this, "Name conflict between model name '"+getName()+"' and filename '"+filename+"'");
		}

		// Check filename/package correspondency.
		String	actpkg	= DynamicURLClassLoader.getPackageOfFileFromInstance(filename);
		// Ignore test, when actpkg==null, because DynamicURLClassLoader might not be available (e.g. in Webstart)
		if(actpkg!=null && !actpkg.equals(packagename==null?"":packagename))
		{
			report.addEntry(this, "File is contained in package '"
					+ (actpkg.equals("")?"default":actpkg) + "' but declared in '"
					+ (packagename==null||packagename.equals("")?"default":packagename) + "'."
					+ "Maybe, this error is not caused by the file itself, but due to adding the wrong (sub-)directory to the project tree or classpath.");
		}

		// Check if names of capability references are unique.
		MBase.checkNameUniqueness(this, report, getCapabilityReferences());
	}

	/**
	 *  The extension for files of this type (e.g. ".agent.xml").
	 */
	protected String	getFileExtension()
	{
		return SXML.FILE_EXTENSION_CAPABILITY;
	}

	//--------- package --------

	/**
	 *  Get the package.
	 *  @return The package.
	 */
	public String getPackage()
	{
		return this.packagename;
	}

	/**
	 *  Set the package.
	 *  @param _package The package.
	 */
	public void setPackage(String _package)
	{
		this.packagename = _package;
	}

	//-------- import methods --------

	/**
	 *  Get the import declarations.
	 *  @return The import statements.
	 */
	public String[] getImports()
	{
		if(imports==null)
			return new String[0];
		return (String[])imports.toArray(new String[imports.size()]);
	}

	/**
	 *  Create an import declaration.
	 *  @param exp The import statement.
	 */
	public void	createImport(String exp)
	{
		if(imports==null)
			imports = SCollection.createArrayList();
		imports.add(exp);
		
		// Reset parser (will be created on next access).
		this.parser	= null;
	}

	/**
	 *  Delete an import declaration.
	 *  @param exp The import statement.
	 */
	public void	deleteImport(String exp)
	{
		if(!imports.remove(exp))
			throw new RuntimeException("Could not find in imports: "+exp);
	}


	//-------- capability references --------

	/**
	 *  Get all capability references.
	 *  @return The capability references.
	 */
	public IMCapabilityReference[] getCapabilityReferences()
	{
		if(capabilityrefs==null)
			return new IMCapabilityReference[0];
		return (IMCapabilityReference[])capabilityrefs.toArray(new IMCapabilityReference[capabilityrefs.size()]);
	}

	/**
	 *  Get a capability reference.
	 *  @param name The capability reference name.
	 *  @return The capability reference.
	 */
	public IMCapabilityReference getCapabilityReference(String name)
	{
		IMCapabilityReference ret = null;

		for(int i=0; capabilityrefs!=null && i<capabilityrefs.size(); i++)
		{
			IMCapabilityReference tmp = (IMCapabilityReference)capabilityrefs.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}

		return ret;
	}

	/**
	 *  Create a capability reference.
	 *  @param name	The capability reference name.
	 *  @param file	The file or identifier of the referenced capability.
	 *  @return The capability reference.
	 */
	public IMCapabilityReference createCapabilityReference(String name, String file)
	{
		if(capabilityrefs==null)
			capabilityrefs = SCollection.createArrayList();

		MCapabilityReference ret = new MCapabilityReference();
		ret.setName(name);
		ret.setFile(file);
		ret.setOwner(this);
		ret.init();
		capabilityrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a capability reference.
	 *  @param reference	The capability reference.
	 */
	public void	deleteCapabilityReference(IMCapabilityReference reference)
	{
		if(!capabilityrefs.remove(reference))
			throw new RuntimeException("Could not find capability reference: "+reference);
	}

	//-------- abstract --------

	/**
	 *  Test if the capability is abstract.
	 *  @return True, if abstract.
	 */
	public boolean isAbstract()
	{
		return this.isabstract;
	}

	/**
	 *  Set the abstract state.
	 *  @param isabstract
	 */
	public void setAbstract(boolean isabstract)
	{
		this.isabstract = isabstract;
	}


	//-------- bases --------

	/**
	 *  Get the belief base.
	 *  @return The belief base.
	 */
	public IMBeliefbase getBeliefbase()
	{
		if(beliefbase==null)
		{
			beliefbase = new MBeliefbase();
			beliefbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				beliefbase.init();
		}
		else if(inited && !beliefbase.inited) // todo: remove this hack, use isChecking()?
			beliefbase.init();
		return beliefbase;
	}

	/**
	 *  Get the goal base.
	 *  @return The goal base.
	 */
	public IMGoalbase getGoalbase()
	{
		if(goalbase==null)
		{
			goalbase = new MGoalbase();
			goalbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				goalbase.init();
		}
		else if(inited && !goalbase.inited) // todo: remove this hack, use isChecking()?
			goalbase.init();

		return goalbase;
	}

	/**
	 *  Get the plan base.
	 *  @return The plan base.
	 */
	public IMPlanbase getPlanbase()
	{
		if(planbase==null)
		{
			planbase = new MPlanbase();
			planbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				planbase.init();
		}
		else if(inited && !planbase.inited) // todo: remove this hack, use isChecking()?
			planbase.init();
		return planbase;
	}

	/**
	 *  Get the event base.
	 *  @return The event base.
	 */
	public IMEventbase getEventbase()
	{
		if(eventbase==null)
		{
			eventbase = new MEventbase();
			eventbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				eventbase.init();
		}
		else if(inited && !eventbase.inited) // todo: remove this hack, use isChecking()?
			eventbase.init();
		return eventbase;
	}

	/**
	 *  Get the expression base.
	 *  @return The expression base.
	 */
	public IMExpressionbase getExpressionbase()
	{
		if(expressionbase==null)
		{
			expressionbase = new MExpressionbase();
			expressionbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				expressionbase.init();
		}
		else if(inited && !expressionbase.inited) // todo: remove this hack, use isChecking()?
			expressionbase.init();
		return expressionbase;
	}

	/**
	 *  Get the property base.
	 *  @return The property base.
	 */
	public IMPropertybase getPropertybase()
	{
		if(propertybase==null)
		{
			propertybase = new MPropertybase();
			propertybase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				propertybase.init();
		}
		else if(inited && !propertybase.inited) // todo: remove this hack, use isChecking()?
			propertybase.init();
		return propertybase;
	}

	/**
	 *  Get the configuration base.
	 *  @return The configuration base.
	 */
	public IMConfigurationbase getConfigurationbase()
	{
		if(configurationbase==null)
		{
			configurationbase = new MConfigurationbase();
			configurationbase.setOwner(this);
			if(inited) // todo: remove this hack, use isChecking()?
				configurationbase.init();
		}
		else if(inited && !configurationbase.inited) // todo: remove this hack, use isChecking()?
			configurationbase.init();
		return configurationbase;
	}

	//-------- attributes --------



	//-------- methods --------

	/**
	 *  Check if checking is in progress.
	 */
	protected boolean	isChecking()
	{
		// Overridden to avoid propagation to owner.
		return checking;
	}
	
	/**
	 *  Get all import declarations (including package).
	 *  @return The import statements.
	 */
	public String[] getFullImports()
	{
		String[] ret = imports==null ? null : (String[])imports.toArray(new String[imports.size()]);

		// Add package to imports.
		if(getPackage()!=null)
		{
			if(ret!=null)
			{
				String[]	tmp	= new String[ret.length+1];
				tmp[0]	= getPackage()+".*";
				System.arraycopy(ret, 0, tmp, 1, ret.length);
				ret	= tmp;
			}
			else
			{
				ret	= new String[]{getPackage()+".*"};
			}
		}

		return ret;
	}

	/**
	 *  Get the parser for this document.
	 *  @return The parser.
	 */
	public IParser getParser()
	{
		if(parser==null)
		{
			// Assumes that the filename ends with xml todo: check?
			String cachename = getFilename().substring(0, getFilename().length()-3)+"prec";
			if(getFilename().startsWith("jar:") || getFilename().startsWith("http:")
				|| getFilename().startsWith("ftp:"))
			{
				try
				{
					// Test if url exists. Otherwise modify cache name to local directory.
					new URL(filename).openConnection();
				}
				catch(IOException e)
				{
					File f = new File(".");
					String ft = getFilename().endsWith("agent.xml")? "agent": "capability"; // Hack!
					cachename = f.getAbsolutePath()+File.separator+getFullName()+ft+".prec";
				}
			}
			this.parser = SParser.createParser(getFullImports(), cachename, getLastModified());
		}
		return this.parser;
	}


	/**
	 *  Get the filename.
	 *  @return The file name.
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * Set the filename.
	 * @param filename The file name.
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
//		// todo: check exactly if <separatorChar>TypeName(.agent.xml | .capability.xml)
//		if(filename.indexOf(getName())==-1)
//		{
//			String short_name = filename;
//
//			int namestart = short_name.lastIndexOf(File.separatorChar);
//			if(namestart!=-1)
//				short_name = short_name.substring(namestart+1);
//
//			short_name = SXML.stripJadexExtension(short_name);
//
//			//System.out.println("short: "+short_name);
//			System.out.println("Agent resp capability type name does not correspond to filename:"
//				+filename+" ("+getName()+"), adjusting type name to: "+short_name);
//			//if(short_name.equals(filename))
//			//	System.out.println("Name format not valid, use .capability.xml | .agent.xml: "+filename);
//
//			setName(short_name);
//		}
	}

	/**
	 *  Get the last modified date.
	 *  @return The last modified date.
	 */
	public long getLastModified()
	{
		return lastmodified;
	}

	/**
	 *  Set the last modified date.
	 *  @param lastmodified The last modified date.
	 */
	public void setLastModified(long lastmodified)
	{
		this.lastmodified = lastmodified;
	}

	/**
	 *  Get the fully qualified name package+"."+typename of a capability.
	 *  @return The fully qualified name.
	 */
	public String getFullName()
	{
		return getPackage()!=null && getPackage().length()>0? getPackage()+"."+getName(): getName();
	}

	/**
	 *  Get an iterator for the imports.
	 *  @return An iterator for the imports.
	 */
	public Iterator iterImports()
	{
		return imports==null? Collections.EMPTY_LIST.iterator(): imports.iterator();
	}

	/**
	 *  Add a capability ref.
	 *  @param ref The capability ref.
	 */
	public void addCapabilityReference(IMCapabilityReference ref)
	{
		if(capabilityrefs==null)
			capabilityrefs = SCollection.createArrayList();
		capabilityrefs.add(ref);
	}

	/**
	 *  Get an iterator for the capability refs.
	 *  @return An iterator for the capability refs.
	 */
	public Iterator iterCapabilityReferences()
	{
		return capabilityrefs==null? Collections.EMPTY_LIST.iterator(): capabilityrefs.iterator();
	}

	/**
	 *  Get the expression parameters.
	 */
	public List	getSystemExpressionParameters()
	{
		// Avoid connection to owner.
		List copy = SCollection.createArrayList();
		copy.add(new ExpressionParameterInfo("$scope", this, "jadex.runtime.impl.RCapability"));
		copy.add(new ExpressionParameterInfo("$beliefbase", getBeliefbase(), "jadex.runtime.impl.RBeliefbase"));
		copy.add(new ExpressionParameterInfo("$goalbase", getGoalbase(), "jadex.runtime.impl.RGoalbase"));
		copy.add(new ExpressionParameterInfo("$planbase", getPlanbase(), "jadex.runtime.impl.RPlanbase"));
		copy.add(new ExpressionParameterInfo("$eventbase", getEventbase(), "jadex.runtime.impl.REventbase"));
		copy.add(new ExpressionParameterInfo("$expressionbase", getExpressionbase(), "jadex.runtime.impl.RExpressionbase"));
		copy.add(new ExpressionParameterInfo("$propertybase", getPropertybase(), "jadex.runtime.impl.RPropertybase"));
		return copy;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MCapability clone = (MCapability)cl;
		if(beliefbase!=null)
			clone.beliefbase = (MBeliefbase)beliefbase.clone();
		if(goalbase!=null)
			clone.goalbase = (MGoalbase)goalbase.clone();
		if(planbase!=null)
			clone.planbase = (MPlanbase)planbase.clone();
		if(eventbase!=null)
			clone.eventbase = (MEventbase)eventbase.clone();
		if(expressionbase!=null)
			clone.expressionbase = (MExpressionbase)expressionbase.clone();
		if(propertybase!=null)
			clone.propertybase = (MPropertybase)propertybase.clone();
		if(goalbase!=null)
			clone.goalbase = (MGoalbase)goalbase.clone();
		if(configurationbase!=null)
			clone.configurationbase = (MConfigurationbase)configurationbase.clone();

		if(imports!=null)
			clone.imports = (ArrayList)imports.clone();
		if(capabilityrefs!=null)
		{
			clone.capabilityrefs = SCollection.createArrayList();
			for(int i=0; i<capabilityrefs.size(); i++)
				clone.capabilityrefs.add(((MElement)capabilityrefs.get(i)).clone());
		}

		// Parser not cloned, as implementations are assumed immutable.
	}
}
