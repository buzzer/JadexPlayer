package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import java.util.Arrays;
import jadex.model.IMCapability;
import jadex.tools.jadexdoc.doclets.*;

/**
 * Generate the documentation in the Html "frame" format in the browser.
 */
public class IndexWriter extends HtmlStandardWriter
{

	/** Number of packages specified on the command line. */
	int noOfPackages;

	/** Number of agents specified on the command line. */
	int noOfAgents;

	/** First file to appear in the right-hand frame in the generated documentation. */
	public String topFile = "";

	/**
	 * Constructor to construct IndexWriter object.
	 * @param filename File to be generated.
	 */
	public IndexWriter(StandardConfiguration configuration, String filename) throws IOException
	{
		super(configuration, filename);
		noOfPackages = configuration.cmdLinePackages.size();
		noOfAgents = configuration.cmdLineAgents.size();
		topFile = getTopFile();
	}

	/**
	 * Construct FrameOutputWriter object and then use it to generate the Html
	 * file which will have the description of all the frames in the
	 * documentation. The name of the generated file is "index.html" which is
	 * the default first file for Html documents.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration)
	{
		IndexWriter framegen;
		String filename = "";
		try
		{
			filename = "index.html";
			framegen = new IndexWriter(configuration, filename);
			framegen.generateFrameFile();
			framegen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.error("doclet.exception_encountered",
					exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Generate the contants in the "index.html" file. Print the frame details
	 * as well as warning if browser is not supporting the Html frames.
	 */
	protected void generateFrameFile()
	{
		if(configuration.windowtitle.length()>0)
		{
			printFramesetHeader(configuration.windowtitle, configuration.notimestamp);
		}
		else
		{
			printFramesetHeader(getText("doclet.Generated_Docs_Untitled"), configuration.notimestamp);
		}
		printFrameDetails();
		printFrameFooter();
	}

	/**
	 * Generate the code for issueing the warning for a non-frame capable web
	 * client. Also provide links to the non-frame version documentation.
	 */
	protected void printFrameWarning()
	{
		noFrames();
		h2();
		printText("doclet.Frame_Alert");
		h2End();
		p();
		printText("doclet.Frame_Warning_Message");
		br();
		printText("doclet.Link_To");
		printHyperLink(topFile,
				getText("doclet.Non_Frame_Version"));
		println("");
		noFramesEnd();
	}

	/**
	 * Print the frame sizes and their contents.
	 */
	protected void printFrameDetails()
	{
		frameSet("cols=\"20%,80%\"");
		if(noOfPackages<=1)
		{
			printAllAgentsFrameTag();
		}
		else if(noOfPackages>1)
		{
			frameSet("rows=\"30%,70%\"");
			printAllPackagesFrameTag();
			printAllAgentsFrameTag();
			frameSetEnd();
		}
		printAgentFrameTag();
		printFrameWarning();
		frameSetEnd();
	}

	/**
	 * Print the FRAME tag for the frame that lists all packages
	 */
	private void printAllPackagesFrameTag()
	{
		frame("src=\"overview-frame.html\" name=\"packageListFrame\""
				+" title=\""+getText("doclet.All_Packages")+"\"");
	}

	/**
	 * Print the FRAME tag for the frame that lists all agents
	 */
	private void printAllAgentsFrameTag()
	{
		frame("src=\""+"allagents-frame.html"+"\""
				+" name=\"packageFrame\""
				+" title=\""+getText("doclet.All_agents_and_capabilities")
				+"\"");
	}

	/**
	 * Print the FRAME tag for the frame that describes the class in detail
	 */
	private void printAgentFrameTag()
	{
		frame("src=\""+topFile+"\""
				+" name=\"agentFrame\""
				+" title=\""
				+getText("doclet.Package_agent_and_capability_descriptions")
				+"\"");
	}


	/**
	 * Decide the page which will appear first in the right-hand frame. It will
	 * be "overview-summary.html" if "-overview" option is used or no
	 * "-overview" but the number of packages is more than one. It will be
	 * "package-summary.html" of the respective package if there is only one
	 * package to document. It will be a agent page(first in the sorted order),
	 * if only agents are provided on the command line.
	 */
	protected String getTopFile()
	{
		String topFile = "";
		if((configuration.overview || noOfPackages>1) && !configuration.nooverview)
		{
			topFile = "overview-summary.html";
			configuration.createoverview = true;
		}
		else
		{

			if(noOfAgents>0)
			{
				IMCapability[] caparr = configuration.getSpecifiedAgents();
				Arrays.sort(caparr, new ElementNameComparator());
				IMCapability cap = caparr[0];
				String pac = cap.getPackage();
				if(noOfPackages==1)
				{
					topFile = pathToAgent(cap);
				}
				else
				{
					topFile = pathString(pac, "package-summary.html");
				}
			}
		}
		return topFile;
	}


}



