package jadex.examples.marsworld;

import jadex.util.SReflect;

/**
 *  The agents homebase.
 */
public class Homebase extends LocationObject
{
	//-------- attributes --------

	/** The currently collected ore amount. */
	protected int ore;

	/** The mission time. */
	protected long missiontime;

	/** The start time. */
	protected long starttime;

	//-------- constructors --------

	/**
	 *  Create a new homebase.
	 */
	public Homebase(Location loc, long missiontime)
	{
		super("homebase", loc);
		this.missiontime = missiontime;
		this.starttime = System.currentTimeMillis();
	}

	/**
	 *  Retrieve some ore amount.
	 *  @param amount The amount.
	 */
	public void deliverOre(int amount)
	{
		this.ore += amount;
	}

	/**
	 *  Get the amount of ore.
	 *  @return The amount of Ore
	 */
	public int getOre()
	{
		return this.ore;
	}

	/**
	 *  Get the mission time.
	 */
	public long getMissionTime()
	{
		return this.missiontime;
	}

	/**
	 *  Get the remaining mission time.
	 */
	public long getRemainingMissionTime()
	{
		return Math.max(0, starttime+missiontime-System.currentTimeMillis());
		//return starttime+missiontime-System.currentTimeMillis();
	}

	/** Getter for missiontime
	 * @return Returns missiontime.
	 */
	public long getMissiontime()
	{
		return this.missiontime;
	}

	/** Setter for missiontime.
	 * @param missiontime The Homebase.java value to set
	 */
	public void setMissiontime(long missiontime)
	{
		this.missiontime = missiontime;
	}

	/** Getter for starttime
	 * @return Returns starttime.
	 */
	public long getStarttime()
	{
		return this.starttime;
	}

	/** Setter for starttime.
	 * @param starttime The Homebase.java value to set
	 */
	public void setStarttime(long starttime)
	{
		this.starttime = starttime;
	}

	/** Setter for ore.
	 * @param ore The Homebase.java value to set
	 */
	public void setOre(int ore)
	{
		this.ore = ore;
	}

	/**
	 *  Convert the Location to a string representation.
	 */
	public String toString()
	{
		return SReflect.getInnerClassName(getClass())+" oew="+ore;
	}
}
