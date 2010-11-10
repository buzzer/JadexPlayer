package jadex.runtime.impl;

import jadex.model.IMBase;
import jadex.model.IMElementReference;
import jadex.model.IMConfigElement;
import jadex.model.IMReferenceableElement;
import jadex.runtime.SystemEvent;
import jadex.util.collection.SCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *  This element has a scope. A scope is a container in form
 *  of a capability or of an agent. Also a referencable element
 *  has a list of references pointing to references of this element
 *  which are contained in another scope.
 */
public abstract class RReferenceableElement extends RElement implements IRReferenceableElement
{
	//-------- attributes --------

	/** The list of references. */
	protected List	references;

	/** The creator. */
	protected RReferenceableElement creator;

	/** The configuration. */
	private IMConfigElement	config;

	/** The flag if is inited. */
	private boolean inited;

	/** The (cached) list of occurrences. */
	protected List	occurrences;

	//-------- constructors --------

	/**
	 *  Create a new runtime element.
	 *  @param name The name, or null for automatic naming.
	 *  @param modelelement	The model of this element.
	 *  @param owner The owner of this element.
	 */
	protected RReferenceableElement(String name, IMReferenceableElement modelelement,
		IMConfigElement config, RElement owner, RReferenceableElement creator, Map exparams)
	{
		//super(name==null? (state!=null? state.getName(): null): name, modelelement, owner, exparams);
		super(name, modelelement, owner, exparams); // todo: use state name? problem: more than one element when binding
		this.config	= config;
		assert owner!=null : name+" "+modelelement+" "+exparams;
		this.creator = creator;

		fetchAssignToElements();
		fetchStrongExports();
	}

	/**
	 *  Init the complete element structure.
	 */
	public void initStructure()
	{
		List occs = getAllOccurrences();
		for(int i=0; i<occs.size(); i++)
		{
			((RReferenceableElement)occs.get(i)).init();
		}
	}

	Exception	ex;
	/**
	 *  Init the element.
	 */
	protected void init()
	{
		boolean	debug	= false;
		assert	debug=true;	// Sets debug to true, if asserts are enabled.
		if(debug)
		{
			if(ex==null)
				try{throw new RuntimeException();}
				catch(RuntimeException e){ex=e;}
			else
				ex.printStackTrace();
		}
		assert !inited : this;
		this.inited = true;
	}

	/**
	 *  Is the element inited.
	 */
	protected boolean isInited()
	{
		return inited;
	}

	/**
	 *  Fetch (create or get) the abstract elements to which this element points.
	 *  todo: code does not work for parameters because owner is not a base!
	 */
	protected void fetchAssignToElements()
	{
		// Instantiate assign to references.
		IMElementReference[]	refs	= ((IMReferenceableElement)getModelElement()).getAssignToElements();
		for(int i=0; i<refs.length; i++)
		{
			// Check if assignto elemented was already created.
			if(creator==null || creator.getModelElement()!=refs[i])
			{
				if(getOwner() instanceof RBase)
				{
					RBase	base	= (RBase)getOwner();
					RElementReference elem = (RElementReference)base.getElementInstance(refs[i], this);
				}
				else
				{
					// todo: parameters
				}
			}
		}
	}

	/**
	 *  Create strong exports (i.e. outer references).
	 */
	// Hack!!! move parts of this code to model if possible.
	// Todo: introduce strong export mode in model.
	protected void fetchStrongExports()
	{
		if(IMReferenceableElement.EXPORTED_TRUE.equals(((IMReferenceableElement)getModelElement()).getExported()))
		{
			if(getOwner() instanceof RBase)
			{
				if(getScope().getParent()!=null)
				{
					RBase	base	= (RBase)getOwner();
					IMBase	mbase	= (IMBase)base.getCorrespondingBase(getScope().getParent()).getModelElement();
					IMReferenceableElement[]	elements	= mbase.getReferenceableElements();
					for(int i=0; i<elements.length; i++)
					{
						if(elements[i] instanceof IMElementReference)
						{
							IMElementReference	ref	= (IMElementReference)elements[i];
							if(ref.getReferencedElement()==this.getModelElement())
							{
								// Check if reference was already created.
								if(creator==null || creator.getModelElement()!=ref)
								{
//									System.out.println("Creating strong reference: "+ref);
									RElementReference elem = (RElementReference)base.getElementInstance(ref, this);
								}
							}
						}
					}
				}
			}
			else
			{
				// todo: parameters
			}
		}
	}

