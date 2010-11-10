/*
 * TracerAdapter.java
 * Copyright (c) 2004 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by 9walczak on Oct 25, 2004.  
 * Last revision $Revision: 5016 $ by:
 * $Author: braubach $ on $Date: 2007-03-15 13:33:49 +0100 (Thu, 15 Mar 2007) $.
 */
package jadex.tools.tracer;

import jadex.adapter.IToolAdapter;
import jadex.adapter.fipa.AgentAction;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.SFipa;
import jadex.model.IMEventbase;
import jadex.runtime.ISystemEventListener;
import jadex.runtime.SystemEvent;
import jadex.runtime.impl.*;
import jadex.runtime.impl.agenda.IAgendaEntry;
import jadex.tools.ontology.OAction;
import jadex.tools.ontology.OBelief;
import jadex.tools.ontology.OEvent;
import jadex.tools.ontology.OGoal;
import jadex.tools.ontology.OMessage;
import jadex.tools.ontology.OPlan;
import jadex.tools.ontology.OTrace;
import jadex.tools.ontology.SendTraces;
import jadex.tools.ontology.Tracing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * <code>TracerAdapter</code> Filters observation based on the values returned
 * by properties
 */
public class TracerAdapter implements ISystemEventListener, IToolAdapter, Serializable
{

	// /** "tracing" a property from the agent PropertyBase as specified in the
	// ADF that may include
	// * following values: reads, writes, plans, goals, actions, messages,
	// events */
	// public static final String TRACING_PROPERTY = "tracing";
	//
	// /** is included in the tracing property if belief reads should be traced
	// */
	// public static final String READS = "reads";
	//
	// /** is included in the tracing property if belief changes should be
	// traced */
	// public static final String WRITES = "writes";
	//
	// /** when the plans of the agent should be traced */
	// public static final String PLANS = "plans";
	//
	// /** when the goals of the agent should be traced */
	// public static final String GOALS = "goals";
	//
	// /** when the agenda actions of the agent should be traced */
	// public static final String ACTIONS = "actions";
	//
	// /** when the messages comming in and going out should be traced */
	// public static final String MESSAGES = "messages";
	//
	// /** all events occuring in the agent */
	// public static final String EVENTS = "events";

	/** the name of this class to be skiped by stack trace */
	private static final String	CLASS_NAME	= TracerAdapter.class.getName();

	/** the sequence of a trace message */
	private static long			seq			= 0;

	/** enumerates events otherwise unidentified */
	private static int			event_seq	= 0;

	/** <code>agent</code>: keeps an agent refernce */
	private final RBDIAgent		agent;

	private Set					traced_events;

	/** stores tools by name */
	private final Map			tools		= new HashMap();

	/** a shortcut to the data of tools */
	private ToolData[]			toolsData;

	/**
	 * Init Constructor: <code>TracerAdapter</code>.
	 *
	 * @param agent the agent using this filter
	 */
	public TracerAdapter(RBDIAgent agent)
	{
		this.agent = agent;
		traced_events = new HashSet();

		// todo use adf properties to setup tracing events
		// RPropertybase properties = agent.getPropertybase();
		//
		// String traced = (String) properties.getProperty(TRACING_PROPERTY);
		//
		// if (traced != null)
		// {
		// traced = traced.toLowerCase();
		// Tracing st = new Tracing(
		// traced.indexOf(ACTIONS) >= 0,
		// traced.indexOf(READS) >= 0,
		// traced.indexOf(WRITES) >= 0,
		// traced.indexOf(MESSAGES) >= 0,
		// traced.indexOf(GOALS) >= 0,
		// traced.indexOf(PLANS) >= 0,
		// traced.indexOf(EVENTS) >= 0);
		// addTracedEvents(st);
		// }
		//
		// System.out.println("TracerAdapter included.\n Tracing: " +
		// traced_events);
		// register();
	}

