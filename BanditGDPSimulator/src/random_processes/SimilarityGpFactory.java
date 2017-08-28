package random_processes;

import java.util.function.BiFunction;
import java.util.function.Function;

import bandit_objects.SimpleTmiAction;
import function_util.ConstantFunction;

public final class SimilarityGpFactory {
	private SimilarityGpFactory(){
		
	}
	
	public static final SimilarityGaussianProcess makeZeroPriorSimilarityGpProcess(){
		return makeConstantPriorSimilarityGpProcess(0.0,1.0);
	}
	
	public static final SimilarityGaussianProcess makeZeroPriorSimilarityGpProcess(Double scaleFactor){
		return makeConstantPriorSimilarityGpProcess(0.0,scaleFactor);
	}
	
	public static final SimilarityGaussianProcess makeConstantPriorSimilarityGpProcess(Double constant,Double scaleFactor){
		Function<SimpleTmiAction,Double> constant_prior = new ConstantFunction(constant);
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmi_action = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer();
		return new SimilarityGaussianProcess(constant_prior, tmi_action,scaleFactor);
	}
	
	public static final SimilarityGaussianProcess makeConstantPriorSimilarityGpProcess(Double constant,Double scaleFactor,
			Double tmiBandwidth){
		Function<SimpleTmiAction,Double> constantPrior = new ConstantFunction(constant);
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmiAction = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer(tmiBandwidth);
		return new SimilarityGaussianProcess(constantPrior, tmiAction,scaleFactor);
	}
}
