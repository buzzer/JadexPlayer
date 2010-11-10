package jadex.tools.testcenter;

import java.io.File;
import java.io.IOException;

import jadex.config.Configuration;
import jadex.model.IMBDIAgent;
import jadex.model.IMCapabilityReference;
import jadex.model.SXML;
import jadex.tools.common.modeltree.FileNode;
import jadex.tools.common.modeltree.IExplorerTreeNode;
import jadex.tools.common.modeltree.INodeFunctionality;
import jadex.util.SGUI;

import javax.swing.Icon;
import javax.swing.UIDefaults;

/**
 *
 */
public class FileNodeFunctionality implements INodeFunctionality
{
	//-------- constants --------

	/**
	 * The image  for (m/r) elements.
	 */
	static UIDefaults icons = new UIDefaults(new Object[]
	{
		"agent", SGUI.makeIcon(FileNode.class, "/jadex/tools/common/images/new_agent.png"),
		"agent_testable", SGUI.makeIcon(FileNode.class, "/jadex/tools/common/images/new_agent_testable.png"),
	});
	
	/**
	 *  Check if the node is valid.
	 *  @return True, is valid.
	 */
	public boolean check(IExplorerTreeNode node)
	{
		boolean	valid	= false;
		FileNode fn = (FileNode)node; 
		String	file	= fn.getFile().getAbsolutePath();
		if(SXML.isJadexFilename(file))
		{
			try
			{
				// todo: remove Hack! Let tree always load fresh models when autorefresh is off
				if(Configuration.getConfiguration().isModelCaching() && !Configuration.getConfiguration().isModelCacheAutoRefresh())
					SXML.clearModelCache(file);
				
				IMBDIAgent	model = null;
				if(SXML.isAgentFilename(file))
					model	= SXML.loadAgentModel(file, null);
				
				if(model!=null)
				{
					boolean ok	= !jadex.config.Configuration.getConfiguration().isModelChecking()
						|| model.getReport().isEmpty();

					if(ok)
					{
						IMCapabilityReference[] caprefs = model.getCapabilityReferences();
						for(int i=0; !valid && i<caprefs.length; i++)
						{
							String name = caprefs[i].getCapability().getFullName();
							valid = name.equals("jadex.planlib.Test");
						}
					}
				}
				// else unknown jadex file type -> ignore.
			}
			catch(IOException e)
			{
			}
		}

		return valid;
	}
	
	/**
	 *  Perform the actual refresh.
	 *  Can be overridden by subclasses.
	 *  @return true, if the node has changed and needs to be checked.
	 */
	public boolean refresh(IExplorerTreeNode node)
	{
		FileNode fn = (FileNode)node;
		boolean	changed	= false;
		long	newdate	= fn.getFile().lastModified();
		if(fn.getLastmodified()<newdate)
		{
			fn.setLastmodified(newdate);
			changed	= true;
		}
		
		return changed;
	}
	
	/**
	 *  Get the icon.
	 *  @return The icon.
	 */
	public Icon getIcon(IExplorerTreeNode node)
	{
		Icon	icon	= null;
		FileNode fn = (FileNode)node;
		if(SXML.isAgentFilename(fn.getFile().getName()))
		{
			icon	= icons.getIcon(fn.isValid()? "agent_testable": "agent");
		}
		return icon;
	}
	
	/**
	 *  Create a new child node.
	 *  @param file The file for the new child node.
	 *	@return The new node.
	 */
	public IExplorerTreeNode createNode(IExplorerTreeNode node, File file)
	{
		return null;
	}
}
