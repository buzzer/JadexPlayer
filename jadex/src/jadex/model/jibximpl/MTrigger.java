package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  The base trigger.
 */
public class MTrigger extends MElement implements IMTrigger
{
	//-------- xml attributes --------

	/** The filter. */
	protected MExpression filter;

	/** The internal events. */
	protected ArrayList internalevents;

	/** The message events. */
	protected ArrayList messageevents;

	/** The goals. */
	protected ArrayList goalfinisheds;

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
		if(filter!=null)
			ret.add(filter);
		if(internalevents!=null)
			ret.addAll(internalevents);
		if(messageevents!=null)
			ret.addAll(messageevents);
		if(goalfinisheds!=null)
			ret.addAll(goalfinisheds);
		return ret;
	}

	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(filter!=null)
		{
			// Hack!!! Dependency to runtime class.
			filter.checkClass(SReflect.findClass0("jadex.runtime.IFilter", null), report);
		}

		IMReference[] refs = getGoalFinisheds();
		for(int i=0; i<refs.length; i++)
		{
			String goalname = refs[i].getReference();
			IMReferenceableElement	target = getScope().getGoalbase().getReferenceableElement(goalname);
			if(target==null)
			{
				report.addEntry(refs[i], "Referenced goal not found '"+goalname+"'.");
			}
			/*else
			{
				IMReferenceParameter[]	params	= refs[i].getParameters();
				for(int p=0; p<params.length; p++)
				{
					Object	para	= target instanceof IMParameterElement
						? (Object)((IMParameterElement)target).getParameter(params[i].getReference())
						: (Object)((IMParameterElementReference)target).getParameterReference(params[i].getReference());
					if(para==null)
					{
						report.addEntry(this, "Referenced parameter '"+params[i].getReference()+"' not found in referenced element "+target);
					}
				}
			}*/
		}

		refs = getMessageEvents();
		for(int i=0; i<refs.length; i++)
		{
			String evname = refs[i].getReference();
			IMReferenceableElement	target = getScope().getEventbase().getReferenceableElement(evname);
			if(target==null)
			{
				report.addEntry(refs[i], "Referenced message event not found '"+evname+"'.");
			}
			else if(!(target instanceof IMMessageEvent) && !(target instanceof IMMessageEventReference))
			{
				report.addEntry(refs[i], "Referenced event must be message event '"+evname+"'.");
			}
			/*else
			{
				IMReferenceParameter[]	params	= refs[i].getParameters();
				for(int p=0; p<params.length; p++)
				{
					Object	para	= target instanceof IMParameterElement
						? (Object)((IMParameterElement)target).getParameter(params[i].getReference())
						: (Object)((IMParameterElementReference)target).getParameterReference(params[i].getReference());
					if(para==null)
					{
						report.addEntry(this, "Referenced parameter '"+params[i].getReference()+"' not found in referenced element "+target);
					}
				}
			}*/
		}

		refs = getInternalEvents();
		for(int i=0; i<refs.length; i++)
		{
			String evname = refs[i].getReference();
			IMReferenceableElement	target = getScope().getEventbase().getReferenceableElement(evname);
			if(target==null)
			{
				report.addEntry(refs[i], "Referenced internal event not found '"+evname+"'.");
			}
			else if(!(target instanceof IMInternalEvent) && !(target instanceof IMInternalEventReference))
			{
				report.addEntry(refs[i], "Referenced event must be internal event '"+evname+"'.");
			}
			/*else
			{
				IMReferenceParameter[]	params	= refs[i].getParameters();
				for(int p=0; p<params.length; p++)
				{
					Object	para	= target instanceof IMParameterElement
						? (Object)((IMParameterElement)target).getParameter(params[i].getReference())
						: (Object)((IMParameterElementReference)target).getParameterReference(params[i].getReference());
					if(para==null)
					{
						report.addEntry(this, "Referenced parameter '"+params[i].getReference()+"' not found in referenced element "+target);
					}
				}
			}*/
		}
	}

	//-------- internal events --------

	/**
	 *  Get the internal events.
	 *  @return The internal events.
	 */
	public IMReference[] getInternalEvents()
	{
		if(internalevents==null)
			return new IMReference[0];
		return (IMReference[])internalevents.toArray(new IMReference[internalevents.size()]);
	}

	/**
	 *  Create an internal event.
	 *  @param ref	The referenced internal event.
	 *  @return The new internal event.
	 */
	public IMReference createInternalEvent(String ref)
	{
		if(internalevents==null)
			internalevents = SCollection.createArrayList();

		MReference ret = new MReference();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		internalevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an internal event.
	 *  @param ref	The internal event.
	 */
	public void deleteInternalEvent(IMReference ref)
	{
		if(!internalevents.remove(ref))
			throw new RuntimeException("Internal event ref not found: "+ref);
	}


	//-------- goals --------

	/**
	 *  Get the goals.
	 *  @return The goals.
	 */
	public IMReference[] getGoalFinisheds()
	{
		if(goalfinisheds==null)
			return new IMReference[0];
		return (IMReference[])goalfinisheds.toArray(new IMReference[goalfinisheds.size()]);
	}

	/**
	 *  Create a goal.
	 *  @param ref	The referenced goal.
	 *  @return The new goal.
	 */
	public IMReference createGoalFinished(String ref)
	{
		if(goalfinisheds==null)
			goalfinisheds = SCollection.createArrayList();

		MReference ret = new MReference();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		goalfinisheds.add(ret);
		return ret;
	}

	/**
	 *  Delete a goal.
	 *  @param ref	The goal.
	 */
	public void deleteGoalFinished(IMReference ref)
	{
		if(!goalfinisheds.remove(ref))
			throw new RuntimeException("Goal ref not found: "+ref);
	}


	//-------- message events --------

	/**
	 *  Get the message events.
	 *  @return The message events.
	 */
	public IMReference[] getMessageEvents()
	{
		if(messageevents==null)
			return new IMReference[0];
		return (IMReference[])messageevents.toArray(new IMReference[messageevents.size()]);
	}

	/**
	 *  Create an message event.
	 *  @param ref	The referenced message event.
	 *  @return The new message event.
	 */
	public IMReference createMessageEvent(String ref)
	{
		if(messageevents==null)
			messageevents = SCollection.createArrayList();

		MReference ret = new MReference();
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		messageevents.add(ret);
		return ret;
	}

	/**
	 *  Delete an message event.
	 *  @param ref	The message event.
	 */
	public void deleteMessageEvent(IMReference ref)
	{
		if(!messageevents.remove(ref))
			throw new RuntimeException("Message ref not found: "+ref);
	}


	//-------- filter --------

	/**
	 *  Get the filter.
	 *  @return The filter.
	 */
	public IMExpression	getFilter()
	{
		return filter;
	}

	/**
	 *  Create the filter.
	 *  @param expression	The filter expression.
	 *  @return The new filter.
	 */
	public IMExpression	createFilter(String expression)
	{
		assert expression!=null : this;
		this.filter = new MExpression();
		filter.setExpressionText(expression);
		filter.setOwner(this);
		filter.init();
		return filter;
	}

	/**
	 *  Delete teh filter.
	 */
	public void deleteFilter()
	{
		filter = null;
	}

	//-------- jibx related --------

	/**
	 *  Add a internalevent.
	 *  @param internalevent The internalevent.
	 */
	public void addInternalEvent(MReference internalevent)
	{
		if(internalevents==null)
			internalevents = SCollection.createArrayList();
		internalevents.add(internalevent);
	}

	/**
	 *  Get an iterator for all internalevents.
	 *  @return The iterator.
	 */
	public Iterator iterInternalEvents()
	{
		return internalevents==null? Collections.EMPTY_LIST.iterator(): internalevents.iterator();
	}

	/**
	 *  Add a messageevent.
	 *  @param messageevent The messageevent.
	 */
	public void addMessageEvent(MReference messageevent)
	{
		if(messageevents==null)
			messageevents = SCollection.createArrayList();
		messageevents.add(messageevent);
	}

	/**
	 *  Get an iterator for all messageevents.
	 *  @return The iterator.
	 */
	public Iterator iterMessageEvents()
	{
		return messageevents==null? Collections.EMPTY_LIST.iterator(): messageevents.iterator();
	}

	/**
	 *  Add a goal.
	 *  @param goal The goal.
	 */
	public void addGoalFinished(MReference goal)
	{
		if(goalfinisheds==null)
			goalfinisheds = SCollection.createArrayList();
		goalfinisheds.add(goal);
	}

	/**
	 *  Get an iterator for all goals.
	 *  @return The iterator.
	 */
	public Iterator iterGoalFinisheds()
	{
		return goalfinisheds==null? Collections.EMPTY_LIST.iterator(): goalfinisheds.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MTrigger clone = (MTrigger)cl;
		if(filter!=null)
			clone.filter = (MExpression)filter.clone();
		if(internalevents!=null)
		{
			clone.internalevents = SCollection.createArrayList();
			for(int i=0; i<internalevents.size(); i++)
				clone.internalevents.add(((MElement)internalevents.get(i)).clone());
		}
		if(messageevents!=null)
		{
			clone.messageevents = SCollection.createArrayList();
			for(int i=0; i<messageevents.size(); i++)
				clone.messageevents.add(((MElement)messageevents.get(i)).clone());
		}
		if(goalfinisheds!=null)
		{
			clone.goalfinisheds = SCollection.createArrayList();
			for(int i=0; i<goalfinisheds.size(); i++)
				clone.goalfinisheds.add(((MElement)goalfinisheds.get(i)).clone());
		}
	}
}
