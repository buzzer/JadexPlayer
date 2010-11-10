package jadex.adapter.standalone;

import jadex.util.concurrent.IExecutorService;
import jadex.util.concurrent.ITimerService;

import java.util.logging.Logger;

/**
 *  The standalone platform interface.
 */
public interface IPlatform
{
	/**
	 *  Get the timer service.
	 *  @return The timer service.
	 */
	public ITimerService getTimerService();
	
	/**
	 *  Get the executor service.
	 *  @return The executor service.
	 */
	public IExecutorService getExecutorService();
   
	/**
	 *  Get the AMS of the platform.
	 *  @return The AMS.
	 */
	public IAMS	getAMS();

	/**
	 *  Get the DF of the platform.
	 *  @return The DF.
	 */
	public IDF	getDF();
	
	/**
	 *  Get the message service.
	 *  @return The message service.
	 */
	public IMessageService getMessageService();
	
	/**
	 *  Get the platform logger.
     *  @return The platform logger.
     */
    //public Logger getLogger();
    
    /**
	 *  Get the name of the platform
	 *  @return The name of this platform.
	 */
	public String getName();
	
	/**
     *  Shutdown the platform.
     */
	public void shutdown();

	/**
     *  Check if the platform is currently shutting down.
     */
	public boolean	isShuttingDown();
}
