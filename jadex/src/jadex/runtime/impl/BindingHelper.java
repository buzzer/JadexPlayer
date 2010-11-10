package jadex.runtime.impl;

import java.io.*;
import java.util.*;
import jadex.model.*;
import jadex.runtime.SystemEvent;
import jadex.util.SReflect;
import jadex.util.SUtil;
import jadex.util.collection.*;


/**
 *  A bindings element holding the binding expressions of a bindable element.
 */
public class BindingHelper	implements Serializable
{
	//-------- attributes --------

	/** The binding owner. */
	protected IMParameterElement binding_owner;

	/** The binding expressions. */
	protected IndexMap	binding_exps;

	/** The scope. */
	protected RCapability scope;

	//-------- constructors --------

	/**
	 *  Create a bindings element.
	 */
	protected BindingHelper(IMParameterElement binding_owner, RElement owner, boolean trace)
	{
		assert binding_owner!=null;
		assert owner!=null;

		this.binding_owner = binding_owner;
		this.binding_exps	= SCollection.createIndexMap();;
		IMParameter[] mbindings = binding_owner.getBindingParameters();

		// Create binding expressions.
		this.scope	= owner.getScope();

		SystemEvent se = null;
		for(int i=0; i<mbindings.length; i++)
		{
			// Hack!!! Modelelement as event source !?
			binding_exps.add(mbindings[i].getName(), scope.getExpressionbase().createInternalExpression(
				mbindings[i].getBindingOptions(), owner, trace? new SystemEvent(SystemEvent.BINDING_EVENT, mbindings[i]): null));
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
		// Remove expressions.
		for(int i=0; i<binding_exps.size(); i++)
		{
			((RExpression)binding_exps.get(i)).cleanup();
		}
	}

	//-------- methods --------
	

	/**
	 *  Calculate bindings for a bindable element.
	 *  @param event The event.
	 *  @param names The names.
	 *  @param values The values.
	 *  @return The list of bindings (containing name value pairs).
	 */
	protected List calculateBindings(IREvent event, String[] names, Object[] values)
	{
		// Provide event to expression evaluation (hack???).
		Map	params	= SCollection.createHashMap();
		params.put("$event", event);
		if(event instanceof IRGoalEvent)
			params.put("$goal", ((IRGoalEvent)event).getGoal());

		ArrayList ret = SCollection.createArrayList();

		IMParameter[] mbindings = binding_owner.getBindingParameters();

		// Get binding sets and first binding.
		IndexMap binds = SCollection.createIndexMap();
		for(int i=0; names!=null && i<names.length; i++)
			binds.put(names[i], values[i]);

		HashMap binding = SCollection.createHashMap();
		for(int i=0; i<mbindings.length; i++)
		{
			try
			{
				// Add model bindings if not already present.
				if(!binds.containsKey(mbindings[i].getName()))
				{
					binds.put(mbindings[i].getName(), ((RExpression)binding_exps
						.get(mbindings[i].getName())).getValue(params));
				}
			}
			catch(Exception e)
			{
				StringWriter	sw	= new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				//RCapability.getScope(this).getLogger().log(Level.SEVERE,
				//	"Bindings of element "+getModelElement().getOwner()+" could not be evaluated: "+bindings.get(name)+"\n"+sw);
				System.out.println("Bindings could not be evaluated: "+binding_exps.get(mbindings[i].getName())+"\n"+sw);
				return ret;	// Empty array means no binding found.
			}
		}
		
		return SUtil.calculateCartesianProduct((String[])binds.getKeys(String.class), 
			(Object[])binds.values().toArray(new Object[binds.size()]));
	}

	/**
	 *  Main for testing.
	 *  @param args The arguments.
	 */
	public static void main(String[] args)
	{
		String[] names = new String[]{"a", "b", "c"};
		Object[] values = new Object[]{new int[]{1,2}, new int[]{3,4}, new int[]{5,6}};
		
		// Iterate through binding sets for subsequent bindings.
		List ret = SUtil.calculateCartesianProduct(names, values);
		
		System.out.println(ret);
	}
}
