package bandit_solver_tests;
import java.security.InvalidAlgorithmParameterException;
import java.util.function.BiFunction;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.SimilarityBanditInstance;
import bandit_simulator.SimilarityBanditRunner;
import bandit_simulator.UniformStateGenerator;
import bandit_solvers.UniRandomSolver;
import function_util.MatrixHelper;
import function_util.QuadraticFormFunction;
import random_processes.GaussianKernel;

public class TestStateGeneration {
	
	@Test
	public void testUniRandomBandit() throws InvalidAlgorithmParameterException{
		UniRandomSolver my_random_solver = new UniRandomSolver();
		for(int i=0; i < 100;i++){
			SimpleTmiAction my_action = my_random_solver.suggestAction(null, 0);
			System.out.println(my_action);
		}
	}
	
	@Test 
	public void testUniformStateGeneratr(){
		UniformStateGenerator myStateGenerator = new UniformStateGenerator(10);
		for(int i=0; i < 100;i++){
			RealVector state = myStateGenerator.generate();
			System.out.println(state);
		}
	}
	
	@Test
	public void testHistoryPregeneration(){
		int state_dimension = 2;

		UniformStateGenerator myStateGenerator = 
				new UniformStateGenerator(state_dimension);
		
		SimilarityBanditInstance myInstance = 
				SimilarityBanditRunner.makeInstance(myStateGenerator, 50);
		System.out.println(myInstance);
	}
	
	@Test
	public void testActionGeneration() throws Exception{
		int state_dimension = 5;
		RealMatrix myTmiMatrix = MatrixHelper.makeIdentityMatrix(state_dimension+SimpleTmiAction.GDP_DIMENSION);
		RealMatrix myNoTmiMatrix = MatrixHelper.makeIdentityMatrix(state_dimension);
		
		RealVector myTmiVector = new ArrayRealVector(new double[]{4,9,1,2,3,5,7,11,13});
		RealVector myNoTmiVector = myTmiVector.getSubVector(0, state_dimension);
		
		Double myConstant = 20.0;

		BiFunction<RealVector,SimpleTmiAction,Double> myFunction = 
				new QuadraticFormFunction(myTmiMatrix,myTmiVector,myConstant,myNoTmiMatrix,myNoTmiVector,myConstant);
		UniformStateGenerator myStateGenerator = 
				new UniformStateGenerator(state_dimension);
		
		GaussianKernel myKernel = new GaussianKernel(1.0, 5);
		SimilarityBanditInstance myInstance = 
				SimilarityBanditRunner.makeInstance(myFunction, myStateGenerator,
						myKernel, 10, 20, new UniRandomSolver());
		System.out.println(myInstance);
	}
}
