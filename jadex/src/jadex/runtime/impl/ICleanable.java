package jadex.runtime.impl;

/**
 *  Interface for elements that can be cleanuped.
 */
public interface ICleanable
{
	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations. They must call super.cleanup
	 *  to ensure that any cleanup operations are performed.
	 */
	public void	cleanup();
}
