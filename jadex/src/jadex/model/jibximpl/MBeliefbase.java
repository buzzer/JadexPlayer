package jadex.model.jibximpl;

import java.util.*;
import jadex.model.*;
import jadex.util.*;
import jadex.util.collection.SCollection;

/**
 *  The model of a beliefbase.
 */
public class MBeliefbase extends MBase implements IMBeliefbase
{
	//-------- xml attributes --------

	/** The beliefs. */
	protected ArrayList beliefs;

	/** The beliefsets. */
	protected ArrayList beliefsets;

	/** The belief references. */
	protected ArrayList beliefrefs;

	/** The beliefset references. */
	protected ArrayList beliefsetrefs;

	//-------- xml methods --------

	/**
	 *  Geneal add method for unmarshalling.
	 *  Necessary to support unordered collections :-(
	 *  @param elem The element to add.
	 */
	public void addElement(IMReferenceableElement elem)
	{
		assert elem instanceof IMBelief || elem instanceof IMBeliefSet
			|| elem instanceof IMBeliefReference || elem instanceof IMBeliefSetReference;

		if(elem instanceof IMBelief)
		{
			if(beliefs==null)
				beliefs = SCollection.createArrayList();
			beliefs.add(elem);
		}
		else if(elem instanceof IMBeliefSet)
		{
			if(beliefsets==null)
				beliefsets = SCollection.createArrayList();
			beliefsets.add(elem);
		}
		else if(elem instanceof IMBeliefReference)
		{
			if(beliefrefs==null)
				beliefrefs = SCollection.createArrayList();
			beliefrefs.add(elem);
		}
		else //if(elem instanceof IMBeliefSetReference)
		{
			if(beliefsetrefs==null)
				beliefsetrefs = SCollection.createArrayList();
			beliefsetrefs.add(elem);
		}
	}

	/**
	 *  Geneal add method for marshalling.
	 *  @return Iterator with all elements.
	 */
	public Iterator iterElements()
	{
		return SReflect.getIterator(getReferenceableElements());
	}

	//-------- methods --------

	/**
	 *  Get the element name.
	 *  @return The element name.
	 */
	public String	getName()
	{
		return "beliefbase";
	}
	
	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	public IMBase	getCorrespondingBase(IMCapability scope)
	{
		return scope.getBeliefbase();
	}

	/**
	 *  Get the elements contained in the base.
	 *  @return The elements.
	 */
	public IMReferenceableElement[]	getReferenceableElements()
	{
		Object	ret	= new IMReferenceableElement[0];
		ret	= SUtil.joinArrays(ret, getBeliefs());
		ret	= SUtil.joinArrays(ret, getBeliefSets());
		ret	= SUtil.joinArrays(ret, getBeliefReferences());
		ret	= SUtil.joinArrays(ret, getBeliefSetReferences());
		return (IMReferenceableElement[])ret;
	}

	/**
	 *  Delete a referenceable element per name.
	 *  @param elem The element.
	 */
	public void deleteReferenceableElement(IMReferenceableElement elem)
	{
		assert elem!=null;

		if(elem instanceof IMBelief)
			deleteBelief((IMBelief)elem);
		else if(elem instanceof IMBeliefSet)
			deleteBeliefSet((IMBeliefSet)elem);
		else if(elem instanceof IMBeliefReference)
			deleteBeliefReference((IMBeliefReference)elem);
		else if(elem instanceof IMBeliefSetReference)
			deleteBeliefSetReference((IMBeliefSetReference)elem);
		else
			throw new RuntimeException("Element not belief/set: "+elem);
	}

	//-------- beliefs --------

	/**
	 *  Get all defined beliefs.
	 *  @return The beliefs.
	 */
	public IMBelief[] getBeliefs()
	{
		if(beliefs==null)
			return new IMBelief[0];
		return (IMBelief[])beliefs.toArray(new IMBelief[beliefs.size()]);
	}

