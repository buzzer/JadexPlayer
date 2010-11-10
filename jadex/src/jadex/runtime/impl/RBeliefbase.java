package jadex.runtime.impl;

import java.util.ArrayList;
import java.util.List;
import jadex.model.*;
import jadex.util.collection.*;
import jadex.util.*;
import jadex.runtime.*;

/**
 *  A beliefbase instance.
 */
// Hack !!! public for parser.
public class RBeliefbase extends RBase
{
	//-------- attributes --------

	/** The beliefs stored by the name of its mbelief. */
	protected IndexMap	beliefs;

	/** The belief sets stored by the name of its mbeliefset. */
	protected IndexMap	beliefsets;

	//-------- constructors --------

	/**
	 *  Create a new beliefbase model.
	 *  @param beliefbase The model element.
	 *  @param owner The owner.
	 */
	protected RBeliefbase(IMBeliefbase beliefbase, RElement owner)
	{
		super(null, beliefbase, owner);
		this.beliefs	= SCollection.createIndexMap();
		this.beliefsets	= SCollection.createIndexMap();
	}

	/**
	 *  Initialize the belief base.
	 */
	protected void	init(int level)
	{
		// On constructor init, create belief instances. 
		if(level==0)
		{
			// Create initial beliefs.
			IMBeliefbase	model	= (IMBeliefbase)getModelElement();
			IMConfigBeliefbase	initialbase	= getScope().getConfiguration()!=null?
				getScope().getConfiguration().getBeliefbase() : null;
	
			// Create belief instances.
			IMBelief[] bels = model.getBeliefs();
			//System.out.println("Found beliefs: "+SUtil.arrayToString(bels));
			for(int i=0; i<bels.length; i++)
			{
				if(!containsBelief(bels[i].getName()))
					createBelief(bels[i], initialbase!=null ? initialbase.getInitialBelief(bels[i]): null, null);
			}
			// Create beliefset instances.
			IMBeliefSet[] belsets = model.getBeliefSets();
			//System.out.println("Found beliefsets: "+SUtil.arrayToString(belsets));
			for(int i=0; i<belsets.length; i++)
			{
				if(!containsBeliefSet(belsets[i].getName()))
					createBeliefSet(belsets[i], initialbase!=null ? initialbase.getInitialBeliefSet(belsets[i]): null, null);
			}
	
			// Create references.
			IMBeliefbase	beliefbase	= (IMBeliefbase)getModelElement();
			// Create belief references.
			IMBeliefReference[] belrefs = beliefbase.getBeliefReferences();
			for(int i=0; i<belrefs.length; i++)
			{
				if(!containsBelief(belrefs[i].getName()))
					createBelief(belrefs[i], initialbase!=null ? initialbase.getInitialBelief(belrefs[i]): null, null);
			}
			// Create beliefset references.
			IMBeliefSetReference[] belsetrefs = beliefbase.getBeliefSetReferences();
			for(int i=0; i<belsetrefs.length; i++)
			{
				if(!containsBeliefSet(belsetrefs[i].getName()))
					createBeliefSet(belsetrefs[i], initialbase!=null ? initialbase.getInitialBeliefSet(belsetrefs[i]): null, null);
			}
		}

		// On action init, initialize belief instances to assign initial values.
		else if(level==1)
		{
			//System.out.println("Called init on: "+getName());
			// Init the beliefs and beliefsets.
			for(int i=0; i<beliefs.size(); i++)
			{
				if(beliefs.get(i) instanceof RBelief)
				{
					RBelief bel = (RBelief)beliefs.get(i);
					bel.initStructure();
				}
//				else
//				{
//					RBeliefReference belref = (RBeliefReference)beliefs.get(i);
//					if(!belref.isInited())
//						belref.init();
//				}
			}
			for(int i=0; i<beliefsets.size(); i++)
			{
				if(beliefsets.get(i) instanceof RBeliefSet)
				{
					RBeliefSet belset = (RBeliefSet)beliefsets.get(i);
					belset.initStructure();
				}
//				else
//				{
//					RBeliefSetReference belsetref = (RBeliefSetReference)beliefsets.get(i);
//					if(!belsetref.isInited())
//						belsetref.init();
//				}
			}
		}
	}

	//-------- methods concerning beliefs --------

