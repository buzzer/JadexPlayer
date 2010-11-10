package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;

/**
 *
 */
public class EventSubWriter extends AbstractSubWriter
{
	/**
	 *
	 * @param writer
	 * @param capability
	 * @param configuration
	 */
	public EventSubWriter(SubWriterHolderWriter writer, IMCapability capability, StandardConfiguration configuration)
	{
		super(writer, capability, configuration);
	}

	/**
	 *
	 */
	public void printSummaryLabel()
	{
		writer.boldText("doclet.Event_Summary");
	}

	/**
	 *
	 */
	public void printSummaryAnchor()
	{
		writer.anchor("event_summary");
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryAnchor(IMCapability cd)
	{
		writer.anchor("events_inherited_from_capability_"+configuration.getAgentName(cd));
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryLabel(IMCapability cd)
	{
		String classlink = writer.getPreQualifiedAgentLink(cd);
		writer.bold();
		writer.printText("doclet.Events_Inherited_From_Capability", classlink);
		writer.boldEnd();
	}

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected void printMemberSpecificInfo(IMElement member)
	{
		printEventFlags(member);
		//printReferencedGoal(member);
		//printTemplateExpression(member);
	}

	/**
	 *
	 * @param member
	 * /
	protected void printReferencedGoal(IMReferenceableElement member)
	{
		// todo: repair
		if (member instanceof IMInitialGoal) {
			IMInitialGoal initial = (IMInitialGoal) member;
			IMReferenceableElement refel = initial.getReferencedGoal();
			if (refel != null) {
				IMCapability cap = refel.getScope();
				if (cap != null) {
					writer.dt();
					writer.boldText("doclet.Referenced_From");
					writer.dd();
					String name = refel.getName();
					String caplink = writer.codeText(writer.getAgentLink(cap));
					String memberlink = writer.codeText(writer.getMemberLink(cap, member, false));
					if (cap != element) {
						writer.printText("doclet.in_capability", memberlink, caplink);
					} else {
						writer.print(memberlink);
					}
				}
			}
		}
	}*/

	/**
	 *  Print the event flags.
	 *  @param member
	 */
	protected void printEventFlags(IMElement member)
	{
		//if(member instanceof IMElementReference)
		//	member = ((IMElementReference)member).getOriginalElement();
		if(member instanceof IMEvent)
		{
			IMEvent event = (IMEvent)member;
			writer.dt();
			writer.boldText("doclet.Event_Flags");
			if(event instanceof IMMessageEvent)
			{
				IMMessageEvent mevent = (IMMessageEvent)event;
				writer.dd();
				writer.print("direction: "+mevent.getDirection());
				writer.dd();
				// todo: problem message type may null when wrong jadex.properties are used
				String msgtype = mevent.getMessageType()==null? "": mevent.getMessageType().getName();
				writer.print("message type: "+msgtype);
			}
			writer.dd();
			writer.print("random selection: "+event.isRandomSelection());
			//writer.dd();
			//writer.print("meta-level reasoning: "+event.isMetaLevelReasoning());
			writer.dd();
			writer.print("post to all: "+event.isPostToAll());
		}
	}

//    protected void printTemplateExpression(IMReferenceableElement member) {
//
//        if (member instanceof IMMessageEvent) {
//            IMMessageEvent mevent = (IMMessageEvent) member;
//            MessageTemplate template = mevent.getTemplate();
//            if (template != null) {
//                writer.dt();
//                writer.boldText("doclet.Message_Template");
//                writer.dd();
//                writer.print(template.toString());
//                
//            }
//        }
//    }

	/**
	 *
	 * @param cd
	 * @return
	 */
	public List getMembers(IMCapability cd)
	{

		// if no events return empty list
		if(!configuration.events)
		{
			return new ArrayList();
		}

		List members = new ArrayList();
		IMReferenceableElement[] allmembers = cd.getEventbase().getReferenceableElements();

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
		writer.anchor("event_detail");
		writer.printTableHeadingBackground(writer.getText("doclet.Event_Detail"));
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
			writer.printHyperLink("", (cd==null)? "event_summary": "events_inherited_from_capability_"+configuration.getAgentName(cd), writer.getText("doclet.navEvent"));
		}
		else
		{
			writer.printText("doclet.navEvent");
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
			writer.printHyperLink("", "event_detail", writer.getText("doclet.navEvent"));
		}
		else
		{
			writer.printText("doclet.navEvent");
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printModifier(IMElement member)
	{
		printExported(member);

		if(member instanceof IMMessageEvent)
			print("messageevent");
		else if(member instanceof IMGoalEvent)
			print("goalevent");
		else if(member instanceof IMInternalEvent)
			print("internalevent");

		else if(member instanceof IMMessageEventReference)
			print("messageeventref");
		else if(member instanceof IMGoalEventReference)
			print("goaleventref");
		else if(member instanceof IMInternalEventReference)
			print("internaleventref");

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
		//stdmembers.add(IMEventbase.CONDITION);
		stdmembers.add(IMEventbase.STANDARD_GOAL_EVENT);
		stdmembers.add(IMEventbase.STANDARD_GOAL_EVENT_REFERENCE);
		stdmembers.add(IMEventbase.LEGACY_INTERNAL_EVENT);
		//stdmembers.add(IMEventbase.STANDARD_MESSAGE_EVENT);
		stdmembers.add(IMEventbase.TYPE_CONDITION_TRIGGERED);
		stdmembers.add(IMEventbase.TYPE_CONDITION_TRIGGERED_REFERENCE);
		stdmembers.add(IMEventbase.TYPE_EXECUTEPLAN);
		stdmembers.add(IMEventbase.TYPE_TIMEOUT);
		return stdmembers;
	}


}