	/**
	 * sends the trace to all receivers
	 *
	 * @param name
	 * @param value
	 * @param causes
	 * @param trace
	 */
	private void send(String name, String value, String causes, OTrace trace)
	{
		try
		{
			if(toolsData != null && toolsData.length > 0)
			{
				trace.setName(name);
				trace.setValue(value);
				trace.setCause(causes == null ? "" : causes);

				// create content
				trace.setSeq(Long.toString(seq++));
				trace.setThread(Thread.currentThread().getName());
				trace.setTime(Long.toString(System.currentTimeMillis()));

				Throwable t = new Exception();
				t.fillInStackTrace();
				StackTraceElement stack[] = t.getStackTrace();
				for(int sfi = 0; sfi < stack.length; sfi++)
				{
					String clazz = stack[sfi].getClassName();
					if(CLASS_NAME.equals(clazz)) continue;

					String method = stack[sfi].getMethodName();
					String file = stack[sfi].getFileName();
					int line = stack[sfi].getLineNumber();

					String sLine = clazz + "." + method;
					if(file != null)
					{
						sLine += "(" + file + (line >= 0 ? ":" + line + ")" : ")");
					}
					trace.addStack(sLine);
				}
				int ti = toolsData.length;
				while(ti > 00)
				{
					ToolData td = toolsData[--ti];
					if(td.subscription_end >= System.currentTimeMillis())
					{
						td.send(trace);
					}
					else
					{
					//	System.out.println("Removing sub scription: "
					//			+ (td.subscription_end - System.currentTimeMillis()));
						removeTracerTool(td.aid);
					}
				}
			}

			if(tools.isEmpty())
			{
				updateRegistration();
			}
		}
		catch(Exception e)
		{ /* NOP */
		}
	}

	// //////////////////////// System Event Listener //////////////////////////

