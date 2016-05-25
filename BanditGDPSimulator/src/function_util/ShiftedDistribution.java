package function_util;

import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;

public class ShiftedDistribution implements RealDistribution{
	private final RealDistribution distribution;
	private final double shift;
	
	public ShiftedDistribution(RealDistribution distribution, double shift) {
		this.distribution = distribution;
		this.shift = shift;
	}
	@Override
	public double cumulativeProbability(double arg0) {
		return distribution.cumulativeProbability(arg0-shift);
	}
	
	@Override
	@Deprecated
	public double cumulativeProbability(double arg0, double arg1) throws NumberIsTooLargeException {
		return distribution.cumulativeProbability(arg0-shift, arg1-shift);
	}
	@Override
	public double density(double arg0) {
		return distribution.density(arg0-shift);
	}
	@Override
	public double getNumericalMean() {
		return distribution.getNumericalMean()+shift;
	}
	@Override
	public double getNumericalVariance() {
		return distribution.getNumericalMean();
	}
	@Override
	public double getSupportLowerBound() {
		return distribution.getSupportLowerBound()+shift;
	}
	@Override
	public double getSupportUpperBound() {
		return distribution.getSupportUpperBound()+shift;
	}
	@Override
	public double inverseCumulativeProbability(double arg0) throws OutOfRangeException {
		return distribution.inverseCumulativeProbability(arg0-shift);
	}
	@Override
	public boolean isSupportConnected() {
		return distribution.isSupportConnected();
	}
	
	@Deprecated
	@Override
	public boolean isSupportLowerBoundInclusive() {
		return distribution.isSupportLowerBoundInclusive();
	}
	
	@Deprecated
	@Override
	public boolean isSupportUpperBoundInclusive() {
		return distribution.isSupportUpperBoundInclusive();
	}
	
	
	@Override
	public double probability(double arg0) {
		return distribution.probability(arg0-shift);
	}
	@Override
	public void reseedRandomGenerator(long arg0) {
		distribution.reseedRandomGenerator(arg0);
	}
	@Override
	public double sample() {
		return distribution.sample()+shift;
	}
	@Override
	public double[] sample(int arg0) {
		double[] samples = new double[arg0];
		for(int i=0; i < arg0;i++){
			samples[i] = distribution.sample()+shift;
		}
		return samples;
	}
}
