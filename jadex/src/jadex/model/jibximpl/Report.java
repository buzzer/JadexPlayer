package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.*;

import java.util.*;

/**
 *  A report contains information about errors in the model.
 */
public class Report implements IReport, Cloneable
{
	//--------- attributes --------

	/** The validated element. */
	protected IMElement	element;
	
	/** The multicollection holding the report messages. */
	protected MultiCollection	entries;

	/** The documents for external elements (e.g. capabilities). */
	protected Map	externals;
	
	/** Flag indicates that checking has been disabled for the given element (assumed valid without check). */
	protected boolean	nocheck;
	
	//-------- constructors --------

	/**
	 *  Create a report object.
	 */
	public Report(IMElement element)
	{
		this(element, false);
	}

	/**
	 *  Create a report object.
	 */
	public Report(IMElement element, boolean nocheck)
	{
		this.element	= element;
		this.nocheck	= nocheck;
	}

	//-------- methods --------

	/**
	 *  Check if this report is empty (i.e. the model is valid).
	 */
	public boolean	isEmpty()
	{
		return entries==null || entries.isEmpty(); 
	}

	/**
	 *  Add an entry to the report.
	 *  @param element	The element to which the entry applies.
	 *  @param message	The problem description. 
	 */
	public void	addEntry(IMElement element, String message)
	{
		if(entries==null)
			// Use index map to keep insertion order for elements.
			this.entries	= new MultiCollection(new IndexMap().getAsMap(), ArrayList.class);

		entries.put(element, message);
	}

	/**
	 *  Get all invalid elements.
	 */
	public IMElement[]	getElements()
	{
		if(entries==null)
			return new IMElement[0];
		else
			return (IMElement[])entries.getKeys(IMElement.class);
	}

	/**
	 *  Get the messages for a given element.
	 */
	public String[]	getMessages(IMElement element)
	{
		if(entries==null)
		{
			return new String[0];
		}
		else
		{
			Collection	ret	= entries.getCollection(element);
			return (String[])ret.toArray(new String[ret.size()]);
		}
	}

	/**
	 *  Generate a string representation of the report.
	 */
	public String	toString()
	{
		StringBuffer	buf	= new StringBuffer();

		buf.append("Report for ");
		buf.append(element);
		buf.append("\n");
		if(element instanceof IMCapability && ((IMCapability)element).getFilename()!=null)
		{
			buf.append("File: ");
			buf.append(((IMCapability)element).getFilename());
			buf.append("\n");
		}
		
		buf.append("\n");

		if(isEmpty())
		{
			buf.append(nocheck ? "Checking disabled for element." : "Model is valid.");
		}
		else
		{
			IMElement[]	elements	= getElements();
			for(int i=0; i<elements.length; i++)
			{
				buf.append(elements[i].toString());
				buf.append(":\n");
				String[]	messages	= 	getMessages(elements[i]);
				for(int j=0; j<messages.length; j++)
				{
					buf.append("\t");
					buf.append(messages[j]);
					buf.append("\n");
				}
			}
		}
		return SUtil.stripTags(buf.toString());
	}

	/**
	 *  Generate an html representation of the report.
	 */
	public String	toHTMLString()
	{
		StringBuffer	buf	= new StringBuffer();

		buf.append("<h3>Report for ");
		buf.append(element);
		buf.append("</h3>\n");
		if(element instanceof IMCapability && ((IMCapability)element).getFilename()!=null)
		{
			buf.append("File: ");
			buf.append(((IMCapability)element).getFilename());
			buf.append("\n");
		}

		if(isEmpty())
		{
			buf.append("<h4>Summary</h4>\n");
			buf.append("Model is valid.");
		}
		else
		{
			IMElement[]	capabilities	= getCapabilityErrors();
			IMElement[]	beliefs	= getOwnedElementErrors(IMBeliefbase.class);
			IMElement[]	goals	= getOwnedElementErrors(IMGoalbase.class);
			IMElement[]	plans	= getOwnedElementErrors(IMPlanbase.class);
			IMElement[]	events	= getOwnedElementErrors(IMEventbase.class);
			IMElement[]	configs	= getOwnedElementErrors(IMConfigBase.class);
			IMElement[]	others	= getOtherErrors();

			
			// Summaries.
			buf.append("<h4>Summary</h4>\n<ul>\n");
			generateOverview(buf, "Capability", capabilities);
			generateOverview(buf, "Belief", beliefs);
			generateOverview(buf, "Goal", goals);
			generateOverview(buf, "Plan", plans);
			generateOverview(buf, "Event", events);
			generateOverview(buf, "Configuration", configs);
			generateOverview(buf, "Other element", others);
			buf.append("</ul>\n");


			// Details.
			generateDetails(buf, "Capability", capabilities);
			generateDetails(buf, "Belief", beliefs);
			generateDetails(buf, "Goal", goals);
			generateDetails(buf, "Plan", plans);
			generateDetails(buf, "Event", events);
			generateDetails(buf, "Configuration", configs);
			generateDetails(buf, "Other element", others);

		}
		return buf.toString();
	}

