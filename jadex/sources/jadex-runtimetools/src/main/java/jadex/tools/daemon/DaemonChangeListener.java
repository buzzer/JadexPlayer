package jadex.tools.daemon;

import jadex.commons.ChangeEvent;
import jadex.commons.Future;
import jadex.commons.IFuture;
import jadex.commons.jtable.ObjectTableModel;

import javax.swing.JTable;

/**
 * 
 */
public class DaemonChangeListener implements IRemoteChangeListener
{
	/** The platform table. */
	protected JTable platformt;
	
	/**
	 *  Create a new change listener.
	 */
	public DaemonChangeListener(JTable platformt)
	{
		this.platformt = platformt;
	}
	
	/**
	 *  Invoked when a change occurs.
	 */
	public IFuture changeOccurred(ChangeEvent event)
	{
		if(IDaemonService.ADDED.equals(event.getType()))
		{
			((ObjectTableModel)platformt.getModel()).addRow(new Object[]{event.getValue()}, event.getValue());
		}
		else if(IDaemonService.REMOVED.equals(event.getType()))
		{
			((ObjectTableModel)platformt.getModel()).removeRow(event.getValue());
		}
		return new Future(null);
	}
}
