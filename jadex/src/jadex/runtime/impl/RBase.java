package jadex.runtime.impl;

import jadex.model.IMBase;
import jadex.model.IMElementReference;
import jadex.model.IMReferenceableElement;

import java.util.HashMap;
import java.util.Map;

/**
 *  The base class for all kinds of bases.
 */
public abstract class RBase extends RElement
{
	//-------- attributes --------

	/** The assignto specifications. */
	protected Map	assignments;

	//-------- constructors --------

	/**
	 *  Create a new base.
	 *  @param name The name.
	 *  @param base	The model of this element.
	 *  @param owner The owner of this element.
	 */
	protected RBase(String name, IMBase base, RElement owner)
	{
		super(name, base, owner);
		// Todo: use weak map (not serializable!) as MElements from other capabilities are stored. 
		this.assignments = new HashMap();

		// Initialize assignments.
		// Method fails for bases without referenceable elements.
		IMReferenceableElement[] res	= base.getReferenceableElements();
		for(int i=0; res!=null && i<res.length; i++)
		{
			IMElementReference[]	assignments	= res[i].getAssignToElements();
			for(int j=0; j<assignments.length; j++)
			{
				RCapability	scope	= this.getScope().getAgent()
					.lookupCapability(assignments[j].getScope());

				assert scope!=null: this;

				getCorrespondingBase(scope).setReferencedElement(assignments[j], res[i]);
			}
		}
	}

	//-------- abstract methods --------

	/**
	 *  Start this capability and its subcapabilities
	 *  with the given configuration.
	 *  Initialization is as follows:
	 *  <ol>
	 * 		<li>The constructor of the agent (and recursively the capabilities)
	 * 			is called, and leads to the creation of all bases.
	 * 		<li>The agent constructor calls the first init(0) method,
	 * 			which evaluates the properties and creates all beliefs.
	 * 		<li>After the constructor call returns, and the execution starts,
	 * 			the start agent action calls the second init(1), creating all the
	 * 			initial elements (e.g. goals and plans) and assigning values to the beliefs.
	 *  </ol> 
	 */
	protected void	init(int level)
	{
		// Empty default implementation.
	}
	
	/**
	 *  Get the runtime element for a model element.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param melement	The model of the element to be retrieved.
	 *  @param creator	The creator of the element (e.g. a reference).
	 */
	protected abstract RReferenceableElement	getElementInstance(
			IMReferenceableElement melement, RReferenceableElement creator);

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected abstract RBase	getCorrespondingBase(RCapability scope);

	/**
	 *  Get the referenced element for a reference.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param ref	The reference.
	 */
	protected RReferenceableElement	getReferencedElement(
		RElementReference ref, RReferenceableElement creator)
	{
		IMElementReference	mref	= (IMElementReference)ref.getModelElement();
		IMReferenceableElement	melement;
		RReferenceableElement	element;

		// Lookup reference (model layer).
		if(mref.isAbstract())
		{
			melement	= (IMReferenceableElement)assignments.get(mref);
		}
		else
		{
			melement	= mref.getReferencedElement();
		}

		// Get/create element instance.
		if(melement==null)
		{
			// pseudo reference of default element (e.g. goal event)
			element	= creator;
		}
		else if(creator!=null && melement==creator.getModelElement())
		{
			// references created by original element (e.g. assignTos).
			element	= creator;
		}
		else
		{
			element	= getElementInstance(melement, ref);
		}

		return element;
	}

	//-------- internal methods --------
	
	/**
	 *  Set the referenced element for a reference.
	 *  Used to initialize assignto settings.
	 *  @param mref	The reference to set.
	 *  @param melement	The element to be referenced.
	 */
	protected void	setReferencedElement(IMElementReference mref, IMReferenceableElement melement)
	{
		assignments.put(mref, melement);
	}

	/**
	 *  Exit the running state of this base.
	 *  Empty default implementation that
	 *  can be overridden, if necessary.
	 */
	public void exitRunningState()
	{
	}

	/**
	 *  Activate the end state of this base.
	 *  Empty default implementation that
	 *  can be overridden, if necessary.
	 */
	public void activateEndState()
	{
	}

	/**
	 *  Check if the end state of this base is terminated.
	 *  Empty default implementation that
	 *  can be overridden, if necessary.
	 *  @return true, when the agent can be safely deleted.
	 */
	public boolean isEndStateTerminated()
	{
		return true;
	}
	
	/**
	 *  Test if an element is protected against being removed
	 *  when changing from running -> terminating agent state.
	 *  Currently only elements are protected that are a direct
	 *  consequence of a plan that is in cleanup phase
	 *  (passed, aborted, failed).
	 */
	public boolean isProtected(IRElement elem)
	{
		boolean ret = false;
		
		if(elem instanceof RPlan)
		{
			// If elmenent is not protected by itself it might belong to a goal that
			// is protected (by belonging to a protected plan)
	
			RPlan plan = (RPlan)elem;
			String state = plan.getState();
			ret = state.equals(RPlan.STATE_ABORTED) || state.equals(RPlan.STATE_PASSED) 
				|| state.equals(RPlan.STATE_FAILED) 
				|| isProtected(plan.getRootGoal().getProprietaryGoal());
		}
		else if(elem instanceof IRGoal)
		{
			RGoal goal = (RGoal)((IRGoal)elem).getOriginalElement();
			ret = isProtected(goal.getRealParent());
		}
		else if(elem instanceof RProcessGoal)
		{
			ret = isProtected(((RProcessGoal)elem).getPlanInstance());
		}
		
		return ret;
	}
}
