package bandit_solvers;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import random_processes.SimilarityGaussianProcess;

/**
 * This class represents an algorithm for solving bandits which involves
 * a Gaussian process.
 * @author Alex
 *
 */
public abstract class GpIndexSolver extends IndexSolver {
	protected SimilarityGaussianProcess myProcess;
	
	public GpIndexSolver(SimilarityGaussianProcess my_process, double bandwidth) {
		super(bandwidth);
		this.myProcess = my_process;
	}
	
	@Override
	public void addHistory(RealVector distances, SimpleTmiAction action, Double outcome) {
		super.addHistory(distances, action, outcome);
		RealVector similarities = distancesToSimilarities(distances);
		myProcess.addEvaluation(action, similarities, outcome);
	}
	
	@Override
	public void reset() {
		myProcess = new SimilarityGaussianProcess(myProcess.getMeanFunction(),myProcess.getCovarianceFunction(),myProcess.getNumPoints());
		super.reset();
	}
}
