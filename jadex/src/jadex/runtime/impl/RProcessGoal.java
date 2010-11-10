package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.goals.*;
import jadex.util.collection.SCollection;
import java.util.*;

/**
 *  This is the runtime object for a goal, that
 *  represents the goal processing side(s).
 */
public class RProcessGoal extends RReferenceableElement implements IRParameterElement
{
	/* The state types of a process goal. */

	/** It is worked on this process goal via the associated plan. */
	public static final String	PROCESS_STATE_IN_PROCESS	= "in_process";

	/** The state is finished and the plan did fail. */
	public static final String	PROCESS_STATE_FAILED	= "failed";

	/** The state is finished and the plan did succeed. */
	public static final String	PROCESS_STATE_SUCCEEDED	= "succeeded";

	/** The state is finished and the plan execution has been aborted. */
	public static final String	PROCESS_STATE_ABORTED_ON_FAILURE	= "aborted_on_failure";

	/** The state is finished and the plan execution has been aborted. */
	public static final String	PROCESS_STATE_ABORTED_ON_SUCCESS = "aborted_on_success";

	//-------- attributes --------

	/** The proprietary goal. */
	protected IRGoal propgoal;

	/** The plan instance. */
	protected RPlan rplan;

	/** The execution time. */
	protected long	time;

	/** The subgoals. */
	protected List subgoals;

	/** The processing state. */
	protected String	processingstate;

	/** The exception (if any). */
	protected Exception exception;

	/** The candidate info. */
	protected ICandidateInfo candidateinfo;

	//-------- parameters --------

	/** The parameters. */
	protected Map parameters;

	/** The parameter sets. */
	protected Map parametersets;

	/** The protection mode for the parameters. */
	protected String protectionmode;

	//-------- constructor --------

	/**
	 *  Create a new goal.
	 *  @param propgoal The proprietary goal.
	 *  @param owner The owner.
	 */
	protected RProcessGoal(IRGoal propgoal, RElement owner, ICandidateInfo cand)
	{
		super(propgoal!=null? createProcessGoalName(propgoal): null,
			propgoal!=null? (IMReferenceableElement)propgoal.getModelElement()
				: ((IMCapability)owner.getScope().getModelElement())
			.getGoalbase().getGoal(IMGoalbase.DUMMY_GOAL), null, owner, null, null);

		this.propgoal = propgoal;
		this.candidateinfo = cand;
    	this.subgoals	= SCollection.createArrayList();
    	this.processingstate	= PROCESS_STATE_IN_PROCESS;
		this.parameters = SCollection.createHashMap();
		this.parametersets = SCollection.createHashMap();

		// The following code should be in createParameters() but cannot
		// due to missing propgoal assignment :-(
		if(propgoal!=null)
		{
			// Copy parameters.
			IRParameter[] params = propgoal.getParameters();
			for(int i=0; i<params.length; i++)
			{
				Object paramclone = new RProcessGoalParameter(params[i], this);
				parameters.put(params[i].getModelElement().getName(), paramclone);
			}

			IRParameterSet[] paramsets = propgoal.getParameterSets();
			for(int i=0; i<paramsets.length; i++)
			{
				Object paramclone = new RProcessGoalParameterSet(paramsets[i], this);
				parametersets.put(paramsets[i].getModelElement().getName(), paramclone);
			}
		}

		// Set execution start time.
		this.time	= System.currentTimeMillis();
		setParameterProtectionMode(RParameterElement.ACCESS_PROTECTION_PROCESSING);

		this.init();	// Hack???
	}

	//-------- methods --------

	/**
	 *  Fetch (create or get) the abstract elements to which this element points.
	 */
	protected void fetchAssignToElements()
	{
		// The process goal has per definition no references.
		// Method must be overridden as its modelelement
		// (from propgoal) may have references
	}

	/**
	 *  Create strong exports (i.e. outer references).
	 */
	protected void fetchStrongExports()
	{
		// The process goal has per definition no references.
		// Method must be overridden as its modelelement
		// (from propgoal) may have exports
	}

	/**
	 *  Get the subgoals.
	 *  @return The subgoals.
	 */
	public IRGoal[]	getSubgoals()
	{
		return (IRGoal[])subgoals.toArray(new IRGoal[subgoals.size()]);
	}

	/**
	 *  Add a subgoal.
	 *  @param subgoal The new subgoal.
	 */
	protected void	addSubgoal(IRGoal subgoal)
	{
		assert !subgoal.isSubgoal()
			: "Cannot add subgoal which has already parent! "+subgoal;
		assert subgoal.getScope()==getScope()
			: "Cannot add subgoal from different scope! "+subgoal;

		this.subgoals.add(subgoal);
		subgoal.setParent(this);
	}

