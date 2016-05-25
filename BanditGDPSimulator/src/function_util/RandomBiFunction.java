package function_util;

import java.util.function.BiFunction;

import org.apache.commons.math3.distribution.RealDistribution;

public interface RandomBiFunction<R,S> extends BiFunction<R, S, Double>{
	
	@Override
	public default Double apply(R r,S s){
		return distribution(r,s).sample();
	}
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	public Double mean(R r,S s);
	
	/**
	 * This should return the distribution that
	 * function evaluations at point t take.
	 * @param t
	 * @return
	 */
	public RealDistribution distribution(R t,S s);
	
	/**
	 * The error distribution should be the distribution at
	 * point t minus the mean.
	 * @param t
	 * @return
	 */
	public default RealDistribution errorDistribution(R r,S s){
		return new ShiftedDistribution(distribution(r,s),-1*mean(r,s));
	}

}
