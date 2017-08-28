package bandit_solvers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class ContextualZoomingSolver extends SimilarityBanditSolver{
	protected List<ContextActionBall> contextActionBalls;
	protected final BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmiComparer;
	protected final Double scaleR;
	protected double estimatedLipschitzFactor;
	protected Random randomNumberGenerator;
	protected final Integer timeHorizon;
	
	public ContextualZoomingSolver(BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmiComparer,
			Double scaleR,int timeHorizon,double bandwidth){
		super(bandwidth);
		this.contextActionBalls = new ArrayList<ContextActionBall>();
		this.tmiComparer = tmiComparer;
		this.scaleR = scaleR;
		this.estimatedLipschitzFactor = 0.0;
		this.timeHorizon = timeHorizon;
		randomNumberGenerator = new Random();
	}
	
	@Override
	public void reset() {
		super.reset();
		this.contextActionBalls = new ArrayList<ContextActionBall>();
		this.estimatedLipschitzFactor = 0.0;
	}
	
	@Override
	public void addHistory(RealVector distances, SimpleTmiAction action, Double outcome) {
		if(distances.getDimension() == 0){
			ContextActionBall firstBall = new ContextActionBall(0, action, 1.0);
			firstBall.addReward(outcome);
			contextActionBalls.add(firstBall);
		}else{
			ContextActionBall bestBall = getBestBall(distances,action);
			updateLipschitzEstimate(distances,action,outcome);
			bestBall.addReward(outcome);
			if(bestBall.getConf(timeHorizon, estimatedLipschitzFactor) < bestBall.getRadius()){
				ContextActionBall newBall = new ContextActionBall(
						actionHistory.size()-1, action, bestBall.getRadius()/2.0);
				contextActionBalls.add(newBall);
			}
		}
		super.addHistory(distances, action, outcome);
	}
	
	private void updateLipschitzEstimate(RealVector distances, SimpleTmiAction action, Double outcome) {
		int historyLength = contextHistory.size();
		Iterator<SimpleTmiAction> actionIter = actionHistory.iterator();
		Iterator<Double> rewardIter = rewardHistory.iterator();
		for(int i=0;i < historyLength;i++){
			double actionSimilarity = tmiComparer.apply(action, actionIter.next());
			double combinedDistance = 1-actionSimilarity*distanceToSimilarity(distances.getEntry(i));
			if(combinedDistance > 0){
				double estimatedFactor = scaleR*Math.abs(outcome-rewardIter.next())/combinedDistance ;
				if(estimatedFactor > estimatedLipschitzFactor){
					estimatedLipschitzFactor = estimatedFactor;
				}
			}
		}
		
	}

	public Set<ContextActionBall> getRelevantBalls(RealVector distances){
		Set<ContextActionBall> relevantBalls = new HashSet<ContextActionBall>();
		Set<ContextActionBall> remainingBalls = 
				new HashSet<ContextActionBall>(contextActionBalls);
		Set<SimpleTmiAction> prevActions = new HashSet<SimpleTmiAction>(actionHistory);
		Iterator<SimpleTmiAction> actionIter = prevActions.iterator();
		//This iterates through the actions and finds the smallest ball that
		//contains each action.
		while(actionIter.hasNext()){
			SimpleTmiAction nextAction = actionIter.next();
			Iterator<ContextActionBall> ballIter = remainingBalls.iterator();
			double bestRadius = Double.POSITIVE_INFINITY;
			Set<ContextActionBall> minRadiiSet = new HashSet<ContextActionBall>();
			while(ballIter.hasNext()){
				ContextActionBall nextBall = ballIter.next();
				if(nextBall.contains(tmiComparer,new KernelFunction(), nextAction, distances)){
					double radius = nextBall.getRadius();
					if(radius < bestRadius){
						minRadiiSet = new HashSet<ContextActionBall>();
						minRadiiSet.add(nextBall);
					}else if(radius == bestRadius){
						minRadiiSet.add(nextBall);
					}
				}
			}
			relevantBalls.addAll(minRadiiSet);
			remainingBalls.removeAll(minRadiiSet);
		}
		return relevantBalls;
	}
	
	public Set<ContextActionBall> getRelevantBalls(RealVector distances,
			SimpleTmiAction givenAction){
		Set<ContextActionBall> relevantBalls = new HashSet<ContextActionBall>();
		Set<ContextActionBall> remainingBalls = 
				new HashSet<ContextActionBall>(contextActionBalls);
		Set<SimpleTmiAction> prevActions = new HashSet<SimpleTmiAction>(actionHistory);
		prevActions.add(givenAction);
		
		Iterator<ContextActionBall> ballIter = contextActionBalls.iterator();
		while(ballIter.hasNext()){
			ContextActionBall nextBall = ballIter.next();
			if(!nextBall.contains(tmiComparer,new KernelFunction(), givenAction, distances)){
				remainingBalls.remove(nextBall);
			}
		}
		
		Iterator<SimpleTmiAction> actionIter = prevActions.iterator();
		while(actionIter.hasNext()){
			SimpleTmiAction nextAction = actionIter.next();
			
			double bestRadius = Double.POSITIVE_INFINITY;
			Set<ContextActionBall> minRadiiSet = new HashSet<ContextActionBall>();
			ballIter = remainingBalls.iterator();
			while(ballIter.hasNext()){
				ContextActionBall nextBall = ballIter.next();
				if(nextBall.contains(tmiComparer,new KernelFunction(), nextAction, distances)){
					double radius = nextBall.getRadius();
					if(radius < bestRadius){
						minRadiiSet = new HashSet<ContextActionBall>();
						minRadiiSet.add(nextBall);
					}else if(radius == bestRadius){
						minRadiiSet.add(nextBall);
					}
				}
			}
			
			relevantBalls.addAll(minRadiiSet);
			remainingBalls.removeAll(minRadiiSet);
		}
		return relevantBalls;
	}
	
	public ContextActionBall getBestBall(Set<ContextActionBall> relevantBalls){
		Iterator<ContextActionBall> ballIter =relevantBalls.iterator();
		double bestOverallIndex = Double.NEGATIVE_INFINITY;
		ContextActionBall bestBall = null;
		while(ballIter.hasNext()){
			ContextActionBall nextBall = ballIter.next();
			Iterator<ContextActionBall> otherBallIter = 
					contextActionBalls.iterator();
			int numOtherBalls = contextActionBalls.size();
			double bestInsideIndex = nextBall.getPreIndex(timeHorizon,
					estimatedLipschitzFactor);
			for(int i =0; i < numOtherBalls;i++ ){
				ContextActionBall nextOtherBall = otherBallIter.next();
				Double contextSim = contextHistory.get(i).get(nextOtherBall.getCentralStateId());
				Double actionSim = tmiComparer.apply(nextOtherBall.getCentralAction(),nextBall.getCentralAction());
				Double simDistance = 1-contextSim*actionSim;
				double index = nextOtherBall.getPreIndex(timeHorizon,estimatedLipschitzFactor)
									+estimatedLipschitzFactor*simDistance;
				if(index < bestInsideIndex){
					bestInsideIndex = index;
				}
			}
			double totalIndex = estimatedLipschitzFactor*nextBall.getRadius()+bestInsideIndex;
			if(totalIndex > bestOverallIndex){
				bestOverallIndex = totalIndex;
				bestBall = nextBall;
			}
		}
		return bestBall;
	}
	
	public ContextActionBall getBestBall(RealVector distances){
		Set<ContextActionBall> relevantBalls = getRelevantBalls(distances);
		return getBestBall(relevantBalls);
		
	}
	
	public ContextActionBall getBestBall(RealVector distances, SimpleTmiAction action){
		Set<ContextActionBall> relevantBalls = getRelevantBalls(distances,action);
		return getBestBall(relevantBalls);
	}

	@Override
	public SimpleTmiAction suggestAction(RealVector distances, int remainingTime) throws Exception {
		ContextActionBall bestBall = getBestBall(distances);
		return chooseActionFromBall(bestBall,distances);
	}

	private SimpleTmiAction chooseActionFromBall(ContextActionBall ball, RealVector distances) {
		Iterator<SimpleTmiAction> myActionIter = actionHistory.iterator();
		List<SimpleTmiAction> relevantActions = new ArrayList<SimpleTmiAction>();
		while(myActionIter.hasNext()){
			SimpleTmiAction nextAction = myActionIter.next();
			if(ball.contains(tmiComparer,new KernelFunction(), nextAction, distances)){
				relevantActions.add(nextAction);
			}
		}
		int numActions = relevantActions.size();
		int chosenAction = randomNumberGenerator.nextInt(numActions);
		return relevantActions.get(chosenAction);
	}
	
	
}
