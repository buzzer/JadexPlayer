package jadex.examples.puzzle.mobile;

import jadex.examples.puzzle.IBoard;
import jadex.runtime.IEvent;
import jadex.runtime.MobilePlan;

import java.io.ObjectStreamException;

import javax.swing.SwingUtilities;


/**
 *  Manage the gui for mobile puzzle agent.
 *  When the plan is serialized (for migration) the gui is automatically closed
 *  and reopened later, when the plan resumes.
 */
public class GUIPlan	extends MobilePlan
{
	//-------- attributes --------

	/** The frame showing the puzzle gui. */
	protected BoardGui	gui;

	//-------- body --------

	/**
	 *  The plan body.
	 */
	public void action(IEvent event)
	{
		// This plan never ends.
		//waitFor(IFilter.NEVER);
		
		// Hack!!! Cannot sleep for ever, because we have to check once in a while
		// if the agent has migrated and has to reopen the gui.
		waitFor(1000);
		
		if(gui==null)
			this.gui	= new BoardGui(getExternalAccess(), (IBoard)getBeliefbase().getBelief("board").getFact());
	}
	
	public void passed(IEvent event)
	{
		// TODO Auto-generated method stub
		super.passed(event);
	}
	
	public void failed(IEvent event)
	{
		// TODO Auto-generated method stub
		super.failed(event);
	}
	
	public void aborted(IEvent event)
	{
		// TODO Auto-generated method stub
		super.aborted(event);
	}

	/**
	 *  Close gui before serialization.
	 */
	protected Object    writeReplace() throws ObjectStreamException
	{
		if(gui!=null)
		{
			gui.migrationCleanup();
			final BoardGui	mygui	= gui;
			gui	= null;
			
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					mygui.dispose();
				}
			});
		}
	    return this;
	}
}

