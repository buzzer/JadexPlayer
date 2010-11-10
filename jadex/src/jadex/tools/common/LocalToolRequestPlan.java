package jadex.tools.common;

import java.util.*;
import jadex.runtime.*;
import jadex.adapter.fipa.*;
import jadex.adapter.IToolAdapter;
import jadex.adapter.IToolAdapter.IToolReply;
import jadex.tools.ontology.*;
import jadex.util.collection.SCollection;

/**
 *  Plan to issue a request to an observed agent.
 */
public class LocalToolRequestPlan extends Plan
{
	//-------- constructors --------

	/** The message preprocessors property identifier. */
	public static final String	PROPERTY_TOOL_ADAPTERS	= "tooladapter";

	//-------- attributes --------

	/** The tool. */
	protected IToolPanel	tool;

	/** The request. */
	protected ToolRequest	request;

	//-------- constructors --------

	/**
	 *  Create a new LocalToolRequestPlan.
	 */
	public LocalToolRequestPlan(IToolPanel tool, ToolRequest request)
	{
		this.tool	= tool;
		this.request	= request;
	}

	//-------- methods --------

	/**
	 *  Plan body.
	 */
	public void body()
	{
		handleLocalToolRequest(getCapability(), request, new ShortcutToolReply(tool, getRootGoal()));
		/*List tooladapters = SCollection.createArrayList();
		String[] keys = getPropertybase().getPropertyNames(PROPERTY_TOOL_ADAPTERS);
		for(int i = 0; i < keys.length; i++)
		{
			IToolAdapter adapter = (IToolAdapter)getPropertybase().getProperty(keys[i]);
			tooladapters.add(adapter);
		}

		boolean processed = false;
		for(int i=0; i<tooladapters.size(); i++)
		{
			IToolAdapter adapter = (IToolAdapter)tooladapters.get(i);
			if(adapter.getMessageClass().isInstance(request))
			{
				try
				{
					adapter.handleToolRequest((AgentIdentifier)getScope().getAgentIdentifier(),
						request, new ShortcutToolReply(tool, getRootGoal()));
					processed = true;
				}
				catch(RuntimeException e)
				{
					getLogger().severe("Tool adapter "+adapter+"threw exception "+e);
					e.printStackTrace();
				}
			}
		}
		if(!processed)
		{
			getLogger().warning("No tool adapter to handle: "+request);
		}*/
	}

	/**
	 *
	 */
	public static void handleLocalToolRequest(ICapability scope, ToolRequest request, IToolReply reply)
	{
		List tooladapters = SCollection.createArrayList();
		String[] keys = scope.getPropertybase().getPropertyNames(PROPERTY_TOOL_ADAPTERS);
		for(int i = 0; i < keys.length; i++)
		{
			IToolAdapter adapter = (IToolAdapter)scope.getPropertybase().getProperty(keys[i]);
			tooladapters.add(adapter);
		}

		boolean processed = false;
		for(int i=0; i<tooladapters.size(); i++)
		{
			IToolAdapter adapter = (IToolAdapter)tooladapters.get(i);
			if(adapter.getMessageClass().isInstance(request))
			{
				try
				{
					adapter.handleToolRequest((AgentIdentifier)scope.getAgentIdentifier(),
						request, reply);
					processed = true;
				}
				catch(RuntimeException e)
				{
					scope.getLogger().severe("Tool adapter "+adapter+"threw exception "+e);
					e.printStackTrace();
				}
			}
		}
		if(!processed)
		{
			scope.getLogger().warning("No tool adapter to handle: "+request);
		}
	}
}
