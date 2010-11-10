package jadex.model;


/**
 *  Interface for message events.
 */
public interface IMMessageEvent extends IMEvent
{
	//-------- constants --------

	/** Describing a message event that can be received. */
	public static final String	DIRECTION_RECEIVE = "receive";

	/** Describing a message event that can be send. */
	public static final String	DIRECTION_SEND = "send";

	/** Describing a message event that can be send and received. */
	public static final String	DIRECTION_SEND_RECEIVE = "send_receive";


	//-------- direction --------

	/**
	 *  Get the direction (send/receive).
	 *  @return The direction of the message.
	 */
	public String	getDirection();

	/**
	 *  Set the direction (send/receive).
	 *  @param direction	The direction of the message.
	 */
	public void	setDirection(String direction);


	//-------- type --------

	/**
	 *  Get the message type (e.g. "fipa").
	 *  @return The type of the message.
	 */
	public String	getType();

	/**
	 *  Set the message type (e.g. "fipa").
	 *  @param type	The type of the message.
	 */
	public void	setType(String type);

	//-------- match expression --------

	/**
	 *  Get the parameters.
	 *  @return The parameters.
	 */
	public IMExpression	getMatchExpression();

	/**
	 *  Create a new parameter.
	 *  @param match	The match expression.
	 *  @return	The newly created match expression.
	 */
	public IMExpression	createMatchExpression(String match);

	/**
	 *  Delete a parameter.
	 */
	public void	deleteMatchExpression();

	//-------- not xml related --------

	/**
	 *  Get the specialization degree.
	 */
	public int getSpecializationDegree();

	/**
	 *  Get the message type object.
	 *  @return The type specification object of the message.
	 */
	public MessageType	getMessageType();

	/**
	 *  Get the user defined parameters.
	 *  Does not return the additional type parameters.
	 *  @return All declared parameters.
	 */
	public IMParameter[] getDeclaredParameters();

	/**
	 *  Get the user defined parameter sets.
	 *  Does not return the additional type parameter sets.
	 *  @return All declared parameter sets.
	 */
	public IMParameterSet[] getDeclaredParameterSets();
}
