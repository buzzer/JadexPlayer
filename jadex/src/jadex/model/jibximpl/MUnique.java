package jadex.model.jibximpl;

import java.util.*;
import jadex.model.IMUnique;
import jadex.util.collection.SCollection;

/**
 *  Uniqueness properties of a goal.
 *  Defines which parameters are considered,
 *  to test if two goals are equal.
 */
public class MUnique extends MElement implements IMUnique
{
	//-------- xml attributes --------

	/** The list of excluded parameter/set names. */
	protected ArrayList excludes;

	//-------- constructors --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		// Check if excluded paramers can be found.
		String[]	excludes	= getExcludes();
		MGoal	goal	= (MGoal)getOwner();
		for(int i=0; i<excludes.length; i++)
		{
			if(goal.getParameter(excludes[i])== null && goal.getParameterSet(excludes[i])==null)
			{
				report.addEntry(this, "Referenced parameter (set) '"+excludes[i]+"' not found.");
			}
		}
	}
	
	//-------- excludes --------

	/**
	 *  Get the excluded parameters (as reference string).
	 *  @return	The excluded parameters.
	 */
	public String[]	getExcludes()
	{
		if(excludes==null)
			return new String[0];
		return (String[])excludes.toArray(new String[excludes.size()]);
	}

	/**
	 *  Create an excluded parameter (as reference string).
	 *  @param ref	The reference.
	 */
	public void	createExclude(String ref)
	{
		if(excludes==null)
			excludes = SCollection.createArrayList();
		excludes.add(ref);
	}

	/**
	 *  Delete an excluded parameter (as reference string).
	 *  @param ref	The reference.
	 */
	public void	deleteExclude(String ref)
	{
		if(!excludes.remove(ref))
			throw new RuntimeException("Exclude param not found: "+ref);
	}
	//-------- jibx related --------

	/**
	 *  Get an iterator for all excludess.
	 *  @return The iterator.
	 */
	public Iterator iterExcludes()
	{
		return excludes==null? Collections.EMPTY_LIST.iterator(): excludes.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MUnique clone = (MUnique)cl;
		if(excludes!=null)
			clone.excludes = (ArrayList)excludes.clone();
	}
}
