package jadex.tools.jadexdoc.doclets.standard;

import java.io.IOException;
import java.util.List;
import jadex.model.*;
import jadex.tools.jadexdoc.doclets.*;

/**
 *  Generate the file with list of all the classes in this run. This page will be
 *  used in the left-hand bottom frame, when "All Agents" link is clicked in
 *  the left-hand top frame. The name of the generated file is
 *  "allagents-frame.html".
 */
public class AllAgentsFrameWriter extends HtmlStandardWriter
{

	/** The name of the output file with frames. */
	public static final String OUTPUT_FILE_NAME_FRAMES = "allagents-frame.html";

	/** The name of the output file without frames. */
	public static final String OUTPUT_FILE_NAME_NOFRAMES = "allagents-noframe.html";

	/**
	 * Index of all the classes.
	 */
	protected IndexBuilder indexbuilder;

	/**
	 * Construct AllAgentsFrameWriter object. Also initilises the indexbuilder
	 * variable in this class.
	 * @throws java.io.IOException
	 * @throws DocletAbortException
	 */
	public AllAgentsFrameWriter(StandardConfiguration configuration, String filename, IndexBuilder indexbuilder)
		throws IOException
	{
		super(configuration, filename);
		this.indexbuilder = indexbuilder;
	}

	/**
	 * Create AllAgentsFrameWriter object. Then use it to generate the
	 * "allagents-frame.html" file. Generate the file in the current or the
	 * destination directory.
	 * @param indexbuilder IndexBuilder object for all agents index.
	 * @throws DocletAbortException
	 */
	public static void generate(StandardConfiguration configuration, IndexBuilder indexbuilder)
	{
		AllAgentsFrameWriter allclassgen;
		String filename = OUTPUT_FILE_NAME_FRAMES;
		try
		{
			allclassgen = new AllAgentsFrameWriter(configuration,
					filename, indexbuilder);
			allclassgen.generateAllAgentsFile(true);
			allclassgen.close();
			filename = OUTPUT_FILE_NAME_NOFRAMES;
			allclassgen = new AllAgentsFrameWriter(configuration,
					filename, indexbuilder);
			allclassgen.generateAllAgentsFile(false);
			allclassgen.close();
		}
		catch(IOException exc)
		{
			configuration.standardmessage.
					error("doclet.exception_encountered",
							exc.toString(), filename);
			throw new DocletAbortException();
		}
	}

	/**
	 * Print all the classes in table format in the file.
	 * @param wantFrames True if we want frames.
	 */
	protected void generateAllAgentsFile(boolean wantFrames) throws IOException
	{
		String label = getText("doclet.All_Agents");

		printHtmlHeader(label, null, false);

		printAllAgentsTableHeader();
		printAllAgents(wantFrames);
		printAllAgentsTableFooter();

		printBodyHtmlEnd();
	}

	/**
	 * Use the sorted index of all the classes and print all the classes.
	 * @param wantFrames True if we want frames.
	 */
	protected void printAllAgents(boolean wantFrames)
	{
		for(int i = 0; i<indexbuilder.elements().length; i++)
		{
			Character unicode = (Character)((indexbuilder.elements())[i]);
			generateContents(indexbuilder.getMemberList(unicode), wantFrames);
		}
	}

	/**
	 * Given a list of agents, generate links for each agents or capability.
	 * If the agent kind is capability, print it in the italics font. Also all
	 * links should target the right-hand frame. If clicked on any agent name
	 * in this page, appropriate agent page should get opened in the right-hand
	 * frame.
	 * @param agentlist Sorted list of agents.
	 * @param wantFrames True if we want frames.
	 */
	protected void generateContents(List agentlist, boolean wantFrames)
	{
		for(int i = 0; i<agentlist.size(); i++)
		{
			IMCapability cd = (IMCapability)(agentlist.get(i));

			String label = Standard.getMemberName(cd);
			if(!(cd instanceof IMBDIAgent))
			{
				label = italicsText(label);
			}

			if(wantFrames)
			{
				print(getAgentLink(cd, "", label, false, "", "agentFrame"));
			}
			else
			{
				print(getAgentLink(cd, "", label, false, "", ""));
			}
			br();
		}
	}

	/**
	 * Print the heading "All Agents" and also print Html table tag.
	 */
	protected void printAllAgentsTableHeader()
	{
		fontSizeStyle("+1", "FrameHeadingFont");
		boldText("doclet.All_Agents");
		fontEnd();
		br();
		table();
		tr();
		tdNowrap();
		fontStyle("FrameItemFont");
	}

	/**
	 * Print Html closing table tag.
	 */
	protected void printAllAgentsTableFooter()
	{
		fontEnd();
		tdEnd();
		trEnd();
		tableEnd();
	}
}