package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

@Deprecated
public class BayesLrTsSolver extends BayesLrSolver{
	public BayesLrTsSolver(int basis_function_dimension, Function<SimpleTmiAction, RealVector> basis_function) {
		super(basis_function_dimension, basis_function);
	}

	public BayesLrTsSolver(int basis_function_dimension,
			Function<SimpleTmiAction, RealVector> basis_functions,
			RealMatrix priorCovMatrix) {
		super(basis_function_dimension,basis_functions,priorCovMatrix);
	}
	
	public BayesLrTsSolver(int basis_function_dimension,
			Function<SimpleTmiAction, RealVector> basis_functions,
			RealVector activity) {
		super(basis_function_dimension,basis_functions,activity);
	}
	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector similarities, int remaining_time) {
		RealVector coeffMean = calculateMean(similarities);
		RealMatrix covariance = calculateCov(similarities);
		Double beta = getBeta();
		Iterator<SimpleTmiAction> pastActions = actionHistory.iterator();
		Iterator<RealVector> basis_iter = basisHistory.iterator();
		HashMap<SimpleTmiAction,Double> myIndices = new HashMap<SimpleTmiAction,Double>();
		
		int n = actionHistory.size();
		GammaDistribution gammaDist = new GammaDistribution(n/2.0, 1/beta);
		Double inverseGammaSample = 1/gammaDist.sample();
		MultivariateNormalDistribution multiNormDist = 
				new MultivariateNormalDistribution(coeffMean.toArray(),
						covariance.scalarMultiply(inverseGammaSample).getData());
		RealVector coeffSample = new ArrayRealVector(multiNormDist.sample());
		while(pastActions.hasNext()){
			SimpleTmiAction nextAction = pastActions.next();
			RealVector basisValue = basis_iter.next();
			if(!myIndices.containsKey(nextAction)){
				double thompsonIndex = basisValue.dotProduct(coeffSample);
				myIndices.put(nextAction, thompsonIndex);
			}		
		}
		return myIndices;
	}
}
