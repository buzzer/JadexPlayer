package jadex.tools.ontology;

import jadex.adapter.fipa.AgentAction;
import jadex.adapter.fipa.AgentIdentifier;
import jadex.tools.common.IToolPanel;

public class Log extends AgentAction implements java.beans.BeanInfo 
{
	//-------- constants ----------

	//-------- attributes ----------

	/** A tool type such as "tracer". */
	protected  String  tooltype;
	
	//-------- constructors --------

	/**
	 *  Default Constructor. <br>
	 *  Create a new <code>Log</code>.
	 */
	public Log()  {
		System.out.println("Created " + this);
	}

	//-------- accessor methods --------
			
	/**
	 *  Get the tool-type of this Log.
	 *  A tool type such as "tracer".
	 * @return tool-type
	 */
	public String  getToolType() {
		return this.tooltype;
	}

	/**
	 *  Set the tool-type of this Log.
	 *  A tool type such as "tracer".
	 * @param tooltype the value to be set
	 */
	public void  setToolType(String tooltype) {
		this.tooltype = tooltype;
	}

	//-------- bean related methods --------

	/** The property descriptors, constructed on first access. */
	private java.beans.PropertyDescriptor[] pds = null;

	/**
	 *  Get the bean descriptor.
	 *  @return The bean descriptor.
	 */
	public java.beans.BeanDescriptor getBeanDescriptor() {
		return null;
	}

	/**
	 *  Get the property descriptors.
	 *  @return The property descriptors.
	 */
	public java.beans.PropertyDescriptor[] getPropertyDescriptors() {
		if(pds==null) {
			try {
				pds = new java.beans.PropertyDescriptor[]{
					 new java.beans.PropertyDescriptor("toolType", this.getClass(), "getToolType", "setToolType")
				};
				java.beans.PropertyDescriptor[] spds = super.getPropertyDescriptors();
				int	l1	= pds.length;
				int	l2	= spds.length;
				Object	res	= java.lang.reflect.Array.newInstance(java.beans.PropertyDescriptor.class, l1+l2);
				System.arraycopy(pds, 0, res, 0, l1);
				System.arraycopy(spds, 0, res, l1, l2);
				pds = (java.beans.PropertyDescriptor[])res;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return pds;
	}

	/**
	 *  Get the default property index.
	 *  @return The property index.
	 */
	public int getDefaultPropertyIndex() {
		return -1;
	}

	/**
	 *  Get the event set descriptors.
	 *  @return The event set descriptors.
	 */
	public java.beans.EventSetDescriptor[] getEventSetDescriptors() {
		return null;
	}

	/**
	 *  Get the default event index.
	 *  @return The default event index.
	 */
	public int getDefaultEventIndex() {
		return -1;
	}

	/**
	 *  Get the method descriptors.
	 *  @return The method descriptors.
	 */
	public java.beans.MethodDescriptor[] getMethodDescriptors() {
		return null;
	}

	/**
	 *  Get additional bean info.
	 *  @return Get additional bean info.
	 */
	public java.beans.BeanInfo[] getAdditionalBeanInfo() {
		return null;
	}

	/**
	 *  Get the icon.
	 *  @return The icon.
	 */
	public java.awt.Image getIcon(int iconKind) {
		return null;
	}

	/**
	 *  Load the image.
	 *  @return The image.
	 */
	public java.awt.Image loadImage(final String resourceName) {
		try {
			final Class c = getClass();
			java.awt.image.ImageProducer ip = (java.awt.image.ImageProducer)
				java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
					public Object run(){
						java.net.URL url;
						if((url = c.getResource(resourceName))==null) {
							return null;
						}
						else {
							try {
								return url.getContent();
							}
							catch(java.io.IOException ioe) {
								return null;
							}
						}
					}
				});
			if(ip==null)
				return null;
			java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
			return tk.createImage(ip);
		}
		catch(Exception ex) {
			return null;
		}
	}

	//-------- additional methods --------

	/**
	 *  Get a string representation of this Log.
	 *  @return The string representation.
	 */
	public String toString() {
		return "Log("
		+ "tooltype="+getToolType()
           + ")";
	}
}
