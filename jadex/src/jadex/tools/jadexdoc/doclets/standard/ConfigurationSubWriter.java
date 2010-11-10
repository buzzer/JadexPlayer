package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;
import jadex.util.SUtil;

/**
 *  The writer for initial and end states (configurations).
 */
public class ConfigurationSubWriter extends AbstractSubWriter
{
	/**
	 *
	 * @param writer
	 * @param capability
	 * @param configuration
	 */
	public ConfigurationSubWriter(SubWriterHolderWriter writer, IMCapability capability, StandardConfiguration configuration)
	{
		super(writer, capability, configuration);
	}

	/**
	 *
	 */
	public void printSummaryLabel()
	{
		writer.boldText("doclet.Initial_State_Summary");
	}

	/**
	 *
	 */
	public void printSummaryAnchor()
	{
		writer.anchor("initial_state_summary");
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryAnchor(IMCapability cd)
	{
		writer.anchor("initial_states_inherited_from_capability_"+configuration.getAgentName(cd));
	}

	/**
	 *
	 * @param cd
	 */
	public void printInheritedSummaryLabel(IMCapability cd)
	{
		String classlink = writer.getPreQualifiedAgentLink(cd);
		writer.bold();
		writer.printText("doclet.Initial_States_Inherited_From_Capability", classlink);
		writer.boldEnd();
	}

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected void printMemberSpecificInfo(IMElement member)
	{
		printCapabilities(member);
		printBeliefs(member);
		printInitialGoals(member);
		printInitialPlans(member);
		printInitialEvents(member);
		printEndGoals(member);
		printEndPlans(member);
		printEndEvents(member);
	}

	/**
	 *
	 * @param cd
	 * @return
	 */
	public List getMembers(IMCapability cd)
	{
		IMConfiguration[] members = cd.getConfigurationbase().getConfigurations();
		return SUtil.arrayToList(members);
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
		}

	}

