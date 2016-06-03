package bandit_run_tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.BanditRunFactory;
import bandit_simulator.Generator;
import bandit_simulator.NasBanditInstance;
import bandit_simulator.NasBanditOutcome;
import bandit_simulator.NasBanditRunner;
import bandit_simulator.RunStateFunction;
import bandit_solvers.ContextualZoomingSolver;
import bandit_solvers.DoNothingSolver;
import bandit_solvers.DoNothingTmiGenerator;
import bandit_solvers.GpGreedySolver;
import bandit_solvers.GpTsSolver;
import bandit_solvers.GpUcbSolver;
import bandit_solvers.GreedyAverageSolver;
import bandit_solvers.RandomHistoryBanditSolver;
import bandit_solvers.SimilarityBanditSolver;
import random_processes.GaussianTmiComparerFactory;
import random_processes.SimilarityGpFactory;
import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.DefaultCapacityComparer;
import state_representation.DefaultState;
import state_update.CapacityScenarioUpdate;
import state_update.DefaultFlightHandler;
import state_update.FlightHandler;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update_factories.DefaultNasUpdateFactory;
import util_random.ConstantDistribution;
import util_random.Distribution;

public class BanditRunTests {
	public static final File instanceFile1 = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/nasBanditInstance1.out");
	public static final DateTimeZone ewrTimeZone = DateTimeZone.forID("America/New_York");
	public static final double rewardMean = -1102.2190280300927;
	public static final double rewardVar = 904567.0701894417;

	// Files storing pools of days with no outcomes
	public static final File testNoOutcomeTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testTmiNoOutPool.out");
	public static final File trainNoOutcomeTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainTmiNoOutPool.out");
	public static final File testNoOutcomeNoTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testNoTmiNoOutPool.out");
	public static final File trainNoOutcomeNoTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainNoTmiNoOutPool.out");

	// Files storing pools of days with historical TMI outcomes
	public static final File testHistTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testHistTmiNoRandPool.out");
	public static final File trainHistTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainHistTmiNoRandPool.out");

	// Files storing pools of day where no TMI is run
	public static final File testNoTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testNoTmiNoRandPool.out");
	public static final File trainNoTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainNoTmiNoRandPool.out");

	// Files storing pools of days where randomly generated TMIs are run
	public static final File testRandTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testRandTmiNoRandPool.out");
	public static final File trainRandTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainRandTmiNoRandPool.out");

	public static final String btsDirName = "C:/Users/Alex/Documents/Research/Data Sets/RITA_EWR_BY_DAY";
	public static final String btsFilePrefix = "EWR_BTS_On_Time";
	public static final String adlDirName = "C:/Users/Alex/Documents/Research/Data Sets/adl_ewr_reduced";
	public static final String adlFilePrefix = "RED_ADL_ewr_lcdm_";

	public static final File capacityFile = new File("TestFiles/CapacityFiles/surv_dates_Apr16_DemoDate_v2.csv");
	public static final File distanceFile = new File("TestFiles/CapacityFiles/surv_dist_demoData_Dep6_v2_2.csv");
	// public static final File tmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP Research/BanditTmiData.csv");
	public static final File tmiFile = new File("C:/Users/Alex/Documents/Research/ACRP Research/BanditGdpData.csv");

	@Test
	public void testTmiFileRead() throws IOException {
		System.out.println(new TreeMap<LocalDate, SimpleTmiAction>(BanditRunFactory.parseTmiFile(tmiFile, true)));
	}

	@Test
	public void testCapacityFileRead() throws IOException {
		Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacities = BanditRunFactory
				.parseCapacityFile(capacityFile);
		IntegerDistribution myDistribution = myCapacities.get(new LocalDate(2011, 1, 7)).get(21);
		for (int i = 0; i < BanditRunFactory.MAXIMUM_CAPACITY; i++) {
			System.out.println(1 - myDistribution.cumulativeProbability(i));
		}
		System.out.println(myCapacities);
	}

