package bandit_run_tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.BanditRunFactory;
import bandit_simulator.NasBanditInstance;
import bandit_simulator.NasBanditOutcome;
import bandit_simulator.NasBanditRunner;
import bandit_simulator.RunStateFunction;
import bandit_solvers.ContextualZoomingSolver;
import bandit_solvers.DoNothingSolver;
import bandit_solvers.GpGreedySolver;
import bandit_solvers.GpTsSolver;
import bandit_solvers.GpUcbSolver;
import bandit_solvers.GreedyAverageSolver;
import bandit_solvers.RandomHistoryBanditSolver;
import bandit_solvers.SimilarityBanditSolver;
import model.Pair;
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
import util_random.UniformDurationDistribution;

public class BanditRunTests {
	public static final File instanceFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/nasBanditInstances.out");
	public static final DateTimeZone ewrTimeZone = DateTimeZone.forID("America/New_York");

	// public static final File testTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP Research/testTmiPool.out");
	// public static final File trainTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP Research/trainTmiPool.out");
	// public static final File testNoTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP Research/testNoTmiPool.out");
	// public static final File trainNoTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/trainNoTmiPool.out");

	public static final File testTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testTmiNoRandPool.out");
	public static final File trainTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainTmiNoRandPool.out");
	public static final File testNoTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/testNoTmiNoRandPool.out");
	public static final File trainNoTmiFile = new File(
			"C:/Users/Alex/Documents/Research/ACRP Research/trainNoTmiNoRandPool.out");

	// public static final File testTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/testTmiUltraHighAirPool.out");
	// public static final File trainTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/trainTmiUltraHighAirPool.out");
	// public static final File testNoTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/testNoTmiUltraHighAirPool.out");
	// public static final File trainNoTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/trainNoTmiUltraHighAirPool.out");
	//
	// public static final File testTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/testTmiHighAirPool.out");
	// public static final File trainTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/trainTmiHighAirPool.out");
	// public static final File testNoTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/testNoTmiHighAirPool.out");
	// public static final File trainNoTmiFile = new
	// File("C:/Users/Alex/Documents/Research/ACRP
	// Research/trainNoTmiHighAirPool.out");

	public static final String btsDirName = "C:/Users/Alex/Documents/Research/Data Sets/RITA_EWR_BY_DAY";
	public static final String btsFilePrefix = "EWR_BTS_On_Time";

	public static final File capacityFile = new File("TestFiles/CapacityFiles/surv_dates_Apr16_DemoDate_v2.csv");
	public static final File distanceFile = new File("TestFiles/CapacityFiles/surv_dist_demoData_Dep6_v2_2.csv");
	public static final File tmiFile = new File("C:/Users/Alex/Documents/Research/ACRP Research/BanditTmiData.csv");