    /**
	 *  Get a belief for a name.
	 *  @param name	The belief name.
	 */
	public IRBelief getBelief(String name)
	{
		if(containsBelief(name))
		{
			return (IRBelief)beliefs.get(name);
		}
		else
		{
			throw new RuntimeException("No such belief: "+name+" in "+this);
		}
	}

	 /**
	 *  Get a belief set for a name.
	 *  @param name	The belief set name.
	 */
	public IRBeliefSet getBeliefSet(String name)
	{
		if(containsBeliefSet(name))
		{
			return (IRBeliefSet)beliefsets.get(name);
		}
		else
		{
			throw new RuntimeException("No such belief set: "+name+" in "+this);
		}
	}

    /**
     *  Returns <tt>true</tt> if this beliefbase contains a belief with the
     *  specified name.
     *  @param name the name of a belief.
     *  @return <code>true</code> if contained, <code>false</code> is not contained, or
     *          the specified name refer to a belief set.
     *  @see #containsBeliefSet(java.lang.String)
     */
    public boolean	containsBelief(String name)
    {
		//Object bel = beliefs.get(name);
		//return bel!=null && (!(bel instanceof RElementReference) || ((RElementReference)bel).isBound());
		return beliefs.containsKey(name);
    }

    /**
     *  Returns <tt>true</tt> if this beliefbase contains a belief set with the
     *  specified name.
     *  @param name the name of a belief set.
     *  @return <code>true</code> if contained, <code>false</code> is not contained, or
     *          the specified name refer to a belief.
     *  @see #containsBelief(java.lang.String)
     */
    public boolean	containsBeliefSet(String name)
    {
		//Object bel = beliefsets.get(name);
		//return bel!=null && (!(bel instanceof RElementReference) || ((RElementReference)bel).isBound());
        return beliefsets.containsKey(name);
    }

    /**
     *  Returns the names of all beliefs.
     *  @return the names of all beliefs.
     */
    public String[]	getBeliefNames()
    {
		return (String[])beliefs.keySet().toArray(new String[beliefs.size()]);
    }

    /**
     *  Returns the names of all belief sets.
     *  @return the names of all belief sets.
     */
    public String[]	getBeliefSetNames()
    {
        return (String[])beliefsets.keySet().toArray(new String[beliefsets.size()]);
    }

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param clazz The class.
	 *  @deprecated
	 */
	public void createBelief(String key, Class clazz, int update)
	{
		if(!containsBelief(name) && !containsBeliefSet(name))
		{
			IMBelief mbel = ((IMBeliefbase)getModelElement()).createBelief(key, clazz, update, IMReferenceableElement.EXPORTED_FALSE);
			//MBelief mbel = new MBelief(key, getModelElement(), false,
			//	MBelief.TYPE_INTERNAL, clazz, update==0? null: ""+update, true); // Hack propagate=true??
			RBelief rbel = (RBelief)createBelief(mbel, null, null);
			rbel.initStructure();
		}
		else
		{
			throw new RuntimeException("Belief "+name+" exlready exists in "+this);
		}
	}

	/**
	 *  Create a belief with given key and class.
	 *  @param key The key identifying the belief.
	 *  @param clazz The class.
	 *  @deprecated
	 */
	public void createBeliefSet(String key, Class clazz, int update)
	{
		if(!containsBelief(name) && !containsBeliefSet(name))
		{
			IMBeliefSet mbelset = ((IMBeliefbase)getModelElement()).createBeliefSet(key, clazz, update, IMReferenceableElement.EXPORTED_FALSE);
			//MBelief mbelset = new MBelief(key, getModelElement(), false,
			//	MBelief.TYPE_INTERNAL, clazz, update==0? null: ""+update, true); // Hack propagate=true??
			RBeliefSet rbelset = (RBeliefSet)createBeliefSet(mbelset, null, null);
			rbelset.initStructure();
		}
		else
		{
			throw new RuntimeException("Belief set "+name+" exlready exists in "+this);
		}
	}

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBelief(String key)
	{
		if(beliefs.containsKey(key))
		{
			RReferenceableElement	belief	= (RReferenceableElement)beliefs.get(key);
			belief.throwSystemEvent(SystemEvent.BELIEF_REMOVED);
			beliefs.removeKey(key);
			((IMBase)getModelElement()).deleteReferenceableElement((IMReferenceableElement)belief.getModelElement());
			// Hack!!! May be cleaned up already because of cascading delete of references.
			if(!belief.isCleanedup())
				belief.cleanup();
		}
		else
		{
			throw new RuntimeException("No such belief: "+key+" in "+this);
		}
	}

