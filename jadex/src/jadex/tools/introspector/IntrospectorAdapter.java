package jadex.tools.introspector;

import jadex.adapter.IToolAdapter;
import jadex.adapter.fipa.AgentAction;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.adapter.fipa.Done;
import jadex.model.IMCapability;
import jadex.model.ISystemEventTypes;
import jadex.runtime.*;
import jadex.runtime.impl.*;
import jadex.tools.ontology.*;
import jadex.util.collection.IndexMap;
import jadex.util.collection.SCollection;

import java.io.Serializable;
import java.util.*;


/**
 * The tool message preprocessors grabs events from the tools and processes them
 * without letting them into the Jadex system.
 */
public class IntrospectorAdapter implements IToolAdapter, ISystemEventListener, Serializable
{
	//-------- static part --------
	
	static
	{
		// Force loading of ontology (e.g. sets bean info search path)
		try
		{
			Class.forName(IntrospectorOntology.class.getName());
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Could not find tool ontology: "+e);
		}
	}
	
	/** The introspector tool type. */
	public static final String TOOL_INTROSPECTOR	= "introspector";

	//-------- attributes --------

	/** The agent. */
	protected RBDIAgent	agent;

	/** The registered tools (aid -> eventtypes). */
	protected IndexMap	tools;

	/** The reply handlers (aid -> reply). */
	protected Map		repliers;

	/** The system event filter of the listener. */
	protected SystemEventFilter		filter;

	//-------- constructors --------

	/**
	 * The tool handler.
	 */
	public IntrospectorAdapter(RBDIAgent agent)
	{
		this.agent = agent;
		this.tools = SCollection.createIndexMap();
		this.repliers = SCollection.createHashMap();
	}

	//-------- interface ToolAdapter --------

	/**
	 * Handle a request from a tool agent.
	 * 
	 * @param sender The tool agent that issued the request.
	 * @param request The the request or query.
	 * @param reply A callback interface to the platform allowing the tool
	 *        adapter to send messages to the tool agents.
	 */
	public void handleToolRequest(AgentIdentifier sender, AgentAction action, IToolReply reply)
	{
		System.out.println("Tool request: "+action);
		
		try
		{
			if(action instanceof Register)
			{
				String[] ltypes = ((Register)action).getEventTypes();
				Set stypes = SCollection.createHashSet();
				for(int i = 0; i < ltypes.length; i++)
					stypes.addAll(ISystemEventTypes.Subtypes.getSubtypes(ltypes[i]));
//				 System.out.println("new event types: "+stypes);

				this.tools.put(sender, stypes);
				this.repliers.put(sender, reply);

				updateListener();

				reply.sendInform(new Done(action), false);
			}
			else if(action instanceof Deregister)
			{
				this.tools.removeKey(sender);
				this.repliers.remove(sender);

				updateListener();

				reply.sendInform(new Done(action), false);
				
				// Hack!!! used to remove outstanding reply waits.
				reply.cleanup();
			}
			else if(action instanceof CurrentState)
			{
				String[] types = ((CurrentState)action).getEventTypes();
				List events = agent.getState(types);
				externalizeSystemEvents(events);
				((CurrentState)action).setSystemEvents((SystemEvent[])events
					.toArray(new SystemEvent[events.size()]));
				reply.sendInform(action, false);
				// reply.sendInform(toSLString(events), false);
			}
			else if(action instanceof ToolAction)
			{
				try
				{
					performToolAction((ToolAction)action);
					reply.sendInform(new Done(action), false);
				}
				catch(Exception e)
				{
					// In case of failure send exception text.
					reply.sendFailure(e.toString(), false);
					e.printStackTrace();
				}
			}
		}
		catch(MessageFailureException e)
		{
			// Remove agent on message failure.
			this.tools.removeKey(sender);
			this.repliers.remove(sender);

			updateListener();
		}
	}