	/**
	 *  Get capability references which have errors, or contain elements with errors.
	 */
	public IMElement[]	getCapabilityErrors()
	{
		List	errors	= SCollection.createArrayList();
		IMElement[]	elements	= getElements();
		for(int i=0; i<elements.length; i++)
		{
			IMElement	element	= elements[i];
			while(element!=null && !errors.contains(element))
			{
				if(element instanceof IMCapabilityReference)
				{
					errors.add(element);
					break;
				}
				element	= element.getOwner();
			}
		}
		return (IMElement[])errors.toArray(new IMElement[errors.size()]);
	}

	/**
	 *  Get elements contained in an element of the given ownertype, which have errors, or contain elements with errors.
	 */
	public IMElement[]	getOwnedElementErrors(Class basetype)
	{
		List	errors	= SCollection.createArrayList();
		IMElement[]	elements	= getElements();			
		for(int i=0; i<elements.length; i++)
		{
			IMElement	element	= elements[i];
			while(element!=null && !errors.contains(element))
			{
				if(element.getOwner()!=null && SReflect.isSupertype(basetype, element.getOwner().getClass()))
				{
					errors.add(element);
					break;
				}
				element	= element.getOwner();
			}
		}
		return (IMElement[])errors.toArray(new IMElement[errors.size()]);
	}

	/**
	 *  Get other errors, not in belief goal and plan base.
	 */
	public IMElement[]	getOtherErrors()
	{
		List	errors	= SCollection.createArrayList();
		IMElement[]	elements	= getElements();			
		for(int i=0; i<elements.length; i++)
		{
			IMElement	element	= elements[i];
			while(element!=null && !errors.contains(element))
			{
				if(element instanceof IMCapabilityReference
					|| element.getOwner() instanceof IMBeliefbase
					|| element.getOwner() instanceof IMGoalbase
					|| element.getOwner() instanceof IMPlanbase
					|| element.getOwner() instanceof IMEventbase
					|| element.getOwner() instanceof IMConfigBase)
				{
					break;
				}
				element	= element.getOwner();
			}
			if(element==null)
				errors.add(elements[i]);
		}
		return (IMElement[])errors.toArray(new IMElement[errors.size()]);
	}

	/**
	 *  Get all elements which have errors and are contained in the given element.
	 */
	public IMElement[]	getElementErrors(IMElement ancestor)
	{
		List	errors	= SCollection.createArrayList();
		IMElement[]	elements	= getElements();			
		for(int i=0; i<elements.length; i++)
		{
			IMElement	element	= elements[i];
			while(element!=null && !errors.contains(element))
			{
				if(ancestor.equals(element))
				{
					errors.add(elements[i]);
					break;
				}
				element	= element.getOwner();
			}
		}
		return (IMElement[])errors.toArray(new IMElement[errors.size()]);
	}

	/**
	 *  Add an external document.
	 *  @param id	The document id as used in anchor tags.
	 *  @param doc	The html text.
	 */
	public void	addDocument(String id, String doc)
	{
		if(externals==null)
			this.externals	= SCollection.createHashMap();
		
		externals.put(id, doc);
	}

	/**
	 *  Get the external documents.
	 */
	public Map	getDocuments()
	{
		return externals==null ? Collections.EMPTY_MAP : externals;
	}
	