	/**
	 *  Remove a subgoal.
	 *  @param subgoal The subgoal to remove.
	 */
	protected void	removeSubgoal(IRGoal subgoal)
	{
		assert subgoal.getParent()==this
			: "Cannot remove subgoal (no such child)! "+subgoal;
		
		boolean removed	= this.subgoals.remove(subgoal);
		assert removed : this +", "+ subgoal;

		// Trigger cleanup of goal, when last process goal was removed.
		if(!PROCESS_STATE_IN_PROCESS.equals(processingstate))
			planOrSubgoalFinished();


		// Do not reset parent, as it is required e.g. for agenda-precodition.
		// Hack!!! Is this a memory leak?
//		subgoal.setParent(null);
	}

	/**
	 *  Check if the goal is adopted.
	 *  @return True, if the goal is adopted.
	 * /
	public boolean 	isAdopted()
	{
		// Hack!!! Dummy goals are always adopted ???
/*		return propgoal!=null && propgoal.isAdopted()
			|| propgoal==null && getModelElement()==getScope().DUMMY;
* /		return getScope().getGoalbase().contains(this);	// Hack ???
	}*/

	/**
	 *  Check if the goal is in some finished state.
	 *  @return True, if the goal is finished.
	 */
	public boolean 	isFinished()
	{
		return PROCESS_STATE_SUCCEEDED.equals(processingstate)
			|| PROCESS_STATE_FAILED.equals(processingstate)
			|| PROCESS_STATE_ABORTED_ON_FAILURE.equals(processingstate)
			|| PROCESS_STATE_ABORTED_ON_SUCCESS.equals(processingstate);
	}

	/**
	 *  Check if the goal is succeeded.
	 *  @return True, if the goal is succeeded.
	 */
	public boolean 	isSucceeded()
	{
		return PROCESS_STATE_SUCCEEDED.equals(processingstate);
	}

	/**
	 *  Check if the goal has failed.
	 *  @return True, if the goal has failed.
	 */
	public boolean 	isFailed()
	{
		return PROCESS_STATE_FAILED.equals(processingstate);
	}

	/**
	 *  Check if the goal has been aborted.
	 *  @return True, if the goal has failed.
	 */
	public boolean 	isAborted()
	{
		return PROCESS_STATE_ABORTED_ON_FAILURE.equals(processingstate)
			|| PROCESS_STATE_ABORTED_ON_SUCCESS.equals(processingstate);
	}

	/**
	 *  Check if the goal was aborted because the proprietary
	 *  goal succeeded during the plan was running.
	 *  @return True, if the goal was aborted on success of the proprietary goal.
	 */
	public boolean isAbortedOnSuccess()
	{
		return PROCESS_STATE_ABORTED_ON_SUCCESS.equals(processingstate);
	}

    /**
     *  Drop the goal, when plan has failed.
     *  @param reason	The failure reason.
     */
	public void	fail(Exception reason)
	{
		setException(reason);
		drop(PROCESS_STATE_FAILED);
    }

    /**
     *  Drop the goal, when plan has succeeded.
     */
	public void	succeed()
	{
		drop(PROCESS_STATE_SUCCEEDED);
    }

    /**
     *  Abort the goal.
	 *  @param aborted_on_success The goal is aborted
	 *  on success of the proprietary goal.
     */
	public void	abort(boolean aborted_on_success)
	{
		if(aborted_on_success)
			drop(PROCESS_STATE_ABORTED_ON_SUCCESS);
		else
			drop(PROCESS_STATE_ABORTED_ON_FAILURE);
    }

	/**
	 *  The proprietary goal.
	 *  @return The proprietary goal
	 */
	public IRGoal getProprietaryGoal()
	{
		return propgoal;
	}

	/**
	 *  Get the execution time.
	 *  This is provided for informational and debugging purposes.
	 *  While the goal is running, returns the time, when it was created.
	 *  Once the goal is finished, returns the total execution time
	 *  (including waiting times) in milliseconds.
	 *  @return The execution time.
	 */
	public long	getExecutionTime()
	{
		return this.time;
	}

	/**
	 *  Get the goal processing state.
	 *  @return The processing state.
	 */
	public String 	getProcessingState()
	{
		assert this.processingstate!=null;
		return this.processingstate;
	}

	/**
	 *  Set a state for the goal.
	 *  @param processingstate The state.
	 */
	protected void	setProcessingState(String processingstate)
	{
		// Each process goal only has one state transition:
		// From in-process to either succeeded, failed, or aborted.
		assert PROCESS_STATE_IN_PROCESS.equals(this.processingstate)
			: this.processingstate+ ", "+this+", planfinished: "+rplan.isFinished();
		assert PROCESS_STATE_SUCCEEDED.equals(processingstate)
			|| PROCESS_STATE_FAILED.equals(processingstate)
			|| PROCESS_STATE_ABORTED_ON_FAILURE.equals(processingstate)
			|| PROCESS_STATE_ABORTED_ON_SUCCESS.equals(processingstate)
			: processingstate;

		this.processingstate = processingstate;
//		throwSystemEvent(SystemEvent.GOAL_CHANGED);
	}

