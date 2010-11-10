package jadex.tutorial;

import java.util.StringTokenizer;
import jadex.runtime.*;
import jadex.util.Tuple;
import jadex.adapter.fipa.SFipa;

/**
 *  Add a english - german word pair to the wordtable.
 */
public class EnglishGermanAddWordPlanD2 extends Plan
{
	//-------- attributes --------

	/** Query the tuples for a word. */
	protected IExpression	testword;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public EnglishGermanAddWordPlanD2()
	{
		getLogger().info("Created :"+this);
		this.testword	= getQuery("query_egword");
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		Object	cont;
		String	reply;
		StringTokenizer stok = new StringTokenizer(
			(String)((IMessageEvent)getInitialEvent()).getContent(), " ");
		if(stok.countTokens()==4)
		{
			stok.nextToken();
			stok.nextToken();
			String eword = stok.nextToken();
			String gword = stok.nextToken();
			Object words = testword.execute("$eword", eword);
			if(words==null)
			{
				getBeliefbase().getBeliefSet("egwords").addFact(new Tuple(eword, gword));
				cont = "Added  new wordpair to database: "+eword+" - "+gword;
				reply = "inform";
			}
			else
			{
				cont= "Sorry database already contains word: "+eword;
				reply = "failure";
			}
		}
		else
		{
			cont = "Sorry format not correct.";
			reply = "failure";
		}
		sendMessage(((IMessageEvent)getInitialEvent()).createReply(reply, cont));
	}
}
