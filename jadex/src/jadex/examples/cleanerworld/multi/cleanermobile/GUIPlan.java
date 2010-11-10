package jadex.examples.cleanerworld.multi.cleanermobile;

import jadex.runtime.IEvent;
import jadex.runtime.MobilePlan;

import java.io.ObjectStreamException;

import javax.swing.SwingUtilities;


/**
 *  The gui for the cleaner world example.
 *  Shows the world from the viewpoint of a single agent.
 *  When the plan is serialized (for migration) the gui is automatically closed
 *  and reopened later, when the plan resumes.
 */
public class GUIPlan	extends MobilePlan
{
	//-------- attributes --------

	/** The frame showing the cleaner world. */
	protected CleanerGui	gui;

	//-------- body --------

	/**
	 *  The plan body.
	 *  Shows the gui, and updates it when beliefs change.
	 */
	public void action(IEvent event)
	{
		// Create the gui if not open.
		if(gui==null)
		{
			gui	= new CleanerGui(getExternalAccess());
		}

		// Recheck every second if the gui is lost (due to migration).
		waitFor(1000);
	}

	//-------- serialization handling --------
	
	/**
	 *  Close frame before serialization.
	 */
	protected Object    writeReplace() throws ObjectStreamException
	{
		if(gui!=null)
		{
			gui.migrationCleanup();
			final CleanerGui	mygui	= gui;
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

