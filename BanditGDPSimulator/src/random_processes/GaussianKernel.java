package random_processes;

import java.security.InvalidParameterException;
import java.util.function.BiFunction;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


/**
 * This implements a gaussian metric function, which gives a similarity 
 * value between points.
 * @author Alex
 *
 */
public class GaussianKernel implements BiFunction<RealVector, RealVector, Double> {
	private final RealMatrix kernel_matrix;
	private final double scale_factor;
	private final int dimension;

	/**
	 * Default constructor, uses Euclidean distance.
	 * @param d
	 * @param dimension
	 */
	public GaussianKernel(double d, int dimension) {
		this.scale_factor = d;
		this.dimension = dimension;
		kernel_matrix = new BlockRealMatrix(dimension, dimension);
		for(int i = 0; i < dimension;i++){
			for(int j =0;j < dimension;j++){
				if(i==j){
					kernel_matrix.setEntry(i,j, 1);
				}else{
					kernel_matrix.setEntry(i,j, 0);
				}
			}
		}
	}
	
	public GaussianKernel(double scale_factor, RealVector activity) {
		this.scale_factor = scale_factor;
		dimension = activity.getDimension();
		kernel_matrix = new BlockRealMatrix(dimension, dimension);
		for(int i = 0; i < dimension;i++){
			if(activity.getEntry(i) >= 0){
				kernel_matrix.setEntry(i,i, activity.getEntry(i));
			}else{
				throw new InvalidParameterException("Activity must be non-negative");
			}
		}
	}
	
	@Override
	public Double apply(RealVector arg0, RealVector arg1) {
		if(dimension == 0){
			return 1.0;
		}else{
			RealVector difference = arg0.subtract(arg1);
			double exponent = -1*kernel_matrix.operate(difference).dotProduct(difference);
			return scale_factor*Math.exp(exponent);
		}
	}
	
	public BiFunction<RealVector,RealVector, RealVector> leftDerivative(){
		return new LeftGaussianDerivative();
	}
	
	private class LeftGaussianDerivative implements BiFunction<RealVector,RealVector,RealVector>{
		
		public LeftGaussianDerivative(){
		}
		
		@Override
		public RealVector apply(RealVector t0, RealVector t1) {
			RealVector difference = t0.subtract(t1);
			RealVector vector = kernel_matrix.operate(difference);
			double exponent = -1*vector.dotProduct(difference);
			double coefficient = -2.0*scale_factor*Math.exp(exponent);
			return vector.mapMultiply(coefficient);
		}
		
	}

}
