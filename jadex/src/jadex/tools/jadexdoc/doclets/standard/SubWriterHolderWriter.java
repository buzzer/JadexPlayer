package jadex.tools.jadexdoc.doclets.standard;


import java.io.IOException;
import jadex.model.*;

/**
 *
 */
public abstract class SubWriterHolderWriter extends HtmlStandardWriter
{
	/**
	 *
	 * @param configuration
	 * @param filename
	 * @throws IOException
	 */
	public SubWriterHolderWriter(StandardConfiguration configuration, String filename) throws IOException
	{
		super(configuration, filename);
	}

	/**
	 *
	 * @param configuration
	 * @param path
	 * @param filename
	 * @param relpath
	 * @throws IOException
	 */
	public SubWriterHolderWriter(StandardConfiguration configuration,
		String path, String filename, String relpath) throws IOException
	{
		super(configuration, path, filename, relpath);
	}

	/**
	 *
	 */
	public void printTypeSummaryHeader()
	{
		tdIndex();
		font("-1");
		code();
	}

	/**
	 *
	 */
	public void printTypeSummaryFooter()
	{
		codeEnd();
		fontEnd();
		tdEnd();
	}

	/**
	 *
	 * @param sub
	 */
	public void printSummaryHeader(AbstractSubWriter sub)
	{
		sub.printSummaryAnchor();
		tableIndexSummary();
		tableHeaderStart("#CCCCFF");
		sub.printSummaryLabel();
		tableHeaderEnd();
	}

	/**
	 *
	 * @param str
	 */
	public void printTableHeadingBackground(String str)
	{
		tableIndexDetail();
		tableHeaderStart("#CCCCFF", 1);
		bold(str);
		tableHeaderEnd();
		tableEnd();
	}

	/**
	 *
	 * @param sub
	 * @param cap
	 */
	public void printInheritedSummaryHeader(AbstractSubWriter sub, IMCapability cap)
	{
		sub.printInheritedSummaryAnchor(cap);
		tableIndexSummary();
		tableInheritedHeaderStart("#EEEEFF");
		sub.printInheritedSummaryLabel(cap);
		tableInheritedHeaderEnd();
		trBgcolorStyle("white", "TableRowColor");
		summaryRow(0);
		code();
	}

	/**
	 *
	 * @param sub
	 */
	public void printSummaryFooter(AbstractSubWriter sub)
	{
		tableEnd();
		space();
	}

	/**
	 *
	 * @param sub
	 * @param member
	 */
	public void printInheritedSummaryFooter(AbstractSubWriter sub, IMElement member)
	{
		codeEnd();
		summaryRowEnd();
		trEnd();
		tableEnd();
		space();
	}

	/**
	 *
	 * @param member
	 */
	protected void printCommentDef(IMElement member)
	{
		printNbsps();
		printIndexComment(member);
	}

	/**
	 *
	 * @param sub
	 * @param cap
	 * @param member
	 */
	public void printSummaryMember(AbstractSubWriter sub, IMCapability cap, IMElement member)
	{
		printSummaryLinkType(sub, member);
		/*if (cd == null) {
			cd = member.containingClass();
		}*/
		sub.printSummaryLink(cap, member);
		printSummaryLinkComment(sub, member);
	}

	/**
	 *
	 * @param sub
	 * @param member
	 */
	public void printSummaryLinkType(AbstractSubWriter sub, IMElement member)
	{
		trBgcolorStyle("white", "TableRowColor");
		sub.printSummaryType(member);
		summaryRow(0);
		code();
	}

	/**
	 *
	 * @param sub
	 * @param member
	 */
	public void printSummaryLinkComment(AbstractSubWriter sub, IMElement member)
	{
		codeEnd();
		println();
		br();
		printCommentDef(member);
		summaryRowEnd();
		trEnd();
	}

	/**
	 *
	 * @param sub
	 * @param cap
	 * @param member
	 */
	public void printInheritedSummaryMember(AbstractSubWriter sub, IMCapability cap, IMElement member)
	{
		sub.printInheritedSummaryLink(cap, member);
	}

	/**
	 *
	 */
	public void printMemberHeader()
	{
		hr();
	}

	/**
	 *
	 */
	public void printMemberFooter()
	{
	}

}