	/**
	 * The tool type supported by this adapter (e.g. "tracer").
	 */
	public Class getMessageClass()
	{
		return ToolRequest.class;
	}

	//-------- ISystemEventListener interface --------

	/**
	 *  An event occured in the model or in the subcapabilities.
	 *  @param events The event.
	 */
	public void systemEventsOccurred(SystemEvent[] events)
	{
		//System.out.println("Introspector adapter update: "+jadex.util.SUtil.arrayToString(events));
		
		// Update listener registration, when capabilities get added/removed.
		for(int e=0; e<events.length; e++)
		{
			if(events[e].getType().equals(SystemEvent.CAPABILITY_ADDED))
			{
				((RCapability)events[e].getSource()).addSystemEventListener(this, filter, false, false);
				// Todo: send state of new capability.
			}
			else if(events[e].getType().equals(SystemEvent.CAPABILITY_REMOVED))
			{
				((RCapability)events[e].getSource()).removeSystemEventListener(this);
			}
		}

		for(int i=0; i<tools.size(); i++)
		{
			Collection types = (Collection)tools.get(i);
			// Filter out events relevant for tool.
			ArrayList matched = SCollection.createArrayList();
			for(int e = 0; e < events.length; e++)
			{
				if(types.contains(events[e].getType()))
				{
					matched.add(events[e]);
				}
			}

			// Inform tool, when some event matched.
			if(matched.size() > 0)
			{
				try
				{
					externalizeSystemEvents(matched);
					CurrentState state = new CurrentState(TOOL_INTROSPECTOR);
					state.setSystemEvents((SystemEvent[])matched.toArray(new SystemEvent[matched.size()]));
					IToolReply reply = (IToolReply)repliers.get(tools.getKey(i));
					reply.sendInform(state, true);
//					((IToolReply)repliers.get(tools.getKey(i))).sendInform(
//							toSLString(matched), true);
				}
				catch(TimeoutException e)
				{
					// Tool not reachable -> remove.
//					System.out.println("Introspector failure: " + e);
					repliers.remove(tools.getKey(i));
					tools.remove(i);
					updateListener();
				}
				catch(MessageFailureException e)
				{
					// Tool not reachable -> remove.
//					System.out.println("Introspector failure: " + e);
					repliers.remove(tools.getKey(i));
					tools.remove(i);
					updateListener();
				}
			}
		}
	}

	//-------- helper methods --------

	/**
	 *  Register, modify or deregister the listener
	 *  based on currently active tools.
	 */
	protected void updateListener()
	{
		Set	eventtypes	= new HashSet();
		for(int i=0; i<tools.size(); i++)
			eventtypes.addAll((Set)tools.get(i));
		
		// Register new listener
		if(filter==null && !eventtypes.isEmpty())
		{
			filter	= new SystemEventFilter((String[])eventtypes.toArray(new String[eventtypes.size()]));
			List	caps	= agent.getAllCapabilities();
			for(int i=0; i<caps.size(); i++)
				((RCapability)caps.get(i)).addSystemEventListener(this, filter, false, false);
		}
		
		// Modify existing listener.
		else if(filter!=null && !eventtypes.isEmpty())
		{
			filter.setEventTypes((String[])eventtypes.toArray(new String[eventtypes.size()]));
		}
		
		// Deregister listener.
		else if(filter!=null && eventtypes.isEmpty())
		{
			this.filter	= null;
			List	caps	= agent.getAllCapabilities();
			for(int i=0; i<caps.size(); i++)
				((RCapability)caps.get(i)).removeSystemEventListener(this);
		}
	}
	
