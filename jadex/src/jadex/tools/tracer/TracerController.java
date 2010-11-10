/*
 * TracerController.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Nov 18, 2004.  
 * Last revision $Revision: 5025 $ by:
 * $Author: Alex $ on $Date: 2007-03-16 00:03:38 +0100 (Fri, 16 Mar 2007) $.
 */
package jadex.tools.tracer;

import jadex.adapter.fipa.AMSAgentDescription;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.runtime.*;
import jadex.tools.ontology.*;
import jadex.tools.tracer.nodes.*;
import jadex.tools.tracer.ui.TracerUI;

import java.util.*;

import javax.swing.SwingUtilities;

import com.touchgraph.graphlayout.Edge;


/** the controller of this application
 * <code>TracerController</code>
 * @since Nov 18, 2004 
 */
public class TracerController
{

	/** <code>HISTORY</code>: how much does the controller looks back for misplaced packages */
	protected final static int		HISTORY				= 50;

	/** <code>agents</code> stores traces by agent */
	protected final Map				agents;

	/** <code>ui</code> the gui for this tracer agent */
	protected TracerUI				ui;

	/** <code>comms</code>: communications */
	protected final Map				comms;

	/** <code>commsArray</code>: contains all ids of communications in the hashmap comms */
	protected final Vector			commsArray;

	/** <code>DEFAULT_NODES_LIMIT</code>:*/
	public static final int			DEFAULT_NODES_LIMIT	= 100;

	/** <code>MINUMUM_NODES_LIMIT</code>: */
	public static final int			MINUMUM_NODES_LIMIT	= 1;

	/** subscribe for 10 seconds */
	private static final int		SUBSCRIPTION_TIME	= 10000;

	private static final Tracing	EMPTY_TRACING		= new Tracing();

	/** <code>prototype</code> the agent used as prototype */
	protected final TAgent			prototype;

	/** <code>trace_agent</code>: the agent this controller belongs to */
	protected IExternalAccess		tagent;

	private BasicAgentIdentifier	aid;

	
	private boolean					active;

	/** Init
	 * Constructor: <code>TracerController</code>.
	 * 
	 * @param agent
	 */
	public TracerController(final IExternalAccess agent)
	{
		//System.out.println("TracerController: " + agent);
		this.tagent = agent;

		agents = new HashMap();

		comms = new HashMap();

		commsArray = new Vector();

		prototype = new TAgent(new AgentIdentifier("default", true));
		resetPrototype();
		
		active = true;
		aid = tagent.getAgentIdentifier();
		
		new SubscriptionRefresh().start();
	}
	
	protected void	resetPrototype()
	{
		prototype.setIgnored(true);
		prototype.setNodesLimit(DEFAULT_NODES_LIMIT);
		prototype.setEnforceNodeLimit(true);
		prototype.setTracing(new Tracing(true, false, true, false, true, false, true));		
	}

	/** 
	 * @param ui
	 */
	public void setUI(TracerUI ui)
	{
		this.ui = ui;
	}

	/** 
	 * @param aid
	 * @param tmsg
	 */
	public void add(final AgentIdentifier aid, OTrace tmsg)
	{
		if(aid != null)
		{
			TNode tn = wrap(aid, tmsg);

			TAgent agent_node = getAgentNode(aid);

			agent_node.setAlive(true);

			if(agent_node.isIgnored())
			{
				trace_off(aid);
				return;
			} // else 

			enforceNodesLimit(agent_node);

			if(agent_node.addTrace(tn))
			{
				addReferences(agent_node, tn);
				if(tn.isCommunication())
				{
					addCommunication(tn);
				}
				ui.addNode(tn);
			}
		} // end if  agent != null 
	}

	/** sends a trace on action 
	 * @param agent
	 */
	public void traceAgent(TAgent agent)
	{
		if(agent == prototype) return;
		if(agent.isIgnored()) trace_off(agent.getAID());
		else trace_request(agent.getAID(), new SendTraces(SUBSCRIPTION_TIME, agent.getTracing()));
	}

	/** @param aid
	 */
	private void trace_off(AgentIdentifier aid)
	{
		trace_request(aid, new SendTraces(0, EMPTY_TRACING));
	}

