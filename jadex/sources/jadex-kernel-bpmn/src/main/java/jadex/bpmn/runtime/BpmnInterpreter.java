package jadex.bpmn.runtime;

import jadex.bpmn.model.MActivity;
import jadex.bpmn.model.MBpmnModel;
import jadex.bpmn.model.MParameter;
import jadex.bpmn.model.MPool;
import jadex.bpmn.model.MSequenceEdge;
import jadex.bpmn.runtime.handler.DefaultActivityHandler;
import jadex.bpmn.runtime.handler.DefaultStepHandler;
import jadex.bpmn.runtime.handler.EventEndErrorActivityHandler;
import jadex.bpmn.runtime.handler.EventIntermediateErrorActivityHandler;
import jadex.bpmn.runtime.handler.EventIntermediateMessageActivityHandler;
import jadex.bpmn.runtime.handler.EventIntermediateMultipleActivityHandler;
import jadex.bpmn.runtime.handler.EventIntermediateNotificationHandler;
import jadex.bpmn.runtime.handler.EventIntermediateTimerActivityHandler;
import jadex.bpmn.runtime.handler.EventMultipleStepHandler;
import jadex.bpmn.runtime.handler.GatewayParallelActivityHandler;
import jadex.bpmn.runtime.handler.GatewayXORActivityHandler;
import jadex.bpmn.runtime.handler.SubProcessActivityHandler;
import jadex.bpmn.runtime.handler.TaskActivityHandler;
import jadex.bpmn.runtime.task.ExecuteStepTask;
import jadex.bridge.ComponentResultListener;
import jadex.bridge.ComponentServiceContainer;
import jadex.bridge.ComponentTerminatedException;
import jadex.bridge.IArgument;
import jadex.bridge.IComponentAdapter;
import jadex.bridge.IComponentAdapterFactory;
import jadex.bridge.IComponentDescription;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentInstance;
import jadex.bridge.IComponentListener;
import jadex.bridge.IComponentManagementService;
import jadex.bridge.IComponentStep;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IInternalAccess;
import jadex.bridge.IMessageAdapter;
import jadex.bridge.IMessageService;
import jadex.bridge.IModelInfo;
import jadex.bridge.MessageType;
import jadex.commons.ChangeEvent;
import jadex.commons.Future;
import jadex.commons.IChangeListener;
import jadex.commons.IFilter;
import jadex.commons.IFuture;
import jadex.commons.concurrent.DefaultResultListener;
import jadex.commons.concurrent.DelegationResultListener;
import jadex.commons.concurrent.IResultListener;
import jadex.commons.service.CacheServiceContainer;
import jadex.commons.service.IServiceContainer;
import jadex.commons.service.IServiceProvider;
import jadex.commons.service.SServiceProvider;
import jadex.commons.service.clock.IClockService;
import jadex.javaparser.IParsedExpression;
import jadex.javaparser.IValueFetcher;
import jadex.javaparser.SJavaParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *  The micro agent interpreter is the connection between the agent platform 
 *  and a user-written micro agent. 
 */
public class BpmnInterpreter implements IComponentInstance, IInternalAccess
{	
	//-------- static part --------

	/** The activity execution handlers (activity type -> handler). */
	public static final Map DEFAULT_ACTIVITY_HANDLERS;
	
	/** The step execution handlers (activity type -> handler). */
	public static final Map DEFAULT_STEP_HANDLERS;

	/** The flag for all pools. */
	public static final String ALL = "All";
	
