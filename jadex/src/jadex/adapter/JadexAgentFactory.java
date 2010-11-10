package jadex.adapter;

import jadex.config.Configuration;
import jadex.model.IMBDIAgent;
import jadex.model.SXML;
import jadex.runtime.AgentCreationException;
import jadex.runtime.impl.JadexInterpreter;

import java.io.IOException;
import java.util.Map;


/**
 *  A factory to be used by adapters to create jadex agents.
 */
public class JadexAgentFactory
{
	/**
	 *  Create a Jadex agent.
	 *  @param adapter	The platform adapter for the agent. 
	 *  @param model	The agent model file (i.e. the name of the XML file).
	 *  @param config	The name of the configuration (or null for default configuration) 
	 *  @param arguments	The arguments for the agent as name/value pairs.
	 *  @return	An instance of a jadex agent.
	 */
	public static IJadexAgent	createJadexAgent(IAgentAdapter adapter, String model, String config, Map arguments)
	{
		try
		{
			IMBDIAgent magent = SXML.loadAgentModel(model, null);
			if(Configuration.getConfiguration().isModelChecking() && !magent.getReport().isEmpty())
			{
				throw new RuntimeException("Cannot start agent "+adapter.getAgentIdentifier().getLocalName()+" due to errors in model:\n"+magent.getReport().toString());
			}
			if(config!=null && config.length()!=0 && magent.getConfigurationbase().getConfiguration(config)==null)
			{
				throw new RuntimeException("Cannot start agent "+adapter.getAgentIdentifier().getLocalName()+" with unknown configuration '"+config+"'.");
			}
		
			//return new RBDIAgent(adapter, magent, config, arguments, null);
			return new JadexInterpreter(adapter, magent, config, arguments);
		}
		catch(IOException e)
		{
			throw new AgentCreationException("Error loading agent model: "+model+"\n", e);
		}
	}
}
