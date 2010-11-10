package jadex.runtime.impl.agenda;

import java.util.*;
import jadex.util.collection.SCollection;

/**
 *  Container for combining preconditions (conjunctive).
 */
public class ComposedPrecondition implements IAgendaActionPrecondition
{
	//--------- attributes --------

	/** The sub-preconditions. */
	protected ArrayList subconditions;

	//-------- constructors --------

	/**
	 *  Create a new composed precondition.
	 */
	public ComposedPrecondition(IAgendaActionPrecondition subcond)
	{
		this.subconditions = SCollection.createArrayList();
		if(subcond!=null)
			subconditions.add(subcond);
	}

	/**
	 *  Create a new composed precondition.
	 */
	public ComposedPrecondition(IAgendaActionPrecondition conda, IAgendaActionPrecondition condb)
	{
		this.subconditions = SCollection.createArrayList();
		if(conda!=null)
			subconditions.add(conda);
		if(condb!=null)
			subconditions.add(condb);
	}

	//-------- methods --------

	/**
	 *  Test, if the precondition is valid.
	 *  @return True, if precondition is valid.
	 */
	public boolean check()
	{
		boolean ret = true;
		for(int i=0; i<subconditions.size() && ret; i++)
		{
			IAgendaActionPrecondition pc = (IAgendaActionPrecondition)subconditions.get(i);
			if(!pc.check())
				ret = false;
		}
		return ret;
	}

	/**
	 *  Add a precondition.
	 *  @param precondition The precondition.
	 */
	public void addPrecondition(IAgendaActionPrecondition precondition)
	{
		if(precondition!=null && !subconditions.contains(precondition))
			subconditions.add(precondition);
	}

	/**
	 *  Get all contained preconditions.
	 *  @return The contained preconditions.
	 */
	protected List getPreconditions()
	{
		return Collections.unmodifiableList(subconditions);
	}
}