	/**
	 * Creates traces from system events
	 *
	 * @param events
	 * @see jadex.runtime.ISystemEventListener#systemEventsOccurred(jadex.runtime.SystemEvent[])
	 */
	public void systemEventsOccurred(SystemEvent[] events)
	{
		// System.out.println("Stub: TracerAdapter.systemEventsOccurred");
		int i = events.length;
		while(i-- > 0)
		{
			String type = events[i].getType();
			Object src = events[i].getSource();
			String evc = events[i].getCause();
			Object val = events[i].getValue();

//			System.out.println("" + events[i] + " cause=" + evc);

			if(SystemEvent.GOAL_ADDED.equals(type))
			{
//				if(src instanceof RProcessGoal)
//				{
//					RProcessGoal goal = (RProcessGoal)src;
//					String id = goal.getName();
//
//					OGoal data = new OGoal();
//					String cause = "";
//					if (evc!=null) cause=evc + ", ";
//					IRGoal irgoal=goal.getProprietaryGoal();
//					if (irgoal!=null) {
//						RProcessGoal pg = irgoal.getParent();
//						if(pg != null)
//						{
//							RPlan causeElement = pg.getPlanInstance();
//							if(causeElement != null) cause += causeElement.getName();
//						}
//					}
//					data.setGoalState(OGoal.GOALSTATE_ADOPTED);
//					send(id, "" + goal, cause, data);
//				} else
				if(src instanceof RGoal)
				{
					RGoal goal = (RGoal)src;
					String id = goal.getName();

					OGoal data = new OGoal();
					String cause = "";
					if(goal instanceof RMetaGoal)
					{
						cause	= ((RMetaGoal)goal).getTrigger().getName();
					}
					else
					{
						RProcessGoal pg = goal.getRealParent();
						if(pg!=null)
						{
							RPlan causeElement = pg.getPlanInstance();
							if(causeElement != null) cause = causeElement.getName();
						}
					}
					data.setGoalState(OGoal.GOALSTATE_ADOPTED);
					data.setGoalKind(goal.getGoalKind());
					send(id, ""+goal, cause, data);
				}
			}
			else if(SystemEvent.PLAN_ADDED.equals(type))
			{
				if(src instanceof RPlan)
				{
					RPlan plan = (RPlan)src;
					String id = plan.getName();
					OPlan data = new OPlan();
					String cause = "";
					IREvent event = (IREvent)plan.getExpressionParameter("event");
					if(event != null) cause = event.getName() + ",";

					IREvent ie = plan.getInitialEvent();
					if (ie instanceof IRMessageEvent) {
						cause += getMessageID((IRMessageEvent)ie) + ", ";
					}

					RProcessGoal pg = plan.getRootGoal();
					if(pg != null)
					{
						IRGoal goal = pg.getProprietaryGoal();
						if(goal != null) {
							cause += goal.getName();
						}
						else
						{ // dummy goal
							cause += pg.getName();
						}
					}
					data.setPlanState(OPlan.PLANSTATE_STARTED);
					send(id, "" + plan, cause, data);
				}
			}
			else if(SystemEvent.FACT_CHANGED.equals(type))
			{
				if(src instanceof RBelief)
				{
					RBelief bel = (RBelief)src;
					traceWrite(bel.getName(), val, evc);
				}
			}
			else if(SystemEvent.FACT_READ.equals(type))
			{
				if(src instanceof RBelief)
				{
					RBelief bel = (RBelief)src;
					OBelief data = new OBelief();
					data.setAccess(OBelief.ACCESS_READ);
					send(bel.getName(), "" + val, evc, data);
				}
			}
			else if(SystemEvent.BSFACT_ADDED.equals(type))
			{
				if(src instanceof RBeliefSet)
				{
					RBeliefSet bs = (RBeliefSet)src;
					traceWrite(bs.getName(), "+" + val, evc);
				}
			}
			else if(SystemEvent.BSFACT_REMOVED.equals(type))
			{
				if(src instanceof RBeliefSet)
				{
					RBeliefSet bs = (RBeliefSet)src;
					traceWrite(bs.getName(), "-" + val, evc);
				}
			}
			else if(SystemEvent.BSFACT_CHANGED.equals(type))
			{
				if(src instanceof RBeliefSet)
				{
					RBeliefSet bs = (RBeliefSet)src;
					traceWrite(bs.getName(), "[" + events[i].getIndex() + "]" + val, evc);
				}
			}
			else if(SystemEvent.BSFACT_READ.equals(type))
			{
				if(src instanceof RBeliefSet)
				{
					RBeliefSet bs = (RBeliefSet)src;
					String id = bs.getName();

					int index = events[i].getIndex();
					if(index < 0 && val instanceof Object[])
					{
						Object facts[] = (Object[])val;
						StringBuffer sb = new StringBuffer("(");
						int j = facts.length;
						while(j-- > 0)
						{
							sb.append(facts[j]);
							sb.append(", ");
						}
						sb.append(')');
						OBelief data = new OBelief();
						data.setAccess(OBelief.ACCESS_READ);
						send(id, sb.toString(), evc, data);
					}
					else
					{
						OBelief data = new OBelief();
						data.setAccess(OBelief.ACCESS_READ);
						send(id, "" + val, evc, data);
					}
				}
			}
			else if(SystemEvent.AGENDA_STEP_DONE.equals(type))
			{
				if(val instanceof IAgendaEntry)
				{
					// Todo: do we need the parent?
					IAgendaEntry entry = (IAgendaEntry)val;
//					IAgendaEntry parent = entry.getParent();
					String c = null;

//					if(parent != null) c = parent.toString();
					Object cause = entry.getCause();
					if(c == null && cause != null) c = cause.toString();

					send(entry.toString(), entry.toString(), c, new OAction());
				}
			}
			else if(SystemEvent.MESSAGE_RECEIVED.equals(type))
			{
				if(src instanceof IRMessageEvent)
				{
					final IRMessageEvent	msg	= (IRMessageEvent)src;
					// Hack!!! Message parameters are not inited, and cannot be in system event transaction -> defer processing.
					agent.getInterpreter().invokeLater(new Runnable()
					{
						public void run()
						{
							traceIncomingMessage(msg);
						}
					});
				}
			}
			else if(SystemEvent.MESSAGE_SENT.equals(type))
			{
				if(src instanceof IRMessageEvent) traceOutgoingMessage((IRMessageEvent)src, evc);
			}
			else if(SystemEvent.INTERNAL_EVENT.equals(type))
			{
				if(val instanceof IRInternalEvent)
				{
					IRInternalEvent ie = (IRInternalEvent)val;
					IRParameter p = null;
					try
					{
						p = ie.getParameter(IMEventbase.CONDITION);
					}
					catch(RuntimeException e)
					{
						// e.printStackTrace();
					}
					String cause = p == null ? null : ((IRCondition)p.getValue()).getName();
					traceEvent(ie.getName(), val, cause);
					return;
				}

				if(val instanceof IRGoalEvent)
				{
					IRGoalEvent ge = (IRGoalEvent)val;
					traceEvent(ge.getName(), val, ge.getGoal().getName());
					return;
				}

				if(val instanceof IRMessageEvent)
				{
					IRMessageEvent me = (IRMessageEvent)val;
					//if(me.isIncoming()) //todo: is this ok?
					{
						IRMessageEvent inReply = me.getInReplyMessageEvent();
						traceEvent(me.getName(), val, (inReply == null) ? "" : inReply.getName());
						return;
					}
				}

				if(val instanceof IREvent)
				{
					IREvent ev = (IREvent)val;
					traceEvent(ev.getName(), val, evc);
				}
			}
			else
			{
				String cause = evc == null ? src.toString() : src.toString() + "," + evc.toString();

				traceEvent(type + event_seq++, events[i], cause);
			}

		}
	}

