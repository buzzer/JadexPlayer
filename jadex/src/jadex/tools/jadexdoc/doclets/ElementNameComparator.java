package jadex.tools.jadexdoc.doclets;

import java.util.Comparator;
import jadex.model.IMElement;


/**
 * Compare model elements by name.
 */
public class ElementNameComparator implements Comparator
{
	/**
	 * Compare two model elements by name.
	 * @param o1 The first model element.
	 * @param o2 The second model element.
	 * @return	The comparison result.
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2)
	{
		if(o1 instanceof IMElement && o2 instanceof IMElement)
			return ((IMElement)o1).getName().compareTo(((IMElement)o2).getName());
		else
			throw new ClassCastException("Not IMElement "+o1.getClass()+", "+o2.getClass());
	}
}
