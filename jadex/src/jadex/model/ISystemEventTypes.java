package jadex.model;

import java.util.ArrayList;
import java.util.Collection;

import jadex.util.collection.*;


/**
 *  This interface specifies the types of system events
 *  and the inheritance relationships.
 */
// Hack!!! Shouldn't be in model, but required for affected settings!?
public interface ISystemEventTypes
{
	//-------- generic types --------

	/** This constant can be used to match all event types. */
	public static final String	TYPE_ANY	= "any";

	/** The system event type for all bdi element specific internal events. */
	public static final String	EVENT_TYPE_BDI	= "BDI";
	
	/** System event type for agent related events. */
	public static final String	AGENT_EVENT	= "agent";

	/** System event type for the birth of an agent.*/
	public static final String	AGENT_BORN	= AGENT_EVENT+"Born";
	
	/** System event type for an agent when moving to end state.. */
	public static final String	AGENT_TERMINATING	= AGENT_EVENT+"Terminating";

	/** System event type for the final removal of the agent. */
	public static final String	AGENT_DIED	= AGENT_EVENT+"Died";

	/** System event type for capability related events. */
	public static final String	CAPABILITY_EVENT	= EVENT_TYPE_BDI+"_capability";

	/** System event type for capability added.*/
	public static final String	CAPABILITY_ADDED	= CAPABILITY_EVENT+"Added";
	
	/** System event type for capability removed. */
	public static final String	CAPABILITY_REMOVED	= CAPABILITY_EVENT+"Removed";

	//-------- beliefbase types --------

	/** System event type for belief(set) related events. */
	public static final String	BELIEF_EVENT	= EVENT_TYPE_BDI+"_belief";

	/** System event type for belief(set) added. */
	public static final String	BELIEF_ADDED	= BELIEF_EVENT+"Added";

	/** System event type for belief(set) removed. */
	public static final String	BELIEF_REMOVED	= BELIEF_EVENT+"Removed";

	/** <code>FACT_EVENT</code>: is a base event type concerning belief facts */
	public static final String FACT_EVENT = EVENT_TYPE_BDI+"_fact";
  
	/** System event type for fact related events. */
	public static final String FACT_CHANGED = FACT_EVENT + "Changed";
  
	/** <code>FACT_READ</code>: thrown on belief access via belief base */
	public static final String FACT_READ    = "_"+FACT_EVENT + "Read";

	/** System event type for (belief set) fact related events. */
	public static final String BSFACT_EVENT = EVENT_TYPE_BDI+"_bsfact";
	
	/** System event type for single fact changed. */
	public static final String BSFACT_CHANGED = BSFACT_EVENT+"Changed";
	
	/** System event type for some facts changed/added/removed. */
	public static final String BSFACTS_CHANGED = BSFACT_EVENT+"sChanged";
	
	/** System event type for fact added. */
	public static final String BSFACT_ADDED = BSFACT_EVENT+"Added";
	
	/** System event type for fact removed. */
	public static final String BSFACT_REMOVED = BSFACT_EVENT+"Removed";

	/** <code>BSFACT_READ</code>: thrown if a belief set is accessed via belief base */
	public static final String BSFACT_READ    = BSFACT_EVENT+"Read";

	//-------- goalbase types --------

	/** System event type for goal related events. */
	public static final String GOAL_EVENT = EVENT_TYPE_BDI+"_goal";

	/** System event type for goal added. */
	public static final String GOAL_ADDED = GOAL_EVENT+"Added";

	/** System event type for goal removed. */
	public static final String GOAL_REMOVED = GOAL_EVENT+"Removed";

	/** System event type for goal changed. */
	public static final String GOAL_CHANGED = GOAL_EVENT+"Changed";

	/** System event type for value changed (of parameter). */
	public static final String VALUE_CHANGED = EVENT_TYPE_BDI+"_valueChanged";

	/** System event type for (element set) value related events. */
	public static final String ESVALUE_EVENT = EVENT_TYPE_BDI+"_esValue";

	/** System event type for value changed. */
	public static final String ESVALUE_CHANGED = ESVALUE_EVENT+"Changed";

	/** System event type for some values changed/added/removed. */
	public static final String ESVALUES_CHANGED = ESVALUE_EVENT+"sChanged";

	/** System event type for value added. */
	public static final String ESVALUE_ADDED = ESVALUE_EVENT+"Added";

	/** System event type for value added. */
	public static final String ESVALUE_REMOVED = ESVALUE_EVENT+"Removed";

