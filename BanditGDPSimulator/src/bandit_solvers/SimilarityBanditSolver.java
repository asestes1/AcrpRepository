package bandit_solvers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public abstract class SimilarityBanditSolver {
	protected List<SimpleTmiAction> actionHistory;
	protected List<Double> rewardHistory; 
	//This keeps a list of the similarity context vectors. The nth vector 
	//has a dimension of n-1. The 1st vector is included as an empty vector.
	protected List<List<Double>> contextHistory;
	public abstract SimpleTmiAction suggestAction(RealVector similarities, int remainingTime) throws Exception;
	
	public SimilarityBanditSolver(){
		this.actionHistory = new ArrayList<SimpleTmiAction>();
		this.rewardHistory = new ArrayList<Double>();
		this.contextHistory = new ArrayList<List<Double>>();
		
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
	
}
