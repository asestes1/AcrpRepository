package bandit_simulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.Immutable;
import bandit_objects.SimpleTmiAction;

public class BanditOutcome implements Immutable {
	private final Set<SimpleTmiAction> actionSet;
	private final List<RealVector> states;
	private final List<SimpleTmiAction> actions;
	private final List<Double> rewards;

	public BanditOutcome() {
		this.actionSet = new HashSet<SimpleTmiAction>();
		this.states = new ArrayList<RealVector>();
		this.actions = new ArrayList<SimpleTmiAction>();
		this.rewards = new ArrayList<Double>();
	}

	public BanditOutcome(Collection<SimpleTmiAction> actionSet, List<RealVector> states, List<SimpleTmiAction> actions,
			List<Double> rewards) {
		super();
		this.actionSet = new HashSet<SimpleTmiAction>(actionSet);
		this.states = new ArrayList<RealVector>(states);
		this.actions = new ArrayList<SimpleTmiAction>(actions);
		this.rewards = new ArrayList<Double>(rewards);
	}

	public BanditOutcome(List<RealVector> states, List<SimpleTmiAction> actions, List<Double> rewards) {
		this(actions, states, actions, rewards);
	}

	public Set<SimpleTmiAction> getActionSet() {
		return new HashSet<SimpleTmiAction>(actionSet);
	}

	public List<RealVector> getStates() {
		return new ArrayList<RealVector>(states);
	}

	public List<SimpleTmiAction> getActions() {
		return new ArrayList<SimpleTmiAction>(actions);
	}

	public List<Double> getRewards() {
		return new ArrayList<Double>(rewards);
	}

	public int getOutcomeLength() {
		return rewards.size();
	}

	public String toString() {
		String myString = "Actions under consideration: \n";
		Iterator<SimpleTmiAction> actionIter = actionSet.iterator();
		while (actionIter.hasNext()) {
			myString += actionIter.next().toString();
		}
		int historyLength = rewards.size();
		myString += "History (" + historyLength + "): \n";
		Iterator<RealVector> stateIter = states.iterator();
		actionIter = actions.iterator();
		Iterator<Double> rewardIter = rewards.iterator();
		for (int i = 0; i < historyLength; i++) {
			myString += "(" + stateIter.next().toString() + ",";
			myString += actionIter.next().toString() + ",";
			myString += rewardIter.next() + ")\n";
		}
		return myString;
	}
}
