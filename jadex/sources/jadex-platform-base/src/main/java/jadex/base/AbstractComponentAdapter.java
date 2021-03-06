package jadex.base;

import jadex.base.service.cms.ComponentManagementService;
import jadex.bridge.CheckedAction;
import jadex.bridge.ComponentTerminatedException;
import jadex.bridge.DefaultMessageAdapter;
import jadex.bridge.IComponentAdapter;
import jadex.bridge.IComponentDescription;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentInstance;
import jadex.bridge.IComponentManagementService;
import jadex.bridge.IExternalAccess;
import jadex.bridge.IMessageAdapter;
import jadex.bridge.IModelInfo;
import jadex.bridge.MessageType;
import jadex.commons.Future;
import jadex.commons.ICommand;
import jadex.commons.IFuture;
import jadex.commons.concurrent.CollectionResultListener;
import jadex.commons.concurrent.DefaultResultListener;
import jadex.commons.concurrent.DelegationResultListener;
import jadex.commons.concurrent.IExecutable;
import jadex.commons.concurrent.IResultListener;
import jadex.commons.service.IServiceContainer;
import jadex.commons.service.SServiceProvider;
import jadex.commons.service.ServiceNotFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *  Base component adapter with reusable functionality for all platforms.
 */
public abstract class AbstractComponentAdapter implements IComponentAdapter, IExecutable, Serializable
{
	//-------- attributes --------

	/** The component identifier. */
	protected IComponentIdentifier cid;

	/** The component identifier. */
	protected IExternalAccess parent;

	/** The component instance. */
	protected IComponentInstance component;
	
	/** The component model. */
	protected IModelInfo model;

	/** The description holding the execution state of the component
	   (read only! managed by component execution service). */
	protected IComponentDescription	desc;
	
	/** The component logger. */
	protected Logger logger;
	
	/** Flag to indicate a fatal error (component termination will not be passed to instance) */
	protected boolean fatalerror;
	
	//-------- steppable attributes --------
	
	/** The flag for a scheduled step (true when a step is allowed in stepwise execution). */
	protected boolean	dostep;
	
	/** The listener to be informed, when the requested step is finished. */
	protected Future stepfuture;
	
	/** The selected breakpoints (component will change to step mode, when a breakpoint is reached). */
	protected Set	breakpoints;
	
	/** The breakpoint commands (executed, when a breakpoint triggers). */
	protected ICommand[]	breakpointcommands;
	
	//-------- external actions --------

	/** The thread executing the component (null for none). */
	// Todo: need not be transient, because component should only be serialized when no action is running?
	protected transient Thread componentthread;

	// todo: ensure that entries are empty when saving
	/** The entries added from external threads. */
	protected List	ext_entries;

	/** The flag if external entries are forbidden. */
	protected boolean ext_forbidden;
	
	/** Set when wakeup was called. */
	protected boolean	wokenup;
	
	/** Does the instance want to be executed again. */
	protected boolean	again;
	
	//-------- constructors --------

	/**
	 *  Create a new component adapter.
	 *  Uses the thread pool for executing the component.
	 */
	public AbstractComponentAdapter(IComponentDescription desc, IModelInfo model, IComponentInstance component, IExternalAccess parent)
	{
		this.desc = desc;
		this.cid	= desc.getName();
		this.model = model;
		this.component = component;
		this.parent	= parent;
		this.ext_entries = Collections.synchronizedList(new ArrayList());
	}
	
	//-------- IComponentAdapter methods --------

