package jadex.tools.jadexdoc;

import java.text.BreakIterator;
import java.util.*;
import jadex.model.IMElement;


/**
 * Comment contains all information in comment part.
 * It allows users to get first sentence of this comment.
 */
public class Comment
{

	/**
	 * The comment text.
	 */
	private String text;

	/**
	 * Sentence instance from the BreakIterator.
	 */
	private final BreakIterator sentenceBreaker;


	/**
	 * constructor of Comment.
	 */
	public Comment(String commentString)
	{
		this.text = commentString;
		sentenceBreaker = BreakIterator.getSentenceInstance(Locale.getDefault());

	}

	public Comment(IMElement element)
	{
		this(element.getDescription());
	}


	/**
	 * Return the text of the comment.
	 */
	public String getCommentText()
	{
		return text;
	}


	/**
	 * Return the first sentence of a string, where a sentence ends
	 * with a period followed be white space.
	 */
	public String getFirstSentence()
	{
		return getFirstSentence(text);
	}


	private String getFirstSentence(String s)
	{
		if(s==null || s.length()==0)
		{
			return "";
		}
		int index = s.indexOf("-->");
		if(s.trim().startsWith("<!--") && index!=-1)
		{
			return getFirstSentence(s.substring(index+3, s.length()));
		}
		sentenceBreaker.setText(s.replace('\n', ' '));
		int start = sentenceBreaker.first();
		int end = sentenceBreaker.next();
		String result = s.substring(start, end);

		// Remove HTML tags from sentence.
		StringBuffer res = new StringBuffer();
		StringTokenizer stok = new StringTokenizer(result, "<>", true);
		while(stok.hasMoreTokens())
		{
			String tok = stok.nextToken();
			if(tok.equals("<"))
			{
				tok = stok.nextToken();
				// Don't treat < followed by whitespace or >.
				if(Character.isWhitespace(tok.charAt(0)) || tok.equals(">"))
				{
					res.append("<");
					res.append(tok);
				}
				else
				{
					while(stok.hasMoreTokens() && !stok.nextToken().equals(">")) ;
				}
			}
			else
			{
				res.append(tok);
			}
		}

		return res.toString();
	}


	/**
	 * Return text for this Doc comment.
	 */
	public String toString()
	{
		return text;
	}
}
