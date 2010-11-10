package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.collection.SCollection;

import java.util.List;
import java.util.Set;


/**
 *  The goal model element is the java representation
 *  of a goal description (e.g. from the xml definition).
 */
public abstract class MGoal extends MParameterElement implements IMGoal
{
	//-------- xml attributes --------

	/** The unique properties. */
	protected MUnique unique;

	/** The creation condition. */
	protected MBindingCondition creationcondition;

	/** The context condition. */
	protected MCondition contextcondition;

	/** The drop condition. */
	protected MCondition dropcondition;

	/** The deliberation settings. */
	protected MDeliberation deliberation;

	/** The retry flag. */
	protected boolean retry	= true;

	/** The random selection flag. */
	protected boolean randomselection	= false;

	/** The retry dely flag. */
	protected long retrydelay	= 0;

	/** The exclude mode. */
	protected String excludemode	= EXCLUDE_WHEN_TRIED;

	/** The posttoall state. */
	protected boolean posttoall	= false;

	/** The recalculate apl state. */
	protected boolean recalculate	= true;

	/** The recur flag. */
	protected boolean recur	= false;

	/** The recur delay. */
	protected long recurdelay	= 0;

	//-------- attributes --------

	/** The goal event. */
	protected IMGoalEvent	goalevent;

	/** The relevant parameters. */
	protected IMParameter[]	relevant;

	/** The relevant parameter sets. */
	protected IMParameterSet[]	relevants;

	//-------- constructors --------

	/**
	 *  Called when the element is setup.
	 */
	protected void init()
	{
		super.init();

		// Set the default trigger mode
		// when nothing else is specified.
		if(getCreationCondition()!=null && getCreationCondition().getTrigger()==null)
		{
			getCreationCondition().setTrigger(IMCondition.TRIGGER_IS_TRUE);
		}
		if(getContextCondition()!=null && getContextCondition().getTrigger()==null)
		{
			getContextCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_FALSE);
		}
		if(getDropCondition()!=null && getDropCondition().getTrigger()==null)
		{
			getDropCondition().setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		}

