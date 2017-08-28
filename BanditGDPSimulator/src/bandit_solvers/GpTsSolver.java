package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import random_processes.SimilarityGaussianProcess;

public class GpTsSolver extends GpIndexSolver{

	public GpTsSolver(SimilarityGaussianProcess my_process,double bandwidth) {
		super(my_process,bandwidth);
	}
	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector distances, int remaining_time) {
		RealVector similarities = distancesToSimilarities(distances);

		Iterator<SimpleTmiAction> pastActions = actionHistory.iterator();
		HashMap<SimpleTmiAction,Double> myIndices = new HashMap<SimpleTmiAction,Double>();
		SimilarityGaussianProcess copyMyProcess = new SimilarityGaussianProcess(myProcess);
		while(pastActions.hasNext()){
			SimpleTmiAction nextAction = pastActions.next();
			if(!myIndices.containsKey(nextAction)){
				double estimated_value = 
						copyMyProcess.evaluate(nextAction,similarities);
				myIndices.put(nextAction, estimated_value);
			}		
		}
		return myIndices;
	}

}
