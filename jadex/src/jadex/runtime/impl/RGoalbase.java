package jadex.runtime.impl;

import jadex.model.*;
import jadex.runtime.*;
import jadex.runtime.impl.agenda.conditions.ConditionDefaultPrecondition;
import jadex.runtime.impl.agenda.goals.GoalCreationAction;
import jadex.util.SReflect;
import jadex.util.collection.IndexMap;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  A goalbase instance containing running goals.
 */
public class RGoalbase	extends RBase
{
	//-------- attributes --------

	// Todo: Find a way to remove never adopted goals, when no longer needed.
//	/** The only created goals. */
//    protected IndexMap	createdgoals;

	/** The adopted goals. */
    protected IndexMap	adoptedgoals;

	/** The process goals. */
    protected IndexMap	processgoals;

	/** The bindings for (m)goals. */
	protected Map	bindings;
    
    /** The internal conditions (stored for later cleanup). */
    protected Map	conditions;
    
    /** The meta goals mapped from their filters (chached for speed). */
    protected Map	metagoalmap;
    
	//-------- constructors --------

	/**
	 *  Create a new goalbase instance.
	 *  @param model	The goalbase model.
	 *  @param owner	The owner of the instance.
	 */
	protected RGoalbase(IMGoalbase model, RElement owner)
	{
		super(null, model, owner);
//		this.createdgoals	= SCollection.createIndexMap();;
		this.adoptedgoals	= SCollection.createIndexMap();;
		this.processgoals	= SCollection.createIndexMap();;
		this.bindings	= SCollection.createHashMap();
		this.conditions	= SCollection.createHashMap();
	}

	/**
	 *  Initialize the goal base.
	 */
	protected void	init(int level)
	{
		// On constructor initialization, create metagoalmappings and creation conditions for the goal model elements.
		if(level==0)
		{
			metagoalmap	= SCollection.createHashMap();
			IMMetaGoal[] metagoals = ((IMGoalbase)getModelElement()).getMetaGoals();
			for(int i=0; i<metagoals.length; i++)
			{
				// Todo: store wait abstraction directly.
				metagoalmap.put(new WaitAbstraction(getScope()).createFilter(metagoals[i].getTrigger()) ,metagoals[i]);
			}
			
			
			IMGoal[] mgoals = ((IMGoalbase)getModelElement()).getGoals();
			for(int i=0; i<mgoals.length; i++)
			{
				registerGoal(mgoals[i]);
			}
		}

		// On action init, create and dispatch initial goals.
		if(level==1)
		{
			IMConfiguration is = getScope().getConfiguration();
			if(is!=null)
			{
				IMConfigGoalbase	initialbase	= is.getGoalbase();
				if(initialbase!=null)
				{
					IMConfigGoal[] inigoals = initialbase.getInitialGoals();
					instantiateConfigGoals(inigoals);
				}
			}
		}
	}

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;

		//System.out.println("Cleanup: "+getName());
		super.cleanup();

		// Cleanup bindings.
		for(Iterator i=bindings.values().iterator(); i.hasNext(); )
		{
			((BindingHelper)i.next()).cleanup();
		}

		// Cleanup conditions.
		for(Iterator i=conditions.values().iterator(); i.hasNext(); )
		{
			((IRCondition)i.next()).cleanup();
		}

		// Drop outstanding goals.
		for(int i=0; i<adoptedgoals.size(); i++)
		{
			// Shouldn't cleanup original goals?
			((IRGoal)adoptedgoals.get(i)).drop();
		}

//		for(int i=0; i<createdgoals.size(); i++)
//		{
//			// Shouldn't cleanup original goals?
//			((IRGoal)createdgoals.get(i)).getOriginalElement().cleanup();
//		}

