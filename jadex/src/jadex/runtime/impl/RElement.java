package jadex.runtime.impl;

import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.*;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 *  A runtime element is an instance of a model element.
 */
public abstract class RElement	implements IRElement, Serializable, IEncodable
{
	//-------- attributes --------

	/** The name. */
	protected String name;

	/** The modelelement. */
	private IMElement modelelement;

	/** The element's owner. */
	private transient WeakObject owner;

	/** The element's scope. */
	private RCapability scope;

	/** The expression parameters (named values accessible from expressions). */
	protected Map	exparams;

	/** Is the element cleanedup. */
	protected boolean cleanedup;

	//-------- constructors --------

	/**
	 *  Create a new runtime element.
	 *  @param modelelement	The model of this element.
	 *  @param owner	The owner of this element.
	 */
	protected RElement(IMElement modelelement, RElement owner)
	{
		this(null, modelelement, owner);
	}

	/**
	 *  Create a new runtime element.
	 *  @param name	The name, or null for automatic naming.
	 *  @param modelelement	The model of this element.
	 *  @param owner	The owner of this element.
	 */
	protected RElement(String name, IMElement modelelement, RElement owner)
	{
		this(name, modelelement, owner, null);
	}

	/**
	 *  Create a new runtime element.
	 *  @param name	The name, or null for automatic naming.
	 *  @param modelelement	The model of this element.
	 *  @param owner	The owner of this element.
	 *  @param exparams	The expression parameters.
	 */
	protected RElement(String name, IMElement modelelement, RElement owner, Map exparams)
	{
		this.modelelement	= modelelement;
		this.owner	= owner==null? null: new WeakObject(owner);
		this.scope = findScope();
		this.name	= name!=null ? name : modelelement.getName()+"#"+scope.instanceCount(modelelement);

//		scope.incActive(modelelement);
	
		if(exparams!=null)
			setExpressionParameters(exparams);
	}

//RuntimeException	st;
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

		/*if(cleanedup)
		{
			System.out.println("--a: "+this.getName());
			st.printStackTrace(System.err);
			System.out.println("--b: "+this.getName());
			new RuntimeException("second cleanup").printStackTrace();
		}
		try
		{
			throw new RuntimeException("first cleanup");
		}
		catch(RuntimeException e)
		{
			st	=e;
		}*/

		this.cleanedup = true;

		// Use this decrement to detect elements which aren't cleaned up.
//		scope.decActive(modelelement);
	}

	/**
	 *  Called when garbage collector decides to discard the element.
	 */
	protected void finalize() throws Throwable
	{
		super.finalize();
		if(!cleanedup)
		{
//			if(finalized_classes.add(this.getClass()))
//				System.out.println("finalize(): "+this.getClass());

			scope.getAgent().scheduleGarbageCollection(this);
//			cleanup();	// Hack!!! Uses wrong thread.
		}
	}
	
//	public static Set	finalized_classes	= Collections.synchronizedSet(new HashSet()); 

	//-------- accessors ---------

	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 *  Get the fully qualified name with respect its owner.
	 *  @return The fully qualified name.
	 */
	public String getDetailName()
	{
		return getOwner()!=null? getOwner().getDetailName()+"."+this.name: this.name;
	}

	/**
	 *  Get the model element.
	 *  @return The model element.
	 */
	public IMElement getModelElement()
	{
		return this.modelelement;
	}

	/**
	 *  Get the type of this runtime element.
	 *  @return The type.
	 */
	public String getType()
	{
		return getModelElement().getName();
	}

	/**
	 *  Get the owner.
	 *  @return The owner.
	 */
	public RElement	getOwner()
	{
		return owner==null? null: (RElement)owner.get();
	}

	/**
	 *  Get the scope.
	 *  @return The scope.
	 */
	public RCapability getScope()
	{
		return this.scope;
	}

	/**
	 *  Set the owner.
	 *  @param owner The owner.
	 * /
	protected void	setOwner(RElement owner)
	{
		this.owner	= owner;
	}*/

	//-------- expression parameters --------

	/**
	 *  Create a local map for holding expression parameters.
	 *  @return The local map.
	 */
	protected Map createLocalExpressionParameters()
	{
		return	getOwner()==null
			? SCollection.createHashMap()
			: (Map)SCollection.createNestedMap(getOwner().getExpressionParameters());
	}

	/**
	 *  Get the expression parameters.
	 *  If this element has no local parameters, will return
	 *  the parameters of the owner, or null if the element
	 *  has no owner.
	 */
	public Map	getExpressionParameters()
	{
		return exparams!=null? exparams:
			getOwner()!=null? getOwner().getExpressionParameters(): null;
	}

	/**
	 *  Set some expression parameters.
	 *  @param parameters The parameter map (name, value).
	 */
	protected void setExpressionParameters(Map parameters)
	{
		if(this.exparams==null)
			this.exparams = createLocalExpressionParameters();
		exparams.putAll(parameters);
	}

	/**
	 *  Get an expression parameter.
	 *  @param name The parameter name.
	 *  @return The parameter value.
	 */
	public Object	getExpressionParameter(String name)
	{
		Map	map	= getExpressionParameters();
		return map!=null ? map.get(name) : null;
	}

	/**
	 *  Set an expression parameter.
	 *  @param name The parameter name.
	 *  @param value The parameter value.
	 */
	public void setExpressionParameter(String name, Object value)
	{
		// Use local parameters because owner parameters
		// must not be overridden.
		if(this.exparams==null)
			this.exparams = createLocalExpressionParameters();
		this.exparams.put(name, value);
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
		sb.append(")");
		return sb.toString();
	}

	//-------- helper methods --------

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  Method will be overridden by subclasses. When the method
	 *  is invoked it newly fetches several proporties.
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		// todo: save rep?
		HashMap rep	= SCollection.createHashMap();
		rep.put("isencodeablepresentation", "true"); // to distinguish this map from normal maps.
		rep.put("type", getType());
		rep.put("name", getName());
		rep.put("id", getClass().getName()+"@"+hashCode());
		rep.put("class", SReflect.getInnerClassName(this.getClass()));
		rep.put("owner", getOwner()==null ? "null" : getOwner().getDetailName());
		return rep;
	}

	/**
	 *  Find the scope of an element.
	 */
	public RCapability findScope()
	{
		// Search the elements scope.
		IRElement tmp = this;
		while(tmp!=null && !(tmp instanceof RCapability))
			tmp = tmp.getOwner();

		if(tmp==null)
			throw new RuntimeException("No scope found for: "+this);

		return (RCapability)tmp;
	}

	/**
	 *  Test, if the element is cleanedup.
	 *  @return True, if cleaned up.
	 */
	public boolean isCleanedup()
	{
		return this.cleanedup;
	}

	//-------- serialization handling --------

	protected Object	serialized_owner;
	
	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	writeReplace() throws ObjectStreamException
	{
		// Extract weak reference to owner as it is not serializable.
		this.serialized_owner	= owner!=null ? owner.get() : null;
		return this;
	}

	/**
	 *  Perform special handling on serialization.
	 */
	protected Object	readResolve() throws ObjectStreamException
	{
		this.owner	= serialized_owner!=null ? new WeakObject(serialized_owner) : null;
		this.serialized_owner	= null;
		return this;
	}
}