	/**
	 *  Set the exception for the goal.
	 *  This is a convenience method, as the goal exception
	 *  is stored as property.
	 *  @param exception The exception.
	 */
	public void	setException(Exception exception)
	{
		this.exception = exception;
	}

	/**
	 *  Get the exception of the goal.
	 *  This is a convenience method, as the goal exception
	 *  is stored as property.
	 *  @return The exception value.
	 */
	public Exception	getException()
	{
		return this.exception;
	}

	/**
	 *  Set the result for the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @param result The result.
	 */
	public void	setResult(Object result)
	{
		//System.out.println("::: "+result+" "+this);
		findResultParemeter().setValue(result);
//		throwSystemEvent(SystemEvent.GOAL_CHANGED);	// Hack??? todo: what for?
	}

	/**
	 *  Get the result of the goal.
	 *  This is a convenience method, as the goal result
	 *  is stored as property.
	 *  @return The result value.
	 * /
	public Object	getResult()
	{
		return findResultParemeter().getValue();
	}*/

	//-------- helper methods --------

	/**
	 *  Find result parameter.
	 *  The result parameter is a unique parameter of direction "out".
	 *  @throws RuntimeException	When no unique out parameter found.
	 */
	public IRParameter	findResultParemeter()
	{
		IMParameter	mret	= null;
		IMParameter[]	mparams	= ((IMGoal)getModelElement()).getParameters();
		for(int i=0; i<mparams.length; i++)
		{
			if(mparams[i].getDirection().equals(IMParameter.DIRECTION_OUT)
				|| mparams[i].getDirection().equals(IMParameter.DIRECTION_INOUT))
			{
				if(mret==null)
				{
					mret	= mparams[i];
				}
				else
				{
					throw new RuntimeException("No unique out parameter: "+this);
				}
			}
		}
		/*if(mret==null)
		{
			throw new RuntimeException("No out parameter found: "+this);
		}*/

		return mret==null? null: getParameter(mret.getName());
	}

	/**
	 *  Get the plan instance.
	 *  @param rplan The plan instance.
	 */
	protected void setPlanInstance(RPlan rplan)
	{
		this.rplan = rplan;
	}

	/**
	 *  Get the plan instance.
	 * @return The plan instance.
	 */
	public RPlan getPlanInstance()
	{
		return rplan;
	}

	/**
	 *  Get the dispatched candidate info.
	 */
	public ICandidateInfo getCandidateInfo()
	{
		return this.candidateinfo;
	}

	/**
	 *  Drop this goal and terminate the associated plan.
	 *  Also drops all subgoals.
	 */
	protected void	drop(String finalstate)
	{
		// When dropping an already dropping goal, ignore (hack???).
		if(!PROCESS_STATE_IN_PROCESS.equals(processingstate))
			return;
		
//		if(rplan.getName().indexOf("request_initiator_plan")!=-1)
//			System.out.println("drop "+this);
		
		// Set final state.
		setProcessingState(finalstate);
		
		// Calculate total execution time.
		this.time	= System.currentTimeMillis() - this.time;

		// Terminate plan processing this goal.
		rplan.terminate();

		// Drop all subgoals, too.
		IRGoal[]	subgoals	= getSubgoals();
		for(int i=0; i<subgoals.length; i++)
		{
			// Dropping of other goal is decoupled. This ensures that e.g.
			// already enqueued finished actions are executed before.
			//subgoals[i].drop();
			getScope().getAgent().getInterpreter().addAgendaEntry(new DropGoalAction(
				new GoalLifecycleStatePrecondition(subgoals[i], IGoal.LIFECYCLESTATE_ADOPTED), subgoals[i]), null); // todo: cause?
		}
	}

//protected boolean waittest;	// Used for debugging (remove).
	
	/**
	 *  When a process goal is dropped, it has to wait before its plan
	 *  and all subgoals are terminated/dropped, before removing itself.
	 */
	protected void	planOrSubgoalFinished()
	{
//		if(rplan.getName().indexOf("request_initiator_plan")!=-1)
//			System.out.println("planOrSubgoalFinished "+this);

		if(!rplan.isAlive() && subgoals.isEmpty())
		{
//			if(waittest)
//				System.out.println("Process goal finished waiting for plan/subgoal "+this);

			if(getProprietaryGoal()!=null)
	            ((RGoal)getProprietaryGoal().getOriginalElement()).processGoalFinished(this);
	
	        // Remove this goal.
			getScope().getGoalbase().removeGoal(this);
			// throwSystemEvent(SystemEvent.GOAL_REMOVED);
			cleanup();
		}
//		else
//		{
//			waittest	= true;
//			System.out.println("Process goal waiting for plan/subgoal "+this+", "+rplan.isAlive()+", "+subgoals);
//		}
	}