	/**
	 *  Delete a belief with given key.
	 *  @param key The key identifying the belief.
	 *  @deprecated
	 */
	public void deleteBeliefSet(String key)
	{
		if(beliefsets.containsKey(key))
		{
			RReferenceableElement	beliefset	= (RReferenceableElement)beliefsets.get(key);
			beliefset.throwSystemEvent(SystemEvent.BELIEF_REMOVED);
			beliefsets.removeKey(key);
			((IMBase)getModelElement()).deleteReferenceableElement((IMReferenceableElement)beliefset.getModelElement());
			// Hack!!! May be cleaned up already because of cascading delete of references.
			if(!beliefset.isCleanedup())
				beliefset.cleanup();
		}
		else
		{
			throw new RuntimeException("No such belief set: "+key+" in "+this);
		}
	}

	/**
	 *  Register a new belief model.
	 *  @param mbelief The belief model.
	 */
	public void registerBelief(IMBelief mbelief)
	{
		// Create initial beliefs.
		IMConfigBeliefbase	initialbase	= getScope().getConfiguration()!=null?
			getScope().getConfiguration().getBeliefbase() : null;

		// Create belief instance.
		if(containsBelief(mbelief.getName()) || containsBeliefSet(mbelief.getName()))
			throw new RuntimeException("Beliefbase already contains a belief(set) with name: "+mbelief.getName());

		RBelief rbel = (RBelief)createBelief(mbelief, initialbase!=null ? initialbase.getInitialBelief(mbelief): null, null);
		rbel.initStructure();
	}

	/**
	 *  Register a new beliefset model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void registerBeliefSet(IMBeliefSet mbeliefset)
	{
		// Create initial beliefs.
		IMConfigBeliefbase	initialbase	= getScope().getConfiguration()!=null?
			getScope().getConfiguration().getBeliefbase() : null;

		// Create belief instance.
		if(containsBelief(mbeliefset.getName()) || containsBeliefSet(mbeliefset.getName()))
			throw new RuntimeException("Beliefbase already contains a belief(set) with name: "+mbeliefset.getName());

		RBeliefSet rbelset = (RBeliefSet)createBeliefSet(mbeliefset, initialbase!=null ? initialbase.getInitialBeliefSet(mbeliefset): null, null);
		rbelset.initStructure();
	}

	/**
	 *  Register a new belief reference model.
	 *  @param mbeliefref The belief reference model.
	 */
	public void registerBeliefReference(IMBeliefReference mbeliefref)
	{
		// Create initial beliefs.
		IMConfigBeliefbase	initialbase	= getScope().getConfiguration()!=null?
			getScope().getConfiguration().getBeliefbase() : null;

		// Create belief instance.
		if(containsBelief(mbeliefref.getName()) || containsBeliefSet(mbeliefref.getName()))
			throw new RuntimeException("Beliefbase already contains a belief(set) with name: "+mbeliefref.getName());

		RBeliefReference rbel = (RBeliefReference)createBelief(mbeliefref, initialbase!=null ? initialbase.getInitialBelief(mbeliefref): null, null);
		rbel.initStructure();
	}

	/**
	 *  Register a new beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void registerBeliefSetReference(IMBeliefSetReference mbeliefsetref)
	{
		// Create initial beliefs.
		IMConfigBeliefbase	initialbase	= getScope().getConfiguration()!=null?
			getScope().getConfiguration().getBeliefbase() : null;

		// Create beliefset instance.
		if(containsBelief(mbeliefsetref.getName()) || containsBeliefSet(mbeliefsetref.getName()))
			throw new RuntimeException("Beliefbase already contains a belief(set) with name: "+mbeliefsetref.getName());

		RBeliefSetReference rbelset = (RBeliefSetReference)createBeliefSet(mbeliefsetref, initialbase!=null ? initialbase.getInitialBeliefSet(mbeliefsetref): null, null);
		rbelset.initStructure();
	}

	/**
	 *  Deregister a belief model.
	 *  @param mbelief The belief model.
	 */
	public void deregisterBelief(IMBelief mbelief)
	{
		if(mbelief==null)
			throw new RuntimeException("Belief must not null.");

		RReferenceableElement	belief	= (RReferenceableElement)beliefs.get(mbelief.getName());
		belief.throwSystemEvent(SystemEvent.BELIEF_REMOVED);
		beliefs.removeKey(mbelief.getName());
		//((IMBase)getModelElement()).deleteReferenceableElement((IMReferenceableElement)belief.getModelElement());
		// Hack!!! May be cleaned up already because of cascading delete of references.
		if(!belief.isCleanedup())
			belief.cleanup();
	}

