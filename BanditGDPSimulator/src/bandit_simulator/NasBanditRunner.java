package bandit_simulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import bandit_objects.SimpleTmiAction;
import bandit_solvers.SimilarityBanditSolver;
import function_util.BiFunctionEx;
import state_representation.DefaultState;

public class NasBanditRunner {
	private NasBanditRunner() {

	}

	/**
	 * This runs the bandit instance with the given solution method, and
	 * provides the total reward from running the bandit.
	 * 
	 * @param myBandit
	 *            - the bandit solution algorithm
	 * @param timeHorizon
	 *            - the time horizon. This should not be larger than the maximum
	 *            time horizon of this instances.
	 * @return
	 * @throws Exception
	 */
	public static final NasBanditOutcome runBandit(
			BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> rewardFunction, NasBanditInstance myInstance,
			SimilarityBanditSolver myBandit, int timeHorizon) throws Exception {
		// This passes the history into the bandit.
		primeBandit(myInstance, myBandit);
		int maxTimeHorizon = myInstance.getMaxTimeHorizon();
		if (timeHorizon > maxTimeHorizon || timeHorizon < 0) {
			throw new OutOfRangeException(timeHorizon, 0, maxTimeHorizon);
		}

		// This gives the set of states to be run
		List<DefaultState> unseenStates = myInstance.getUnseenStates();
		Iterator<DefaultState> stateIter = unseenStates.iterator();

		// This gives the similarities between the new states and the historical
		// states
		Iterator<RealVector> similarities = myInstance.getUnseenDistances().iterator();

		// This is the list of actions that we will choose from
		List<SimpleTmiAction> actionList = new ArrayList<SimpleTmiAction>(timeHorizon);

		// This will store the rewards that we observe
		List<Double> rewardList = new ArrayList<Double>(timeHorizon);

		// This will store the matrix of all similarity information, for new
		// states together with old states
		List<RealVector> allSimilarities = myInstance.getHistoricalOutcomes().getSimilarities();
		for (int i = 0; i < timeHorizon; i++) {
			// Get the next unseen state
			DefaultState nextState = stateIter.next();
			// Get the next similarity context vector
			RealVector nextContext = similarities.next();
			allSimilarities.add(nextContext);
			// Use our bandit solving method to generate the tmi.
			SimpleTmiAction nextAction = myBandit.suggestAction(nextContext, timeHorizon - i);
			//System.out.println(nextAction);
			// Calculate our reward
			double reward = rewardFunction.apply(nextState, nextAction);
			// Pass the reward to the bandit
			myBandit.addHistory(nextContext, nextAction, reward);
			// Update our list of TMIs taken
			actionList.add(nextAction);
			// Update our list of rewards observed
			rewardList.add(reward);
		}
		return new NasBanditOutcome(myInstance.getHistoricalOutcomes().getActionSet(), allSimilarities,
				actionList, rewardList);
	}

	/**
	 * 
	 * @param myInstance
	 * @param myBandit
	 * @throws Exception
	 */
	public static final void primeBandit(NasBanditInstance myInstance, SimilarityBanditSolver myBandit)
			throws Exception {
		NasBanditOutcome historicalOutcomes = myInstance.getHistoricalOutcomes();

		int historyTime = historicalOutcomes.getActions().size();
		Iterator<SimpleTmiAction> actionIter = historicalOutcomes.getActions().iterator();
		Iterator<Double> rewardIter = historicalOutcomes.getRewards().iterator();
		Iterator<RealVector> similarities = historicalOutcomes.getSimilarities().iterator();
		for (int i = 0; i < historyTime; i++) {
			RealVector similarity = similarities.next();
			Double nextReward = rewardIter.next();
			SimpleTmiAction nextAction = actionIter.next();
			myBandit.addHistory(similarity, nextAction, nextReward);
		}
	}

	public static final SimpleTmiAction getBestAction(BiFunction<RealVector, SimpleTmiAction, Double> meanFunction,
			RealVector state, Set<SimpleTmiAction> possibleActions) {
		Iterator<SimpleTmiAction> actionIter = possibleActions.iterator();
		double bestValue = Double.NEGATIVE_INFINITY;
		SimpleTmiAction bestAction = null;
		while (actionIter.hasNext()) {
			SimpleTmiAction nextAction = actionIter.next();
			double value = meanFunction.apply(state, nextAction);
			if (value > bestValue) {
				bestAction = nextAction;
				bestValue = value;
			}
		}
		return bestAction;
	}

	public static final double getBestValue(BiFunction<RealVector, SimpleTmiAction, Double> meanFunction,
			RealVector state, Set<SimpleTmiAction> possibleActions) {
		SimpleTmiAction bestAction = getBestAction(meanFunction, state, possibleActions);
		return meanFunction.apply(state, bestAction);
	}

	public static final RealVector computeSimilarity(BiFunction<RealVector, RealVector, Double> similarityFunction,
			List<RealVector> stateHistory, int time, RealVector state) {
		Iterator<RealVector> stateIter = stateHistory.iterator();
		RealVector similarities = new ArrayRealVector(time);
		for (int i = 0; i < time; i++) {
			RealVector nextState = stateIter.next();
			similarities.setEntry(i, similarityFunction.apply(nextState, state));
		}
		return similarities;
	}

	public static final DescriptiveStatistics calculateRewardStats(NasBanditOutcome myOutcome) {
		Iterator<Double> rewardIter = myOutcome.getRewards().iterator();
		int outcomeLength = myOutcome.getOutcomeLength();
		DescriptiveStatistics myStats = new DescriptiveStatistics();
		for (int i = 0; i < outcomeLength; i++) {
			myStats.addValue(rewardIter.next());
		}
		return myStats;
	}
}