package jadex.runtime.impl.agenda.treeimpl;

import java.io.Serializable;
import java.util.*;
import jadex.util.*;
import jadex.util.collection.SCollection;
import jadex.runtime.impl.IEncodable;
import jadex.runtime.impl.agenda.IAgendaAction;
import jadex.runtime.impl.agenda.IAgendaEntry;


/**
 *  The agenda entry consisting of an action
 *  to execute and a source element. 
 */
public class TreeAgendaEntry implements IEncodable, Serializable, IAgendaEntry
{
	//-------- attributes --------

	/** The entry list. */
	protected IAgendaAction action;

	/** The agenda. */
	protected TreeAgenda agenda;

	/** The cause for this action (e.g. a system event or an element). */
	protected Object cause;

	/** The processed flag. */
	protected boolean processed;

	/** The executed flag. */
	protected boolean executed;

	/** The children (lazily created). */
	protected List children;

	/** The parent. */
	protected TreeAgendaEntry parent;

	//-------- constructors --------

	// todo: change the cause of the action to SystemEvent as
	// conditional actions are the normal case?!
	/**
	 *  Create a new event list.
	 */
	protected TreeAgendaEntry(IAgendaAction action, TreeAgenda agenda, Object cause)
	{
		this.action = action;
		this.agenda = agenda;
		this.cause = cause;
	}

	//-------- methods --------

//public static long	timer;
//public static long	delib;
//public static int	cnt;

	/**
	 *  Execute an agenda entry, when
	 *  the precondition is valid.
	 */
	public void execute()
	{
		if(action.isValid())
		{
//long start	= System.currentTimeMillis();
			action.execute();
//long end	= System.currentTimeMillis();
//timer	+= end - start;
//if(action instanceof DeliberateGoalActivationAction
//	|| action instanceof DeliberateInhibitedGoalsReactivationAction
//	|| action instanceof DeactivateInhibitedGoalsAction)
//{
//	delib	+= end - start;
//}
//cnt++;
//if(cnt==5000)
//{
//	System.out.println("Actions (total, delib, ratio): "+timer+", "+delib+", "+(int)(((double)delib/timer)*100));
//	timer	= 0;
//	delib	= 0;
//	cnt	= 0;
//}
			executed = true;
		}
		//else
		//	System.out.println("Precondition invalid of: "+action);

		/*else if(action instanceof DeliberateInhibitedGoalsReactivationAction)
		{
			IRGoal goal = ((DeliberateInhibitedGoalsReactivationAction)action).getGoal();
			System.out.println("Precondition invalid of: "+action+" "+goal.getLifecycleState()+" "+goal.getProcessingState());
		}*/
		/*else if(action instanceof DeliberateGoalActivationAction)
		{
			IRGoal goal = ((DeliberateGoalActivationAction)action).getGoal();
			IAgendaActionPrecondition prec = ((AbstractAgendaAction)action).getPrecondition();
			System.out.println("Precondition invalid of: "+action+" "+goal.getLifecycleState()+" "+prec.getClass()+" "+prec.check());//+" "+goal.getProcessingState());
		}*/
		/*else if(action instanceof RGoal.DropAction)
		{
			IRGoal goal = ((RGoal.DropAction)action).getGoal();
			IAgendaActionPrecondition prec = ((AbstractAgendaAction)action).getPrecondition();
			System.out.println("Precondition invalid of: "+action+" "+goal.getLifecycleState()+" "+prec.getClass()+" "+prec.check());//+" "+goal.getProcessingState());
		}*/
	}

	/**
	 *  Get the action.
	 *  @return The action.
	 */
	public IAgendaAction getAction()
	{
		return this.action;
	}

	/**
	 *  Get the cause.
	 *  @return The cause.
	 */
	public Object getCause()
	{
		return this.cause;
	}

	/**
	 *  Set if the entry is already processed.
	 *  @param processed The processed flag.
	 */
	protected void setProcessed(boolean processed)
	{
		assert processed;
		this.processed = processed;
	}

	/**
	 *  Test if this action was executed. It is only
	 *  executed when the action could be executed.
	 */
	protected boolean isExecuted()
	{
		return executed;
	}

	/**
	 *  Test if this entry is already processed.
	 *  (Processed=scheduled for execution and
	 *  possibly executed!)
	 *  Does not include children.
	 *  @return True if already processed.
	 */
	public boolean isProcessed()
	{
		return processed;
	}

	/**
	 *  Test if this entry and all children entries are processed.
	 *  @return True, if completed.
	 */
	public boolean isCompleted()
	{
		boolean ret = isProcessed();
		if(children!=null)
		{
			for(int i=0; i<children.size() && ret; i++)
			{
				ret = ((TreeAgendaEntry)children.get(i)).isCompleted();
			}
		}
		return ret;
	}

	/**
	 *  Add a new entry as child.
	 *  @param entry The new child.
	 */
	protected void addChild(TreeAgendaEntry entry)
	{
		// Lazily create childrens list (for speed).
		if(children==null)
			this.children = SCollection.createArrayList();
		children.add(entry);
	}

	/**
	 *  Remove an entry as child.
	 *  @param entry The new child.
	 */
	public void removeChild(TreeAgendaEntry entry)
	{
		if(children!=null)
			children.remove(entry);
	}