	/**
	 * Externalize the given a system events. When the source is encodable the
	 * key value pairs are extracted.
	 */
	public void externalizeSystemEvents(List events)
	{
		for(int i = 0; i < events.size(); i++)
		{
			SystemEvent event = (SystemEvent)events.get(i);
			event = (SystemEvent)event.clone();
			events.set(i, event);
			event.setSource(externalizeValue(event.getSource()));

			if(event.getValue() instanceof List)
			{
				List	values	= (List)event.getValue();
				List	newvalues	= new ArrayList();
				for(int j=0; j<values.size(); j++)
					newvalues.add(externalizeValue(values.get(j)));
				event.setValue(newvalues);
			}
			else
			{
				event.setValue(externalizeValue(event.getValue()));
			}
		}
	}

	/**
	 *  Externalize a value (convert to string representation).
	 */
	public Object	externalizeValue(Object value)
	{
		if(value instanceof IEncodable)
		{
			value	= ((IEncodable)value).getEncodableRepresentation();
		}
		else if(value!=null && !(value instanceof String))
		{
			value	= value.toString();
		}
		return value;
	}

	/**
	 * Convert a list of system events to an SL string. When the source is
	 * encodable the key value pairs are extracted as slots. Slot values of type
	 * java.util.Collection are stored as sequence.
	 * 
	 * @return A string representation in SL.
	 * /
	public static String toSLString(java.util.List events)
	{
		StringBuffer content = new StringBuffer("(");
		for(int i = 0; i < events.size(); i++)
		{
			content.append(" ");
			SystemEvent event = (SystemEvent)events.get(i);
			Object source = event.getSource();
			if(source instanceof IEncodable)
			{
				source = ((IEncodable)source).getEncodableRepresentation();
			}

			Map map = SCollection.createHashMap();
			map.put("type", event.getType());
			map.put("source", source);
			if(event.getValue() != null)
			{
				if(event.getType().equals(SystemEvent.BSFACTS_CHANGED) || event.getType().equals(SystemEvent.ESVALUES_CHANGED))
				{
					// Store element set values as sl-encoded sequence.
					map.put("value", event.getValue());
				}
				else
				{
					// Store value as string.
					map.put("value", "" + event.getValue());
				}
			}
			map.put("index", "" + event.getIndex());

			// Append event to content buffer.
			SUtil.toSLString(map, content);
		}
		content.append(" )");
		return content.toString();
	}*/

