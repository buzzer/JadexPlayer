package jadex.tutorial;

import java.util.StringTokenizer;
import jadex.runtime.*;
import jadex.util.Tuple;

/**
 *  Add a english - german word pair to the wordtable.
 */
public class EnglishGermanAddWordPlanC3 extends Plan
{
	//-------- attributes --------

	/** Query the tuples for a word. */
	protected IExpression	testword;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public EnglishGermanAddWordPlanC3()
	{
		getLogger().info("Created: "+this);
		this.testword	= getExpression("query_egword");
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		StringTokenizer stok = new StringTokenizer(
			(String)((IMessageEvent)getInitialEvent()).getContent(), " ");
		if(stok.countTokens()==4)
		{
			stok.nextToken();
			stok.nextToken();
			String eword = stok.nextToken();
			String gword = stok.nextToken();
			// Hack!!! Should check if contains.
			Object test = testword.execute("$eword", eword);
			if(test==null)
			{
				getBeliefbase().getBeliefSet("egwords").addFact(new Tuple(eword, gword));
				getLogger().info("Added  new wordpair to database: "
					+eword+" - "+gword);
			}
			else
			{
				getLogger().info("Sorry database already contains word: "+eword);
			}
		}
		else
		{
			getLogger().warning("Sorry format not correct.");
		}
	}
}
