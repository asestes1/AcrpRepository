package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import random_processes.SimilarityGaussianProcess;

/**
 * This class uses a Gaussian process to estimate each TMIAction's value, and
 * then chooses the one with the highest estimated value.
 * 
 * @author Alex
 *
 */
public class GpGreedySolver extends GpIndexSolver {

	public GpGreedySolver(SimilarityGaussianProcess my_process, double bandwidth) {
		super(my_process,bandwidth);
	}

	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector distances, int remaining_time) {
		RealVector similarities = distancesToSimilarities(distances);
		Iterator<SimpleTmiAction> past_actions = actionHistory.iterator();
		HashMap<SimpleTmiAction, Double> my_indices = new HashMap<SimpleTmiAction, Double>();
		while (past_actions.hasNext()) {
			SimpleTmiAction next_action = past_actions.next();
			if (!my_indices.containsKey(next_action)) {
				double estimated_value = myProcess.postMean(next_action, similarities);
				// System.out.println(next_action);
				// System.out.println(estimated_value);
				my_indices.put(next_action, estimated_value);
			}
		}
		return my_indices;
	}

}
