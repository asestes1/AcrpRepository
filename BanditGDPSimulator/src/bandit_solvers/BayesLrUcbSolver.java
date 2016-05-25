package bandit_solvers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

@Deprecated
public class BayesLrUcbSolver extends BayesLrSolver{
	protected final double power;
	public BayesLrUcbSolver(double power, int basis_function_dimension, Function<SimpleTmiAction, RealVector> basis_function) {
		super(basis_function_dimension, basis_function);
		this.power = power;
	}

	public BayesLrUcbSolver(int basis_function_dimension,
			Function<SimpleTmiAction, RealVector> basis_functions,
			RealMatrix priorCovMatrix,
			double power) {
		super(basis_function_dimension,basis_functions,priorCovMatrix);
		this.power = power;
	}
	
	public BayesLrUcbSolver(int basis_function_dimension,
			Function<SimpleTmiAction, RealVector> basis_functions,
			RealVector activity,
			double power) {
		super(basis_function_dimension,basis_functions,activity);
		this.power = power;
	}
	@Override
	public Map<SimpleTmiAction, Double> getIndices(RealVector similarities, int remaining_time) {
		RealVector coefficients = calculateMean(similarities);
		RealMatrix covariance = calculateCov(similarities);
		Double beta = getBeta();
		int previous_time = actionHistory.size();
		int total_time = previous_time+remaining_time;
		double quantile = (1-1/((previous_time+1)*Math.pow(Math.log(total_time),power)));
		Iterator<SimpleTmiAction> past_actions = actionHistory.iterator();
		Iterator<RealVector> basis_iter = basisHistory.iterator();
		HashMap<SimpleTmiAction,Double> my_indices = new HashMap<SimpleTmiAction,Double>();
		int n = actionHistory.size();
		while(past_actions.hasNext()){
			SimpleTmiAction next_action = past_actions.next();
			RealVector basis_value = basis_iter.next();
			if(!my_indices.containsKey(next_action)){
				double mean = basis_value.dotProduct(coefficients);
				if(beta > 0.0){
					double scale = ((2.0*beta/(n))*
							covariance.operate(basis_value).dotProduct(basis_value));
					
					TDistribution my_distribution = new TDistribution(n);
					double ucb_index = mean+(Math.sqrt(scale))*my_distribution.inverseCumulativeProbability(quantile);
					my_indices.put(next_action, ucb_index);
				}else{
					System.out.println("WARNING: NUMERICAL INSTABILITIES");
					my_indices.put(next_action, mean);
					
				}
			}		
		}
		return my_indices;
	}

}