		// Init relevant parameters.
		if(getUnique()!=null)
		{
			List	rel	= SCollection.createArrayList();
			//Set	includes	= getIncludes();
			Set	excludes	= getExcludes();
			IMParameter[]	params	= getParameters();
			for(int i=0; i<params.length; i++)
			{
				// Excluded parameters are not considered.
				if(!excludes.contains(params[i]))
				{
					rel.add(params[i]);
			}
			}
			this.relevant	= (IMParameter[])rel.toArray(new IMParameter[rel.size()]);

			// Init relevant parameter sets
			rel.clear();
			IMParameterSet[]	paramsets	= getParameterSets();
			for(int i=0; i<paramsets.length; i++)
			{
				// Excluded parameter sets are not considered.
				if(!excludes.contains(params[i]))
				{
					rel.add(params[i]);
			}
			}
			this.relevants	= (IMParameterSet[])rel.toArray(new IMParameterSet[rel.size()]);
		}
		else
		{
			this.relevant	= new IMParameter[0];
			this.relevants	= new IMParameterSet[0];
		}
	}

	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(unique!=null)
		{
			ret.add(unique);
		}
		if(creationcondition!=null)
		{
			ret.add(creationcondition);
		}
		if(contextcondition!=null)
		{
			ret.add(contextcondition);
		}
		if(dropcondition!=null)
		{
			ret.add(dropcondition);
		}
		if(deliberation!=null)
		{
			ret.add(deliberation);
		}
		return ret;
	}

	//-------- creation condition --------

	/**
	 *  Get the creation condition of the goal.
	 *  @return The creation condition (if any).
	 */
	public IMCondition	getCreationCondition()
	{
		return creationcondition;
	}

	/**
	 *  Create a creation condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new creation condition.
	 */
	public IMCondition	createCreationCondition(String expression)
	{
		this.creationcondition = new MBindingCondition();
		creationcondition.setExpressionText(expression);
		creationcondition.setTrigger(IMCondition.TRIGGER_IS_TRUE);
		creationcondition.setOwner(this);
		creationcondition.init();
		return creationcondition;
	}

	/**
	 *  Delete the creation condition of the goal.
	 */
	public void	deleteCreationCondition()
	{
		this.creationcondition = null;
	}

	//-------- context condition --------

	/**
	 *  Get the context condition of the goal.
	 *  @return The context condition (if any).
	 */
	public IMCondition getContextCondition()
	{
		return this.contextcondition;
	}

	/**
	 *  Create a context condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new context condition.
	 */
	public IMCondition	createContextCondition(String expression)
	{
		this.contextcondition = new MCondition();
		contextcondition.setExpressionText(expression);
		contextcondition.setTrigger(IMCondition.TRIGGER_CHANGES_TO_FALSE);
		contextcondition.setOwner(this);
		contextcondition.init();
		return contextcondition;
	}

	/**
	 *  Delete the context condition of the goal.
	 */
	public void	deleteContextCondition()
	{
		this.contextcondition = null;
	}


	//-------- drop condition --------

	/**
	 *  Get the drop condition of the goal.
	 *  @return The drop condition (if any).
	 */
	public IMCondition getDropCondition()
	{
		return this.dropcondition;
	}

	/**
	 *  Create a drop condition for the goal.
	 *  @param expression	The expression string.
	 *  @return The new drop condition.
	 */
	public IMCondition	createDropCondition(String expression)
	{
		this.dropcondition = new MCondition();
		dropcondition.setExpressionText(expression);
		dropcondition.setTrigger(IMCondition.TRIGGER_CHANGES_TO_TRUE);
		dropcondition.setOwner(this);
		dropcondition.init();
		return dropcondition;
	}

	/**
	 *  Delete the drop condition of the goal.
	 */
	public void	deleteDropCondition()
	{
		this.dropcondition = null;
	}


	//-------- unique --------

	/**
	 *  Get the uniqueness properties of the goal (if any).
	 *  @return The uniqueness properties.
	 */
	public IMUnique	getUnique()
	{
		return unique;
	}

	/**
	 *  Create new the uniqueness properties for the goal.
	 *  @return The uniqueness properties.
	 */
	public IMUnique	createUnique()
	{
		this.unique = new MUnique();
		unique.setOwner(this);
		unique.init();
		return unique;
	}

	/**
	 *  Delete the uniqueness properties of the goal.
	 */
	public void	deleteUnique()
	{
		this.unique = null;
	}

	//-------- deliberation --------

	/**
	 *  Get the deliberation properties of the goal (if any).
	 *  @return The deliberation properties.
	 */
	public IMDeliberation getDeliberation()
	{
		return this.deliberation;
	}

	/**
	 *  Create new the deliberation properties for the goal.
	 *  @param cardinality	The cardinality (i.e. number of concurrently active goals) of this type.
	 *  @return The deliberation properties.
	 */
	public IMDeliberation	createDeliberation(int cardinality)
	{
		this.deliberation = new MDeliberation();
		deliberation.setOwner(this);
		deliberation.init();
		return deliberation;
	}

	/**
	 *  Delete the deliberation properties of the goal.
	 */
	public void	deleteDeliberation()
	{
		this.deliberation = null;
	}

	//-------- bdi flags --------

	/**
	 *  Get the retry flag.
	 *  @return The flag indicating if this goal should be retried when the first plan fails.
	 */
	public boolean	isRetry()
	{
		return retry;
	}

	/**
	 *  Set the retry flag.
	 *  @param retry	The flag indicating if this goal should be retried when the first plan fails.
	 */
	public void	setRetry(boolean retry)
	{
		this.retry = retry;
	}


	/**
	 *  Get the retry delay.
	 *  @return The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public long	getRetryDelay()
	{
		return retrydelay;
	}

	/**
	 *  Set the retry delay flag.
	 *  @param retrydelay	The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public void	setRetryDelay(long retrydelay)
	{
		this.retrydelay = retrydelay;
	}


	/**
	 *  Get the exclude mode.
	 *  @return The mode indicating which plans should be excluded after they have been tried.
	 */
	public String	getExcludeMode()
	{
		return excludemode;
	}

	/**
	 *  Set the exclude flag.
	 *  @param exclude	The mode indicating which plans should be excluded after they have been tried.
	 */
	public void	setExcludeMode(String exclude)
	{
		this.excludemode = exclude;
	}

	/**
	 *  Get the random selection flag.
	 *  @return The flag indicating if plans should be selected at random or by prominence.
	 */
	public boolean	isRandomSelection()
	{
		return randomselection;
	}

	/**
	 *  Set the random selection flag.
	 *  @param randomselection	The flag indicating if plans should be selected at random or by prominence.
	 */
	public void	setRandomSelection(boolean randomselection)
	{
		this.randomselection = randomselection;
	}

	/**
	 *  Get the post-to-all flag.
	 *  @return The flag indicating if all applicable plans should be executed at once.
	 */
	public boolean	isPostToAll()
	{
		return posttoall;
	}

	/**
	 *  Set the post-to-all flag.
	 *  @param posttoall	The flag indicating if all applicable plans should be executed at once.
	 */
	public void	setPostToAll(boolean posttoall)
	{
		this.posttoall = posttoall;
	}

	/**
	 *  Get the recalculate applicable candidates list (apl) state.
	 *  @return True, if should be recalculated eacht time.
	 */
	public boolean isRecalculating()
	{
		return recalculate;
	}

	/**
	 *  Set the recalculate applicable candidates list (apl) state.
	 *  @param recalculating True, if should be recalculated eacht time.
	 */
	public void setRecalculating(boolean recalculating)
	{
		this.recalculate = recalculating;
	}

	/**
	 *  Get the recur flag.
	 *  @return The flag indicating if this goal should be retried when the first plan fails.
	 */
	public boolean	isRecur()
	{
		return recur;
	}

	/**
	 *  Set the recur flag.
	 *  @param recur	The flag indicating if this goal should be retried when the first plan fails.
	 */
	public void	setRecur(boolean recur)
	{
		this.recur = recur;
	}


	/**
	 *  Get the recur delay.
	 *  @return The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public long	getRecurDelay()
	{
		return recurdelay;
	}

	/**
	 *  Set the recur delay flag.
	 *  @param recurdelay	The delay between retries of the goal (in milliseconds) or -1 for no delay.
	 */
	public void	setRecurDelay(long recurdelay)
	{
		this.recurdelay = recurdelay;
	}

	//-------- methods --------

	/**
	 *  Get the goal event.
	 */
	public IMGoalEvent	getGoalEvent()
	{
		// Init goal event (cached for speed).
		if(goalevent==null)
		{
			goalevent	= getScope().getEventbase().getGoalEvent(IMEventbase.STANDARD_GOAL_EVENT);
		}
		return goalevent;
	}


	/**
	 *  Get the parameters which are relevant for comparing goals.
	 */
	public IMParameter[]	getRelevantParameters()
	{
		return relevant;
	}

	/**
	 *  Get the parameter sets which are relevant for comparing goals.
	 */
	public IMParameterSet[]	getRelevantParameterSets()
	{
		return relevants;
	}

	/**
	 *  Get the expression parameters.
	 *  If this element has no local parameters, will return
	 *  the parameters of the owner, or null if the element
	 *  has no owner.
	 */
	public List	getSystemExpressionParameters()
	{
		List copy = super.getSystemExpressionParameters();
		copy.add(new ExpressionParameterInfo("$goal", this, "jadex.runtime.impl.IRGoal"));
		return copy;
	}

	//-------- helper methods --------

	/**
	 *  Get the excluded parameter(set)s.
	 */
	protected Set	getExcludes()
	{
		Set	ret	= SCollection.createHashSet();
		String[] exs = getUnique().getExcludes();
		for(int i=0; i<exs.length; i++)
		{
			// todo: what about parameter refs?
			Object param = getParameter(exs[i]);
			if(param==null)
			{
				param = getParameterSet(exs[i]);
			}
			ret.add(param);
		}
		return ret;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MGoal clone = (MGoal)cl;
		if(unique!=null)
		{
			clone.unique = (MUnique)unique.clone();
		}
		if(creationcondition!=null)
		{
			clone.creationcondition = (MBindingCondition)creationcondition.clone();
		}
		if(contextcondition!=null)
		{
			clone.contextcondition = (MCondition)contextcondition.clone();
		}
		if(dropcondition!=null)
		{
			clone.dropcondition = (MCondition)dropcondition.clone();
		}
		if(deliberation!=null)
		{
			clone.deliberation = (MDeliberation)deliberation.clone();
		}
		if(goalevent!=null)
		{
			clone.goalevent = (IMGoalEvent)((MGoalEvent)goalevent).clone();
		}
		if(relevant!=null)
		{
			clone.relevant = new IMParameter[relevant.length];
			for(int i=0; i<relevant.length; i++)
			{
				clone.relevant[i] = (IMParameter)((MParameter)relevant[i]).clone();
		}
		}
		if(relevants!=null)
		{
			clone.relevants = new IMParameterSet[relevants.length];
			for(int i=0; i<relevants.length; i++)
			{
				clone.relevants[i] = (IMParameterSet)((MParameterSet)relevants[i]).clone();
		}
	}
}
}