	/**
	 *
	 */
	protected void printHeader()
	{
		writer.anchor("initial_state_detail");
		writer.printTableHeadingBackground(writer.getText("doclet.Initial_State_Detail"));
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
			writer.printHyperLink("", (cd==null)? "initial_state_summary": "initial_states_inherited_from_capability_"
				+configuration.getAgentName(cd), writer.getText("doclet.navInitialState"));
		}
		else
		{
			writer.printText("doclet.navInitialState");
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
			writer.printHyperLink("", "initial_state_detail", writer.getText("doclet.navInitialState"));
		}
		else
		{
			writer.printText("doclet.navInitialState");
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
		}
	}

	/**
	 *  Print the modifiers.
	 *  @param member The member.
	 */
	protected void printModifier(IMElement member)
	{
		printDefault(member);
		print("configuration");
		space();
	}

	/**
	 *  Print "default" for the default initial state.
	 *  @param member The member.
	 */
	protected void printDefault(IMElement member)
	{
		if(member.getScope().getConfigurationbase().getDefaultConfiguration().equals(member))
		{
			print("default");
			space();
		}
	}

	/**
	 *  Print the initial capabilities.
	 *  @param member The member.
	 */
	protected void printCapabilities(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getCapabilitybase()!=null)
		{
			IMConfigCapability[] iss = is.getCapabilitybase().getCapabilityConfigurations();
			for(int i=0; i<iss.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.printText("doclet.Initial_Capabilities");
				}
				writer.dd();
				writer.printAgentLink(((IMCapabilityReference)iss[i].getOriginalElement()).getCapability());
				space();
				writer.italics("Configuration");
				space();
				writer.print(iss[i].getConfiguration());
			}
		}
	}

	/**
	 *  Print the initial beliefs.
	 *  @param member The member.
	 */
	protected void printBeliefs(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getBeliefbase()!=null)
		{
			IMConfigBelief[] ibb = is.getBeliefbase().getInitialBeliefs();
			for(int i=0; i<ibb.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.Initial_Beliefs");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), ibb[i].getOriginalElement(), false);
				space();
				writer.italics(writer.getText("doclet.Initial_Value"));
				space();
				printExpression(ibb[i].getInitialFact());
			}
			IMConfigBeliefSet[] ibsets = is.getBeliefbase().getInitialBeliefSets();
			for(int i=0; i<ibsets.length; i++)
			{
				if(i==0 && ibb.length==0)
				{
					writer.dt();
					writer.boldText("doclet.Initial_Beliefs");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), ibsets[i].getOriginalElement(), false);
				if(ibsets[i].getInitialFacts().length>0)
				{
					IMExpression[] ifs = ibsets[i].getInitialFacts();
					space();
					writer.italics(writer.getText("doclet.Initial_Values"));
					for(int j=0; j<ifs.length; j++)
					{
						space();
						printExpression(ifs[j]);
					}
				}
				else if(ibsets[i].getInitialFactsExpression()!=null)
				{
					space();
					writer.italics(writer.getText("doclet.Initial_Values_Expression"));
					printExpression(ibsets[i].getInitialFactsExpression());
				}
			}
		}
	}

	/**
	 *  Print the initial goals.
	 *  @param member The member.
	 */
	protected void printInitialGoals(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getGoalbase()!=null)
		{
			IMConfigGoal[] igs = is.getGoalbase().getInitialGoals();
			for(int i=0; i<igs.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.Initial_Goals");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), igs[i].getOriginalElement(), false);
				printInitialParameters(igs[i]);
				writer.ddEnd();
			}
			if(igs.length>0)
				writer.dtEnd();
		}
	}
	
	/**
	 *  Print the end goals.
	 *  @param member The member.
	 */
	protected void printEndGoals(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getGoalbase()!=null)
		{
			IMConfigGoal[] fgs = is.getGoalbase().getEndGoals();
			for(int i=0; i<fgs.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.End_Goals");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), fgs[i].getOriginalElement(), false);
				printInitialParameters(fgs[i]);
				writer.ddEnd();
			}
			if(fgs.length>0)
				writer.dtEnd();
		}
	}

	/**
	 *  Print the initial plans.
	 *  @param member The member.
	 */
	protected void printInitialPlans(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getPlanbase()!=null)
		{
			IMConfigPlan[] ips = is.getPlanbase().getInitialPlans();
			for(int i=0; i<ips.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.Initial_Plans");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), ips[i].getOriginalElement(), false);
				printInitialParameters(ips[i]);
				writer.ddEnd();
			}
			if(ips.length>0)
				writer.dtEnd();
		}
	}
	
	/**
	 *  Print the end plans.
	 *  @param member The member.
	 */
	protected void printEndPlans(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getPlanbase()!=null)
		{
			IMConfigPlan[] eps = is.getPlanbase().getEndPlans();
			for(int i=0; i<eps.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.End_Plans");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), eps[i].getOriginalElement(), false);
				printInitialParameters(eps[i]);
				writer.ddEnd();
			}
			if(eps.length>0)
				writer.dtEnd();
		}
	}

	/**
	 *  Print the initial events.
	 *  @param member The member.
	 */
	protected void printInitialEvents(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getEventbase()!=null)
		{
			IMConfigInternalEvent[] ievents = is.getEventbase().getInitialInternalEvents();
			for(int i=0; i<ievents.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.Initial_Internal_Events");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), ievents[i].getOriginalElement(), false);
				printInitialParameters(ievents[i]);
				writer.ddEnd();
			}
			if(ievents.length>0)
				writer.dtEnd();

			IMConfigMessageEvent[] mevents = is.getEventbase().getInitialMessageEvents();
			for(int i=0; i<mevents.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.Initial_Message_Events");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), mevents[i].getOriginalElement(), false);
				printInitialParameters(mevents[i]);
				writer.ddEnd();
			}
			if(mevents.length>0)
				writer.dtEnd();
		}
	}
	
	/**
	 *  Print the initial events.
	 *  @param member The member.
	 */
	protected void printEndEvents(IMElement member)
	{
		IMConfiguration is = (IMConfiguration)member;
		if(is.getEventbase()!=null)
		{
			IMConfigInternalEvent[] eevents = is.getEventbase().getEndInternalEvents();
			for(int i=0; i<eevents.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.End_Internal_Events");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), eevents[i].getOriginalElement(), false);
				printInitialParameters(eevents[i]);
				writer.ddEnd();
			}
			if(eevents.length>0)
				writer.dtEnd();

			IMConfigMessageEvent[] emevents = is.getEventbase().getEndMessageEvents();
			for(int i=0; i<emevents.length; i++)
			{
				if(i==0)
				{
					writer.dt();
					writer.boldText("doclet.End_Message_Events");
				}
				writer.dd();
				writer.printMemberLink(member.getScope(), emevents[i].getOriginalElement(), false);
				printInitialParameters(emevents[i]);
				writer.ddEnd();
			}
			if(emevents.length>0)
				writer.dtEnd();
		}
	}


	/**
	 *
	 */
	protected void printInitialParameters(IMConfigParameterElement elem)
	{
		IMConfigParameter[] iparams = elem.getParameters();
		IMConfigParameterSet[] iparamsets = elem.getParameterSets();

		if(iparams.length>0 || iparamsets.length>0)
		{
			space();
			print("(");
			for(int i=0; i<iparams.length; i++)
			{
				if(i>0)
					space();
				print(iparams[i].getReference());
				print("=");
				printExpression(iparams[i].getInitialValue());
				if(i+1<iparams.length)
					print(",");
			}
			for(int i=0; i<iparamsets.length; i++)
			{
				if(i>0)
					space();
				print(iparamsets[i].getReference());
				if(iparamsets[i].getInitialValues().length>0)
				{
					print("=[");
					IMExpression[] ivs = iparamsets[i].getInitialValues();
					for(int j=0; j<ivs.length; j++)
					{
						printExpression(ivs[j]);
						if(j+1<ivs.length)
						{
							print(",");
							space();
						}
					}
					print("],");
				}
				else if(iparamsets[i].getInitialValuesExpression()!=null)
				{
					print("=");
					printExpression(iparamsets[i].getInitialValuesExpression());
					if(i+1<iparamsets.length)
						print(",");
				}
			}
			print(")");
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printType(IMElement member)
	{
	}

	/**
	 *
	 * @return
	 */
	protected List getStandardMembers()
	{
		// there are no standard members in the initial state base
		return Collections.EMPTY_LIST;
	}
}


