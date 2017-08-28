package bandit_solvers;

import random_processes.SimilarityGpFactory;

public final class GpSolverFactory {
	private GpSolverFactory(){
		
	}
	
	public static final GpGreedySolver makeGpGreedySolver(Double bandwidth){
		return new GpGreedySolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),bandwidth);
	}
	
	public static final GpUcbSolver makeGpUcbSolver(Double bandwidth){
		return new GpUcbSolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),0.0,bandwidth);
	}
	
	public static final GpTsSolver makeGpTsSolver(Double bandwidth){
		return new GpTsSolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),bandwidth);
	}
	
	public static final GpUcbSolver makeGpUcbSolver(double power,Double bandwidth){
		return new GpUcbSolver(
				SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),power,bandwidth);
	}
}
