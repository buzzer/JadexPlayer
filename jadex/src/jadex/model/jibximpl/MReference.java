package jadex.model.jibximpl;

import jadex.model.*;
import jadex.util.SReflect;
import jadex.util.collection.SCollection;

import java.util.*;

/**
 *  A reference used in MTrigger.
 */
public class MReference extends MElement implements IMReference
{
	//-------- xml attributes --------

	/** The reference parameters. */
	protected ArrayList parameters;

	/** The reference. */	
	protected String reference;

	/** The match expression. */
	protected MExpression match;

	//-------- methods --------

	/**
	 *  Method called on first setup run.
	 *  Overwritten in elements that have
	 *  child elements.
	 *  @return The list of direct child elements.
	 */
	public List	getChildren()
	{
		List ret = super.getChildren();
		if(getMatchExpression()!=null)
			ret.add(getMatchExpression());
		return ret;
	}
	
	/**
	 *  Do the check for this element.
	 *  @param report	The current check report.
	 */
	protected void doCheck(Report report)
	{
		super.doCheck(report);
		
		if(match!=null)
		{
			match.checkClass(Boolean.class, report);
		}
	}

	//-------- reference --------

	/**
	 *  Get the reference to the element.
	 *  @return	The inhibited goal type.
	 */
	public String	getReference()
	{
		return this.reference;
	}

	/**
	 *  Set the reference to the element.
	 *  @param ref	The inhibited goal type.
	 */
	public void	setReference(String ref)
	{
		this.reference = ref;
	}

	//-------- match expression --------

	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 */
	public IMExpression	getMatchExpression()
	{
		return match;
	}

	/**
	 *  Create a new parameter.
	 *  @param match	The match expression.
	 *  @return	The newly created match expression.
	 */
	public IMExpression	createMatchExpression(String match)
	{
		MExpression ret = new MExpression();
		ret.setName(name);
		ret.setExpressionText(match);
		ret.setClazz(Boolean.class);
		ret.setEvaluationMode(IMExpression.MODE_DYNAMIC);
		ret.setExported(IMReferenceableElement.EXPORTED_FALSE);
		ret.setOwner(this);
		this.match = ret;
		return ret;
	}

	/**
	 *  Delete a parameter.
	 */
	public void	deleteMatchExpression()
	{
		match = null;
	}

	//-------- parameters --------

	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 */
	public IMReferenceParameter[]	getParameters()
	{
		if(parameters==null)
			return new IMReferenceParameter[0];
		return (IMReferenceParameter[])parameters
			.toArray(new IMReferenceParameter[parameters.size()]);
	}

	/**
	 *  Create a new parameter.
	 *  @param ref	The name of the referenced parameter.
	 *  @param expression	The value expression.
	 *  @return	The newly created parameter.
	 */
	public IMReferenceParameter	createInitialParameter(String ref, String expression)
	{
		if(parameters==null)
			parameters = SCollection.createArrayList();

		MReferenceParameter ret = new MReferenceParameter();
		ret.setReference(ref);
		if(expression!=null)
			ret.createValue(expression);
		ret.setOwner(this);
		ret.init();
		parameters.add(ret);
		return ret;
	}

	/**
	 *  Delete a parameter.
	 *  @param parameter	The parameter to delete.
	 */
	public void	deleteInitialParameter(IMReferenceParameter parameter)
	{
		if(!parameters.remove(parameter))
			throw new RuntimeException("Element not found: "+parameter);
	}

	//-------- jibx related --------

	/**
	 *  Add a parameter.
	 *  @param parameter The parameter.
	 */
	public void addParameter(MReferenceParameter parameter)
	{
		if(parameters==null)
			parameters = SCollection.createArrayList();
		parameters.add(parameter);
	}

	/**
	 *  Get an iterator for all parameters.
	 *  @return The iterator.
	 */
	public Iterator iterParameters()
	{
		return parameters==null? Collections.EMPTY_LIST.iterator(): parameters.iterator();
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MReference clone = (MReference)cl;
		if(parameters!=null)
		{
			clone.parameters = SCollection.createArrayList();
			for(int i=0; i<parameters.size(); i++)
				clone.parameters.add(((MElement)parameters.get(i)).clone());
		}
		if(match!=null)
			clone.match = (MExpression)match.clone();
	}

	/**
	 *  Create a string representation of this element.
	 */
	public String	toString()
	{
		// Overridden as getName() assert name not null.
		// Use reference as fallback, when name is null.
		String	name	= this.name!=null ? getName() : getReference();
		return SReflect.getInnerClassName(this.getClass())+"("+name+")";
	}
}
