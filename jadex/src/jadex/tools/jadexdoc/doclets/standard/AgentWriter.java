package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import jadex.model.*;
import jadex.tools.jadexdoc.doclets.*;

/**
 *  The main file writer for agents.
 */
public class AgentWriter extends CapabilityWriter
{
	//-------- attributes --------

	/** The agent. */
	protected IMBDIAgent agent;

	//-------- constructors --------

	/**
	 *
	 * @param configuration
	 * @param path
	 * @param agent
	 * @param prev
	 * @param next
	 * @param agenttree
	 * @throws IOException
	 */
	public AgentWriter(StandardConfiguration configuration, String path, IMBDIAgent agent,
			IMCapability prev, IMCapability next, AgentTree agenttree) throws IOException
	{
		super(configuration, path, agent, prev, next, agenttree);
		this.agent = agent;
	}

	//-------- methods --------

	/**
	 * Generate the agent file contents.
	 */
	public void generateDocumentationFile()
	{
		String cltype = getText("doclet.Agent");
		String pkgname = getCapability().getPackage();
		String clname = Standard.getMemberName(getCapability());
		String label = cltype+" "+clname;

		printHtmlHeader(clname);
		navLinks(true);
		hr();
		println("<!-- ======== START OF AGENT DATA ======== -->");
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

		println("<!-- ========= END OF AGENT DATA ========= -->");
		hr();
		navLinks(false);
		printBottom();
		printBodyHtmlEnd();
	}

	/**
	 * Print the agent description regarding capabilities contained
	 * and the agentclass
	 */
	protected void printAgentDescription()
	{
		dl();
		dt();
		print("agent");
		print(" ");

		bold(Standard.getMemberName(agent));

		IMCapabilityReference[] implIntfacs = agent.getCapabilityReferences();
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

		// Print the agent arguments
		IMBelief[] mbels = agent.getBeliefbase().getBeliefs();
		boolean found = false;
		
		for(int i=0; i<mbels.length; i++)
		{
			if(mbels[i].getExported().equals(IMReferenceableElement.EXPORTED_TRUE))
			{
				if(!found)
				{
					br();br();
					bold("Agent arguments: ");
					found = true;
				}
				else
				{
					br();
				}
				dd();
				printMemberLink(agent, mbels[i], true);
				print(" ");
				beliefsubwriter.printTypeLink(mbels[i].getClazz());
				ddEnd();
			}
		}
		
		IMBeliefReference[] mbelrefs = agent.getBeliefbase().getBeliefReferences();
		for(int i=1; i<mbelrefs.length; i++)
		{
			if(mbelrefs[i].getExported().equals(IMReferenceableElement.EXPORTED_TRUE))
			{
				if(!found)
				{
					br();br();
					bold("Agent arguments: ");
					found = true;
				}
				else
				{
					print(", ");
				}
				print(mbelrefs[i].getName());
				IMElement ref = mbelrefs[i];
				Class clazz = null;
				while(clazz==null && ref!=null)
				{
					if(ref instanceof IMBeliefReference)
					{
						clazz = ((IMBeliefReference)ref).getClazz();
						ref = ((IMBeliefReference)ref).getReferencedElement();
					}
					else
					{
						clazz = ((IMBelief)ref).getClazz();
						ref = null;
					}
					
				}
				dd();
				printMemberLink(agent, mbelrefs[i], true);
				if(clazz!=null)
				{
					print(" ");
					beliefsubwriter.printTypeLink(clazz);
				}
				ddEnd();
			}
		}
		
//        if (isAgent) {
//            Class agentclass = ((IMBDIAgent)agent).getClazz();
//            if (agentclass != null) {
//                dt();
//                print("agentclass ");
//                printClassLink(agentclass);
//            }
//        }

		dtEnd();
		dlEnd();
	}

	/**
	 * Print agent page indicator
	 */
	protected void navLinkAgent()
	{
		navCellRevStart();
		fontStyle("NavBarFont1Rev");
		boldText("doclet.Agent");
		fontEnd();
		navCellEnd();
	}

	/**
	 * Print agent use link
	 */
	protected void navLinkAgentUse()
	{
		navCellStart();
		printHyperLink("agent-use/"+filename, "",
			getText("doclet.navAgentUse"), true, "NavBarFont1");
		navCellEnd();
	}

	/**
	 * Print previous package link
	 */
	protected void navLinkPrevious()
	{
		if(prev==null)
		{
			printText("doclet.Prev_Agent");
		}
		else
		{
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
			printText("doclet.Next_Agent");
		}
		else
		{
			printAgentLink(next, getText("doclet.Next_Agent"), true);
		}
	}
}