	//-------- methods --------

	/**
	 *  Get the configuration (null, if none.)
	 *  @return The configuration.
	 */
	public IMConfigElement getConfiguration()
	{
		return config;
	}

	/**
	 *  Remove the configuration
	 *  (set to null for garbage collection).
	 */
	public void removeConfiguration()
	{
		config = null; // Helping garbage collection.
	}

	/**
	 *  Get the creator.
	 */
	protected RReferenceableElement getCreator()
	{
		return this.creator;
	}

	/**
	 *  Add a reference to this element.
	 *  @param reference 	The reference to add.
	 */
	protected void addReference(RElementReference reference)
	{
		// Lazy creation of reference list for speed.
		if(references==null)
			this.references	= SCollection.createArrayList();
		// Remove cached occurrences.
		occurrences	= null;
		references.add(reference);
	}

	/**
	 *  Get the direct references to this element.
	 */
	public List getReferences()
	{
		if(references!=null)
			return references;
		else
			return Collections.EMPTY_LIST;
	}

	/**
	 *  Get all occurrences of this element
	 *  (including the original element, direct references,
	 *  and all indirect references).
	 */
	public List getAllOccurrences()
	{
		if(occurrences==null)
		{
			ArrayList	refs	= SCollection.createArrayList();
			refs.add(this);
	
			// Iterate through references.
			for(int i=0; i<refs.size(); i++)
			{
				RReferenceableElement	elm	= (RReferenceableElement)refs.get(i);
				// Hack!!! Access lazily assigned field for speed.
				if(elm.references!=null)
					refs.addAll(elm.references);
			}
			occurrences	= Collections.unmodifiableList(refs);
		}

		return occurrences;
	}

	/**
	 *  Get the original goal of the given goal.
	 *  Resolves all references (if any).
	 */
	public RReferenceableElement	getOriginalElement()
	{
		return this;
	}

	//-------- system event handling --------
	
	/**
	 *  Generate a change event for this element.
	 *  @param event The event type.
	 */
	public void throwSystemEvent(String event)
	{
		throwSystemEvent(new SystemEvent(event, this));
	}

	/**
	 *  Generate a change event for this element.
	 *  @param event The event.
	 */
	public void throwSystemEvent(SystemEvent event)
	{
		if(!inited)
			event.setInit(true);
		if(getScope().getAgent().isTransactionStarted() && !event.isChangeRelevant())
		{
			// When info event occurs inside transaction just add it.
			getScope().addInfoEvent(event);
		}
		else
		{
			getScope().getAgent().startSystemEventTransaction();
			createSystemEvents(event);
			getScope().getAgent().commitSystemEventTransaction();
		}
	}

	/**
	 *  Create (and collect) system events.
	 *  @param event The event.
	 */
	// Internal method used to create events for references.
	protected void createSystemEvents(SystemEvent event)
	{
//getScope().blocked = true;
		getScope().collectSystemEvents(event);
//getScope().blocked = false;

		// Throw event for all references also.
		if(references!=null)
		{
			for(int i=0; i<references.size(); i++)
			{
				RElementReference	ref	= (RElementReference)references.get(i);
				SystemEvent clone = new SystemEvent(event.getType(), ref, event.getValue(), event.getIndex());
				ref.createSystemEvents(clone);
			}
		}
	}
	
	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that the cleanedup property is set.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		super.cleanup();

		// Cleanup references.
		if(references!=null)
		{
			for(int i=0; i<references.size(); i++)
			{
				((RElementReference)references.get(i)).cleanup();
			}
		}
	}

	//-------- helpers --------

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map rep = super.getEncodableRepresentation();
		rep.put("scope", getScope()==null ? "null" : getScope().getDetailName());
		return rep;
	}
}
