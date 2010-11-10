package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.SReflect;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  Baseclass for stub elements that are just references to other
 *  elements from other capabilities.
 */
public abstract class RElementReference extends RReferenceableElement
{
	//-------- attributes --------

	/** The original element referenced by this element. */
	private RReferenceableElement	element;

	/** The flag if the element was tried to be resolved. */
	private boolean resolved;

	//-------- constructors --------

	/**
	 *  Create a new RElementReference.
	 *  @param name
	 *  @param modelelement
	 *  @param owner
	 */
	protected RElementReference(String name, IMElementReference modelelement,
		IMConfigElement state, RElement owner, RReferenceableElement creator)
	{
		super(name, modelelement, state, owner, creator, null);
		resolveReference(getCreator());
	}

RuntimeException exe = null;
	/**
	 *  Initialize the element.
	 */
	protected void	resolveReference(RReferenceableElement creator)
	{
		if(resolved)
		{
			//System.out.println("Element is: "+getName()+" "+getClass());
			if(exe!=null)
				exe.printStackTrace();
			throw new RuntimeException("test2");
		}
		assert !resolved;
		boolean	debug	= false;
		assert	debug=true;	// Sets debug to true, if asserts are enabled.
		if(debug)
		{
			try
			{
				throw new RuntimeException("test");
			}
			catch(RuntimeException e)
			{
				exe = e;
			}
		}

		// Initialize reference.
		this.element	= ((RBase)getOwner()).getReferencedElement(this, creator);
		IMElementReference melem = (IMElementReference)getModelElement();

		if(element!=null)
		{
			element.addReference(this);
		}
		else if(!melem.isAbstract() || melem.isRequired())
		{
			throw new RuntimeException("Could not resolve reference of: "+getName());
		}
		/*else
		{
			// else special case: element is abstract and !required
			System.out.println("Found special case, optional unbound ref:"+getName());
		}*/
		resolved = true;
	}

	//-------- methods --------

	/**
	 *  Get the referenced element.
	 */
	public RReferenceableElement	getReferencedElement()
	{
		//if(!inited)
		//	init();
		if(!isBound())
			throw new RuntimeException("Reference is unbound: "+getName());//+" "+element+" "+resolved+" "+inited);
		return element;
	}

	/**
	 *  Set referenced element.
	 */
	protected void setReferencedElement(RReferenceableElement element)
	{
		// When the element is decoupled it can be resolved again.
		if(element==null)
			this.resolved = false;
		this.element = element;
	}

	/**
	 *  Get all occurrences of this element
	 *  (including the original element, direct references,
	 *  and all indirect references).
	 */
	public List getAllOccurrences()
	{
		return getReferencedElement().getAllOccurrences();
	}

	/**
	 *  Get the original goal of the given goal.
	 *  Resolves all references (if any).
	 *  @throws RuntimeException if the element is unbound.
	 */
	public RReferenceableElement	getOriginalElement()
	{
		RReferenceableElement elem = getReferencedElement();
		while(elem instanceof RElementReference)
			elem = ((RElementReference)elem).getReferencedElement();
		return elem;
	}
	
	/**
	 *  Is it forbidden to execute init code.
	 */
	protected boolean isResolved()
	{
		return resolved;
	}

	/**
	 *  Set the resolved state.
	 */
	protected void setResolved(boolean resolved)
	{
		this.resolved = resolved;
	}

	/**
	 *  Test if the element is bound
	 */
	public boolean isBound()
	{
		//assert inited;
		assert resolved;
		// Possibly inits element.
		//if(!inited)
		//	init();
		return element instanceof RElementReference?
			((RElementReference)element).isBound():
				element==null? false: true;
	}

	/**
	 *  Check if an element is (directly) bound.
	 * /
	protected boolean checkBound0()
	{
		if(!resolved)
			resolveReference(getCreator());
		return element!=null;
	}*/

	/**
	 *  Check if an element is bound.
	 * /
	protected void checkBound()
	{
		if(!checkBound0())
			throw new RuntimeException("Reference is unbound: "+getName());
	}*/

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map	map;
		if(isBound())
		{
			// Make unified encodable representation (hack???).
			// Provides values of this element (name, owner etc.)
			// Adds values of referenced element (e.g. belief value).
			map= new HashMap(getReferencedElement().getEncodableRepresentation());
			map.putAll(super.getEncodableRepresentation());
		}
		else
		{
			map	= super.getEncodableRepresentation();
		}
		return map;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(getName());
//		sb.append(", reference=");
//		sb.append(getReferencedElement());
		sb.append(")");
		return sb.toString();
	}
}
