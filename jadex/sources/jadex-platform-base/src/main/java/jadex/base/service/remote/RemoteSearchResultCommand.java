package jadex.base.service.remote;

import jadex.commons.Future;
import jadex.commons.IFuture;
import jadex.commons.concurrent.DelegationResultListener;
import jadex.commons.concurrent.IResultListener;
import jadex.commons.service.IService;
import jadex.commons.service.SServiceProvider;
import jadex.commons.service.library.ILibraryService;
import jadex.micro.IMicroExternalAccess;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  Command for remote searching.
 */
public class RemoteSearchResultCommand extends RemoteResultCommand
{
	//-------- constructors --------
	
	/**
	 *  Create a new remote search result command.
	 */
	public RemoteSearchResultCommand()
	{
	}

	/**
	 *  Create a new remote search result command.
	 */
	public RemoteSearchResultCommand(Object result, Exception exception, String callid)
	{
		super(result, exception, callid);
	}
	
	//-------- methods --------
	
	/**
	 *  Execute the command.
	 *  @param lrms The local remote management service.
	 *  @return An optional result command that will be 
	 *  sent back to the command origin. 
	 */
	public IFuture execute(final IMicroExternalAccess component, final Map waitingcalls)
	{
		final Future ret = new Future();
		
		// Post-process results to make them real proxies.
		
		SServiceProvider.getService(component.getServiceProvider(), ILibraryService.class)
			.addResultListener(new IResultListener()
//			.addResultListener(component.createResultListener(new IResultListener()
		{
			public void resultAvailable(Object source, Object res)
			{
				ILibraryService ls = (ILibraryService)res;
				
				if(result instanceof Collection)
				{
					List tmp = new ArrayList();
					for(Iterator it=((Collection)result).iterator(); it.hasNext(); )
					{
						ProxyInfo pi = (ProxyInfo)it.next();
						IService ser = (IService)Proxy.newProxyInstance(ls.getClassLoader(), 
							new Class[]{pi.getServiceIdentifier().getServiceType(), IService.class},
							new RemoteMethodInvocationHandler(component, pi, waitingcalls));
						tmp.add(ser);
					}
					result = tmp;
				}
				else if(result instanceof ProxyInfo)
				{
					ProxyInfo pi = (ProxyInfo)result;
					result = (IService)Proxy.newProxyInstance(ls.getClassLoader(), 
						new Class[]{pi.getServiceIdentifier().getServiceType(), IService.class},
						new RemoteMethodInvocationHandler(component, pi, waitingcalls));
				}
				
				RemoteSearchResultCommand.super.execute(component, waitingcalls)
					.addResultListener(new DelegationResultListener(ret));
//					.addResultListener(component.createResultListener(new DelegationResultListener(ret)));
			}
			
			public void exceptionOccurred(Object source, Exception exception)
			{
				ret.setException(exception);
			}
		});
		
		return ret;
	}
}