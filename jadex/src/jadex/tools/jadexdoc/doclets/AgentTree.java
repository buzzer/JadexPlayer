package jadex.tools.jadexdoc.doclets;

import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.Configuration;

/**
 * Build Class Hierarchy for all the Agents. This class builds the Agent
 * Tree and the Capability Tree separately.
 */
public class AgentTree
{
	/**
	 * List of baseagents. Contains only IMBDIAgent. Can be used to get
	 * the mapped listing of sub-capabilities.
	 */
	private List baseagents = new AgentList();

	/**
	 * Mapping for each Agent with their containig capabilities
	 */
	private Map subcapabilities = new AgentMap();

	/**
	 * List of base-capabilities. Contains list of all the capabilities who do not
	 * have containing capabilities. Can be used to get the mapped listing of
	 * super-capabilities.
	 */
	private List basecapabilities = new AgentList();

	/**
	 * Mapping for each Capabilitiy with their SuperCapabilities
	 */
	private Map supercapabilities = new AgentMap();

	/**
	 * Mapping for each Interface with classes who implement it.
	 */
	private Map implementingclasses = new AgentMap();


	/**
	 * Constructor. Build the Tree.
	 * @param configuration the configuration of the doclet.
	 */
	public AgentTree(Configuration configuration)
	{
		configuration.message.notice("doclet.Building_Tree");
		buildTree(configuration.cmdLineAgents);
	}


	/**
	 * Constructor. Build the tree for the given array of agents.
	 * @param agents List of agents.
	 */
	public AgentTree(List agents)
	{
		buildTree(agents);
	}

	/**
	 * Generate mapping for the sub-capabilities for every agent and
	 * the mapping of the super-capabilities for every base-capability.
	 * @param agents all the agents in this run.
	 */
	private void buildTree(List agents)
	{
		for(int i = 0; i<agents.size(); i++)
		{
			processClass((IMCapability)agents.get(i));
			processInterface((IMCapability)agents.get(i));
		}

		Collections.sort(baseagents, new ElementNameComparator());
		Collections.sort(basecapabilities, new ElementNameComparator());
		for(Iterator it = supercapabilities.values().iterator(); it.hasNext();)
		{
			Collections.sort((List)it.next(), new ElementNameComparator());
		}
		for(Iterator it = subcapabilities.values().iterator(); it.hasNext();)
		{
			Collections.sort((List)it.next(), new ElementNameComparator());
		}
	}

	/**
	 * For the capability passed map it to it's own sub-capabilities listing.
	 * @param cd capability for which sub-capabilities mapping to be generated.
	 */
	private void processClass(IMCapability cd)
	{
		if(cd instanceof IMBDIAgent)
		{
			baseagents.add(cd);
		}

		IMCapabilityReference[] caprefs = cd.getCapabilityReferences();
		for(int i = 0; i<caprefs.length; i++)
		{
			IMCapability cap = caprefs[i].getCapability();

			if(!add(subcapabilities, cd, cap))
			{
				return;
			}
			else
			{
				processClass(cap);   // Recurse
			}

		}
	}

	/**
	 * For the capability passed get the containing capabilities.
	 * <p/>
	 * <p/>
	 * If a capability doesn't have sub capabilities just attach
	 * that capability in the list of all the basecapabilities.
	 * @param cd Interface under consideration.
	 */
	private void processInterface(IMCapability cd)
	{
		boolean containing = false;

		IMCapabilityReference[] intfacs = cd.getCapabilityReferences();

		if(intfacs.length>0)
		{
			for(int i = 0; i<intfacs.length; i++)
			{
				IMCapability cap = intfacs[i].getCapability();

				if(!add(supercapabilities, cap, cd))
				{
					return;
				}
				else
				{
					processInterface(cap);   // Recurse
				}
			}
		}
		else if(!(cd instanceof IMBDIAgent))
		{
			// we need to add all the capabilities who do not have
			// sub-capabilities to basecapabilities list to traverse them
			/*
			for (int i = 0; i < basecapabilities.size(); i++) {
				IMCapability capability = (IMCapability) basecapabilities.get(i);
				if (capability.getName().equals(cd.getName())) {
					containing = true;
				}
			}
			if (!containing) {
			*/

			if(!basecapabilities.contains(cd))
			{
				basecapabilities.add(cd);
			}
		}
	}

	/**
	 * Adjust the Agent Tree. Add the agents capabilities  in to it's
	 * super-capabilities' or  sub-capabilities' list.
	 * @param map the entire map.
	 * @param supercap super-capability.
	 * @param cd sub-capability to be mapped.
	 * @return boolean true if capability added, false if capability already processed.
	 */
	private boolean add(Map map, IMCapability supercap, IMCapability cd)
	{
		List list = (List)map.get(supercap);
		/*
		List list=null;
		boolean containing=false;

		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			IMCapability capability = (IMCapability) iterator.next();
			if (capability.getName().equals(supercap.getName())) {
				list = (List)map.get(capability);
				for (int i = 0; i < list.size(); i++) {
					IMCapability imCapability = (IMCapability) list.get(i);
					if (imCapability.getName().equals(cd.getName())) {
						containing=true;
					}

				}
			}
		}
		*/
		if(list==null)
		{
			//            list = new ArrayList();
			list = new AgentList();
			map.put(supercap, list);
		}
		//        if (containing) {

		if(list.contains(cd))
		{
			return false;
		}
		else
		{
			list.add(cd);
		}
		return true;
	}

