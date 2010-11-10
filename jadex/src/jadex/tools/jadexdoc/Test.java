package jadex.tools.jadexdoc;

import java.util.*;
import jadex.model.SXML;

/**
 *
 */
public class Test
{

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{

		List list = new ArrayList();
		//list.add("-notree");
		//list.add("-nonavbar");
		//list.add("-noindex");

		// list of packages to document
// 		  list.add("jadex.examples.blackjack.dealer");
		// list of agents to document
//        list.add("jadex/examples/hunterprey/creature/actsense/Creature.capability.xml");
//        list.add("jadex/examples/hunterprey/creature/CleverPrey.agent.xml");
//        list.add( "jadex/examples/hunterprey/environment/RemoteObserver.agent.xml");
//        list.add("jadex/examples/cleanerworld/multi/cleaner/Cleaner.agent.xml");
//        list.add("jadex/planlib/DF.capability.xml");
//        list.add( "jadex/examples/testcases/NoPackage.agent.xml");

		// list of packages (and subpackages) to document
		list.add("-subpackages");
		list.add("jadex.planlib");
		list.add("-subpackages");
		list.add("jadex.examples");
		list.add("-subpackages");
		list.add("jadex.testcases");
		list.add("-subpackages");
		list.add("jadex.tutorial");
//        list.add("-subpackages");
//        list.add("jadex.adapter.jade.planlib");
//        list.add("-subpackages");
//        list.add("jadex.adapter.jade.examples");

//        list.add("jadex.examples.hunterprey.creature");

		// Group example
		list.add("-group");
		list.add("BlackJack Packages");
		list.add("jadex.examples.blackjack*");
		list.add("-group");
		list.add("Blocksworld Packages");
		list.add("jadex.examples.blocksworld*");
		list.add("-group");
		list.add("Cleanerworld Packages");
		list.add("jadex.examples.cleanerworld*");
		list.add("-group");
		list.add("GarbageCollector Packages");
		list.add("jadex.examples.garbagecollector*");
		list.add("-group");
		list.add("HunterPrey Packages");
		list.add("jadex.examples.hunterprey*");
		list.add("-group");
		list.add("Marsworld Packages");
		list.add("jadex.examples.marsworld*");
		list.add("-group");
		list.add("Puzzle Packages");
		list.add("jadex.examples.puzzle*");

		// Java api doc
		list.add("-link");
		list.add("http://java.sun.com/j2se/1.4.2/docs/api");
		// Jadex api doc
//        list.add("-link");
//		list.add("../api");

		// output directory
		list.add("-d");
		list.add("docs/api");
		//list.add("c:/temp/jadexdoc/test");

		// Title in navbar
		list.add("-header");
		list.add("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>\n"+"<b>Jadex&nbsp;0.94&nbsp;Platform</b></EM>\n"+"</TD>");

		// file with description for overview page
		list.add("-overview");
		list.add("jadex/overview.html");
		// title on overview page
		list.add("-doctitle");
		list.add("Jadex 0.94beta Agent Documentation ");
		// HTML window title extending the current agentname, e.g. TestAgent (WindowTitle)
		list.add("-windowtitle");
		list.add("Jadex Examples");

		// dont show any comments
//        list.add("-nocomment");

		// dont show the top and bottom navbar
//        list.add("-nonavbar");

		// show only exported members
//        list.add("-exported");

		// split the index page
		//list.add("-splitindex");


		// remove comment to show standard members
        //list.add("-standardmembers");

		// dont use following qualifiers
//        list.add("-noqualifier");
//        list.add("jadex.examples.hunterprey.*");

		// deep copy of "doc-files" subdirectories to destination directory
		// list.add("-docfilessubdirs");

		args = (String[])list.toArray(new String[list.size()]);
		Main.main(args);

	}


}
