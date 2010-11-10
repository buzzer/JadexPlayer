package jadex.tools.common;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import jadex.util.SGUI;
import jadex.config.Configuration;


/**
 *  The agent manager gui allows for starting/stopping  agents platform independently.
 */
public class AboutDialog extends JAutoPositionDialog
{
	//-------- static part --------

	/** The image icons. */
	protected static UIDefaults	icons	= new UIDefaults(new Object[]{"logo",
			SGUI.makeIcon(AboutDialog.class, "/jadex/tools/common/images/jadex_logo.png"),});

	/** The text to display. */
	public static String		infotext;

	static
	{
		infotext = "<head/><body>(c) 2002-2007<br>"
				+ "Alexander Pokahr, Lars Braubach<br>"
				+ "Artwork by Dirk Bade<br>"
				+ "All rights reserved<br>";
		String rn = Configuration.getConfiguration().getReleaseNumber();
		String rd = Configuration.getConfiguration().getReleaseDate();
		infotext += "Version " + rn + " (" + rd + ")<br>";
		infotext += "<a href=\"http://sourceforge.net/projects/jadex\">http://sourceforge.net/projects/jadex</a><br>";
		infotext += "</body>";
	}

	//-------- constructors --------

	/**
	 *  Open the gui.
	 */
	public AboutDialog(Frame owner)
	{
		super(owner);
		setTitle("About Jadex");
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());
		ImageIcon logo = (ImageIcon)icons.getIcon("logo");

		BrowserPane bp = new BrowserPane();
		bp.setText(infotext);
		bp.setDefaultOpenMode(true);

		JLabel lab = new JLabel(logo);
		cp.add(lab, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
		cp.add(bp, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
		Color bg = new Color(247, 248, 253);
		//Color bg = new Color(0xd8, 0xdf, 0xf2);
		cp.setBackground(bg);
		bp.setBackground(bg);
		lab.setBackground(bg);

		setUndecorated(true);
		Border bl = BorderFactory.createLineBorder(Color.black);
		((JComponent)cp).setBorder(bl);
		addWindowFocusListener(new WindowFocusListener()
		{
			public void windowLostFocus(WindowEvent e)	{dispose();}
			public void windowGainedFocus(WindowEvent e){/*NOP*/}
		});
		pack();
		setVisible(true);
	}

	/**
	 *  Main for testing.
	 *  @param args
	 */
	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.setSize(500, 100);
		f.setVisible(true);
		new AboutDialog(f);
	}
}
