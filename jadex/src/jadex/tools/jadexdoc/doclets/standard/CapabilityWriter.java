package jadex.tools.jadexdoc.doclets.standard;

import java.util.*;
import java.io.IOException;
import jadex.tools.jadexdoc.doclets.*;
import jadex.tools.jadexdoc.PathManager;
import jadex.model.*;

/**
 *  The main file writer for capabilities.
 */
public class CapabilityWriter extends SubWriterHolderWriter
{
	//-------- attributes --------

	/** The agent tree. */
	protected AgentTree agenttree;

	/** The capability. */
	protected IMCapability capa;

	/** The previous agent. */
	protected IMCapability prev;

	/** The next agent. */
	protected IMCapability next;

	/** The subwriter for plans. */
	protected PlanSubWriter plansubwriter;

	/** The subwriter for goals. */
	protected GoalSubWriter goalsubwriter;

	/** The subwriter for beliefs. */
	protected BeliefSubWriter beliefsubwriter;

	/** The subwriter for expressions. */
	protected ExpressionSubWriter expsubwriter;

	/** The subwriter for events. */
	protected EventSubWriter eventsubwriter;

	/** The subwriter for events. */
	protected ConfigurationSubWriter inistatesubwriter;

	//    protected AgentSubWriter nestedSubWriter;  //

	//-------- constructors --------

	/**
	 *
	 * @param configuration
	 * @param path
	 * @param capa
	 * @param prev
	 * @param next
	 * @param agenttree
	 * @throws java.io.IOException
	 */
	public CapabilityWriter(StandardConfiguration configuration, String path, IMCapability capa,
			IMCapability prev, IMCapability next, AgentTree agenttree) throws IOException
	{
		super(configuration, path, getLocalFilename(capa), PathManager.getRelativePath(capa.getPackage()));
		this.capa = capa;
		this.agenttree = agenttree;
		configuration.currentelement = capa;
		this.prev = prev;
		this.next = next;
		beliefsubwriter = new BeliefSubWriter(this, capa, configuration);
		goalsubwriter = new GoalSubWriter(this, capa, configuration);
		plansubwriter = new PlanSubWriter(this, capa, configuration);
		eventsubwriter = new EventSubWriter(this, capa, configuration);
		expsubwriter = new ExpressionSubWriter(this, capa, configuration);
		inistatesubwriter = new ConfigurationSubWriter(this, capa, configuration);
		//        nestedSubWriter = new AgentSubWriter(this, agent);
	}

	//-------- methods --------

	/**
	 *  Get the capability.
	 *  @return The capability.
	 */
	protected IMCapability getCapability()
	{
		return capa;
	}

	/**
	 * Print this package link
	 */
	protected void navLinkPackage()
	{
		navCellStart();
		String pd = getCapability().getPackage();
		if(pd!=null && pd.length()>0)
		{
			printHyperLink("package-summary.html", "", getText("doclet.Package"), true, "NavBarFont1");
		}
		else
		{
			fontStyle("NavBarFont1");
			printText("doclet.Package");
			fontEnd();
//            super.navLinkPackage();
		}
		navCellEnd();
	}

