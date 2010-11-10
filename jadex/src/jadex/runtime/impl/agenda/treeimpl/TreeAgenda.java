package jadex.runtime.impl.agenda.treeimpl;

import jadex.runtime.impl.RBDIAgent;
import jadex.runtime.impl.RMetaGoal;
import jadex.runtime.impl.agenda.IAgenda;
import jadex.runtime.impl.agenda.IAgendaAction;
import jadex.runtime.impl.agenda.IAgendaEntry;
import jadex.runtime.impl.agenda.plans.ExecutePlanStepAction;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 *  The agenda of an agent, holding the actions the agents wants to perform
 *  as tree structures ([most] consequences of actions become new leafs).
 *  @see jadex.runtime.impl.agenda.treeimpl.TreeAgendaEntry
 */
public class TreeAgenda implements IAgenda, Serializable
{
	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent	agent;

	/** The agenda entries. */
	protected List entries;

	/** The agenda entries added from external threads. */
	protected final List ext_entries;

	/** The agenda state. */
	protected int state;

	/** The currently processed agenda entry. */
	protected TreeAgendaEntry current;

	/** The keep old entries flag (for debugging). */
	protected boolean keep_processed_entries;// = true;

	/** The maximum execution time for invoke later actions. */
	protected long max_time = 20000;

	//-------- constructors ---------

	/**
	 *  Create an agenda.
	 *  @param agent	The agent.
	 */
	public TreeAgenda(RBDIAgent agent)
	{
		this.agent	= agent;
		this.entries	= SCollection.createLinkedList();
		//this.entries	= SCollection.createArrayList();
		this.ext_entries = SCollection.createVector();
	}

	//-------- methods --------

	/**
	 *  Check if there are unprocessed elements in the agenda.
	 */
	public boolean	isEmpty()
	{
		// Optimized for speed, as used in every behavior action.
		boolean	empty	= entries.isEmpty() && ext_entries.isEmpty();
		if(!empty)
		{
			empty	= true;
			for(int i=0; empty && i<ext_entries.size(); i++)
			{
				empty	= !((IAgendaAction)ext_entries.get(i)).isValid();
			}
			for(int i=0; empty && i<entries.size(); i++)
			{
				empty	= ((TreeAgendaEntry)entries.get(i)).getNextExecutableEntry()==null;
			}
		}
		return empty;
	}

	/**
	 *  Get the number of entries in the agenda.
	 */
	public int	size()
	{
		// Optimized for speed (needed?).
		return entries.isEmpty() && ext_entries.isEmpty() ? 0
			: getUnprocessedEntries().size();
	}

	/**
	 *  Select and execute an agenda entry.
	 *  @return true when
	 */
	public IAgendaEntry	executeAction()
	{
		assert current==null;

		copyExternalEntries();
		TreeAgendaEntry	ae	= select();
		if(ae!=null)
		{
			this.current	= ae;
			ae.execute();
			processed();
		}
		
		return ae;
	}

	/**
	 *  Copy externally added entries into the agenda list.
	 */
	public void copyExternalEntries()
	{
		// Add external entries (caused from property events of other threads).
		synchronized(ext_entries)
		{
			while(ext_entries.size()>0)
			{
				add((IAgendaAction)ext_entries.get(0), null); // todo: external actions: cause=null?
				ext_entries.remove(0);
			}
		}
	}

	/**
	 *  Fetch the next agenda entry for processing.
	 */
	public TreeAgendaEntry	select()
	{
		TreeAgendaEntry	ret	= null;
		// todo: allow strategies to be implemented for selecting action
		// todo: a) from which of parallel trees
		// todo: b) which entry from one tree

		for(int i=0; i<entries.size() && ret==null; i++)
		{
			TreeAgendaEntry	tmp = (TreeAgendaEntry)entries.get(i);
			ret = tmp.getNextExecutableEntry();
			//List cands = tmp.getExecutableEntries();
			//if(cands.size()>0)
			//	ret = (AgendaEntry)cands.get(0);
		}

		return ret;
	}

	/**
	 *  Indicate that an action is processed.
	 */
	protected void processed()
	{
		assert current!=null;
		assert !current.isProcessed();
		assert entries.contains(current.getRoot());

		current.setProcessed(true);
		TreeAgendaEntry root = current.getRoot();
		if(root.isCompleted() && !keep_processed_entries)
			entries.remove(root);
		current = null;
	}

