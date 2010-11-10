package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.doclets.ElementNameComparator;

/**
 *  Sub writer for plans.
 */
public class PlanSubWriter extends AbstractSubWriter
{
	/**
	 *  Create a new writer.
	 *  @param writer The holding writer.
	 *  @param capability The capability or agent.
	 *  @param configuration The configuration.
	 */
	public PlanSubWriter(SubWriterHolderWriter writer, IMCapability capability, StandardConfiguration configuration)
	{
		super(writer, capability, configuration);
	}

	/**
	 *
	 */
	public void printSummaryLabel()
	{
		writer.boldText("doclet.Plan_Summary");
	}

	/**
	 *
	 */
	public void printSummaryAnchor()
	{
		writer.anchor("plan_summary");
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryAnchor(IMCapability cd)
	{
		writer.anchor("plans_inherited_from_capability_"+configuration.getAgentName(cd));
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryLabel(IMCapability cd)
	{
		String classlink = writer.getPreQualifiedAgentLink(cd);
		writer.bold();
		writer.printText("doclet.Plans_Inherited_From_Capability", classlink);
		writer.boldEnd();
	}

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected void printMemberSpecificInfo(IMElement member)
	{
		printPlanbody(member);
		printTrigger(member);
		printWaitqueue(member);
	}

	/**
	 *  Print the information about the plan body.
	 *  @param member The member.
	 */
	protected void printPlanbody(IMElement member)
	{
		// print elements such as parameter, plan_body etc.
		if(member instanceof IMPlan)
		{
			IMPlan plan = (IMPlan)member;

			if(!plan.getBody().isInline())
			{
				printExpression("doclet.Planbody", plan.getBody());
				/*if(plan.getBody().getStaticType()!=null)
					result += "<DD><CODE>" + writer.getClassLink(plan.getBody().getStaticType(), plan.getBody().getExpressionText()) + "</CODE>";
				else
					result += "<DD><CODE>" + plan.getBody().getExpressionText() + "</CODE>";*/
			}
			else
			{
				printText("doclet.Inline_Planbody", HtmlStandardWriter.replace(plan.getBody().getExpressionText().trim(),"\n", "<br>"));
				if(plan.getBody().getPassedCode()!=null && plan.getBody().getPassedCode().length()>0)
					printText("doclet.Inline_Passed_Code", HtmlStandardWriter.replace(plan.getBody().getPassedCode().trim(),"\n", "<br>"));
				if(plan.getBody().getFailedCode()!=null && plan.getBody().getAbortedCode().length()>0)
					printText("doclet.Inline_Failed_Code", HtmlStandardWriter.replace(plan.getBody().getFailedCode().trim(),"\n", "<br>"));
				if(plan.getBody().getAbortedCode()!=null && plan.getBody().getAbortedCode().length()>0)
					printText("doclet.Inline_Aborted_Code", HtmlStandardWriter.replace(plan.getBody().getAbortedCode().trim(),"\n", "<br>"));
				//printText("doclet.Inline_Planbody", "<pre>"+plan.getBody().getExpressionText()+"</pre>");
				//printText("doclet.Inline_Passed_Code", "<pre>"+plan.getBody().getPassedCode()+"</pre>");
				//printText("doclet.Inline_Failed_Code", "<pre>"+plan.getBody().getFailedCode()+"</pre>");
				//printText("doclet.Inline_Aborted_Code", "<pre>"+plan.getBody().getAbortedCode()+"</pre>");
			}

			if(plan.getBody().getDescription()!=null)
			{
				print(" - "+plan.getBody().getDescription());
			}
		}
	}

	/**
	 *  Print the information about the plan trigger.
	 *  @param member The member.
	 */
	protected void printTrigger(IMElement member)
	{
		if(member instanceof IMPlan)
		{
			IMPlan plan = (IMPlan)member;
			IMPlanTrigger trigger = plan.getTrigger();
			if(trigger!=null)
			{
				writer.dt();
				writer.boldText("doclet.Plan_trigger");
				writer.dd();
				writer.dl();
				
				IMReference[] refs = trigger.getGoals();
				for(int i = 0; i<refs.length; i++)
				{
					IMReferenceableElement goal = element.getGoalbase().getReferenceableElement(refs[i].getReference());
					printMemberReference("doclet.Goals_Trigger", goal);
				}

				refs = trigger.getGoalFinisheds();
				for(int i = 0; i<refs.length; i++)
				{
					IMReferenceableElement goal = element.getGoalbase().getReferenceableElement(refs[i].getReference());
					printMemberReference("doclet.GoalFinished_Trigger", goal);
				}

				refs = trigger.getInternalEvents();
				for(int i = 0; i<refs.length; i++)
				{
					IMReferenceableElement ievent = element.getEventbase().getReferenceableElement(refs[i].getReference());
					printMemberReference("doclet.Internal_Event_Trigger", ievent);
				}

				refs = trigger.getMessageEvents();
				for(int i = 0; i<refs.length; i++)
				{
					IMReferenceableElement mevent = element.getEventbase().getReferenceableElement(refs[i].getReference());
					printMemberReference("doclet.Message_Event_Trigger", mevent);
				}

				if(trigger.getFilter()!=null)
					printExpression("doclet.Filter", trigger.getFilter());

				if(trigger.getCondition()!=null)
					printExpression("doclet.Condition", trigger.getCondition());

				String[] belchanges = trigger.getBeliefChanges();
				for(int i = 0; i<belchanges.length; i++)
				{
					IMReferenceableElement bel = member.getScope().getBeliefbase().getReferenceableElement(belchanges[i]);
					printMemberReference("doclet.Belief_Change_Trigger", bel);
				}

				String[] belsetchanges = trigger.getBeliefSetChanges();
				for(int i = 0; i<belsetchanges.length; i++)
				{
					IMReferenceableElement belset = member.getScope().getBeliefbase().getReferenceableElement(belsetchanges[i]);
					printMemberReference("doclet.Beliefset_Change_Trigger", belset);
				}

				String[] fadds = trigger.getFactAddedTriggers();
				for(int i = 0; i<fadds.length; i++)
				{
					IMReferenceableElement belset = member.getScope().getBeliefbase().getReferenceableElement(fadds[i]);
					printMemberReference("doclet.Fact_Added_Trigger", belset);
				}

				String[] frems = trigger.getFactRemovedTriggers();
				for(int i = 0; i<frems.length; i++)
				{
					IMReferenceableElement belset = member.getScope().getBeliefbase().getReferenceableElement(frems[i]);
					printMemberReference("doclet.Fact_Removed_Trigger", belset);
				}
				
				writer.dlEnd();
				writer.ddEnd();
				writer.dtEnd();
			}
		}
	}

	/**
	 *  Print the information about the plan trigger.
	 *  @param member The member.
	 */
	protected void printWaitqueue(IMElement member)
	{
		if(member instanceof IMPlan)
		{
			IMPlan plan = (IMPlan)member;
			IMTrigger waitqueue = plan.getWaitqueue();
			if(waitqueue!=null)
			{
				writer.dt();
				writer.boldText("doclet.Plan_waitqueue");
				writer.dd();
				
				IMReference[] refs = waitqueue.getGoalFinisheds();
				IMReferenceableElement[] tmp = new IMReferenceableElement[refs.length];
				for(int i = 0; i<refs.length; i++)
				{
					tmp[i] = element.getGoalbase().getReferenceableElement(refs[i].getReference());
				}
				printMemberReferences("doclet.GoalFinished_Trigger", tmp);
				
				refs = waitqueue.getInternalEvents();
				tmp = new IMReferenceableElement[refs.length];
				for(int i = 0; i<refs.length; i++)
				{
					tmp[i] = element.getEventbase().getReferenceableElement(refs[i].getReference());
				}
				printMemberReferences("doclet.Internal_Event_Trigger", tmp);
				
				refs = waitqueue.getMessageEvents();
				tmp = new IMReferenceableElement[refs.length];
				for(int i = 0; i<refs.length; i++)
				{
					tmp[i] = element.getEventbase().getReferenceableElement(refs[i].getReference());
				}
				printMemberReferences("doclet.Message_Event_Trigger", tmp);
				
				if(waitqueue.getFilter()!=null)
					printExpression("doclet.Filter", waitqueue.getFilter());
				
				writer.ddEnd();
				writer.dtEnd();
			}
		}
	}
	
	/**
	 *
	 * @param cd
	 * @return
	 */
	public List getMembers(IMCapability cd)
	{
		List members = new ArrayList();
		IMReferenceableElement[] allmembers = cd.getPlanbase().getReferenceableElements();

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
	 * @param method
	 */
	protected void printOverridden(IMCapability overridden, IMElement method)
	{
		if(configuration.nocomment)
		{
			return;
		}
		String label = "doclet.Overrides";

		if(method!=null)
		{

			String overriddenclasslink = writer.codeText(writer.getAgentLink(overridden));
			String methlink = "";
			String name = method.getName();
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
	 * @param method
	 */
	protected void printImplementsInfo(IMReferenceableElement method)
	{
		if(configuration.nocomment)
		{
			return;
		}
		IMCapability intfac = method.getScope();
		if(intfac!=null)
		{
			String methlink = "";
			String intfaclink = writer.codeText(writer.getAgentLink(intfac));
			writer.dt();
			writer.boldText("doclet.Specified_By");
			writer.dd();
			methlink = writer.codeText(writer.getAgentLink(intfac, method.getName()));
			writer.printText("doclet.in_capability", methlink, intfaclink);
			writer.ddEnd();
			writer.dtEnd();
		}

	}

	/**
	 *
	 */
	protected void printHeader()
	{
		writer.anchor("plan_detail");
		writer.printTableHeadingBackground(writer.getText("doclet.Plan_Detail"));
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
			writer.printHyperLink("", (cd==null)? "plan_summary": "plans_inherited_from_capability_"+configuration.getAgentName(cd), writer.getText("doclet.navPlan"));
		}
		else
		{
			writer.printText("doclet.navPlan");
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
			writer.printHyperLink("", "plan_detail", writer.getText("doclet.navPlan"));
		}
		else
		{
			writer.printText("doclet.navPlan");
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printModifier(IMElement member)
	{
		printExported(member);

		if(member instanceof IMPlan)
		{
			IMPlan plan = (IMPlan)member;
			print(plan.getBody().getType());
			writer.space();

			/* if (plan.getInitial()) {
				 print("initial");
 //                print(' ');
				 writer.space();
			 }*/ //todo: refactor

			print("plan");
			print(' ');
		}


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
		return Collections.EMPTY_LIST;
	}

	/**
	 *  Print parameter(set) info summary.
	 *  @param member The member.
	 *  @param linebreak True, for line break.
	 */
	protected void printParameters(IMElement member, boolean linebreak)
	{
		//if(member instanceof IMElementReference)
		//	member = ((IMElementReference)member).getOriginalElement();
		boolean first = true;

		if(member instanceof IMParameterElement)
		{
			IMParameterElement paramel = (IMParameterElement)member;

			IMParameter[] params = paramel.getParameters();
			IMParameterSet[] paramsets = paramel.getParameterSets();
			Arrays.sort(params, new ElementNameComparator());
			Arrays.sort(paramsets, new ElementNameComparator());

			print('(');
			String indent = makeSpace(writer.displayLength);

			for(int i=0; i<params.length; i++)
			{
				Class clazz = params[i].getClazz();
				if(!first)
				{
					writer.print(", ");
					if(linebreak)
					{
						writer.print('\n');
						writer.print(indent);
					}
				}
				printTypedName(clazz, Standard.getMemberName(params[i]));
				first = false;
			}

			for(int i=0; i<paramsets.length; i++)
			{
				Class clazz = paramsets[i].getClazz();
				if(!first)
				{
					writer.print(", ");
					if(linebreak)
					{
						writer.print('\n');
						writer.print(indent);
					}
				}
				printTypedName(clazz, "[set] "+Standard.getMemberName(paramsets[i]));
				//writer.space();
				//writer.print("(parameterset)");
				first = false;
			}

			print(')');
		}
	}
}


