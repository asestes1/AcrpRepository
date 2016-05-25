package function_util;

import java.util.function.Function;

import org.apache.commons.math3.distribution.RealDistribution;

public class MeanErrorFunction<R> implements RandomFunction<R> {
	private final Function<R, Double> meanProcess;
	private final Function<R, RealDistribution> errorProcess;

	public MeanErrorFunction(Function<R, Double> meanProcess, Function<R, RealDistribution> errorProcess) {
		super();
		this.meanProcess = meanProcess;
		this.errorProcess = errorProcess;
	}

	@Override
	public Double mean(R t) {
		return meanProcess.apply(t);
	}

	@Override
	public RealDistribution distribution(R t) {
		return new ShiftedDistribution(errorProcess.apply(t), mean(t));
	}

}
