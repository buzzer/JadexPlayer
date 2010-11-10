package jadex.runtime.impl.agenda;

import jadex.util.SReflect;
import jadex.util.collection.SCollection;
import jadex.runtime.impl.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *  Abstract super class for all agenda actions.
 */
public abstract class AbstractAgendaAction implements IAgendaAction, IEncodable, Serializable
{
	//-------- attributes --------

	/** The precondition. */
	protected IAgendaActionPrecondition precondition;

	//-------- constructors --------

	/**
	 *  Create a deliberation action.
	 */
	public AbstractAgendaAction(IAgendaActionPrecondition precondition)
	{
		this.precondition = precondition;
	}

	//-------- methods --------

	/**
	 *  Execute the action.
	 */
	public abstract void execute();

	/**
	 *  Test if precondition is valid.
	 */
	public boolean isValid()
	{
		return precondition==null || precondition.check();
	}

	/**
	 *  Get the precondition.
	 *  @return The precondition.
	 */
	protected IAgendaActionPrecondition getPrecondition()
	{
		return precondition;
	}

	/**
	 *  Add a conjunctive precondition.
	 *  @param precondition The precondition.
	 */
	public void addPrecondition(IAgendaActionPrecondition precondition)
	{
		if(!(this.precondition instanceof ComposedPrecondition))
			this.precondition = new ComposedPrecondition(this.precondition);
		((ComposedPrecondition)this.precondition).addPrecondition(precondition);
	}

	/**
	 *  Get the cause of this action.
	 *  @return The "cause" of this action
	 * /
	public Object getCause()
	{
		return null;
	}*/

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return SReflect.getInnerClassName(this.getClass());//+")";
	}

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		HashMap rep	= SCollection.createHashMap();
		rep.put("isencodeablepresentation", "true"); // to distinguish this map from normal maps.
		return rep;
	}
}