	/**
	 *  Get the total number of errors.
	 */
	public int getErrorCount()
	{
		return entries==null ? 0 : entries.size();
	}

	//-------- helper methods --------

	/**
	 *  Generate overview HTML code for the given elements.
	 */
	protected void	generateOverview(StringBuffer buf, String type, IMElement[] elements)
	{
		if(elements.length>0)
		{
			buf.append("<li>");
			buf.append(type);
			buf.append(" errors\n<ul>\n");
			for(int i=0; i<elements.length; i++)
			{
				buf.append("<li><a href=\"#");
				buf.append(SUtil.makeConform(""+elements[i]));
				buf.append("\">");
				buf.append(SUtil.makeConform(""+elements[i]));
				buf.append("</a> has errors.</li>\n");
			}
			buf.append("</ul>\n</li>\n");
		}
	}

	/**
	 *  Generate detail HTML code for the given elements.
	 */
	protected void	generateDetails(StringBuffer buf, String type, IMElement[] elements)
	{
		if(elements.length>0)
		{
			buf.append("<h4>");
			buf.append(type);
			buf.append(" details</h4>\n<ul>\n");
			for(int i=0; i<elements.length; i++)
			{
				buf.append("<li><a name=\"");
				buf.append(SUtil.makeConform(""+elements[i]));
				buf.append("\">");
				buf.append(SUtil.makeConform(""+elements[i]));
				// Add name of configuration (hack???)
				if(elements[i] instanceof IMConfigElement)
				{
					IMElement	owner	= elements[i];
					while(owner!=null && !(owner instanceof IMConfiguration))
						owner	= owner.getOwner();
					if(owner!=null)
					buf.append(" in ");
					buf.append(SUtil.makeConform(""+owner));
				}
				buf.append("</a> errors:\n");

				IMElement[]	errors	= getElementErrors(elements[i]);
				buf.append("<dl>\n");
				for(int j=0; j<errors.length; j++)
				{
					buf.append("<dt>");
					buf.append(errors[j]);
					SourceLocation	loc	= errors[j].getSourceLocation();
					if(loc!=null)
					{
						buf.append(" (");
						buf.append(loc.getFilename());
						buf.append(": line ");
						buf.append(loc.getLineNumber());
						buf.append(", column ");
						buf.append(loc.getColumnNumber());
						buf.append(")");
					}
					
					String[]	msgs	= getMessages(errors[j]);
					buf.append("\n<dd>");
					for(int k=0; k<msgs.length; k++)
					{
						buf.append(msgs[k]);
						buf.append("\n");
						if(msgs.length>1 && k!=msgs.length-1)
							buf.append("<br>");
					}
					buf.append("</dd>\n");
				}
				buf.append("</dl>\n</li>\n");
			}
			buf.append("</ul>\n");
		}
	}

	/**
	 *  Make a deep copy of this element.
	 *  @return The deep copy.
	 */
	public Object clone()
	{
		Object ret = null;

		boolean creator = false;
		HashMap cls = (HashMap)MElement.clones.get();
		if(cls==null)
		{
			cls = SCollection.createHashMap();
			MElement.clones.set(cls);
			creator = true;
		}
		else
		{
			ret = cls.get(this);
		}

		if(ret==null)
		{
			try
			{
				ret = super.clone();
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
			MElement.clones.set(null);

		return ret;
	}

	/**
	 *  Do clone makes a deep clone without regarding cycles.
	 *  Method is overridden by subclasses to actually incorporate their attributes.
	 *  @param clone The clone.
	 */
	protected void doClone(Object clone)
	{
		Report rep = (Report)clone;
		if(element!=null)
			rep.element = (IMElement)((MElement)rep.element).clone();
		if(entries!=null)
		{
			rep.entries = new MultiCollection(new IndexMap().getAsMap(), ArrayList.class);
			Object[] keys = entries.keySet().toArray();
			for(int i=0; i<keys.length; i++)
			{
				Object[] vals = entries.getCollection(keys[i]).toArray();
				for(int j=0; j<vals.length; j++)
					rep.entries.put(((MElement)keys[i]).clone(), vals[j]);
			}
		}
	}

}