	/**
	 *  Deregister a beliefset model.
	 *  @param mbeliefset The beliefset model.
	 */
	public void deregisterBeliefSet(IMBeliefSet mbeliefset)
	{
		if(mbeliefset==null)
			throw new RuntimeException("BeliefSet must not null.");

		RReferenceableElement	beliefset	= (RReferenceableElement)beliefsets.get(mbeliefset.getName());
		beliefset.throwSystemEvent(SystemEvent.BELIEF_REMOVED); // HACK: BELIEFSET_
		beliefsets.removeKey(mbeliefset.getName());
		//((IMBase)getModelElement()).deleteReferenceableElement((IMReferenceableElement)beliefset.getModelElement());
		// Hack!!! May be cleaned up already because of cascading delete of references.
		if(!beliefset.isCleanedup())
			beliefset.cleanup();
	}

	/**
	 *  Deregister a belief reference model.
	 *  @param mbeliefref The belief reference model.
	 */
	public void deregisterBeliefReference(IMBeliefReference mbeliefref)
	{
		if(mbeliefref==null)
			throw new RuntimeException("BeliefReference must not null.");

		RReferenceableElement	belief	= (RReferenceableElement)beliefs.get(mbeliefref.getName());
		belief.throwSystemEvent(SystemEvent.BELIEF_REMOVED);
		beliefs.removeKey(mbeliefref.getName());
		//((IMBase)getModelElement()).deleteReferenceableElement((IMReferenceableElement)belief.getModelElement());
		// Hack!!! May be cleaned up already because of cascading delete of references.
		if(!belief.isCleanedup())
			belief.cleanup();
	}

	/**
	 *  Deregister a beliefset reference model.
	 *  @param mbeliefsetref The beliefset reference model.
	 */
	public void deregisterBeliefSetReference(IMBeliefSetReference mbeliefsetref)
	{
		if(mbeliefsetref==null)
			throw new RuntimeException("BeliefSetReference must not null.");

		RReferenceableElement	beliefset	= (RReferenceableElement)beliefsets.get(mbeliefsetref.getName());
		beliefset.throwSystemEvent(SystemEvent.BELIEF_REMOVED); // HACK: BELIEFSET_
		beliefsets.removeKey(mbeliefsetref.getName());
		//((IMBase)getModelElement()).deleteReferenceableElement((IMReferenceableElement)beliefset.getModelElement());
		// Hack!!! May be cleaned up already because of cascading delete of references.
		if(!beliefset.isCleanedup())
			beliefset.cleanup();
	}

	//-------- overridings --------