	static
	{
		Map stephandlers = new HashMap();
		
		stephandlers.put(IStepHandler.STEP_HANDLER, new DefaultStepHandler());
		stephandlers.put(MBpmnModel.EVENT_INTERMEDIATE_MULTIPLE, new EventMultipleStepHandler());
		
		DEFAULT_STEP_HANDLERS = Collections.unmodifiableMap(stephandlers);
		
		Map activityhandlers = new HashMap();
		
		// Task/Subprocess handler.
		activityhandlers.put(MBpmnModel.TASK, new TaskActivityHandler());
		activityhandlers.put(MBpmnModel.SUBPROCESS, new SubProcessActivityHandler());
	
		// Gateway handler.
		activityhandlers.put(MBpmnModel.GATEWAY_PARALLEL, new GatewayParallelActivityHandler());
		activityhandlers.put(MBpmnModel.GATEWAY_DATABASED_EXCLUSIVE, new GatewayXORActivityHandler());
	
		// Initial events.
		// Options: empty, message, rule, timer, signal, multi, link
		// Missing: link 
		// Note: non-empty start events are currently only supported in subworkflows
		// It is currently not possible to start a top-level workflow using the other event types,
		// i.e. the creation of a workflow is not supported. 
		activityhandlers.put(MBpmnModel.EVENT_START_EMPTY, new DefaultActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_START_TIMER, new EventIntermediateTimerActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_START_MESSAGE, new EventIntermediateMessageActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_START_MULTIPLE, new EventIntermediateMultipleActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_START_RULE, new EventIntermediateNotificationHandler());
		activityhandlers.put(MBpmnModel.EVENT_START_SIGNAL, new EventIntermediateNotificationHandler());
			
		// Intermediate events.
		// Options: empty, message, rule, timer, error, signal, multi, link, compensation, cancel
		// Missing: link, compensation, cancel
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_EMPTY, new DefaultActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_MESSAGE, new EventIntermediateMessageActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_RULE, new EventIntermediateNotificationHandler());
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_TIMER, new EventIntermediateTimerActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_ERROR, new EventIntermediateErrorActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_MULTIPLE, new EventIntermediateMultipleActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_SIGNAL, new EventIntermediateNotificationHandler());
//		defhandlers.put(MBpmnModel.EVENT_INTERMEDIATE_RULE, new UserInteractionActivityHandler());
		
		// End events.
		// Options: empty, message, error, compensation, terminate, signal, multi, cancel, link
		// Missing: link, compensation, cancel, terminate, signal, multi
		activityhandlers.put(MBpmnModel.EVENT_END_EMPTY, new DefaultActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_END_ERROR, new EventEndErrorActivityHandler());
		activityhandlers.put(MBpmnModel.EVENT_END_MESSAGE, new EventIntermediateMessageActivityHandler());

		DEFAULT_ACTIVITY_HANDLERS = Collections.unmodifiableMap(activityhandlers);
	}
	
	//-------- attributes --------
	
	/** The platform adapter for the agent. */
	protected IComponentAdapter	adapter;
	
	/** The micro agent model. */
	protected MBpmnModel model;
	
	/** The configuration. */
	protected String config;
	
	/** The configuration. */
	protected String pool;
	
	/** The configuration. */
	protected String lane;
	
	/** The parent. */
	protected IExternalAccess	parent;
	
	/** The activity handlers. */
	protected Map activityhandlers;
	
	/** The step handlers. */
	protected Map stephandlers;

	/** The global value fetcher. */
	protected IValueFetcher	fetcher;

	/** The thread context. */
	protected ThreadContext	context;
	
	/** The execution history. */
	protected List history;
	
	/** The change listeners. */
	protected List listeners;
	
	/** The step number. */
	protected int stepnumber;
	
	/** The context variables. */
	protected Map variables;
	
	/** The finishing flag marker. */
	protected boolean finishing;
	
	/** The messages waitqueue. */
	protected List messages;
	
	/** The service container. */
	protected IServiceContainer container;
	
	/** The flag if is inited. */
	protected boolean initedflag;
	
	/** The inited future. */
	protected Future inited;
	
	/** Listeners for activities. */
	protected List activitylisteners;
	
	/** The component listeners. */
	protected List componentlisteners;

	
	//-------- constructors --------
	
	// todo: 
	/**
	 *  Create a new bpmn process.
	 *  @param adapter The adapter.
	 */
	// Constructor for bdi plan interpreter
	public BpmnInterpreter(IComponentAdapter adapter, MBpmnModel model, Map arguments, 
		String config, final IExternalAccess parent, Map activityhandlers, Map stephandlers, 
		IValueFetcher fetcher, IComponentManagementService cms, IClockService cs, IMessageService ms,
		IServiceContainer container)
	{
		this.adapter = adapter;
		construct(model, arguments, config, parent, activityhandlers, stephandlers, fetcher);
		variables.put("$cms", cms);
		variables.put("$clock", cs);
		variables.put("$msgservice", ms);
		
		// Assign container
		this.container = container;
		
		initContextVariables();
		
		// Create initial thread(s). 
		List	startevents	= model.getStartActivities();
		for(int i=0; startevents!=null && i<startevents.size(); i++)
		{
			context.addThread(new ProcessThread((MActivity)startevents.get(i), context, BpmnInterpreter.this));
		}
		initedflag = true;	// No further init for BDI plan.
	}	
		
	/**
	 *  Create a new bpmn process.
	 *  @param adapter The adapter.
	 */
	// Constructor for self-contained bpmn components
	public BpmnInterpreter(IComponentDescription desc, IComponentAdapterFactory factory, MBpmnModel model, Map arguments, 
		String config, final IExternalAccess parent, Map activityhandlers, Map stephandlers, 
		IValueFetcher fetcher, Future inited)
	{
		this.adapter = factory.createComponentAdapter(desc, model.getModelInfo(), this, parent);
		this.inited = inited;
		this.variables	= new HashMap();
		construct(model, arguments, config, parent, activityhandlers, stephandlers, fetcher);
	}
	
	/**
	 *  Init method holds constructor code for both implementations.
	 */
	protected void construct(final MBpmnModel model, Map arguments, String config, 
		final IExternalAccess parent, Map activityhandlers, Map stephandlers, 
		IValueFetcher fetcher)
	{
		this.model = model;
		this.config = config;
		
		// Extract pool/lane from config.
		if(config==null || ALL.equals(config))
		{
			this.pool	= null;
			this.lane	= null;
		}
		else
		{
			int idx	= config.indexOf('.');
			if(idx==-1)
			{
				this.pool	= config;
				this.lane	= null;
			}
			else
			{
				this.pool	= config.substring(0, idx);
				this.lane	= config.substring(idx+1);
			}
		}
		
		this.parent	= parent;
		this.activityhandlers = activityhandlers!=null? activityhandlers: DEFAULT_ACTIVITY_HANDLERS;
		this.stephandlers = stephandlers!=null? stephandlers: DEFAULT_STEP_HANDLERS;
		this.fetcher = fetcher!=null? fetcher: new BpmnInstanceFetcher(this, fetcher);
		this.context = new ThreadContext(model);
		this.messages = new ArrayList();
		this.variables	= new HashMap();

		// Init the arguments with default values.
		IArgument[] args = model.getModelInfo().getArguments();
		for(int i=0; i<args.length; i++)
		{
			if(arguments!=null && arguments.containsKey(args[i].getName()))
			{
				this.variables.put(args[i].getName(), arguments.get(args[i].getName()));
			}
			else if(args[i].getDefaultValue(config)!=null)
			{
				this.variables.put(args[i].getName(), args[i].getDefaultValue(config));
			}
		}		
	}
	
	//-------- IComponentInstance interface --------
	
	/**
	 *  Can be called on the component thread only.
	 * 
	 *  Main method to perform component execution.
	 *  Whenever this method is called, the component performs
	 *  one of its scheduled actions.
	 *  The platform can provide different execution models for components
	 *  (e.g. thread based, or synchronous).
	 *  To avoid idle waiting, the return value can be checked.
	 *  The platform guarantees that executeStep() will not be called in parallel. 
	 *  @return True, when there are more actions waiting to be executed. 
	 */
	public boolean executeStep()
	{
		boolean ret = false;
		
		if(!initedflag)
		{
			initedflag = true;
			executeInitStep1();
		}
		else if(inited.isDone())	// Todo: do we need this?
		{
			try
			{
				if(!isFinished(pool, lane) && isReady(pool, lane))
					executeStep(pool, lane);
				
	//			System.out.println("After step: "+this.getComponentAdapter().getComponentIdentifier().getName()+" "+isFinished(pool, lane));
				if(!finishing && isFinished(pool, lane))
				{
					finishing = true;
					((IComponentManagementService)variables.get("$cms")).destroyComponent(adapter.getComponentIdentifier());
					
	//				SServiceProvider.getService(getServiceProvider(), IComponentManagementService.class)
	//					.addResultListener(createResultListener(new DefaultResultListener()
	//				{
	//					public void resultAvailable(Object source, Object result)
	//					{
	//						((IComponentManagementService)result).destroyComponent(adapter.getComponentIdentifier());
	//					}
	//				}));
				}
				
	//			System.out.println("Process wants: "+this.getComponentAdapter().getComponentIdentifier().getLocalName()+" "+!isFinished(null, null)+" "+isReady(null, null));
				
				ret = !isFinished(pool, lane) && isReady(pool, lane);
			}
			catch(ComponentTerminatedException ate)
			{
				// Todo: fix kernel bug.
				ate.printStackTrace();
			}
		}
		
		return ret;
	}

	/**
	 *  Execute the init step 1.
	 */
	protected void executeInitStep1()
	{
		// Fetch and cache services, then init service container.
		// Note: It is very tricky to call createResultListener() in the constructor as this
		// indirectly calls adapter.wakeup() but the component shouldn't run automatically!
		
		// todo: remove this hack of caching services
		final boolean services[] = new boolean[3];
		SServiceProvider.getServiceUpwards(getServiceProvider(), IComponentManagementService.class)
			.addResultListener(createResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				variables.put("$cms", result);
				boolean init2;
				synchronized(services)
				{
					services[0]	= true;
					init2 = services[0] && services[1] && services[2];
				}
				if(init2)
				{
					executeInitStep2();
				}
			}
			public void exceptionOccurred(Object source, Exception exception)
			{
				inited.setException(exception);
			}
		}));
		SServiceProvider.getService(getServiceProvider(), IClockService.class)
			.addResultListener(createResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				variables.put("$clock", result);
				boolean init2;
				synchronized(services)
				{
					services[1]	= true;
					init2 = services[0] && services[1] && services[2];
				}
				if(init2)
				{
					executeInitStep2();
				}
			}
			public void exceptionOccurred(Object source, Exception exception)
			{
				inited.setException(exception);
			}
		}));
		SServiceProvider.getService(getServiceProvider(), IMessageService.class)
			.addResultListener(createResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				variables.put("$msgservice", result);
				boolean init2;
				synchronized(services)
				{
					services[2]	= true;
					init2 = services[0] && services[1] && services[2];
				}
				if(init2)
				{
					executeInitStep2();
				}
			} 
			public void exceptionOccurred(Object source, Exception exception)
			{
				inited.setException(exception);
			}
		}));
	}
	
	/**
	 *  Execute the init step 2.
	 */
	protected void executeInitStep2()
	{
		// Initialize context variables.
		variables.put("$interpreter", this);
		
		if(getModel().getName().indexOf("AddTargetPlan")!=-1)
		{
			System.out.println("debug sdlkhfyg");
		}
		
		initContextVariables();

		// Start the container and notify cms when start has finished.		
		getServiceContainer().start().addResultListener(createResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				// Create initial thread(s). 
				List	startevents	= model.getStartActivities();
				for(int i=0; startevents!=null && i<startevents.size(); i++)
				{
					context.addThread(new ProcessThread((MActivity)startevents.get(i), context, BpmnInterpreter.this));
				}
				
				// Notify cms that init is finished.
				inited.setResult(new Object[]{BpmnInterpreter.this, adapter});
			}
			
			public void exceptionOccurred(Object source, Exception exception)
			{
				inited.setException(exception);
			}
		}));
	}

	protected void initContextVariables()
	{
		Set	vars	= model.getContextVariables();
		for(Iterator it=vars.iterator(); it.hasNext(); )
		{
			String	name	= (String)it.next();
			if(!variables.containsKey(name))	// Don't overwrite arguments.
			{
				Object	value	= null;
				IParsedExpression	exp	= model.getContextVariableExpression(name);
				if(exp!=null)
				{
					try
					{
						value	= exp.getValue(this.fetcher);
					}
					catch(RuntimeException e)
					{
						throw new RuntimeException("Error parsing context variable: "+this+", "+name+", "+exp, e);
					}
				}
				variables.put(name, value);
			}
		}
	}
	
	/**
	 *  Can be called concurrently (also during executeAction()).
	 *  
	 *  Inform the agent that a message has arrived.
	 *  Can be called concurrently (also during executeAction()).
	 *  @param message The message that arrived.
	 */
	public void messageArrived(final IMessageAdapter message)
	{
		adapter.invokeLater(new Runnable()
		{
			public void run()
			{
				// Iterate through process threads and dispatch message to first
				// waiting and fitting one (filter check).
				boolean processed = false;
				for(Iterator it=context.getAllThreads().iterator(); it.hasNext() && !processed; )
				{
					ProcessThread pt = (ProcessThread)it.next();
					if(pt.isWaiting())
					{
						IFilter filter = pt.getWaitFilter();
						if(filter!=null && filter.filter(message))
						{
							BpmnInterpreter.this.notify(pt.getActivity(), pt, message);
//							((DefaultActivityHandler)getActivityHandler(pt.getActivity())).notify(pt.getActivity(), BpmnInterpreter.this, pt, message);
							processed = true;
						}
					}
				}
				
				if(!processed)
				{
					messages.add(message);
//					System.out.println("Dispatched to waitqueue: "+message);
				}
			}
		});
	}

	/**
	 *  Can be called concurrently (also during executeAction()).
	 *   
	 *  Request agent to kill itself.
	 *  The agent might perform arbitrary cleanup activities during which executeAction()
	 *  will still be called as usual.
	 *  Can be called concurrently (also during executeAction()).
	 *  @param listener	When cleanup of the agent is finished, the listener must be notified.
	 */
	public IFuture cleanupComponent()
	{
		final Future ret = new Future();
		// Todo: cleanup required???
		
		if(componentlisteners!=null)
		{
			for(int i=0; i<componentlisteners.size(); i++)
			{
				IComponentListener lis = (IComponentListener)componentlisteners.get(i);
				lis.componentTerminating(new ChangeEvent(adapter.getComponentIdentifier()));
			}
		}
		
		adapter.invokeLater(new Runnable()
		{
			public void run()
			{	
				// Call cancel on all running threads.
				for(Iterator it= getThreadContext().getAllThreads().iterator(); it.hasNext(); )
				{
					ProcessThread pt = (ProcessThread)it.next();
					getActivityHandler(pt.getActivity()).cancel(pt.getActivity(), BpmnInterpreter.this, pt);
//					System.out.println("Cancelling: "+pt.getActivity()+" "+pt.getId());
				}
				ret.setResult(adapter.getComponentIdentifier());
				
				if(componentlisteners!=null)
				{
					for(int i=0; i<componentlisteners.size(); i++)
					{
						IComponentListener lis = (IComponentListener)componentlisteners.get(i);
						lis.componentTerminated(new ChangeEvent(adapter.getComponentIdentifier()));
					}
				}
			}
		});
		
		return ret;
	}
	
	/**
	 *  Kill the component.
	 */
	public IFuture killComponent()
	{
		final Future ret = new Future();
		
		SServiceProvider.getService(getServiceProvider(), IComponentManagementService.class)
			.addResultListener(createResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				((IComponentManagementService)result).destroyComponent(adapter.getComponentIdentifier())
					.addResultListener(new DelegationResultListener(ret));
			}
		}));
		
		return ret;
	}
	
	/**
	 *  Can be called concurrently (also during executeAction()).
	 * 
	 *  Get the external access for this agent.
	 *  The specific external access interface is kernel specific
	 *  and has to be casted to its corresponding incarnation.
	 *  @param listener	External access is delivered via result listener.
	 */
	public IExternalAccess getExternalAccess()
	{
		return new ExternalAccess(BpmnInterpreter.this);
		
//		final Future ret = new Future();
//		
//		adapter.invokeLater(new Runnable()
//		{
//			public void run()
//			{
//				ret.setResult(new ExternalAccess(BpmnInterpreter.this));
//			}
//		});
//		
//		return ret;
	}
	
	/**
	 *  Get the class loader of the agent.
	 *  The agent class loader is required to avoid incompatible class issues,
	 *  when changing the platform class loader while agents are running. 
	 *  This may occur e.g. when decoding messages and instantiating parameter values.
	 *  @return	The agent class loader. 
	 */
	public ClassLoader getClassLoader()
	{
		return model.getModelInfo().getClassLoader();
	}
	
	/**
	 *  Get the results of the component (considering it as a functionality).
	 *  @return The results map (name -> value). 
	 */
	public Map getResults()
	{
		IArgument[] results = getModel().getResults();
		Map res = new HashMap();
		
		for(int i=0; i<results.length; i++)
		{
			String resname = results[i].getName();
			if(variables.containsKey(resname))
			{
				res.put(resname, variables.get(resname));
			}
		}
		return res;
	}
	
	/**
	 *  Test if the component's execution is currently at one of the
	 *  given breakpoints. If yes, the component will be suspended by
	 *  the platform.
	 *  @param breakpoints	An array of breakpoints.
	 *  @return True, when some breakpoint is triggered.
	 */
	public boolean isAtBreakpoint(String[] breakpoints)
	{
		boolean	isatbreakpoint	= false;
		Set	bps	= new HashSet(Arrays.asList(breakpoints));	// Todo: cache set across invocations for speed?
		for(Iterator it=context.getAllThreads().iterator(); !isatbreakpoint && it.hasNext(); )
		{
			ProcessThread	pt	= (ProcessThread)it.next();
			isatbreakpoint	= bps.contains(pt.getActivity().getBreakpointId());
		}
		return isatbreakpoint;
	}
	