	/**
	 * From the map return the list of sub-capabilities or super-capabilities. If list
	 * is null create a new one and return it.
	 * @param map The entire map.
	 * @param cd capability for which the sub-capability list is requested.
	 * @return List Sub-capability list for the capability passed.
	 */
	private List get(Map map, IMCapability cd)
	{
		List list = (List)map.get(cd);
		/*
		List list=null ;
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			IMCapability capability = (IMCapability) iterator.next();
			if (capability.getName().equals(cd.getName())) {
				list = (List)map.get(capability);
			}
		}
		*/
		if(list==null)
		{
			//            return new ArrayList();
			return new AgentList();
		}
		return list;
	}

	/**
	 * Return the sub-capabilities list for the capability passed.
	 * @param cd capability whose sub-capability list is required.
	 */
	public List subcapabilities(IMCapability cd)
	{
		return get(subcapabilities, cd);
	}

	/**
	 * Return the super-capability list for the capability passed.
	 * @param cd capability whose super-capability list is required.
	 */
	public List supercapabilities(IMCapability cd)
	{
		return get(supercapabilities, cd);
	}

	/**
	 * Return the list of agents which implement the capability passed.
	 * @param cd capability whose implementing-agents list is required.
	 */
	public List implementingagents(IMCapability cd)
	{
		List result = get(implementingclasses, cd);
		List subinterfaces = allSubs(cd);

		//If class x implements a subinterface of cd, then it follows
		//that class x implements cd.
		Iterator implementingClassesIter, subInterfacesIter = subinterfaces.listIterator();
		IMCapability c;
		while(subInterfacesIter.hasNext())
		{
			implementingClassesIter = implementingagents((IMCapability)
					subInterfacesIter.next()).listIterator();
			while(implementingClassesIter.hasNext())
			{
				c = (IMCapability)implementingClassesIter.next();
				if(!result.contains(c))
				{
					result.add(c);
				}
			}
		}
		return result;
	}


	/**
	 * Return a list of all direct or indirect, sub-classes and subinterfaces
	 * of the ClassDoc argument.
	 * @param cd ClassDoc whose sub-classes or sub-interfaces are requested.
	 */
	public List allSubs(IMCapability cd)
	{
		List list = get((cd instanceof IMBDIAgent)? subcapabilities: supercapabilities, cd);
		for(int i = 0; i<list.size(); i++)
		{
			cd = (IMCapability)list.get(i);
			List tlist = get((cd instanceof IMBDIAgent)? subcapabilities: supercapabilities, cd);
			for(int j = 0; j<tlist.size(); j++)
			{
				IMCapability tcd = (IMCapability)tlist.get(j);
				if(!list.contains(tcd))
				{
					list.add(tcd);
				}
			}
		}
		Collections.sort(list, new ElementNameComparator());
		return list;
	}

	/**
	 * Return the base-agents list.
	 */
	public List baseagents()
	{
		return baseagents;
	}

	/**
	 * Return the list of base capabilities. This is the list of capabilities
	 * which do not have contaioning capabilities.
	 */
	public List basecapabilities()
	{
		return basecapabilities;
	}

	/**
	 *
	 */
	private static class AgentList extends ArrayList
	{
		public boolean contains(Object elem)
		{
			boolean ret = super.contains(elem);

			if(ret==true)
			{
				return true;
			}
			else
			{
				for(int i = 0; i<size(); i++)
				{
					String name1 = ((IMCapability)elem).getFullName();
					String name2 = ((IMCapability)get(i)).getFullName();
					if(name1.equals(name2) && elem.getClass().equals(get(i).getClass()))
						return true;
				}
			}
			return false;
		}

		/*private String getQualifiedAgentName(IMCapability agent)
		{
			String pck = agent.getPackage();
			if(pck!=null && pck.length()>0)
			{
				return agent.getPackage()+"."+agent.getName();
			}
			else
			{
				return agent.getName();
			}
		}*/

	}

	/**
	 *
	 */
	private static class AgentMap extends HashMap
	{
		public Object get(Object key)
		{
			Object ret = super.get(key);

			if(ret!=null)
			{
				return ret;
			}
			else
			{
				Set keyset = super.keySet();
				for(Iterator iterator = keyset.iterator(); iterator.hasNext();)
				{
					Object k = iterator.next();
					String name1 = ((IMCapability)key).getFullName();
					String name2 = ((IMCapability)k).getFullName();
					if(name1.equals(name2) && key.getClass().equals(k.getClass()))
						return super.get(k);
				}

			}
			return null;
		}

		/**
		 *
		 * @param agent
		 * @return
		 * /
		private String getQualifiedAgentName(IMCapability agent)
		{
			String pck = agent.getPackage();
			if(pck!=null && pck.length()>0)
			{
				return agent.getPackage()+"."+agent.getName();
			}
			else
			{
				return agent.getName();
			}

		}*/
	}
}
