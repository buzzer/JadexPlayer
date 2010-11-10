/*
 * OMessage.java
 *
 * Generated by Protege plugin Beanynizer. 
 * Changes will be lost! 
 */
package jadex.tools.ontology;



/**
 *  Java class for concept OMessage of jadex.tools.tracer ontology.
 */
public class OMessage	extends OTrace implements java.beans.BeanInfo 
{
	//-------- constants ----------

	//-------- attributes ----------

	/** Attribute for slot to. */
	protected  java.util.List  to;

	/** Attribute for slot from. */
	protected  String  from;

	/** Attribute for slot incoming. */
	protected  boolean  incoming;

	//-------- constructors --------

	/**
	 *  Default Constructor. <br>
	 *  Create a new <code>OMessage</code>.
	 */
	public OMessage()  {
		this.to  = new java.util.ArrayList();
	}

	/**
	 *  Init Constructor. <br>
	 *  Create a new OMessage.<br>
	 *  Initializes the object with required attributes.
	 * @param from
	 * @param incoming
	 * @param name
	 * @param seq
	 * @param thread
	 * @param time
	 * @param value
	 */
	public OMessage(String from, boolean incoming, String name, String seq, String thread, String time, String value)  {
		this();
		setFrom(from);
		setIncoming(incoming);
		setName(name);
		setSeq(seq);
		setThread(thread);
		setTime(time);
		setValue(value);
	}

	//-------- accessor methods --------

	/**
	 *  Get the to of this OMessage.
	 * @return to
	 */
	public String[]  getTo() {
		return (String[])to.toArray(new String [to.size()]);
	}

	/**
	 *  Set the to of this OMessage.
	 * @param to the value to be set
	 */
	public void  setTo(String[] to) {
		this.to.clear();
		for(int i=0; i<to.length; i++)
			this.to.add(to[i]);
	}

	/**
	 *  Get an to of this OMessage.
	 *  @param idx The index.
	 *  @return to
	 */
	public String  getTo(int idx) {
		return (String)this.to.get(idx);
	}

	/**
	 *  Set a to to this OMessage.
	 *  @param idx The index.
	 *  @param to a value to be added
	 */
	public void  setTo(int idx, String to) {
		this.to.set(idx, to);
	}

	/**
	 *  Add a to to this OMessage.
	 *  @param to a value to be removed
	 */
	public void  addTo(String to)  {
		this.to.add(to);
	}

	/**
	 *  Remove a to from this OMessage.
	 *  @param to a value to be removed
	 *  @return  True when the to have changed.
	 */
	public boolean  removeTo(String to)  {
		return this.to.remove(to);
	}


	/**
	 *  Get the from of this OMessage.
	 * @return from
	 */
	public String  getFrom() {
		return this.from;
	}

	/**
	 *  Set the from of this OMessage.
	 * @param from the value to be set
	 */
	public void  setFrom(String from) {
		this.from = from;
	}

	/**
	 *  Get the incoming of this OMessage.
	 * @return incoming
	 */
	public boolean  isIncoming() {
		return this.incoming;
	}

	/**
	 *  Set the incoming of this OMessage.
	 * @param incoming the value to be set
	 */
	public void  setIncoming(boolean incoming) {
		this.incoming = incoming;
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
					 new java.beans.IndexedPropertyDescriptor("to", this.getClass(),
						"getTo", "setTo", "getTo", "setTo")
					, new java.beans.PropertyDescriptor("from", this.getClass(), "getFrom", "setFrom")
					, new java.beans.PropertyDescriptor("incoming", this.getClass(), "isIncoming", "setIncoming")
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
	 *  Get a string representation of this OMessage.
	 *  @return The string representation.
	 */
	public String toString() {
		return "OMessage("
		+ "from="+getFrom()
		+ ", incoming="+isIncoming()
		+ ", name="+getName()
		+ ", seq="+getSeq()
		+ ", thread="+getThread()
		+ ", time="+getTime()
		+ ", value="+getValue()
           + ")";
	}

}