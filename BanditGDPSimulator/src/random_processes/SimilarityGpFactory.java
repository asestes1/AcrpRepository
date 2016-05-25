package random_processes;

import java.util.function.BiFunction;
import java.util.function.Function;

import bandit_objects.SimpleTmiAction;
import function_util.ConstantFunction;

public final class SimilarityGpFactory {
	private SimilarityGpFactory(){
		
	}
	
	public static final SimilarityGaussianProcess makeZeroPriorSimilarityGpProcess(){
		return makeConstantPriorSimilarityGpProcess(0.0);
	}
	
	public static final SimilarityGaussianProcess makeConstantPriorSimilarityGpProcess(Double constant){
		Function<SimpleTmiAction,Double> constant_prior = new ConstantFunction(constant);
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmi_action = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer();
		return new SimilarityGaussianProcess(constant_prior, tmi_action);
	}
}