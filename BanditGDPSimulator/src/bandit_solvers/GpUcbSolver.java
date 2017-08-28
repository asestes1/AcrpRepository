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
	
	public GpUcbSolver(SimilarityGaussianProcess myProcess,
				double power, Double bandwidth) {
			super(myProcess,bandwidth);
		this.power = power;
	}
	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector distances, int remainingTime) {
		RealVector similarities = distancesToSimilarities(distances);
		int previousTime = actionHistory.size();
		int totalTime = previousTime+remainingTime;
		double quantile = (1-1/((previousTime+1)*Math.pow(Math.log(totalTime),power)));
		Iterator<SimpleTmiAction> pastActions = actionHistory.iterator();
		HashMap<SimpleTmiAction,Double> myIndices = new HashMap<SimpleTmiAction,Double>();
		while(pastActions.hasNext()){
			SimpleTmiAction nextAction = pastActions.next();
			if(!myIndices.containsKey(nextAction)){
				double mean = myProcess.postMean(nextAction, similarities);
				double var = myProcess.postCov(nextAction, nextAction, similarities);
				if(var > 0.0){
					NormalDistribution myDistribution = new NormalDistribution(mean,Math.sqrt(var));
					double ucb_index = myDistribution.inverseCumulativeProbability(quantile);
					myIndices.put(nextAction, ucb_index);
				}else{
					//System.out.println("WARNING: NUMERICAL INSTABILITY");
					myIndices.put(nextAction, mean);
				}

			}		
		}
		return myIndices;
	}
}