	/** System event type for binding (of plan/goal with creation condition) changed. */
	public static final String BINDING_EVENT = EVENT_TYPE_BDI+"_bindingChanged";

	//-------- planbase types --------

	/** System event type for plan related events. */
	public static final String PLAN_EVENT = EVENT_TYPE_BDI+"_plan";

	/** System event type for plan added. */
	public static final String PLAN_ADDED = PLAN_EVENT+"Added";

	/** System event type for plan removed. */
	public static final String PLAN_REMOVED = PLAN_EVENT+"Removed";

	/** System event type for plan changed. */
	public static final String PLAN_CHANGED = PLAN_EVENT+"Changed";
	
	//-------- eventbase types --------
	
	/** Message events */
	public static final String MESSAGE_EVENT = "message_";

	/** Indicates a received message */
	public static final String MESSAGE_RECEIVED = MESSAGE_EVENT + "received";

	/** Posted if the agent has send a message */
	public static final String MESSAGE_SENT = MESSAGE_EVENT + "sent";
	
	/** Internal events */
	public static final String INTERNAL_EVENT = "internalevent_";

	/** Indicates that the agent has dispatched an internal event */
	public static final String INTERNAL_EVENT_OCCURRED = INTERNAL_EVENT + "occurred";

	//-------- expression base types --------
	
	/** Condition triggered events */
	public static final String CONDITION_TRIGGERED = "condition_triggered";
	
	//-------- steppable types (hack!!!) --------

	/** The generic steppable system event type. */
	public static final String EVENT_TYPE_STEPPABLE = "steppable_";

	/** The system event type that the execution mode changed. */
	public static final String AGENDA_MODE_CHANGED = EVENT_TYPE_STEPPABLE+"agenda_mode_changed";

	/** The system event type that a scheduler step is finished. */
	public static final String AGENDA_STEP_DONE = EVENT_TYPE_STEPPABLE+"agenda_step_done";

	/** The system event type that a dispatcher step count changed. */
	public static final String AGENDA_STEPS_CHANGED = EVENT_TYPE_STEPPABLE+"agenda_steps_changed";

	/** The system event type that a dispatcher step count changed. */
	public static final String AGENDA_CHANGED = EVENT_TYPE_STEPPABLE+"agenda_changed";
  
//	/** The system event type that the execution mode changed. */
//	public static final String DISPATCHER_MODE_CHANGED = EVENT_TYPE_STEPPABLE+"dispatcher_mode_changed";
//
//	/** The system event type that a dispatcher step is finished. */
//	public static final String DISPATCHER_STEP_DONE = EVENT_TYPE_STEPPABLE+"dispatcher_step_done";
//
//	/** The system event type that a dispatcher step count changed. */
//	public static final String DISPATCHER_STEPS_CHANGED = EVENT_TYPE_STEPPABLE+"dispatcher_steps_changed";
//
//	/** The system event type that the execution mode changed. */
//	public static final String SCHEDULER_MODE_CHANGED = EVENT_TYPE_STEPPABLE+"scheduler_mode_changed";
//
//	/** The system event type that a scheduler step is finished. */
//	public static final String SCHEDULER_STEP_DONE = EVENT_TYPE_STEPPABLE+"scheduler_step_done";
//
//	/** The system event type that a dispatcher step count changed. */
//	public static final String SCHEDULER_STEPS_CHANGED = EVENT_TYPE_STEPPABLE+"scheduler_steps_changed";
//
//	/** The system event type that an element was added to the event list. */
//	public static final String EVENT_LIST_ELEMENT_ADDED = EVENT_TYPE_STEPPABLE+"event_list_element_added";
//
//	/** The system event type that an element was removed from the event list. */
//	public static final String EVENT_LIST_ELEMENT_REMOVED = EVENT_TYPE_STEPPABLE+"event_list_element_removed";
//
//	/** The system event type that an element was added to the ready list. */
//	public static final String READY_LIST_ELEMENT_ADDED = EVENT_TYPE_STEPPABLE+"ready_list_element_added";
//
//	/** The system event type that an element was removed from the ready list. */
//	public static final String READY_LIST_ELEMENT_REMOVED = EVENT_TYPE_STEPPABLE+"ready_list_element_removed";
//
	
	//-------- inheritance relationships --------

	/**
	 *  Subtypes have to be defined in inner class, because
	 *  static initializers not possible in interfaces.
	 */
	public static class	Subtypes
	{
		/** The type->subtypes mapping. */
		protected static final MultiCollection	subtypes	= new MultiCollection();
	