	/** @param aid
	 */
	private void trace_request(BasicAgentIdentifier aid, final Object action)
	{
		if(!aid.equals(this.aid)) try
		{
			IMessageEvent me = tagent.createMessageEvent("tool_request");
			me.getParameterSet(SFipa.RECEIVERS).addValue(aid);
			me.getParameter(SFipa.REPLY_WITH).setValue(SFipa.createUniqueId(tagent.getName()));
			me.getParameter(SFipa.CONVERSATION_ID).setValue(tagent.getName());
			me.setContent(action);
			tagent.sendMessage(me);
		}
		catch(MessageFailureException e)
		{
			//			String text = SUtil.wrapText("Tool request failed: "+e.getMessage());
			//			JOptionPane.showMessageDialog(SGUI.getWindowParent(ui), text,
			//				"Tool Problem", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/** 
	 * @param agent
	 */
	public void	enforceNodesLimit(TAgent agent)
	{
		if(agent.getEnforceNodeLimit())
		{
			int limit	= agent.getNodesLimt();
			
			// Store nodes to be removed and remember connected nodes which will be orphaned.
			TNode[] traces = agent.getTraces();
			Set orphanes	= new HashSet(); 
			Set removed	= new HashSet(); 
			for(int i=0; i<(traces.length-limit); i++)
			{
				removed.add(traces[i]);
				for(int j=0; j<traces[i].edgeCount(); j++)
				{
					Edge	edge	= traces[i].edgeAt(j);
					orphanes.add(edge.getFrom()==traces[i] ? edge.getTo() : edge.getFrom());
				}
			}
			
			// Remove stored nodes.
			for(Iterator it=removed.iterator(); it.hasNext(); )
			{
				removeTrace((TNode)it.next());	
			}
	
			// Connect orphaned nodes which are not removed themselves to agent.
			for(Iterator it=orphanes.iterator(); it.hasNext(); )
			{
				TNode	node	= (TNode)it.next();
				if(node!=agent && !removed.contains(node))
				{
					ui.addEdge(agent, node);
				}
			}
		}
	}

	/** Creates a communication node that collects all communications 
	 * @param tn
	 */
	protected void addCommunication(TNode tn)
	{
		List cList = (List)comms.get(tn.getName());
		if(cList == null)
		{
			comms.put(tn.getName(), cList = new Vector());
			commsArray.add(tn.getName());
		}
		if(tn instanceof TSend)
		{
			cList.add(0, tn);
		}
		else
		{
			cList.add(tn);
		}
	}

	/** 
	 * @param aid
	 * @return a trace index for given agent ID (a new one if needed)
	 */
	protected TAgent getAgentNode(AgentIdentifier aid)
	{
		TAgent ta = (TAgent)agents.get(aid);

		if(ta == null)
		{
			// new agent
			ta = new TAgent(aid);
			agents.put(aid, ta);
			ta.setVisible(false);
			ta.copyFilters(prototype);
			// Thanks...
			ta.setIgnored(ta.isIgnored() || this.aid.equals(aid));
			
			if(ui != null) ui.addNode(ta);
		}

		return ta;
	}

	/** Updates the reference of nodes.
	 *  First it looks for any parents of this node and links it as child.
	 *  If there is no parent the agent will be choosen.
	 *  In case of a Belief - this will be the belief node.
	 * @param anode
	 * @param tn
	 */
	protected void addReferences(TAgent anode, TNode tn)
	{
		String[] causes = tn.getCausesArray();
		// update references
		if(causes != null)
		{
			int i = causes.length;
			while(i > 00)
			{
				TNode c = anode.getTrace(causes[--i]);
				tn.addParent(c);
			}
		}

		String name = tn.getName();
		TNode[] traces = anode.getTraces();
		int i = traces.length;
		while(i > 00)
		{
			TNode node = traces[--i];
			if(node.hasCause(name))
			{
				node.addParent(tn);
			}
		}

		if(!tn.hasParents() && !tn.hasChildren())
		{
			// add it to the agent
			tn.addParent(anode);
		}

		if(tn.isBeliefAccess())
		{
			Map beliefs = anode.getBeliefs();
			TBelief bel = (TBelief)beliefs.get(tn.getName());
			if(bel == null)
			{
				bel = new TBelief(tn.getAID(), tn.getName());
				beliefs.put(bel.getName(), bel);
				bel.addParent(anode);
				ui.addNode(bel);
			}
			tn.addParent(bel);
		}

	}

	/** 
	 * kill tracer agent
	 */
	public void uiClosed()
	{
		active = false;
		deregisterAll();
		new Thread()
		{
			public void run()
			{
				tagent.killAgent();
			}
		}.start();
	}

	/** 
	 * 
	 */
	public void close()
	{
		active = false;
		deregisterAll();
	}

	// ----------------- agent --------------------

	/** removes from all agents
	 * 
	 */
	private void deregisterAll()
	{
		Object[] as = agents.keySet().toArray();
		int i = as.length;
		while(i > 00)
		{
			trace_off((AgentIdentifier)as[--i]);
		}
	}

	/** Ignore an agent.
	 * @param agent
	 * @param b
	 */
	public void ignoreAgent(TAgent agent, boolean b)
	{
		// System.out.println("Stub: TracerController.ignore "+agent+": "+b);
		if(agent == null || (aid.equals(agent.getAID()) && !b)) return;
		agent.setIgnored(b);
		traceAgent(agent);
	}

	/** Ignore or observe all agents 
	 * @param b the flag
	 */
	public void ignoreAll(boolean b)
	{
		// System.out.println("Stub: TracerController.ignoreAll");
		Object[] elems = agents.values().toArray();
		int i = elems.length;
		while(i > 00)
		{
			ignoreAgent((TAgent)elems[--i], b);
		}
	}

	/** Remove the agent. Works only for death agents as live agents will send their traces
	 * @param agent
	 */
	public void removeAgent(TAgent agent)
	{
		if(agent == null) return;
		// System.out.println("Stub: TracerController.removeAgent");
		TAgent ta = (TAgent)agents.remove(agent.getAID());
		if(ta != null)
		{
			removeTraces(ta.getTraces());
			//there is only one parent for an agent
			ta.removeParent((TNode)ta.getParent());
			Object[] bs = ta.getBeliefs().values().toArray();
			int i = bs.length;
			while(i > 00)
			{
				TBelief bel = (TBelief)bs[--i];
				bel.deleted();
			}
			ta.deleted();
		}
	}

	/** 
	 * Removes all agents from the tracer - and all observations
	 */
	public void removeDeathAgents()
	{
		// System.out.println("Stub: TracerController.removeAllAgents");
		Object[] agents = this.agents.values().toArray();
		int i = agents.length;
		while(i > 00)
		{
			TAgent ta = (TAgent)agents[--i];
			if(!ta.isAlive())
			{
				removeAgent(ta); //~
			}
		}
	}

	/** Removes the trace
	 * @param trace
	 */
	public void removeTrace(TNode trace)
	{
		if(trace instanceof TAgent)
		{
			removeAgent((TAgent)trace);
		}
		else
		{
			deleteNode(trace); //~
		}
	}

	/** 
	 * Deletes selected traces. All unconected traces will be deleted
	 * @param nodes
	 * 
	 */
	public void removeTraces(TNode[] nodes)
	{
		int i = nodes.length;
		while(i > 00)
		{
			deleteNode(nodes[--i]); // ~
		}
	}

	//--------------------------------------------------------------------------

	/** 
	 * @param aid
	 * @param ti
	 * @return a tracer node
	 */
	public static final TNode wrap(AgentIdentifier aid, OTrace ti)
	{
		if(ti instanceof OAction) return new TAction(aid, ti);
		if(ti instanceof OBelief)
		{
			OBelief ob = (OBelief)ti;
			if(OBelief.ACCESS_READ.equals(ob.getAccess()))
			{
				return new TRead(aid, ti);
			}
			return new TWrite(aid, ti);
		}
		if(ti instanceof OEvent) return new TEvent(aid, ti);
		if(ti instanceof OGoal) return new TGoal(aid, ti);
		if(ti instanceof OPlan) return new TPlan(aid, ti);
		if(ti instanceof OMessage)
		{
			OMessage om = (OMessage)ti;
			if(om.isIncoming())
			{
				return new TReceive(aid, ti);
			}
			return new TSend(aid, ti);
		}

		throw new IllegalArgumentException("Unknown trace information: " + ti);
	}

	/** Removes a node from the traces list - and if it is a communication also from comms hash 
	 * @param node 
	 * @see jadex.tools.tracer.nodes.TNodeListener#nodeDeleted(jadex.tools.tracer.nodes.TNode)
	 */
	private void deleteNode(TNode node)
	{
		// purge communications
		if(node.isCommunication())
		{
			List aList = (List)comms.get(node.getName());
			if(aList != null)
			{
				aList.remove(node);
				if(aList.isEmpty())
				{
					comms.remove(node.getName());
					commsArray.remove(node.getName());
				}
			}
		}
		// magic
		TNode[] parents = node.getParents();
		int pi = parents.length;
		while(pi > 00)
		{
			TNode p = parents[--pi];
			node.removeParent(p);
		}

		TNode[] children = node.getChildren();
		int ci = children.length;
		while(ci > 00)
		{
			TNode c = children[--ci];
			pi = parents.length;
			while(pi > 00)
			{
				TNode p = parents[--pi];
				c.addParent(p);
			}
			c.removeParent(node);
		}

		node.deleted();
	}

	/**  
	 * @return all communication ids as a list
	 */
	public String[] getComms()
	{
		synchronized(commsArray)
		{
			return (String[])commsArray.toArray(new String[commsArray.size()]);
		}
	}

	/**  
	 * @param id is the id of the communication
	 * @return a list of communications with the same id or null
	 */
	public List getComms(String id)
	{
		// System.out.println("Stub: TracerController.getComms");
		return (List)comms.get(id);
	}

	/** 
	 * @return all agents known to this tracer
	 */
	public TAgent[] getAgents()
	{
		synchronized(agents)
		{
			Collection tas = agents.values();
			return (TAgent[])tas.toArray(new TAgent[tas.size()]);
		}
	}

	/**
	 * Getter: <code>prototype</code>.
	 * @return Returns the prototype.
	 */
	public TAgent getPrototype()
	{
		return prototype;
	}

	/** @return a linked list of agent names
	 */
	public List getAgentsNames()
	{
		return new LinkedList(this.agents.keySet());
	}

	/** adds an agent to the tracer
	 * @param ad
	 */
	public void agentBorn(final AMSAgentDescription ad)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				//  trace_request(ad.getName(),new SendTraces(SUBSCRIPTION_TIME, EMPTY_TRACING));

				TAgent ta = getAgentNode(ad.getName());
				ta.setAlive(true);
				ta.setTracing(prototype.getTracing());
			}
		});
	}


	/** @param ad
	 */
	public void agentsDied(AMSAgentDescription ad)
	{
		TAgent ta = (TAgent)this.agents.get(ad.getName());
		if(ta != null) ta.setAlive(false);
	}

	class SubscriptionRefresh extends Thread
	{

		/** 
		 * @see java.lang.Thread#run()
		 */
		public void run()
		{
			try
			{
				while(active)
				{
	
					setName("SubscriptionRefresh: " + getAID());
	
					try
					{
						sleep(SUBSCRIPTION_TIME / 2);
					}
					catch(Exception e)
					{/*NOP*/}
					TAgent[] agents = getAgents();
					int i = agents.length;
					while(i > 00)
					{
						TAgent a = agents[--i];
						if(!a.isIgnored() && a.isAlive()) traceAgent(a);
					}
				}
			}
			catch(AgentDeathException e) {/**/}
		}
	}

	final static String	TRUE	= "true";

	final static String	FALSE	= "false";


	/** 
	 * @param ps
	 */
	public void setProperties(Properties ps)
	{
		Tracing t = prototype.getTracing();
		t.setActions(TRUE.equalsIgnoreCase(ps.getProperty("trace.actions", ""+t.isActions())));
		t.setBeliefReads(TRUE.equalsIgnoreCase(ps.getProperty("trace.reads", ""+t.isBeliefReads())));
		t.setBeliefWrites(TRUE.equalsIgnoreCase(ps.getProperty("trace.writes", ""+t.isBeliefWrites())));
		t.setEvents(TRUE.equalsIgnoreCase(ps.getProperty("trace.events", ""+t.isEvents())));
		t.setGoals(TRUE.equalsIgnoreCase(ps.getProperty("trace.goals", ""+t.isGoals())));
		t.setMessages(TRUE.equalsIgnoreCase(ps.getProperty("trace.messages", ""+t.isMessages())));
		t.setPlans(TRUE.equalsIgnoreCase(ps.getProperty("trace.plans", ""+t.isPlans())));

		prototype.setIgnored(TRUE.equalsIgnoreCase(ps.getProperty("trace.ignore_at_first", ""+prototype.isIgnored())));
		prototype.setNodesLimit(Integer.parseInt(ps.getProperty("trace.nodes_limit", ""+prototype.getNodesLimt())));
		prototype.setEnforceNodeLimit(TRUE.equalsIgnoreCase(ps.getProperty("trace.enforce_nodes_limit", ""+prototype.getEnforceNodeLimit())));
	}

	/** 
	 * @param ps
	 */
	public void getProperties(Properties ps)
	{
		Tracing t = prototype.getTracing();
		ps.setProperty("trace.actions", t.isActions() ? TRUE : FALSE);
		ps.setProperty("trace.reads", t.isBeliefReads() ? TRUE : FALSE);
		ps.setProperty("trace.writes", t.isBeliefWrites() ? TRUE : FALSE);
		ps.setProperty("trace.events", t.isEvents() ? TRUE : FALSE);
		ps.setProperty("trace.goals", t.isGoals() ? TRUE : FALSE);
		ps.setProperty("trace.messages", t.isMessages() ? TRUE : FALSE);
		ps.setProperty("trace.plans", t.isPlans() ? TRUE : FALSE);

		ps.setProperty("trace.ignore_at_first", prototype.isIgnored() ? TRUE : FALSE);
		ps.setProperty("trace.nodes_limit", Integer.toString(prototype.getNodesLimt()));
		ps.setProperty("trace.enforce_nodes_limit", prototype.getEnforceNodeLimit() ? TRUE : FALSE);
	}

	/** 
	 * @return aid of the tracer agent
	 */
	public Object getAID()
	{
		return tagent.getAgentIdentifier();
	}

}

/*  
 */