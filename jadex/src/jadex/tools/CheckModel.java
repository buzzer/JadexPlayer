package jadex.tools;

import java.io.IOException;

import jadex.config.Configuration;
import jadex.model.IMElement;
import jadex.model.IReport;
import jadex.model.SXML;

/**
 *  Loads an agent model and prints the result.
 */
public class CheckModel
{
	public static void	main(String args[]) throws IOException
	{
		if(args.length!=1)
		{
			System.out.println("Usage: CheckModel <adf name>");
			return;
		}
		
		Configuration.setFallbackConfiguration("jadex/config/batch_conf.properties");

		IMElement	model	= SXML.loadModel(args[0], null);
		if(model.getDescription()!=null)
			System.out.println(model.getDescription());

		IReport	report	=model.check();
		System.out.println(report);
	}
}
