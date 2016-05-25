package bandit_solvers;

import random_processes.SimilarityGpFactory;

public final class GpSolverFactory {
	private GpSolverFactory(){
		
	}
	
	public static final GpGreedySolver makeGpGreedySolver(){
		return new GpGreedySolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess());
	}
	
	public static final GpUcbSolver makeGpUcbSolver(){
		return new GpUcbSolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),0.0);
	}
	
	public static final GpTsSolver makeGpTsSolver(){
		return new GpTsSolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess());
	}
	
	public static final GpUcbSolver makeGpUcbSolver(double power){
		return new GpUcbSolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),power);
	}
}
