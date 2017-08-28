package bandit_solvers;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class ContextActionBall {
	private final int centralStateId;
	private final SimpleTmiAction centralAction;
	private final Double radius;
	private int numObs;
	private double totalReward;

	public ContextActionBall(int stateId, SimpleTmiAction centralAction, Double radius) {
		this.centralStateId = stateId;
		this.centralAction = centralAction;
		this.radius = radius;
		this.numObs = 0;
		this.totalReward = 0;
	}

	public boolean contains(BiFunction<SimpleTmiAction, SimpleTmiAction, Double> tmiComparer,
			Function<Double, Double> distanceToSimilarityKernel, SimpleTmiAction action, RealVector distances) {
		double actionSimilarity = tmiComparer.apply(centralAction, action);
		double stateSimilarity = distanceToSimilarityKernel.apply(distances.getEntry(centralStateId));
		double similarity = actionSimilarity * stateSimilarity;
		if ((1 - similarity) <= radius) {
			return true;
		} else {
			return false;
		}

	}

	public void tryAddObservation(BiFunction<SimpleTmiAction, SimpleTmiAction, Double> tmi_comparer,
			Function<Double, Double> distanceToSimilarityKernel, SimpleTmiAction action, RealVector similarities,
			Double reward) {
		if (contains(tmi_comparer, distanceToSimilarityKernel, action, similarities)) {
			numObs++;
			totalReward += reward;
		}
	}

	public void addReward(Double reward) {
		numObs++;
		totalReward += reward;
	}

	public Double getConf(int T, Double lipschitz_c) {
		return lipschitz_c * 4 * Math.sqrt(Math.log(T) / (1 + numObs));
	}

	public Double getPreIndex(int T, Double lipschitz_c) {
		double index = 0.0;
		if (numObs > 0) {
			index = totalReward / numObs;
		}
		index += lipschitz_c * (radius + getConf(T, lipschitz_c));
		return index;
	}

	public int getNumObs() {
		return numObs;
	}

	public void setNumObs(int numObs) {
		this.numObs = numObs;
	}

	public double getTotalReward() {
		return totalReward;
	}

	public void setTotalReward(double totalReward) {
		this.totalReward = totalReward;
	}

	public int getCentralStateId() {
		return centralStateId;
	}

	public SimpleTmiAction getCentralAction() {
		return centralAction;
	}

	public Double getRadius() {
		return radius;
	}

	@Override
	public String toString() {
		String myString = centralStateId + "," + centralAction.toString() + ",";
		myString += radius + "," + numObs + "," + totalReward;
		return myString;
	}

}
