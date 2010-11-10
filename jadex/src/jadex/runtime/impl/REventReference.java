package jadex.runtime.impl;

import jadex.model.*;

/**
 *  The reference to an event.
 */
public abstract class REventReference extends RParameterElementReference implements IREvent
{
	//-------- constructor --------

	/**
	 *  Create a new event.
	 *  @param name The name.
	 *  @param event The event model element.
	 *  @param config The configuration.
	 *  @param owner The owner.
	 */
	protected REventReference(String name, IMEventReference event,
			IMConfigParameterElement config, RElement owner, RReferenceableElement creator)
	{
		super(name, event, config, owner, creator);
	}

	//-------- BDI event properties --------

	/**
	 *  Is it a post-to-all event.
	 *  @return True, if post-to-all is set.
	 */
	public boolean isPostToAll()
	{
		return ((IREvent)getReferencedElement()).isPostToAll();
	}

	/**
	 *  Get the random selection flag.
	 *  @return True, when applicable
	 *  selection is random style.
	 */
	public boolean	isRandomSelection()
	{
		return ((IREvent)getReferencedElement()).isRandomSelection();
	}

	//-------- methods ---------

	/**
	 *  Get the goal type.
	 *  @return The goal type.
	 * /
	public String	getType()
	{
		return getModelElement().getName();
	}*/

	/**
	 *  Called when the event is dispatched.
	 */
	public void dispatched()
	{
		((IREvent)getReferencedElement()).dispatched();
	}

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(final String name)
	{
		return ((IREvent)getReferencedElement()).hasParameter(name);
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(final String name)
	{
		return ((IREvent)getReferencedElement()).hasParameterSet(name);
	}

	/**
	 *  Get (or create) the apl for the event.
	 *  @return The apl.
	 */
	public ApplicableCandidateList getApplicableCandidatesList()
	{
		return ((IREvent)getReferencedElement()).getApplicableCandidatesList();
	}
}