package jadex.runtime.planwrapper;

import java.io.Serializable;
import java.util.Map;

import jadex.runtime.impl.*;
import jadex.util.collection.SCollection;
import jadex.model.IMElement;

/**
 *  Provides some helper stuff for plan wrappers.
 */
public class ElementWrapper	implements Serializable
{
	//-------- attributes --------

	/** The element. */
	private IRElement element;

	//-------- constructors --------

	/**
	 *  Create an abstract wrapper.
	 */
	public ElementWrapper(IRElement element)
	{
		assert element!=null;
		this.element = element;
	}

	//-------- interface methods --------

	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		checkThreadAccess();
		return element.getName();
	}

	/**
	 *  Get the model element.
	 *  @return The model element.
	 */
	public IMElement getModelElement()
	{
		checkThreadAccess();
		return element.getModelElement();
	}

	//-------- helper methods --------

	/**
	 *  Get the original element.
	 */
	public IRElement	unwrap()
	{
		return element;
	}

	/**
	 *  Get the capability.
	 *  @return the agent this gui is for
	 */
	protected RCapability getCapability()
	{
		return element.getScope();
	}
	
	/**
	 *  Get the agent.
	 *  @return the agent this gui is for
	 */
	protected RBDIAgent getAgent()
	{
		return element.getScope().getAgent();
	}

	/**
	 *  Test if two elements are equal.
	 *  @return True, if equal.
	 */
	public boolean equals(Object o)
	{
		if(o instanceof ElementWrapper)
			o = ((ElementWrapper)o).unwrap();
		return element.equals(o);
	}

	/**
	 *  Return the hashcode of the element.
	 */
	public int	hashCode()
	{
		return element.hashCode();
	}

	/**
	 *  Return the string representation of the element.
	 */
	public String	toString()
	{
		return element.toString();
	}

	/**
	 *  Check if the plan thread is accessing.
	 *  @return True, if access is ok.
	 */
	public boolean checkThreadAccess0()
	{
		return !element.getScope().getAgent().getInterpreter().isExternalThread();
	}

	/**
	 *  Check if the plan thread is accessing.
	 *  @throws RuntimeException when wrong thread (e.g. GUI) is calling agent methods.
	 */
	public void checkThreadAccess()
	{
		if(!checkThreadAccess0())
		{
			throw new RuntimeException("Wrong thread calling plan interface. " +
				"Only the plan thread is allowed to call methods on the plan or other elements directly. " +
				"Other threads, e.g. GUI listeners, need to use the external access interface: "+Thread.currentThread());
		}
	}
}