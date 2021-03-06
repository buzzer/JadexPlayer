package jadex.tools.generic;

import jadex.base.gui.componentviewer.libservice.LibServiceBrowser;
import jadex.commons.Future;
import jadex.commons.IFuture;
import jadex.commons.SGUI;
import jadex.commons.service.IService;
import jadex.commons.service.library.ILibraryService;

import javax.swing.Icon;

/**
 *  The library service plugin is used to wrap the library panel as JCC plugin.
 */
public class LibraryServicePlugin extends AbstractServicePlugin
{
	//-------- constants --------

	static
	{
		icons.put("library", SGUI.makeIcon(LibraryServicePlugin.class, "/jadex/tools/common/images/libcenter.png"));
		icons.put("library_sel", SGUI.makeIcon(LibraryServicePlugin.class, "/jadex/tools/common/images/libcenter_sel.png"));
	}

	//-------- methods --------
	
	/**
	 *  Get the service type.
	 *  @return The service type.
	 */
	public Class getServiceType()
	{
		return ILibraryService.class;
	}
	
	/**
	 *  Create the service panel.
	 */
	public IFuture createServicePanel(IService service)
	{
		LibServiceBrowser ret = new LibServiceBrowser();
		ret.init(getJCC(), service);
		return new Future(ret);
	}
	
	/**
	 *  Get the icon.
	 */
	public Icon getToolIcon(boolean selected)
	{
		return selected? icons.getIcon("library_sel"): icons.getIcon("library");
	}
}
