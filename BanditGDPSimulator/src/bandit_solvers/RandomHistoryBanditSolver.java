package bandit_solvers;

import java.util.Random;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class RandomHistoryBanditSolver extends SimilarityBanditSolver{
	private final Random numberGenerator;
	
	public RandomHistoryBanditSolver() {
		super(1.0);
		this.numberGenerator = new Random();
	}
	@Override
	public SimpleTmiAction suggestAction(RealVector distances, int remainingTime) throws Exception {
		int numActions = actionHistory.size();
		return actionHistory.get(numberGenerator.nextInt(numActions));
	}

}
