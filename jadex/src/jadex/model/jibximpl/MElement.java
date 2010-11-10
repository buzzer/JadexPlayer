package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import org.jibx.runtime.ITrackSource;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

/**
 *  The base element for all model elements.
 */
public class MElement implements IMElement, Serializable, Cloneable
{
	//-------- xml attributes --------

	/** The elements name. */
	protected String	name;

	/** The elements description. */
	protected String	description;

	/** The check report (if any). */
	//protected transient IReport	report;
	protected Report	report;

	/** The list of externally added expression parameters. */
	protected List exparams;

	//--------- attributes --------

	/** The owner. */
	private IMElement owner;

	/** The scope. */
	protected IMCapability scope;

	/** Flag indicating that checking is in progress (hack???). */
	protected boolean	checking;

	//-------- xml methods --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		//assert name!=null : getScope()+", "+getClass();
		return this.name;
	}

	/**
	 *  Set the element name.
	 *  @param name The name.
	 */
	public void	setName(String name)
	{
		this.name = name;
	}

	/**
	 *  Get the description.
	 *  @return The description.
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 *  Set the description.
	 *  @param description The description.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	//-------- constructors --------

	/**
	 *  Called from outside to start (recursive) element setup.
	 */
	public void setup()
	{
		//System.out.println("Called setup on: "+getName()+" "+getOwner());
//		// Get a list of all children (level-order).
//		List cs = SCollection.createArrayList();
//		cs.add(this);
//		for(int i=0; i<cs.size(); i++)
//		{
//			List tmp = ((MElement)cs.get(i)).getChildren();
//			if(tmp.contains(null))
//			{
//				List tmp2 = ((MElement)cs.get(i)).getChildren();
//				System.out.println("scheisse"+cs.get(i)+" "+tmp);
//			}
//			cs.addAll(tmp);
//			//cs.addAll(((MElement)cs.get(i)).getChildren());
//		}
//
//		// Init all children, beginning with the outermost.
//		for(int i=cs.size()-1; i>-1; i--)
//			((MElement)cs.get(i)).init();

	
		// Init recursively (post-order).
		List	cs	= getChildren();
		for(int i=0; i<cs.size(); i++)
			((MElement)cs.get(i)).setup();

		this.init();
	}

	Exception	ex;
	protected boolean inited = false;
	/**
	 *  Called when the element is setup.
	 *  Overwritten in elements that need a setup.
	 */
	protected void init()
	{
		boolean	debug	= false;
		assert	debug=true;	// Sets debug to true, if asserts are enabled.
		if(debug)
		{
			if(ex==null)
			{
				try{throw new RuntimeException("first init: "+this);}
				catch(RuntimeException e){ex=e;}
			}
			else
			{
				ex.printStackTrace();
				try{throw new RuntimeException("second init: "+this);}
				catch(RuntimeException e){e.printStackTrace();}
			}
		}
		assert !inited : this;
		inited = true;
		//System.out.println("Called init on: "+getName());
	}

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		return SCollection.createArrayList();
	}

	//private boolean presetted = false;
	/**
	 *  The owner is automatically set by jibx.
	 *  @param uc The unmarshalling context.
	 */
	public void preset(IUnmarshallingContext uc)
	{
		//System.out.println("Called: "+uc+" "+this.getClass());
		for(int i=0; i<uc.getStackDepth() && owner==null; i++)
		{
			Object test = uc.getStackObject(i);
			//System.out.println("Found: "+test);
			if(test instanceof IMElement && test!=this) // todo: new Hack for Jibx RC1?
				owner = (IMElement)test;
		}

		assert owner!=null || this instanceof Agent || this instanceof Capability || this instanceof Properties
			: getName()+" "+getClass();
		assert owner!=this;

		/*if(owner==null && !(this instanceof IMBDIAgent))
		{
			System.out.println("Preset on: "+getClass());
			System.out.println(getOwner());
		}*/
		//presetted = true;
	}

	//--------- methods  --------

	/**
	 * Get the owner.
	 * @return The owner.
	 */
	public IMElement getOwner()
	{
		return owner;
	}

	/**
	 *  Set the owner of the element.
	 *  Called when the element is created manually.
	 */
	protected void setOwner(IMElement owner)
	{
		this.owner	= owner;
	}

	/**
	 *  Get the elements scope, that means the
	 *  capability it is contained in.
	 *  @return The capability.
	 */
	public IMCapability getScope()
	{
		if(scope==null)
			scope = findScope();
		return scope;
	}

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		//System.out.println("pi: "+getClass()+" "+getName());
		// todo: object variable that is new initialized?!
		Map representation	= SCollection.createHashMap();
		representation.put("isencodeablepresentation", "true"); // to distinguish this map from normal maps.
		representation.put("name", getName());
		representation.put("class", SReflect.getInnerClassName(this.getClass()));
		representation.put("id", getClass().getName()+"@"+hashCode());
		representation.put("description", getDescription());
		return representation;
	}

	/**
	 *  Get the expression parameters.
	 *  If this element has no local parameters, will return
	 *  the parameters of the owner, or null if the element
	 *  has no owner.
	 */
	public List	getSystemExpressionParameters()
	{
		List copy = getOwner()!=null? getOwner().getSystemExpressionParameters(): SCollection.createArrayList();
		if(exparams != null)
			copy.addAll(exparams);
		return copy;
	}

	/**
	 *  Add an expression parameter externally.
	 *  Used e.g. to add user binding parameters to conditions.
	 */
	protected void addExpressionParameter(ExpressionParameterInfo expi)
	{
		if(exparams==null)
			exparams = SCollection.createArrayList();
		exparams.add(expi);
	}

	/**
	 *  Check the validity of the model.
	 *  Returns a report of errors or an empty report
	 *  when the model is valid.
	 */
	public IReport	check()
	{
		boolean	nocheck	= false;
		if(!nocheck)
		{
			this.report	= new Report(this);
			this.checking	= true;
			
			// Check iteratively (breadth search).
			List	elements	= SCollection.createArrayList();
			elements.add(this);
	
			// Check all elements.
			for(int i=0; i<elements.size(); i++)
			{
				MElement	element	= (MElement)elements.get(i);
				elements.addAll(element.getChildren());
				element.doCheck(report);
			}
			
			// Cleanup all elements.
			for(int i=0; i<elements.size(); i++)
			{
				MElement	element	= (MElement)elements.get(i);
				element.doCheckCleanup();
			}
			
			this.checking	= false;
		}
		else
		{
			this.report	= new Report(this, true);
		}

		return report;
	}

	/**
	 *  Return the last check report, or generate a new one, if the model
	 *  has not yet been checked.
	 *  @see #check()
	 */
	public IReport	getReport()
	{
		if(this.report==null)
			check();
		return this.report;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		// Empty default implementation.
	}

	/**
	 *  Perform cleanup after checking this element.
	 */
	protected void doCheckCleanup()
	{
		// Empty default implementation.
	}

	/**
	 *  Check if checking is in progress.
	 */
	protected boolean	isChecking()
	{
		return checking || (owner!=null && ((MElement)owner).isChecking());
	}
	
	/**
	 *  Get the source location of this element.
	 *  @return null, if the source location is unknown.
	 */
	public SourceLocation	getSourceLocation()
	{
		SourceLocation	loc	= null;
		if(this instanceof ITrackSource)
		{
			ITrackSource	its	= (ITrackSource)this;
			if(its.jibx_getLineNumber()!=-1 && its.jibx_getColumnNumber()!=-1)
			{
				String	filename;
				if(this instanceof IMCapability)
				{
					filename	= ((IMCapability)this).getFilename();
				}
				else
				{
					filename	= this.getScope().getFilename();
				}
				loc	= new SourceLocation(filename, its.jibx_getLineNumber(), its.jibx_getColumnNumber());
			}
		}
		return loc;
	}

	//-------- helper methods --------

	/**
	 *  Create a string representation of this element.
	 */
	public String	toString()
	{
		if(hackflag)
		{
			// fallback
			String	name	= this.name;
			if(name==null)
			{
				// For unnamed element use owner.
				name	= owner!=null ? "of "+owner.toString() : null;
			}
			return SReflect.getInnerClassName(this.getClass())+"("+name+")";
		}
		return toString(0);
	}

	/**
	 *  Create a string representation of this element.
	 *  @param indent	The indentation level.
	 */
	public String	toString(int indent)
	{
		hackflag	= true;
		return indent(indent) + toString();
	}

	// Hack ??? avoid stack-overflow, when neither method is overriden.
	private boolean	hackflag	= false;

	/**
	 *  Create an indentation prefix.
	 *  @param indent	The indentation level.
	 */
	protected static String	indent(int indent)
	{
		StringBuffer	buf	= new StringBuffer();
		while(indent-- > 0)
			buf.append("    ");
		return buf.toString();
	}

	/**
	 *  Find and assign the scope.
	 */
	public IMCapability findScope()
	{
		IMCapability	ret;
		if(getOwner() instanceof IMCapability)
		{
			ret	= (IMCapability)getOwner();
		}
		else
		{
			assert getOwner()!=null : this;
			ret	= getOwner().getScope();
		}
		return ret;
	}

	/** The thread local. */
	protected static ThreadLocal clones = new ThreadLocal();

	/**
	 *  Make a deep copy of this element.
	 *  @return The deep copy.
	 */
	public Object clone()
	{
		MElement ret = null;

		boolean creator = false;
		HashMap cls = (HashMap)clones.get();
		if(cls==null)
		{
			cls = SCollection.createHashMap();
			clones.set(cls);
			creator = true;
		}
		else
		{
			ret = (MElement)cls.get(this);
		}

		if(ret==null)
		{
			try
			{
				ret = (MElement)super.clone();
			}
			catch(CloneNotSupportedException exception)
			{
				throw new RuntimeException("Cloning not supported: "+this);
			}

			// Save the clone immediately to make it accessible for other elements.
			cls.put(this, ret);

			doClone(ret);
		}

		// Delete clones hashmap if element was creator.
		if(creator)
			clones.set(null);

		return ret;
	}

	/**
	 *  Do clone makes a deep clone without regarding cycles.
	 *  Method is overridden by subclasses to actually incorporate their attributes.
	 *  @param clone The clone.
	 */
	protected void doClone(MElement clone)
	{
		if(report!=null)
			clone.report = (Report)report.clone();
		if(exparams!=null)
		{
			clone.exparams = SCollection.createArrayList();
			for(int i=0; i<exparams.size(); i++)
				clone.exparams.add(((MElement)exparams.get(i)).clone());
		}
		if(owner!=null)
			clone.owner = (IMElement)((MElement)owner).clone();
		if(scope!=null)
			clone.scope = (IMCapability)((MElement)scope).clone();
	}

}