	/**
	 * Sends a belief trace to the tracer agent
	 *
	 * @param id
	 * @param value
	 * @param causes
	 */
	private void traceWrite(String id, Object value, String causes)
	{
		// System.out.println("Stub: TracerAdapter.traceWrite");
		OBelief data = new OBelief();
		data.setAccess(OBelief.ACCESS_WRITE);
		send(id, "" + value, causes, data);
	}

	/** traces events
	 * @param id
	 * @param ev
	 * @param causes
	 */
	private void traceEvent(String id, Object ev, String causes)
	{
		send(id, "" + ev, causes, new OEvent());
	}

	/**
	 * Traces outgoing messages
	 * @param e
	 * @param causes
	 */
	private void traceOutgoingMessage(IRMessageEvent e, String causes)
	{
//		System.out.println("traceOutgoingMessage: "+e);
		
		OMessage data = new OMessage();
		data.setFrom(agent.getName());
		data.setIncoming(false);

		Object rcvs[] = e.getParameterSet(SFipa.RECEIVERS).getValues();
		if(rcvs != null) for(int i = rcvs.length; --i >= 0;)
		{
			data.addTo(((AgentIdentifier)rcvs[i]).getName());
		}
		send(getMessageID(e), messageToString(e), causes, data);
	}

	/**
	 * Traces incoming messages
	 * @param e
	 */
	private void traceIncomingMessage(IRMessageEvent e)
	{
//		System.out.println("traceIncomingMessage: "+e);

		OMessage data = new OMessage();
		data.setFrom("" + e.getParameter(SFipa.SENDER).getValue());
		data.setIncoming(true);

		send(getMessageID(e), messageToString(e), null, data);
	}

	/** gives a textual representation of the message
	 * @param e
	 * @return the string representation of this message
	 */
	private static String messageToString(IRMessageEvent e)
	{
//		String p="unknown";
//		String c="unknown";
//		if (e.hasParameter("performative")) {
//			p = ""+e.getParameter("performative").getValue();
//		}
//		if (e.hasParameter("content")) {
//			c = ""+e.getParameter("content").getValue();
//		}
//
		return  String.valueOf(e.getContent());
	}

	/**
	 *  Get the message id.
	 * @param e The event.
	 * @return The message id.
	 */
	private static String getMessageID(IRMessageEvent e) {
		String p="";
//		if (e.hasParameter("performative")) {
//			p = ""+e.getParameter("performative").getValue();
//		}
		return p+e.getId();
	}

	/**
	 * @param tool
	 * @param request
	 * @param reply
	 * @see jadex.adapter.IToolAdapter#handleToolRequest(jadex.adapter.fipa.AgentIdentifier,
	 *      AgentAction,
	 *      jadex.adapter.IToolAdapter.IToolReply)
	 */
	public void handleToolRequest(AgentIdentifier tool, AgentAction request, IToolReply reply)
	{
		if(request instanceof SendTraces)
		{
			// System.out.println("SendTraces("+agent.getAgentName()+"): " +
			// request);
			SendTraces st = (SendTraces)request;
			if(st.getSubscriptionTime() > 0)
			{
				addTracerTool(new ToolData(tool, reply, (SendTraces)request));
			}
			else
			{
				removeTracerTool(tool);
			}
		}
	}

	private void addTracerTool(ToolData td)
	{
		tools.put(td.aid, td);
		toolsData = (ToolData[])tools.values().toArray(new ToolData[tools.values().size()]);
		updateRegistration();
	}

	private void removeTracerTool(AgentIdentifier aid)
	{
		if(tools.remove(aid) != null)
		{
			toolsData = (ToolData[])tools.values().toArray(new ToolData[tools.values().size()]);
			updateRegistration();
		}
	}

	private synchronized void updateRegistration()
	{
		Set old = traced_events;
		traced_events = new HashSet();

		int i = toolsData.length;
		while(i > 00)
		{
			addTracedEvents(toolsData[--i].filter);
		}
		if(!traced_events.equals(old))
		{
			deregisterAsSystemListener(agent);
			if(!traced_events.isEmpty())
			{
//				System.out.println("registering: "+traced_events);
				registerAsSystemListener(agent, (String[])traced_events
						.toArray(new String[traced_events.size()]));
			}
		}
	}