	/**
	 *  Called by the component when it probably awoke from an idle state.
	 *  The platform has to make sure that the component will be executed
	 *  again from now on.
	 *  Note, this method can be called also from external threads
	 *  (e.g. property changes). Therefore, on the calling thread
	 *  no component related actions must be executed (use some kind
	 *  of wake-up mechanism).
	 *  Also proper synchronization has to be made sure, as this method
	 *  can be called concurrently from different threads.
	 */
	public void wakeup()
	{
//		System.err.println("wakeup: "+getComponentIdentifier());		
		
		wokenup	= true;

		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()))
			throw new ComponentTerminatedException(cid);
		
		// Set processing state to ready if not running.
		if(IComponentDescription.PROCESSINGSTATE_IDLE.equals(desc.getProcessingState()))
		{
			getCMS().addResultListener(new DefaultResultListener()
			{
				public void resultAvailable(Object source, Object result)
				{
					((ComponentManagementService)result).setProcessingState(cid, IComponentDescription.PROCESSINGSTATE_READY);
				}
			});				
		}
		
		// Resume execution of the component.
		if(IComponentDescription.STATE_ACTIVE.equals(desc.getState())
			|| IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))	// Hack!!! external entries must also be executed in suspended state.
		{
			doWakeup();
		}
	}

	/**
	 *  Return a component-identifier that allows to send
	 *  messages to this component.
	 */
	public IComponentIdentifier getComponentIdentifier()
	{
		return cid;
	}
	
	/**
	 *  Get the logger.
	 *  @return The logger.
	 */
	public Logger getLogger()
	{
		// todo: problem: loggers can cause memory leaks
		// http://bugs.sun.com/view_bug.do;jsessionid=bbdb212815ddc52fcd1384b468b?bug_id=4811930
		
		// Todo: include parent name for nested loggers.
		String name = getComponentIdentifier().getLocalName();
		logger = LogManager.getLogManager().getLogger(name);
		
		// if logger does not already exists, create it
		if(logger==null)
		{
			// Hack!!! Might throw exception in applet / webstart.
			try
			{
				logger = Logger.getLogger(name);
				initLogger(logger);
				//System.out.println(logger.getParent().getLevel());
			}
			catch(SecurityException e)
			{
				// Hack!!! For applets / webstart use anonymous logger.
				logger = Logger.getAnonymousLogger();
				initLogger(logger);
			}
		}
		
		return logger;
	}
	
	/**
	 *  Init the logger with capability settings.
	 *  @param logger The logger.
	 */
	protected void initLogger(Logger logger)
	{
		// get logging properties (from ADF)
		// the level of the logger
		// can be Integer or Level
		
		Object prop = model.getProperties().get("logging.level");
		Level level = prop==null? Level.WARNING: (Level)prop;
		logger.setLevel(level);

		// if logger should use Handlers of parent (global) logger
		// the global logger has a ConsoleHandler(Level:INFO) by default
		prop = model.getProperties().get("logging.useParentHandlers");
		if(prop!=null)
		{
			logger.setUseParentHandlers(((Boolean)prop).booleanValue());
		}
			
		// add a ConsoleHandler to the logger to print out
        // logs to the console. Set Level to given property value
		prop = model.getProperties().get("addConsoleHandler");
		if(prop!=null)
		{
            ConsoleHandler console = new ConsoleHandler();
            console.setLevel(Level.parse(prop.toString()));
            logger.addHandler(console);
        }
		
		// Code adapted from code by Ed Komp: http://sourceforge.net/forum/message.php?msg_id=6442905
		// if logger should add a filehandler to capture log data in a file. 
		// The user specifies the directory to contain the log file.
		// $scope.getAgentName() can be used to have agent-specific log files 
		//
		// The directory name can use special patterns defined in the
		// class, java.util.logging.FileHandler, 
		// such as "%h" for the user's home directory.
		// 
		String logfile =	(String)model.getProperties().get("logging.file");
		if(logfile!=null)
		{
		    try
		    {
			    Handler fh	= new FileHandler(logfile);
		    	fh.setFormatter(new SimpleFormatter());
		    	logger.addHandler(fh);
		    }
		    catch (IOException e)
		    {
		    	System.err.println("I/O Error attempting to create logfile: "
		    		+ logfile + "\n" + e.getMessage());
		    }
		}
	}
	
	/**
	 *  Get the model.
	 *  @return The model.
	 */
	public IModelInfo getModel()
	{
		return this.model;
	}

	/**
	 *  Get the parent component.
	 *  @return The parent (if any).
	 */
	public IExternalAccess getParent()
	{
		return parent;
	}
	
	/**
	 *  Get the children (if any).
	 *  @return The children.
	 * /
	public IFuture getChildren()
	{
		final Future ret = new Future();
		
		SServiceProvider.getServiceUpwards(getServiceContainer(), IComponentManagementService.class)
			.addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				final IComponentManagementService cms = (IComponentManagementService)result;
				
				cms.getChildren(getComponentIdentifier()).addResultListener(new DefaultResultListener()
				{
					public void resultAvailable(Object source, Object result)
					{
						IComponentIdentifier[] childs = (IComponentIdentifier[])result;
						IResultListener	crl	= new CollectionResultListener(childs.length, true, new DelegationResultListener(ret));
						for(int i=0; !ret.isDone() && i<childs.length; i++)
						{
							cms.getExternalAccess(childs[i]).addResultListener(crl);
						}
					}
				});
			}
		});
		
		return ret;
	}*/
	
	/**
	 *  Get the children (if any).
	 *  @return The children.
	 */
	public IFuture getChildren()
	{
		final Future ret = new Future();
		
		getCMS().addResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				final IComponentManagementService cms = (IComponentManagementService)result;
				cms.getChildren(getComponentIdentifier()).addResultListener(new DelegationResultListener(ret));
			}
			
			public void exceptionOccurred(Object source, Exception exception)
			{
				ret.setException(exception);
			}
		});
		
		return ret;
	}
	
	/**
	 *  Get the children (if any).
	 *  @return The children.
	 */
	public IFuture getChildrenIdentifiers()
	{
		final Future ret = new Future();
		
		getCMS().addResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				final IComponentManagementService cms = (IComponentManagementService)result;
				cms.getChildren(getComponentIdentifier()).addResultListener(new DelegationResultListener(ret));
			}
			
			public void exceptionOccurred(Object source, Exception exception)
			{
				ret.setException(exception);
			}
		});
		
		return ret;
	}
	
	/**
	 *  Get the children (if any).
	 *  @return The children.
	 */
	public IFuture getChildrenAccesses()
	{
		final Future ret = new Future();
		
		SServiceProvider.getServiceUpwards(getServiceContainer(), IComponentManagementService.class)
			.addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				final IComponentManagementService cms = (IComponentManagementService)result;
				
				cms.getChildren(getComponentIdentifier()).addResultListener(new DefaultResultListener()
				{
					public void resultAvailable(Object source, Object result)
					{
						IComponentIdentifier[] childs = (IComponentIdentifier[])result;
						IResultListener	crl	= new CollectionResultListener(childs.length, true, new DelegationResultListener(ret));
						for(int i=0; !ret.isDone() && i<childs.length; i++)
						{
							cms.getExternalAccess(childs[i]).addResultListener(crl);
						}
					}
				});
			}
		});
		
		return ret;
	}
	
	/**
	 *  String representation of the component.
	 */
	public String toString()
	{
		return "StandaloneComponentAdapter("+cid.getName()+")";
	}

	
	/**
	 *  Get the service provider.
	 */
	public IServiceContainer getServiceContainer()
	{
		return component.getServiceContainer();
	}

	/** The cached cms. */
	protected IComponentManagementService	cms;
	
	/**
	 *  Get the (cached) cms.
	 */
	protected IFuture getCMS()
	{
		// Change comments below to test performance of cached cms vs. direct access.
		return SServiceProvider.getServiceUpwards(getServiceContainer(), IComponentManagementService.class);
//		final Future	ret	= new Future();
//		if(cms==null)
//		{
//			SServiceProvider.getServiceUpwards(getServiceContainer(), IComponentManagementService.class)
//				.addResultListener(new IResultListener()
//			{
//				public void resultAvailable(Object source, Object result)
//				{
//					cms	= (IComponentManagementService)result;
//					ret.setResult(cms);
//				}
//				
//				public void exceptionOccurred(Object source, Exception exception)
//				{
//					ret.setException(exception);
//				}
//			});
//		}
//		else
//		{
//			ret.setResult(cms);
//		}
//		return ret;
	}

	
	//-------- methods called by the standalone platform --------
	
	/**
	 *  Get description.
	 */
	public IComponentDescription getDescription()
	{
		return desc;
	}
	
	/**
	 *  Gracefully terminate the component.
	 *  This method is called from ams and delegated to the reasoning engine,
	 *  which might perform arbitrary cleanup actions, goals, etc.
	 *  @param listener	When cleanup of the component is finished, the listener must be notified.
	 */
	public IFuture killComponent()
	{
		final Future ret = new Future();
		
//		System.out.println("killComponent: "+listener);
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()))
		{
			ret.setException(new ComponentTerminatedException(cid));
		}
		else
		{
			if(!fatalerror)
			{
				component.cleanupComponent().addResultListener(new IResultListener()
				{
					public void resultAvailable(Object source, Object result)
					{
						synchronized(ext_entries)
						{
							// Do final cleanup step as (last) ext_entry
							// for allowing previously added entries still be executed.
							invokeLater(new Runnable()
							{								
								public void run()
								{
									shutdownContainer().addResultListener(new DelegationResultListener(ret));
									
//									System.out.println("Checking ext entries after cleanup: "+cid);
									assert ext_entries.isEmpty() : "Ext entries after cleanup: "+cid+", "+ext_entries;
								}
							});
							
							// No more ext entries after cleanup step allowed.
							ext_forbidden	= true;
						}
						
					}
					
					public void exceptionOccurred(Object source, Exception exception)
					{
						getLogger().warning("Exception during component cleanup: "+exception);
						shutdownContainer().addResultListener(new DelegationResultListener(ret));
					}
				});
			}
			else
			{
				ret.setResult(getComponentIdentifier());
//				listener.resultAvailable(this, getComponentIdentifier());
			}
		}
		
		return ret;
		
		// LogManager causes memory leak till Java 7
		// No way to remove loggers and no weak references. 
	}

	/**
	 *  Called from killComponent.
	 */
	protected IFuture shutdownContainer()
	{
		final Future ret = new Future();
		
		getServiceContainer().shutdown().addResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				ret.setResult(getComponentIdentifier());
//				listener.resultAvailable(this, getComponentIdentifier());
			}
			
			public void exceptionOccurred(Object source, Exception exception)
			{
				getLogger().warning("Exception during service container shutdown: "+exception);
//				listener.resultAvailable(this, getComponentIdentifier());
				ret.setResult(getComponentIdentifier());
			}
		});
		
		return ret;
	}

	/**
	 *  Called when a message was sent to the component.
	 *  (Called from message transport).
	 *  (Is it ok to call on external thread?).
	 */
	public void	receiveMessage(Map message, MessageType type)
	{
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || fatalerror)
			throw new ComponentTerminatedException(cid);

		// Add optional receival time.
