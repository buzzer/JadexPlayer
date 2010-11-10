package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import jadex.model.*;
import jadex.tools.jadexdoc.doclets.*;
import jadex.util.SUtil;

/**
 *  The base class for all sub writers (for beliefs, plans, goals, etc.)
 */
public abstract class AbstractSubWriter
{
	protected boolean printedSummaryHeader = false;
	protected final SubWriterHolderWriter writer;
	protected final IMCapability element;

	public List visibleClasses = new ArrayList();
	public Map visibleMembers = new HashMap();
	StandardConfiguration configuration;

	/**
	 *
	 * @param writer
	 * @param element
	 * @param configuration
	 */
	public AbstractSubWriter(SubWriterHolderWriter writer, IMCapability element, StandardConfiguration configuration)
	{
		this.writer = writer;
		this.element = element;
		this.configuration = configuration;
		buildVisibleMemberMap();

	}

	/**
	 *
	 * @param writer
	 * @param configuration
	 */
	public AbstractSubWriter(SubWriterHolderWriter writer, StandardConfiguration configuration)
	{
		this(writer, null, configuration);
	}

	/**
	 *
	 * @param member
	 */
	protected void printMember(IMElement member)
	{
		writer.anchor(writer.getMemberAnchor(member));
		printHead(member);
		printSignature(member);
		printFullCommentAndInfo(member);
	}

	/**
	 *
	 * @param member
	 */
	protected void printHead(IMElement member)
	{
		writer.h3();
		//writer.print(member.getName());
		writer.print(Standard.getMemberName(member));
		writer.h3End();
	}

	/**
	 *
	 * @param member
	 */
	protected void printSignature(IMElement member)
	{
		writer.displayLength = 0;
		writer.pre();
		printModifier(member);
		printType(member);
		bold(Standard.getMemberName(member));
		printParameters(member, true);
		writer.preEnd();
	}

	/**
	 *
	 * @param member
	 */
	protected void printFullCommentAndInfo(IMElement member)
	{
		if(configuration().nocomment)
		{
			return;
		}
		writer.dl();
		printComment(member);
		printMemberInfo(member);
		writer.dlEnd();
	}

