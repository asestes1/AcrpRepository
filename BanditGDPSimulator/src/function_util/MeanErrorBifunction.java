package function_util;

import java.util.function.BiFunction;

import org.apache.commons.math3.distribution.RealDistribution;

public class MeanErrorBifunction<R,S> implements RandomBiFunction<R, S>{
	private final BiFunction<R,S, Double> meanProcess;
	private final BiFunction<R,S, RealDistribution> errorProcess;

	public MeanErrorBifunction(BiFunction<R,S, Double> meanProcess, BiFunction<R,S, RealDistribution> errorProcess) {
		super();
		this.meanProcess = meanProcess;
		this.errorProcess = errorProcess;
	}

	@Override
	public Double mean(R r, S s) {
		return meanProcess.apply(r, s);
	}

	@Override
	public RealDistribution distribution(R r, S s) {
		return new ShiftedDistribution(errorProcess.apply(r,s), mean(r,s));
	}
}
