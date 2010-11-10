package jadex.runtime.impl.agenda.easydeliberation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import jadex.model.*;
import jadex.runtime.impl.IRGoal;
import jadex.runtime.impl.RElement;
import jadex.runtime.impl.RExpression;
import jadex.util.Tuple;
import jadex.util.collection.SCollection;

/**
 *  Helper class for easy deliberation strategy.
 */
public class SEasyDeliberation
{
	//-------- attributes --------
	
	/** lookup table for inhibitions of goal instances. */
	protected static final Map	lookup	= SCollection.createWeakHashMap();
		
	//-------- methods --------
	
	/**
	 *  Check if the source goal inhibits the target goal.
	 *  @param when_active	Test if the goal inhibits another goal when active.
	 *  @param when_process	Test if the goal inhibits another goal when in process.
	 */
	public static boolean	inhibits(IRGoal source, IRGoal target, boolean when_active, boolean when_process)
	{
		boolean	inhibits	= false;
		Tuple	inhibition;

		// Goals don't inhibit themselves.
		if(source!=target && (inhibition=getInhibition(source, target))!=null)
		{
			// Inhibit when state matches.
			String	when	= (String)inhibition.get(0);
			inhibits	= when_active && when.equals(IMInhibits.WHEN_ACTIVE) 
				|| (when_process && when.equals(IMInhibits.WHEN_IN_PROCESS));
			
			// Inhibition expression: Only inhibits goals that match expression.
			if(inhibits && inhibition.get(1)!=null)
			{
				try
				{
					RExpression	exp	= (RExpression)inhibition.get(1);
					exp.setExpressionParameter("$ref", target);
					inhibits	= ((Boolean)exp.getValue()).booleanValue();
				}
				catch(Exception e)
				{
					StringWriter	sw	= new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					source.getScope().getLogger().severe("Inhibition expression threw exception: "+inhibition.get(1)+"\n"+sw);
				}
			}

			// Else unconstrained inhibition: Inhibits all goals of given type.
		}
		return inhibits;
	}
	
	/**
	 *  Get the inhibition settings for a goal pair.
	 *  @return A tuple of inhibit string (e.g. "when_active") and inhibits expression (if any). 
	 */
	protected static Tuple	getInhibition(IRGoal source, IRGoal target)
	{
		Map	inhibitions	= (Map)lookup.get(source);
		
		if(inhibitions==null)
		{
			// Initialize inhibition settings.
			IMElement	goal	= source.getModelElement();
			IMDeliberation	deliberation	= null;
			if(goal instanceof IMGoal)
				deliberation	= ((IMGoal)goal).getDeliberation();
			else if(goal instanceof IMGoalReference)
				deliberation	= ((IMGoalReference)goal).getDeliberation();
			
			if(deliberation!=null)
			{
				inhibitions	= SCollection.createHashMap();
				IMInhibits[]	inhibits	= deliberation.getInhibits();
				for(int i=0; i<inhibits.length; i++)
				{
					RExpression	exp	= null;
					if(inhibits[i].getInhibitingExpression()!=null)
					{
						// Hack!!! Shouldn't be allowed to create expression for goal?
						// Todo: expressions are not cleaned up (but don't need to cleanup internal expressions???)
						exp	= source.getScope().getExpressionbase().createInternalExpression(
							inhibits[i].getInhibitingExpression(), (RElement)source, null);
					}
					inhibitions.put(inhibits[i].getInhibitedGoal(),
						new Tuple(inhibits[i].getInhibit() ,exp));
				}
			}
			else
			{
				inhibitions	= Collections.EMPTY_MAP;
			}
			
			lookup.put(source, inhibitions);
		}
		
		// Return inhibition settings for target element.
		return (Tuple)inhibitions.get(target.getModelElement());
	}
}