	/**
	 * Execute a command.
	 */
	public void performToolAction(ToolAction action)	throws Exception
	{
		if(action instanceof ExecuteCommand)
		{
			String com = ((ExecuteCommand)action).getCommand();
			StringTokenizer stok = new StringTokenizer(com, " ");
			String todo = stok.nextToken();

			/*
			 * if("setDispatcherExecutionMode".equals(todo)) {
			 * ((ISteppable)getAgent().getPlatformAgent().getDispatcherBehaviour())
			 * .setExecutionMode(stok.nextToken()); }
			 */
			/*
			 * else if("doDispatcherStep".equals(todo)) {
			 * ((ISteppable)getAgent().getJadeWrapperAgent().getDispatcherBehaviour()).doStep(); }
			 */
			/*
			 * else if("setDispatcherSteps".equals(todo)) { int steps =
			 * Integer.parseInt(stok.nextToken());
			 * ((ISteppable)getAgent().getPlatformAgent().getDispatcherBehaviour()).setSteps(steps); }
			 */
			/*
			 * else if("setSchedulerExecutionMode".equals(todo)) {
			 * ((ISteppable)getAgent().getPlatformAgent().getSchedulerBehaviour())
			 * .setExecutionMode(stok.nextToken()); }
			 */
			/*
			 * else if("doSchedulerStep".equals(todo)) {
			 * ((ISteppable)getAgent().getJadeWrapperAgent().getSchedulerBehaviour()).doStep(); }
			 */
			/*
			 * else if("setSchedulerSteps".equals(todo)) { int steps =
			 * Integer.parseInt(stok.nextToken());
			 * ((ISteppable)getAgent().getPlatformAgent().getSchedulerBehaviour()).setSteps(steps); }
			 */
			/*
			 * else if("moveEvent".equals(todo)) { REvent event =
			 * getAgent().getEvent(stok.nextToken()); if(event!=null)
			 * getAgent().moveEventListElement(Integer.parseInt(stok.nextToken()),
			 * event); } else if("deleteEvent".equals(todo)) { REvent event =
			 * getAgent().getEvent(stok.nextToken()); if(event!=null)
			 * getAgent().removeEventListElement(event); } else
			 * if("moveReady".equals(todo)) { RPlan rplan =
			 * getAgent().getRPlan(stok.nextToken()); if(rplan!=null)
			 * getAgent().moveReadyListElement(Integer.parseInt(stok.nextToken()),
			 * rplan); } else if("deleteReady".equals(todo)) { RPlan rplan =
			 * getAgent().getRPlan(stok.nextToken()); if(rplan!=null)
			 * getAgent().removeReadyListElement(rplan); }
			 */

			if("setAgendaExecutionMode".equals(todo))
			{
				agent.getInterpreter().setExecutionMode(stok.nextToken());
			}
			/*
			 * else if("doSchedulerStep".equals(todo)) {
			 * ((ISteppable)agent.getPlatformAgent().getAgendaControlBehaviour()).doStep(); }
			 */
			else if("setAgendaSteps".equals(todo))
			{
				int steps = Integer.parseInt(stok.nextToken());
				agent.getInterpreter().setSteps(steps);
			}
			else
			{
				throw new RuntimeException("command not understood: " + todo);
			}
		}
		else if(action instanceof ElementAction)
		{
			ElementAction ea = (ElementAction)action;

			// Get the capability.
			RCapability cap = agent.getCapability(ea.getScope());

			// Single fact belief.
			if(ea.getElementType().equals("RBelief")
					|| ea.getElementType().equals("RBeliefReference")) // Hack
			{
				// Get the belief.
				IRBelief bel = cap.getBeliefbase().getBelief(
						ea.getElementName());

				if(action instanceof ChangeAttribute)
				{
					ChangeAttribute ca = (ChangeAttribute)action;
					// Evaluate the value expression.
					// Problem: has no imports
					Object value = ((IMCapability)cap.getModelElement())
							.getParser().parseExpression(ca.getValue().trim(),
									null) // todo: what parammodels?
							.getValue(cap.getExpressionParameters());

					if(ca.getAttributeName().equals("value"))
					{
						// Set single fact.
						bel.setFact(value);
					}
					// else if(ca.getAttribute().equals("updaterate")) ???
					else
					{
						throw new RuntimeException("Can't change attribute: "
								+ ca.getAttributeName());
					}
				}
				else if(action instanceof PerformAction)
				{
					PerformAction pa = (PerformAction)action;
					if(pa.getActionName().equals("update"))
					{
						bel.getFact();
					}
					else if(pa.getActionName().equals("delete"))
					{
						cap.getBeliefbase().deleteBelief(ea.getElementName());
					}
					else
					{
						throw new RuntimeException("Can't apply action: "
								+ pa.getActionName());
					}
				}
			}

			// Beliefsets (Hack, merge with beliefs???)
			else if(ea.getElementType().equals("RBeliefSet")
					|| ea.getElementType().equals("RBeliefSetReference")) // Hack
																			// ???
			{
				// For beliefset fact, extract name and index.
				String name = ea.getElementName();
				int index = Integer.parseInt(name
						.substring(name.indexOf(".") + 1));
				name = name.substring(0, name.indexOf("."));
				IRBeliefSet bel = cap.getBeliefbase().getBeliefSet(name);

				if(action instanceof ChangeAttribute)
				{
					ChangeAttribute ca = (ChangeAttribute)action;
					// Evaluate the value expression.
					// Problem: has no imports
					Object value = ((IMCapability)cap.getModelElement())
							.getParser().parseExpression(ca.getValue().trim(),
									null) // todo: what parammodels
							.getValue(cap.getExpressionParameters());

					if(ca.getAttributeName().equals("value"))
					{
						// Update fact in beliefset ???.
						// Should beliefbase support indexed access?
						// bel.replaceFact(bel.getFacts()[index], value); //
						// Hack !!!!
						bel.removeFact(bel.getFacts()[index]);
						bel.addFact(value);
					}
					// else if(ca.getAttribute().equals("updaterate")) ???
					else
					{
						throw new RuntimeException("Can't change attribute: "
								+ ca.getAttributeName());
					}
				}
				else if(action instanceof PerformAction)
				{
					PerformAction pa = (PerformAction)action;
					if(pa.getActionName().equals("removeFact"))
					{
						bel.removeFact(bel.getFacts()[index]);
					}
					else if(pa.getActionName().equals("update"))
					{
						bel.getFacts();
					}
					else
					{
						throw new RuntimeException("Can't apply action: "
								+ pa.getActionName());
					}
				}
				else
				{
					throw new RuntimeException("Can't apply action: "
							+ ea.getClass().getName());
				}
			}

			// Beliefsetcontainers
			else if(ea.getElementType().equals("RBeliefSetContainer")
					|| ea.getElementType().equals(
							"RBeliefSetReferenceContainer")) // Hack ???
			{
				// For beliefset extract name.
				String name = ea.getElementName();
				IRBeliefSet bel = cap.getBeliefbase().getBeliefSet(name);

				if(action instanceof PerformAction)
				{
					PerformAction pa = (PerformAction)action;
					if(pa.getActionName().equals("addFact"))
					{
						// doesnt work !?
						// bel.addFact(null);
					}
					else if(pa.getActionName().equals("removeFacts"))
					{
						bel.removeFacts();
					}
					else if(pa.getActionName().equals("delete"))
					{
						cap.getBeliefbase().deleteBeliefSet(ea.getElementName());
					}
					else
					{
						throw new RuntimeException("Can't apply action: "
								+ pa.getActionName());
					}
				}
			}

			// Plan.
			else if(ea.getElementType().equals("RPlan"))
			{
				RPlan plan = cap.getPlanbase().getPlan(ea.getElementName());

				if(action instanceof PerformAction)
				{
					PerformAction pa = (PerformAction)action;
					if(pa.getActionName().equals("terminate"))
					{
						// Instead of terminating the plan directly
						// abort the root goal to cleanup everything.
						plan.getRootGoal().abort(false);
					}
					else
					{
						throw new RuntimeException("Can't apply action: "
								+ pa.getActionName());
					}
				}
				else
				{
					throw new RuntimeException("Can't apply action: "
							+ ea.getClass().getName());
				}
			}

			// Goal.
			else if(ea.getElementType().endsWith("Goal")
				|| ea.getElementType().endsWith("GoalReference"))
			{
				// Hack??? What about process goals?
				IRGoal goal = cap.getGoalbase().getGoal(ea.getElementName());

				if(action instanceof PerformAction)
				{
					PerformAction pa = (PerformAction)action;
					if(pa.getActionName().equals("drop"))
					{
						goal.drop();
					}
					else if(pa.getActionName().equals("suspend"))
					{
						goal.suspend();
					}
					else if(pa.getActionName().equals("option"))
					{
						goal.option();
					}
					else if(pa.getActionName().equals("activate"))
					{
						// Optionize goal, if suspended.
						if(IGoal.LIFECYCLESTATE_SUSPENDED.equals(goal.getLifecycleState()))
							goal.option();
						goal.activate();
					}
					else
					{
						throw new RuntimeException("Can't apply action: "
								+ pa.getActionName());
					}
				}
				else
				{
					throw new RuntimeException("Can't apply action: "
							+ ea.getClass().getName());
				}
			}

			else
			{
				throw new RuntimeException("Can't apply actions to element: "
						+ ea.getElementType());
			}
		}
		else
		{
			throw new RuntimeException("Unknown tool action: " + action);
		}
	}
}
