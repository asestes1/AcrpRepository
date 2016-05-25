/**
 * 
 */
package random_processes;

import java.util.Iterator;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import function_util.MatrixHelper;

/**
 * This class is an implementation of a Gaussian Process. This is a random function.
 * The value of [f(a_1),f(a_2),...,f(a_n)] has a multivariate distribution. 
 * @author Alex
 *
 */
public class GaussianProcess {
	 int numPoints;
	private final Function<RealVector,Double> meanFunction;
	private final BiFunction<RealVector,RealVector,Double> covarianceFunction; 
	/* The keys are points which have been evaluated. The value is the
	 * observed value of that point */ 
	protected LinkedHashMap<RealVector,Double> evaluated_points;
	/*This is a map, where the keys are points which have been evaluated. The value
	* is the number of times it has been evaluated */
	private LinkedHashMap<RealVector,Integer> numEvals;
	//This stores the precision matrix for points which are evaluated.
	protected RealMatrix precision;
	//This stores evaluated function value - prior value.
	protected RealVector error;
	
	public GaussianProcess(GaussianProcess other){
		this.numPoints = other.numPoints;
		this.meanFunction = other.meanFunction;
		this.covarianceFunction = other.covarianceFunction;
		this.evaluated_points = new LinkedHashMap<RealVector,Double>(evaluated_points);
		this.numEvals = new LinkedHashMap<RealVector,Integer>(numEvals);
		this.precision = new BlockRealMatrix(numPoints,numPoints);
		for(int i =0;i< numPoints;i++){
			for(int j =0;j< numPoints;j++){
				this.precision.setEntry(i, j,other.precision.getEntry(i, j));
			}
		}
		this.error = new ArrayRealVector(error);
	}
	/**
	 * This is the standard constructor for a Gaussian process. None of the points
	 * will be evaluated yet.
	 * @param mean_function - The mean function of the Gaussian process. Could be
	 * any function, although continuous functions are usually used.
	 * @param correlation_function - This function should be symmetric.
	 */
	public GaussianProcess(Function<RealVector,Double> mean_function,
			BiFunction<RealVector,RealVector,Double> correlation_function){
		this.meanFunction = mean_function;
		this.covarianceFunction = correlation_function;
		//We start with no evaluated points
		evaluated_points = new LinkedHashMap<RealVector,Double>();
		numEvals = new LinkedHashMap<RealVector,Integer>();
		numPoints = 0;
		//The precision matrix and error of evaluated points is initialized to null.
		precision = null;
		error = null;
	}
	
	/**
	 * Given a vector x, this generates the function value at f(x),
	 * and then updates the set of evaluated points.
	 * @param vector
	 * @return
	 */
	public double evaluate(RealVector vector){
		//If the point has already been evaluated, just return the value it takes.
		if(evaluated_points.containsKey(vector)){
			return evaluated_points.get(vector);
		}
		if(numPoints==0){
			double mean = meanFunction.apply(vector);
			double var = covarianceFunction.apply(vector,vector);
			double drawn_value = new NormalDistribution(mean,Math.sqrt(var)).sample();
			add_evaluation(vector,drawn_value);
			return drawn_value;
		}
		//These are the calculations for the posterior mean and variance,
		//given the points that were already evaluated.
		double prior_mean = meanFunction.apply(vector);
		double prior_var = covarianceFunction.apply(vector,vector);
		RealVector v_cov = findCov(vector);
		RealVector pre_cov = precision.preMultiply(v_cov);
		double post_mean = prior_mean+pre_cov.dotProduct(error);
		double post_var = prior_var - pre_cov.dotProduct(v_cov);
		double drawn_value = new NormalDistribution(post_mean,Math.sqrt(post_var)).sample();
		//We add this point to the set of observed points.
		add_evaluation(vector, drawn_value);
		return drawn_value;
	}
	
	/**
	 * This adds an evaluation point that we know the value of already.
	 * @param vector - the evaluation point
	 * @param value - the value.
	 */
	public void add_evaluation(RealVector vector, double value){
		//If this point already has been evaluated, we average the value
		//with the existing valuations
		if(evaluated_points.containsKey(vector)){
			int n_times_evaluated = numEvals.get(vector);
			numEvals.put(vector, n_times_evaluated+1);
			double new_value = ((double) n_times_evaluated)/(n_times_evaluated+1)*
								evaluated_points.get(vector) +
								value/((double)n_times_evaluated);
			evaluated_points.put(vector, new_value);
		//If the point has not already been evaluated, add the point, and update
		//the prior precision and error for this evaluation point.
		}else{
			updatePrecision(vector);
			updateError(vector,value);
			numPoints++;
			evaluated_points.put(vector, value);

		}
	}
	
