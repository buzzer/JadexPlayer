package jadex.runtime.impl;

import java.util.List;

/**
 *	Base interface for all referenceable runtime elements.
 */
public interface IRReferenceableElement	extends IRElement
{
	/**
	 *  Get all occurrences of this element
	 *  (including the original element, direct references,
	 *  and all indirect references).
	 */
	public List getAllOccurrences();

	/**
	 *  Get the original goal of the given goal.
	 *  Resolves all references (if any).
	 */
	public RReferenceableElement	getOriginalElement();
}
