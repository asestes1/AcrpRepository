package random_processes;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.linear.RealVector;

/**
 * This class represents a Gaussian Process with a differentiable mean function
 * and a differentiable covariance function. 
 * @author Alex
 * 
 */
public class DifferentiableGaussianProcess extends GaussianProcess {
	private Function<RealVector,RealVector> mean_derivative;
	private BiFunction<RealVector,RealVector,RealVector> kernel_derivative;
	
	/**
	 * 
	 * @param mean_function - this is the mean function
	 * @param covariance_function - this is the covariance function
	 * @param mean_derivative - this should give the derivative of the mean function
	 * @param kernel_derivative - this should be the derivative of the kernel
	 * covariance function, with respect to the left vector. That is,
	 * kernel_derivative.evaluate(u,v) should give the derivative of 
	 * covar(u,v) with respect to u.  
	 */
	public DifferentiableGaussianProcess(Function<RealVector, Double> mean_function,
			BiFunction<RealVector, RealVector, Double> covariance_function,
			Function<RealVector,RealVector> mean_derivative,
			BiFunction<RealVector,RealVector,RealVector> kernel_derivative) {
		super(mean_function, covariance_function);
		this.mean_derivative = mean_derivative;
		this.kernel_derivative = kernel_derivative;
	}
	
	/**
	 * This is the derivative of the posterior function at given point.
	 * @param vector
	 * @return
	 */
	public RealVector post_derivative(RealVector vector){
		if(numPoints > 0){
			RealVector mult_factors = precision.operate(error);
			RealVector derivative = mean_derivative.apply(vector);
			Iterator<RealVector> my_point_iter = evaluated_points.keySet().iterator();
			for(int i =0; i< numPoints;i++){
				derivative = derivative.add(kernel_derivative.apply(vector,my_point_iter.next())
						.mapMultiply(mult_factors.getEntry(i)));
			}
			return derivative;
		}else{
			return mean_derivative.apply(vector);
		}
	}
	
}
