package bandit_solvers;

import java.security.InvalidAlgorithmParameterException;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class UniRandomSolver extends SimilarityBanditSolver{
	private final EnumeratedIntegerDistribution tmiDistribution;
	private final IntegerDistribution scopeDistribution;
	private final IntegerDistribution startTimeDistribution;
	private final IntegerDistribution durationDistribution; 
	private final IntegerDistribution rateDistribution;
	
	private static final int GDP_TYPE = SimpleTmiAction.GDP_TYPE;
	private static final int GS_TYPE = SimpleTmiAction.GS_TYPE;
	private static final int NO_ACTION_TYPE = SimpleTmiAction.NONE_TYPE;
	
	private static final int DEFAULT_START_LOW = 0;
	private static final int DEFAULT_START_HIGH = 1440;
	private static final int DEFAULT_DURATION_LOW = 60;
	private static final int DEFAULT_DURATION_HIGH = 720;
	private static final int DEFAULT_SCOPE_LOW = 200;
	private static final int DEFAULT_SCOPE_HIGH = 3000;
	private static final int DEFAULT_RATE_LOW = 10;
	private static final int DEFAULT_RATE_HIGH = 50;
	
	public UniRandomSolver() {
		int[] tmi_types = {GDP_TYPE,GS_TYPE,NO_ACTION_TYPE};
		double[] probs = {1.0,1.0,1.0};
		tmiDistribution = new EnumeratedIntegerDistribution(tmi_types, probs);
		startTimeDistribution = new UniformIntegerDistribution(DEFAULT_START_LOW,
				DEFAULT_START_HIGH);
		durationDistribution = new UniformIntegerDistribution(DEFAULT_DURATION_LOW,
				DEFAULT_DURATION_HIGH);
		scopeDistribution = new UniformIntegerDistribution(DEFAULT_SCOPE_LOW,
				DEFAULT_SCOPE_HIGH);
		rateDistribution = new UniformIntegerDistribution(DEFAULT_RATE_LOW, DEFAULT_RATE_HIGH);
	}
	
	@Override
	public SimpleTmiAction suggestAction(RealVector similarities, int remainingTime) throws InvalidAlgorithmParameterException {
		int chosenTmiType = tmiDistribution.sample();
		if(chosenTmiType == NO_ACTION_TYPE){
			return new SimpleTmiAction();
		}else if(chosenTmiType == GDP_TYPE){
			return new SimpleTmiAction((double) rateDistribution.sample(),
					(double) scopeDistribution.sample(),
					(double) startTimeDistribution.sample(),
					(double) durationDistribution.sample());
		}else if(chosenTmiType == GS_TYPE){
			return new SimpleTmiAction(0.0, (double) scopeDistribution.sample(),
					(double) startTimeDistribution.sample(),
					(double) durationDistribution.sample());
		}else{
			throw new InvalidAlgorithmParameterException("The TMI type distriubtion"
					+ " generated a TMI type id which does not match any existing"
					+ " TMI type");
		}
	}

}