//		String rd = type.getReceiveDateIdentifier();
//		Object recdate = message.get(rd);
//		if(recdate==null)
//			message.put(rd, new Long(getClock().getTime()));
		
		IMessageAdapter msg = new DefaultMessageAdapter(message, type);
		component.messageArrived(msg);
	}
	
	//-------- IExecutable interface --------
	
	boolean executing;
	Exception	rte;

	/**
	 *  Executable code for running the component
	 *  in the platforms executor service.
	 */
	public boolean	execute()
	{
//		synchronized(AsyncExecutionService.DEBUG)
//		{
//			AsyncExecutionService.DEBUG.put(this, "adapter execute()");
//		}
		
//		System.out.println("entering exe: "+getComponentIdentifier());
		if(executing)
		{
			System.err.println(getComponentIdentifier()+": double execution");
//			List	debug	= (List)AsyncExecutionService.DEBUG.getCollection(this);
//			for(int i=0; i<debug.size(); i++)
//				System.err.println(getComponentIdentifier()+": "+debug.get(i));
//			rte.printStackTrace();
			new RuntimeException("executing: "+getComponentIdentifier()).printStackTrace();
		}
//		rte	= new RuntimeException("executing: "+getComponentIdentifier());
//		rte.fillInStackTrace();
		executing	= true;
		wokenup	= false;	
		
		// Note: wakeup() can be called from arbitrary threads (even when the
		// component itself is currently running. I.e. it cannot be ensured easily
		// that an execution task is enqueued and the component has terminated
		// meanwhile.
		boolean	ret;
		if(!IComponentDescription.STATE_TERMINATED.equals(desc.getState()))
		{
			if(fatalerror)
				return false;	// Component already failed: tell executor not to call again. (can happen during failed init)
	
			// Remember execution thread.
			this.componentthread	= Thread.currentThread();
			
			ClassLoader	cl	= componentthread.getContextClassLoader();
			componentthread.setContextClassLoader(model.getClassLoader());
	
			getCMS().addResultListener(new DefaultResultListener()
			{
				public void resultAvailable(Object source, Object result)
				{
					((ComponentManagementService)result).setProcessingState(cid, IComponentDescription.PROCESSINGSTATE_RUNNING);
				}
				
				public void exceptionOccurred(Object source, Exception exception)
				{
					// CMS may be null during platform init
					if(!(exception instanceof ServiceNotFoundException))
						super.exceptionOccurred(source, exception);
				}
			});
			
			// Copy actions from external threads into the state.
			// Is done in before tool check such that tools can see external actions appearing immediately (e.g. in debugger).
			boolean	extexecuted	= false;
			Runnable[]	entries	= null;
			synchronized(ext_entries)
			{
				if(!(ext_entries.isEmpty()))
				{
					entries	= (Runnable[])ext_entries.toArray(new Runnable[ext_entries.size()]);
					ext_entries.clear();
					
					extexecuted	= true;
				}
			}
			for(int i=0; entries!=null && i<entries.length; i++)
			{
				if(entries[i] instanceof CheckedAction)
				{
					if(((CheckedAction)entries[i]).isValid())
					{
						try
						{
							entries[i].run();
						}
						catch(Exception e)
						{
							fatalError(e);
						}
					}
					try
					{
						((CheckedAction)entries[i]).cleanup();
					}
					catch(Exception e)
					{
						fatalError(e);
					}
				}
				else //if(entries[i] instanceof Runnable)
				{
					try
					{
//						if(entries[i].toString().indexOf("calc")!=-1)
//						{
//							System.out.println("scheduleStep: "+getComponentIdentifier());
//						}
						entries[i].run();
					}
					catch(Exception e)
					{
	//					e.printStackTrace();
						fatalError(e);
					}
				}
			}
	
			// Suspend when breakpoint is triggered.
			boolean	breakpoint_triggered	= false;
			if(!dostep && !IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
			{
				if(component.isAtBreakpoint(desc.getBreakpoints()))
				{
					breakpoint_triggered	= true;
					getCMS().addResultListener(new DefaultResultListener()
					{
						public void resultAvailable(Object source, Object result)
						{
							((IComponentManagementService)result).suspendComponent(cid);
						}
					});
				}
			}
			
			if(!breakpoint_triggered && !extexecuted && (!IComponentDescription.STATE_SUSPENDED.equals(desc.getState()) || dostep))
			{
				try
				{
	//				System.out.println("Executing: "+component);
					again	= component.executeStep();
				}
				catch(Exception e)
				{
					fatalError(e);
				}
				if(dostep)
				{
					dostep	= false;
					if(stepfuture!=null)
					{
						stepfuture.setResult(desc);
					}
				}
				
				// Suspend when breakpoint is triggered.
				if(!IComponentDescription.STATE_SUSPENDED.equals(desc.getState()))
				{
					if(component.isAtBreakpoint(desc.getBreakpoints()))
					{
						breakpoint_triggered	= true;
						getCMS().addResultListener(new DefaultResultListener()
						{
							public void resultAvailable(Object source, Object result)
							{
								((IComponentManagementService)result).suspendComponent(cid);
							}
						});
					}
				}
			}
			
			final boolean	ready	= again && !breakpoint_triggered || extexecuted || wokenup;
			getCMS().addResultListener(new DefaultResultListener()
			{
				public void resultAvailable(Object source, Object result)
				{
					((ComponentManagementService)result).setProcessingState(cid, ready
						? IComponentDescription.PROCESSINGSTATE_READY : IComponentDescription.PROCESSINGSTATE_IDLE);
				}
			});

			// Reset execution thread.
			componentthread.setContextClassLoader(cl);
			this.componentthread = null;		

			ret	= (again && !IComponentDescription.STATE_SUSPENDED.equals(desc.getState())) || extexecuted;
		}
		else
		{
			ret	= false;
		}
		
//		System.out.println("end: "+getComponentIdentifier());
		executing	= false;
//		synchronized(AsyncExecutionService.DEBUG)
//		{
//			AsyncExecutionService.DEBUG.put(this, "adapter execute() finished");
//		}
		
		return ret;
	}

	/**
	 * 	Called when an error occurs during component execution.
	 *  @param e	The error.
	 */
	protected void fatalError(final Exception e)
	{
//		e.printStackTrace();
		
		// Fatal error!
		fatalerror	= true;
		
		// Remove component from platform.
		getCMS().addResultListener(new DefaultResultListener()
		{
			public void resultAvailable(Object source, Object result)
			{
				ComponentManagementService	cms	= (ComponentManagementService)result;
				cms.setComponentException(cid, e);
				cms.destroyComponent(cid);
			}
		});
	}
	
	/**
	 *  Check if the external thread is accessing.
	 *  @return True, if called from an external (i.e. non-synchronized) thread.
	 */
	public boolean isExternalThread()
	{
		return Thread.currentThread()!=componentthread;
	}
	
	//-------- external access --------
	
	/**
	 *  Execute an action on the component thread.
	 *  May be safely called from any (internal or external) thread.
	 *  The contract of this method is as follows:
	 *  The component adapter ensures the execution of the external action, otherwise
	 *  the method will throw a terminated exception.
	 *  @param action The action to be executed on the component thread.
	 */
	public void invokeLater(Runnable action)
	{
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || fatalerror)
			throw new ComponentTerminatedException(cid);

		synchronized(ext_entries)
		{
//			System.out.println("Adding to ext entries: "+cid);
			if(ext_forbidden)
			{
				throw new ComponentTerminatedException(cid);
			}
			else
			{
				ext_entries.add(action);
			}
		}
		wakeup();
	}
	
	//-------- test methods --------
	
	/**
	 *  Make kernel component available.
	 */
	public IComponentInstance	getComponentInstance()
	{
		return component;
	}

	//-------- step handling --------
	
	/**
	 *  Set the step mode.
	 */
	public IFuture doStep()
	{
		Future ret = new Future();
		if(IComponentDescription.STATE_TERMINATED.equals(desc.getState()) || fatalerror)
			ret.setException(new ComponentTerminatedException(cid));
		else if(dostep)
			ret.setException(new RuntimeException("Only one step allowed at a time."));
			
		this.dostep	= true;		
		this.stepfuture = ret;
		
		wakeup();
		
		return ret;
	}
	
	/**
	 *  Wake up this component.
	 */
	protected abstract void	doWakeup();
}