	/**
	 * Updates the error vector by adding a new point and corresponding value
	 * 
	 * IMPORTANT: Update num_points after calling this function.
	 * @param vector - the evaluation point
	 * @param value - the corresponding value
	 */
	private void updateError(RealVector vector, double value) {
		//Perform a deep copy of the first i entries fo the previous error
		double[] new_error = new double[numPoints+1];
		for(int i =0; i < numPoints;i++){
			new_error[i] = error.getEntry(i);
		}
		//Add the new error
		new_error[numPoints] = value-meanFunction.apply(vector);
		error = new ArrayRealVector(new_error);
	}

	/**
	 * This matrix finds the new precision matrix for those
	 * points which have been evaluated. It uses a block-matrix
	 * inverse formula to make this efficient.
	 * 
	 * IMPORTANT NOTE: When adding an evaluation point ALWAYS 
	 * UPDATE num_points AFTER you call this function!!!!!!
	 * @param vector - the new evaluation point
	 */
	private void updatePrecision(RealVector vector){
		//Find the variance at the new point
		double var = covarianceFunction.apply(vector,vector);
		if(numPoints == 0){
			precision = new BlockRealMatrix(1,1);
			precision.setEntry(0, 0, 1/var);
		}else{
		
			//This stores the covariance between our new vector and each existing evaluation
			// point.
			RealVector v_cov = findCov(vector);
			RealVector pre_cov = precision.operate(v_cov);
			double scale_factor = 1/(var-v_cov.dotProduct(pre_cov));
			RealMatrix perturbation_matrix = MatrixHelper.makeRankOneMatrix(pre_cov, pre_cov);
			perturbation_matrix = 
					perturbation_matrix.scalarMultiply(scale_factor);
			RealMatrix upper_block = precision.add(perturbation_matrix);
			RealVector right_block = pre_cov.mapMultiply(-1*scale_factor);
			RealVector lower_block = upper_block.preMultiply(v_cov).mapMultiply(-1.0/var);
			
			//Copy all the elements to the correct entry.
			precision = new BlockRealMatrix(numPoints+1,numPoints+1);
			for(int i =0; i < numPoints;i++){
				for(int j =0;j < numPoints;j++){
					precision.setEntry(i, j, upper_block.getEntry(i, j));
				}
			}
			for(int i =0; i < numPoints;i++){
					precision.setEntry(i, numPoints, right_block.getEntry(i));
			}
			for(int j =0; j < numPoints;j++){
				precision.setEntry(numPoints,j, lower_block.getEntry(j));
			}
			precision.setEntry(numPoints, numPoints, scale_factor);
		}
	}

	/**
	 * This finds the vector of prior covariances between a given point
	 * and the previously observed points.
	 * @param vector
	 * @return
	 */
	private RealVector findCov(RealVector vector) {
		Iterator<RealVector> my_iter = evaluated_points.keySet().iterator();
		int count = 0;
		double[] cov_values = new double[numPoints];
		while(my_iter.hasNext()){
			cov_values[count] = covarianceFunction.apply(vector,my_iter.next());
			count++;
		}
		return new ArrayRealVector(cov_values);
	}
	
	public Function<RealVector,Double> postMean(){
		return new posteriorMean();
	}
	private class posteriorMean implements Function<RealVector,Double>{

		@Override
		public Double apply(RealVector vector) {
			if(numPoints == 0){
				return meanFunction.apply(vector);
			}else if(evaluated_points.containsKey(vector)){
				return evaluated_points.get(vector);
			}else{
				double prior_mean = meanFunction.apply(vector);
				RealVector v_cov = findCov(vector);
				RealVector pre_cov = precision.preMultiply(v_cov);
				return prior_mean+pre_cov.dotProduct(error);
			}
		}
		
	}
	
	public Double prior_mean(RealVector vector){
		return meanFunction.apply(vector);
	}
}
