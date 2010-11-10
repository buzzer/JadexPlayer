package jadex.model;

/**
 *  The goalbase behaviour interface.
 */
public interface IMGoalbase extends IMBase
{
	//-------- constants --------

	/** The constant for defining the dummy goal. */
	public static final String DUMMY_GOAL = "dummy_goal";

	
	//-------- perform goals --------

	/**
	 *  Get all known perform goals.
	 *  @return The perform goals.
	 */
	public IMPerformGoal[] getPerformGoals();

	/**
	 *  Get a perform goal by name.
	 *  @param name The perform goal name.
	 *  @return The perform goal with that name (if any).
	 */
	public IMPerformGoal getPerformGoal(String name);

	/**
	 *  Create a perform goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.  
	 *  @return The perform goal.
	 */
	public IMPerformGoal	createPerformGoal(String name, String exported, boolean retry, long retrydelay, String exclude);

	/**
	 *  Delete a perform goal.
	 *  @param goal	The perform goal.
	 */
	public void	deletePerformGoal(IMPerformGoal goal);


	//-------- perform goal references --------

	/**
	 *  Get all known perform goal references.
	 *  @return The perform goal references.
	 */
	public IMPerformGoalReference[] getPerformGoalReferences();

	/**
	 *  Get a goal by name.
	 *  @param name The perform goal reference name.
	 *  @return The perform goal reference with that name (if any).
	 */
	public IMPerformGoalReference getPerformGoalReference(String name);

	/**
	 *  Create a perform goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The perform goal reference.
	 */
	public IMPerformGoalReference	createPerformGoalReference(String name, String exported, String ref);

	/**
	 *  Delete a perform goal reference.
	 *  @param goal	The perform goal reference.
	 */
	public void	deletePerformGoalReference(IMPerformGoalReference goal);

	
	//-------- achieve goals --------

	/**
	 *  Get all known achieve goals.
	 *  @return The achieve goals.
	 */
	public IMAchieveGoal[] getAchieveGoals();

	/**
	 *  Get an achieve goal by name.
	 *  @param name The achieve goal name.
	 *  @return The achieve goal with that name (if any).
	 */
	public IMAchieveGoal getAchieveGoal(String name);

	/**
	 *  Create an achieve goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.  
	 *  @return The achieve goal.
	 */
	public IMAchieveGoal	createAchieveGoal(String name, String exported, boolean retry, long retrydelay, String exclude);

	/**
	 *  Delete an achieve goal.
	 *  @param goal	The achieve goal.
	 */
	public void	deleteAchieveGoal(IMAchieveGoal goal);


	//-------- achieve goal references --------

	/**
	 *  Get all known achieve goal references.
	 *  @return The achieve goal references.
	 */
	public IMAchieveGoalReference[] getAchieveGoalReferences();

	/**
	 *  Get an goal by name.
	 *  @param name The achieve goal reference name.
	 *  @return The achieve goal reference with that name (if any).
	 */
	public IMAchieveGoalReference getAchieveGoalReference(String name);

	/**
	 *  Create an achieve goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The achieve goal reference.
	 */
	public IMAchieveGoalReference	createAchieveGoalReference(String name, String exported, String ref);

	/**
	 *  Delete an achieve goal reference.
	 *  @param goal	The achieve goal reference.
	 */
	public void	deleteAchieveGoalReference(IMAchieveGoalReference goal);

	
	//-------- query goals --------

	/**
	 *  Get all known query goals.
	 *  @return The query goals.
	 */
	public IMQueryGoal[] getQueryGoals();

	/**
	 *  Get a query goal by name.
	 *  @param name The query goal name.
	 *  @return The query goal with that name (if any).
	 */
	public IMQueryGoal getQueryGoal(String name);

	/**
	 *  Create a query goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.  
	 *  @return The query goal.
	 */
	public IMQueryGoal	createQueryGoal(String name, String exported, boolean retry, long retrydelay, String exclude);

	/**
	 *  Delete a query goal.
	 *  @param goal	The query goal.
	 */
	public void	deleteQueryGoal(IMQueryGoal goal);


	//-------- query goal references --------

	/**
	 *  Get all known query goal references.
	 *  @return The query goal references.
	 */
	public IMQueryGoalReference[] getQueryGoalReferences();

	/**
	 *  Get a query goal by name.
	 *  @param name The query goal reference name.
	 *  @return The query goal reference with that name (if any).
	 */
	public IMQueryGoalReference getQueryGoalReference(String name);

	/**
	 *  Create a query goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The query goal reference.
	 */
	public IMQueryGoalReference	createQueryGoalReference(String name, String exported, String ref);