	/**
	 *  Add an agenda entry.
	 */
	protected void	add(TreeAgendaEntry entry)
	{
		assert !contains(entry);

		// In the current strategy everything is a side-effect,
		// except plan steps, which do not belong to meta-plans.
		// todo: allow strategies to be implemented for adding as top-level or child nodes
		if(current!=null
			&& (!(entry.getAction() instanceof ExecutePlanStepAction)
			|| ((ExecutePlanStepAction)entry.getAction()).getPlan().getRootGoal()
				.getProprietaryGoal() instanceof RMetaGoal))
//			|| !((ExecutePlanStepAction)entry.getAction()).getPlan().getState().equals(RPlan.STATE_BODY)))
				//.getType().equals(IMGoalbase.META_LEVEL_REASONING_GOAL)))
			//&& !(entry.getAction() instanceof InvokeLaterAction)) // Hack?!
		{
//			if(agent.getName().indexOf("Trigger")!=-1)
//				System.out.println("Adding to "+current+": "+entry);
			current.addChild(entry);
			entry.setParent(current);
		}
		else
		{
			entries.add(entry);
		}

		state++;
		// info event
		//agent.throwSystemEvent(event_agenda_changed);
		
		// Execution is not triggered internally when in creating/terminated state
		if(!agent.getLifecycleState().equals(RBDIAgent.LIFECYCLESTATE_CREATING) &&
			!agent.getLifecycleState().equals(RBDIAgent.LIFECYCLESTATE_TERMINATED))
		{
			agent.getAgentAdapter().wakeup();
		}
	}

	/**
	 *  Add an agenda entry.
	 */
	public void	add(IAgendaAction action, Object cause)
	{
		add(new TreeAgendaEntry(action, this, cause));
	}

	/**
	 *  Add an agenda entry from external thread.
	 */
	public void addExternal(IAgendaAction action)
	{
		synchronized(ext_entries)
		{
			ext_entries.add(action);
		}
		agent.getAgentAdapter().wakeup();
	}

	/**
	 *  Get the agenda state. Changes whenever the
	 *  agenda changes. Can be used to determine changes.
	 *  @return The actual state.
	 */
	public int getState()
	{
		return this.state;
	}

	/**
	 *  Get unprocessed entries as list.
	 */
	public List getUnprocessedEntries()
	{
		List ret = SCollection.createArrayList();
		for(int i=0; i<entries.size(); i++)
		{
			ret.addAll(((TreeAgendaEntry)entries.get(i)).getExecutableEntries());
		}

		// Remove current entry (is not yet
		// processed, but is not considered unprocessed)
		if(current!=null)
			ret.remove(current);

		return ret;
	}

	/**
	 *  Get unprocessed entries as list.
	 */
	protected List asList()
	{
		List ret = SCollection.createArrayList();
		ret.addAll(entries);
		for(int i=0; i<entries.size(); i++)
		{
			ret.addAll(((TreeAgendaEntry)entries.get(i)).getAllChildren());
		}
		return ret;
	}

	/**
	 *  Test if an entry is contained in the agenda.
	 *  @param entry The entry.
	 *  @return True, if contains entry.
	 */
	protected boolean contains(TreeAgendaEntry entry)
	{
		boolean ret = entries.contains(entry);
		for(int i=0; i<entries.size() && !ret; i++)
		{
			TreeAgendaEntry tmp = (TreeAgendaEntry)entries.get(i);
			ret = tmp.getAllChildren().contains(entry);
		}
		return ret;
	}

	/**
	 *  Get the current agenda entry.
	 *  @return The current agenda entry.
	 */
	public IAgendaEntry getCurrentEntry()
	{
		return current;
	}

	/**
	 *  Get all top-level entries
	 *  @return The top-level entries.
	 */
	public List getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	/**
	 *  Get the agent.
	 */
	protected RBDIAgent getAgent()
	{
		return agent;
	}

	/**
	 *  Create a string representation of the agenda.
	 */
	public String	toString()
	{
		return SReflect.getInnerClassName(getClass())
			+"(current="+current
			+", unprocessed="+getUnprocessedEntries()
			+")";
	}
}
