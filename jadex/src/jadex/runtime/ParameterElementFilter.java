package jadex.runtime;

import jadex.runtime.impl.*;
import jadex.util.SReflect;
import jadex.util.collection.MultiCollection;
import jadex.model.IMExpression;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 *  A parameter element filter filters objects based on their parameter values.
 *  Parameter values of elements can be matched against
 *  required values, or recursively against other filters.
 *  When more than one value and / or filter is
 *  specified for an attribute, only one of those has to match.
 */
public abstract class ParameterElementFilter	implements IFilter,	Serializable
{
	//-------- attributes ---------

	/** The required attribute values. */
	protected MultiCollection values;

	/** The match expression. */
	protected IMExpression match;

	//-------- constructors --------

	/**
	 *  Create an attribute filter.
	 */
	public ParameterElementFilter()
	{
	}

	//-------- methods --------

	/**
	 *  Add a required attribute value.
	 *  The attribute value is checked with equals().
	 *  @param name	The attribute name.
	 *  @param value	The attribute value.
	 */
	public void	addValue(String name, Object value)
	{
		if(values==null)
			this.values	= new MultiCollection();
		values.put(name, value);
	}

	/**
	 *  Add a match expression for a parameter/set.
	 */
	public void setMatchExpression(IMExpression match)
	{
		this.match = match;
	}

	/**
	 *  Match an object against the filter.
	 *  @param object The object.
	 *  @return True, if the filter matches.
	 * @throws Exception
	 */
	public boolean filter(Object object) throws Exception
	{
		if(values==null && match==null)
			return true;

		boolean ret	= values==null;
		if(object instanceof IRParameterElement)
		{
			IRParameterElement	probj	= (IRParameterElement)object;
			// Check, that all properties match.
			if(values!=null)
			{
				Iterator	vkeys	= values.keySet().iterator();

				// Check all values; ret is AND
				ret	= true;
				while(vkeys.hasNext() && ret)
				{
					// Todo: parameter sets???
					String	vkey	= (String)vkeys.next();
					if(!probj.hasParameter(vkey))
					{
						ret	= false;
						break;
					}

					Object	val	= probj.getParameter(vkey).getValue();
					Object[]	vals	= ((Collection)values.get(vkey)).toArray();

					// Has to match at least one; ret is OR.
					ret	= false;
					for(int j=0; val!=null && j<vals.length && !ret; j++)	// ret is OR
					{
						ret	= vals[j].equals(val);
					}
				}
			}

			if(ret && match!=null)
			{
				try
				{
					ret = ((Boolean)RExpression.evaluateExpression(match, probj.getOriginalElement()
						.getExpressionParameters())).booleanValue();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					ret = false;
				}
			}
		}

		return ret;
	}

	/**
	 *  Create a string representation of this filter.
	 *  @return A string representing this filter.
	 */
	public String	toString()
	{
		//return toText();
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(values=[");
		if(values!=null)
		{
			Object[]	vkeys	= values.getKeys();
			for(int i=0; i<vkeys.length; i++)
			{
				//sb.append(vkeys[i]);
				sb.append(vkeys[i]+" = "+values.get(vkeys[i]));
				if(i<vkeys.length-1)
				{
					sb.append(", ");
				}
			}
		}
		sb.append("])");
		return sb.toString();
	}
}
