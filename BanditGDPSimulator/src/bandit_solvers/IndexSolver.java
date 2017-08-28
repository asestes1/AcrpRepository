package bandit_solvers;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public abstract class IndexSolver extends SimilarityBanditSolver {
	public IndexSolver(double bandwidth) {
		super(bandwidth);
	}
	@Override
	public final SimpleTmiAction suggestAction(RealVector distances, int remainingTime) {
		Map<SimpleTmiAction,Double> myValues = getIndices(distances, remainingTime);
		Iterator<SimpleTmiAction> pointIter = myValues.keySet().iterator();
		
		double bestOutcome = Double.NEGATIVE_INFINITY;
		SimpleTmiAction bestAction = null;
		while(pointIter.hasNext()){
			SimpleTmiAction nextAction = pointIter.next();
			double index = myValues.get(nextAction);
			if(index > bestOutcome){
				bestAction = nextAction;
				bestOutcome = index;
			}
		}
		return bestAction;
	}
	
	public abstract Map<SimpleTmiAction,Double> getIndices(RealVector similarities, int remainingTime);

}
