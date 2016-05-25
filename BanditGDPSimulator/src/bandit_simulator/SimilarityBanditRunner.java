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

public class SimilarityBanditRunner {
	private SimilarityBanditRunner() {

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
	public static final BanditOutcome runBandit(BiFunction<RealVector, SimpleTmiAction, Double> rewardFunction,
			BiFunction<RealVector, RealVector, Double> similarityFunction, SimilarityBanditInstance myInstance,
			SimilarityBanditSolver myBandit, int timeHorizon) throws Exception {
		primeBandit(similarityFunction, myInstance, myBandit);
		int maxTimeHorizon = myInstance.getMaxTimeHorizon();
		if (timeHorizon > maxTimeHorizon || timeHorizon < 0) {
			throw new OutOfRangeException(timeHorizon, 0, maxTimeHorizon);
		}
		List<RealVector> allStates = myInstance.getHistoricalOutcomes().getStates();
		int historyLength = allStates.size();
		List<RealVector> unseenStates = myInstance.getUnseenStates();
		allStates.addAll(unseenStates);
		Iterator<RealVector> stateIter = unseenStates.iterator();
		List<SimpleTmiAction> actionList = new ArrayList<SimpleTmiAction>(timeHorizon);
		List<Double> rewardList = new ArrayList<Double>(timeHorizon);
		for (int i = 0; i < timeHorizon; i++) {
			RealVector nextState = stateIter.next();
			RealVector nextContext = computeSimilarity(similarityFunction, allStates, historyLength + i, nextState);

			SimpleTmiAction nextAction = myBandit.suggestAction(nextContext, timeHorizon - i);
			double reward = rewardFunction.apply(nextState, nextAction);
			myBandit.addHistory(nextContext, nextAction, reward);
			actionList.add(nextAction);
			rewardList.add(reward);
		}
		return new BanditOutcome(myInstance.getHistoricalOutcomes().getActionSet(), unseenStates, actionList,
				rewardList);
	}

	public static final void primeBandit(BiFunction<RealVector, RealVector, Double> similarityFunction,
			SimilarityBanditInstance myInstance, SimilarityBanditSolver myBandit) throws Exception {
		BanditOutcome historicalOutcomes = myInstance.getHistoricalOutcomes();
		List<RealVector> stateHistory = historicalOutcomes.getStates();
		List<Double> rewardHistory = historicalOutcomes.getRewards();
		List<SimpleTmiAction> actionHistory = historicalOutcomes.getActions();

		int historyTime = actionHistory.size();
		Iterator<SimpleTmiAction> actionIter = actionHistory.iterator();
		Iterator<Double> rewardIter = rewardHistory.iterator();
		Iterator<RealVector> stateIter = stateHistory.iterator();
		for (int i = 0; i < historyTime; i++) {
			RealVector nextState = stateIter.next();
			RealVector similarity = computeSimilarity(similarityFunction, stateHistory, i, nextState);
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

	/**
	 * This pre-generates all of the states for a similarity bandit instance. No
	 * action history or reward history is generated.
	 * 
	 * @param rewardFunction
	 * @param contextGenerator
	 * @param similarityFunction
	 * @param maxTimeHorizon
	 * @return
	 */
	public static final SimilarityBanditInstance makeInstance(Generator<RealVector> contextGenerator,
			int maxTimeHorizon) {
		List<RealVector> myHistory = pregenerateHistory(contextGenerator, maxTimeHorizon);
		return new SimilarityBanditInstance(myHistory, new BanditOutcome());
	}

	/**
	 * This uses a similarity bandit solver to generate a history.
	 * 
	 * @param rewardFunction
	 * @param contextGenerator
	 * @param similarityFunction
	 * @param maxTimeHorizon
	 * @param historyTime
	 * @param historySolver
	 * @return
	 * @throws Exception
	 */
	public static final SimilarityBanditInstance makeInstance(
			BiFunction<RealVector, SimpleTmiAction, Double> rewardFunction, Generator<RealVector> contextGenerator,
			BiFunction<RealVector, RealVector, Double> similarityFunction, int runTime, int historyTime,
			SimilarityBanditSolver historySolver) throws Exception {

		BanditOutcome myOutcomes = makeHistoricalOutcomes(rewardFunction, contextGenerator, similarityFunction,
				historyTime, historySolver);
		List<RealVector> myHistory = pregenerateHistory(contextGenerator, runTime);
		return new SimilarityBanditInstance(myHistory, myOutcomes);
	}

	public static final BanditOutcome makeHistoricalOutcomes(
			BiFunction<RealVector, SimpleTmiAction, Double> rewardFunction, Generator<RealVector> contextGenerator,
			BiFunction<RealVector, RealVector, Double> similarityFunction, int historyTime,
			SimilarityBanditSolver historySolver) throws Exception {
		List<RealVector> contextStates = new ArrayList<RealVector>(historyTime);
		List<SimpleTmiAction> actionHistory = new ArrayList<SimpleTmiAction>(historyTime);
		List<Double> rewardHistory = new ArrayList<Double>(historyTime);
		for (int i = 0; i < historyTime; i++) {
			RealVector nextContext = contextGenerator.generate();
			contextStates.add(nextContext);
			RealVector nextSimilarities = computeSimilarity(similarityFunction, contextStates, i, nextContext);
			SimpleTmiAction nextAction = historySolver.suggestAction(nextSimilarities, historyTime);
			Double reward = rewardFunction.apply(nextContext, nextAction);
			actionHistory.add(nextAction);
			rewardHistory.add(reward);
		}
		return new BanditOutcome(contextStates, actionHistory, rewardHistory);
	}

	public static final List<RealVector> pregenerateHistory(Generator<RealVector> contextGenerator,
			int maxTimeHorizon) {
		List<RealVector> contextStates = new ArrayList<RealVector>(maxTimeHorizon);
		for (int i = 0; i < maxTimeHorizon; i++) {
			RealVector nextContext = contextGenerator.generate();
			contextStates.add(nextContext);
		}
		return contextStates;
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

	public static final DescriptiveStatistics calculateRewardStats(
			BiFunction<RealVector, SimpleTmiAction, Double> meanFunction, BanditOutcome myOutcome,
			Set<SimpleTmiAction> actionSet) {
		Iterator<Double> rewardIter = myOutcome.getRewards().iterator();
		int outcomeLength = myOutcome.getOutcomeLength();
		DescriptiveStatistics myStats = new DescriptiveStatistics();
		for (int i = 0; i < outcomeLength; i++) {
			myStats.addValue(rewardIter.next());
		}
		return myStats;
	}

	public static final DescriptiveStatistics calculateRegretStats(
			BiFunction<RealVector, SimpleTmiAction, Double> meanFunction, BanditOutcome myOutcome,
			Set<SimpleTmiAction> actionSet) {
		Iterator<RealVector> stateIter = myOutcome.getStates().iterator();
		Iterator<SimpleTmiAction> takenActionIter = myOutcome.getActions().iterator();
		int outcomeLength = myOutcome.getOutcomeLength();
		DescriptiveStatistics regretStatistics = new DescriptiveStatistics();
		for (int i = 0; i < outcomeLength; i++) {
			RealVector nextState = stateIter.next();
			SimpleTmiAction nextTakenAction = takenActionIter.next();
			double expectedReward = meanFunction.apply(nextState, nextTakenAction);
			double bestValue = getBestValue(meanFunction, nextState, actionSet);
			regretStatistics.addValue(bestValue - expectedReward);
		}
		return regretStatistics;
	}
}