	/**
	 *  Create a string representation of this element.
	 *  @return	A string representing this element.
	 */
	public String	toString()
	{
		StringBuffer	sb	= new StringBuffer();
		sb.append(SReflect.getInnerClassName(this.getClass()));
		sb.append("(name=");
		sb.append(name);
		sb.append("\nbeliefs=\n");
		for(int i=0; beliefs!=null && i<beliefs.size(); i++)
		{
			sb.append("  ");
			sb.append(((RElement)beliefs.get(i)).getName());
			sb.append("\n");
		}
		sb.append("\nbeliefsets=\n");
		for(int i=0; beliefsets!=null && i<beliefsets.size(); i++)
		{
			sb.append("  ");
			sb.append(((RElement)beliefsets.get(i)).getName());
			sb.append("\n");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 *  Get the state of this belief base.
	 *  encoded in a set of corresponding change events.
	 *  @param types	The system event types, the caller is interested in.
	 */
	public List	getState(String[] types)
	{
  		ArrayList	ret	= SCollection.createArrayList();

		// Add events for beliefs.
		if(ISystemEventTypes.Subtypes.isSubtype(SystemEvent.BELIEF_ADDED, types))
		{
			for(int i=0; i<beliefs.size(); i++)
			{
				IRBelief bel = (IRBelief)beliefs.get(i);
				// Hack! To allow initially invalid facts (e.g. special "dynamic" facts)
				Object	value;
				try
				{
					value = bel.getFact();
				}
				catch(Exception e)
				{
					value = "n/a";
				}
				ret.add(new SystemEvent(SystemEvent.BELIEF_ADDED, bel, value));
			}
			for(int i=0; i<beliefsets.size(); i++)
			{
				RElement bel = (RElement)beliefsets.get(i);
				ret.add(new SystemEvent(SystemEvent.BELIEF_ADDED, bel));
			}
		}

		// Add events for facts of beliefsets.
		if(ISystemEventTypes.Subtypes.isSubtype(SystemEvent.BSFACT_ADDED, types))
		{
			for(int i=0; i<beliefsets.size(); i++)
			{
				IRBeliefSet bel	= (IRBeliefSet)beliefsets.get(i);
				// Hack! To allow initially invalid facts (e.g. special "dynamic" facts)
				try
				{
					Object[]	facts	= bel.getFacts();
					for(int j=0; j<facts.length; j++)
					{
						ret.add(new SystemEvent(SystemEvent.BSFACT_ADDED, bel, facts[j], j));
					}
				}
				catch(Exception e){}
			}
		}
		
		return ret;
	}

	//-------- RBase abstract methods --------

	/**
	 *  Get the runtime element for a model element.
	 *  Depending on the type it might have to be created (e.g. a goal)
	 *  or might be already there (e.g. belief).
	 *  @param melement	The model of the element to be retrieved.
	 *  @param creator	The creator of the element (e.g. a reference).
	 */
	protected RReferenceableElement	getElementInstance(
			IMReferenceableElement melement, RReferenceableElement creator)
	{
		assert melement!=null;

		RCapability	scope	= getScope().getAgent().lookupCapability(melement.getScope());

		assert scope!=null: "Scope of "+melement.getName()+" nulls, defined in: "+melement.getScope();
		assert scope.getBeliefbase()!=null: scope.getName()+" "+getName()+" "+melement.getName();

		// Create belief if not already contained.
		if(!scope.getBeliefbase().containsBelief(melement.getName())
			&& !scope.getBeliefbase().containsBeliefSet(melement.getName()))
		{
			//System.out.println("Creating: "+melement.getName()+" "+scope.getName()+" "+melement);
			IMConfigBeliefbase	initialbase	= scope.getConfiguration()!=null?
				scope.getConfiguration().getBeliefbase() : null;
			if(melement instanceof IMBelief)
			{
				scope.getBeliefbase().createBelief(melement, initialbase!=null
					? initialbase.getInitialBelief((IMBelief)melement) : null, creator);
			}
			else if(melement instanceof IMBeliefReference)
			{
				scope.getBeliefbase().createBelief(melement, initialbase!=null
					? initialbase.getInitialBelief((IMBeliefReference)melement) : null, creator);
			}
			else if(melement instanceof IMBeliefSet)
			{
				scope.getBeliefbase().createBeliefSet(melement, initialbase!=null
					? initialbase.getInitialBeliefSet((IMBeliefSet)melement) : null, creator);
			}
			else if(melement instanceof IMBeliefSetReference)
			{
				scope.getBeliefbase().createBeliefSet(melement, initialbase!=null
					? initialbase.getInitialBeliefSet((IMBeliefSetReference)melement) : null, creator);
			}
		}

		if(melement instanceof IMBelief || melement instanceof IMBeliefReference)
			return (RReferenceableElement)scope.getBeliefbase().getBelief(melement.getName());
		else if(melement instanceof IMBeliefSet || melement instanceof IMBeliefSetReference)
			return (RReferenceableElement)scope.getBeliefbase().getBeliefSet(melement.getName());
		else
			throw new RuntimeException("Cannot lookup element: "+melement);
	}

	/**
	 *  Get the corresponding base of a given scope.
	 *  Depending on the type of this base (e.g. beliefbase),
	 *  the corresponding base of the given scope is returned.
	 *  @param scope	The scope.
	 *  @return	The corresponding base.
	 */
	protected RBase getCorrespondingBase(RCapability scope)
	{
		assert scope!=null: this;
		return scope.getBeliefbase();
	}

	//-------- helper methods --------

	/**
	 *  Create a runtime element for a model element.
	 *  @param melement The model element.
	 * /
	protected RBelief createBelief(IMBelief melement, IMInitialBelief state)
	{
		RBelief	element	= new RBelief(melement, state, this);
		beliefs.add(melement.getName(), element);
		return element;
	}*/

	/**
	 *  Create a runtime element for a model element.
	 *  @param melement The model element.
	 * /
	protected RBeliefReference createBeliefReference(IMBeliefReference melement)
	{
		RBeliefReference	element = new RBeliefReference(melement, null, this);
		beliefs.add(melement.getName(), element);
		return element;
	}*/

	/**
	 *  Create a runtime element for a model element.
	 *  @param melement The model element.
	 * /
	protected RBeliefSet createBeliefSet(IMBeliefSet melement, IMInitialBeliefSet state)
	{
		RBeliefSet	element = new RBeliefSet(melement, state, this);
		beliefsets.add(melement.getName(), element);
		return element;
	}*/

	/**
	 *  Create a runtime element for a model element.
	 *  @param melement The model element.
	 * /
	protected RBeliefSetReference createBeliefSetReference(IMBeliefSetReference melement)
	{
		RBeliefSetReference	element = new RBeliefSetReference(melement, null, this);
		beliefsets.add(melement.getName(), element);
		return element;
	}*/

	/**
	 *  Create a belief or a belief reference.
	 *  @param belief The model.
	 *  @param config The configuration.
	 *  @param creator The creator.
	 *  @return The new belief.
	 */
	protected IRBelief createBelief(IMReferenceableElement belief,
		IMConfigBelief config, RReferenceableElement creator)
	{
		assert belief!=null: belief+" "+creator;
		assert belief instanceof IMBelief || belief instanceof IMBeliefReference;
		assert belief.getScope() == getScope().getModelElement()
			: "error+ "+belief.getScope()+" "+getScope();//belief.getScope().getName()+" "+RCapability.getScope(this).getModelElement().getName();
		assert creator==null || creator instanceof IRBelief : creator;

		IRBelief ret = null;

		if(belief instanceof IMBelief)
			ret	= new RBelief((IMBelief)belief, config, this, creator);
		else
			ret = new RBeliefReference((IMBeliefReference)belief, config, this, creator);

		beliefs.add(belief.getName(), ret);

		return ret;
	}

	/**
 	 *  Create a belief or a belief reference.
	 *  @param beliefset The model.
	 *  @param config The configuration.
	 *  @param creator The creator.
	 *  @return The new belief.
	 */
	protected IRBeliefSet createBeliefSet(IMReferenceableElement beliefset,
		IMConfigBeliefSet config, RReferenceableElement creator)
	{
		assert beliefset!=null: beliefset+" "+creator;
		assert beliefset instanceof IMBeliefSet || beliefset instanceof IMBeliefSetReference;
		assert beliefset.getScope() == getScope().getModelElement();
		assert creator==null || creator instanceof IRBeliefSet : creator;

		IRBeliefSet ret = null;

		if(beliefset instanceof IMBeliefSet)
			ret	= new RBeliefSet((IMBeliefSet)beliefset, config, this, creator);
		else
			ret = new RBeliefSetReference((IMBeliefSetReference)beliefset, config, this, creator);

		beliefsets.add(beliefset.getName(), ret);

		return ret;
	}

	/**
	 *  Get a belief for a model element recursively from inner capabilities.
	 *  @param name	The belief model element name.
	 * /
	protected RElement getBeliefRec(String name)
	{
		// Hack!!! Should go directly to correct capability?
		RElement ret = null;

		if(containsBelief(name))
		{
			ret = getBelief(name);
		}
		else if(containsBeliefSet(name))
		{
			ret = getBeliefSet(name);
		}
		else
		{
			// todo: only for direct subcaps!!!
			RCapability[] capas = RReferenceableElement.getScope(this).getChildren();
			for(int i=0; i<capas.length && ret==null; i++)
				ret = capas[i].getBeliefbase().getBeliefRec(name);
		}
		return ret;
	}*/

	/**
	 *  Perform any necessary clean up, when this element is
	 *  no longer used.
	 *  Subclasses may override this method to do their
	 *  cleanup operations.
	 */
	public void	cleanup()
	{
		if(cleanedup)
			return;
		
		//System.out.println("Cleanup: "+getName());
		super.cleanup();
		for(int i=0; i<beliefs.size(); i++)
		{
			((RElement)beliefs.get(i)).cleanup();
		}
		for(int i=0; i<beliefsets.size(); i++)
		{
			((RElement)beliefsets.get(i)).cleanup();
		}
	}
}