//	/**
//	 *  Check if the value of a result is set.
//	 *  @param name	The result name. 
//	 *  @return	True, if the result is set to some value. 
//	 */
//	public boolean hasResultValue(String name)
//	{
//		return results.containsKey(name);
//	}
//
//	/**
//	 *  Get the value of a result.
//	 *  @param name	The result name. 
//	 *  @return	The result value. 
//	 */
//	public Object getResultValue(String name)
//	{
//		return results.get(name);
//	}
//	
//	/**
//	 *  Set the value of a result.
//	 *  @param name	The result name. 
//	 *  @param value The result value. 
//	 */
//	public void	setResultValue(String name, Object value)
//	{
//		results.put(name, value);
//	}
	
	//-------- helpers --------
	
	/**
	 *  Add an action from external thread.
	 *  The contract of this method is as follows:
	 *  The agent ensures the execution of the external action, otherwise
	 *  the method will throw a agent terminated sexception.
	 *  @param action The action.
	 * /
	public void invokeLater(Runnable action)
	{
		synchronized(ext_entries)
		{
			if(ext_forbidden)
				throw new ComponentTerminatedException("External actions cannot be accepted " +
					"due to terminated agent state: "+this);
			{
				ext_entries.add(action);
			}
		}
		adapter.wakeup();
	}*/
	
	/**
	 *  Invoke some code with agent behaviour synchronized on the agent.
	 *  @param code The code to execute.
	 *  The method will block the externally calling thread until the
	 *  action has been executed on the agent thread.
	 *  If the agent does not accept external actions (because of termination)
	 *  the method will directly fail with a runtime exception.
	 *  Note: 1.4 compliant code.
	 *  Problem: Deadlocks cannot be detected and no exception is thrown.
	 * /
	public void invokeSynchronized(final Runnable code)
	{
		if(isExternalThread())
		{
//			System.err.println("Unsynchronized internal thread.");
//			Thread.dumpStack();

			final boolean[] notified = new boolean[1];
			final RuntimeException[] exception = new RuntimeException[1];
			
			// Add external will throw exception if action execution cannot be done.
//			System.err.println("invokeSynchonized("+code+"): adding");
			adapter.invokeLater(new Runnable()
			{
				public void run()
				{
					try
					{
						code.run();
					}
					catch(RuntimeException e)
					{
						exception[0]	= e;
					}
					
					synchronized(notified)
					{
						notified.notify();
						notified[0] = true;
					}
				}
				
				public String	toString()
				{
					return code.toString();
				}
			});
			
			try
			{
//				System.err.println("invokeSynchonized("+code+"): waiting");
				synchronized(notified)
				{
					if(!notified[0])
					{
						notified.wait();
					}
				}
//				System.err.println("invokeSynchonized("+code+"): returned");
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			if(exception[0]!=null)
				throw exception[0];
		}
		else
		{
			System.err.println("Method called from internal agent thread.");
			Thread.dumpStack();
			code.run();
		}
	}*/
	
	/**
	 *  Check if the external thread is accessing.
	 *  @return True, if access is ok.
	 */ 
	public boolean isExternalThread()
	{
		return adapter.isExternalThread();
	}
	
	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger()
	{
		return adapter.getLogger();
	}

	/**
	 *  Get the agent adapter.
	 *  @return The agent adapter.
	 */
	public IComponentAdapter getComponentAdapter()
	{
		return adapter;
	}

	/**
	 *  Get the agent model.
	 *  @return The model.
	 */
	public IModelInfo getModel()
	{
		return model.getModelInfo();
	}
	
	/**
	 *  Get the id of the component.
	 *  @return	The component id.
	 */
	public IComponentIdentifier	getComponentIdentifier()
	{
		return adapter.getComponentIdentifier();
	}
	
	/**
	 *  Get the parent component.
	 *  @return The parent component.
	 */
	public IExternalAccess getParent()
	{
		return parent;
	}
	
	/** 
	 *  Get the service provider.
	 */
	public IServiceProvider getServiceProvider()
	{
		return getServiceContainer();
	}
	
	/**
	 *  Create the service container.
	 *  @return The service container.
	 */
	public IServiceContainer getServiceContainer()
	{
		if(container==null)
		{
			// todo: support container customization via bpmn file
			container = new CacheServiceContainer(new ComponentServiceContainer(getComponentAdapter()), 25, 1*30*1000); // 30 secs cache expire
		}
		return container;
	}

	/**
	 *  Create a result listener which is called on agent thread.
	 *  @param The original listener to be called.
	 *  @return The listener.
	 * /
	public IResultListener createResultListener(IResultListener listener)
	{
		throw new UnsupportedOperationException();
//		return new MicroListener(listener);
	}*/
	
	/**
	 *  Get the model of the BPMN process instance.
	 *  @return The model.
	 */
	public MBpmnModel	getModelElement()
	{
		return (MBpmnModel)context.getModelElement();
	}
	
	/**
	 *  Get the thread context.
	 *  @return The thread context.
	 */
	public ThreadContext getThreadContext()
	{
		return context;
	}
	
	/**
	 *  Check, if the process has terminated.
	 *  @param pool	The pool to be executed or null for any.
	 *  @param lane	The lane to be executed or null for any. Nested lanes may be addressed by dot-notation, e.g. 'OuterLane.InnerLane'.
	 *  @return True, when the process instance is finished with regards to the specified pool/lane. When both pool and lane are null, true is returned only when all pools/lanes are finished.
	 */
	public boolean isFinished(String pool, String lane)
	{
		return context.isFinished(pool, lane);
	}

	/**
	 *  Execute one step of the process.
	 *  @param pool	The pool to be executed or null for any.
	 *  @param lane	The lane to be executed or null for any. Nested lanes may be addressed by dot-notation, e.g. 'OuterLane.InnerLane'.
	 */
	public void executeStep(String pool, String lane)
	{
		if(isFinished(pool, lane))
			throw new UnsupportedOperationException("Cannot execute a finished process: "+this);
		
		if(!isReady(pool, lane))
			throw new UnsupportedOperationException("Cannot execute a process with only waiting threads: "+this);
		
		ProcessThread	thread	= context.getExecutableThread(pool, lane);
		
		// Thread may be null when external entry has not changed waiting state of any active plan. 
		if(thread!=null)
		{
			// Update parameters based on edge inscriptions and initial values.
			thread.updateParametersBeforeStep(this);
			
			// Find handler and execute activity.
			IActivityHandler handler = (IActivityHandler)activityhandlers.get(thread.getActivity().getActivityType());
			if(handler==null)
				throw new UnsupportedOperationException("No handler for activity: "+thread);
			if(history!=null)
				history.add(new HistoryEntry(stepnumber++, thread.getId(), thread.getActivity()));

			if(thread.getLastEdge()!=null && thread.getLastEdge().getSource()!=null)
				fireEndActivity(thread.getId(), thread.getLastEdge().getSource());
			
			fireStartActivity(thread.getId(), thread.getActivity());

//			System.out.println("Step: "+this.getComponentAdapter().getComponentIdentifier().getName()+" "+thread.getActivity()+" "+thread);
			MActivity act = thread.getActivity();
			handler.execute(act, this, thread);
	
			// Moved to StepHandler
//			thread.updateParametersAfterStep(act, this);
			
			// Check if thread now waits for a message and there is at least one in the message queue.
			// Todo: check if thread directly or indirectly (multiple events!) waits for a message event before checking waitqueue
			if(thread.isWaiting() && messages.size()>0 /*&& MBpmnModel.EVENT_INTERMEDIATE_MESSAGE.equals(thread.getActivity().getActivityType()) 
				&& (thread.getPropertyValue(EventIntermediateMessageActivityHandler.PROPERTY_MODE)==null 
					|| EventIntermediateMessageActivityHandler.MODE_RECEIVE.equals(thread.getPropertyValue(EventIntermediateMessageActivityHandler.PROPERTY_MODE)))*/)
			{
				boolean processed = false;
				for(int i=0; i<messages.size() && !processed; i++)
				{
					Object message = messages.get(i);
					IFilter filter = thread.getWaitFilter();
					if(filter!=null && filter.filter(message))
					{
						notify(thread.getActivity(), thread, message);
						processed = true;
						messages.remove(i);
//						System.out.println("Dispatched from waitqueue: "+messages.size()+" "+message);
					}
				}
			}
			
			notifyListeners(new ChangeEvent(this, "step_executed"));
		}
	}

	/**
	 *  Check if the process is ready, i.e. if at least one process thread can currently execute a step.
	 *  @param pool	The pool to be executed or null for any.
	 *  @param lane	The lane to be executed or null for any. Nested lanes may be addressed by dot-notation, e.g. 'OuterLane.InnerLane'.
	 */
	public boolean	isReady(String pool, String lane)
	{
		boolean	ready;
//		// Todo: consider only external entries belonging to pool/lane
//		synchronized(ext_entries)
//		{
//			ready	= !ext_entries.isEmpty();
//		}
		ready	= context.getExecutableThread(pool, lane)!=null;
		return ready;
	}
	
	/**
	 *  Method that should be called, when an activity is finished and the following activity should be scheduled.
	 *  Can safely be called from external threads.
	 *  @param activity	The timing event activity.
	 *  @param instance	The process instance.
	 *  @param thread	The process thread.
	 *  @param event	The event that has occurred, if any.
	 */
	public void	notify(final MActivity activity, final ProcessThread thread, final Object event)
	{
		if(isExternalThread())
		{
			getComponentAdapter().invokeLater(new Runnable()
			{
				public void run()
				{
					if(isCurrentActivity(activity, thread))
					{
//						System.out.println("Notify: "+activity+" "+thread+" "+event);
						getStepHandler(activity).step(activity, BpmnInterpreter.this, thread, event);
						thread.setNonWaiting();
						notifyListeners(new ChangeEvent(this, "notify"));
					}
					else
					{
						System.out.println("Nop, due to outdated notify: "+thread+" "+activity);
					}
				}
			});
		}
		else
		{
			if(isCurrentActivity(activity, thread))
			{
//				System.out.println("Notify: "+activity+" "+thread+" "+event);
				getStepHandler(activity).step(activity, BpmnInterpreter.this, thread, event);
				thread.setNonWaiting();
				notifyListeners(new ChangeEvent(this, "notify"));
			}
			else
			{
				System.out.println("Nop, due to outdated notify: "+thread+" "+activity);
			}
		}
	}
	
	/**
	 *  Test if the notification is relevant for the current thread.
	 *  The normal test is if thread.getActivity().equals(activity).
	 *  This method must handle the additional cases that the current
	 *  activity of the thread is a multiple event activity or
	 *  when the activity is a subprocess with an attached timer event.
	 *  In this case the notification could be for one of the child/attached events. 
	 */
	protected boolean isCurrentActivity(final MActivity activity, final ProcessThread thread)
	{
		boolean ret = thread.getActivity().equals(activity);
		if(!ret && MBpmnModel.EVENT_INTERMEDIATE_MULTIPLE.equals(thread.getActivity().getActivityType()))
		{
			List outedges = thread.getActivity().getOutgoingSequenceEdges();
			for(int i=0; i<outedges.size() && !ret; i++)
			{
				MSequenceEdge edge = (MSequenceEdge)outedges.get(i);
				ret = edge.getTarget().equals(activity);
			}
		}
		if(!ret && MBpmnModel.SUBPROCESS.equals(thread.getActivity().getActivityType()))
		{
			List handlers = thread.getActivity().getEventHandlers();
			for(int i=0; !ret && handlers!=null && i<handlers.size(); i++)
			{
				MActivity	handler	= (MActivity)handlers.get(i);
				ret	= activity.equals(handler) && handler.getActivityType().equals("EventIntermediateTimer");
			}
		}
		return ret;
		
	}
	
	/**
	 *  Add an external entry to be invoked during the next executeStep.
	 *  This method may be called from external threads.
	 *  @param code	The external code. 
	 */
