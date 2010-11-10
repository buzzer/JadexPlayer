package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;

/**
 *
 */
public class ExpressionSubWriter extends AbstractSubWriter
{
	/**
	 *
	 * @param writer
	 * @param capability
	 * @param configuration
	 */
	public ExpressionSubWriter(SubWriterHolderWriter writer, IMCapability capability, StandardConfiguration configuration)
	{
		super(writer, capability, configuration);
	}

	/**
	 *
	 */
	public void printSummaryLabel()
	{
		writer.boldText("doclet.Expression_Summary");
	}

	/**
	 *
	 */
	public void printSummaryAnchor()
	{
		writer.anchor("expression_summary");
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryAnchor(IMCapability cd)
	{
		writer.anchor("expressions_inherited_from_capability_"+configuration.getAgentName(cd));
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryLabel(IMCapability cd)
	{
		String classlink = writer.getPreQualifiedAgentLink(cd);
		writer.bold();
		writer.printText("doclet.Expressions_Inherited_From_Capability", classlink);
		writer.boldEnd();
	}

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected void printMemberSpecificInfo(IMElement member)
	{
		printExpression("doclet.Expression", member, true);
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public List getMembers(IMCapability cd)
	{
		// if no expression return empty list
		if(!configuration.expressions)
		{
			return new ArrayList();
		}

		List members = new ArrayList();
		IMReferenceableElement[] allmembers = cd.getExpressionbase().getReferenceableElements();

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
	 * @param goal
	 */
	protected void printOverridden(IMCapability overridden, IMElement goal)
	{
		if(configuration.nocomment)
		{
			return;
		}
		String label = "doclet.Overrides";

		if(goal!=null)
		{

			String overriddenclasslink = writer.codeText(writer.getAgentLink(overridden));
			String methlink = "";
			String name = Standard.getMemberName(goal);
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
	 * @param goal
	 */
	protected void printImplementsInfo(IMReferenceableElement goal)
	{
		if(configuration.nocomment)
		{
			return;
		}
		IMCapability intfac = goal.getScope();
		if(intfac!=null)
		{
			String methlink = "";
			String intfaclink = writer.codeText(writer.getAgentLink(intfac));
			writer.dt();
			writer.boldText("doclet.Specified_By");
			writer.dd();
			methlink = writer.codeText(writer.getAgentLink(intfac, Standard.getMemberName(goal)));
			writer.printText("doclet.in_capability", methlink, intfaclink);
		}

	}

	/**
	 *
	 */
	protected void printHeader()
	{
		writer.anchor("expression_detail");
		writer.printTableHeadingBackground(writer.getText("doclet.Expression_Detail"));
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
			writer.printHyperLink("", (cd==null)? "expression_summary": "expressions_inherited_from_capability_"+configuration.getAgentName(cd), writer.getText("doclet.navExpression"));
		}
		else
		{
			writer.printText("doclet.navExpression");
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
			writer.printHyperLink("", "expression_detail", writer.getText("doclet.navExpression"));
		}
		else
		{
			writer.printText("doclet.navExpression");
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

		if(member instanceof IMCondition)
		{
			print("condition");
		}
		else if(member instanceof IMExpression)
		{
			print("expression");
		}
		if(member instanceof IMConditionReference)
		{
			print("conditionref");
		}
		else if(member instanceof IMExpressionReference)
		{
			print("expressionref");
		}

		print(' ');
		//        writer.space();
	}

	/**
	 *
	 * @param member
	 */
	protected void printType(IMElement member)
	{
		//        writer.space();
	}

	/**
	 *
	 * @return
	 */
	protected List getStandardMembers()
	{
		List stdmembers = new ArrayList();
		stdmembers.add(IMExpressionbase.STANDARD_EXPRESSION_REFERENCE);
		return stdmembers;
	}

}


