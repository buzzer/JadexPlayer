package jadex.xml.reader;


import jadex.xml.IPostProcessor;
import jadex.xml.TypeInfo;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 *  Interface for object reader handler.
 *  Is called when a tag start is found and an object could be created.
 *  Is called when an end tag is found and an object could be linked to its parent.
 */
public interface IObjectReaderHandler extends IObjectLinker, IBulkObjectLinker
{
	/**
	 *  Create an object for the current tag.
	 *  @param type The object type to create.
	 *  @param root Flag, if object should be root object.
	 *  @param context The context.
	 *  @return The created object (or null for none).
	 */
	public Object createObject(Object typeinfo, boolean root, ReadContext context, Map rawattributes) throws Exception;
	
	/**
	 *  Get the object type
	 *  @param object The object.
	 *  @return The object type.
	 */
	public Object getObjectType(Object object, ReadContext context);
	
	/**
	 *  Convert a content string object to another type of object.
	 */
	public Object convertContentObject(String object, QName tag, ReadContext context) throws Exception;
	
	/**
	 *  Handle the attribute of an object.
	 *  @param object The object.
	 *  @param xmlattrname The attribute name.
	 *  @param attrval The attribute value.
	 *  @param attrinfo The attribute info.
	 *  @param context The context.
	 */
	public void handleAttributeValue(Object object, QName xmlattrname, List attrpath, String attrval, 
		Object attrinfo, ReadContext context) throws Exception;
	
	/**
	 *  Link an object to its parent.
	 *  @param object The object.
	 *  @param parent The parent object.
	 *  @param linkinfo The link info.
	 *  @param tagname The current tagname (for name guessing).
	 *  @param context The context.
	 * /
	public void linkObject(Object object, Object parent, Object linkinfo, QName[] pathname, 
		Object context, ClassLoader classloader, Object root) throws Exception;*/
	
	/**
	 *  Bulk link an object to its parent.
	 *  @param parent The parent object.
	 *  @param children The children objects (link datas).
	 *  @param context The context.
	 *  @param classloader The classloader.
	 *  @param root The root object.
	 * /
	public void bulkLinkObjects(Object parent, List children, Object context, 
		ClassLoader classloader, Object root) throws Exception;*/
	
	/**
	 *  Get the most specific mapping info.
	 *  @param tag The tag.
	 *  @param fullpath The full path.
	 *  @return The most specific mapping info.
	 */
	public TypeInfo getTypeInfo(QName tag, QName[] fullpath, Map rawattributes);
	
	/**
	 *  Get the most specific mapping info.
	 *  @param tag The tag.
	 *  @param fullpath The full path.
	 *  @return The most specific mapping info.
	 */
	public TypeInfo getTypeInfo(Object type, QName[] fullpath, ReadContext context);
	
	/**
	 *  Get the post-processor.
	 *  @return The post-processor
	 */
	public IPostProcessor getPostProcessor(Object object, Object typeinfo);
}