	/**
	 *  Generate a property representation for encoding this element
	 *  (eg to SL).
	 *  @return A properties object representing this element.
	 */
	public Map getEncodableRepresentation()
	{
		Map	representation	= super.getEncodableRepresentation();
		representation.put("plan", rplan==null ? "null" : rplan.getName());
		// Change goal kind to process (hack???)
		representation.put("kind", "process");
		representation.put("processingstate", processingstate);
		if(propgoal!=null)
		{
			representation.put("proprietarygoal", propgoal.getName());
		}
		return representation;
	}

	/**
	 *  Get the parameter protection mode.
	 *  @return The parameter protection mode.
	 */
	public String getParameterProtectionMode()
	{
		return protectionmode;
	}

	/**
	 *  Set the parameter protection mode.
	 *  @param protectionmode The protection mode.
	 */
	public void setParameterProtectionMode(String protectionmode)
	{
		this.protectionmode = protectionmode;
	}

	/**
	 *  Get all parameters.
	 *  @return All parameters.
	 */
	public IRParameter[]	getParameters()
	{
		return (IRParameter[])parameters.values().toArray(new IRParameter[parameters.size()]);
	}

	/**
	 *  Get all parameter sets.
	 *  @return All parameter sets.
	 */
	public IRParameterSet[]	getParameterSets()
	{
		return (IRParameterSet[])parametersets.values().toArray(new IRParameterSet[parametersets.size()]);
	}

	/**
	 *  Get the parameter element.
	 *  @param name The name.
	 *  @return The param.
	 */
	public IRParameter getParameter(String name)
	{
		IRParameter ret = (IRParameter)parameters.get(name);
		if(ret==null)
			throw new RuntimeException("No such parameter: "+name+" in "+this);
		return ret;
	}

	/**
	 *  Get the parameter set element.
 	 *  @param name The name.
	 *  @return The param set.
	 */
	public IRParameterSet getParameterSet(String name)
	{
		IRParameterSet ret = (IRParameterSet)parametersets.get(name);
		if(ret==null)
			throw new RuntimeException("No such parameter set: "+name+" in "+this);
		return ret;
	}

	/**
	 *  Has the element a parameter element.
	 *  @param name The name.
	 *  @return True, if it has the parameter.
	 */
	public boolean hasParameter(String name)
	{
		return parameters.get(name)!=null;
	}

	/**
	 *  Has the element a parameter set element.
	 *  @param name The name.
	 *  @return True, if it has the parameter set.
	 */
	public boolean hasParameterSet(String name)
	{
		return parametersets.get(name)!=null;
	}

	//-------- Hack!!! needed because of bean access --------

	/**
	 *  Get a value corresponding to a belief.
	 *  @param name The name identifiying the belief.
	 *  @return The value.
	 */
	public Object getParameterValue(String name)
	{
    	return getParameter(name).getValue();
	}

	 /**
	 *  Get all values corresponding to one beliefset.
	 *  @param name The name identifiying the beliefset.
	 *  @return The values.
	 */
	public Object[] getParameterSetValues(String name)
	{
		return getParameterSet(name).getValues();
	}

	/**
	 *  Copy the content of this process goal back into the
	 *  original goal.
	 */
	protected void copyContent()
	{
		IRParameter[] params = getParameters();
		for(int i=0; i<params.length; i++)
		{
			((RProcessGoalParameter)params[i]).copyContent();
		}

		IRParameterSet[] paramsets = getParameterSets();
		for(int i=0; i<paramsets.length; i++)
		{
			((RProcessGoalParameterSet)paramsets[i]).copyContent();
		}
	}

	/**
	 *	Test if the goal is adopted.  
	 */
	public boolean isAdopted()
	{
		return getPlanInstance()!=null && getPlanInstance().isAlive();
	}

	//-------- static part --------

	/** The process goal counter prop goals. */
	protected static Map	procount	= new WeakHashMap();

	/**
	 *  Create a name for a process goal.
	 *  @param goal	The proprietary goal.
	 *  @return The name for the process goal.
	 */
	protected static String	createProcessGoalName(IRGoal goal)
	{
		Integer	cnt	= (Integer)procount.get(goal);
		if(cnt==null)
		{
			cnt	= new Integer(1);
		}
		else
		{
			cnt	= new Integer(cnt.intValue()+1);
		}
		procount.put(goal, cnt);
		return goal.getName()+"_"+cnt;
	}
}

