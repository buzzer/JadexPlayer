package jadex.runtime;

import jadex.runtime.impl.IRGoal;
import jadex.runtime.impl.IRGoalEvent;


/**
 *  A goal filter to match against goals,
 *  which are contained in goal events,
 *  i.e. the contained values and filters of this filter
 *  are not applied to the event, but to the goal.
 *  The implicit event filter cannot be finetuned.
 */
public class GoalEventFilter	extends ParameterElementFilter
{
	//-------- attributes --------

	/** The event type to match (true for info events). */
	protected boolean	info;

	/** The goal type to match. */
	protected String	type;

	/** The goal name to match (optional). */
	protected String	name;

	//-------- constructors --------

	/**
	 *  Create a goal event filter to match against process goal events.
	 *  @param type	The goal type.
	 */
	public GoalEventFilter(String type)
	{
		this(type, null, false);
	}

	/**
	 *  Create a goal event filter to match against goal events.
	 *  @param type	The goal type.
	 *  @param info Match info (or process) events.
	 */
	public GoalEventFilter(String type, boolean info)
	{
		this(type, null, info);
	}

	/**
	 *  Create a goal event filter to match against goal events.
	 *  @param type	The goal type.
	 *  @param name	The goal (instance) name.
	 *  @param info Match info (or process) events.
	 */
	public GoalEventFilter(String type, String name, boolean info)
	{
		this.type	= type;
		this.name	= name;
		this.info	= info;
	}

	//-------- IFilter methods --------

	/**
	 *  Match an object against the filter.
	 *  @param object The object.
	 *  @return True, if the filter matches.
	 * @throws Exception
	 */
	public boolean filter(Object object) throws Exception
	{
		if(object instanceof jadex.runtime.planwrapper.ElementWrapper)
		{
			object	= ((jadex.runtime.planwrapper.ElementWrapper)object).unwrap();
		}
		else if(object instanceof jadex.runtime.externalaccesswrapper.ElementWrapper)
		{
			object	= ((jadex.runtime.externalaccesswrapper.ElementWrapper)object).unwrap();
		}

		boolean	ret	= false;
		if(object instanceof IRGoalEvent)
		{
			IRGoalEvent	event	= (IRGoalEvent)object;
			if(event.isInfo()==info)
			{
				IRGoal	goal	= event.getGoal();
				ret	= (type==null || goal.getType().equals(type))
					&& (name==null || goal.getName().equals(name))
					&& super.filter(goal);
			}
		}
		return ret;
	}
}
