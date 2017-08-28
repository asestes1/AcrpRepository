package bandit_solvers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public abstract class SimilarityBanditSolver {
	protected List<SimpleTmiAction> actionHistory;
	protected List<Double> rewardHistory; 
	//This keeps a list of the similarity context vectors. The nth vector 
	//has a dimension of n-1. The 1st vector is included as an empty vector.
	protected List<List<Double>> contextHistory;
	protected final Double bandwidth;
	public abstract SimpleTmiAction suggestAction(RealVector similarities, int remainingTime) throws Exception;
	
	public SimilarityBanditSolver(double bandwidth){
		this.actionHistory = new ArrayList<SimpleTmiAction>();
		this.rewardHistory = new ArrayList<Double>();
		this.contextHistory = new ArrayList<List<Double>>();
		this.bandwidth = bandwidth;
	}
	
	public SimilarityBanditSolver(Double bandwidth){
		this.actionHistory = new ArrayList<SimpleTmiAction>();
		this.rewardHistory = new ArrayList<Double>();
		this.contextHistory = new ArrayList<List<Double>>();
		this.bandwidth = bandwidth;
	}
	public void addHistory(RealVector context,SimpleTmiAction action,Double outcome){
		Iterator<List<Double>> contextIter = contextHistory.iterator();
		List<Double> newContext = new ArrayList<Double>();
		int historyLength = contextHistory.size();
		for(int i=0; i < historyLength;i++){
			contextIter.next().add(context.getEntry(i));
			newContext.add(context.getEntry(i));
		}
		newContext.add(1.0);
		contextHistory.add(newContext);
		actionHistory.add(action);
		rewardHistory.add(outcome);
	}
	
	public void reset(){
		this.actionHistory = new ArrayList<SimpleTmiAction>();
		this.rewardHistory = new ArrayList<Double>();
		this.contextHistory = new ArrayList<List<Double>>();
	}
	
	protected Double distanceToSimilarity(Double distance){
		return new KernelFunction().apply(distance);
	}
	
	protected RealVector distancesToSimilarities(RealVector distances){
		return distances.map(new KernelFunction());
	}
	
	protected final class KernelFunction implements Function<Double,Double>, UnivariateFunction{

		@Override
		public Double apply(Double distance) {
			if(bandwidth.equals(Double.POSITIVE_INFINITY)){
				return 1.0;
			}else if(bandwidth.equals(0.0)){
				if(distance == 0.0){
					return 1.0;
				}else{
					return 0.0;
				}
			}else{
				return Math.exp(-Math.pow(distance, 2.0) /  bandwidth);
			}
		}

		@Override
		public double value(double arg0) {
			return apply(arg0);
		}
		
	}
	
	public Double getBandwidth(){
		return bandwidth;
	}

	
}
