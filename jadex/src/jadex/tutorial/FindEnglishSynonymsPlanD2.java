package jadex.tutorial;

import java.util.*;
import jadex.runtime.*;
import jadex.adapter.fipa.SFipa;

/**
 *  Find english synonyms for a word.
 */
public class FindEnglishSynonymsPlanD2 extends Plan
{
	//-------- attributes --------

	/** Query the tuples for a word. */
	protected IExpression	querytranslate;

	/** Query to find synonyms. */
	protected IExpression	queryfind;


	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public FindEnglishSynonymsPlanD2()
	{
		getLogger().info("Created: "+this);

		// Create precompiled queries.
		String	translate	= "select one $wordpair.get(1) "
			+"from Tuple $wordpair in $beliefbase.egwords "
			+"where $wordpair.get(0).equals($eword)";

		String	find	= "select $wordpair.get(0) "
			+"from Tuple $wordpair in $beliefbase.egwords "
			+"where $wordpair.get(1).equals($gword) && !$wordpair.get(0).equals($eword)";

		this.querytranslate	= createExpression(translate, new String[]{"$eword"}, new Class[]{String.class});
		this.queryfind	= createExpression(find, new String[]{"$gword", "$eword"}
			, new Class[]{String.class, String.class});
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		IMessageEvent me = (IMessageEvent)getInitialEvent();
		IMessageEvent reply;
		StringTokenizer stok = new StringTokenizer((String)me.getContent(), " ");

		if(stok.countTokens()==3)
		{
			stok.nextToken();
			stok.nextToken();
			String eword = stok.nextToken();
			String gword = (String)querytranslate.execute("$eword", eword);
			queryfind.setParameter("$gword", gword);
			queryfind.setParameter("$eword", eword);
			List syns = (List)queryfind.execute();
			//getLogger().info("Synonyms for eword: "+syns);
			reply = me.createReply("inform", "Synonyms for "+eword+" : "+syns);
		}
		else
		{
			reply = me.createReply("failure", "Request format not correct.");
		}
		sendMessage(reply);
	}
}