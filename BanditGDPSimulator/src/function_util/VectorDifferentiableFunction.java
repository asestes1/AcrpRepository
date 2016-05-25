package function_util;

import java.util.function.Function;

import org.apache.commons.math3.linear.RealVector;

public interface VectorDifferentiableFunction extends Function<RealVector, Double> {
	public Function<RealVector,RealVector> derivative();
}
