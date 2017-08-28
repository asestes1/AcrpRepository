package bandit_solver_tests;

import java.io.File;
import java.util.function.BiFunction;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.BanditOutcome;
import bandit_simulator.Generator;
import bandit_simulator.SimilarityBanditInstance;
import bandit_simulator.SimilarityBanditRunner;
import bandit_simulator.UniformStateGenerator;
import bandit_solvers.ContextualZoomingSolver;
import bandit_solvers.GpGreedySolver;
import bandit_solvers.GpTsSolver;
import bandit_solvers.GpUcbSolver;
import bandit_solvers.GreedyAverageSolver;
import bandit_solvers.RandomHistoryBanditSolver;
import bandit_solvers.UniRandomSolver;
import function_util.ConstantErrorBifunction;
import function_util.MeanErrorBifunction;
import function_util.QuadraticFormFunction;
import function_util.QuadraticFormFunctionFactory;
import function_util.ReflectedBiFunction;
import random_processes.GaussianKernel;
import random_processes.GaussianTmiComparerFactory;
import random_processes.SimilarityGpFactory;

public class RunSolverTests {
	private static final File testFile1 = new File("test/test_files/QuadForm1");

	@Test
	public void testSolver1() throws Exception {
		QuadraticFormFunction myMeanFunction = QuadraticFormFunctionFactory.parseQuadraticForm(testFile1);
		RealDistribution errorDistribution = new NormalDistribution(0.0, 1.0);
		ConstantErrorBifunction<RealVector, SimpleTmiAction> myErrorFunction = new ConstantErrorBifunction<RealVector, SimpleTmiAction>(
				errorDistribution);
		BiFunction<RealVector, SimpleTmiAction, Double> myRewardFunction = new MeanErrorBifunction<RealVector, SimpleTmiAction>(
				myMeanFunction, myErrorFunction);

		GaussianKernel mySimilarityFunction = new GaussianKernel(1.0, 2);

		Generator<RealVector> myGenerator = new UniformStateGenerator(2);
		SimilarityBanditInstance myInstance = SimilarityBanditRunner.makeInstance(myRewardFunction, myGenerator,
				mySimilarityFunction, 1000, 10, new UniRandomSolver());

		BiFunction<SimpleTmiAction, SimpleTmiAction, Double> tmiComparer = GaussianTmiComparerFactory
				.makeDefaultTmiComparer();
		GreedyAverageSolver mySolver = new GreedyAverageSolver(tmiComparer,1.0);
		BanditOutcome myOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction, myInstance,
				mySolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));

		ContextualZoomingSolver myZoomSolver = new ContextualZoomingSolver(tmiComparer, 1.5, 10,1.0);
		BanditOutcome myZoomOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myZoomSolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myZoomOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));

		GpGreedySolver myGreedySolver = new GpGreedySolver(SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),1.0);
		BanditOutcome myGreedyOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myGreedySolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myGreedyOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));

		GpUcbSolver myGpUcbSolver = new GpUcbSolver(SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(), 0.0,1.0);
		BanditOutcome myGpUcbOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myGpUcbSolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myGpUcbOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));

		GpTsSolver myGpTsSolver = new GpTsSolver(SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),1.0);
		BanditOutcome myGpTsOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myGpTsSolver, 10);
		System.out.println(myGpTsOutcome);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myGpTsOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));
		/*
		BayesLrGreedySolver myLrGreedySolver = BayesLrFactory.MakeOneDegreeGreedyBayesLr();
		BanditOutcome myLrGreedyOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myLrGreedySolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myLrGreedyOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));

		BayesLrTsSolver myLrTsSolver = BayesLrFactory.MakeOneDegreeTsBayesLr();
		BanditOutcome myLrTsOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myLrTsSolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myLrTsOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));

		BayesLrSolver myLrUcbSolver = BayesLrFactory.MakeOneDegreeUcbBayesLr(0.0);
		BanditOutcome myLrUcbOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myLrUcbSolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myLrUcbOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));
		*/
		UniRandomSolver myRandomSolver = new UniRandomSolver();
		BanditOutcome myRandomOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
				myInstance, myRandomSolver, 10);
		System.out.println(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myRandomOutcome,
				myInstance.getHistoricalOutcomes().getActionSet()));
	}

	@Test
	public void testSolver2() throws Exception {
		QuadraticFormFunction myQuadFunction = QuadraticFormFunctionFactory.parseQuadraticForm(testFile1);
//		QuadraticFormFunction myMeanFunction = myQuadFunction;

		ReflectedBiFunction<RealVector, SimpleTmiAction> myMeanFunction = new ReflectedBiFunction<RealVector, SimpleTmiAction>(
				myQuadFunction, 50.0);
		RealDistribution errorDistribution = new NormalDistribution(0.0, 10.0);
		ConstantErrorBifunction<RealVector, SimpleTmiAction> myErrorFunction = new ConstantErrorBifunction<RealVector, SimpleTmiAction> (
				errorDistribution);
		BiFunction<RealVector, SimpleTmiAction, Double> myRewardFunction = new MeanErrorBifunction<RealVector, SimpleTmiAction>(
				myMeanFunction, myErrorFunction);

		GaussianKernel mySimilarityFunction = new GaussianKernel(1.0, 2);

		Generator<RealVector> myGenerator = new UniformStateGenerator(2);
		int numTrials = 100;
		DescriptiveStatistics avgRegretStats = new DescriptiveStatistics();
		DescriptiveStatistics zoomRegretStats = new DescriptiveStatistics();
		DescriptiveStatistics gpGrRegretStats = new DescriptiveStatistics();
		DescriptiveStatistics gpUcbRegretStats = new DescriptiveStatistics();
		DescriptiveStatistics gpTsRegretStats = new DescriptiveStatistics();
		DescriptiveStatistics randomRegretStats = new DescriptiveStatistics();
		DescriptiveStatistics avgOutcomeStats = new DescriptiveStatistics();
		DescriptiveStatistics zoomOutcomeStats = new DescriptiveStatistics();
		DescriptiveStatistics gpGrOutcomeStats = new DescriptiveStatistics();
		DescriptiveStatistics gpUcbOutcomeStats = new DescriptiveStatistics();
		DescriptiveStatistics gpTsOutcomeStats = new DescriptiveStatistics();
		DescriptiveStatistics randomOutcomeStats = new DescriptiveStatistics();
		/*
		 * double lrGrOutcome=0.0; double lrUcbOutcome=0.0; double
		 * lrTsOutcome=0.0;
		 */
		int historyLength = 500;
		int runLength = 7;
		for (int i = 0; i < numTrials; i++) {
			if (i % 1 == 0) {
				System.out.println(i);
			}
			SimilarityBanditInstance myInstance = SimilarityBanditRunner.makeInstance(myRewardFunction, myGenerator,
					mySimilarityFunction, runLength, historyLength, new UniRandomSolver());

			BiFunction<SimpleTmiAction, SimpleTmiAction, Double> tmiComparer = GaussianTmiComparerFactory
					.makeDefaultTmiComparer();
			GreedyAverageSolver mySolver = new GreedyAverageSolver(tmiComparer,1.0);
			BanditOutcome myOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction, myInstance,
					mySolver, runLength);
			avgRegretStats.addValue(SimilarityBanditRunner
					.calculateRegretStats(myMeanFunction, myOutcome, myInstance.getHistoricalOutcomes().getActionSet())
					.getMean());
			avgOutcomeStats.addValue(SimilarityBanditRunner
					.calculateRewardStats(myMeanFunction, myOutcome, myInstance.getHistoricalOutcomes().getActionSet())
					.getMean());

			ContextualZoomingSolver myZoomSolver = new ContextualZoomingSolver(tmiComparer, 1.5, runLength,1.0);
			BanditOutcome myZoomOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
					myInstance, myZoomSolver, runLength);
			zoomRegretStats.addValue(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myZoomOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
			zoomOutcomeStats.addValue(SimilarityBanditRunner.calculateRewardStats(myMeanFunction, myZoomOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());

			GpGreedySolver myGpGreedySolver = new GpGreedySolver(
					SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),1.0);
			BanditOutcome myGpGreedyOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
					myInstance, myGpGreedySolver, runLength);
			gpGrRegretStats.addValue(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myGpGreedyOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
			gpGrOutcomeStats.addValue(SimilarityBanditRunner.calculateRewardStats(myMeanFunction, myGpGreedyOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());

			GpUcbSolver myGpUcbSolver = new GpUcbSolver(SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(), 0.0,1.0);
			BanditOutcome myGpUcbOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
					myInstance, myGpUcbSolver, runLength);
			gpUcbRegretStats.addValue(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myGpUcbOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
			gpUcbOutcomeStats.addValue(SimilarityBanditRunner.calculateRewardStats(myMeanFunction, myGpUcbOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());

			GpTsSolver myGpTsSolver = new GpTsSolver(SimilarityGpFactory.makeZeroPriorSimilarityGpProcess(),1.0);
			BanditOutcome myGpTsOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
					myInstance, myGpTsSolver, runLength);
			gpTsRegretStats.addValue(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myGpTsOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
			gpTsOutcomeStats.addValue(SimilarityBanditRunner.calculateRewardStats(myMeanFunction, myGpTsOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
			/*
			 * BayesLrGreedySolver myLrGreedySolver =
			 * BayesLrFactory.MakeOneDegreeGreedyBayesLr(); BanditOutcome
			 * myLrGreedyOutcome =
			 * SimilarityBanditRunner.runBandit(myRewardFunction,
			 * mySimilarityFunction, myInstance, myLrGreedySolver, runLength);
			 * lrGrOutcome+=SimilarityBanditRunner.calculateExpectedRegret(
			 * myMeanFunction, myLrGreedyOutcome,
			 * myInstance.getHistoricalOutcomes().getActionSet());
			 * 
			 * BayesLrTsSolver myLrTsSolver =
			 * BayesLrFactory.MakeOneDegreeTsBayesLr(); BanditOutcome
			 * myLrTsOutcome =
			 * SimilarityBanditRunner.runBandit(myRewardFunction,
			 * mySimilarityFunction, myInstance, myLrTsSolver, runLength);
			 * lrTsOutcome+=SimilarityBanditRunner.calculateExpectedRegret(
			 * myMeanFunction, myLrTsOutcome,
			 * myInstance.getHistoricalOutcomes().getActionSet());
			 * 
			 * BayesLrSolver myLrUcbSolver =
			 * BayesLrFactory.MakeOneDegreeUcbBayesLr(0.0); BanditOutcome
			 * myLrUcbOutcome =
			 * SimilarityBanditRunner.runBandit(myRewardFunction,
			 * mySimilarityFunction, myInstance, myLrUcbSolver, runLength);
			 * lrUcbOutcome +=
			 * SimilarityBanditRunner.calculateExpectedRegret(myMeanFunction,
			 * myLrUcbOutcome,
			 * myInstance.getHistoricalOutcomes().getActionSet());
			 */
			RandomHistoryBanditSolver myRandomSolver = new RandomHistoryBanditSolver();
			BanditOutcome myRandomOutcome = SimilarityBanditRunner.runBandit(myRewardFunction, mySimilarityFunction,
					myInstance, myRandomSolver, runLength);
			randomRegretStats.addValue(SimilarityBanditRunner.calculateRegretStats(myMeanFunction, myRandomOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
			randomOutcomeStats.addValue(SimilarityBanditRunner.calculateRewardStats(myMeanFunction, myRandomOutcome,
					myInstance.getHistoricalOutcomes().getActionSet()).getMean());
		}
		System.out.println(avgRegretStats.getMean() + " (" + avgRegretStats.getStandardDeviation() + ")");
		System.out.println(zoomRegretStats.getMean() + " (" + zoomRegretStats.getStandardDeviation() + ")");
		System.out.println(gpGrRegretStats.getMean() + " (" + gpGrRegretStats.getStandardDeviation() + ")");
		System.out.println(gpUcbRegretStats.getMean() + " (" + gpUcbRegretStats.getStandardDeviation() + ")");
		System.out.println(gpTsRegretStats.getMean() + " (" + gpTsRegretStats.getStandardDeviation() + ")");
		System.out.println(randomRegretStats.getMean() + " (" + randomRegretStats.getStandardDeviation() + ")");

		System.out.println(avgOutcomeStats.getMean() + " (" + avgOutcomeStats.getStandardDeviation() + ")");
		System.out.println(zoomOutcomeStats.getMean() + " (" + zoomOutcomeStats.getStandardDeviation() + ")");
		System.out.println(gpGrOutcomeStats.getMean() + " (" + gpGrOutcomeStats.getStandardDeviation() + ")");
		System.out.println(gpUcbOutcomeStats.getMean() + " (" + gpUcbOutcomeStats.getStandardDeviation() + ")");
		System.out.println(gpTsOutcomeStats.getMean() + " (" + gpTsOutcomeStats.getStandardDeviation() + ")");
		System.out.println(randomOutcomeStats.getMean() + " (" + randomOutcomeStats.getStandardDeviation() + ")");

	}
}