package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import random_processes.SimilarityGaussianProcess;

public class GpTsSolver extends GpIndexSolver{

	public GpTsSolver(SimilarityGaussianProcess my_process) {
		super(my_process);
	}
	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector similarities, int remaining_time) {
		Iterator<SimpleTmiAction> pastActions = actionHistory.iterator();
		HashMap<SimpleTmiAction,Double> my_indices = new HashMap<SimpleTmiAction,Double>();
		SimilarityGaussianProcess copy_my_process = new SimilarityGaussianProcess(myProcess);
		while(pastActions.hasNext()){
			SimpleTmiAction next_action = pastActions.next();
			if(!my_indices.containsKey(next_action)){
				double estimated_value = 
						copy_my_process.evaluate(next_action, similarities);
				my_indices.put(next_action, estimated_value);
			}		
		}
		return my_indices;
	}

}
