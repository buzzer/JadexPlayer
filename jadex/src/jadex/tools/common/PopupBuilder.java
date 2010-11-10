package jadex.tools.common;

import jadex.util.SUtil;

import javax.swing.*;
import java.util.List;

/**
 *  The popup builder. Constructs a (non-nested) popup menu from actions.
 */
public class PopupBuilder
{
	/** The menu-items and actions. */
	protected List rawmenu;

	/**
	 *  Create the popup builder.
	 *  @param rawmenu The raw menu.
	 */
	public PopupBuilder(List rawmenu)
	{
		this.rawmenu = rawmenu;
	}

	/**
	 *  Create the popup builder.
	 *  @param rawmenu The raw menu.
	 */
	public PopupBuilder(Object[] rawmenu)
	{
		this.rawmenu = SUtil.arrayToList(rawmenu);
	}

	/**
	 *  Build the popup menu.
	 *  @return The popup menu.
	 */
	public JPopupMenu buildPopupMenu()
	{
		JPopupMenu pop = new JPopupMenu();

		for(int i=0; i<rawmenu.size(); i++)
		{
			Object tmp = rawmenu.get(i);
			if(tmp instanceof Action)
			{
				if(((Action)tmp).isEnabled())
				{
					pop.add((Action)tmp);
				}
			}
			else if(tmp instanceof IMenuItemConstructor)
			{
				JMenuItem item = ((IMenuItemConstructor)tmp).getMenuItem();
				if(item!=null)
					pop.add(item);
			}
		}

		return pop;
	}

}