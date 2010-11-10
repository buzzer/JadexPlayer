package jadex.model.jibximpl;

import java.util.List;
import jadex.model.*;

/**
 *  A parameter of a reference.
 */
public class MReferenceParameter extends MElement implements IMReferenceParameter
{
	//-------- xml attributes --------

	/** The reference. */
	protected String reference;

	/** The value. */
	protected MExpression value;
	
	//-------- constructors --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(value!=null)
			ret.add(value);
		return ret;
	}

	//-------- reference --------

	/**
	 *  Get the reference to the inhibited goal type.
	 *  @return	The inhibited goal type.
	 */
	public String	getReference()
	{
		return reference;
	}

	/**
	 *  Set the reference to the inhibited goal type.
	 *  @param ref	The inhibited goal type.
	 */
	public void	setReference(String ref)
	{
		this.reference = ref;
	}


	//-------- value --------

	/**
	 *  Get the value of the parameter.
	 *  @return The value.
	 */
	public IMExpression	getValue()
	{
		return this.value;
	}

	/**
	 *  Create a value for the parameter.
	 *  @param expression	The expression string.
	 *  @return The new value.
	 */
	public IMExpression	createValue(String expression)
	{
		assert expression!=null : this;
		this.value = new MExpression();
		value.setExpressionText(expression);
		value.setOwner(this);
		value.init();
		return value;
	}

	/**
	 *  Delete the value of the parameter.
	 */
	public void	deleteValue()
	{
		value = null;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MReferenceParameter clone = (MReferenceParameter)cl;
		if(value!=null)
			clone.value = (MExpression)value.clone();
	}
}