	/**
	 *  Get a belief by name.
	 *  Searches the goal in direct subcapabilities,
	 *  when path notation is used (a.name).
	 *  @param name	The belief name.
	 *  @return The belief with that name (if any).
	 */
	public IMBelief	getBelief(String name)
	{
		assert name!=null;
		assert name.indexOf(".")==-1;

		IMBelief ret = null;
		for(int i=0; beliefs!=null && i<beliefs.size() && ret==null; i++)
		{
			IMBelief tmp = (IMBelief)beliefs.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new belief.
	 *  @param name	The belief name.
	 *  @param clazz	The class for facts.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @return	The newly created belief.
	 */
	public IMBelief	createBelief(String name, Class clazz, long updaterate, String exported)
	{
		if(beliefs==null)
			beliefs = SCollection.createArrayList();

		MBelief ret = new MBelief();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setUpdateRate(updaterate);
		ret.setExported(exported);
		ret.setOwner(this);
		ret.init();
		beliefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a belief.
	 *  @param belief	The belief to delete.
	 */
	public void	deleteBelief(IMBelief belief)
	{
		if(!beliefs.remove(belief))
			throw new RuntimeException("Belief model element not found: "+belief);
	}

	//-------- belief sets --------

	/**
	 *  Get all defined belief sets.
	 *  @return The belief sets.
	 */
	public IMBeliefSet[] getBeliefSets()
	{
		if(beliefsets==null)
			return new IMBeliefSet[0];
		return (IMBeliefSet[])beliefsets.toArray(new IMBeliefSet[beliefsets.size()]);
	}

		/**
	 *  Get a belief set by name.
	 *  Searches the goal in subcapabilities,
	 *  when path notation is used (a.name).
	 *  @param name	The belief set name.
	 *  @return The belief set with that name (if any).
	 */
	public IMBeliefSet	getBeliefSet(String name)
	{
		assert name!=null;
		assert name.indexOf(".")==-1;

		IMBeliefSet ret = null;
		for(int i=0; beliefsets!=null && i<beliefsets.size() && ret==null; i++)
		{
			IMBeliefSet tmp = (IMBeliefSet)beliefsets.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new belief set.
	 *  @param name	The belief set name.
	 *  @param clazz	The class for facts.
	 *  @param updaterate	The updaterate (or -1 for none).
	 *  @param exported	Flag indicating if this belief set may be referenced from outside capabilities.
	 *  @return	The newly created belief set.
	 */
	public IMBeliefSet	createBeliefSet(String name, Class clazz, long updaterate, String exported)
	{
		if(beliefsets==null)
			beliefsets = SCollection.createArrayList();

		MBeliefSet ret = new MBeliefSet();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setUpdateRate(updaterate);
		ret.setExported(exported);
		ret.setOwner(this);
		ret.init();
		beliefsets.add(ret);
		return ret;
	}

	/**
	 *  Delete a belief set.
	 *  @param beliefset	The belief set to delete.
	 */
	public void	deleteBeliefSet(IMBeliefSet beliefset)
	{
		if(!beliefsets.remove(beliefset))
			throw new RuntimeException("Beliefset model element not found: "+beliefset);
	}


	//-------- belief references --------

	/**
	 *  Get all belief references.
	 *  @return The belief references.
	 */
	public IMBeliefReference[] getBeliefReferences()
	{
		if(beliefrefs==null)
			return new IMBeliefReference[0];
		return (IMBeliefReference[])beliefrefs.toArray(new IMBeliefReference[beliefrefs.size()]);
	}

	/**
	 *  Get a belief reference.
	 *  @param name The name.
	 *  @return The belief reference.
	 */
	public IMBeliefReference getBeliefReference(String name)
	{
		assert name!=null;
		assert name.indexOf(".")==-1;

		IMBeliefReference ret = null;
		for(int i=0; beliefrefs!=null && i<beliefrefs.size() && ret==null; i++)
		{
			IMBeliefReference tmp = (IMBeliefReference)beliefrefs.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new belief reference.
	 *  @param name	The belief reference name.
	 *  @param clazz	The class for facts.
	 *  @param exported	Flag indicating if this belief reference may be referenced from outside capabilities.
	 *  @param ref	The referenced belief (or null for abstract).
	 *  @return	The newly created belief reference.
	 */
	public IMBeliefReference	createBeliefReference(String name, Class clazz, String exported, String ref)
	{
		if(beliefrefs==null)
			beliefrefs = SCollection.createArrayList();

		MBeliefReference ret = new MBeliefReference();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setExported(exported);
		if(ref!=null)
			ret.setReference(ref);
		else
			ret.setAbstract(true);
		ret.setOwner(this);
		ret.init();
		beliefrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a belief reference.
	 *  @param beliefreference	The belief reference to delete.
	 */
	public void	deleteBeliefReference(IMBeliefReference beliefreference)
	{
		if(!beliefrefs.remove(beliefreference))
			throw new RuntimeException("Belief reference not found: "+beliefreference);
	}


	//-------- belief set references --------

	/**
	 *  Get all defined belief set references.
	 *  @return The belief set references.
	 */
	public IMBeliefSetReference[] getBeliefSetReferences()
	{
		if(beliefsetrefs==null)
			return new IMBeliefSetReference[0];
		return (IMBeliefSetReference[])beliefsetrefs.toArray(new IMBeliefSetReference[beliefsetrefs.size()]);
	}

	/**
	 *  Get a belief set reference by name.
	 *  Searches the belief indirect subcapability,
	 *  when path notation is used (a.name).
	 *  @param name	The belief set name.
	 *  @return The belief set with that name (if any).
	 */
	public IMBeliefSetReference	getBeliefSetReference(String name)
	{
		assert name!=null;
		assert name.indexOf(".")==-1;

		IMBeliefSetReference ret = null;
		for(int i=0; beliefsetrefs!=null && i<beliefsetrefs.size() && ret==null; i++)
		{
			IMBeliefSetReference tmp = (IMBeliefSetReference)beliefsetrefs.get(i);
			if(name.equals(tmp.getName()))
				ret = tmp;
		}
		return ret;
	}

	/**
	 *  Create a new belief set reference.
	 *  @param name	The belief set reference name.
	 *  @param clazz	The class for facts.
	 *  @param exported	Flag indicating if this belief set reference may be referenced from outside capabilities.
	 *  @param ref	The referenced belief set (or null for abstract).
	 *  @return	The newly created belief set reference.
	 */
	public IMBeliefSetReference	createBeliefSetReference(String name, Class clazz, String exported, String ref)
	{
		if(beliefsetrefs==null)
			beliefsetrefs = SCollection.createArrayList();

		MBeliefSetReference ret = new MBeliefSetReference();
		ret.setName(name);
		ret.setClazz(clazz);
		ret.setExported(exported);
		ret.setReference(ref);
		ret.setOwner(this);
		ret.init();
		beliefsetrefs.add(ret);
		return ret;
	}

	/**
	 *  Delete a belief set reference.
	 *  @param beliefsetreference	The belief set reference to delete.
	 */
	public void	deleteBeliefSetReference(IMBeliefSetReference beliefsetreference)
	{
		if(!beliefsetrefs.remove(beliefsetreference))
			throw new RuntimeException("Belief reference not found: "+beliefsetreference);
	}

	//-------- other --------

	/**
	 *  Get an encodable representation.
	 *  @return A map containing the relevant properties of the object.
	 */
	public Map	getEncodableRepresentation()
	{
		Map representation = super.getEncodableRepresentation();

		/*IMBelief[] bels = getBeliefs();
		for(int i=0; i<bels.length; i++)
			representation.put(bels[i].getName(), bels[i].getEncodableRepresentation());*/

		// Todo: implement me

		return representation;
	}

	/**
	 *  Make a deep copy of this element.
	 *  @param cl The clone.
	 */
	public void doClone(MElement cl)
	{
		super.doClone(cl);
		MBeliefbase clone = (MBeliefbase)cl;
		if(beliefs!=null)
		{
			clone.beliefs = SCollection.createArrayList();
			for(int i=0; i<beliefs.size(); i++)
				clone.beliefs.add(((MElement)beliefs.get(i)).clone());
		}
		if(beliefrefs!=null)
		{
			clone.beliefrefs = SCollection.createArrayList();
			for(int i=0; i<beliefrefs.size(); i++)
				clone.beliefrefs.add(((MElement)beliefrefs.get(i)).clone());
		}
		if(beliefsets!=null)
		{
			clone.beliefsets = SCollection.createArrayList();
			for(int i=0; i<beliefsets.size(); i++)
				clone.beliefsets.add(((MElement)beliefsets.get(i)).clone());
		}
		if(beliefsetrefs!=null)
		{
			clone.beliefsetrefs = SCollection.createArrayList();
			for(int i=0; i<beliefsetrefs.size(); i++)
				clone.beliefsetrefs.add(((MElement)beliefsetrefs.get(i)).clone());
		}
	}
}
