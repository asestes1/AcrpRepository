package bandit_solvers;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.Generator;

public class DoNothingTmiGenerator implements Generator<SimpleTmiAction> {

	@Override
	public SimpleTmiAction generate() {
		return new SimpleTmiAction();
	}

}
