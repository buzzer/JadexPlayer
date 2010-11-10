package jadex.tools.jadexdoc.doclets;

import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.Configuration;

/**
 * Build the mapping of each Unicode character with it's member lists
 * containing members names starting with it. Also build a list for all the
 * Unicode characters which start a member name. Member name is
 * agent or belief or goal or plan name.
 */
public class IndexBuilder
{

	/**
	 * Mapping of each Unicode Character with the member list containing
	 * members with names starting with it.
	 */
	private Map indexmap = new HashMap();

	/**
	 * Configuration
	 */
	private Configuration configuration;

	/**
	 * Build this Index only for bdiagents not for capabilities?
	 */
	private boolean agentsOnly;

	// array of members
	protected final Object[] elements;


	/**
	 * Constructor. Build the index map.
	 */
	public IndexBuilder(Configuration configuration)
	{
		this(configuration, false);
	}

	/**
	 * Constructor. Build the index map.
	 * @param agentsOnly Include only classes in index.
	 */
	public IndexBuilder(Configuration configuration, boolean agentsOnly)
	{
		if(agentsOnly)
		{
			configuration.message.notice("doclet.Building_Index_For_All_Agents");
		}
		else
		{
			configuration.message.notice("doclet.Building_Index");
		}

		this.configuration = configuration;
		this.agentsOnly = agentsOnly;
		buildIndexMap();
		Set set = indexmap.keySet();
		elements = set.toArray();
		Arrays.sort(elements);
	}

	/**
	 * Sort the index map. Traverse the index map for all it's elements and
	 * sort each element which is a list.
	 */
	protected void sortIndexMap()
	{
		for(Iterator it = indexmap.values().iterator(); it.hasNext();)
		{
			Collections.sort((List)it.next(), new ElementNameComparator());
		}
	}

	/**
	 * Get all the members in all the Packages and all the Agents
	 * given on the command line. Form separate list of those members depending
	 * upon their names.
	 */
	protected void buildIndexMap()
	{
		String[] packages = configuration.specifiedPackages();
		IMCapability[] classes = configuration.getSpecifiedAgents();
		List list = new ArrayList();
		if(!agentsOnly)
		{
			// Packages are skipped for index
			//adjustIndexMap(packages);
		}
		adjustIndexMap(classes);
		if(!agentsOnly)
		{
			for(int i = 0; i<classes.length; i++)
			{
				putMembersInIndexMap(classes[i]);
			}
		}
		sortIndexMap();
	}

	/**
	 * Put all the members(beliefs, goals and plans) of the agent
	 * or capability to the indexmap.
	 * @param agent Agent or Capability whose members will be added to the indexmap.
	 */
	protected void putMembersInIndexMap(IMCapability agent)
	{
		IMReferenceableElement[] refelements = agent.getGoalbase().getReferenceableElements();
		List members = new ArrayList(Arrays.asList(refelements));
		// Hack: dont include dummy goal and metalevel_reasoning_goal
		for(int i = members.size()-1; i>=0; i--)
		{
			IMElement goal = (IMElement)members.get(i);
			if(goal.getName().equals("dummy_goal") ||
					goal.getName().equals("metalevel_reasoning_goal"))
			{
				members.remove(goal);
			}
		}
		adjustIndexMap((IMElement[])members.toArray(new IMElement[members.size()]));

		//   adjustIndexMap(agent.getGoalbase().getReferenceableElements());
		adjustIndexMap(agent.getBeliefbase().getReferenceableElements());
		adjustIndexMap(agent.getPlanbase().getReferenceableElements());
		if(configuration.expressions)
		{
			adjustIndexMap(agent.getExpressionbase().getReferenceableElements());
		}
		if(configuration.events)
		{
			adjustIndexMap(agent.getEventbase().getReferenceableElements());
		}
	}


	/**
	 * Adjust list of members according to their names. Check the first
	 * character in a member name, and then add the member to a list of members
	 * for that particular unicode character.
	 * @param elements Array of members.
	 */
	protected void adjustIndexMap(IMElement[] elements)
	{
		for(int i = 0; i<elements.length; i++)
		{

			String name = elements[i].getName();
			char ch = (name.length()==0)?
					'*':
					Character.toUpperCase(name.charAt(0));
			Character unicode = new Character(ch);
			List list = (List)indexmap.get(unicode);
			if(list==null)
			{
				list = new ArrayList();
				indexmap.put(unicode, list);
			}
			list.add(elements[i]);

		}
	}


	/**
	 * Return a map of all the individual member lists with Unicode character.
	 * @return Map index map.
	 */
	public Map getIndexMap()
	{
		return indexmap;
	}

	/**
	 * Return the sorted list of members, for passed Unicode Character.
	 * @param index index Unicode character.
	 * @return List member list for specific Unicode character.
	 */
	public List getMemberList(Character index)
	{
		return (List)indexmap.get(index);
	}

	/**
	 * Array of IndexMap keys, Unicode characters.
	 */
	public Object[] elements()
	{
		return elements;
	}
}
