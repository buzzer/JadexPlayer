package jadex.examples.blocksworld;

import java.awt.*;
import java.io.*;
import jadex.runtime.*;


/**
 *  Measures Jadex performance by executing several configure goals.
 */
public class EvaluationPlan	extends Plan
{
	//-------- attributes --------

	/** The number of runs to be performed (more runs = more accurate, but slower). */
	protected int	runs;

	/** The number of goals to be added per set of runs. */
	protected int	deltagoals;

	/** The maximum number of goals to be executed. */
	protected int	maxgoals;

	//-------- constructors --------

	/**
	 *  Create a new plan.
	 */
	public EvaluationPlan()
	{
		this(5, 50, 1000);
	}

	/**
	 *  Create a new plan.
	 */
	public EvaluationPlan(int runs, int deltagoals, int maxgoals)
	{
		this.runs	= runs;
		this.deltagoals	= deltagoals;
		this.maxgoals	= maxgoals;
	}

	//-------- methods --------

	/**
	 *  Perform the evaluation.
	 */
	public void	body()
	{
		try
		{
			// Perform initial runs until values settle down.
			long prev, cur=Long.MAX_VALUE;
			do
			{
				prev	= cur;
				cur	= evaluate(deltagoals, false, false);
				cur	+= evaluate(deltagoals, true, false);
				cur	+= evaluate(deltagoals, false, true);
				cur	+= evaluate(deltagoals, true, true);
				System.out.println("Initialization run took: "+cur);
			}
			while(prev>(cur*1.1));

			// Open file.
			PrintWriter	out	= new PrintWriter(new FileWriter(new File("evaluation.csv")));
			out.println("# of Goals; No Deliberation / Sequential; No Deliberation / Concurrent; Deliberation / Sequential; Deliberation / Concurrent");
			System.out.println("# of Goals; No Deliberation / Sequential; No Deliberation / Concurrent; Deliberation / Sequential; Deliberation / Concurrent");
	
			for(int goals=deltagoals; goals<=maxgoals; goals+=deltagoals)
			{
				long noseq	= evaluate(goals, false, false);
				long nocon	= evaluate(goals, false, true);
				long deseq	= evaluate(goals, true, false);
				long decon	= evaluate(goals, true, true);
				out.println(goals+"; "+noseq+"; "+nocon+"; "+deseq+"; "+decon);
				System.out.println(goals+"; "+noseq+"; "+nocon+"; "+deseq+"; "+decon);
			}
	
			out.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		System.out.println("Evaluation finished");
	}
	
	/**
	 *  Evaluate with the given settings.
	 */
	public long	evaluate(int goals, boolean delib, boolean concurrent)
	{
		// Perform runs and save fastest time.
		long	mintime	= Long.MAX_VALUE;
		for(int run=0; run<runs; run++)
		{
			// Setup
			String	goalname	= delib ? "stack_delib" : "stack";
			Block	table	= new Table();
			Block	bucket	= new Table("Bucket", Color.lightGray);
			Block[]	blocks	= new Block[goals];
			for(int i=0; i<blocks.length; i++)
			{
				blocks[i]	= new Block(i, Color.RED, table);
			}

			// Perform run.
			long	time	= System.currentTimeMillis();

			IFilter	filter	= new GoalEventFilter(goalname, true);
			getWaitqueue().addFilter(filter);
			
			if(concurrent)
				startAtomic();
			IFilter[]	filters	= new IFilter[blocks.length];
			for(int i=0; i<goals; i++)
			{
				// Create goal to put block in bucket.
				IGoal	achieve	= createGoal(goalname);
				achieve.getParameter("block").setValue(blocks[i]);
				achieve.getParameter("target").setValue(bucket);
				if(concurrent)
					filters[i]	= dispatchSubgoal(achieve);
				else
					dispatchSubgoalAndWait(achieve);
			}

			if(concurrent)
			{
				endAtomic();
				for(int i=0; i<goals; i++)
				{
					waitFor(filters[i]);
				}
			}
			getWaitqueue().removeFilter(filter);
		
			// Calculate mintime.
			time	= System.currentTimeMillis()-time;
			mintime	= Math.min(time, mintime);
		}

		return mintime*deltagoals/goals;
	}
}

