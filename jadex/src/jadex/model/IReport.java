package jadex.model;

import java.util.*;
import java.io.Serializable;

/**
 *  The interface for an error report.
 */
public interface IReport extends Serializable
{
	/**
	 *  Check if this report is empty (i.e. the model is valid).
	 *  @return True, if empty.
	 */
	public boolean	isEmpty();
//
//	/**
//	 *  Add an entry to the report.
//	 *  @param element	The element to which the entry applies.
//	 *  @param message	The problem description.
//	 */
//	public void	addEntry(IMElement element, String message);

	/**
	 *  Get all invalid elements.
	 *  @return All invalid elements.
	 */
	public IMElement[]	getElements();

	/**
	 *  Get the messages for a given element.
	 *  @return All messages.
	 */
	public String[]	getMessages(IMElement element);

	/**
	 *  Generate an html representation of the report.
	 *  @return The html string.
	 */
	public String	toHTMLString();

//	/**
//	 *  Get beliefs, belief sets, references, and initial elements which have errors,
//	 * 	or contain elements with errors.
//	 *  @return The beliefbase errors.
//	 */
//	public IMElement[]	getBeliefbaseErrors();
//
//
//	/**
//	 *  Get goals, references, and initial elements which have errors,
//	 *  or contain elements with errors.
//	 *  @return The goalbase errors.
//	 */
//	public IMElement[]	getGoalbaseErrors();
//
//	/**
//	 *  Get plans, and initial elements which have errors, or contain elements with errors.
//	 *  @return The planbase errors.
//	 */
//	public IMElement[]	getPlanbaseErrors();
//
//	/**
//	 *  Get events, references, and initial elements which have errors, or contain elements with errors.
//	 *  @return The eventbase errors.
//	 */
//	public IMElement[]	getEventbaseErrors();
//
//	/**
//	 *  Get other errors, not in belief goal and plan base.
//	 *  @return All other errors.
//	 */
//	public IMElement[]	getOtherErrors();
//
//	/**
//	 *  Get all elements which have errors and are contained in the given element.
//	 *  @return All contained error elements.
//	 */
//	public IMElement[]	getElementErrors(IMElement ancestor);
//
//	/**
//	 *  Add an external document.
//	 *  @param id	The document id as used in anchor tags.
//	 *  @param doc	The html text.
//	 */
//	public void	addDocument(String id, String doc);

	/**
	 *  Get the external documents.
	 *  @return The external documents.
	 */
	public Map	getDocuments();

	/**
	 *  Get the total number of errors.
	 *  @return The total number of errors.
	 */
	public int getErrorCount();
}
