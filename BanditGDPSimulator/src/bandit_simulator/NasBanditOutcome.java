package bandit_simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.Immutable;
import bandit_objects.SimpleTmiAction;
import state_representation.DefaultState;

public class NasBanditOutcome implements Immutable, Serializable {
	private static final long serialVersionUID = -9020134556481796430L;
	private final Set<SimpleTmiAction> actionSet;
	private final List<DefaultState> states;
	private final List<RealVector> similarities;
	private final List<SimpleTmiAction> actions;
	private final List<Double> rewards;

	public NasBanditOutcome() {
		this.actionSet = new HashSet<SimpleTmiAction>();
		this.states = new ArrayList<DefaultState>();
		this.similarities = new ArrayList<RealVector>();
		this.actions = new ArrayList<SimpleTmiAction>();
		this.rewards = new ArrayList<Double>();
	}

	public NasBanditOutcome(Collection<SimpleTmiAction> actionSet,List<DefaultState> states, List<RealVector> similarities,
			List<SimpleTmiAction> actions, List<Double> rewards) {
		super();
		this.actionSet = new HashSet<SimpleTmiAction>(actionSet);
		this.states = new ArrayList<DefaultState>(states);
		this.similarities = new ArrayList<RealVector>(similarities);
		this.actions = new ArrayList<SimpleTmiAction>(actions);
		this.rewards = new ArrayList<Double>(rewards);
	}

	public NasBanditOutcome(List<DefaultState> states, List<RealVector> similarities,List<SimpleTmiAction> actions, List<Double> rewards) {
		this(actions, states, similarities,actions, rewards);
	}

	public List<RealVector> getSimilarities() {
		return similarities;
	}

	public Set<SimpleTmiAction> getActionSet() {
		return new HashSet<SimpleTmiAction>(actionSet);
	}

	public List<DefaultState> getStates() {
		return new ArrayList<DefaultState>(states);
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
		Iterator<DefaultState> stateIter = states.iterator();
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
