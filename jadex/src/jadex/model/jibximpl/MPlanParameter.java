package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SUtil;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  The plan parameter.
 */
public class MPlanParameter extends MParameter implements IMPlanParameter
{
	//-------- xml-attributes --------

	/** The internal event mappings. */
	protected ArrayList internaleventmappings;

	/** The message event mappings. */
	protected ArrayList messageeventmappings;

	/** The goal mappings. */
	protected ArrayList goalmappings;

	//-------- xml-methods --------

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);

		// Check if mappings can be resolved
		IMPlan plan = (IMPlan)getOwner();
		checkMappings(getInternalEventMappings(),  plan.getTrigger()!=null?
			plan.getTrigger().getInternalEvents(): null, report, getScope().getEventbase());
		checkMappings(getMessageEventMappings(),  plan.getTrigger()!=null?
			plan.getTrigger().getMessageEvents(): null, report, getScope().getEventbase());
		
		// For goals, either goal or goalfinished trigger suffices -> check both at once.
		IMReference[]	goalrefs	= null;
		if(plan.getTrigger()!=null)
		{
			goalrefs	= (IMReference[])SUtil.joinArrays(plan.getTrigger().getGoalFinisheds(), plan.getTrigger().getGoals());
		}
		checkMappings(getGoalMappings(), goalrefs, report, getScope().getGoalbase());
	}

	/**
	 *  Check if mappings are correct with respect to the triggers.
	 *  @param mappings The mapping names.
	 *  @param triggers The trigger names.
	 *  @param report The report.
	 */
	protected void checkMappings(String[] mappings, IMReference[] triggers, Report report, IMBase base)
	{
		if(mappings.length>0)
		{
			HashSet trigset = SCollection.createHashSet();
			for(int i=0; triggers!=null && i<triggers.length; i++)
				trigset.add(triggers[i].getReference());
			for(int i=0; i<mappings.length; i++)
			{
				// Check if trigger is known.
				int idx = mappings[i].indexOf(".");
				if(idx==-1)
					report.addEntry(this, "Wrong mapping syntax, must be <triggername>.<paramname>: "+mappings[i]);
				String trname = mappings[i].substring(0, idx);
				if(!trigset.contains(trname))
					report.addEntry(this, "Trigger not found: "+mappings[i]);

				// Check if parameter is known.
				String paramname = mappings[i].substring(idx+1);
				IMReferenceableElement elem = base.getReferenceableElement(trname);

				if(!findParameter(elem, paramname))
					report.addEntry(this, "Parameter of trigger not found: "+paramname);
			}
		}
	}

	/**
	 *  Find a parameter of a parameter element.
	 *  @param elem The element.
	 *  @param paramname The parameter name.
	 *  @return True, if could be found.
	 */
	protected boolean findParameter(IMReferenceableElement elem, String paramname)
	{
		boolean success = false;

		if(elem instanceof IMParameterElement)
		{
			if(((IMParameterElement)elem).getParameter(paramname)!=null)
			{
				success = true;
			}
		}
		else if(elem instanceof IMParameterElementReference)
		{
			if(((IMParameterElementReference)elem).getParameterReference(paramname)!=null)
			{
				success = true;
			}
			else
			{
				// It could be the special case that the param refences will be created
				// automatically in correspondence to the original ones in init of MParameterElementReference.
				success = findParameter(((IMParameterElementReference)elem).getReferencedElement(), paramname);
			}
		}
		return success;
	}


	//-------- internal event mappings --------

	/**
	 *  Create an internal event mapping.
	 *  @param name The mapping name.
	 */
	public void createInternalEventMapping(String name)
	{
		if(internaleventmappings==null)
			internaleventmappings = SCollection.createArrayList();
		internaleventmappings.add(name);
	}

	/**
	 *  Delete an internal event mapping.
	 *  @param name The mapping name.
	 */
	public void deleteInternalEventMapping(String name)
	{
		internaleventmappings.remove(name);
	}

	/**
	 * Get all parameter internal event mappings.
	 * @return All mappings.
	 */
	public String[] getInternalEventMappings()
	{
		if(internaleventmappings==null)
			return new String[0];
		return (String[])internaleventmappings.toArray(new String[internaleventmappings.size()]);
	}

	//-------- message event mappings --------

	/**
	 *  Create an message event mapping.
	 *  @param name The mapping name.
	 */
	public void createMessageEventMapping(String name)
	{
		if(messageeventmappings==null)
			messageeventmappings = SCollection.createArrayList();
		messageeventmappings.add(name);
	}

	/**
	 *  Delete an message event mapping.
	 *  @param name The mapping name.
	 */
	public void deleteMessageEventMapping(String name)
	{
		messageeventmappings.remove(name);
	}

	/**
	 * Get all parameter message event mappings.
	 * @return All mappings.
	 */
	public String[] getMessageEventMappings()
	{
		if(messageeventmappings==null)
			return new String[0];
		return (String[])messageeventmappings.toArray(new String[messageeventmappings.size()]);
	}

	//-------- goal event mappings --------

	/**
	 *  Create a goal event mapping.
	 *  @param name The mapping name.
	 */
	public void createGoalMapping(String name)
	{
		if(goalmappings==null)
			goalmappings = SCollection.createArrayList();
		goalmappings.add(name);
	}

	/**
	 *  Delete a goal event mapping.
	 *  @param name The mapping name.
	 */
	public void deleteGoalMapping(String name)
	{
		goalmappings.remove(name);
	}

	/**
	 * Get all parameter goal mappings.
	 * @return All mappings.
	 */
	public String[] getGoalMappings()
	{
		if(goalmappings==null)
			return new String[0];
		return (String[])goalmappings.toArray(new String[goalmappings.size()]);
	}

	//-------- jibx related --------

	/**
	 *  Get an iterator for all mappings.
	 *  @return The iterator.
	 */
	public Iterator iterInternalEventMappings()
	{
		return internaleventmappings==null? Collections.EMPTY_LIST.iterator(): internaleventmappings.iterator();
	}

	/**
	 *  Get an iterator for all mappings.
	 *  @return The iterator.
	 */
	public Iterator iterMessageEventMappings()
	{
		return messageeventmappings==null? Collections.EMPTY_LIST.iterator(): messageeventmappings.iterator();
	}

	/**
	 *  Get an iterator for all mappings.
	 *  @return The iterator.
	 */
	public Iterator iterGoalMappings()
	{
		return goalmappings==null? Collections.EMPTY_LIST.iterator(): goalmappings.iterator();
	}

}
