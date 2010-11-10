package jadex.examples.puzzle.mobile;

import jadex.examples.puzzle.BoardPanel;
import jadex.examples.puzzle.IBoard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 *  Listener to update the gui after each move.
 */
public class BoardListener implements PropertyChangeListener, Serializable
{
	//-------- attributes --------
	
	/** The board to listen on. */
	protected IBoard	board;

	/** The board panel to update. */
	protected transient BoardPanel	panel;

	//-------- constructors --------
	
	/**
	 *  Create a new BoardListener.
	 */
	public BoardListener(IBoard board, BoardPanel panel)
	{
		this.board	= board;
		this.panel	= panel;
	}

	//-------- methods --------
	
	/**
	 *  Update panel after move.
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if(panel!=null)
		{
			panel.update(evt);
		}
		else
		{
			// When board is lost, agent has been migrated, remove listener for old gui.
			board.removePropertyChangeListener(this);
		}
	}
}