	/**
	 *  Set the parent.
	 *  @param parent The parent.
	 */
	protected void setParent(TreeAgendaEntry parent)
	{
		this.parent = parent;
	}

	/**
	 *  Get the parent.
	 *  @return The parent.
	 */
	public TreeAgendaEntry getParent()
	{
		return this.parent;
	}

	/**
	 *  Get all executable entries from each branch.
	 *  (Uses depth first algo.)
	 * /
	public List getExecutableEntries()
	{
		List ret = SCollection.createArrayList();
		if(!isProcessed())
		{
			ret.add(this);
		}

		// Hack!!! Unprocessed entries have no children.
		// Unless it's the current entry (in process but not yet processed) :-(
		for(int i=0; i<children.size(); i++)
		{
			ret.addAll(((AgendaEntry)children.get(i)).getExecutableEntries());
		}
		return ret;
	}*/

	List cs;
	/**
	 *  Get all executable entries from each branch.
	 *  (Uses a breadth first algo.)
	 */
	public List getExecutableEntries()
	{
		List ret;
		if(children!=null)
		{
			ret	= SCollection.createArrayList();

			if(cs==null)
				cs	= SCollection.createArrayList();
			else
				cs.clear();
			cs.add(children);

			// Breadth-search by traversing list of child arrasy
			// ( (children1), (children2), ...)
			for(int i=0; i<cs.size(); i++)
			{
				List list = (List)cs.get(i);
				for(int j=0; j<list.size(); j++)
				{
					TreeAgendaEntry tmp = (TreeAgendaEntry)list.get(j);
					if(!tmp.isProcessed())
					{
						ret.add(tmp);
					}
					if(tmp.children!=null)
					{
						cs.add(tmp.children);
					}
				}
			}
		}
		else
		{
			if(!isProcessed())
			{
				ret	= Collections.singletonList(this);
			}
			else
			{
				ret	= Collections.EMPTY_LIST;
			}
		}
		return ret;
	}

	/**
	 *  Get all executable entries from each branch.
	 *  (Uses a breadth first algo.)
	 */
	public TreeAgendaEntry getNextExecutableEntry()
	{
		TreeAgendaEntry ret = !isProcessed()? this: null;

		if(ret==null && children!=null)
		{
			if(cs==null)
				cs	= SCollection.createArrayList();
			else
				cs.clear();
			cs.add(children);

			// Breadth-search by traversing list of child arrasy
			// ( (children1), (children2), ...)
			for(int i=0; ret==null && i<cs.size(); i++)
			{
				List list = (List)cs.get(i);
				for(int j=0; ret==null && j<list.size(); j++)
				{
					TreeAgendaEntry tmp = (TreeAgendaEntry)list.get(j);
					if(!tmp.isProcessed())
					{
						ret = tmp;
					}
					else if(tmp.children!=null)
					{
						cs.add(tmp.children);
					}
				}
			}
		}
		return ret;
	}

	/**
	 *  Get the root entry.
	 *  @return The root entry;
	 */
	public TreeAgendaEntry getRoot()
	{
		return parent==null? this: parent.getRoot();
	}

	/**
	 *  Get direct children (does not contain this element).
	 *  @return The direct children.
	 */
	public List getChildren()
	{
		if(children!=null)
			return Collections.unmodifiableList(children);
		else
			return Collections.EMPTY_LIST;
	}

	/**
	 *  Get all children (does not contain this element).
	 *  @return All children.
	 */
	public List getAllChildren()
	{
		List ret;
		if(children!=null)
		{
			ret	= SCollection.createArrayList();
			ret.addAll(children);
			for(int i=0; i<children.size(); i++)
				ret.addAll(((TreeAgendaEntry)children.get(i)).getAllChildren());
		}
		else
		{
			ret	= Collections.EMPTY_LIST;
		}
		return ret;
	}

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public  String toString()
	{
		return ""+getAction();//+" source: "+getSource().getName();
	}

	/**
	 *  Create encodable representation of this element.
	 */
	public Map getEncodableRepresentation()
	{
        //HashMap rep	= SCollection.createHashMap();
        Map rep	= SCollection.createHashMap();
		rep.put("isencodeablepresentation", "true"); // to distinguish this map from normal maps.

		// Todo: listenernotification and garbage collection actions are not encodable!?
		if(getAction() instanceof IEncodable)
		{
			rep.put("action", ((IEncodable)getAction()).getEncodableRepresentation());
		}
//		else
//		{
//			System.out.println("Not encodable: "+getAction().getClass().getName());
//			rep.put("action", ""+getAction());
//		}
		rep.put("hashcode", ""+hashCode());
		rep.put("processed", ""+isProcessed());
		rep.put("executed", ""+isExecuted());
		rep.put("valid", ""+action.isValid());
		rep.put("actionclass", SReflect.getInnerClassName(getAction().getClass()));
		rep.put("text", ""+getAction());
		if(children!=null)
        {
			for(int i=0; i<children.size(); i++)
            {
				rep.put(""+i, ((TreeAgendaEntry)children.get(i)).getEncodableRepresentation());
                //agenda.num++;
            }
        }
        return rep;
	}
}