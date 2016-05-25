package function_util;

import java.util.function.Function;

import org.apache.commons.math3.distribution.RealDistribution;

public interface RandomFunction<R> extends Function<R,Double>{

	@Override
	public default Double apply(R t){
		return distribution(t).sample();
	}
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	public Double mean(R t);
	
	/**
	 * This should return the distribution that
	 * function evaluations at point t take.
	 * @param t
	 * @return
	 */
	public RealDistribution distribution(R t);
	
	/**
	 * The error distribution should be the distribution at
	 * point t minus the mean.
	 * @param t
	 * @return
	 */
	public default RealDistribution errorDistribution(R t){
		return new ShiftedDistribution(distribution(t),-1*mean(t));
	}
}
