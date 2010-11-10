package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  A element from some base that can be referenced (belief, goal, event, etc.).
 */
public abstract class MReferenceableElement extends MElement implements IMReferenceableElement
{
	//-------- xml attributes --------

	/** The exported flag of the element. */
	protected String exported	= EXPORTED_FALSE;

	/** The list of assign to elements. */
	private ArrayList assigntonames;// = SCollection.createArrayList();

	//-------- attributes --------

	/** The elements this element is assigned to. */
	private List assigntos;  // These are the model elements!

	//-------- constructors --------
	
	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();
		//this.assigntos = SCollection.createArrayList();
		initAssignToElements();
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		// Check if assigntos are valid.
		String[] asstos = getAssignTos();
		for(int i=0; i<asstos.length; i++)
		{
			IMReferenceableElement	refelem	= findReferencedElement(asstos[i]);
			if(refelem==null)
			{
				report.addEntry(this, "Invalid assignto specification '"+asstos[i]+"'. Referenced element not found.");
			}
			else if(!(refelem instanceof IMElementReference) || !((IMElementReference)refelem).isAbstract())
			{
				report.addEntry(this, "Invalid assignto specification '"+asstos[i]+"'. Cannot reference element, because it is not abstract.");
			}
			else if(!assignable((IMElementReference)refelem))
			{
				report.addEntry(this, "Invalid assignto specification '"+asstos[i]+"'. Cannot reference incompatible element '"+SReflect.getInnerClassName(refelem.getClass())+"'.");
			}			
		}
	}
	
	/**
	 *  Check if an element is assignable from this element.
	 *  E.g., a beliefsetreference is assignable from a beliefset,
	 *  a beliefreference is not.
	 */
	protected abstract boolean	assignable(IMElementReference ref);
	
	//-------- exported --------

	/**
	 *  Test if element is exported.
	 *  @return True, if element is exported.
	 */
	public String getExported()
	{
		return this.exported;
	}

	/**
	 *  Set the exported state.
	 *  @param exported The exported state.
	 */
	public void setExported(String exported)
	{
		this.exported = exported;
	}

	//-------- assigntos --------

	/**
	 *  Get the assignto elements (as reference string).
	 *  @return	The assignto elements.
	 */
	public String[]	getAssignTos()
	{
		if(assigntonames==null)
			return new String[0];
		return (String[])assigntonames.toArray(new String[assigntonames.size()]);
	}

	/**
	 *  Create an assignto element (as reference string).
	 *  @param ref	The reference.
	 */
	public void	createAssignTo(String ref)
	{
		if(assigntonames==null)
			assigntonames = SCollection.createArrayList();
		assigntonames.add(ref);
	}

	/**
	 *  Delete an assignto element (as reference string).
	 *  @param ref	The reference.
	 */
	public void	deleteAssignTo(String ref)
	{
		if(!assigntonames.remove(ref))
			throw new RuntimeException("Could not find: "+ref);
	}

	//-------- not xml related ---------

	/**
	 *  Get the references to which this element is assigned.
	 *  @return The references to which this element is assigned.
	 */
	public IMElementReference[] getAssignToElements()
	{
		if(assigntos==null)
			return new IMElementReference[0];
		return (IMElementReference[])assigntos.toArray(new IMElementReference[assigntos.size()]);
	}

	/**
	 *  Init the assign to elements.
	 */
	protected void initAssignToElements()
	{
		assert !isChecking() : this;
		assert getOwner()!=null;
		// the following is not valid for parameters
		//assert getOwner() instanceof IMBase: getName()+" "+getOwner();

		String[] asstos = getAssignTos();
		for(int i=0; i<asstos.length; i++)
		{
			String refname = asstos[i];

			IMReferenceableElement refelem = findReferencedElement(refname);
			assert refelem!=null;
			addAssignToElement(refelem);
		}
	}

	/**
	 *  Resolve an assignto element.
	 *  @return The resolved element or null in case of errors.
	 */
	protected IMReferenceableElement	findReferencedElement(String refname)
	{
		IMReferenceableElement	ret	= null;
		int idx = refname.indexOf(".");
		if(idx!=-1)
		{
			String capaname = refname.substring(0, idx);
			IMCapabilityReference caparef = getScope().getCapabilityReference(capaname);
			if(caparef!=null)
			{
				IMCapability	capa	= caparef.getCapability();
				if(capa!=null)
				{
					String rest = refname.substring(idx+1, refname.length());
					IMBase base = (IMBase)getOwner();
					ret	= base.getCorrespondingBase(capa).getReferenceableElement(rest);
				}
			}
		}
		return ret;
	}

	/**
	 *  Add an assignto element.
	 *  @param elem The new element.
	 */
	protected void addAssignToElement(IMReferenceableElement elem)
	{
		assert elem!=null;
		if(assigntos==null)
			assigntos = SCollection.createArrayList();
		assigntos.add(elem);

		// Set the opposite side also.
		if(elem instanceof IMElementReference)
			((MElementReference)elem).setAssignFromElement(this);
	}

	//-------- jibx helper --------

	/**
	 *  Add an assign to reference.
	 *  @param ref The assign to reference.
	 */
	public void addAssignToName(String ref)
	{
		if(assigntonames==null)
			assigntonames = SCollection.createArrayList();
		assigntonames.add(ref);
	}

	/**
	 *  Get an iterator for assign to names.
	 *  @return An iterator for all assign to names.
	 */
	public Iterator iterAssignToNames()
	{
		return assigntonames==null? Collections.EMPTY_LIST.iterator(): assigntonames.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MReferenceableElement clone = (MReferenceableElement)cl;
		if(assigntonames!=null)
			clone.assigntonames = (ArrayList)assigntonames.clone();
		if(assigntos!=null)
		{
			clone.assigntos = SCollection.createArrayList();
			for(int i=0; i<assigntos.size(); i++)
			{
				MElement	elem	= (MElement)assigntos.get(i);
				if(elem==null)
				{
					throw new NullPointerException("Problem in assignto "+i+" of element: "+this+", "+this.getScope());
				}
				clone.assigntos.add(elem.clone());
			}
		}
	}
}
