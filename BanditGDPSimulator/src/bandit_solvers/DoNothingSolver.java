package bandit_solvers;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class DoNothingSolver extends SimilarityBanditSolver {

	@Override
	public SimpleTmiAction suggestAction(RealVector similarities, int remainingTime) throws Exception {
		return new SimpleTmiAction();
	}

}