	@Test
	public void testTmiFileRead() throws IOException {
		System.out.println(new TreeMap<LocalDate, SimpleTmiAction>(BanditRunFactory.parseTmiFile(tmiFile)));
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
				startHour, 0,ewrTimeZone);
		System.out.println(FlightStateFactory.parseBtsFlightFile(btsDirName, btsFilePrefix, date, startTime,ewrTimeZone,
				FlightFactory.BTS_FORMAT_ID));
	}

	@Test
	public void testTmiDataReading() throws Exception {
		// Read capacity distributions for each day
		System.out.println("Reading capacity file");
		Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacityDistributions = BanditRunFactory
				.parseCapacityFile(capacityFile);
		// Read TMIs for each day
		System.out.println("Reading tmi file.");
		Map<LocalDate, SimpleTmiAction> myTmiActions = BanditRunFactory.parseTmiFile(tmiFile);

		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		Set<LocalDate> myDates = BanditRunFactory.getValidDates(myTmiActions, myCapacityDistributions, myDistances);
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
	public void testMakeDayPool() throws Exception {
		int numTmi = 250;
		int numNoTmi = 250;

		int startHour = 10;
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
		RunStateFunction myNasRunner = new RunStateFunction(myFlightHandler, myCompleteUpdate, timeStep,
				airDelayWeight);

		BanditRunFactory.makeDayPool(capacityFile, tmiFile, distanceFile, trainTmiFile, testTmiFile, trainNoTmiFile,
				testNoTmiFile, btsDirName, btsFilePrefix, ewrTimeZone, startHour, numTmi, numNoTmi, myFlightHandler,
				myNasRunner);
		Map<DefaultState, Pair<SimpleTmiAction, Double>> outcomes = BanditRunFactory.readOutcomesFromFile(trainTmiFile);
		for (Map.Entry<DefaultState, Pair<SimpleTmiAction, Double>> myEntry : outcomes.entrySet()) {
			System.out.println(myEntry.getKey().getCurrentTime());
			System.out.println(myEntry.getKey().getCapacityState().getActualScenario());
			System.out.println(myEntry.getValue().getItemA());
			System.out.println(myEntry.getValue().getItemB());
		}
	}

	@Test
	public void testReadPool() throws ClassNotFoundException, IOException {
		Map<DefaultState, Pair<SimpleTmiAction, Double>> outcomes = BanditRunFactory.readOutcomesFromFile(trainTmiFile);
		for (Map.Entry<DefaultState, Pair<SimpleTmiAction, Double>> myEntry : outcomes.entrySet()) {
			System.out.println(myEntry.getKey().getCurrentTime());
			System.out.println(myEntry.getKey().getCapacityState().getActualScenario());
			System.out.println(myEntry.getValue().getItemA());
			System.out.println(myEntry.getValue().getItemB());
		}
	}

	@Test
	public void testSerialization() throws Exception {
		int numInstances = 50;
		int numHistory = 50;
		int numRun = 5;

		int startHour = 10;
		Duration timeStep = Duration.standardMinutes(1);
		double airDelayWeight = 100.0;

		// Choose delay distributions and make the flight handler
		Distribution<Duration> depDelayDistribution = new UniformDurationDistribution(-5 * 60, 15 * 60,
				Duration.standardSeconds(1));
		Distribution<Duration> arrDelayDistribution = new UniformDurationDistribution(-15 * 60, 15 * 60,
				Duration.standardSeconds(1));
		FlightHandler myFlightHandler = new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);

		// Make update module
		NASStateUpdate myUpdate = DefaultNasUpdateFactory.makeDefault();
		UpdateModule myCompleteUpdate = new UpdateModule(myUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		RunStateFunction myNasRunner = new RunStateFunction(myFlightHandler, myCompleteUpdate, timeStep,
				airDelayWeight);

		BanditRunFactory.serializeInstances(instanceFile, capacityFile, tmiFile, distanceFile, btsDirName,
				btsFilePrefix, ewrTimeZone, numInstances, numHistory, numRun, startHour, myFlightHandler, myNasRunner,
				2.0);

	}

	@Test
	public void testDeserialization() throws Exception {
		List<NasBanditInstance> myInstances = BanditRunFactory.deserializeInstances(instanceFile);
		for (NasBanditInstance myInstance : myInstances) {
			Iterator<SimpleTmiAction> myActionIter = myInstance.getHistoricalOutcomes().getActions().iterator();
			for (DefaultState state : myInstance.getHistoricalOutcomes().getStates()) {
				System.out.println(state.getCapacityState());
				System.out.println(myActionIter.next());
			}
		}

	}

	@Test
	public void runBanditTestTwo() throws Exception {
		int numHistory = 400;
		int numRun = 10;

		Duration timeStep = Duration.standardMinutes(1);
		double airDelayWeight = 3.0;

		// Choose delay distributions and make the flight handler
		// Distribution<Duration> depDelayDistribution = new
		// UniformDurationDistribution(-5 * 60, 15 * 60,
		// Duration.standardSeconds(1));
		// Distribution<Duration> arrDelayDistribution = new
		// UniformDurationDistribution(-15 * 60, 15 * 60,
		// Duration.standardSeconds(1));
		Distribution<Duration> depDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
		Distribution<Duration> arrDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
		FlightHandler myFlightHandler = new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);

		// Make update module
		NASStateUpdate myUpdate = DefaultNasUpdateFactory.makeDefault();
		UpdateModule myCompleteUpdate = new UpdateModule(myUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		RunStateFunction myNasRunner = new RunStateFunction(myFlightHandler, myCompleteUpdate, timeStep,
				airDelayWeight);

		System.out.println("Reading instances");
		List<NasBanditInstance> myInstances = BanditRunFactory.createInstancesFromPool(distanceFile, trainTmiFile,
				trainNoTmiFile, 50, 220, 200, numHistory, numRun, 2.0);
		List<SimilarityBanditSolver> mySolvers = makeListSolvers(numRun, numHistory);

		int outerIterNum = 0;
		List<DescriptiveStatistics> myStatList = new LinkedList<DescriptiveStatistics>();
		for (SimilarityBanditSolver solver : mySolvers) {
			DescriptiveStatistics myStats = new DescriptiveStatistics();
			int innerIterNum = 0;
			for (NasBanditInstance instance : myInstances) {

				DescriptiveStatistics instanceStatistics = new DescriptiveStatistics();
				for (Double outcome : instance.getUnseenBaseOutcome()) {
					instanceStatistics.addValue(outcome);
				}

				System.out.println("Running solver " + outerIterNum + ", instance: " + innerIterNum);
				NasBanditOutcome myOutcome = NasBanditRunner.runBandit(myNasRunner, instance, solver, numRun);
				myStats.addValue(
						NasBanditRunner.calculateRewardStats(myOutcome).getMean() - instanceStatistics.getMean());
				solver.reset();
				innerIterNum++;
			}
			myStatList.add(myStats);
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
		System.out.println("Base solution: " + baseStats.getMean() + " (" + baseStats.getStandardDeviation() + ")");
		outerIterNum = 0;
		for (DescriptiveStatistics stats : myStatList) {
			System.out.println(
					"Solver " + outerIterNum + ": " + stats.getMean() + " (" + stats.getStandardDeviation() + ")");
			outerIterNum++;
		}
	}

	@Test
	public void runBanditTestOne() throws Exception {
		int numHistory = 10;
		int numRun = 5;

		Duration timeStep = Duration.standardMinutes(1);
		double airDelayWeight = 100.0;

		// Choose delay distributions and make the flight handler
		Distribution<Duration> depDelayDistribution = new UniformDurationDistribution(-5 * 60, 15 * 60,
				Duration.standardSeconds(1));
		Distribution<Duration> arrDelayDistribution = new UniformDurationDistribution(-15 * 60, 15 * 60,
				Duration.standardSeconds(1));
		FlightHandler myFlightHandler = new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);

		// Make update module
		NASStateUpdate myUpdate = DefaultNasUpdateFactory.makeDefault();
		UpdateModule myCompleteUpdate = new UpdateModule(myUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		RunStateFunction myNasRunner = new RunStateFunction(myFlightHandler, myCompleteUpdate, timeStep,
				airDelayWeight);

		System.out.println("Reading instances");
		List<NasBanditInstance> myInstances = BanditRunFactory.deserializeInstances(instanceFile);
		List<SimilarityBanditSolver> mySolvers = makeListSolvers(numRun, numHistory);

		int outerIterNum = 0;
		List<DescriptiveStatistics> myStatList = new LinkedList<DescriptiveStatistics>();
		for (SimilarityBanditSolver solver : mySolvers) {
			DescriptiveStatistics myStats = new DescriptiveStatistics();
			int innerIterNum = 0;
			for (NasBanditInstance instance : myInstances) {
				System.out.println("Running solver " + outerIterNum + ", instance: " + innerIterNum);
				NasBanditOutcome myOutcome = NasBanditRunner.runBandit(myNasRunner, instance, solver, numRun);
				myStats.addValue(NasBanditRunner.calculateRewardStats(myOutcome).getMean());
				solver.reset();
				innerIterNum++;
			}
			myStatList.add(myStats);
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
		System.out.println("Base solution: " + baseStats.getMean() + " (" + baseStats.getStandardDeviation() + ")");
		outerIterNum = 0;
		for (DescriptiveStatistics stats : myStatList) {
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
		GpGreedySolver myGreedySolver = new GpGreedySolver(
				SimilarityGpFactory.makeConstantPriorSimilarityGpProcess(0.0));
		GpUcbSolver myGpUcbSolver = new GpUcbSolver(SimilarityGpFactory.makeConstantPriorSimilarityGpProcess(0.0), 0.0);
		GpTsSolver myGpTsSolver = new GpTsSolver(SimilarityGpFactory.makeConstantPriorSimilarityGpProcess(-20000.0));
		RandomHistoryBanditSolver myRandomSolver = new RandomHistoryBanditSolver();
		// mySolvers.add(myDoNothingSolver);
		// mySolvers.add(mySolver);
		// mySolvers.add(myZoomSolver);
		// mySolvers.add(myGreedySolver);
		// mySolvers.add(myGpUcbSolver);
		mySolvers.add(myGpTsSolver);
		// mySolvers.add(myRandomSolver);

		return mySolvers;
	}
}
