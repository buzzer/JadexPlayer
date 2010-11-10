package jadex.model.jibximpl;

import jadex.model.IMPlanBody;
import jadex.parser.ITerm;

/**
 *  The plan body provides access to the executable code of a plan.
 */
public class MPlanBody extends MExpression implements IMPlanBody
{
	//-------- xml attributes --------

	/** The plan body type. */
	protected String type	= "standard";

	/** The inline state. */
	protected boolean inline;
	
	/** The passed code. */
	protected String passedcode;

	/** The failed code. */
	protected String failedcode;

	/** The aborted code. */
	protected String abortedcode;

	//-------- constructors --------
	
	// Todo: check expression type (ask executor!?)
	
	//-------- type methods --------

	/**
	 *  Get the type (e.g. "mobile").
	 *  @return The type of the plan body.
	 */
	public String	getType()
	{
		return this.type;
	}

	/**
	 *  Set the type (e.g. "mobile").
	 *  @param type	The type of the plan body.
	 */
	public void	setType(String type)
	{
		this.type = type;
	}

	//-------- inline --------

	/**
	 *  Is this an inline plan body.
	 *  @return True, if inline.
	 */
	public boolean	isInline()
	{
		return inline;
	}

	/**
	 *  Set if inline plan body.
	 *  @param inline The inline state.
	 */
	public void	setInline(boolean inline)
	{
		this.inline = inline;
	}

	//-------- passed code --------

	/**
	 *  Get the passed code.
	 *  @return The passed code.
	 */
	public String getPassedCode()
	{
		return passedcode;
	}

	/**
	 *  Set the passed code.
	 *  @param passed The passed code.
	 */
	public void	setPassedCode(String passed)
	{
		this.passedcode = passed;
	}

	//-------- failed code --------

	/**
	 *  Get the failed code.
	 *  @return The failed code.
	 */
	public String getFailedCode()
	{
		return failedcode;
	}

	/**
	 *  Set the failed code.
	 *  @param failed The failed code.
	 */
	public void	setFailedCode(String failed)
	{
		this.failedcode = failed;
	}

	//-------- aborted code --------

	/**
	 *  Get the aborted code.
	 *  @return The aborted code.
	 */
	public String getAbortedCode()
	{
		return abortedcode;
	}

	/**
	 *  Set the aborted code.
	 *  @param aborted The aborted code.
	 */
	public void	setAbortedCode(String aborted)
	{
		this.abortedcode = aborted;
	}

	/**
	 *  todo: remove me. Hack to allow MExpression doCheck() for inline plans.
	 *  todo: check if inline plan body can be parsed!
	 *  Get the term.
	 *  @return The term.
	 */
	public ITerm	getTerm()
	{
		return !isInline()? super.getTerm(): null;
	}
}