	/**
	 * set filters
	 *
	 * @param st - tracer on action
	 */
	private void addTracedEvents(Tracing st)
	{
		if(st.isBeliefReads())
		{
			traced_events.add(SystemEvent.FACT_READ);
			traced_events.add(SystemEvent.BSFACT_READ);
		}
		if(st.isBeliefWrites())
		{
			traced_events.add(SystemEvent.BSFACT_ADDED);
			traced_events.add(SystemEvent.BSFACT_CHANGED);
			traced_events.add(SystemEvent.BSFACT_REMOVED);
			// traced_events.add(SystemEvent.BELIEF_ADDED);
			// traced_events.add(SystemEvent.BELIEF_REMOVED);
			traced_events.add(SystemEvent.FACT_CHANGED);
		}
		if(st.isPlans())
		{
			traced_events.add(SystemEvent.PLAN_ADDED);
			// traced_events.add(SystemEvent.PLAN_CHANGED);
			// traced_events.add(SystemEvent.PLAN_REMOVED);
		}
		if(st.isGoals())
		{
			traced_events.add(SystemEvent.GOAL_ADDED);
			// traced_events.add(SystemEvent.GOAL_CHANGED);
			// traced_events.add(SystemEvent.GOAL_REMOVED);;
		}
		if(st.isMessages())
		{
			traced_events.add(SystemEvent.MESSAGE_RECEIVED);
			traced_events.add(SystemEvent.MESSAGE_SENT);
		}
		if(st.isActions())
		{
			traced_events.add(SystemEvent.AGENDA_STEP_DONE);
		}
		if(st.isEvents())
		{
			traced_events.add(SystemEvent.INTERNAL_EVENT);
		}
	}

	/**
	 * register as system event listener - recursive over capabilities
	 *
	 * @param cap
	 * @param te
	 */
	private void registerAsSystemListener(RCapability cap, String[] te)
	{
		try
		{
			cap.addSystemEventListener(this, new SystemEventFilter(te), false, false);
			RCapability[] caps = cap.getSubCapabilities();
			int i = caps.length;
			while(i-- > 0)
			{
				registerAsSystemListener(caps[i], te);
			}
		}
		catch(Exception e)
		{ /* NO OP */
			e.printStackTrace();
		}

	}

	/**
	 * stops to listen to system events - recursive over capabilities
	 *
	 * @param cap
	 */
	private void deregisterAsSystemListener(RCapability cap)
	{
		try
		{
			cap.removeSystemEventListener(this);
			RCapability[] caps = cap.getSubCapabilities();
			int i = caps.length;
			while(i-- > 0)
			{
				deregisterAsSystemListener(caps[i]);
			}
		}
		catch(Exception e)
		{ /* NO OP */
			e.printStackTrace();
		}
	}

	private static final class ToolData implements Serializable
	{
		final AgentIdentifier	aid;

		IToolReply				reply;

		SendTraces				request;

		Tracing					filter;

		long					subscription_end;

		/**
		 * Constructor for ToolData.
		 *
		 * @param aid
		 * @param reply
		 * @param request
		 */
		public ToolData(AgentIdentifier aid, IToolReply reply, SendTraces request)
		{
			this.aid = aid;
			this.reply = reply;
			this.request = request;
			this.filter = request.getTracing();
			this.subscription_end = System.currentTimeMillis() + request.getSubscriptionTime();
		}

		/**
		 * @param trace
		 */
		public void send(OTrace trace)
		{
			if(filter.isTracing(trace)) try
			{
				reply.sendInform(trace, false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return "tracer"
	 * @see jadex.adapter.IToolAdapter#getMessageClass()
	 */
	public Class getMessageClass()
	{
		return SendTraces.class;
	}

}

/*
 * $Log$
 * Revision 1.23  2006/12/31 00:18:52  braubach
 * *** empty log message ***
 *
 * Revision 1.22  2006/07/12 16:14:08  braubach
 * *** empty log message ***
 *
 * Revision 1.21  2006/07/12 13:47:16  walczak
 * fixed tracer adapter
 *
 * Revision 1.20  2006/07/11 12:06:23  walczak
 * added message id parameter to the IREventMessage interface
 *
 */