		static
		{
			// Only add direct relationships (omit transitives).
			subtypes.put(TYPE_ANY, EVENT_TYPE_BDI);

			subtypes.put(AGENT_EVENT, AGENT_BORN);
			subtypes.put(AGENT_EVENT, AGENT_TERMINATING);
			subtypes.put(AGENT_EVENT, AGENT_DIED);

			subtypes.put(EVENT_TYPE_BDI, CAPABILITY_EVENT);
			subtypes.put(EVENT_TYPE_BDI, BELIEF_EVENT);
			subtypes.put(EVENT_TYPE_BDI, FACT_EVENT);
			subtypes.put(EVENT_TYPE_BDI, BSFACT_EVENT);
			subtypes.put(EVENT_TYPE_BDI, GOAL_EVENT);
			subtypes.put(EVENT_TYPE_BDI, VALUE_CHANGED);
			subtypes.put(EVENT_TYPE_BDI, ESVALUE_EVENT);
			subtypes.put(EVENT_TYPE_BDI, BINDING_EVENT);
			subtypes.put(EVENT_TYPE_BDI, PLAN_EVENT);
			subtypes.put(EVENT_TYPE_BDI, CONDITION_TRIGGERED);

			subtypes.put(CAPABILITY_EVENT, CAPABILITY_ADDED);
			subtypes.put(CAPABILITY_EVENT, CAPABILITY_REMOVED);
			
			subtypes.put(BELIEF_EVENT, BELIEF_ADDED);
			subtypes.put(BELIEF_EVENT, BELIEF_REMOVED);
			
			subtypes.put(GOAL_EVENT, GOAL_ADDED);
			subtypes.put(GOAL_EVENT, GOAL_REMOVED);
			subtypes.put(GOAL_EVENT, GOAL_CHANGED);
			
			subtypes.put(PLAN_EVENT, PLAN_ADDED);
			subtypes.put(PLAN_EVENT, PLAN_REMOVED);
			subtypes.put(PLAN_EVENT, PLAN_CHANGED);

			subtypes.put(FACT_EVENT, FACT_CHANGED);
			subtypes.put(FACT_EVENT, FACT_READ);
      
			subtypes.put(BSFACT_EVENT, BSFACT_READ);
			subtypes.put(BSFACT_EVENT, BSFACTS_CHANGED);
			subtypes.put(BSFACTS_CHANGED, BSFACT_ADDED);
			subtypes.put(BSFACTS_CHANGED, BSFACT_REMOVED);
			subtypes.put(BSFACTS_CHANGED, BSFACT_CHANGED);

			subtypes.put(ESVALUE_EVENT, ESVALUES_CHANGED);
			subtypes.put(ESVALUES_CHANGED, ESVALUE_ADDED);
			subtypes.put(ESVALUES_CHANGED, ESVALUE_REMOVED);
			subtypes.put(ESVALUES_CHANGED, ESVALUE_CHANGED);
			
			subtypes.put(INTERNAL_EVENT, INTERNAL_EVENT_OCCURRED);
			
			subtypes.put(MESSAGE_EVENT, MESSAGE_RECEIVED);
			subtypes.put(MESSAGE_EVENT, MESSAGE_SENT);
			
			subtypes.put(TYPE_ANY, EVENT_TYPE_STEPPABLE);
			subtypes.put(EVENT_TYPE_STEPPABLE, AGENDA_MODE_CHANGED);
			subtypes.put(EVENT_TYPE_STEPPABLE, AGENDA_STEPS_CHANGED);
			subtypes.put(EVENT_TYPE_STEPPABLE, AGENDA_STEP_DONE);
			subtypes.put(EVENT_TYPE_STEPPABLE, AGENDA_CHANGED);
		}

		/**
		 *  Get all (transitive) subtypes of a given event type
		 *  including the base type.
		 */
		public static Collection	getSubtypes(String basetype)
		{
			ArrayList	ret	= SCollection.createArrayList();
			ret.add(basetype);
			for(int i=0; i<ret.size(); i++)
			{
				ret.addAll(subtypes.getCollection(ret.get(i)));
			}
			return ret;
		}

		/**
		 *  Check if an event type is a (sub) type
		 *  of any event of a given set of event types.
		 *  @param event	The system event type.
		 *  @param types	The system event types.
		 */
		public static boolean	isSubtype(String event, String[] types)
		{
			boolean	found	= false;
			for(int i=0; !found && i<types.length; i++)
			{
				Collection	stypes	= ISystemEventTypes.Subtypes.getSubtypes(types[i]);
				found	= stypes.contains(event);
			}
			return found;
		}
	}
}