	/**
	 *
	 * @param member
	 */
	protected void printComment(IMElement member)
	{
		if(writer.configuration.nocomment)
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
	 *  Print all information about the member.
	 *  @param member The member.
	 */
	protected void printMemberInfo(IMElement member)
	{
		printInfoHeader();
		printParameterInfo(member);
//        printBindings(member);
		//printFacts(member);
		printMemberSpecificInfo(member);
		printConcrete(member);
		printAssignTo(member);
		printInfoFooter();
	}

	/**
	 *
	 */
	public abstract void printSummaryLabel();

	/**
	 *
	 * @param cd
	 */
	public abstract void printInheritedSummaryLabel(IMCapability cd);

	/**
	 *
	 */
	public abstract void printSummaryAnchor();

	/**
	 *
	 * @param cd
	 */
	public abstract void printInheritedSummaryAnchor(IMCapability cd);

	/**
	 *
	 * @param member
	 */
	protected void printSummaryType(IMElement member)
	{
		writer.printTypeSummaryHeader();
		printModifier(member);
		printType(member);
		writer.printTypeSummaryFooter();
	}

	/**
	 *
	 * @param cd
	 * @param member
	 */
	protected void printSummaryLink(IMCapability cd, IMElement member)
	{
		String name = Standard.getMemberName(member);
		writer.bold();
		writer.printMemberLink(cd, member, false);
		writer.boldEnd();
		writer.displayLength = name.length();
		printParameters(member, false);
	}

	/**
	 *
	 * @param cd
	 * @param member
	 */
	protected void printInheritedSummaryLink(IMCapability cd, IMElement member)
	{
		writer.printMemberLink(cd, member, false);
	}

	/**
	 *
	 */
	protected abstract void printHeader();

	//    protected abstract void printBodyHtmlEnd(IMElement cd);

	//    protected abstract void printElements(IMElement elem);

	/**
	 *
	 * @param elem
	 * @return
	 */
	protected abstract List getMembers(IMCapability elem);

	//    protected abstract void printDeprecatedLink(IMElement member);

	/**
	 *
	 * @param cd
	 * @param link
	 */
	protected abstract void printNavSummaryLink(IMCapability cd, boolean link);

	/**
	 *
	 * @param link
	 */
	protected abstract void printNavDetailLink(boolean link);

	/**
	 *
	 * @param member
	 */
	protected abstract void printModifier(IMElement member);

	/**
	 *
	 * @param member
	 */
	protected abstract void printType(IMElement member);

	/**
	 *
	 */
	protected void space()
	{
		writer.space();
		writer.displayLength += 1;
	}

	/**
	 *
	 * @param str
	 */
	protected void print(String str)
	{
		writer.print(str);
		writer.displayLength += str.length();
	}

	/**
	 *
	 * @param ch
	 */
	protected void print(char ch)
	{
		writer.print(ch);
		writer.displayLength++;
	}

	/**
	 *
	 * @param str
	 */
	protected void bold(String str)
	{
		writer.bold(str);
		writer.displayLength += str.length();
	}

	/**
	 *
	 * @param len
	 * @return
	 */
	protected String makeSpace(int len)
	{
		if(len<=0)
		{
			return "";
		}
		StringBuffer sb = new StringBuffer(len);
		for(int i = 0; i<len; i++)
		{
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 *
	 * @param param
	 */
	protected void printParam(IMParameter param)
	{
		printTypedName(param.getClazz(), Standard.getMemberName(param));
	}

	/**
	 *
	 * @param type
	 * @param name
	 */
	protected void printTypedName(Class type, String name)
	{
		if(type!=null)
		{
			printTypeLink(type);
		}
		if(name.length()>0)
		{
			writer.space();
			writer.print(name);
		}
	}

	/**
	 *
	 * @param type
	 */
	protected void printTypeLink(Class type)
	{
		if(type==null)
			return;
		StringBuffer dimension = new StringBuffer(0);
		while(type.getComponentType()!=null)
		{
			type = type.getComponentType();
			dimension.append("[]");
		}
		printTypeLinkNoDimension(type);
		print(dimension.toString());
	}

	/**
	 *
	 * @param type
	 */
	protected void printTypeLinkNoDimension(Class type)
	{
		if(type==null)
			return;

		writer.printClassLink(type);
	}

	/**
	 *
	 * @param member
	 * @return
	 */
	protected String name(IMElement member)
	{
		return Standard.getMemberName(member);
	}

	/**
	 * Forward to containing writer
	 */
	public void printSummaryHeader()
	{
		printedSummaryHeader = true;
		writer.printSummaryHeader(this);
	}

	/**
	 * Forward to containing writer
	 */
	public void printInheritedSummaryHeader(IMCapability cd)
	{
		writer.printInheritedSummaryHeader(this, cd);
	}

	/**
	 * Forward to containing writer
	 */
	public void printInheritedSummaryFooter(IMElement cd)
	{
		writer.printInheritedSummaryFooter(this, cd);
	}

	/**
	 * Forward to containing writer
	 */
	public void printSummaryFooter()
	{
		writer.printSummaryFooter(this);
	}

	/**
	 * Forward to containing writer
	 */
	public void printSummaryMember(IMCapability cd, IMElement member)
	{
		writer.printSummaryMember(this, cd, member);
	}

	/**
	 * Forward to containing writer
	 */
	public void printInheritedSummaryMember(IMCapability cd, IMElement member)
	{
		writer.printInheritedSummaryMember(this, cd, member);
	}

	/**
	 *
	 */
	public void printMembers()
	{
		List members = getMembers(element);
		if(members.size()>0)
		{
			printHeader();
			for(int i = 0; i<members.size(); i++)
			{
				if(i>0)
				{
					writer.printMemberHeader();
				}
				writer.println("");
				printMember((IMElement)(members.get(i)));
				writer.printMemberFooter();
			}
		}
	}

	/**
	 * Print use info.
	 */
	protected void printUseInfo(Object mems, String heading)
	{
		if(mems==null)
		{
			return;
		}
		List members = (List)mems;
		if(members.size()>0)
		{
			writer.tableIndexSummary();
			writer.tableUseInfoHeaderStart("#CCCCFF");
			writer.print(heading);
			writer.tableHeaderEnd();
			/*
			for (Iterator it = members.iterator(); it.hasNext(); ) {

				ProgramElementDoc pgmdoc = (ProgramElementDoc)it.next();
				ClassDoc cd = pgmdoc.containingClass();

				writer.printSummaryLinkType(this, pgmdoc);
				if (cd != null && !(pgmdoc instanceof ConstructorDoc)
							   && !(pgmdoc instanceof ClassDoc)) {
					// Add class context
					writer.bold(cd.name() + ".");
				}
				printSummaryLink(cd, pgmdoc);
				writer.printSummaryLinkComment(this, pgmdoc);

			}   */

			writer.tableEnd();
			writer.space();
			writer.p();
		}
	}

	/**
	 *
	 */
	protected void navSummaryLink()
	{
		List members = getMembers(element);
		if(members.size()>0)
		{
			printNavSummaryLink(null, true);
			return;
		}
		else
		{
			/*
			IMCapability icd = (IMCapability) element.getOwner();
			List capabilities = Arrays.asList(((IMCapability) element).getCapabilityReferences());
			for (int i = 0; i < capabilities.size(); i++) {
				IMCapability capability = ((IMCapabilityReference) capabilities.get(i)).getCapability();
				List inhmembers = getMembers(icd);
				if (inhmembers.size() > 0) {
					printNavSummaryLink(icd, true);
					return;
				}
			}
			*/
		}
		printNavSummaryLink(null, false);
	}

	/**
	 *
	 */
	protected void navDetailLink()
	{
		List members = getMembers(element);
		printNavDetailLink(members.size()>0? true: false);
	}

	/**
	 *
	 * @return
	 */
	public StandardConfiguration configuration()
	{
		return writer.configuration;
	}

	/**
	 *
	 * @return
	 */
	public MessageRetriever msg()
	{
		return writer.msg(true);
	}

	/**
	 *
	 * @param member
	 */
	protected void printExported(IMElement member)
	{
		if(member instanceof IMReferenceableElement)
		{
			if(IMReferenceableElement.EXPORTED_TRUE.equals(((IMReferenceableElement)member).getExported()))
			{
				print("exported");
				space();
			}
			else if(IMReferenceableElement.EXPORTED_SHIELDED.equals(((IMReferenceableElement)member).getExported()))
			{
				print("exported_shielded");
				space();
			}
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printAbstract(IMElement member)
	{
		if(member instanceof IMElementReference)
		{
			if(((IMElementReference)member).isAbstract())
			{
				print("abstract");
				space();
				if(((IMElementReference)member).isRequired())
				{
					print("required");
					space();
				}
			}
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printConcrete(IMElement member)
	{
		IMElementReference elref;
		IMReferenceableElement refel;
		IMCapability cap;

		if(member instanceof IMElementReference)
		{
			elref = (IMElementReference)member;
			refel = elref.getReferencedElement();
			if(refel!=null)
			{
				cap = refel.getScope();
				if(cap!=null)
				{
					String methlink = "";
					String intfaclink = writer.codeText(writer.getAgentLink(cap));
					writer.dt();
					writer.boldText("doclet.Referenced_From");
					writer.dd();
					methlink = writer.codeText(writer.getMemberLink(cap, member, false));
					writer.printText("doclet.in_capability", methlink, intfaclink);
					writer.ddEnd();
					writer.dtEnd();
				}
			}
		}
	}

	/**
	 *
	 * @param member
	 */
	protected void printAssignTo(IMElement member)
	{
		IMElementReference elref;
		IMReferenceableElement refel;
		IMCapability cap;

		if(member instanceof IMReferenceableElement)
		{
			refel = (IMReferenceableElement)member;

//            System.out.println(refel.getName());

			for(int i = 0; i<refel.getAssignToElements().length; i++)
			{
				elref = refel.getAssignToElements()[i];
				cap = elref.getScope();

				if(cap!=null)
				{
					String methlink = "";
					String intfaclink = writer.codeText(writer.getAgentLink(cap));
					writer.dt();
					writer.boldText("doclet.Assign_To");
					writer.dd();
					methlink = writer.codeText(writer.getMemberLink(cap, member, false));
					writer.printText("doclet.in_capability", methlink, intfaclink);
					writer.ddEnd();
					writer.dtEnd();
				}
			}
		}
	}



	/**
	 *
	 * @param member
	 * /
	protected void printFacts(IMElement member)
	{
		IMExpression[] expressions = null;
		String text = "";

		if(member instanceof IMTypedElementReference)
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
		}

		if(member instanceof IMTypedElement)
		{
			text = "doclet.Default_Fact";
			if(((IMBelief)member).getDefaultFact()!=null)
			{
				expressions = new IMExpression[]{((IMBelief)member).getDefaultFact()};
			}
		}

		if(member instanceof IMTypedElementSet)
		{
			text = "doclet.Default_Facts";
			expressions = ((IMBeliefSet)member).getDefaultFacts();
		}

		if(expressions!=null && expressions.length>0)
		{
			writer.dt();
			writer.boldText(text);
			for(int i = 0; i<expressions.length; i++)
			{
				writer.dd();
				writer.print(writer.codeText(expressions[i].getExpressionText()));
				if(expressions[i].getDescription()!=null)
				{
					writer.print(" - ");
					writer.print(expressions[i].getDescription());
				}

			}
		}
	}*/

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
			IMParameter[] params;
			IMParameterSet[] paramsets;
			if(paramel instanceof IMMessageEvent)
			{
				params = ((IMMessageEvent)paramel).getDeclaredParameters();
				paramsets = ((IMMessageEvent)paramel).getDeclaredParameterSets();
			}
			else
			{
				params = paramel.getParameters();
			 	paramsets = paramel.getParameterSets();
			}
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

	/**
	 *  Print parameter(set) information about a member.
	 *  @param member The member.
	 */
	protected void printParameterInfo(IMElement member)
	{
		//if(member instanceof IMElementReference)
		//	member = ((IMElementReference)member).getOriginalElement();
		boolean first = true;

		if(member instanceof IMParameterElement)
		{
			IMParameterElement paramel = (IMParameterElement)member;
			IMParameter[] params;
			IMParameterSet[] paramsets;
			if(paramel instanceof IMMessageEvent)
			{
				params = ((IMMessageEvent)paramel).getDeclaredParameters();
				paramsets = ((IMMessageEvent)paramel).getDeclaredParameterSets();
			}
			else
			{
				params = paramel.getParameters();
			 	paramsets = paramel.getParameterSets();
			}
			Arrays.sort(params, new ElementNameComparator());
			Arrays.sort(paramsets, new ElementNameComparator());

			for(int i=0; i<params.length; i++)
			{
				if(first)
				{
					writer.dt();
					writer.boldText("doclet.Parameters");
					first = false;
				}
				writer.dd();
				if(params[i].isOptional())
				{
					writer.print("optional");
					writer.space();
				}
				writer.print(params[i].getDirection());
				writer.space();
				writer.print("parameter");
				writer.space();
				writer.print(writer.codeText(Standard.getMemberName(params[i])));
				if(params[i].getDefaultValue()!=null)
				{
					writer.space();
					writer.italics(writer.getText("doclet.Default_Value"));
					writer.space();
					printExpression(params[i].getDefaultValue());
				}
				else if(params[i].getBindingOptions()!=null)
				{
					writer.space();
					writer.italics(writer.getText("doclet.Binding_Options"));
					writer.space();
					printExpression(params[i].getBindingOptions());
				}
				// additional mapping info for plan parameters
				if(params[i] instanceof IMPlanParameter)
				{
					IMPlanParameter pp = (IMPlanParameter)params[i];
					printMappings(pp.getGoalMappings(), pp.getInternalEventMappings(),
						pp.getMessageEventMappings());
				}
				if(params[i].getDescription()!=null)
				{
					writer.print(" - ");
					writer.print(params[i].getDescription());
				}
				writer.ddEnd();
			}

			for(int i=0; i<paramsets.length; i++)
			{
				if(first)
				{
					writer.dt();
					writer.boldText("doclet.Parameters");
					first = false;
				}
				writer.dd();
				if(paramsets[i].isOptional())
				{
					writer.print("optional");
					writer.space();
				}
				writer.print(paramsets[i].getDirection());
				writer.space();
				writer.print("parameterset");
				writer.space();
				writer.print(writer.codeText(Standard.getMemberName(paramsets[i])));
				if(paramsets[i].getDefaultValues().length>0)
				{
					writer.space();
					writer.italics(writer.getText("doclet.Default_Values"));
					writer.space();
					IMExpression[] defs = paramsets[i].getDefaultValues();
					for(int j=0; j<defs.length; j++)
					{
						writer.space();
						printExpression(defs[j]);
					}
				}
				else if(paramsets[i].getDefaultValuesExpression()!=null)
				{
					writer.space();
					writer.italics(writer.getText("doclet.Default_Values_Expression"));
					writer.space();
					printExpression(paramsets[i].getDefaultValuesExpression());
				}
				// additional mapping info for plan parameters
				if(paramsets[i] instanceof IMPlanParameterSet)
				{
					IMPlanParameterSet ppset = (IMPlanParameterSet)paramsets[i];
					printMappings(ppset.getGoalMappings(), ppset.getInternalEventMappings(),
						ppset.getMessageEventMappings());
				}
				if(paramsets[i].getDescription()!=null)
				{
					writer.print(" - ");
					writer.print(paramsets[i].getDescription());
				}
				writer.ddEnd();
			}
		}

		else if(member instanceof IMParameterElementReference)
		{
			IMParameterElementReference paramelref = (IMParameterElementReference)member;
			IMParameterReference[] paramrefs = paramelref.getParameterReferences();
			IMParameterSetReference[] paramsetrefs = paramelref.getParameterSetReferences();
			Arrays.sort(paramrefs, new ElementNameComparator());
			Arrays.sort(paramsetrefs, new ElementNameComparator());

			for(int i=0; i<paramrefs.length; i++)
			{
				if(first)
				{
					writer.dt();
					writer.boldText("doclet.Parameters");
					first = false;
				}
				writer.dd();
				writer.print("parameterref");
				writer.space();
				writer.print(writer.codeText(Standard.getMemberName(paramrefs[i])));
				if(paramrefs[i].getDescription()!=null)
				{
					writer.print(" - ");
					writer.print(paramrefs[i].getDescription());
				}
				writer.ddEnd();
			}
			if(paramrefs.length>0)
				writer.dtEnd();

			for(int i=0; i<paramsetrefs.length; i++)
			{
				if(first)
				{
					writer.dt();
					writer.boldText("doclet.Parameters");
					first = false;
				}
				writer.dd();
				writer.print("parameterset");
				writer.space();
				writer.print(writer.codeText(Standard.getMemberName(paramsetrefs[i])));
				if(paramsetrefs[i].getDescription()!=null)
				{
					writer.print(" - ");
					writer.print(paramsetrefs[i].getDescription());
				}
				writer.ddEnd();
			}
			if(paramsetrefs.length>0)
				writer.dtEnd();
		}
	}

	/**
	 *  Print all plan parameter(set) mappings.
	 */
	protected void printMappings(String[] goals, String[] ievents, String[] mevents)
	{
		if(goals.length>0 || ievents.length>0 || mevents.length>0)
		{
			writer.space();
			writer.print("(");
		}
		for(int i=0; i<goals.length; i++)
		{
			if(i==0)
			{
				writer.print("goal mappings:");
			}
			writer.space();
			writer.print(goals[i]);
		}
		for(int i=0; i<ievents.length; i++)
		{
			if(i==0)
			{
				writer.space();
				writer.print("internal event mappings:");
			}
			writer.space();
			writer.print(ievents[i]);
		}
		for(int i=0; i<mevents.length; i++)
		{
			if(i==0)
			{
				writer.space();
				writer.print("message event mappings:");
			}
			writer.space();
			writer.print(mevents[i]);
		}
		if(goals.length>0 || ievents.length>0 || mevents.length>0)
		{
			writer.print(")");
		}
	}

//    protected void printBindings(IMElement member) {
//
//        IMBindings bindings = null;
//
//        while (member instanceof IMElementReference && !((IMElementReference) member).hasAssignFrom()) {
//                member = ((IMElementReference) member).getAssignFromElement();
//        }
//
//        if (member instanceof IMGoal) {
//            bindings=((IMGoal)member).getBindings();
//        }
//        if (member instanceof IMPlan) {
//            bindings=((IMPlan)member).getBindings();
//        }
//
//        if (bindings != null) {
//            String[] bindingNames = bindings.getBindingNames();
//
//            if (bindingNames!=null && bindingNames.length>0 ) {
//                writer.dt();
//                writer.boldText("doclet.Bindings");
//
//                for (int i = 0; i < bindingNames.length; i++) {
//                    IMExpression expression = bindings.getBinding(bindingNames[i]);
//                    writer.dd();
//                    printExported(expression);
//                    writer.print("binding");
//                    writer.space();
//                    writer.print(writer.codeText(expression.getName()));
//                    if (expression.hasDescription()) {
//                        writer.print(" - ");
//                        writer.print(expression.getDescription());
//                    }
//                    writer.dl();
//                    writer.dd();
//                    writer.print(writer.codeText(expression.getExpressionText()));
//                    writer.dlEnd();
//                }
//
//            }
//        }
//    }

	/**
	 *  This is the important method for all concrete
	 *  sub writers that need to print out custom info.
	 *  @param member The member.
	 */
	protected abstract void printMemberSpecificInfo(IMElement member);

	/**
	 *
	 */
	protected void printInfoHeader()
	{
		writer.dd();
		writer.dl();
	}

	/**
	 *
	 */
	protected void printInfoFooter()
	{
		writer.dlEnd();
		writer.ddEnd();
	}

	/**
	 *
	 */
	public void printMembersSummary()
	{
		List members = getMembers(element);
		if(members.size()>0)
		{
			Collections.sort(members, new ElementNameComparator());
			printSummaryHeader();
			for(int i=0; i<members.size(); i++)
			{
				IMElement member = (IMElement)members.get(i);
				printSummaryMember(element, member);
			}
			printSummaryFooter();
		}
	}

	/**
	 *
	 */
	public void buildVisibleMemberMap()
	{
		List members = new ArrayList(getMembers(element));
		Collections.sort(members, new ElementNameComparator());
		for(int i=0; i<members.size(); i++)
		{
			if(members instanceof IMReferenceableElement)
			{
				IMReferenceableElement member = (IMReferenceableElement)members.get(i);
				if(member.getAssignToElements().length>0)
				{
					IMElementReference[] references = member.getAssignToElements();
					for(int j=0; j<references.length; j++)
					{
						IMCapability refcap = references[j].getScope();
						if(refcap!=element)
						{
							if(!visibleClasses.contains(refcap))
							{
								visibleClasses.add(refcap);
							}
							add(visibleMembers, refcap, member);
						}
					}
				}
				if(member instanceof IMElementReference)
				{
					IMReferenceableElement refMember = ((IMElementReference)member).getReferencedElement();
					if(refMember!=null)
					{
						IMCapability refcap = refMember.getScope();
						if(refcap!=element)
						{
							if(!visibleClasses.contains(refcap))
							{
								visibleClasses.add(refcap);
							}
							add(visibleMembers, refcap, member);
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * @param member
	 * @return
	 */
	protected boolean isExportedMember(IMReferenceableElement member)
	{
		return IMReferenceableElement.EXPORTED_TRUE.equals(member.getExported())
			|| IMReferenceableElement.EXPORTED_SHIELDED.equals(member.getExported());
	}

	/**
	 *
	 * @return
	 */
	protected abstract List getStandardMembers();

	/**
	 *
	 * @param member
	 * @return
	 */
	protected boolean isStandardMember(IMElement member)
	{
		return getStandardMembers().contains(Standard.getMemberName(member));
	}

	/**
	 *
	 * @param members
	 * @return
	 */
	protected List getExportedMembers(List members)
	{
		List exportedMembers = new ArrayList();
		for(int i = 0; i<members.size(); i++)
		{
			if(members.get(i) instanceof IMReferenceableElement)
			{
				if(isExportedMember((IMReferenceableElement)members.get(i)))
				{
					exportedMembers.add(members.get(i));
				}
			}
		}
		return exportedMembers;
	}

	/**
	 *
	 * @param map
	 * @param capability
	 * @param element
	 * @return
	 */
	protected boolean add(Map map, IMCapability capability, IMElement element)
	{
		List list = (List)map.get(capability);

		if(list==null)
		{
			list = new ArrayList();
			map.put(capability, list);
		}
		if(list.contains(element))
		{
			return false;
		}
		else
		{
			list.add(element);
		}
		return true;
	}

	/**
	 *
	 * @param map
	 * @param capability
	 * @return
	 */
	protected List get(Map map, IMCapability capability)
	{
		List list = (List)map.get(capability);

		if(list==null)
		{
			return new ArrayList();
		}
		return list;
	}

	/**
	 *
	 */
	public void printInheritedMembersSummary()
	{
		for(int i = 0; i<visibleClasses.size(); i++)
		{
			IMCapability inhclass = (IMCapability)(visibleClasses.get(i));
			if(inhclass==element)
			{
				continue;
			}

			List inhmembers = new ArrayList(getExportedMembers(getMembers(inhclass)));
			;

			if(inhmembers.size()>0)
			{
				if(!(printedSummaryHeader))
				{
					printSummaryHeader();
					printSummaryFooter();
					printedSummaryHeader = true;
				}
				Collections.sort(inhmembers, new ElementNameComparator());
				printInheritedSummaryHeader(inhclass);

				boolean printedPrev = false;

				for(int j = 0; j<inhmembers.size(); ++j)
				{
					IMReferenceableElement inhmember = (IMReferenceableElement)(inhmembers.get(j));

					if(printedPrev)
					{
						print(", ");
					}
					printInheritedSummaryMember(inhclass, inhmember);
					printedPrev = true;
				}
				printInheritedSummaryFooter(inhclass);
			}
		}
	}

	/**
	 *
	 */
	public void printInheritedMembersSummaryOnlyUsed()
	{
		for(int i = 0; i<visibleClasses.size(); i++)
		{
			IMCapability inhclass = (IMCapability)(visibleClasses.get(i));
			if(inhclass==element)
			{
				continue;
			}

			List inhmembers = get(visibleMembers, inhclass);

			if(inhmembers.size()>0)
			{
				if(!(printedSummaryHeader))
				{
					printSummaryHeader();
					printSummaryFooter();
					printedSummaryHeader = true;
				}
				Collections.sort(inhmembers, new ElementNameComparator());
				printInheritedSummaryHeader(inhclass);

				boolean printedPrev = false;

				for(int j = 0; j<inhmembers.size(); ++j)
				{
					IMReferenceableElement inhmember = (IMReferenceableElement)(inhmembers.get(j));
					if(printedPrev)
					{
						print(", ");
					}
					printInheritedSummaryMember(inhclass, inhmember);
					printedPrev = true;
				}
				printInheritedSummaryFooter(inhclass);
			}
		}
	}

	/**
	 *
	 * @param expression
	 */
	protected void printExpression(IMExpression expression)
	{
		printExpression(null, expression);
	}

	/**
	 *
	 * @param text
	 * @param expression
	 */
	protected void printExpression(String text, IMExpression expression)
	{
		printExpression(text, expression, false);
	}

	/**
	 *
	 * @param text
	 * @param expression
	 */
	protected void printExpression(String text, IMElement expression, boolean exparams)
	{
		if(expression!=null)
		{
			if(text!=null && text.length()>0)
			{
				writer.dt();
				writer.boldText(text);
				writer.dd();
			}
			if(expression instanceof IMExpression)
			{
				IMExpression exp = (IMExpression)expression;
				writer.print(writer.codeText(exp.getExpressionText()));
				if(exparams)
				{
					IMExpressionParameter[] exps = exp.getExpressionParameters();
					for(int i=0; i<exps.length; i++)
					{
						writer.dd();
						writer.italics(writer.getText("doclet.Expression_Parameter"));
						space();
						writer.print(exps[i].getClassname());
						space();
						writer.print(Standard.getMemberName(exps[i]));
					}
				}
				writer.ddEnd();
			}
			if(text!=null && text.length()>0)
				writer.dtEnd();
		}
	}

	/**
	 *  Print some dt text with a dd value.
	 * @param text
	 * @param value
	 */
	protected void printText(String text, String value)
	{
		writer.dt();
		writer.boldText(text);
		writer.dd();
		writer.print(writer.codeText(value));
		writer.ddEnd();
		writer.dtEnd();
	}

	/**
	 *
	 * @param text
	 * @param member
	 */
	protected void printMemberReference(String text, IMReferenceableElement member)
	{
		if(member!=null)
		{
			IMCapability scope = member.getScope();
			String memberlink = writer.codeText(writer.getMemberLink(scope, member, false));
			String caplink = writer.codeText(writer.getAgentLink(scope));
			writer.dt();
			writer.boldText(text);
			writer.dd();
			if(element==scope)
			{
				writer.print(memberlink);
			}
			else
			{
				writer.printText("doclet.in_capability", memberlink, caplink);
			}
			writer.ddEnd();
			writer.dtEnd();
		}
	}
	
	/**
	 *
	 * @param text
	 * @param member
	 */
	protected void printMemberReferences(String text, IMReferenceableElement[] members)
	{
		if(members!=null && members.length>0)
		{
			writer.dl();
			writer.dt();
			writer.boldText(text);
			writer.dtEnd();
			for(int i=0; i<members.length; i++)
			{
				writer.dd();
				IMCapability scope = members[i].getScope();
				String memberlink = writer.codeText(writer.getMemberLink(scope, members[i], false));
				String caplink = writer.codeText(writer.getAgentLink(scope));
				if(element==scope)
				{
					writer.print(memberlink);
				}
				else
				{
					writer.printText("doclet.in_capability", memberlink, caplink);
				}
				writer.ddEnd();
			}
			writer.dlEnd();
		}
	}

}