	/**
	 *  Delete a query goal reference.
	 *  @param goal	The query goal reference.
	 */
	public void	deleteQueryGoalReference(IMQueryGoalReference goal);

	
	//-------- maintain goals --------

	/**
	 *  Get all known maintain goals.
	 *  @return The maintain goals.
	 */
	public IMMaintainGoal[] getMaintainGoals();

	/**
	 *  Get a maintain goal by name.
	 *  @param name The maintain goal name.
	 *  @return The maintain goal with that name (if any).
	 */
	public IMMaintainGoal getMaintainGoal(String name);

	/**
	 *  Create a maintain goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.  
	 *  @param recur	Should the goal be recurred when not succeeded after all available plans have been tried.
	 *  @param recurdelay	An optional delay (in milliseconds) before the goal is recurred (-1 for no delay).
	 *  @return The maintain goal.
	 */
	public IMMaintainGoal	createMaintainGoal(String name, String exported, boolean retry, long retrydelay, String exclude, boolean recur, long recurdelay);

	/**
	 *  Delete a maintain goal.
	 *  @param goal	The maintain goal.
	 */
	public void	deleteMaintainGoal(IMMaintainGoal goal);


	//-------- maintain goal references --------

	/**
	 *  Get all known maintain goal references.
	 *  @return The maintain goal references.
	 */
	public IMMaintainGoalReference[] getMaintainGoalReferences();

	/**
	 *  Get an goal by name.
	 *  @param name The maintain goal reference name.
	 *  @return The maintain goal reference with that name (if any).
	 */
	public IMMaintainGoalReference getMaintainGoalReference(String name);

	/**
	 *  Create an maintain goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The maintain goal reference.
	 */
	public IMMaintainGoalReference	createMaintainGoalReference(String name, String exported, String ref);

	/**
	 *  Delete an maintain goal reference.
	 *  @param goal	The maintain goal reference.
	 */
	public void	deleteMaintainGoalReference(IMMaintainGoalReference goal);

	
	//-------- meta goals --------

	/**
	 *  Get all known meta goals.
	 *  @return The meta goals.
	 */
	public IMMetaGoal[] getMetaGoals();

	/**
	 *  Get a meta goal by name.
	 *  @param name The meta goal name.
	 *  @return The meta goal with that name (if any).
	 */
	public IMMetaGoal getMetaGoal(String name);

	/**
	 *  Create a meta goal.
	 *  @param name	The name of the goal.
	 *  @param exported	Flag indicating if this goal may be referenced from outside capabilities.
	 *  @param retry	Should the goal be retried when not succeeded after the first plan.
	 *  @param retrydelay	An optional delay (in milliseconds) before the next plan is executed (-1 for no delay).
	 *  @param exclude	An optional identifer specifying which plans to exclude after they have been executed.  
	 *  @return The meta goal.
	 */
	public IMMetaGoal	createMetaGoal(String name, String exported, boolean retry, long retrydelay, String exclude);

	/**
	 *  Delete a meta goal.
	 *  @param goal	The meta goal.
	 */
	public void	deleteMetaGoal(IMMetaGoal goal);


	//-------- meta goal references --------

	/**
	 *  Get all known meta goal references.
	 *  @return The meta goal references.
	 */
	public IMMetaGoalReference[] getMetaGoalReferences();

	/**
	 *  Get a meta goal by name.
	 *  @param name The meta goal reference name.
	 *  @return The meta goal reference with that name (if any).
	 */
	public IMMetaGoalReference getMetaGoalReference(String name);

	/**
	 *  Create a meta goal reference.
	 *  @param name	The name of the goal reference.
	 *  @param exported	Flag indicating if this goal reference may be referenced from outside capabilities.
	 *  @param ref	The referenced goal (or null for abstract).
	 *  @return The meta goal reference.
	 */
	public IMMetaGoalReference	createMetaGoalReference(String name, String exported, String ref);

	/**
	 *  Delete a meta goal reference.
	 *  @param goal	The meta goal reference.
	 */
	public void	deleteMetaGoalReference(IMMetaGoalReference goal);


	//-------- not xml related --------

	/**
	 *  Get all known goals.
	 *  @return The goals.
	 */
	public IMGoal[] getGoals();

	/**
	 *  Get a goal by name.
	 *  @param name The goal name.
	 *  @return The goal with that name (if any).
	 */
	public IMGoal getGoal(String name);

	/**
	 *  Get all goal references.
	 *  @return The goal references.
	 */
	public IMGoalReference[] getGoalReferences();

	/**
	 *  Get a goal reference.
	 *  @param name The name.
	 *  @return The goal reference.
	 */
	public IMGoalReference getGoalReference(String name);
}
