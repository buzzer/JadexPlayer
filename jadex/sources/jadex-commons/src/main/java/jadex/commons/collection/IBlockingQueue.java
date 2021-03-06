package jadex.commons.collection;

/**
 *  A queue that blocks until an element is available.
 */
public interface IBlockingQueue
{
	/**
	 *  Enqueue an element.
	 *  @param element The element.
	 */
	public void enqueue(Object element)	throws ClosedException;

	/**
	 *  Dequeue an element.
	 *  @return The element. When queue is empty
	 *  the methods blocks until an element is added.
	 */
	public Object dequeue()	throws ClosedException;

	/**
	 *  Dequeue an element.
	 *  @param timeout	the time to wait (in millis) or -1 for no timeout.
	 *  @return The element. When queue is empty
	 *  the methods blocks until an element is added or the timeout occurs.
	 */
	public Object dequeue(long timeout)	throws ClosedException, TimeoutException;

	/**
	 *  Open/close the queue.
	 *  @param closed The closed state.
	 */
	public void setClosed(boolean closed);

	/**
	 *  Get the size.
	 *  @return The size.
	 */
	public int size();

	//-------- exceptions --------
	
	/**
	 *  Closed exception.
	 */
	public static class ClosedException extends RuntimeException
	{
		/**
		 *  Create a new closed exception.
		 */
		public ClosedException(String text)
		{
			super(text);
		}
	}

	/**
	 *  Timeout exception.
	 */
	public static class TimeoutException extends RuntimeException
	{
		/**
		 *  Create a new closed exception.
		 */
		public TimeoutException(String text)
		{
			super(text);
		}
	}

}