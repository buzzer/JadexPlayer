package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import java.util.List;
import jadex.model.*;
import jadex.tools.jadexdoc.Comment;
import jadex.tools.jadexdoc.doclets.IndexBuilder;

/**
 * Generate Index for all the Member Names with Indexing in
 * Unicode Order.
 */
public class AbstractIndexWriter extends HtmlStandardWriter
{

	/** The index of all the members with unicode character. */
	protected IndexBuilder indexbuilder;

	/**
	 * Initialises path to this file and relative path from this file.
	 * @param path Path to the file which is getting generated.
	 * @param filename Name of the file which is getting genrated.
	 * @param relpath Relative path from this file to the current directory.
	 * @param indexbuilder Unicode based Index from {@link IndexBuilder}
	 */
	protected AbstractIndexWriter(StandardConfiguration configuration,
			String path, String filename,
			String relpath, IndexBuilder indexbuilder)
			throws IOException
	{
		super(configuration, path, filename, relpath);
		this.indexbuilder = indexbuilder;
	}

	/**
	 *
	 * @param configuration
	 * @param filename
	 * @param indexbuilder
	 * @throws IOException
	 */
	protected AbstractIndexWriter(StandardConfiguration configuration,
			String filename, IndexBuilder indexbuilder)
			throws IOException
	{
		super(configuration, filename);
		this.indexbuilder = indexbuilder;
	}

	/**
	 * Print the text "Index" in bold format in the navigation bar.
	 */
	protected void navLinkIndex()
	{
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Index");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Generate the member information for the unicode character along with the
	 * list of the members.
	 * @param unicode Unicode for which member list information to be generated.
	 * @param memberlist List of members for the unicode character.
	 */
	protected void generateContents(Character unicode, List memberlist)
	{
		anchor("_"+unicode+"_");
		h2();
		bold(unicode.toString());
		h2End();
		dl();
		for(int i = 0; i<memberlist.size(); i++)
		{
			IMElement element = (IMElement)memberlist.get(i);
			if(element instanceof IMCapability)
			{
				printDescription((IMCapability)element);
			}
			else if(element instanceof IMReferenceableElement)
			{
				printDescription((IMReferenceableElement)element);
			}
		}
		dlEnd();
		hr();
	}


	/**
	 * Print one line summary comment for the package.
	 * @param pd PackageDoc passed.
	 */
	protected void printDescription(String pd)
	{
		dt();
		printPackageLink(pd, true);
		print(" - ");
		print("package "+pd);
		//dd();
		//        printSummaryComment(new Comment(pd));
		dtEnd();
	}

	/**
	 * Print one line summary comment for the capability.
	 * @param cd IMCapability passed.
	 */
	protected void printDescription(IMCapability cd)
	{
		dt();
		printAgentLink(cd, true);
		print(" - ");
		printAgentDesc(cd);
		dd();
		printSummaryComment(new Comment(cd));
		ddEnd();
		dtEnd();
	}

	/**
	 * What is the agentkind? Print the agentkind(agent, capability)
	 * of the capability passed.
	 * @param cd IMCapability.
	 */
	protected void printAgentDesc(IMCapability cd)
	{
		if(cd instanceof IMBDIAgent)
		{
			print("agent ");
		}
		else
		{
			print("capability ");
		}
		printPreQualifiedAgentLink(cd);
	}

	/**
	 * Generate Description for Beliefs, Goials, Plans, Expression and Events.
	 * @param member member of the Agent Kind.
	 */

	protected void printDescription(IMReferenceableElement member)
	{
		IMCapability containing = member.getScope();

		//String name = member.getName();
		String name = Standard.getMemberName(member);
		dt();
		printMemberLink(containing, member, true);
		println(" - ");
		printMemberDesc(member);
		println();
		dd();
		printSummaryComment(new Comment(member));
		println();
		ddEnd();
		dtEnd();
	}

	/**
	 * Print description about the Type for a member.
	 * @param member IMReferenceableElement.
	 */
	protected void printMemberDesc(IMReferenceableElement member)
	{
		IMCapability containing = member.getScope();

		String classdesc = ((containing instanceof IMBDIAgent)? "agent ": "capability ")+
				getPreQualifiedAgentLink(containing);
		if(member.getOwner() instanceof IMBeliefbase)
			printText("doclet.Belief_in", classdesc);
		else if(member.getOwner() instanceof IMGoalbase)
			printText("doclet.Goal_in", classdesc);
		else if(member.getOwner() instanceof IMPlanbase)
			printText("doclet.Plan_in", classdesc);
		else if(member.getOwner() instanceof IMEventbase)
			printText("doclet.Event_in", classdesc);
		else if(member.getOwner() instanceof IMExpressionbase)
			printText("doclet.Expression_in", classdesc);

	}
}