		// Drop remaining (i.e. dummy) process goals.
		for(int i=0; i<processgoals.size(); i++)
		{
			// Todo: introduce new state aborted_on_deadagent ???
			((RProcessGoal)processgoals.get(i)).drop(RProcessGoal.PROCESS_STATE_ABORTED_ON_FAILURE);
		}
	}

	//-------- interface methods --------

	/**
	 *  Get a (proprietary) adopted goal by name.
	 *  @param name	The goal name.
	 *  @return The goal (if found).
	 */
	public IRGoal getGoal(String name)
	{
		return (IRGoal)adoptedgoals.get(name);
	}

	/**
	 *  Test if an adopted goal is already contained in the goal base.
	 *  @param goal	The goal to test.
	 *  @return True, if the goal is contained.
	 */
	public boolean containsGoal(IRGoal goal)
	{
		return adoptedgoals.contains(goal);
	}

	/**
	 *  Test if a goal is already contained in the goal base.
	 *  @param goal	The goal to test.
	 *  @return True, if the goal is contained.
	 */
	public boolean containsProcessGoal(RProcessGoal goal)
	{
		return processgoals.contains(goal);
	}

	/**
	 *  Create a goal from a template goal.
	 *  To be processed, the goal has to be dispatched as subgoal
	 *  or adopted as top-level goal.
	 *  @param type	The template goal name as specified in the ADF.
	 *  @return The created goal.
	 */
	public IRGoal	createGoal(String type)
	{
        IMReferenceableElement ref = ((IMBase)getModelElement()).getReferenceableElement(type);
        if(ref==null)
        	throw new RuntimeException("Unknown goal: "+type);
        return createGoal(null, ref, null, null, null);
	}

	/**
	 *  Get all proprietary goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All proprietary goals of the specified type.
	 */
	public IRGoal[] getGoals(String type)
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<adoptedgoals.size(); i++)
		{
			IRGoal tmp = (IRGoal)adoptedgoals.get(i);
			if(tmp.getType().equals(type))
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (IRGoal[])ret.toArray(new IRGoal[ret.size()]);
	}

	/**
	 *  Get all proprietary goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All proprietary goals of the specified type.
	 */
	public IRGoal[] getActiveGoals(String type)
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<adoptedgoals.size(); i++)
		{
			IRGoal tmp = (IRGoal)adoptedgoals.get(i);
			if(tmp.getType().equals(type) && tmp.isActive())
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (IRGoal[])ret.toArray(new IRGoal[ret.size()]);
	}

	/**
	 *  Get all proprietary goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All proprietary goals of the specified type.
	 */
	public IRGoal[] getOptions(String type)
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<adoptedgoals.size(); i++)
		{
			IRGoal tmp = (IRGoal)adoptedgoals.get(i);
			if(tmp.getType().equals(type) && tmp.isOption())
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (IRGoal[])ret.toArray(new IRGoal[ret.size()]);
	}

	/**
	 *  Get all the adopted and active goals (including subgoals).
	 *  @return All goals and subgoals.
	 */
	public IRGoal[]	getActiveGoals()
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<adoptedgoals.size(); i++)
		{
			IRGoal tmp = (IRGoal)adoptedgoals.get(i);
			if(tmp.isActive())
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (IRGoal[])ret.toArray(new IRGoal[ret.size()]);
	}

	/**
	 *  Get all goal in option state.
	 */
	public IRGoal[] getOptions()
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<adoptedgoals.size(); i++)
		{
			IRGoal tmp = (IRGoal)adoptedgoals.get(i);
			if(tmp.isOption())
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (IRGoal[])ret.toArray(new IRGoal[ret.size()]);
	}

	/**
	 *  Get all the adopted goals (including subgoals).
	 *  @return All goals and subgoals.
	 */
	public IRGoal[]	getGoals()
	{
		return (IRGoal[])adoptedgoals.getObjects(IRGoal.class);
	}

	/**
	 *  todo: remove method. Problem: GoalCreationAction create and adopt goals,
	 *  but one does not know that this will happen.
	 *  Get all proprietary goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All proprietary goals of the specified type.
	 * /
	public IRGoal[] getCreatedGoals(String type)
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<createdgoals.size(); i++)
		{
			IRGoal tmp = (IRGoal)createdgoals.get(i);
			if(tmp.getType().equals(type))
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (IRGoal[])ret.toArray(new IRGoal[ret.size()]);
	}*/

	/**
	 *  Register a new goal model.
	 *  @param mgoal The goal model.
	 */
	public void registerGoal(IMGoal mgoal)
	{
		// Create bindings.
		BindingHelper	binding = null;
		if(mgoal.getBindingParameters().length>0)
		{
			//BindingHelper	binding	= new BindingHelper(goals[i].getBindings(), this);
			binding	= new BindingHelper(mgoal, this, mgoal.getCreationCondition()!=null);
			bindings.put(mgoal, binding);
		}

		//String[]	bnames	= goal.getBindingNames();
		if(mgoal.getCreationCondition()!=null)
		{
			// Create condition for checking trigger.
			final RBindingCondition creation = (RBindingCondition)getScope().getExpressionbase()
				.createInternalCondition(mgoal.getCreationCondition(), this, null, binding);
			conditions.put(mgoal, creation);

			// Create action for changed binding expressions.
			creation.setAction(new GoalCreationAction(this, new ConditionDefaultPrecondition(creation), mgoal));

			// Activate tracing: May be triggered, when atomic agent init block ends.
			creation.setTraceMode(ICondition.TRACE_ALWAYS);
		}
	}

	/**
	 *  Deregister a goal model.
	 *  @param mgoal The goal model.
	 */
	public void deregisterGoal(IMGoal mgoal)
	{
		leaveRunningState(mgoal);
	}

	/**
	 *  Register a new goal reference model.
	 *  @param mgoalref The goal reference model.
	 */
	public void registerGoalReference(IMGoalReference mgoalref)
	{
		// Nothing to do !?
	}

	/**
	 *  Deregister a goal reference model.
	 *  @param mgoalref The goal reference model.
	 */
	public void deregisterGoalReference(IMGoalReference mgoalref)
	{
		leaveRunningState(mgoalref);
	}

	/**
	 *  Create a string representation of this element.
	 *  @return	A string representing this element.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(name);
		sb.append("\ngoals=\n");
		IRGoal[]	mgoals	= getGoals();
		for(int i=0; i<mgoals.length; i++)
		{
			sb.append("  ");
			sb.append(mgoals[i].getName());
			sb.append("\n");
		}
		sb.append("\nprocessgoals=\n");
		RProcessGoal[]	pgoals	= getProcessGoals();
		for(int i=0; i<pgoals.length; i++)
		{
			sb.append("  ");
			sb.append(pgoals[i].getName());
			sb.append("\n");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Get the state of this goalbase.
	 *  encoded in a set of corresponding change events.
	 *  @param types	The system event types, the caller is interested in.
	 */
	public List	getState(String[] types)
	{
  		List	ret	= SCollection.createArrayList();

		// Add events for goals and processgoals.
		if(ISystemEventTypes.Subtypes.isSubtype(SystemEvent.GOAL_ADDED, types))
		{
			for(int i=0; i<adoptedgoals.size(); i++)
			{
				RReferenceableElement	goal	= (RReferenceableElement)adoptedgoals.get(i);
				ret.add(new SystemEvent(SystemEvent.GOAL_ADDED, goal));
			}

//			for(int i=0; i<processgoals.size(); i++)
//			{
//				RReferenceableElement	goal	= (RReferenceableElement)processgoals.get(i);
//				ret.add(new SystemEvent(SystemEvent.GOAL_ADDED, goal));
//			}
		}

		return ret;
	}

	/**
	 *  Dispatch a new top-level goal.
	 *  @param goal The new goal.
	 *  Note: plan step is interrupted after call.
	 */
	public IFilter dispatchTopLevelGoal(IRGoal goal)
	{
		if(goal==null)
			throw new RuntimeException("Dispatched goal must not null: "+goal);
		//assert goal!=null;
		//assert goal.getParent()==null;
		// Create filter for identifying the result info event.
		GoalEventFilter gf = new GoalEventFilter(goal.getType(), goal.getName(), true);

		// todo: meaning of uniqueness
		if(!adoptGoal(goal))
			getScope().getLogger().warning("Cannot adopt goal (already exists): "+goal);
			//throw new RuntimeException("Cannot adopt goal (already exists): "+goal);

		return gf;
	}

	/**
	 *  Dispatch a new subgoal.
	 *  @param subgoal The new subgoal.
	 *  @return The eventfilter for identifying the result event.
	 */
	public IFilter dispatchSubgoal(RProcessGoal parent, IRGoal subgoal)
	{
		assert parent!=null;
		assert subgoal!=null;

		// Create filter for identifying the result info event.
		GoalEventFilter gf = new GoalEventFilter(subgoal.getType(), subgoal.getName(), true);

		// Add as subgoal.
		parent.addSubgoal(subgoal);

		// Goal adoptions makes the goal available for deliberation.
		if(!adoptGoal(subgoal))
		{
			parent.removeSubgoal(subgoal);
			throw new GoalFailureException(null, "Cannot adopt goal (already exists).");
		}

		return gf;
	}

	/**
	 *  Get all the process goals.
	 *  @return All process goals.
	 */
	public RProcessGoal[]	getProcessGoals()
	{
		return (RProcessGoal[])processgoals.getObjects(RProcessGoal.class);
	}

	/**
	 *  Get all process goals of a specified type (=model element name).
	 *  @param type The goal type.
	 *  @return All process goals of the specified type.
	 */
	protected RProcessGoal[] getProcessGoals(String type)
	{
		ArrayList ret = SCollection.createArrayList();
		for(int i=0; i<processgoals.size(); i++)
		{
			RProcessGoal tmp = (RProcessGoal)processgoals.get(i);
			if(tmp.getType().equals(type))
				ret.add(tmp);
		}
		//System.out.println("found for "+type+" : "+ret);
		return (RProcessGoal[])ret.toArray(new RProcessGoal[ret.size()]);
	}

	/**
	 *  Add a new goal to the intention stack.
	 *  @param igoal The goal.
	 *  @return True, if the goal has been adopted.
	 *    False, when a same goal already existed.
	 */
	public boolean	adoptGoal(IRGoal igoal)
	{
		assert igoal!=null;
		assert !((RGoal)igoal.getOriginalElement()).isAdopted();
		// Don't adopt goals whose parent is not active.
		// Todo: what about subgoals in cleanup of aborted plans
//		assert igoal.getRealParent()==null
//			|| igoal.getRealParent().isAdopted()
//			&& (igoal.getRealParent().getProprietaryGoal()==null
//				|| igoal.getRealParent().getProprietaryGoal().isActive());

		// Check if adopting goal is allowed.
		boolean	adopt	= true;
		RGoal	goal	= (RGoal)igoal.getOriginalElement();
		IMGoal	mgoal	= (IMGoal)goal.getModelElement();
		if(mgoal.getUnique()!=null)
		{
			IRGoal[]	siblings	= getGoals(goal.getType());
			for(int i=0; adopt && i<siblings.length; i++)
			{
				adopt	= !goal.isSame(siblings[i]);
			}
		}

		// Adopt goal, if allowed.
		if(adopt)
		{
			List occs = goal.getAllOccurrences();
			for(int i=0; i<occs.size(); i++)
			{
				IRGoal tmp = (IRGoal)occs.get(i);
				((RGoalbase)tmp.getOwner()).internalAdoptGoal(tmp);
			}
	
			goal.adopt();
		}
		else
		{
			goal.cleanup();
		}

		return adopt;
	}

	/**
	 *  Move a goal/reference to the adopted goal container.
	 */
	protected void internalAdoptGoal(IRGoal goal)
	{
		assert goal!=null;
//		assert createdgoals.containsKey(goal.getName());
		assert !adoptedgoals.containsKey(goal.getName()): "Goal with same name is already adopted: "+goal.getName();

		// Adopt goal (will be activated by deliberation).
//		createdgoals.removeKey(goal.getName());
		adoptedgoals.add(goal.getName(), goal);
	}

	/**
	 *	Remove this adopted! goal from the goal base.
	 *  @param goal The goal to remove.
	 */
	protected void removeGoal(IRGoal goal)
	{
		assert goal!=null;
//		assert !createdgoals.containsKey(goal.getName());
		assert adoptedgoals.containsKey(goal.getName());

		adoptedgoals.removeKey(goal.getName());
	}

	/**
	 *	Remove this goal from the goal base.
	 *  @param goal The goal to remove.
	 */
	protected void removeGoal(RProcessGoal goal)
	{
		assert goal!=null;
		assert processgoals.containsKey(goal.getName());

		processgoals.removeKey(goal.getName());
	}

	//-------- RBase abstract methods --------

	/**
	 *  Get the runtime element for a model element.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param melement	The model of the element to be retrieved.
	 *  @param creator	The creator of the element (e.g. a reference).
	 */
	protected RReferenceableElement	getElementInstance(IMReferenceableElement melement, RReferenceableElement creator)
	{
		RCapability	scope	= getScope().getAgent().lookupCapability(melement.getScope());
		assert scope!=null: this+ ", "+this.getScope()+", "+melement.getScope();
		return (RReferenceableElement)scope.getGoalbase().internalCreateGoal(null, melement, null, creator, null);
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected RBase getCorrespondingBase(RCapability scope)
	{
		return scope.getGoalbase();
	}
	
	//-------- helper methods --------

	/**
	 *  Test if a goal is contained in the goal hierarchy of another goal.
	 *  @param goal	The goal to test.
	 *  @param parent	The goal-hierarchy to search in.
	 *  @return True, if the goal is contained.
	 * /
	// ??? Still needed?
	protected static boolean contains(RAbstractGoal goal, RAbstractGoal parent)
	{
		boolean ret	= goal.equals(parent);

		RAbstractGoal[] subgoals	= parent.getChildren();
		for(int i=0; i<subgoals.length && !ret; i++)
		{
			ret	= contains(goal, subgoals[i]);
		}

		return ret;
	}*/

	/**
	 *  Get the original goal of the given goal.
	 *  Resolves all references (if any).
	 * /
	protected static RGoal	getOriginalGoal(IRGoal goal)
	{
		assert goal!=null: goal;

		// Resolve references.
		while(goal instanceof RGoalReference)
			goal	= (IRGoal)((RGoalReference)goal).getReferencedElement();

		assert goal!=null;

		return (RGoal)goal;
	}*/

	/**
	 *  Factory method for goal creation.
	 *  @param goal The goal model.
	 *  @param binding The binding.
	 *  @return The new goal.
	 */
	public IRGoal	createGoal(String name, IMReferenceableElement goal, IMConfigGoal state,
		RReferenceableElement creator, Map binding)
	{
		IRGoal ret = internalCreateGoal(name, goal, state, creator, binding);
		ret.getOriginalElement().initStructure();
		return ret;
	}

	/**
	 *  Factory method for goal creation.
	 *  @param goal The goal model.
	 *  @param binding The binding.
	 *  @return The new goal.
	 */
	protected IRGoal	internalCreateGoal(String name, IMReferenceableElement goal, IMConfigGoal state,
		RReferenceableElement creator, Map binding)
	{
		assert goal!=null: goal+" "+creator;
		assert goal instanceof IMGoal || goal instanceof IMGoalReference;
		assert goal.getScope()==getScope().getModelElement();
		assert creator==null || creator instanceof IRGoal : creator;
		//if(goal.getScope()!=getScope().getModelElement())
		//	throw new RuntimeException("Creation of goal only allowed in definition scope! "+goal.getName());

		IRGoal ret = null;

		if(goal instanceof IMGoal)
		{
			// If no binding given, but model has bindings, use first applicable binding.
			if(binding==null && bindings.containsKey(goal))
			{
				BindingHelper	bh	= (BindingHelper)this.bindings.get(goal);
				
				IMConfigParameter[] inips =  state.getParameters();
				String[] names = new String[inips.length];
				Object[] vals = new Object[inips.length];
				for(int i=0; i<inips.length; i++)
				{
					// todo: does not need to calculate value as set in parameter setInitialValue()
					Object value = getScope().getExpressionbase().evaluateInternalExpression(inips[i].getInitialValue(), this);
					//Object value = inips[j].getInitialValue().getValue(null);
					names[i] = inips[i].getOriginalElement().getName();
					vals[i] = new Object[]{getScope().getExpressionbase().evaluateInternalExpression(inips[i].getInitialValue(), this)};
				}

				List bindings = bh.calculateBindings(null, names, vals);
				
				if(bindings.size()==0)
					throw new RuntimeException("No binding available!");
				binding = (Map)bindings.get(0);
			}

			// todo: put name in here!!!
			if(goal instanceof IMMetaGoal)
				ret = new RMetaGoal(name, (IMMetaGoal)goal, state, this, creator, binding);
			else if(goal instanceof IMPerformGoal)
				ret = new RPerformGoal(name, (IMPerformGoal)goal, state, this, creator, binding);
			else if(goal instanceof IMAchieveGoal)
				ret = new RAchieveGoal(name, (IMAchieveGoal)goal, state, this, creator, binding);
			else if(goal instanceof IMMaintainGoal)
				ret = new RMaintainGoal(name, (IMMaintainGoal)goal, state, this, creator, binding);
			else if(goal instanceof IMQueryGoal)
				ret = new RQueryGoal(name, (IMQueryGoal)goal, state, this, creator, binding);
		}
		else
		{
			RGoalReference	rgr	= null;
			if(goal instanceof IMMetaGoalReference)
				rgr = new RMetaGoalReference(name, (IMMetaGoalReference)goal, state, this, creator);
			else if(goal instanceof IMPerformGoalReference)
				rgr = new RPerformGoalReference(name, (IMPerformGoalReference)goal, state, this, creator);
			else if(goal instanceof IMAchieveGoalReference)
				rgr = new RAchieveGoalReference(name, (IMAchieveGoalReference)goal, state, this, creator);
			else if(goal instanceof IMMaintainGoalReference)
				rgr = new RMaintainGoalReference(name, (IMMaintainGoalReference)goal, state, this, creator);
			else if(goal instanceof IMQueryGoalReference)
				rgr = new RQueryGoalReference(name, (IMQueryGoalReference)goal, state, this, creator);

			assert rgr!=null : goal;
//			if(!rgr.isInited())
//				rgr.init();
			ret	= rgr; 
		}

		assert ret!=null : goal;

		// Add goal to goalbase.
//		this.createdgoals.add(ret.getName(), ret);
    
		return ret;
	}

	/**
	 *  Create a process goal and add it to the intention stack.
	 *  When goal is null a dummy goal will be created.
	 *  @param goal The goal to process.
	 */
	protected RProcessGoal	createProcessGoal(IRGoal goal, ICandidateInfo cand)
	{		
		// Goal is null when a process goal with dummy goal is needed.
		// todo: Should the process goal create the dummy goal?
		if(goal!=null && !adoptedgoals.containsKey(goal.getName()))
			System.out.println("holla");
		assert goal==null || adoptedgoals.containsKey(goal.getName()): goal;
		assert cand!=null;

		RProcessGoal	ret	= new RProcessGoal(goal, this, cand);

		// Add goal to goalbase (done in schedule candidate action).
		//this.processgoals.add(ret.getName(), ret);

//		SystemEvent e = new SystemEvent(SystemEvent.GOAL_ADDED, ret);
//		e.setCause(""+cand.getEvent(this));
//        ret.throwSystemEvent(e);

        return ret;
	}

	/**
	 *  Add a process goal, when a plan is adopted.
	 */
	public void	addProcessGoal(RProcessGoal goal)
	{
		// Add goal to goalbase (called from schedule candidate action).
		this.processgoals.add(goal.getName(), goal);
	}

	/**
	 *  Exit the running state of the goalbase.
	 *  Drops all goals.
	 */
	public void exitRunningState()
	{
		// Deactivate all goals.
		IMGoal[]	goals	= ((IMGoalbase)getModelElement()).getGoals();
		for(int i=0; i<goals.length; i++)
		{
			leaveRunningState(goals[i]);
		}
		IMGoalReference[]	goalrefs	= ((IMGoalbase)getModelElement()).getGoalReferences();
		for(int i=0; i<goalrefs.length; i++)
		{
			leaveRunningState(goalrefs[i]);
		}
	}

	/**
	 *  Activate the end state of the goalbase.
	 *  Drops all goals and creates the plans specified
	 *  in the end state.
	 */
	public void activateEndState()
	{
		// Instantiate goals from end state.
		IMConfiguration config = getScope().getConfiguration();
		if(config!=null)
		{
			IMConfigGoalbase	goalbase	= config.getGoalbase();
			if(goalbase!=null)
			{
				instantiateConfigGoals(goalbase.getEndGoals());
			}
		}
	}

	/**
	 *  Instantiate goals from the configuration
	 */
	protected void instantiateConfigGoals(IMConfigGoal[] configgoals)
	{
		for(int i=0; i<configgoals.length; i++)
		{
			BindingHelper bh = (BindingHelper)bindings.get(configgoals[i].getOriginalElement());
			if(bh!=null)
			{
				IMConfigParameter[] inips =  configgoals[i].getParameters();
				String[] names = new String[inips.length];
				Object[] vals = new Object[inips.length];
				for(int j=0; j<inips.length; j++)
				{
					// todo: does not need to calculate value as set in parameter setInitialValue()
					Object value = getScope().getExpressionbase().evaluateInternalExpression(inips[j].getInitialValue(), this);
					//Object value = inips[j].getInitialValue().getValue(null);
					names[j] = inips[j].getOriginalElement().getName();
					vals[j] = new Object[]{value};
				}

				List bindings = bh.calculateBindings(null, names, vals);

				for(int j=0; j<bindings.size(); j++)
				{
					// Create goal.
					// todo: names for binding elements?
					IRGoal	goal	= createGoal(null, (IMReferenceableElement)configgoals[i].getOriginalElement()
						, configgoals[i], null, (Map)bindings.get(j));
					dispatchTopLevelGoal(goal);
				}
			}
			else
			{
				IRGoal	goal	= createGoal(configgoals[i].getName(), (IMReferenceableElement)configgoals[i].getOriginalElement(), configgoals[i], null, null);
				dispatchTopLevelGoal(goal);
			}
		}
	}

	/**
	 *  Deactivate everything related to a goal type.
	 */
	protected void leaveRunningState(IMGoal mgoal)
	{
		// Remove traced conditions.
		BindingHelper bh = (BindingHelper)bindings.remove(mgoal);
		if(bh!=null)
			bh.cleanup();
		IRCondition pc = (IRCondition)conditions.remove(mgoal);
		if(pc!=null)
			pc.cleanup();
		
		// Drop outstanding goal instances.
		IRGoal[]	goals	= getGoals(mgoal.getName());
		for(int i=0; i<goals.length; i++)
		{
			if(!isProtected(goals[i]))
				goals[i].drop();
		}

		// Drop remaining (i.e. dummy) process goal instances.
		RProcessGoal[]	pgoals	= getProcessGoals(mgoal.getName());
		for(int i=0; i<pgoals.length; i++)
		{
			// Todo: introduce new state aborted_on_deadagent ???
			if(!isProtected(pgoals[i]))
				pgoals[i].drop(RProcessGoal.PROCESS_STATE_ABORTED_ON_FAILURE);
		}
	}

	/**
	 *  Deactivate everything related to a goal type.
	 */
	protected void leaveRunningState(IMGoalReference mgoal)
	{
		// Drop outstanding goal instances.
		IRGoal[]	goals	= getGoals(mgoal.getName());
		for(int i=0; i<goals.length; i++)
		{
			if(!isProtected(goals[i]))
				goals[i].drop();
		}

		// Drop remaining (i.e. dummy) process goal instances.
		RProcessGoal[]	pgoals	= getProcessGoals(mgoal.getName());
		for(int i=0; i<pgoals.length; i++)
		{
			// Todo: introduce new state aborted_on_deadagent ???
			if(!isProtected(pgoals[i]))
				pgoals[i].drop(RProcessGoal.PROCESS_STATE_ABORTED_ON_FAILURE);
		}
	}

	/**
	 *  Check if the end state of this base is terminated.
	 *  Checks if no more goals are adopted.
	 *  
	 *  @return true, when the agent can be safely deleted.
	 */
	public boolean isEndStateTerminated()
	{
//		if(adoptedgoals.size()>0)
//			System.out.println("adoptedgoals ("+getScope().getName()+"): "+adoptedgoals);
//		if(processgoals.size()>0)
//			System.out.println("processgoals("+getScope().getName()+"): "+processgoals);

		// No need to consider process goals as corresponding plans are considered
		// in plan base.
		// return adoptedgoals.size()==0 && processgoals.size()==0;
		return adoptedgoals.size()==0;
	}

	/**
	 *  Get the metagoal for an event (if any).
	 */
	public IMMetaGoal getMetaGoals(IREvent event)
	{
		IMMetaGoal	ret	= null;
		Iterator	metagoalfilters	= metagoalmap.keySet().iterator();
		while(metagoalfilters.hasNext())
		{
			IFilter	filter	= (IFilter)metagoalfilters.next();
			if(getScope().getAgent().applyFilter(filter, event))
			{
				if(ret!=null)
					throw new RuntimeException("Multiple meta goals matching event: "+event+", "+ret+", "+metagoalmap.get(filter));
				else
					ret	= (IMMetaGoal)metagoalmap.get(filter);
			}
		}
		return ret;
	}
}