	/**
	 * Print agent page indicator
	 */
	protected void navLinkAgent()
	{
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Capability");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Print agent use link
	 */
	protected void navLinkAgentUse()
	{
		navCellStart();
		printHyperLink("capability-use/"+filename, "",
				getText("doclet.navCapabilityUse"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print previous package link
	 */
	protected void navLinkPrevious()
	{
		if(prev==null)
		{
			//printText("doclet.Prev_Capability");
			printText("doclet.Prev_Agent");
		}
		else
		{
			//printAgentLink(prev, getText("doclet.Prev_Capability"), true);
			printAgentLink(prev, getText("doclet.Prev_Agent"), true);
		}
	}

	/**
	 * Print next package link
	 */
	protected void navLinkNext()
	{
		if(next==null)
		{
			//printText("doclet.Next_Capability");
			printText("doclet.Next_Agent");
		}
		else
		{
			//printAgentLink(next, getText("doclet.Next_Capability"), true);
			printAgentLink(next, getText("doclet.Next_Agent"), true);
		}
	}

	/**
	 * Generate the documenttation file contents.
	 */
	public void generateDocumentationFile()
	{
		String cltype = getText("doclet.Capability");
		String pkgname = getCapability().getPackage();
		String clname = Standard.getMemberName(getCapability());
		String label = cltype+" "+clname;

		printHtmlHeader(clname);
		navLinks(true);
		hr();
		println("<!-- ======== START OF CAPABILITY DATA ======== -->");
		h2();
		if(pkgname!=null && pkgname.length()>0)
		{
			font("-1");
			print(pkgname);
			fontEnd();
			br();
		}
		print(label);
		h2End();

		// if this is a agent (not an capability) then generate
		// the agent class tree.
		/*
		if (agent instanceof IMBDIAgent) {
			pre();
			printTreeForClass(((IMBDIAgent) agent).getClazz());
			preEnd();
		}
		*/

		// if ther are contained capabilities then generate
		// the contained capability tree
		if(getCapability().getCapabilityReferences().length>0)
		{
			code();
			printTreeForAgent(getCapability());
			codeEnd();
		}

//        printSuperImplementedCapabilitiesInfo();

//                printSubClassInterfaceInfo();

		// list of agents that implements the capability
		printImplementingAgents();
		// list of capabilities that implements the capability
		printImplementingCapabilities();

		//        printEnclosingInfo();

		hr();

		printAgentDescription();

		p();

		if(!configuration.nocomment)
		{
			// generate documentation for the agent.
			printInlineComment(getCapability());
			hr();
			p();
		}
		else
		{
			hr();
		}

		printAllMembers();

		println("<!-- ========= END OF CAPABILITY DATA ========= -->");
		hr();
		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}


	/**
	 * Print summary and detail information for the specified members in the
	 * class.
	 */
	protected void printAllMembers()
	{
		if(!configuration.nocomment)
		{
			println("<!-- ======== NESTED CAPABILITY SUMMARY ======== -->");
			println();
			//            nestedSubWriter.printMembersSummary();
			//            nestedSubWriter.printInheritedMembersSummary();
			println();
			println("<!-- =========== BELIEF SUMMARY =========== -->");
			println();
			beliefsubwriter.printMembersSummary();
//            beliefSubWriter.printInheritedMembersSummary();
			println();
			println("<!-- ======== GOAL SUMMARY ======== -->");
			println();
			goalsubwriter.printMembersSummary();
//            goalSubWriter.printInheritedMembersSummary();
			println();
			println("<!-- ========== PLAN SUMMARY =========== -->");
			println();
			plansubwriter.printMembersSummary();
//            planSubWriter.printInheritedMembersSummary();
			println("<!-- ========== EVENT SUMMARY =========== -->");
			println();
			eventsubwriter.printMembersSummary();
//            eventSubWriter.printInheritedMembersSummary();
			println("<!-- ========== EXPRESSION SUMMARY =========== -->");
			println();
			expsubwriter.printMembersSummary();
//            expressionSubWriter.printInheritedMembersSummary();
			println("<!-- ========== IINITIAL STATES SUMMARY =========== -->");
			println();
			inistatesubwriter.printMembersSummary();
//            expressionSubWriter.printInheritedMembersSummary();
			p();
		}
		println();
		println("<!-- ============ BELIEF DETAIL =========== -->");
		println();
		beliefsubwriter.printMembers();
		println();
		println("<!-- ========= GOAL DETAIL ======== -->");
		println();
		goalsubwriter.printMembers();
		println();
		println("<!-- ============ PLAN DETAIL ========== -->");
		println();
		plansubwriter.printMembers();
		println("<!-- ============ EVENT DETAIL ========== -->");
		println();
		eventsubwriter.printMembers();
		println("<!-- ============ EXPRESSION DETAIL ========== -->");
		println();
		expsubwriter.printMembers();
		println("<!-- ========== IINITIAL STATES DETAIL =========== -->");
		println();
		inistatesubwriter.printMembers();
	}

	/**
	 * Print the agent description regarding capabilities contained
	 * and the agentclass
	 */
	protected void printAgentDescription()
	{
		dl();
		dt();
		print("capability");
		print(" ");

		bold(Standard.getMemberName(getCapability()));

		IMCapabilityReference[] implIntfacs = getCapability().getCapabilityReferences();
		if(implIntfacs!=null && implIntfacs.length>0)
		{
			dd();
			print("contains ");
			printAgentLink(implIntfacs[0].getCapability());
			for(int i = 1; i<implIntfacs.length; i++)
			{
				print(", ");
				printAgentLink(implIntfacs[i].getCapability());
			}
			ddEnd();
		}
		dtEnd();

//        if (isAgent) {
//            Class agentclass = ((IMBDIAgent)agent).getClazz();
//            if (agentclass != null) {
//                dt();
//                print("agentclass ");
//                printClassLink(agentclass);
//            }
//        }

		dlEnd();
	}

	/**
	 * Generate the indent and get the line image for the class tree.
	 * For user accessibility, the image includes the alt attribute
	 * "extended by".  (This method is not intended for a class
	 * implementing an interface, where "implemented by" would be required.)
	 * <p/>
	 * indent  integer indicating the number of spaces to indent
	 */
	protected void printStep(int indent)
	{
		print(spaces(4*indent-2));
		String alttext = getText("doclet.extended_by");
		print("<IMG SRC=\""+relativepathNoSlash+"/resources/inherit.png\" "+
				"ALT=\""+alttext+"\">");
	}


	/**
	 * Print the agent hierarchy tree for this agent/capability only.
	 */
	protected void printTreeForAgent(IMCapability cd)
	{
		dl();
		if(cd.equals(getCapability()))
		{
			String pck = cd.getPackage()==null? "": cd.getPackage();
			if(configuration.shouldExcludeQualifier(pck))
			{
				bold(Standard.getMemberName(cd));
			}
			else
			{
				bold(pck.length()>0? pck+"."+Standard.getMemberName(cd): Standard.getMemberName(cd));
			}
		}
		else
		{
			li("circle");
			printQualifiedAgentLink(cd);
		}
		List list = agenttree.subcapabilities(cd);
		if(list.size()>0)
		{
			for(int i = 0; i<list.size(); i++)
			{
				IMCapability local = (IMCapability)list.get(i);
				printTreeForAgent(local);   // Recurse
			}
		}
		dlEnd();
	}


	/**
	 * Print the class hierarchy tree for this class only.
	 */
	protected int printTreeForClass(Class cd)
	{
		//String pck = getCapability().getPackage()==null? "": getCapability().getPackage();
		Class sup = cd.getSuperclass();
		//        ClassDoc sup = cd.superclass();
		int indent = 0;
		if(sup!=null)
		{
			indent = printTreeForClass(sup);
			printStep(indent);
		}
//        if (cd.equals(bdiagent.getClazz())) {
//            if (configuration.shouldExcludeQualifier(pck)) {
//                bold(bdiagent.getClazz().getName());
//            } else {
//                bold(cd.getName());
//            }
//        } else {
		printQualifiedClassLink(cd);
//        }
		println();
		return indent+1;
	}

	/**
	 * If this is a capability which are the agents, that implement this?
	 */
	protected void printImplementingAgents()
	{
		List all = agenttree.supercapabilities(getCapability());
		List implcl = new ArrayList();
		for(int i = 0; i<all.size(); i++)
		{
			if(all.get(i) instanceof IMBDIAgent)  // todo: Hack
			{
				implcl.add(all.get(i));
			}
		}
		if(implcl.size()>0)
		{
			printInfoHeader();
			boldText("doclet.Implementing_Agents");
			printSubAgentLinkInfo(implcl);
		}
	}

	/**
	 * If this is a capability which are the capability, that implement this?
	 */
	protected void printImplementingCapabilities()
	{
		List all = agenttree.supercapabilities(getCapability());
		List implcl = new ArrayList();
		for(int i = 0; i<all.size(); i++)
		{
			if(!(all.get(i) instanceof IMBDIAgent)) // todo: Hack
			{
				implcl.add(all.get(i));
			}
		}
		if(implcl.size()>0)
		{
			printInfoHeader();
			boldText("doclet.Implementing_Capabilities");
			printSubAgentLinkInfo(implcl);
		}
	}

	/**
	 *
	 */
	protected void printSuperImplementedCapabilitiesInfo()
	{
		List intarr = agenttree.subcapabilities(getCapability());
		if(intarr.size()>0)
		{
			printInfoHeader();
			boldText("doclet.All_Implemented_Capabilities");
			printSubAgentLinkInfo(intarr);
		}
	}

	/**
	 * Generate a link for the sub-capabilities.
	 */
	protected void printSubAgentLinkInfo(List list)
	{
		int i = 0;
		Object[] capRefList = list.toArray();
		//Sort the list to be printed.
		Arrays.sort(capRefList, new ElementNameComparator());
		print(' ');
		dd();
		for(; i<list.size()-1; i++)
		{
//            printAgentLink(((IMCapabilityReference) capRefList[i]).getCapability());
			printAgentLink((IMCapability)capRefList[i]);
			print(", ");
		}
//        printAgentLink(((IMCapabilityReference) capRefList[i]).getCapability());
		printAgentLink((IMCapability)capRefList[i]);
		ddEnd();
		
		dtEnd();
		dlEnd();
	}

	/**
	 *
	 */
	protected void printInfoHeader()
	{
		dl();
		dt();
	}

	/**
	 *
	 */
	protected void navLinkTree()
	{
		navCellStart();
		String pd = getCapability().getPackage();
		if(pd!=null && pd.length()>0)
		{
			printHyperLink("package-tree.html", "", getText("doclet.Tree"),
					true, "NavBarFont1");
		}
		else
		{
			printHyperLink(relativepath+"overview-tree.html", "",
					getText("doclet.Tree"), true, "NavBarFont1");
		}
		navCellEnd();
	}

	/**
	 *
	 */
	protected void printSummaryDetailLinks()
	{
		tr();
		tdVAlignClass("top", "NavBarCell3");
		font("-2");
		print("  ");
		navSummaryLinks();
		fontEnd();
		tdEnd();

		tdVAlignClass("top", "NavBarCell3");
		font("-2");
		navDetailLinks();
		fontEnd();
		tdEnd();
		trEnd();
	}

	/**
	 *
	 */
	protected void navSummaryLinks()
	{
		printText("doclet.Summary");
		print("&nbsp;");
		//        nestedSubWriter.navSummaryLink();
		navGap();
		beliefsubwriter.navSummaryLink();
		navGap();
		goalsubwriter.navSummaryLink();
		navGap();
		plansubwriter.navSummaryLink();
		if(configuration.events)
		{
			navGap();
			eventsubwriter.navSummaryLink();
		}
		if(configuration.expressions)
		{
			navGap();
			expsubwriter.navSummaryLink();
		}
		navGap();
		inistatesubwriter.navSummaryLink();
	}

	/**
	 *
	 */
	protected void navDetailLinks()
	{
		printText("doclet.Detail");
		print("&nbsp;");
		beliefsubwriter.navDetailLink();
		navGap();
		goalsubwriter.navDetailLink();
		navGap();
		plansubwriter.navDetailLink();
		if(configuration.events)
		{
			navGap();
			eventsubwriter.navDetailLink();
		}
		if(configuration.expressions)
		{
			navGap();
			expsubwriter.navDetailLink();
		}
		navGap();
		inistatesubwriter.navDetailLink();
	}

	/**
	 *
	 */
	protected void navGap()
	{
		space();
		print('|');
		space();
	}

	/**
	 *  Get the local filename for the documentation page.
	 *  @param capa The capability or agent.
	 */
	public static String getLocalFilename(IMCapability capa)
	{
		if(capa instanceof IMBDIAgent)
			return Standard.getMemberName(capa)+".agent.html";
		else
			return Standard.getMemberName(capa)+".capability.html";
	}

	/**
	 * Generate a agent page.
	 * @param prev the previous agent generated, or null if no previous.
	 * @param capa the capa to generate.
	 * @param next the next capa to be generated, or null if no next.
	 * /
	public static void generate(StandardConfiguration configuration, IMCapability capa, IMCapability prev,
			IMCapability next, AgentTree agenttree)
	{
		//        String pkgpath =  PathManager.getDirectoryPath(agent.containingPackage());
		String pkgpath = PathManager.getDirectoryPath(capa.getPackage());
		try
		{
			CapabilityWriter capgen;
			if(capa instanceof IMBDIAgent)
			{
				capgen = new AgentWriter(configuration, pkgpath, (IMBDIAgent)capa, prev, next, agenttree);
			}
			else
			{
				capgen = new CapabilityWriter(configuration, pkgpath, capa, prev, next, agenttree);
			}
			capgen.generateDocumentationFile();
			String cp = capa.getPackage();

			if((configuration.cmdLinePackages==null || !configuration.cmdLinePackages.contains(cp)) &&
				!containingPackagesSeen.contains(cp))
			{
				//Only copy doc files dir if the containing package is not documented
				//AND if we have not documented a class from the same package already.
				//Otherwise, we are making duplicate copies.
				copyDocFiles(configuration, getSourcePath(configuration, capa.getPackage()),
					pkgpath+fileseparator+DOC_FILES_DIR_NAME, true);
				containingPackagesSeen.add(cp);
			}
			capgen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), getLocalFilename(capa));
			throw new DocletAbortException();
		}
	}*/

}





