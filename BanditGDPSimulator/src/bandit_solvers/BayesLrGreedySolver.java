package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
@Deprecated
public class BayesLrGreedySolver extends BayesLrSolver {

	public BayesLrGreedySolver(int basisFunctionDimension, Function<SimpleTmiAction, RealVector> basisFunctions) {
		super(basisFunctionDimension, basisFunctions);
	}

	public BayesLrGreedySolver(int basisFunctionDimension, Function<SimpleTmiAction, RealVector> basisFunctions,
			RealMatrix priorCovMatrix) {
		super(basisFunctionDimension, basisFunctions, priorCovMatrix);
	}

	public BayesLrGreedySolver(int basisFunctionDimension, Function<SimpleTmiAction, RealVector> basisFunctions,
			RealVector activity) {
		super(basisFunctionDimension, basisFunctions, activity);
	}

	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector similarities, int remaining_time) {
		RealVector coefficients = calculateMean(similarities);
		Map<SimpleTmiAction, Double> my_indices = new HashMap<SimpleTmiAction, Double>();
		Iterator<SimpleTmiAction> action_iter = actionHistory.iterator();
		Iterator<RealVector> basis_iter = basisHistory.iterator();
		while (action_iter.hasNext()) {
			SimpleTmiAction next_action = action_iter.next();
			RealVector next_basis = basis_iter.next();
			if (!my_indices.containsKey(next_action)) {
				Double index = next_basis.dotProduct(coefficients);
				my_indices.put(next_action, index);
			}
		}
		return my_indices;
	}

}
