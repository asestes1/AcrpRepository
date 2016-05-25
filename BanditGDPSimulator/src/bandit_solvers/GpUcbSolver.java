package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import random_processes.SimilarityGaussianProcess;

public class GpUcbSolver  extends GpIndexSolver{
	private final double power;
	
	public GpUcbSolver(SimilarityGaussianProcess my_process,
				double power) {
			super(my_process);
		this.power = power;
	}
	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector similarities, int remaining_time) {
		int previous_time = actionHistory.size();
		int total_time = previous_time+remaining_time;
		double quantile = (1-1/((previous_time+1)*Math.pow(Math.log(total_time),power)));
		Iterator<SimpleTmiAction> past_actions = actionHistory.iterator();
		HashMap<SimpleTmiAction,Double> my_indices = new HashMap<SimpleTmiAction,Double>();
		while(past_actions.hasNext()){
			SimpleTmiAction next_action = past_actions.next();
			if(!my_indices.containsKey(next_action)){
				double mean = myProcess.postMean(next_action, similarities);
				double var = myProcess.postCov(next_action, next_action, similarities);
				if(var > 0.0){
					NormalDistribution my_distribution = new NormalDistribution(mean,Math.sqrt(var));
					double ucb_index = my_distribution.inverseCumulativeProbability(quantile);
					my_indices.put(next_action, ucb_index);
				}else{
					//System.out.println("WARNING: NUMERICAL INSTABILITY");
					my_indices.put(next_action, mean);
				}

			}		
		}
		return my_indices;
	}
}
