package jadex.runtime;

import jadex.model.IMEventbase;


/**
 *  The interface for all internal events (concrete and referenced).
 */
public interface IInternalEvent extends IEvent
{
	/** The type string for legacy internal events.
	 *  @deprecated Use explicitly defined custom types in ADF. */
	public static final String TYPE_LEGACY	= IMEventbase.LEGACY_INTERNAL_EVENT;

	/** The custom event type parameter for legacy internal events.
	 *  @deprecated Use explicitly defined custom types in ADF. */
	public static final String PARAMETER_LEGACY_TYPE	= IMEventbase.LEGACY_TYPE;

	/** The custom event content parameter for legacy internal events.
	 *  @deprecated Use explicitly defined custom types in ADF. */
	public static final String PARAMETER_LEGACY_CONTENT	= IMEventbase.LEGACY_CONTENT;

	/** The type string for condition triggered events. */
	public static final String TYPE_CONDITION_TRIGGERED	= IMEventbase.TYPE_CONDITION_TRIGGERED;

	/** The type string for condition triggered event references. */
	public static final String TYPE_CONDITION_TRIGGERED_REFERENCE	= IMEventbase.TYPE_CONDITION_TRIGGERED_REFERENCE;

	/** The condition parameter of condition triggered events. */
	public static final String PARAMETER_CONDITION = IMEventbase.CONDITION;

	/** The type string for timeout events. */
	public static final String TYPE_TIMEOUT	= IMEventbase.TYPE_TIMEOUT;

}
