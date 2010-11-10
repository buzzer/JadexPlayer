package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;

/**
 *
 */
public class GoalSubWriter extends AbstractSubWriter
{
	/**
	 *
	 * @param writer
	 * @param capability
	 * @param configuration
	 */
	public GoalSubWriter(SubWriterHolderWriter writer, IMCapability capability, StandardConfiguration configuration)
	{
		super(writer, capability, configuration);
	}

	/**
	 *
	 */
	public void printSummaryLabel()
	{
		writer.boldText("doclet.Goal_Summary");
	}

	/**
	 *
	 */
	public void printSummaryAnchor()
	{
		writer.anchor("goal_summary");
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryAnchor(IMCapability cd)
	{
		writer.anchor("goals_inherited_from_capability_"+configuration.getAgentName(cd));
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryLabel(IMCapability cd)
	{
		String classlink = writer.getPreQualifiedAgentLink(cd);
		writer.bold();
		writer.printText("doclet.Goals_Inherited_From_Capability", classlink);
		writer.boldEnd();
	}

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected void printMemberSpecificInfo(IMElement member)
	{
		printBDIFlags(member);
		printUnique(member);
		//printReferencedGoal(member);
		printConditions(member);
		printDeliberation(member);
		printTrigger(member);
	}

	/**
	 *  Print the goal conditions.
	 *  @param member The member.
	 */
	protected void printConditions(IMElement member)
	{
		if(member instanceof IMGoal)
		{
			IMGoal goal = (IMGoal)member;
			printExpression("doclet.Context_Condition", goal.getContextCondition());
			printExpression("doclet.Creation_Condition", goal.getCreationCondition());
			printExpression("doclet.Drop_Condition", goal.getDropCondition());
		}

		if(member instanceof IMPerformGoal)
		{
			// IMPerformGoal goal = (IMPerformGoal)member;
			// nothing more
		}
		else if(member instanceof IMMetaGoal)
		{
			// IMMetaGoal goal = (IMMetaGoal)member;
			// nothing more
		}
		else if(member instanceof IMMaintainGoal)
		{
			IMMaintainGoal goal = (IMMaintainGoal)member;
			printExpression("doclet.Maintain_Condition", goal.getMaintainCondition());
			printExpression("doclet.Target_Condition", goal.getTargetCondition());
		}
		else if(member instanceof IMAchieveGoal)
		{
			IMAchieveGoal goal = (IMAchieveGoal)member;
			printExpression("doclet.Failure_Condition", goal.getFailureCondition());
			printExpression("doclet.Target_Condition", goal.getTargetCondition());
		}
		else if(member instanceof IMQueryGoal)
		{
			IMQueryGoal goal = (IMQueryGoal)member;
			printExpression("doclet.Failure_Condition", goal.getFailureCondition());
			// printExpression("doclet.Target_Condition", goal.getTargetCondition()); // not interesting for query goals
		}
	}

	/**
	 *  Print the BDI flags.
	 *  @param member
	 */
	protected void printBDIFlags(IMElement member)
	{
		//if(member instanceof IMElementReference)
		//	member = ((IMElementReference)member).getOriginalElement();
		if(member instanceof IMGoal)
		{
			IMGoal goal = (IMGoal)member;
			writer.dt();
			writer.boldText("doclet.BDIFlags");
			writer.dd();
			writer.print("retry: "+goal.isRetry());
			writer.ddEnd();
			writer.dd();
			writer.print("retry delay: "+goal.getRetryDelay());
			writer.ddEnd();
			writer.dd();
			writer.print("exclude: "+goal.getExcludeMode());
			writer.ddEnd();
			writer.dd();
			writer.print("random selection: "+goal.isRandomSelection());
			writer.ddEnd();
			//writer.dd();
			//writer.print("meta-level reasoning: "+goal.isMetaLevelReasoning());
			writer.dd();
			writer.print("post to all: "+goal.isPostToAll());
			writer.ddEnd();
			if(goal instanceof IMMaintainGoal)
			{
				IMMaintainGoal mgoal = (IMMaintainGoal)goal;
				writer.dd();
				writer.print("recur: "+mgoal.isRecur());
				writer.ddEnd();
				writer.dd();
				writer.print("recur delay: "+mgoal.getRecurDelay());
				writer.ddEnd();
			}
			writer.dtEnd();
		}
	}

	/**
	 *  Print the uniqueness settings.
	 *  @param member The member.
	 */
	protected void printUnique(IMElement member)
	{
		//if(member instanceof IMElementReference)
		//	member = ((IMElementReference)member).getOriginalElement();
		if(member instanceof IMGoal)
		{
			IMGoal goal = (IMGoal)member;
			IMUnique uni = goal.getUnique();
			if(uni!=null)
			{
				String[] exparams = uni.getExcludes();
				writer.dt();
				writer.boldText("doclet.Unique");
				for(int i=0; i<exparams.length; i++)
				{
					writer.dd();
					writer.print("excluded parameter "+exparams[i]);
				}
			}
		}
	}

	/**
	 *
	 * @param member
	 * /
	protected void printReferencedGoal(IMReferenceableElement member)
	{
		if(member instanceof IMInitialGoal)
		{
			IMInitialGoal initial = (IMInitialGoal)member;
			IMElement refel = initial.getOriginalElement();
			if(refel!=null)
			{
				IMCapability cap = refel.getScope();
				if(cap!=null)
				{
					writer.dt();
					writer.boldText("doclet.Referenced_From");
					writer.dd();
					String caplink = writer.codeText(writer.getAgentLink(cap));
					String memberlink = writer.codeText(writer.getMemberLink(cap, member, false));
					if(cap!=element)
					{
						writer.printText("doclet.in_capability", memberlink, caplink);
					}
					else
					{
						writer.print(memberlink);
					}
				}
			}
		}
	}*/

	/**
	 *  Print deliberation info for goals and goal references.
	 *  @param member The member.
	 */
	protected void printDeliberation(IMElement member)
	{
		IMDeliberation delib = null;

		if(member instanceof IMGoal)
		{
			delib = ((IMGoal)member).getDeliberation();
		}
		else if (member instanceof IMGoalReference)
		{
			delib = ((IMGoalReference)member).getDeliberation();
		}
		if(delib!=null)
		{
			IMInhibits[] inhibits = delib.getInhibits();
			if(inhibits!=null && inhibits.length>0)
			{
				writer.dt();
				writer.boldText("doclet.Deliberation");
				if(delib.getCardinality()!=-1)
				{
					writer.dd();
					writer.print("cardinality");
					writer.space();
					writer.print(delib.getCardinality());
					writer.ddEnd();
				}
				for(int i = 0; i<inhibits.length; i++)
				{
					writer.dd();
					writer.print("inhibits");
					writer.space();
					IMReferenceableElement refel = inhibits[i].getInhibitedGoal();
					IMCapability cap = refel.getScope();
					String caplink = writer.codeText(writer.getAgentLink(cap));
					String memberlink = writer.codeText(writer.getMemberLink(cap, refel, false));
					if(cap!=element)
					{
						writer.printText("doclet.in_capability", memberlink, caplink);
					}
					else
					{
						writer.print(memberlink);
					}
					if(inhibits[i].getDescription()!=null)
					{
						writer.print(" - ");
						writer.print(inhibits[i].getDescription());
					}
					writer.ddEnd();

					IMExpression expression = inhibits[i].getInhibitingExpression();
					if(expression!=null)
					{
						writer.dl();
						writer.dt();
						writer.dd();
						printExpression(expression);
						writer.ddEnd();
						writer.dtEnd();
						writer.dlEnd();
					}
					writer.dtEnd();
				}
			}
		}
	}

	/**
	 *  Print the goal triggers.
	 *  @param member The member.
	 */
	protected void printTrigger(IMElement member)
	{
		if(member instanceof IMMetaGoal)
		{
			writer.dt();
			writer.boldText("doclet.Goal_Trigger");
			writer.dd();
			writer.dl();
			
			IMMetaGoal metagoal = (IMMetaGoal)member;
			IMMetaGoalTrigger trigger = metagoal.getTrigger();
			if(trigger!=null)
			{
				IMReference[] refs = trigger.getGoalFinisheds();
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

				refs = trigger.getGoals();
				for(int i = 0; i<refs.length; i++)
				{
					IMReferenceableElement goal = element.getGoalbase().getReferenceableElement(refs[i].getReference());
					printMemberReference("doclet.Goal_Trigger", goal);
				}
			}
			
			writer.dlEnd();
			writer.ddEnd();
			writer.dtEnd();
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
		IMReferenceableElement[] allmembers = cd.getGoalbase().getReferenceableElements();

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

//        Collections.sort(members);
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
		writer.anchor("goal_detail");
		writer.printTableHeadingBackground(writer.getText("doclet.Goal_Detail"));
	}

	/**
	 *
	 * @param cd
	 * @param link
	 */
	protected void printNavSummaryLink(IMCapability cd, boolean link)
	{
		if(link)
			writer.printHyperLink("", (cd==null)? "goal_summary": "goals_inherited_from_capability_"+configuration.getAgentName(cd), writer.getText("doclet.navGoal"));
		else
			writer.printText("doclet.navGoal");
	}

	/**
	 *
	 * @param link
	 */
	protected void printNavDetailLink(boolean link)
	{
		if(link)
			writer.printHyperLink("", "goal_detail", writer.getText("doclet.navGoal"));
		else
			writer.printText("doclet.navGoal");
	}

	/**
	 *
	 * @param member
	 */
	protected void printModifier(IMElement member)
	{

		printExported(member);

		printAbstract(member);

		if(member instanceof IMMaintainGoal)
			print("maintaingoal");
		else if(member instanceof IMPerformGoal)
			print("performgoal");
		else if(member instanceof IMMetaGoal)
			print("metagoal");
		else if(member instanceof IMQueryGoal)
			print("querygoal");
		else if(member instanceof IMAchieveGoal)
			print("achievegoal");


		if(member instanceof IMMaintainGoalReference)
			print("maintaingoalref");
		else if(member instanceof IMPerformGoalReference)
			print("performgoalref");
		if(member instanceof IMMetaGoalReference)
			print("metagoalref");
		else if(member instanceof IMQueryGoalReference)
			print("querygoalref");
		else if(member instanceof IMAchieveGoalReference)
			print("achievegoalref");

		print(' ');
		//        space();
	}

	/**
	 *
	 * @param member
	 */
	protected void printType(IMElement member)
	{
		//        space();
	}

	/**
	 *
	 * @return
	 */
	protected List getStandardMembers()
	{
		List stdmembers = new ArrayList();
		stdmembers.add(IMGoalbase.DUMMY_GOAL);
		//stdmembers.add(IMGoalbase.META_LEVEL_REASONING_GOAL);
		return stdmembers;
	}
}


