package bandit_solvers;

import org.apache.commons.math3.distribution.IntegerDistribution;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.Generator;

public class UniRandomTmiGenerator implements Generator<SimpleTmiAction>{
	private final IntegerDistribution scopeDistribution;
	private final IntegerDistribution startTimeDistribution;
	private final IntegerDistribution durationDistribution; 
	private final IntegerDistribution rateDistribution;
	
	
	public UniRandomTmiGenerator(IntegerDistribution scopeDistribution, IntegerDistribution startTimeDistribution,
			IntegerDistribution durationDistribution, IntegerDistribution rateDistribution) {
		super();
		this.scopeDistribution = scopeDistribution;
		this.startTimeDistribution = startTimeDistribution;
		this.durationDistribution = durationDistribution;
		this.rateDistribution = rateDistribution;
	}


	public IntegerDistribution getScopeDistribution() {
		return scopeDistribution;
	}


	public IntegerDistribution getStartTimeDistribution() {
		return startTimeDistribution;
	}


	public IntegerDistribution getDurationDistribution() {
		return durationDistribution;
	}


	public IntegerDistribution getRateDistribution() {
		return rateDistribution;
	}


	@Override
	public SimpleTmiAction generate() {
		
		return new SimpleTmiAction(rateDistribution.sample(), startTimeDistribution.sample(),
				durationDistribution.sample(), scopeDistribution.sample());
	}

}