	@Test
	public void testDistanceFileRead() throws IOException {
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);
		Iterator<LocalDate> outerIter = myDistances.keySet().iterator();
		for (int i = 0; i < 10; i++) {
			LocalDate outerKey = outerIter.next();
			Map<LocalDate, Double> row = myDistances.get(outerKey);
			Iterator<LocalDate> innerIter = row.keySet().iterator();
			for (int j = 0; j < 10; j++) {
				LocalDate innerKey = innerIter.next();
				System.out.println(outerKey.toString() + "," + innerKey.toString() + "," + row.get(innerKey));
			}
		}
	}

	@Test
	public void testSamplingFunction() {
		Set<Integer> myInts = new HashSet<Integer>();
		Random myRandom = new Random();
		for (int i = 0; i < 25; i++) {
			myInts.add(myRandom.nextInt(500));
		}
		System.out.println(myInts);
		System.out.println(BanditRunFactory.randomSample(myInts, 5, 3));
	}

	@Test
	public void testBtsFlightStateMakingFunction() throws Exception {
		int startHour = 10;
		LocalDate date = new LocalDate(2011, 10, 22);
		DateTime startTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(),
				startHour, 0, ewrTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(14)));
		System.out.println(FlightStateFactory.parseBtsFlightFile(btsDirName, btsFilePrefix, date, runInterval,
				ewrTimeZone, FlightFactory.BTS_FORMAT_ID));
	}

	@Test
	public void testAdlFlightStateMakingFunction() throws Exception {
		int startHour = 10;
		LocalDate date = new LocalDate(2011, 10, 22);
		DateTime startTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(),
				startHour, 0, ewrTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(14)));
		System.out.println(FlightStateFactory.parseAdlFlightFile(adlDirName, adlFilePrefix, date, runInterval,
				ewrTimeZone, FlightFactory.ADL_FORMAT_ID));
	}

	@Test
	public void testTmiDataReading() throws Exception {
		// Read capacity distributions for each day
		System.out.println("Reading capacity file");
		Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacityDistributions = BanditRunFactory
				.parseCapacityFile(capacityFile);
		// Read TMIs for each day
		System.out.println("Reading tmi file.");
		Map<LocalDate, SimpleTmiAction> myTmiActions = BanditRunFactory.parseTmiFile(tmiFile, true);

		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);
		
		Set<LocalDate> availableDates = new HashSet<LocalDate>(myCapacityDistributions.keySet());
		availableDates.retainAll(myDistances.keySet());
		
		Set<LocalDate> myDates = BanditRunFactory.getValidDates(myTmiActions, myCapacityDistributions.keySet());
		for (LocalDate date : myDates) {
			if (!myTmiActions.containsKey(date)) {
				myTmiActions.put(date, new SimpleTmiAction());
			}
		}

		SortedSet<LocalDate> mySortedDates = new TreeSet<LocalDate>(myDates);
		for (LocalDate day : mySortedDates) {
			System.out.println(day);
			System.out.println(myTmiActions.get(day));
		}
	}

	@Test
	public void testShuffle() {
		List<Integer> myList = new ArrayList<Integer>();
		for (int i = 0; i < 25; i++) {
			myList.add(i);
		}
		for (int i = 0; i < 10; i++) {
			System.out.println(BanditRunFactory.shuffle(myList));
		}
	}

	@Test
	public void testMakeNoOutcomeDayPool() throws Exception {
		// Choose delay distributions and make the flight handler
		Distribution<Duration> depDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
		Distribution<Duration> arrDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
		FlightHandler myFlightHandler = new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);
		int startHour = 10;
		int runHours = 14;
		BanditRunFactory.makeNoOutcomeDayPools(capacityFile, tmiFile, distanceFile, trainNoOutcomeTmiFile,
				testNoOutcomeTmiFile, trainNoOutcomeNoTmiFile, testNoOutcomeNoTmiFile, adlDirName, adlFilePrefix,
				ewrTimeZone, startHour, runHours, myFlightHandler);
		@SuppressWarnings("unchecked")
		Map<LocalDate, DefaultState> trainTmis = (Map<LocalDate, DefaultState>) BanditRunFactory
				.readObjectFromFile(trainNoOutcomeTmiFile);
		@SuppressWarnings("unchecked")
		Map<LocalDate, DefaultState> testTmis = (Map<LocalDate, DefaultState>) BanditRunFactory
				.readObjectFromFile(testNoOutcomeTmiFile);
		@SuppressWarnings("unchecked")
		Map<LocalDate, DefaultState> trainNoTmis = (Map<LocalDate, DefaultState>) BanditRunFactory
				.readObjectFromFile(trainNoOutcomeNoTmiFile);
		@SuppressWarnings("unchecked")
		Map<LocalDate, DefaultState> testNoTmis = (Map<LocalDate, DefaultState>) BanditRunFactory
				.readObjectFromFile(testNoOutcomeNoTmiFile);
		System.out.println("Train tmis: " + trainTmis.keySet().size());
		System.out.println("Test tmis: " + testTmis.keySet().size());
		System.out.println("Train no tmis: " + trainNoTmis.keySet().size());
		System.out.println("Test no tmis: " + testNoTmis.keySet().size());

		Set<LocalDate> allTmis = new HashSet<LocalDate>(trainTmis.keySet());
		allTmis.addAll(testTmis.keySet());
		System.out.println("All tmis: " + allTmis.size());

		Set<LocalDate> allNoTmis = new HashSet<LocalDate>(trainNoTmis.keySet());
		allNoTmis.addAll(testNoTmis.keySet());
		System.out.println("All no tmis: " + allNoTmis.size());
		allNoTmis.retainAll(allTmis);
		System.out.println("All tmis intersect all no tmis: " + allNoTmis.size());
	}

	@Test
	public void testMakeInstances() throws ClassNotFoundException, IOException {
		BanditRunFactory.writeObjectToFile(BanditRunFactory.createInstancesFromPool(distanceFile, trainHistTmiFile,
				trainNoTmiFile, 50, 129, 129, 200, 5, 1.0), instanceFile1);
	}

	private RunStateFunction makeDayRunner() {
		Duration timeStep = Duration.standardMinutes(1);
		double airDelayWeight = 3.0;

		// Choose delay distributions and make the flight handler
		Distribution<Duration> depDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
		Distribution<Duration> arrDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
		FlightHandler myFlightHandler = new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);

		// Make update module
		NASStateUpdate myUpdate = DefaultNasUpdateFactory.makeDefault();
		UpdateModule myCompleteUpdate = new UpdateModule(myUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		return new RunStateFunction(myFlightHandler, myCompleteUpdate, timeStep, airDelayWeight);
	}

	@Test
	public void testReadOutcomes() throws Exception {
		@SuppressWarnings("unchecked")
		Map<LocalDate, ImmutableTriple<DefaultState, SimpleTmiAction, Double>> myOutcomes = (Map<LocalDate, ImmutableTriple<DefaultState, SimpleTmiAction, Double>>) BanditRunFactory
				.readObjectFromFile(trainRandTmiFile);
		for (Entry<LocalDate, ImmutableTriple<DefaultState, SimpleTmiAction, Double>> entry : myOutcomes.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue().getLeft().getCurrentTime());
			System.out.println(entry.getValue().getMiddle());
			System.out.println(entry.getValue().getRight());
		}
	}

	@Test
	public void testAddOutcomesToDayPool() throws Exception {
		RunStateFunction myNasRunner = makeDayRunner();
		System.out.println("Adding historical outcomes to training TMIs.");
		BanditRunFactory.addHistoricalTmiOutcomesToPool(tmiFile, false, distanceFile, trainNoOutcomeTmiFile,
				trainHistTmiFile, myNasRunner);
		System.out.println("Adding historical outcomes to test TMIs.");
		BanditRunFactory.addHistoricalTmiOutcomesToPool(tmiFile, false, distanceFile, testNoOutcomeTmiFile,
				testHistTmiFile, myNasRunner);

		System.out.println("Adding generated outcomes to train TMIs.");
		Generator<SimpleTmiAction> myTmiGenerator = BanditRunFactory.makeDefaultEWRGdpGenerator();
		BanditRunFactory.addGeneratedTmiOutcomesToPool(myTmiGenerator, distanceFile, trainNoOutcomeTmiFile,
				trainRandTmiFile, myNasRunner);
		System.out.println("Adding generated outcomes to test TMIs.");
		BanditRunFactory.addGeneratedTmiOutcomesToPool(myTmiGenerator, distanceFile, testNoOutcomeTmiFile,
				testRandTmiFile, myNasRunner);

		System.out.println("Adding outcomes to train no TMI days.");
		myTmiGenerator = new DoNothingTmiGenerator();
		BanditRunFactory.addGeneratedTmiOutcomesToPool(myTmiGenerator, distanceFile, trainNoOutcomeNoTmiFile,
				trainNoTmiFile, myNasRunner);
		System.out.println("Adding generated outcomes to test no TMI days.");
		BanditRunFactory.addGeneratedTmiOutcomesToPool(myTmiGenerator, distanceFile, testNoOutcomeNoTmiFile,
				testNoTmiFile, myNasRunner);
	}

	@Test
	public void runBanditTest() throws Exception {
		int numHistory = 150;
		int numRun = 5;

		// Choose delay distributions and make the flight handler
		// Distribution<Duration> depDelayDistribution = new
		// UniformDurationDistribution(-5 * 60, 15 * 60,
		// Duration.standardSeconds(1));
		// Distribution<Duration> arrDelayDistribution = new
		// UniformDurationDistribution(-15 * 60, 15 * 60,
		// Duration.standardSeconds(1));
		RunStateFunction myNasRunner = makeDayRunner();

		System.out.println("Reading instances");
		@SuppressWarnings("unchecked")
		List<NasBanditInstance> myInstances = (List<NasBanditInstance>) BanditRunFactory
				.readObjectFromFile(instanceFile1);
		List<SimilarityBanditSolver> mySolvers = makeListSolvers(numRun, numHistory);

		int outerIterNum = 0;
		List<DescriptiveStatistics> myRewardStatList = new LinkedList<DescriptiveStatistics>();
		List<DescriptiveStatistics> myImprovementStatList = new LinkedList<DescriptiveStatistics>();
		for (SimilarityBanditSolver solver : mySolvers) {
			DescriptiveStatistics myRewardStats = new DescriptiveStatistics();
			DescriptiveStatistics myImprovementStats = new DescriptiveStatistics();

			int innerIterNum = 0;
			for (NasBanditInstance instance : myInstances) {

				DescriptiveStatistics instanceStatistics = new DescriptiveStatistics();
				for (Double outcome : instance.getUnseenBaseOutcome()) {
					instanceStatistics.addValue(outcome);
				}

				System.out.println("Running solver " + outerIterNum + ", instance: " + innerIterNum);
				NasBanditOutcome myOutcome = NasBanditRunner.runBandit(myNasRunner, instance, solver, numRun);
				myRewardStats.addValue(NasBanditRunner.calculateRewardStats(myOutcome).getMean());
				myImprovementStats.addValue(
						NasBanditRunner.calculateRewardStats(myOutcome).getMean() - instanceStatistics.getMean());

				solver.reset();
				innerIterNum++;
			}
			myRewardStatList.add(myRewardStats);
			myImprovementStatList.add(myImprovementStats);
			outerIterNum++;
		}

		DescriptiveStatistics baseStats = new DescriptiveStatistics();
		for (NasBanditInstance instance : myInstances) {
			DescriptiveStatistics instanceStatistics = new DescriptiveStatistics();
			for (Double outcome : instance.getUnseenBaseOutcome()) {
				instanceStatistics.addValue(outcome);
			}
			baseStats.addValue(instanceStatistics.getMean());
		}
		System.out.println("Rewards: ");
		System.out.println("Base solution: " + baseStats.getMean() + " (" + baseStats.getStandardDeviation() + ")");
		outerIterNum = 0;
		for (DescriptiveStatistics stats : myRewardStatList) {
			System.out.println(
					"Solver " + outerIterNum + ": " + stats.getMean() + " (" + stats.getStandardDeviation() + ")");
			outerIterNum++;
		}

		System.out.println("Improvement over base solution: ");
		outerIterNum = 0;
		for (DescriptiveStatistics stats : myImprovementStatList) {
			System.out.println(
					"Solver " + outerIterNum + ": " + stats.getMean() + " (" + stats.getStandardDeviation() + ")");
			outerIterNum++;
		}
	}

	private List<SimilarityBanditSolver> makeListSolvers(int numRun, int numHistory) {
		List<SimilarityBanditSolver> mySolvers = new ArrayList<SimilarityBanditSolver>();

		BiFunction<SimpleTmiAction, SimpleTmiAction, Double> tmiComparer = GaussianTmiComparerFactory
				.makeDefaultTmiComparer();
		DoNothingSolver myDoNothingSolver = new DoNothingSolver();
		GreedyAverageSolver mySolver = new GreedyAverageSolver(tmiComparer);
		ContextualZoomingSolver myZoomSolver = new ContextualZoomingSolver(tmiComparer, 1.5, numRun + numHistory);
		double lowerBound = rewardMean - 2 * Math.sqrt(rewardVar);
		GpGreedySolver myGreedySolver = new GpGreedySolver(
				SimilarityGpFactory.makeConstantPriorSimilarityGpProcess(lowerBound, rewardVar));
		GpUcbSolver myGpUcbSolver = new GpUcbSolver(
				SimilarityGpFactory.makeConstantPriorSimilarityGpProcess(lowerBound, rewardVar), 0.0);
		GpTsSolver myGpTsSolver = new GpTsSolver(
				SimilarityGpFactory.makeConstantPriorSimilarityGpProcess(lowerBound, rewardVar));
		RandomHistoryBanditSolver myRandomSolver = new RandomHistoryBanditSolver();
		mySolvers.add(myDoNothingSolver);
		mySolvers.add(mySolver);
		mySolvers.add(myZoomSolver);
		mySolvers.add(myGreedySolver);
		mySolvers.add(myGpUcbSolver);
		mySolvers.add(myGpTsSolver);
		mySolvers.add(myRandomSolver);

		return mySolvers;
	}
}
