package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;

/**
 *  The writer for beliefs and beliefsets.
 */
public class BeliefSubWriter extends AbstractSubWriter
{
	/**
	 *
	 * @param writer
	 * @param capability
	 * @param configuration
	 */
	public BeliefSubWriter(SubWriterHolderWriter writer, IMCapability capability, StandardConfiguration configuration)
	{
		super(writer, capability, configuration);
	}

	/**
	 *
	 */
	public void printSummaryLabel()
	{
		writer.boldText("doclet.Belief_Summary");
	}

	/**
	 *
	 */
	public void printSummaryAnchor()
	{
		writer.anchor("belief_summary");
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryAnchor(IMCapability cd)
	{
		writer.anchor("beliefs_inherited_from_capability_"+configuration.getAgentName(cd));
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryLabel(IMCapability cd)
	{
		String classlink = writer.getPreQualifiedAgentLink(cd);
		writer.bold();
		writer.printText("doclet.Beliefs_Inherited_From_Capability", classlink);
		writer.boldEnd();
	}

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected void printMemberSpecificInfo(IMElement member)
	{
		printUpdateRate(member);
		printFacts(member);
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public List getMembers(IMCapability cd)
	{
		List members = new ArrayList();
		IMReferenceableElement[] allmembers = cd.getBeliefbase().getReferenceableElements();

		for(int i = 0; i<allmembers.length; i++)
		{
			IMReferenceableElement member = allmembers[i];
			if(!configuration.standardmembers && isStandardMember(member))
			{
				continue;
			}
			if(configuration.exported && !isExportedMember(member))
			{
				continue;
			}
			members.add(member);
		}

//        Collections.sort(members, new ElementNameComparator());
		return members;
	}

	/**
	 *
	 * @param overridden
	 * @param belief
	 */
	protected void printOverridden(IMCapability overridden, IMBelief belief)
	{
		if(configuration.nocomment)
		{
			return;
		}
		String label = "doclet.Overrides";

		if(belief!=null)
		{
			String overriddenclasslink = writer.codeText(writer.getAgentLink(overridden));
			String methlink = "";
			String name = Standard.getMemberName(belief);
			writer.dt();
			writer.boldText(label);
			writer.dd();
			methlink = writer.codeText(writer.getAgentLink(overridden, name, name, false));
			writer.printText("doclet.in_agent", methlink, overriddenclasslink);
			writer.ddEnd();
			writer.dtEnd();
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printComment(IMBelief member)
	{
		if(configuration.nocomment)
		{
			return;
		}
		if(member.getDescription()!=null)
		{
			writer.dd();
			writer.printInlineComment(member);
			writer.ddEnd();
		}
	}

	/**
	 *
	 */
	protected void printHeader()
	{
		writer.anchor("belief_detail");
		writer.printTableHeadingBackground(writer.getText("doclet.Belief_Detail"));
	}

	/**
	 *
	 * @param cd
	 * @param link
	 */
	protected void printNavSummaryLink(IMCapability cd, boolean link)
	{
		if(link)
		{
			writer.printHyperLink("", (cd==null)? "belief_summary": "beliefs_inherited_from_capability_"
				+configuration.getAgentName(cd), writer.getText("doclet.navBelief"));
		}
		else
		{
			writer.printText("doclet.navBelief");
		}
	}

	/**
	 *
	 * @param link
	 */
	protected void printNavDetailLink(boolean link)
	{
		if(link)
		{
			writer.printHyperLink("", "belief_detail", writer.getText("doclet.navBelief"));
		}
		else
		{
			writer.printText("doclet.navBelief");
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printModifier(IMElement member)
	{
		printExported(member);
		printAbstract(member);

		if(member instanceof IMBelief)
			print("belief ");
		else if(member instanceof IMBeliefSet)
			print("beliefset ");
		else if(member instanceof IMBeliefReference)
			print("beliefref ");
		else if(member instanceof IMBeliefSetReference)
			print("beliefsetref ");
	}

	/**
	 *
	 * @param member
	 */
	protected void printType(IMElement member)
	{
		Class clazz = null;

		if(member instanceof IMTypedElement)
			clazz = ((IMTypedElement)member).getClazz();
		else if(member instanceof IMTypedElementSet)
			clazz = ((IMTypedElementSet)member).getClazz();

		else if(member instanceof IMTypedElementReference)
			clazz = ((IMTypedElementReference)member).getClazz();
		else if(member instanceof IMTypedElementSetReference)
			clazz = ((IMTypedElementSetReference)member).getClazz();

		printTypeLink(clazz);
		print(' ');
		//        writer.space();
	}

	/**
	 *  Print out the update rate.
	 *  @param member The member.
	 */
	protected void printUpdateRate(IMElement member)
	{
		if(member instanceof IMElementReference)
			member = ((IMElementReference)member).getOriginalElement();

		long rate = 0;
		if(member instanceof IMTypedElement && ((IMTypedElement)member).getUpdateRate()!=0)
		{
			rate = ((IMTypedElement)member).getUpdateRate();
		}
		else if(member instanceof IMTypedElementSet && ((IMTypedElementSet)member).getUpdateRate()!=0)
		{
			rate = ((IMTypedElementSet)member).getUpdateRate();
		}

		if(rate>0)
		{
			writer.dt();
			writer.bold("Update rate:");
			writer.dd();
			print(rate+" ms");
			writer.ddEnd();
			writer.dtEnd();
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printFacts(IMElement member)
	{
		IMExpression[] expressions = null;
		String text = "";

		if(member instanceof IMElementReference)
			member = ((IMElementReference)member).getOriginalElement();

		/*if(member instanceof IMTypedElementReference)
		{

			//  get fact from the referenced element
			while(!(member instanceof IMTypedElement) && !((IMTypedElementReference)member).isAbstract())
			{
				member = ((IMTypedElementReference)member).getReferencedElement();
			}
		}

		if(member instanceof IMTypedElementSetReference)
		{
			//  get facts from the referenced element
			while(!(member instanceof IMTypedElementSet) && !((IMTypedElementSetReference)member).isAbstract())
			{
				member = ((IMTypedElementSetReference)member).getReferencedElement();
			}
		}*/

		if(member instanceof IMTypedElement)
		{
			text = "doclet.Default_Fact";
			if(((IMBelief)member).getDefaultFact()!=null)
			{
				expressions = new IMExpression[]{((IMBelief)member).getDefaultFact()};
			}
		}
		else if(member instanceof IMTypedElementSet)
		{
			if(((IMBeliefSet)member).getDefaultFacts().length>0)
			{
				text = "doclet.Default_Facts";
				expressions = ((IMBeliefSet)member).getDefaultFacts();
			}
			else if(((IMBeliefSet)member).getDefaultFactsExpression()!=null)
			{
				text = "doclet.Default_Facts_Expression";
				expressions = new IMExpression[]{((IMBeliefSet)member).getDefaultFactsExpression()};
			}
		}

		if(expressions!=null && expressions.length>0)
		{
			writer.dt();
			writer.boldText(text);
			if(IMExpression.MODE_DYNAMIC.equals(expressions[0].getEvaluationMode()))
				writer.bold(" ("+IMExpression.MODE_DYNAMIC+")");
			for(int i = 0; i<expressions.length; i++)
			{
				writer.dd();
				printExpression(expressions[i]);
				if(expressions[i].getDescription()!=null)
				{
					writer.print(" - ");
					writer.print(expressions[i].getDescription());
				}
				writer.ddEnd();
			}
			writer.dtEnd();
		}
	}

	/**
	 *
	 * @return
	 */
	protected List getStandardMembers()
	{
		// there are no standard members in beliefbase
		return Collections.EMPTY_LIST;
	}
}


