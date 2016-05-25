/**
 * 
 */
package random_processes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import function_util.MatrixHelper;

/**
 * This class is an implementation of a Gaussian Process. This is a random function.
 * The value of [f(a_1),f(a_2),...,f(a_n)] has a multivariate distribution. 
 * @author Alex
 *
 */
public class SimilarityGaussianProcess {
	private int numPoints;
	private final Function<SimpleTmiAction,Double> meanFunction;
	private final BiFunction<SimpleTmiAction,SimpleTmiAction,Double> covarianceFunction; 
	/* The keys are points which have been evaluated. The value is the
	 * observed value of that point */ 
	private List<SimpleTmiAction> evaluatedPoints;
	private List<Double> values;

	//This stores the precision matrix for points which are evaluated.
	private RealMatrix precision;
	//This stores evaluated function value - prior value.
	private RealVector error;
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public SimilarityGaussianProcess(SimilarityGaussianProcess other){
		this.numPoints = other.numPoints;
		this.meanFunction = other.meanFunction;
		this.covarianceFunction = other.covarianceFunction;
		this.evaluatedPoints = new ArrayList<SimpleTmiAction>(other.evaluatedPoints);
		this.values = new ArrayList<Double>(other.values);
		this.precision = other.precision.copy();
		this.error = other.error.copy();
	}
	
	/**
	 * This is the standard constructor for a Gaussian process. None of the points
	 * will be evaluated yet.
	 * @param mean_function - The mean function of the Gaussian process. Could be
	 * any function, although continuous functions are usually used.
	 * @param correlation_function - This function should be symmetric.
	 */
	public SimilarityGaussianProcess(Function<SimpleTmiAction,Double> mean_function,
			BiFunction<SimpleTmiAction,SimpleTmiAction,Double> correlation_function){
		this.meanFunction = mean_function;
		this.covarianceFunction = correlation_function;
		//We start with no evaluated points
		evaluatedPoints = new ArrayList<SimpleTmiAction>();
		values = new ArrayList<Double>();
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
	public double evaluate(SimpleTmiAction vector,RealVector similarity){
		if(numPoints==0){
			double mean = meanFunction.apply(vector);
			double var = covarianceFunction.apply(vector,vector);
			double drawn_value = new NormalDistribution(mean,Math.sqrt(var)).sample();
			return drawn_value;
		}
		//These are the calculations for the posterior mean and variance,
		//given the points that were already evaluated.
		double prior_mean = meanFunction.apply(vector);
		double prior_var = covarianceFunction.apply(vector,vector);
		RealVector v_cov = findCov(vector,similarity);
		RealVector pre_cov = precision.preMultiply(v_cov);
		double post_mean = prior_mean+pre_cov.dotProduct(error);
		double post_var = prior_var - pre_cov.dotProduct(v_cov);
		if(post_var <= 0){
			//System.out.println("WARNING: NUMERICAL INSTABILITY.");
			return post_mean;
		}
		double drawn_value = new NormalDistribution(post_mean,Math.sqrt(post_var)).sample();
		return drawn_value;
	}
	
	/**
	 * Given a vector x, this generates the function value at f(x),
	 * and then updates the set of evaluated points.
	 * @param vector
	 * @return
	 */
	public double evaluateAndAdd(SimpleTmiAction vector,RealVector similarity){
		double value = evaluate(vector, similarity);
		addEvaluation(vector, similarity, value);
		return value;
	}
	
	/**
	 * This adds an evaluation point that we know the value of already.
	 * @param tmiAction - the evaluation point
	 * @param value - the value.
	 */
	public void addEvaluation(SimpleTmiAction tmiAction, RealVector similarityVector, double value){
		//If the point has not already been evaluated, add the point, and update
		//the prior precision and error for this evaluation point.
		updatePrecision(tmiAction,similarityVector);
		updateError(tmiAction,value);
		numPoints++;
		evaluatedPoints.add(tmiAction);
		values.add(value);
	}
	
	/**
	 * Updates the error vector by adding a new point and corresponding value
	 * 
	 * IMPORTANT: Update num_points after calling this function.
	 * @param vector - the evaluation point
	 * @param value - the corresponding value
	 */
	private void updateError(SimpleTmiAction vector, double value) {
		//Perform a deep copy of the first i entries for the previous error
		double[] newError = new double[numPoints+1];
		for(int i =0; i < numPoints;i++){
			newError[i] = error.getEntry(i);
		}
		//Add the new error
		newError[numPoints] = value-meanFunction.apply(vector);
		error = new ArrayRealVector(newError);
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
	private void updatePrecision(SimpleTmiAction vector, RealVector similarities){
		//Find the variance at the new point
		double var = covarianceFunction.apply(vector,vector);
		if(numPoints == 0){
			precision = new BlockRealMatrix(1,1);
			precision.setEntry(0, 0, 1/var);
		}else{
		
			//This stores the covariance between our new vector and each existing evaluation
			// point.
			RealVector v_cov = findCov(vector,similarities);
			RealVector pre_cov = precision.operate(v_cov);
			double scale_factor = 1/(var-v_cov.dotProduct(pre_cov));
			RealMatrix perturbation_matrix = MatrixHelper.makeRankOneMatrix(pre_cov, pre_cov);
			perturbation_matrix = 
					perturbation_matrix.scalarMultiply(scale_factor);
			RealMatrix upper_block = precision.add(perturbation_matrix);
			RealVector right_block = pre_cov.mapMultiply(-1*scale_factor);
			RealVector lower_block = upper_block.preMultiply(v_cov).mapMultiply(-1/var);
			
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
	private RealVector findCov(SimpleTmiAction vector, RealVector similarities) {
		Iterator<SimpleTmiAction> my_iter = evaluatedPoints.iterator();
		int count = 0;
		double[] cov_values = new double[numPoints];
		while(my_iter.hasNext()){
			cov_values[count] = covarianceFunction.apply(vector,my_iter.next())*
					similarities.getEntry(count);
			count++;
		}
		return new ArrayRealVector(cov_values);
	}
	
	public Double postMean(SimpleTmiAction vector, RealVector similarities){
		if(numPoints == 0){
			return meanFunction.apply(vector);
		}else{
			double prior_mean = meanFunction.apply(vector);
			RealVector v_cov = findCov(vector,similarities);
			RealVector pre_cov = precision.preMultiply(v_cov);
			return prior_mean+pre_cov.dotProduct(error);
		}
	}
	
	public Double postCov(SimpleTmiAction vector_1, SimpleTmiAction vector_2, RealVector similarities){
			RealVector v1_cov = findCov(vector_1,similarities);
			RealVector v2_cov = findCov(vector_2,similarities);
			double prior_cov = covarianceFunction.apply(vector_1,vector_2);
			return prior_cov -  v2_cov.dotProduct(precision.preMultiply(v1_cov));
	}

	public int getNumPoints() {
		return numPoints;
	}

	public List<SimpleTmiAction> getEvaluatedPoints() {
		return evaluatedPoints;
	}

	public List<Double> getValues() {
		return values;
	}

	public RealMatrix getPrecision() {
		return precision;
	}

	public RealVector getError() {
		return error;
	}

	public Function<SimpleTmiAction, Double> getMeanFunction() {
		return meanFunction;
	}

	public BiFunction<SimpleTmiAction, SimpleTmiAction, Double> getCovarianceFunction() {
		return covarianceFunction;
	}
	
	
}
