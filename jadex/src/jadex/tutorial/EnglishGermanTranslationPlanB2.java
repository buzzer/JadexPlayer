package jadex.tutorial;

import java.util.*;
import jadex.runtime.*;

/**
 *  An english german translation plan can translate
 *  english words to german and is instantiated on demand.
 */
public class EnglishGermanTranslationPlanB2 extends Plan
{
	//-------- attributes --------

	/** The wordtable. */
	protected Map wordtable;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public EnglishGermanTranslationPlanB2()
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
	 *  Execute the plan.
	 */
	public void body()
	{
		String eword = (String)((IMessageEvent)getInitialEvent()).getContent();
		String gword = (String)this.wordtable.get(eword);
		if(gword!=null)
		{
			System.out.println("Translating from english to german: "+eword+" - "+gword);
		}
		else
		{
			System.out.println("Sorry word is not in database: "+eword);
		}
	}
}
