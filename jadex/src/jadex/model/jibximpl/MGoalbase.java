package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;

/**
 *  The model of a goalbase containing models of goals.
 */
public class MGoalbase extends MBase implements IMGoalbase
{
	//-------- xml attributes --------

	/** The perform goals. */
	protected ArrayList performgoals;

	/** The perform goal references. */
	protected ArrayList performgoalrefs;

	/** The achieve goals. */
	protected ArrayList achievegoals;

	/** The achieve goal references. */
	protected ArrayList achievegoalrefs;

	/** The query goals. */
	protected ArrayList querygoals;

	/** The query goal references. */
	protected ArrayList querygoalrefs;

	/** The maintain goals. */
	protected ArrayList maintaingoals;

	/** The maintain goal references. */
	protected ArrayList maintaingoalrefs;

	/** The meta goals. */
	protected ArrayList metagoals;

	/** The meta goal references. */
	protected ArrayList metagoalrefs;


	//-------- constructors --------

	/**
	 *  The setup is called by a configuration code object.
	 */
	protected void init()
	{
		super.init();

		// Create a dummy goal for handling event without goals. Hack?
		createPerformGoal(DUMMY_GOAL, IMReferenceableElement.EXPORTED_FALSE, false, 0, IMGoal.EXCLUDE_WHEN_TRIED);
	}


	//-------- xml methods --------

	/**
	 *  Geneal add method for unmarshalling.
	 *  Necessary to support unordered collections :-(
	 *  @param elem The element to add.
	 */
	public void addElement(IMReferenceableElement elem)
	{
		assert elem instanceof IMMetaGoal || elem instanceof IMMetaGoalReference
			|| elem instanceof IMPerformGoal || elem instanceof IMPerformGoalReference
			|| elem instanceof IMAchieveGoal || elem instanceof IMAchieveGoalReference
			|| elem instanceof IMMaintainGoal || elem instanceof IMMaintainGoalReference
			|| elem instanceof IMQueryGoal || elem instanceof IMQueryGoalReference;

		if(elem instanceof IMMetaGoal)
		{
			if(metagoals==null)
				metagoals = SCollection.createArrayList();
			metagoals.add(elem);
		}
		else if(elem instanceof IMMetaGoalReference)
		{
			if(metagoalrefs==null)
				metagoalrefs = SCollection.createArrayList();
			metagoalrefs.add(elem);
		}
		else if(elem instanceof IMPerformGoal)
		{
			if(performgoals==null)
				performgoals = SCollection.createArrayList();
			performgoals.add(elem);
		}
		else if(elem instanceof IMPerformGoalReference)
		{
			if(performgoalrefs==null)
				performgoalrefs = SCollection.createArrayList();
			performgoalrefs.add(elem);
		}
		else if(elem instanceof IMAchieveGoal)
		{
			if(achievegoals==null)
				achievegoals = SCollection.createArrayList();
			achievegoals.add(elem);
		}
		else if(elem instanceof IMAchieveGoalReference)
		{
			if(achievegoalrefs==null)
				achievegoalrefs = SCollection.createArrayList();
			achievegoalrefs.add(elem);
		}
		else if(elem instanceof IMMaintainGoal)
		{
			if(maintaingoals==null)
				maintaingoals = SCollection.createArrayList();
			maintaingoals.add(elem);
		}
		else if(elem instanceof IMMaintainGoalReference)
		{
			if(maintaingoalrefs==null)
				maintaingoalrefs = SCollection.createArrayList();
			maintaingoalrefs.add(elem);
		}
		else if(elem instanceof IMQueryGoal)
		{
			if(querygoals==null)
				querygoals = SCollection.createArrayList();
			querygoals.add(elem);
		}
		else //if(elem instanceof IMQueryGoalReference)
		{
			if(querygoalrefs==null)
				querygoalrefs = SCollection.createArrayList();
			querygoalrefs.add(elem);
		}
	}

	/**
	 *  Geneal add method for marshalling.
	 *  @return Iterator with all elements.
	 */
	public Iterator iterElements()
	{
		return SReflect.getIterator(getReferenceableElements());
	}
	
	//-------- perform goals --------

	/**
	 *  Get all known perform goals.
	 *  @return The perform goals.
	 */
	public IMPerformGoal[] getPerformGoals()
	{
		if(performgoals==null)
			return new IMPerformGoal[0];
		return (IMPerformGoal[])performgoals.toArray(new IMPerformGoal[performgoals.size()]);
	}

