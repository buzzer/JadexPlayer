package jadex.runtime.externalaccesswrapper;

import jadex.model.IMElement;
import jadex.runtime.impl.IRElement;
import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.RCapability;

import java.io.Serializable;


/**
 *  Provides some helper stuff for gui wrappers.
 */
public class ElementWrapper	implements Serializable
{
	//-------- attributes --------

	/** The agenda. */
	private RBDIAgent agent;

	/** The wrapped element. */
	private IRElement	element;

	//-------- constructors --------

	/**
	 *  Create an abstract wrapper.
	 */
	public ElementWrapper(RBDIAgent agent, IRElement element)
	{
		assert agent!=null;
		assert element!=null;
		this.agent = agent;
		this.element	= element;
	}

	//-------- methods --------
	
	/**
	 *  Get the name.
	 *  @return The name.
	 */
	public String getName()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				string	= element.getName();
			}
		};
		return exe.string;
	}

	/**
	 *  Get the model element.
	 *  @return The model element.
	 */
	public IMElement getModelElement()
	{
		//checkThreadAccess();
		AgentInvocation	exe	= new AgentInvocation()
		{
			public void	run()
			{
				object	= element.getModelElement();
			}
		};
		return (IMElement)exe.object;
	}

	/**
	 *  Get the agent.
	 *  @return the agent this gui is for
	 */
	protected RBDIAgent getAgent()
	{
		return agent;
	}
	
	/**
	 *  Get the capability.
	 *  @return the agent this gui is for
	 */
	protected RCapability getCapability()
	{
		return element.getScope();
	}

	//-------- helper methods --------

	/**
	 *  Get the original element.
	 *  @return The original element.
	 */
	public IRElement	unwrap()
	{
		return element;
	}
	
	/**
	 *  Check if the external plan/agent thread is accessing.
	 *  @return True, if access is ok.
	 * / 
	public boolean isExternalThread()
	{
		RPlan	rp	= agent.getCurrentPlan();
		Object pt = rp!=null ? rp.getThread() : null;
		return Thread.currentThread()!=pt
			&& !agent.getAgentAdapter().isAgentThread(Thread.currentThread());
	}*/
	
	/**
	 *  Check if the plan/agent thread is accessing.
	 *  @return True, if access is ok.
	 * / 
	public boolean checkThreadAccess0()
	{
		return true; // Allow calling external access from every thread.
		//RPlan	rp	= agent.getCurrentPlan();
		//Object pt = rp!=null ? rp.getThread() : null;
		//return Thread.currentThread()!=pt
		//	&& !agent.getAgentAdapter().isAgentThread(Thread.currentThread());
	}*/

	/**
	 *  Check if the plan thread is accessing.
	 *  @throws RuntimeException when wrong thread (e.g. plan) is calling agent methods.
	 * /
	public void checkThreadAccess()
	{
		if(!checkThreadAccess0())
		{
			throw new RuntimeException("Plan or agent thread are "
				+"not allowed to call gui interface: "+Thread.currentThread());
		}
	}*/

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

	//-------- inner classes --------

	/**
	 *  An action to be executed on the agent thread.
	 *  Provides predefined variables to store results.
	 *  Directly invokes agenda in construcor.
	 */
	public abstract class AgentInvocation	implements Runnable
	{
		//-------- attributes --------

		/** The object result variable. */
		protected Object	object;

		/** The string result variable. */
		protected String	string;

		/** The int result variable. */
		protected int	integer;

		/** The long result variable. */
		protected long	longint;

		/** The boolean result variable. */
		protected boolean	bool;

		/** The object array result variable. */
		protected Object[]	oarray;

		/** The string result variable. */
		protected String[]	sarray;

		/** The class result variable. */
		protected Class	clazz;

		/** The exception. */
		protected Exception exception;

		//-------- constructors --------

		/**
		 *  Create an action to be executed in sync with the agent thread.
		 */
		public AgentInvocation()
		{
			//agenda.invokeAndWait(this);
			
			if(agent.getInterpreter().isExternalThread())
			{
				agent.getInterpreter().invokeSynchronized(this);
			}
			else
			{
				run();
			}
		}
	}
}
