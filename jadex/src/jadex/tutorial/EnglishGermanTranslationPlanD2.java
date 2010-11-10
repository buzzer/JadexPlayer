package jadex.tutorial;

import java.util.StringTokenizer;
import jadex.runtime.*;
import jadex.adapter.fipa.SFipa;

/**
 *  An english german translation plan can translate
 *  english words to german and is instantiated on demand.
 */
public class EnglishGermanTranslationPlanD2 extends Plan
{
	//-------- attributes --------

	/** Query the tuples for a word. */
	protected IExpression	query_word;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public EnglishGermanTranslationPlanD2()
	{
		getLogger().info("Created:"+this);
		this.query_word	= getQuery("query_egword");
	}

	//-------- methods --------

	/**
	 *  Do a plan step.
	 */
	public void body()
	{
		String	reply;
		Object	cont;
		StringTokenizer stok = new StringTokenizer(
			(String)((IMessageEvent)getInitialEvent()).getContent(), " ");
		if(stok.countTokens()==3)
		{
			stok.nextToken();
			stok.nextToken();
			String eword = stok.nextToken();
			String gword = (String)query_word.execute("$eword", eword);
			if(gword!=null)
			{
				getLogger().info("Translating from english to german: "+eword+" - "+gword);
				cont = gword;
				reply = "inform";
			}
			else
			{
				cont = "Sorry word is not in database: "+eword;
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

