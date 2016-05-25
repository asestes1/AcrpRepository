package bandit_solver_tests;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import function_util.ConstantFunction;
import function_util.IntTuple;
import function_util.MultiVarPolynomial;
import random_processes.DifferentiableGaussianProcess;
import random_processes.GaussianKernel;
import random_processes.GaussianProcess;
import random_processes.GaussianProcessUtilities;
import random_processes.GaussianTmiComparerFactory;
import random_processes.SimilarityGaussianProcess;

public class TestGaussianProcess {
	
	@Test
	public void GaussianProcessTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		
		DifferentiableGaussianProcess my_proc = GaussianProcessUtilities.make_1d_quad_process(1, 0, 0, 15, 1);
		//GaussianProcessUtilities.plot_1d_process(my_proc, -1, 1, 21, 41);
		System.out.println(GaussianProcessUtilities.matlab_1d_process(my_proc, -1, 1, 10, 200));
		
		RealVector activity = new ArrayRealVector(2);
		activity.setEntry(0, 2);
		activity.setEntry(1, 4);

		DifferentiableGaussianProcess my_proc_2 = GaussianProcessUtilities.make_zero_process(2, activity, 0.01);
		String command = GaussianProcessUtilities.matlab_plot_2d_process(my_proc_2, -5, 5, 11, 11);
		System.out.println(command);

		Map<IntTuple,Double> coeffs = new HashMap<IntTuple,Double>();
		IntTuple x_tuple = new IntTuple(2, new int[] {2,0});
		IntTuple y_tuple = new IntTuple(2, new int[] {0,2});
		coeffs.put(x_tuple, 1.0);
		coeffs.put(y_tuple, 1.0);
		MultiVarPolynomial mean_func = new MultiVarPolynomial(2, coeffs);
		activity = new ArrayRealVector(2);
		activity.setEntry(0, 1);
		activity.setEntry(1, 25);
		GaussianKernel cov_func = new GaussianKernel(2, activity);

		GaussianProcess my_process = new GaussianProcess(mean_func, cov_func);
		command = GaussianProcessUtilities.matlab_plot_2d_process(my_process, -5, 5, 11, 11);
		System.out.println(command);
	}
	
	@Test
	public void SimilarityGaussianProcessStateTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		
		Function<SimpleTmiAction,Double> zero_prior = new ConstantFunction();
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmi_action = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer();
		SimilarityGaussianProcess my_sim_proc = new SimilarityGaussianProcess(zero_prior,tmi_action);

		int num_evaluations = 100; 
		GaussianKernel my_kernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>(num_evaluations);
		String eval_x = "EVAL_X = [";
		String eval_y = "EVAL_Y = [";
		double min = 0;
		double max = 10;
		for(int i =0; i <= num_evaluations;i++){
			Double next = min+ (max-min)*(i/(double) num_evaluations);
			eval_x += next+",";
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, next);
			RealVector similarity = new ArrayRealVector(i);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < i;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			eval_y+=my_sim_proc.evaluateAndAdd(new SimpleTmiAction(), similarity)+",";
		}
		eval_x += "];\n";
		eval_y += "];\n";

		
		int num_plot_pts = 1000;
		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		for(int i=0; i<= num_plot_pts;i++){
			Double next = min+(max-min)*(i/(double) num_plot_pts);
			plot_x+=next+",";
			RealVector next_vec = new ArrayRealVector(1);
			next_vec.setEntry(0, next);
			RealVector similarity = new ArrayRealVector(num_evaluations+1);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j <= num_evaluations;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j, my_kernel.apply(old_state,next_vec));
			}
			plot_y += my_sim_proc.postMean(new SimpleTmiAction(), similarity)+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'-k',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);

	}
	
	@Test
	public void SimilarityGaussianProcessRateTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		
		Function<SimpleTmiAction,Double> zero_prior = new ConstantFunction();
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmi_action = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer();
		SimilarityGaussianProcess my_sim_proc = new SimilarityGaussianProcess(zero_prior,tmi_action);

		GaussianKernel my_kernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		String eval_x = "EVAL_X = [";
		String eval_y = "EVAL_Y = [";
		int min = 0;
		int max = 120;
		int num_evals = 0;
		for(int i =min; i <= max;i+=6){
			eval_x += i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(i, 700, 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, 0.0);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			eval_y+=my_sim_proc.evaluateAndAdd(my_tmi, similarity)+",";
			num_evals++;

		}
		eval_x += "];\n";
		eval_y += "];\n";

		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		for(int i=min; i<= max;i++){
			plot_x+=i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(i, 700, 600, 360);
			RealVector next_vec = new ArrayRealVector(1);
			next_vec.setEntry(0, 0.0);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j, my_kernel.apply(old_state,next_vec));
			}
			plot_y += my_sim_proc.postMean(my_tmi, similarity)+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'-k',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);
	}

	@Test
	public void SimilarityGaussianProcessScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		
		Function<SimpleTmiAction,Double> zero_prior = new ConstantFunction();
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmi_action = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer();
		SimilarityGaussianProcess my_sim_proc = new SimilarityGaussianProcess(zero_prior,tmi_action);

		GaussianKernel my_kernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		String eval_x = "EVAL_X = [";
		String eval_y = "EVAL_Y = [";
		int min = 500;
		int max = 1000;
		int num_evals = 0;
		for(int i =min; i <= max;i+=25){
			eval_x += i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50, i, 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, 0.0);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			eval_y+=my_sim_proc.evaluateAndAdd(my_tmi, similarity)+",";
			num_evals++;

		}
		eval_x += "];\n";
		eval_y += "];\n";

		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		for(int i=min; i<= max;i++){
			plot_x+=i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50, i, 600, 360);
			RealVector next_vec = new ArrayRealVector(1);
			next_vec.setEntry(0, 0.0);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j, my_kernel.apply(old_state,next_vec));
			}
			plot_y += my_sim_proc.postMean(my_tmi, similarity)+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'-k',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);
	}
	
	@Test
	public void GaussianUcbScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		Function<SimpleTmiAction,Double> zero_prior = new ConstantFunction();
		BiFunction<SimpleTmiAction,SimpleTmiAction,Double> tmi_action = 
				GaussianTmiComparerFactory.makeDefaultTmiComparer();
		SimilarityGaussianProcess my_sim_proc = new SimilarityGaussianProcess(zero_prior,tmi_action);

		GaussianKernel my_kernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		String eval_x = "EVAL_X = [";
		String eval_y = "EVAL_Y = [";
		int min = 500;
		int max = 1500;
		int num_evals = 0;
		for(int i =min; i <= max;i+=100){
			eval_x += i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50,i , 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, 0.0);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			eval_y+=(i/100)*(i/100)+",";
			my_sim_proc.addEvaluation( my_tmi,similarity, (double) (i/100)*(i/100)); 
			num_evals++;
		}
		eval_x += "];\n";
		eval_y += "];\n";


		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		for(int i=min; i<= max;i+=25){
			plot_x+=i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50, i, 600, 360);
			RealVector next_vec = new ArrayRealVector(1);
			next_vec.setEntry(0, 0.0);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j, my_kernel.apply(old_state,next_vec));
			}
			Double coefficients = my_sim_proc.postMean(my_tmi, similarity);
			plot_y += coefficients+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'-k',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);
	}
}