//	public void	invokeLater(Runnable code)
//	{
//		synchronized(extentries)
//		{
//			extentries.add(code);
//		}
//		if(adapter!=null)
//			adapter.wakeUp();
//	}
	
	/**
	 *  Get the activity handler for an activity.
	 *  @param actvity The activity.
	 *  @return The activity handler.
	 */
	public IActivityHandler getActivityHandler(MActivity activity)
	{
		return (IActivityHandler)activityhandlers.get(activity.getActivityType());
	}
	
	/**
	 *  Get the step handler.
	 *  @return The step handler.
	 */
	public IStepHandler getStepHandler(MActivity activity)
	{
		IStepHandler ret = (IStepHandler)stephandlers.get(activity.getActivityType());
		return ret!=null? ret: (IStepHandler)stephandlers.get(IStepHandler.STEP_HANDLER);
	}

	/**
	 *  Get the global value fetcher.
	 *  @return The value fetcher (if any).
	 */
	public IValueFetcher getValueFetcher()
	{
		return this.fetcher;
	}
	
	/**
	 *  Get the history mode.
	 */
	public boolean isHistoryEnabled()
	{
		return history!=null;
	}
	
	/**
	 *  Get the history.
	 *  @return The history.
	 */
	public List getHistory()
	{
		return this.history;
	}

	/**
	 *  Set the history mode.
	 */
	public void	setHistoryEnabled(boolean enabled)
	{
		// Hack!!! synchronized because of ProcessViewPanel.
		if(enabled && history==null)
			history	= Collections.synchronizedList(new ArrayList());
		else if(!enabled && history!=null)
			history	= null;
	}
	
	/**
	 *  Add a change listener.
	 *  @param listener The listener.
	 */
	public void addChangeListener(IChangeListener listener)
	{
		if(listeners==null)
			listeners = new ArrayList();
		listeners.add(listener);
	}
	
	/**
	 *  Remove a change listener.
	 *  @param listener The listener.
	 */
	public void removeChangeListener(IChangeListener listener)
	{
		if(listeners!=null)
			listeners.remove(listener);
	}
	
	/**
	 *  Notify the change listeners.
	 */
	public void notifyListeners(ChangeEvent event)
	{
		if(listeners!=null)
		{
			for(int i=0; i<listeners.size(); i++)
			{
				((IChangeListener)listeners.get(i)).changeOccurred(event);
			}
		}
	}
	
	/**
	 *  Test if the given context variable is declared.
	 *  @param name	The variable name.
	 *  @return True, if the variable is declared.
	 */
	public boolean hasContextVariable(String name)
	{
		return variables!=null && variables.containsKey(name);
	}
	
	/**
	 *  Get the value of the given context variable.
	 *  @param name	The variable name.
	 *  @return The variable value.
	 */
	public Object getContextVariable(String name)
	{
		if(variables!=null && variables.containsKey(name))
		{
			return variables.get(name);			
		}
		else
		{
			throw new RuntimeException("Undeclared context variable: "+name+", "+this);
		}
	}
	
	/**
	 *  Set the value of the given context variable.
	 *  @param name	The variable name.
	 *  @param value	The variable value.
	 */
	public void setContextVariable(String name, Object value)
	{
		setContextVariable(name, null, value);
	}
	
	/**
	 *  Set the value of the given context variable.
	 *  @param name	The variable name.
	 *  @param value	The variable value.
	 */
	public void setContextVariable(String name, Object key, Object value)
	{
		if(variables!=null && variables.containsKey(name))
		{
			if(key==null)
			{
				variables.put(name, value);	
			}
			else
			{
				Object coll = variables.get(name);
				if(coll instanceof List)
				{
					int index = key==null? -1: ((Number)key).intValue();
					if(index>=0)
						((List)coll).set(index, value);
					else
						((List)coll).add(value);
				}
				else if(coll!=null && coll.getClass().isArray())
				{
					int index = ((Number)key).intValue();
					Array.set(coll, index, value);
				}
				else if(coll instanceof Map)
				{
					((Map)coll).put(key, value);
				}
//				else
//				{
//					throw new RuntimeException("Unsupported collection type: "+coll);
//				}
			}
		}
		else
		{
			throw new RuntimeException("Undeclared context variable: "+name+", "+this);
		}
	}

	/**
	 *  Create component identifier.
	 *  @param name The name.
	 *  @param local True for local name.
	 *  @param addresses The addresses.
	 *  @return The new component identifier.
	 */
	public IComponentIdentifier createComponentIdentifier(String name)
	{
		IComponentManagementService cms = (IComponentManagementService)variables.get("$cms");
		return cms.createComponentIdentifier(name);
	}
	
	/**
	 *  Create component identifier.
	 *  @param name The name.
	 *  @param local True for local name.
	 *  @param addresses The addresses.
	 *  @return The new component identifier.
	 */
	public IComponentIdentifier createComponentIdentifier(String name, boolean local)
	{
		return createComponentIdentifier(name, local);
	}
	
	/**
	 *  Create component identifier.
	 *  @param name The name.
	 *  @param local True for local name.
	 *  @param addresses The addresses.
	 *  @return The new component identifier.
	 */
	public IComponentIdentifier createComponentIdentifier(final String name, final boolean local, final String[] addresses)
	{
		IComponentManagementService cms = (IComponentManagementService)variables.get("$cms");
		return cms.createComponentIdentifier(name, local, addresses);
	}
	
	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @return The reply event.
	 */
	public Map createReply(IMessageAdapter msg)
	{
		return createReply(msg.getParameterMap(), msg.getMessageType());
	}
	
	/**
	 *  Create a reply to this message event.
	 *  @param msgeventtype	The message event type.
	 *  @return The reply event.
	 */
	public Map createReply(final Map msg, final MessageType mt)
	{
		IMessageService ms = (IMessageService)variables.get("$msgservice");
		return ms.createReply(msg, mt);
		
//		final Future ret = new Future();
		
//		SServiceProvider.getService(getServiceProvider(), IMessageService.class)
//			.addResultListener(createResultListener(new DefaultResultListener()
//		{
//			public void resultAvailable(Object source, Object result)
//			{
//				IMessageService ms = (IMessageService)result;
//				ret.setResult(ms.createReply(msg, mt));
//			}
//		}));
				
//		return ret;
	}
	
	/**
	 *  Called when a component has been created as a subcomponent of this component.
	 *  This event may be ignored, if no special reaction  to new or destroyed components is required.
	 *  The current subcomponents can be accessed by IComponentAdapter.getSubcomponents().
	 *  @param comp	The newly created component.
	 */
	public IFuture	componentCreated(IComponentDescription desc, IModelInfo model)
	{
		return new Future(null);
	}

	/**
	 *  Called when a subcomponent of this component has been destroyed.
	 *  This event may be ignored, if no special reaction  to new or destroyed components is required.
	 *  The current subcomponents can be accessed by IComponentAdapter.getSubcomponents().
	 *  @param comp	The destroyed component.
	 */
	public IFuture	componentDestroyed(IComponentDescription desc)
	{
		return new Future(null);
	}
	
	/**
	 *  Create a component result listener.
	 */
	public IResultListener createResultListener(IResultListener listener)
	{
		return new ComponentResultListener(listener, adapter);
	}
	
	/**
	 *  Fires an activity execution event.
	 *  @param threadid ID of the executing ProcessThread.
	 *  @param activity The activity being executed.
	 */
	protected void fireStartActivity(String threadid, MActivity activity)
	{
//		System.out.println("fire start: "+activity);
		if(activitylisteners!=null)
		{
			for(Iterator it = activitylisteners.iterator(); it.hasNext(); )
				((IActivityListener)it.next()).activityStarted(new ChangeEvent(threadid, null, activity));
		}
	}
	
	/**
	 *  Fires an activity execution event.
	 *  @param threadid ID of the executing ProcessThread.
	 *  @param activity The activity being executed.
	 */
	protected void fireEndActivity(String threadid, MActivity activity)
	{
//		System.out.println("fire end: "+activity);
		if(activitylisteners!=null)
		{
			for(Iterator it = activitylisteners.iterator(); it.hasNext(); )
				((IActivityListener)it.next()).activityEnded(new ChangeEvent(threadid, null, activity));
		}
	}
	
	/**
	 *  Schedule a step of the agent.
	 *  May safely be called from external threads.
	 *  @param step	Code to be executed as a step of the agent.
	 *  @return The result of the step.
	 */
	public IFuture scheduleStep(IComponentStep step)
	{
		// To schedule a step an implicit activity is created.
		// In order to put the step parameter value it is necessary
		// to have an edge with a mapping. Otherwise the parameter
		// value with be deleted in process thread updateParametersBeforeStep().
		
		Future ret = new Future();
		MActivity act = new MActivity();
		act.setName("External Step Activity.");
		act.setClazz(ExecuteStepTask.class);
		act.addParameter(new MParameter(MParameter.DIRECTION_IN, Object[].class, "step", null));
		act.setActivityType(MBpmnModel.TASK);
		MSequenceEdge edge = new MSequenceEdge();
		edge.setTarget(act);
		edge.addParameterMapping("step", SJavaParser.parseExpression("step", null, null), null);
		act.addIncomingSequenceEdge(edge);
		MPool pl = model.getPool(pool);
		act.setPool(pl);
		ProcessThread pt = new ProcessThread(act, context, this);
		pt.setLastEdge(edge);
		pt.setParameterValue("step", new Object[]{step, ret});
		context.addThread(pt);
		return ret;
	}
	
	/**
	 *  Get the children (if any).
	 *  @return The children.
	 */
	public IFuture getChildren()
	{
		return adapter.getChildrenAccesses();
	}
	
	/**
	 *  Adds an activity listener. The listener will be called
	 *  once a process thread executes a new activity.
	 *  @param listener The activity listener.
	 */
	public void addActivityListener(IActivityListener listener)
	{
		if(activitylisteners==null)
			activitylisteners = new ArrayList();
		activitylisteners.add(listener);
	}
	
	/**
	 *  Removes an activity listener.
	 *  @param listener The activity listener.
	 */
	public void removeActivityListener(IActivityListener listener)
	{
		if(activitylisteners!=null)
			activitylisteners.remove(listener);
	}
	
	/**
	 *  Add an component listener.
	 *  @param listener The listener.
	 */
	public void addComponentListener(IComponentListener listener)
	{
		if(componentlisteners==null)
			componentlisteners = new ArrayList();
		componentlisteners.add(listener);
	}
	
	/**
	 *  Remove a component listener.
	 *  @param listener The listener.
	 */
	public void removeComponentListener(IComponentListener listener)
	{
		if(componentlisteners!=null)
			componentlisteners.remove(listener);
	}
	
}
