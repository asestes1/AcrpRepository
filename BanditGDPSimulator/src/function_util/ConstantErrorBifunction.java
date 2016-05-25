package function_util;

import java.util.function.BiFunction;

import org.apache.commons.math3.distribution.RealDistribution;

public class ConstantErrorBifunction<R,S> implements BiFunction<R,S,RealDistribution>{
	private final RealDistribution myDistribution;

	public ConstantErrorBifunction(RealDistribution myDistribution) {
		super();
		this.myDistribution = myDistribution;
	}

	@Override
	public RealDistribution apply(R r,S s) {
		return myDistribution;
	}
	
	
}
