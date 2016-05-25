package function_util;

import java.util.function.Function;

import org.apache.commons.math3.distribution.RealDistribution;

public class ConstantErrorFunction<R> implements Function<R,RealDistribution>{
	private final RealDistribution myDistribution;

	public ConstantErrorFunction(RealDistribution myDistribution) {
		super();
		this.myDistribution = myDistribution;
	}

	@Override
	public RealDistribution apply(R t) {
		return myDistribution;
	}
	
	
}