	/**
	 *  Get a perform goal by name.
	 *  @param name The perform goal name.
	 *  @return The perform goal with that name (if any).
	 */
	public IMPerformGoal getPerformGoal(String name)
	{
		assert name!=null;

		IMPerformGoal ret = null;
		for(int i=0; performgoals!=null && i<performgoals.size() && ret==null; i++)
		{
			IMPerformGoal test = (IMPerformGoal)performgoals.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a perform goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.
	 *  @return The perform goal.
	 */
	public IMPerformGoal	createPerformGoal(String name, String exported, boolean retry, long retrydelay, String exclude)
	{
		if(performgoals==null)
			performgoals = SCollection.createArrayList();

		MPerformGoal ret = new MPerformGoal();
		ret.setName(name);
		ret.setExported(exported);
		ret.setRetry(retry);
		ret.setRetryDelay(retrydelay);
		ret.setExcludeMode(exclude);
		ret.setOwner(this);
		ret.init();
		performgoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a perform goal.
	 *  @param goal	The perform goal.
	 */
	public void	deletePerformGoal(IMPerformGoal goal)
	{
		if(!performgoals.remove(goal))
			throw new RuntimeException("Perform goal not found: "+goal);
	}

	//-------- perform goal references --------

	/**
	 *  Get all known perform goal references.
	 *  @return The perform goal references.
	 */
	public IMPerformGoalReference[] getPerformGoalReferences()
	{
		if(performgoalrefs==null)
			return new IMPerformGoalReference[0];
		return (IMPerformGoalReference[])performgoalrefs.
			toArray(new IMPerformGoalReference[performgoalrefs.size()]);
	}

	/**
	 *  Get a goal by name.
	 *  @param name The perform goal reference name.
	 *  @return The perform goal reference with that name (if any).
	 */
	public IMPerformGoalReference getPerformGoalReference(String name)
	{
		assert name!=null;

		IMPerformGoalReference ret = null;
		for(int i=0; performgoalrefs!=null && i<performgoalrefs.size() && ret==null; i++)
		{
			IMPerformGoalReference test = (IMPerformGoalReference)performgoalrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a perform goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The perform goal reference.
	 */
	public IMPerformGoalReference	createPerformGoalReference(String name, String exported, String ref)
	{
		if(performgoalrefs==null)
			performgoalrefs = SCollection.createArrayList();

		MPerformGoalReference ret = new MPerformGoalReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		performgoalrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a perform goal reference.
	 *  @param goal	The perform goal reference.
	 */
	public void	deletePerformGoalReference(IMPerformGoalReference goal)
	{
		if(!performgoalrefs.remove(goal))
			throw new RuntimeException("Perform goal reference not found: "+goal);
	}


	//-------- achieve goals --------

	/**
	 *  Get all known achieve goals.
	 *  @return The achieve goals.
	 */
	public IMAchieveGoal[] getAchieveGoals()
	{
		if(achievegoals==null)
			return new IMAchieveGoal[0];
		return (IMAchieveGoal[])achievegoals.toArray(new IMAchieveGoal[achievegoals.size()]);
	}

	/**
	 *  Get an achieve goal by name.
	 *  @param name The achieve goal name.
	 *  @return The achieve goal with that name (if any).
	 */
	public IMAchieveGoal getAchieveGoal(String name)
	{
		assert name!=null;

		IMAchieveGoal ret = null;
		for(int i=0; achievegoals!=null && i<achievegoals.size() && ret==null; i++)
		{
			IMAchieveGoal test = (IMAchieveGoal)achievegoals.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an achieve goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.
	 *  @return The achieve goal.
	 */
	public IMAchieveGoal	createAchieveGoal(String name, String exported, boolean retry, long retrydelay, String exclude)
	{
		if(achievegoals==null)
			achievegoals = SCollection.createArrayList();

		MAchieveGoal ret = new MAchieveGoal();
		ret.setName(name);
		ret.setExported(exported);
		ret.setRetry(retry);
		ret.setRetryDelay(retrydelay);
		ret.setExcludeMode(exclude);
		ret.setOwner(this);
		ret.init();
		achievegoals.add(ret);
		return ret;
	}

	/**
	 *  Delete an achieve goal.
	 *  @param goal	The achieve goal.
	 */
	public void	deleteAchieveGoal(IMAchieveGoal goal)
	{
		if(!achievegoals.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}


	//-------- achieve goal references --------

	/**
	 *  Get all known achieve goal references.
	 *  @return The achieve goal references.
	 */
	public IMAchieveGoalReference[] getAchieveGoalReferences()
	{
		if(achievegoalrefs==null)
			return new IMAchieveGoalReference[0];
		return (IMAchieveGoalReference[])achievegoalrefs
			.toArray(new IMAchieveGoalReference[achievegoalrefs.size()]);
	}

	/**
	 *  Get an goal by name.
	 *  @param name The achieve goal reference name.
	 *  @return The achieve goal reference with that name (if any).
	 */
	public IMAchieveGoalReference getAchieveGoalReference(String name)
	{
		assert name!=null;

		IMAchieveGoalReference ret = null;
		for(int i=0; achievegoalrefs!=null && i<achievegoalrefs.size() && ret==null; i++)
		{
			IMAchieveGoalReference test = (IMAchieveGoalReference)achievegoalrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an achieve goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The achieve goal reference.
	 */
	public IMAchieveGoalReference	createAchieveGoalReference(String name, String exported, String ref)
	{
		if(achievegoalrefs==null)
			achievegoalrefs = SCollection.createArrayList();

		MAchieveGoalReference ret = new MAchieveGoalReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		achievegoalrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete an achieve goal reference.
	 *  @param goal	The achieve goal reference.
	 */
	public void	deleteAchieveGoalReference(IMAchieveGoalReference goal)
	{
		if(!achievegoalrefs.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- query goals --------

	/**
	 *  Get all known query goals.
	 *  @return The query goals.
	 */
	public IMQueryGoal[] getQueryGoals()
	{
		if(querygoals==null)
			return new IMQueryGoal[0];
		return (IMQueryGoal[])querygoals.toArray(new IMQueryGoal[querygoals.size()]);
	}

	/**
	 *  Get a query goal by name.
	 *  @param name The query goal name.
	 *  @return The query goal with that name (if any).
	 */
	public IMQueryGoal getQueryGoal(String name)
	{
		assert name!=null;

		IMQueryGoal ret = null;
		for(int i=0; querygoals!=null && i<querygoals.size() && ret==null; i++)
		{
			IMQueryGoal test = (IMQueryGoal)querygoals.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a query goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.
	 *  @return The query goal.
	 */
	public IMQueryGoal	createQueryGoal(String name, String exported, boolean retry, long retrydelay, String exclude)
	{
		if(querygoals==null)
			querygoals = SCollection.createArrayList();

		MQueryGoal ret = new MQueryGoal();
		ret.setName(name);
		ret.setExported(exported);
		ret.setRetry(retry);
		ret.setRetryDelay(retrydelay);
		ret.setExcludeMode(exclude);
		ret.setOwner(this);
		ret.init();
		querygoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a query goal.
	 *  @param goal	The query goal.
	 */
	public void	deleteQueryGoal(IMQueryGoal goal)
	{
		if(!querygoals.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- query goal references --------

	/**
	 *  Get all known query goal references.
	 *  @return The query goal references.
	 */
	public IMQueryGoalReference[] getQueryGoalReferences()
	{
		if(querygoalrefs==null)
			return new IMQueryGoalReference[0];
		return (IMQueryGoalReference[])querygoalrefs.toArray(new IMQueryGoalReference[querygoalrefs.size()]);
	}

	/**
	 *  Get a query goal by name.
	 *  @param name The query goal reference name.
	 *  @return The query goal reference with that name (if any).
	 */
	public IMQueryGoalReference getQueryGoalReference(String name)
	{
		assert name!=null;

		IMQueryGoalReference ret = null;
		for(int i=0; querygoalrefs!=null && i<querygoalrefs.size() && ret==null; i++)
		{
			IMQueryGoalReference test = (IMQueryGoalReference)querygoalrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a query goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The query goal reference.
	 */
	public IMQueryGoalReference	createQueryGoalReference(String name, String exported, String ref)
	{
		if(querygoalrefs==null)
			querygoalrefs = SCollection.createArrayList();

		MQueryGoalReference ret = new MQueryGoalReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		querygoalrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a query goal reference.
	 *  @param goal	The query goal reference.
	 */
	public void	deleteQueryGoalReference(IMQueryGoalReference goal)
	{
		if(!querygoalrefs.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- maintain goals --------

	/**
	 *  Get all known maintain goals.
	 *  @return The maintain goals.
	 */
	public IMMaintainGoal[] getMaintainGoals()
	{
		if(maintaingoals==null)
			return new IMMaintainGoal[0];
		return (IMMaintainGoal[])maintaingoals.toArray(new IMMaintainGoal[maintaingoals.size()]);
	}

	/**
	 *  Get a maintain goal by name.
	 *  @param name The maintain goal name.
	 *  @return The maintain goal with that name (if any).
	 */
	public IMMaintainGoal getMaintainGoal(String name)
	{
		assert name!=null;

		IMMaintainGoal ret = null;
		for(int i=0; maintaingoals!=null && i<maintaingoals.size() && ret==null; i++)
		{
			IMMaintainGoal test = (IMMaintainGoal)maintaingoals.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a maintain goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.  
	 *  @param recur	Should the goal be recurred when not succeeded after all available plans have been tried.
	 *  @param recurdelay	An optional delay (in milliseconds) before the goal is recurred (-1 for no delay).
	 *  @return The maintain goal.
	 */
	public IMMaintainGoal	createMaintainGoal(String name, String exported, boolean retry, long retrydelay, String exclude, boolean recur, long recurdelay)
	{
		if(maintaingoals==null)
			maintaingoals = SCollection.createArrayList();

		MMaintainGoal ret = new MMaintainGoal();
		ret.setName(name);
		ret.setExported(exported);
		ret.setRetry(retry);
		ret.setRetryDelay(retrydelay);
		ret.setExcludeMode(exclude);
		ret.setRecur(recur);
		ret.setRecurDelay(recurdelay);
		ret.setOwner(this);
		ret.init();
		maintaingoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a maintain goal.
	 *  @param goal	The maintain goal.
	 */
	public void	deleteMaintainGoal(IMMaintainGoal goal)
	{
		if(!maintaingoals.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- maintain goal references --------

	/**
	 *  Get all known maintain goal references.
	 *  @return The maintain goal references.
	 */
	public IMMaintainGoalReference[] getMaintainGoalReferences()
	{
		if(maintaingoalrefs==null)
			return new IMMaintainGoalReference[0];
		return (IMMaintainGoalReference[])maintaingoalrefs
			.toArray(new IMMaintainGoalReference[maintaingoalrefs.size()]);
	}

	/**
	 *  Get an goal by name.
	 *  @param name The maintain goal reference name.
	 *  @return The maintain goal reference with that name (if any).
	 */
	public IMMaintainGoalReference getMaintainGoalReference(String name)
	{
		assert name!=null;

		IMMaintainGoalReference ret = null;
		for(int i=0; maintaingoalrefs!=null && i<maintaingoalrefs.size() && ret==null; i++)
		{
			IMMaintainGoalReference test = (IMMaintainGoalReference)maintaingoalrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create an maintain goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The maintain goal reference.
	 */
	public IMMaintainGoalReference	createMaintainGoalReference(String name, String exported, String ref)
	{
		if(maintaingoalrefs==null)
			maintaingoalrefs = SCollection.createArrayList();

		MMaintainGoalReference ret = new MMaintainGoalReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		maintaingoalrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete an maintain goal reference.
	 *  @param goal	The maintain goal reference.
	 */
	public void	deleteMaintainGoalReference(IMMaintainGoalReference goal)
	{
		if(!maintaingoalrefs.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- meta goals --------

	/**
	 *  Get all known meta goals.
	 *  @return The meta goals.
	 */
	public IMMetaGoal[] getMetaGoals()
	{
		if(metagoals==null)
			return new IMMetaGoal[0];
		return (IMMetaGoal[])metagoals.toArray(new IMMetaGoal[metagoals.size()]);
	}

	/**
	 *  Get a meta goal by name.
	 *  @param name The meta goal name.
	 *  @return The meta goal with that name (if any).
	 */
	public IMMetaGoal getMetaGoal(String name)
	{
		assert name!=null;

		IMMetaGoal ret = null;
		for(int i=0; metagoals!=null && i<metagoals.size() && ret==null; i++)
		{
			IMMetaGoal test = (IMMetaGoal)metagoals.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a meta goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.
	 *  @return The meta goal.
	 */
	public IMMetaGoal	createMetaGoal(String name, String exported, boolean retry, long retrydelay, String exclude)
	{
		if(metagoals==null)
			metagoals = SCollection.createArrayList();

		MMetaGoal ret = new MMetaGoal();
		ret.setName(name);
		ret.setExported(exported);
		ret.setRetry(retry);
		ret.setRetryDelay(retrydelay);
		ret.setExcludeMode(exclude);
		ret.setOwner(this);
		ret.init();
		metagoals.add(ret);
		return ret;
	}

	/**
	 *  Delete a meta goal.
	 *  @param goal	The meta goal.
	 */
	public void	deleteMetaGoal(IMMetaGoal goal)
	{
		if(!metagoals.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}


	//-------- meta goal references --------

	/**
	 *  Get all known meta goal references.
	 *  @return The meta goal references.
	 */
	public IMMetaGoalReference[] getMetaGoalReferences()
	{
		if(metagoalrefs==null)
			return new IMMetaGoalReference[0];
		return (IMMetaGoalReference[])metagoalrefs
			.toArray(new IMMetaGoalReference[metagoalrefs.size()]);
	}

	/**
	 *  Get a meta goal by name.
	 *  @param name The meta goal reference name.
	 *  @return The meta goal reference with that name (if any).
	 */
	public IMMetaGoalReference getMetaGoalReference(String name)
	{
		assert name!=null;

		IMMetaGoalReference ret = null;
		for(int i=0; metagoalrefs!=null && i<metagoalrefs.size() && ret==null; i++)
		{
			IMMetaGoalReference test = (IMMetaGoalReference)metagoalrefs.get(i);
			if(test.getName().equals(name))
				ret = test;
		}
		return ret;
	}

	/**
	 *  Create a meta goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The meta goal reference.
	 */
	public IMMetaGoalReference	createMetaGoalReference(String name, String exported, String ref)
	{
		if(metagoalrefs==null)
			metagoalrefs = SCollection.createArrayList();

		MMetaGoalReference ret = new MMetaGoalReference();
		ret.setName(name);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		metagoalrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a meta goal reference.
	 *  @param goal	The meta goal reference.
	 */
	public void	deleteMetaGoalReference(IMMetaGoalReference goal)
	{
		if(!metagoalrefs.remove(goal))
			throw new RuntimeException("Goal not found: "+goal);
	}

	//-------- methods --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		return "goalbase";
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public IMBase	getCorrespondingBase(IMCapability scope)
	{
		return scope.getGoalbase();
	}

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public IMReferenceableElement[]	getReferenceableElements()
	{
		Object	ret	= new IMReferenceableElement[0];
		ret	= SUtil.joinArrays(ret, getGoals());
		ret	= SUtil.joinArrays(ret, getGoalReferences());
		return (IMReferenceableElement[])ret;
	}

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem)
	{
		assert elem!=null;

		if(elem instanceof IMMetaGoal)
			deleteMetaGoal((IMMetaGoal)elem);
		else if(elem instanceof IMMetaGoalReference)
			deleteMetaGoalReference((IMMetaGoalReference)elem);
		else if(elem instanceof IMPerformGoal)
			deletePerformGoal((IMPerformGoal)elem);
		else if(elem instanceof IMPerformGoalReference)
			deletePerformGoalReference((IMPerformGoalReference)elem);
		else if(elem instanceof IMAchieveGoal)
			deleteAchieveGoal((IMAchieveGoal)elem);
		else if(elem instanceof IMAchieveGoalReference)
			deleteAchieveGoalReference((IMAchieveGoalReference)elem);
		else if(elem instanceof IMQueryGoal)
			deleteQueryGoal((IMQueryGoal)elem);
		else if(elem instanceof IMQueryGoalReference)
			deleteQueryGoalReference((IMQueryGoalReference)elem);
		else if(elem instanceof IMMaintainGoal)
			deleteMaintainGoal((IMMaintainGoal)elem);
		else if(elem instanceof IMMaintainGoalReference)
			deleteMaintainGoalReference((IMMaintainGoalReference)elem);
		else
			throw new RuntimeException("Element could not be deleted from model: "+elem.getName());
	}

	/**
	 *  Get all known goals.
	 *  @return The goals.
	 */
	public IMGoal[] getGoals()
	{
		Object	ret	= new IMGoal[0];
		ret	= SUtil.joinArrays(ret, getPerformGoals());
		ret	= SUtil.joinArrays(ret, getAchieveGoals());
		ret	= SUtil.joinArrays(ret, getQueryGoals());
		ret	= SUtil.joinArrays(ret, getMaintainGoals());
		ret	= SUtil.joinArrays(ret, getMetaGoals());
		return (IMGoal[])ret;
	}

	/**
	 *  Get a goal by name.
	 *  Searches the goal in subcapabilities, when path notation is used (a.b.name).
	 *  @param name The goal name.
	 *  @return The goal with that name (if any).
	 */
	public IMGoal getGoal(String name)
	{
		assert name!=null;
		assert name.indexOf(".")==-1;

		IMGoal ret = null;

		IMGoal[] goals = getGoals();
		for(int i=0; i<goals.length && ret==null; i++)
		{
			if(name.equals(goals[i].getName()))
			{
				ret = goals[i];
			}
		}

		return ret;
	}

	/**
	 *  Get all goal references.
	 *  @return The goal references.
	 */
	public IMGoalReference[] getGoalReferences()
	{
		Object	ret	= new IMGoalReference[0];
		ret	= SUtil.joinArrays(ret, getPerformGoalReferences());
		ret	= SUtil.joinArrays(ret, getAchieveGoalReferences());
		ret	= SUtil.joinArrays(ret, getQueryGoalReferences());
		ret	= SUtil.joinArrays(ret, getMaintainGoalReferences());
		ret	= SUtil.joinArrays(ret, getMetaGoalReferences());
		return (IMGoalReference[])ret;
	}

	/**
	 *  Get a goal reference.
	 *  @param name The name.
	 *  @return The goal reference.
	 */
	public IMGoalReference getGoalReference(String name)
	{
		IMGoalReference ret = null;

		IMGoalReference[] goalrefs = getGoalReferences();
		for(int i=0; i<goalrefs.length && ret==null; i++)
		{
			if(name.equals(goalrefs[i].getName()))
			{
				ret = goalrefs[i];
			}
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
		MGoalbase clone = (MGoalbase)cl;
		if(performgoals!=null)
		{
			clone.performgoals = SCollection.createArrayList();
			for(int i=0; i<performgoals.size(); i++)
				clone.performgoals.add(((MElement)performgoals.get(i)).clone());
		}
		if(performgoalrefs!=null)
		{
			clone.performgoalrefs = SCollection.createArrayList();
			for(int i=0; i<performgoalrefs.size(); i++)
				clone.performgoalrefs.add(((MElement)performgoalrefs.get(i)).clone());
		}
		if(achievegoals!=null)
		{
			clone.achievegoals = SCollection.createArrayList();
			for(int i=0; i<achievegoals.size(); i++)
				clone.achievegoals.add(((MElement)achievegoals.get(i)).clone());
		}
		if(achievegoalrefs!=null)
		{
			clone.achievegoalrefs = SCollection.createArrayList();
			for(int i=0; i<achievegoalrefs.size(); i++)
				clone.achievegoalrefs.add(((MElement)achievegoalrefs.get(i)).clone());
		}
		if(querygoals!=null)
		{
			clone.querygoals = SCollection.createArrayList();
			for(int i=0; i<querygoals.size(); i++)
				clone.querygoals.add(((MElement)querygoals.get(i)).clone());
		}
		if(querygoalrefs!=null)
		{
			clone.querygoalrefs = SCollection.createArrayList();
			for(int i=0; i<querygoalrefs.size(); i++)
				clone.querygoalrefs.add(((MElement)querygoalrefs.get(i)).clone());
		}
		if(maintaingoals!=null)
		{
			clone.maintaingoals = SCollection.createArrayList();
			for(int i=0; i<maintaingoals.size(); i++)
				clone.maintaingoals.add(((MElement)maintaingoals.get(i)).clone());
		}
		if(maintaingoalrefs!=null)
		{
			clone.maintaingoalrefs = SCollection.createArrayList();
			for(int i=0; i<maintaingoalrefs.size(); i++)
				clone.maintaingoalrefs.add(((MElement)maintaingoalrefs.get(i)).clone());
		}
		if(metagoals!=null)
		{
			clone.metagoals = SCollection.createArrayList();
			for(int i=0; i<metagoals.size(); i++)
				clone.metagoals.add(((MElement)metagoals.get(i)).clone());
		}
		if(metagoalrefs!=null)
		{
			clone.metagoalrefs = SCollection.createArrayList();
			for(int i=0; i<metagoalrefs.size(); i++)
				clone.metagoalrefs.add(((MElement)metagoalrefs.get(i)).clone());
		}
	}
}
