package bandit_solver_tests;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_solvers.GpGreedySolver;
import bandit_solvers.GpIndexSolver;
import bandit_solvers.GpSolverFactory;
import random_processes.GaussianKernel;

public class TestGaussianSolver {
	@Test
	public void GpGreedyScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		GpGreedySolver myGreedySolver = GpSolverFactory.makeGpGreedySolver(1.0);
		GpIndexSolver myUcbSolver = GpSolverFactory.makeGpUcbSolver(1.0);
		GpIndexSolver myTsSolver = GpSolverFactory.makeGpTsSolver(1.0);

		GaussianKernel my_kernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		String eval_x = "EVAL_X = [";
		String eval_y = "EVAL_Y = [";
		int min = 500;
		int max = 1500;
		int num_evals = 0;
		for(int i =min; i <= max;i+=50){
			eval_x += i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50,i , 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, (i-min)/(double)(max-min));
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			double evaluationValue =-1*((double) (i-1000))/100*((double) (i-1000))/100; 
			eval_y+=evaluationValue+",";
			myGreedySolver.addHistory(similarity, my_tmi,evaluationValue);
			myUcbSolver.addHistory(similarity, my_tmi,evaluationValue);
			myTsSolver.addHistory(similarity, my_tmi,evaluationValue);
			num_evals++;
		}
		eval_x += "];\n";
		eval_y += "];\n";


		RealVector similarity = new ArrayRealVector(num_evals);
		Iterator<RealVector> state_iter = my_states.iterator();
		RealVector next_vec = new ArrayRealVector(1);
		next_vec.setEntry(0, 0.55);
		for(int j =0; j < num_evals;j++){
			RealVector old_state = state_iter.next();
			similarity.setEntry(j, my_kernel.apply(old_state,next_vec));
		}
		
		Map<SimpleTmiAction, Double> greedyIndices = myGreedySolver.getIndices(similarity, 10);
		Map<SimpleTmiAction, Double> ucbIndices = myUcbSolver.getIndices(similarity, 10);
		Map<SimpleTmiAction, Double> tsIndices = myTsSolver.getIndices(similarity, 10);
		String plot_x = "PLOT_X = [";
		String plot_g = "PLOT_G = [";
		String plot_u = "PLOT_U = [";
		String plot_t = "PLOT_T = [";
		Iterator<SimpleTmiAction> myActionIter = greedyIndices.keySet().iterator();
		while(myActionIter.hasNext()){
			SimpleTmiAction nextGdp = myActionIter.next();
			plot_x+=nextGdp.getRadius()+",";
			plot_g += greedyIndices.get(nextGdp)+",";
			plot_u += ucbIndices.get(nextGdp)+",";
			plot_t += tsIndices.get(nextGdp)+",";
		}
		plot_x += "];\n";
		plot_g += "];\n";
		plot_u += "];\n";
		plot_t += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_g+plot_u+plot_t;
		matlab_string += "plot(PLOT_X,PLOT_G,'xm',"
				+ "PLOT_X,PLOT_U,'xc',PLOT_X,PLOT_T,'xg',EVAL_X,EVAL_Y,'+r')";
		SimpleTmiAction greedySuggestion = myGreedySolver.suggestAction(similarity, 10);
		SimpleTmiAction ucbSuggestion = myUcbSolver.suggestAction(similarity, 10);
		SimpleTmiAction tsSuggestion = myTsSolver.suggestAction(similarity, 10);
		System.out.println(matlab_string);
		System.out.println();
		System.out.println(greedySuggestion);
		System.out.println(ucbSuggestion);
		System.out.println(tsSuggestion);
	}
	
}
