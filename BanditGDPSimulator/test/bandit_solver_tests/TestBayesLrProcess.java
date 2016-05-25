package bandit_solver_tests;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_solvers.BayesLrFactory;
import bandit_solvers.BayesLrGreedySolver;
import bandit_solvers.BayesLrTsSolver;
import bandit_solvers.BayesLrUcbSolver;
import random_processes.GaussianKernel;

@Deprecated
public class TestBayesLrProcess {
	@Test
	public void BayesLrDegreeOneScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		BayesLrGreedySolver my_solver = BayesLrFactory.MakeOneDegreeGreedyBayesLr();

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
			next_state.setEntry(0, (i-min)/(double)(max-min));
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			eval_y+=-1*(i/100)*(i/100)+",";
			my_solver.addHistory(similarity, my_tmi, (double) -1*(i/100)*(i/100)); 
			num_evals++;
		}
		eval_x += "];\n";
		eval_y += "];\n";

		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		for(int i=min; i<= max;i+=50){
			plot_x+=i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50, i, 600, 360);
			RealVector next_vec = new ArrayRealVector(1);
			next_vec.setEntry(0, 0.55);
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j, my_kernel.apply(old_state,next_vec));
			}
			RealVector coefficients = my_solver.calculateMean(similarity);
			plot_y += coefficients.getEntry(0)+coefficients.getSubVector(1, 4).dotProduct(my_tmi.asVector())+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'xk',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);
	}
	
	@Test
	public void BayesLrDegreeOneUcbScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		BayesLrUcbSolver my_solver = BayesLrFactory.MakeOneDegreeUcbBayesLr();

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
			eval_y+=((double) i)/100*((double) i)/100+",";
			my_solver.addHistory(similarity, my_tmi, ((double) i)/100*((double) i)/100); 
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
		
		Map<SimpleTmiAction, Double> indices = my_solver.getIndices(similarity, 10);
		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		Iterator<SimpleTmiAction> myActionIter = indices.keySet().iterator();
		while(myActionIter.hasNext()){
			SimpleTmiAction nextGdp = (SimpleTmiAction) myActionIter.next();
			plot_x+=nextGdp.getRadius()+",";
			plot_y += indices.get(nextGdp)+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'xk',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);
	}
	
	@Test
	public void BayesLrDegreeOneTsScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		BayesLrTsSolver my_solver = BayesLrFactory.MakeOneDegreeTsBayesLr();

		GaussianKernel my_kernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		String eval_x = "EVAL_X = [";
		String eval_y = "EVAL_Y = [";
		int min = 500;
		int max = 1500;
		int num_evals = 0;
		for(int i =min; i <= max;i+=50){
			eval_x += i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(0,i , 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, (i-min)/(double)(max-min));
			RealVector similarity = new ArrayRealVector(num_evals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < num_evals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,my_kernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			eval_y+=((double) i)/100*((double) i)/100+",";
			my_solver.addHistory(similarity, my_tmi, ((double) i)/100*((double) i)/100); 
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
		
		Map<SimpleTmiAction, Double> indices = my_solver.getIndices(similarity, 10);
		String plot_x = "PLOT_X = [";
		String plot_y = "PLOT_Y = [";
		Iterator<SimpleTmiAction> myActionIter = indices.keySet().iterator();
		while(myActionIter.hasNext()){
			SimpleTmiAction nextGs = myActionIter.next();
			plot_x+=nextGs.getRadius()+",";
			plot_y += indices.get(nextGs)+",";
		}
		plot_x += "];\n";
		plot_y += "];\n";
		String matlab_string= eval_x+eval_y+plot_x+plot_y;
		matlab_string += "plot(PLOT_X,PLOT_Y,'xk',EVAL_X,EVAL_Y,'+r')";
		System.out.println(matlab_string);
	}
}
