package bandit_solver_tests;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.BanditOutcome;
import bandit_simulator.Generator;
import bandit_simulator.SimilarityBanditInstance;
import bandit_simulator.SimilarityBanditRunner;
import bandit_simulator.UniformStateGenerator;
import bandit_solvers.ContextualZoomingSolver;
import bandit_solvers.UniRandomSolver;
import function_util.ConstantErrorBifunction;
import function_util.MeanErrorBifunction;
import function_util.QuadraticFormFunction;
import function_util.QuadraticFormFunctionFactory;
import random_processes.GaussianKernel;
import random_processes.GaussianTmiComparerFactory;

public class TestContextualZoomingSolver {
	private static final File testFile1 = new File("test/test_files/QuadForm1");

	public void GpRelevantGDPsTest() throws Exception{
		ContextualZoomingSolver myGreedySolver = new ContextualZoomingSolver(
				 GaussianTmiComparerFactory.makeDefaultTmiComparer(), 1.0, 10);

		GaussianKernel myKernel = new GaussianKernel(1.0, 1);
		List<RealVector> my_states = new ArrayList<RealVector>();
		int min = 500;
		int max = 1500;
		int numEvals = 0;
		for(int i =min; i <= max;i+=10){
			SimpleTmiAction my_tmi = new SimpleTmiAction(50,i , 600, 360);
			RealVector next_state = new ArrayRealVector(1);
			next_state.setEntry(0, (i-min)/(double)(max-min));
			RealVector similarity = new ArrayRealVector(numEvals);
			Iterator<RealVector> state_iter = my_states.iterator();
			for(int j =0; j < numEvals;j++){
				RealVector old_state = state_iter.next();
				similarity.setEntry(j,myKernel.apply(next_state, old_state));
			}
			my_states.add(next_state);
			double evaluationValue =-1*((double) (i-1000))/100*((double) (i-1000))/100; 
			myGreedySolver.addHistory(similarity, my_tmi,evaluationValue);
			numEvals++;
		}



		RealVector similarity = new ArrayRealVector(numEvals);
		Iterator<RealVector> stateIter = my_states.iterator();
		RealVector nextVec = new ArrayRealVector(1);
		nextVec.setEntry(0, 0.55);
		for(int j =0; j < numEvals;j++){
			RealVector oldState = stateIter.next();
			similarity.setEntry(j, myKernel.apply(oldState,nextVec));
		}
		SimpleTmiAction greedySuggestion = myGreedySolver.suggestAction(similarity, 10);
		System.out.println(greedySuggestion);
	}
	
	@Test
	public void testContextZooming() throws Exception{
		QuadraticFormFunction myMeanFunction = QuadraticFormFunctionFactory.parseQuadraticForm(testFile1);
		RealDistribution errorDistribution = new NormalDistribution(0.0, 10.0);
		ConstantErrorBifunction<RealVector, SimpleTmiAction> myErrorFunction = new ConstantErrorBifunction<RealVector,SimpleTmiAction>(
				errorDistribution);
		BiFunction<RealVector, SimpleTmiAction, Double> myRewardFunction = new MeanErrorBifunction<RealVector, SimpleTmiAction>(
				myMeanFunction, myErrorFunction);

		GaussianKernel mySimilarityFunction = new GaussianKernel(1.0, 2);

		Generator<RealVector> myGenerator = new UniformStateGenerator(2);
		SimilarityBanditInstance myInstance = SimilarityBanditRunner.makeInstance(myRewardFunction, myGenerator,
				mySimilarityFunction, 1000, 100, new UniRandomSolver());

		BiFunction<SimpleTmiAction, SimpleTmiAction, Double> tmiComparer = GaussianTmiComparerFactory.makeDefaultTmiComparer();

		ContextualZoomingSolver myZoomSolver = new ContextualZoomingSolver(tmiComparer, 1.5, 100);
		BanditOutcome myZoomOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myZoomSolver, 100);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myZoomOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));
	}
}
