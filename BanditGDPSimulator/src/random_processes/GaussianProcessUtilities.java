package random_processes;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import function_util.IntTuple;
import function_util.MultiVarPolynomial;

public class GaussianProcessUtilities {
	public static DifferentiableGaussianProcess make_zero_process(int dimension, RealVector activity,double scale){
		GaussianKernel cov_func = new GaussianKernel(scale, activity);
		MultiVarPolynomial mean_func = new MultiVarPolynomial(dimension,new HashMap<IntTuple,Double>());
		Function<RealVector,RealVector> mean_der = mean_func.derivative();
		BiFunction<RealVector,RealVector,RealVector> cov_der = cov_func.leftDerivative();
		return new DifferentiableGaussianProcess(mean_func, cov_func, mean_der, cov_der);
	}
	
	public static DifferentiableGaussianProcess make_1d_quad_process(double a, double b, double c,double activity,double scale){
		RealVector activity_vec = new ArrayRealVector(1);
		activity_vec.setEntry(0, activity);
		GaussianKernel cov_func = new GaussianKernel(scale, activity_vec);
		HashMap<IntTuple,Double> coeffs = new HashMap<IntTuple,Double>();
		IntTuple constant = new IntTuple(1,new int[] {0});
		IntTuple linear_factor = new IntTuple(1,new int[] {1});
		IntTuple quad_factor = new IntTuple(1,new int[] {2});
		coeffs.put(constant,c);
		coeffs.put(linear_factor,b);
		coeffs.put(quad_factor,a);
		MultiVarPolynomial mean_func = new MultiVarPolynomial(1,coeffs);
		
		Function<RealVector,RealVector> mean_der = mean_func.derivative();
		BiFunction<RealVector,RealVector,RealVector> cov_der = cov_func.leftDerivative();
		return new DifferentiableGaussianProcess(mean_func, cov_func, mean_der, cov_der);
	}
	
	
	public static String matlab_1d_process(DifferentiableGaussianProcess my_process,
			double min,double max, int n, int m){
		String plot_points_string = "PLOT_PTS = [";
		String prior_string = "PRIOR = [";
		for(int i =0;i <= m;i++){
			RealVector my_eval_point = new ArrayRealVector(1);
			my_eval_point.setEntry(0, min+((double) i)*(max-min)/m);
			double next_value = my_process.prior_mean(my_eval_point);
			plot_points_string += my_eval_point.getEntry(0);
			prior_string += next_value;
			if(i < m){
				plot_points_string += ",";
				prior_string += ",";
			}else{
				plot_points_string += "];\n";
				prior_string += "];\n";
			}
		}
		//System.out.println(prior_string);
		
		String eval_points_string = "EVAL_PTS = [";
		String eval_vals_string = "EVAL_VALS = [";
		for(int i =0;i <= n;i++){
			RealVector my_eval_point = new ArrayRealVector(1);
			my_eval_point.setEntry(0, min+((double) i)*(max-min)/n);
			double next_value = my_process.evaluate(my_eval_point);
			eval_points_string += my_eval_point.getEntry(0);
			eval_vals_string += next_value;
			if(i < n){
				eval_points_string += ",";
				eval_vals_string += ",";
			}else{
				eval_points_string += "];\n";
				eval_vals_string += "];\n";
			}
			//System.out.print(next_value+",");
		}
		//System.out.println();
		
		String post_vals_string = "POST_VALS = [";
		Function<RealVector,Double> posterior_mean = my_process.postMean();
		for(int i =0; i <= m;i++){
			RealVector my_eval_point = new ArrayRealVector(1);
			my_eval_point.setEntry(0, min+((double) i)*(max-min)/m);
			double next_value = posterior_mean.apply(my_eval_point);
			post_vals_string += next_value;
			if(i < m){
				post_vals_string += ",";
			}else{
				post_vals_string += "];\n";
			}
		}
		String matlab_string = plot_points_string+post_vals_string+eval_points_string;
		matlab_string += eval_vals_string+prior_string;
		matlab_string += "plot(PLOT_PTS,PRIOR,'k-',PLOT_PTS,POST_VALS,'b-',EVAL_PTS,EVAL_VALS,'r+')\n";
		return matlab_string;
	}
	
	public static String matlab_plot_2d_process(GaussianProcess my_process,
			double min,double max, int n, int m){
		for(int i =0;i < n;i++){
			for(int j =0; j < n;j++){
				RealVector my_eval_point = new ArrayRealVector(2);
				my_eval_point.setEntry(0, min+((double) i)*(max-min)/(n-1));
				my_eval_point.setEntry(1, min+((double) j)*(max-min)/(n-1));
				my_process.evaluate(my_eval_point);
			}
			//System.out.print(next_value+",");
		}
		//System.out.println();
		
		double[] x = new double[m];
		double[] y = new double[m];
		double[][] z = new double[m][m];
		Function<RealVector,Double> posterior_mean = my_process.postMean();
		for(int i =0; i < m;i++){
			x[i] = min+((double) i)*(max-min)/(m-1);
		}
		for(int i =0; i < m;i++){
			y[i] = min+((double) i)*(max-min)/(m-1);
		}
		for(int i =0; i < m;i++){
			for(int j =0; j < m;j++){
				
				RealVector my_eval_point = new ArrayRealVector(2);
				my_eval_point.setEntry(0,x[i] );
				my_eval_point.setEntry(1, y[j]);
				double next_value = posterior_mean.apply(my_eval_point);
				z[i][j] = next_value;
			}

					
		}
		String matlab_command = "X=[";
		for(int i =0; i < m;i++){
			matlab_command+=x[i];
			if(i < m-1){
				matlab_command+=",";
			}else{
				matlab_command+="]\n";
			}
		}
		
		matlab_command += "Y=[";
		for(int i =0; i < m;i++){
			matlab_command+=x[i];
			if(i < m-1){
				matlab_command+=",";
			}else{
				matlab_command+="]\n";
			}
		}
		
		matlab_command += "Z=[";
		for(int i =0; i < m;i++){
			for(int j =0; j < m; j++){
				matlab_command+=z[i][j];
				if(j < m-1){
					matlab_command+=",";
				}
			}
			if(i < m-1){
				matlab_command+=";";
			}else{
				matlab_command+="]\n";
			}
		}
		
		matlab_command+= "contour(X,Y,Z)\n";
		matlab_command+= "figure()\n";
		matlab_command+= "surf(X,Y,Z)\n";
		return matlab_command;
	}
}
