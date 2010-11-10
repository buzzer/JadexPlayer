package jadex.examples.puzzle.mobile;

import jadex.examples.puzzle.BoardControlPanel;
import jadex.examples.puzzle.BoardPanel;
import jadex.examples.puzzle.IBoard;
import jadex.runtime.AgentEvent;
import jadex.runtime.IAgentListener;
import jadex.runtime.IExternalAccess;
import jadex.util.SGUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *  The board gui.
 */
public class BoardGui extends JFrame
{
	//-------- attributes --------

	/** The board to visualize. */
	protected IBoard board;

	/** The agent interface. */
	protected IExternalAccess	agent;
	
	/** The agent listener (must be removed before migration). */
	protected IAgentListener	listener;

	//-------- constructors --------

	/**
	 *  Create a new board gui.
	 */
	public BoardGui(IExternalAccess agent, final IBoard board)
	{
		this(agent, board, false);
	}

	/**
	 *  Create a new board gui.
	 */
	public BoardGui(IExternalAccess agent, IBoard board, boolean controls)
	{
		this.agent	= agent;
		this.board	= board;
		BoardPanel bp = new BoardPanel(board);

		this.getContentPane().add("Center", bp);
		this.board.addPropertyChangeListener(new BoardListener(board, bp));

		if(controls)
		{
			final BoardControlPanel bcp = new BoardControlPanel(board, bp);
			this.getContentPane().add("South", bcp);
		}
		this.setTitle("Puzzle Board");
		this.setSize(400, 400);
		this.setLocation(SGUI.calculateMiddlePosition(this));
		this.setVisible(true);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				BoardGui.this.agent.killAgent();
			}
		});
		
		this.listener	= new IAgentListener()
		{
			public void agentTerminating(AgentEvent ae)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						BoardGui.this.dispose();
					}
				});
			}
		};
		agent.addAgentListener(listener, false);
	}
	
	//-------- methods --------
	
	/**
	 *  Cleanup before migration.
	 */
	public void	migrationCleanup()
	{
		agent.removeAgentListener(listener);
	}
}
