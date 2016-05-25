package bandit_solver_tests;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_solvers.GreedyAverageSolver;
import random_processes.GaussianKernel;
import random_processes.GaussianTmiComparerFactory;

public class TestGreedyAverageSolver {
	@Test
	public void GpGreedyScopeTest(){
		//GaussianKernel cov_func = new GaussianKernel(1,1);
		GreedyAverageSolver myGreedySolver = new GreedyAverageSolver(
				GaussianTmiComparerFactory.makeDefaultTmiComparer());

		GaussianKernel myKernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		String evalX = "EVAL_X = [";
		String evalY = "EVAL_Y = [";
		int min = 500;
		int max = 1500;
		int numEvals = 0;
		for(int i =min; i <= max;i+=50){
			evalX += i+",";
			SimpleTmiAction my_tmi = new SimpleTmiAction(50,i , 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, 1);
			//next_state.setEntry(0, (i-min)/(double)(max-min));
			RealVector similarity = new ArrayRealVector(numEvals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < numEvals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,myKernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			double evaluationValue =-1*((double) (i-1000))/100*((double) (i-1000))/100; 
			evalY+=evaluationValue+",";
			myGreedySolver.addHistory(similarity, my_tmi,evaluationValue);
			numEvals++;
		}
		evalX += "];\n";
		evalY += "];\n";


		RealVector similarity = new ArrayRealVector(numEvals);
		Iterator<RealVector> stateIter = my_states.iterator();
		RealVector nextVec = new ArrayRealVector(1);
		nextVec.setEntry(0, 1);
		for(int j =0; j < numEvals;j++){
			RealVector oldState = stateIter.next();
			similarity.setEntry(j, myKernel.apply(oldState,nextVec));
		}
		
		Map<SimpleTmiAction, Double> greedyIndices = myGreedySolver.getIndices(similarity, 10);

		String plot_x = "PLOT_X = [";
		String plot_g = "PLOT_G = [";
		Iterator<SimpleTmiAction> myActionIter = greedyIndices.keySet().iterator();
		while(myActionIter.hasNext()){
			SimpleTmiAction nextGdp =  myActionIter.next();
			plot_x+=nextGdp.getRadius()+",";
			plot_g += greedyIndices.get(nextGdp)+",";

		}
		plot_x += "];\n";
		plot_g += "];\n";

		String matlab_string= evalX+evalY+plot_x+plot_g;
		matlab_string += "plot(PLOT_X,PLOT_G,'xm',EVAL_X,EVAL_Y,'+r')";
		SimpleTmiAction greedySuggestion = myGreedySolver.suggestAction(similarity, 10);
		System.out.println(matlab_string);
		System.out.println();
		System.out.println(greedySuggestion);
	}
}
