package jadex.model.jibximpl;

import java.io.FileInputStream;
import java.io.Serializable;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

/**
 *
 */
public class JadexTest implements Serializable
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			// Read an object from xml
			IBindingFactory bfa = BindingDirectory.getFactory(MBDIAgent.class);
			//IBindingFactory bfa = BindingDirectory.getFactory(Capability.class);
			IUnmarshallingContext uc = bfa.createUnmarshallingContext();
			//Object o = uc.unmarshalDocument(new FileInputStream("kernel/src/jadex/model/jibximpl/Test.agent.xml"), null);
			//Object o = uc.unmarshalDocument(new FileInputStream("applications/src/jadex/examples/puzzle/Sokrates.agent.xml"), null);
			Object o = uc.unmarshalDocument(new FileInputStream("applications/src/jadex/testcases/goals/GoalRegressionTest.agent.xml"), null);

			//SXML.setFactory(SXML.FACTORY_JIBX);
			//Object o = SXML.loadAgentModel("kernel/src/jadex/model/jibximpl/Test.agent.xml", null, null);
			//Object o = SXML.loadAgentModel("applications/src/jadex/examples/puzzle/Sokrates.agent.xml", null, null);
			System.out.println("Could read object: "+o+((MElement)o).getName());
			((MElement)o).setup();
			//System.out.println("here: "+SUtil.arrayToString(((Capability)o).getGoalbase().getGoal("eat_food").getDeliberation().getInhibits()));
			//System.out.println("here: "+((MReferenceableElement)o).isExported()+" "+SUtil.arrayToString(((MReferenceableElement)o).getAssignTos()));
			//System.out.println(SUtil.arrayToString(((IMBDIAgent)o).getBeliefbase().getBeliefSet("a").getDefaultFacts()));
			//System.out.println(SUtil.arrayToString(((IMBDIAgent)o).getBeliefbase().getBelief("b").getDefaultFact()));
			//System.out.println(SUtil.arrayToString(((IMBDIAgent)o).getGoalbase().getGoal("b").getParameterSet("bp").getDefaultValues()));

			//System.out.println(SUtil.arrayToString(((IMBDIAgent)o).getBeliefbase().getReferenceableElements()));
			//System.out.println(SUtil.arrayToString(((IMBDIAgent)o).getGoalbase().getReferenceableElements()));
			//System.out.println(((IMBDIAgent)o).getGoalbase().getOwner());
			//System.out.println(((IMBDIAgent)o).getGoalbase().getScope());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

