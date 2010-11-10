package jadex.examples.cleanerworld.single;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import jadex.runtime.*;
import jadex.util.SGUI;


/**
 *  The gui for the cleaner world example.
 *  Shows the world from the viewpoint of a single agent.
 */
public class CleanerGui	extends JFrame
{
	//-------- constructors --------

	/**
	 *  Create a new gui plan.
	 */
	public CleanerGui(final IExternalAccess agent)
	{
		super("Cleaner World");
		final JPanel	map	= new JPanel()
		{
			// overridden paint method.
			protected void	paintComponent(Graphics g)
			{
				try
				{
					// Get world state from beliefs.
					Location	agentloc	= (Location)agent.getBeliefbase().getBelief("my_location").getFact();
					Location[]	wastelocs	= (Location[])agent.getExternalAccess().getBeliefbase().getBeliefSet("waste_locations").getFacts();
					Location[]	kwastelocs	= (Location[])agent.getExternalAccess().getBeliefbase().getBeliefSet("known_waste_locations").getFacts();
					Location	chargeloc	= (Location)agent.getExternalAccess().getBeliefbase().getBelief("chargingstation_location").getFact();
					double	vision	= ((Double)agent.getBeliefbase().getBelief("vision").getFact()).doubleValue();
					double	charge	= ((Double)agent.getBeliefbase().getBelief("chargestate").getFact()).doubleValue();
					boolean	waste	= ((Boolean)agent.getBeliefbase().getBelief("carrieswaste").getFact()).booleanValue();
					boolean	daytime	= ((Boolean)agent.getBeliefbase().getBelief("daytime").getFact()).booleanValue();
	
					// Paint background (dependent on daytime).
					Rectangle	bounds	= getBounds();
					g.setColor(daytime ? Color.lightGray : Color.darkGray);
					g.fillRect(0, 0, bounds.width, bounds.height);
	
					// Paint agent.
					Point	p	= onScreenLocation(agentloc, bounds);
					int w	= (int)(vision*bounds.width);
					int h	= (int)(vision*bounds.height);
					g.setColor(Color.yellow);	// Vision
					g.fillOval(p.x-w, p.y-h, w*2, h*2);
					g.setColor(Color.black);	// Agent
					g.fillOval(p.x-3, p.y-3, 7, 7);
					g.drawString(agent.getAgentName(), p.x+5, p.y-5);
					g.drawString("battery: " + (int)(charge*100.0) + "%",
						p.x+5, p.y+5);
					g.drawString("waste: " + (waste ? "yes" : "no"),
						p.x+5, p.y+15);
	
					// Paint charge Station.
					g.setColor(Color.blue);
					p	= onScreenLocation(chargeloc, bounds);
					g.drawRect(p.x-10, p.y-10, 21, 21);
					g.setColor(daytime ? Color.black : Color.white);
					g.drawString("Charge Station", p.x+14, p.y+5);
	
					// Paint waste Bin.
					Location loci = (Location)agent.getBeliefbase().getBelief("wastebin_location").getFact();
					g.setColor(Color.red);
					p	= onScreenLocation(loci, bounds);
					g.drawOval(p.x-10, p.y-10, 21, 21);
					g.setColor(daytime ? Color.black : Color.white);
					g.drawString("Wastebin", p.x+14, p.y+5);
	
					// Paint waste.
					for(int i=0; i<wastelocs.length; i++)
					{
						g.setColor(Color.red);
						p	= onScreenLocation(wastelocs[i], bounds);
						g.fillOval(p.x-3, p.y-3, 7, 7);
					}
	
					// Paint known waste.
					for(int i=0; i<kwastelocs.length; i++)
					{
						g.setColor(Color.black);
						p	= onScreenLocation(kwastelocs[i], bounds);
						g.drawLine(p.x, p.y-5, p.x, p.y+5);
						g.drawLine(p.x-5, p.y, p.x+5, p.y);
					}
				}
				catch(AgentDeathException e) {}
			}
		};

		// Option panel.
		JPanel	options	= new JPanel();
		final JCheckBox	daytime	= new JCheckBox("daytime", ((Boolean)agent.getBeliefbase().getBelief("daytime").getFact()).booleanValue());
		daytime.addChangeListener(new ChangeListener()
		{
			public void	stateChanged(ChangeEvent ce)
			{
				agent.getBeliefbase().getBelief("daytime").setFact(new Boolean(daytime.isSelected()));
			}
		});
		options.add(daytime);

		getContentPane().add(BorderLayout.NORTH, options);
		getContentPane().add(BorderLayout.CENTER, map);
		setSize(600, 600);
		setLocation(SGUI.calculateMiddlePosition(this));
		setVisible(true);

		map.addMouseListener(new MouseAdapter()
		{
			IExpression	nearest;
			public void mouseClicked(MouseEvent me)
			{
				// Problem!!! Beliefbase is changed in
				// the gui thread.
				Point	p	= me.getPoint();
				Rectangle	bounds	= map.getBounds();
				Location	mouseloc	= new Location((double)p.x/(double)bounds.width,
					1.0-(double)p.y/(double)bounds.height);
				double tol = 7/(double)bounds.height;

				if(nearest==null)
					nearest	= agent.getExpressionbase().getExpression("nearest_waste");
				nearest.setParameter("$mouseloc", mouseloc);
				nearest.setParameter("$tol", new Double(tol));
				Location wasteloc = (Location)nearest.execute();

				// If waste is near clicked position remove the waste
				if(wasteloc!=null)
				{
					agent.getBeliefbase().getBeliefSet("waste_locations").removeFact(wasteloc);
				}

				// If position is clean add a new waste
				else
				{
					agent.getBeliefbase().getBeliefSet("waste_locations").addFact(mouseloc);
				}
			}
		});
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				agent.killAgent();
			}
		});
		
		agent.addAgentListener(new IAgentListener()
		{
			public void agentTerminating(AgentEvent ae)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						CleanerGui.this.dispose();
					}
				});
			}
		}, false);
		
		Timer	timer	= new Timer(50, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				map.invalidate();
				map.repaint();
			}
		});
		timer.start();
	}

	//-------- method --------

	/**
	 *  Get the on screen location for a location in the world.
	 */
	protected Point	onScreenLocation(Location loc, Rectangle bounds)
	{
		return new Point((int)(bounds.width*loc.x),
			(int)(bounds.height*(1.0-loc.y)));
	}
}

