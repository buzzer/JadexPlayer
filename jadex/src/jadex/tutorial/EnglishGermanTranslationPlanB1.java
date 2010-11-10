package jadex.tutorial;

import java.util.*;
import jadex.runtime.*;

/**
 *  An initial english german translation plan can translate
 *  english words to german.
 */
public class EnglishGermanTranslationPlanB1 extends Plan
{
	//-------- attributes --------

	/** The wordtable. */
	protected Map wordtable;

	//-------- methods --------

	/**
	 *  Create a new plan.
	 */
	public EnglishGermanTranslationPlanB1()
	{
		System.out.println("Created: "+this);

		this.wordtable = new HashMap();
		this.wordtable.put("coffee", "Kaffee");
		this.wordtable.put("milk", "Milch");
		this.wordtable.put("cow", "Kuh");
		this.wordtable.put("cat", "Katze");
		this.wordtable.put("dog", "Hund");
	}

	//-------- methods --------

	/**
	 *  The plan body.
	 */
	public void body()
	{
		while(true)
		{
			IMessageEvent me = waitForMessageEvent("request_translation");
			String eword = (String)me.getContent();
			String gword = (String)this.wordtable.get(eword);
			if(gword!=null)
			{
				System.out.println("Translating from English to German: "+eword+" - "+gword);
			}
			else
			{
				System.out.println("Sorry word is not in database: "+eword);
			}
		}
	}
}
