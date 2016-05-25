package bandit_solvers;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public abstract class IndexSolver extends SimilarityBanditSolver {
	public IndexSolver() {
		super();
	}
	@Override
	public final SimpleTmiAction suggestAction(RealVector similarities, int remainingTime) {
		Map<SimpleTmiAction,Double> myValues = getIndices(similarities, remainingTime);
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
