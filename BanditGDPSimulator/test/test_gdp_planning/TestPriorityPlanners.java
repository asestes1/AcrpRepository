package test_gdp_planning;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import engine_factories.SimulationEngineFactory;
import function_util.FunctionEx;
import gdp_planning.BasicPAARChooser;
import gdp_planning.ConstantRadiusChooser;
import gdp_planning.FixedDurationGDPIntervalChooser;
import gdp_planning.IndParamPlanner;
import gdp_planning.ShiftedBasicPAARChooser;
import gdp_planning.StandardTmiPlanner;
import metrics.MetricCalculator;
import model.DoNothingModule;
import model.GdpAction;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import model.StateAction;
import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
import state_criteria.AtStartCriteriaFactory;
import state_criteria.RateIncreaseCriteria;
import state_criteria.StateCriteria;
import state_factories.AirportStateFactory;
import state_factories.CapacityScenarioFactory;
import state_factories.DateTimeFactory;
import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.AirportState;
import state_representation.CapacityScenarioState;
import state_representation.DefaultCapacityComparer;
import state_representation.DefaultState;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.CapacityScenarioUpdate;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightDurationFieldComparator;
import state_update.FlightHandler;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update_factories.FlightHandlerFactory;
import util_random.ConstantRunwayDistribution;
import util_random.Distribution;
import util_random.UniformIntDistribution;

public class TestPriorityPlanners {
	private final File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
	private final File flightFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
	private final File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
	private final File btsFile = new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	private final File highCapacityFile = new File("TestFiles/CapacityFiles/testBasicCapacityA");
	private final File lowCapacityFile = new File("TestFiles/CapacityFiles/testBasicCapacityB");
	private final File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
	private final File singleLoToHiFile = new File("TestFiles/ScenarioFiles/singleScenarioLoHi");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");

	@Test
	public void FullCapacityRBDTest() throws Exception {

		File outFile = new File("TestOutputFiles/TestGDPSolvers/RBDFullCapacityTestOut1");

		FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(new BasicPAARChooser(),
				new FixedDurationGDPIntervalChooser(Duration.standardDays(1)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		StandardTmiPlanner myGDPModule = new StandardTmiPlanner(myTmiChooser,
				new FlightDurationFieldComparator(Flight.flightTimeID).reversed());

		SimulationEngineInstance<DefaultState> myEngine = SimulationEngineFactory.makeSimulationInstance(startTimeFile,
				flightHandlerFile, SimulationEngineFactory.FULL_CAPACITY, flightFile,
				SimulationEngineFactory.BASIC_SCENARIO, highCapacityFile, airportFile, updateFile, myGDPModule,
				ordTimeZone);

		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println(finalState.toString());
		myStream.close();

	}

	@Test
	public void FullCapacityRBSTest() throws Exception {

		File outFile = new File("TestOutputFiles/TestGDPSolvers/RBSFullCapacityTestOut1");
		FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(new BasicPAARChooser(),
				new FixedDurationGDPIntervalChooser(Duration.standardDays(1)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		StandardTmiPlanner myGDPModule = new StandardTmiPlanner(myTmiChooser,
				new FlightDurationFieldComparator(Flight.origETAFieldID));

		SimulationEngineInstance<DefaultState> myEngine = SimulationEngineFactory.makeSimulationInstance(startTimeFile,
				flightHandlerFile, SimulationEngineFactory.FULL_CAPACITY, flightFile,
				SimulationEngineFactory.BASIC_SCENARIO, highCapacityFile, airportFile, updateFile, myGDPModule,
				ordTimeZone);

		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println(finalState.toString());
		myStream.close();
	}

	@Test
	public void CrunchedCapacityRBDTest() throws Exception {

		File outFile = new File("TestOutputFiles/TestGDPSolvers/RBDLowCapacityTestOut1");
		FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(new BasicPAARChooser(),
				new FixedDurationGDPIntervalChooser(Duration.standardDays(1)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		StandardTmiPlanner myGDPModule = new StandardTmiPlanner(myTmiChooser,
				new FlightDurationFieldComparator(Flight.flightTimeID).reversed());

		SimulationEngineInstance<DefaultState> myEngine = SimulationEngineFactory.makeSimulationInstance(startTimeFile,
				flightHandlerFile, SimulationEngineFactory.FULL_CAPACITY, flightFile,
				SimulationEngineFactory.BASIC_SCENARIO, lowCapacityFile, airportFile, updateFile, myGDPModule,
				ordTimeZone);

		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println("Total scheduled ground delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.scheduledDelayID));
		List<Flight> myFlights = new ArrayList<Flight>(finalState.getFlightState().getLandedFlights());
		Collections.sort(myFlights, new FlightDateTimeFieldComparator(Flight.origETAFieldID));
		Iterator<Flight> myIter = myFlights.iterator();
		myStream.println("Landed Flights: ");
		while (myIter.hasNext()) {
			myStream.println(myIter.next().toString());
		}
		myStream.close();

	}

	@Test
	public void CrunchedCapacityRBSTest() throws Exception {

		File outFile = new File("TestOutputFiles/TestGDPSolvers/RBSCrunchedCapacityTestOut1");
		FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(new BasicPAARChooser(),
				new FixedDurationGDPIntervalChooser(Duration.standardDays(1)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		StandardTmiPlanner myGDPModule = new StandardTmiPlanner(myTmiChooser,
				new FlightDurationFieldComparator(Flight.origETAFieldID));

		SimulationEngineInstance<DefaultState> myEngine = SimulationEngineFactory.makeSimulationInstance(startTimeFile,
				flightHandlerFile, SimulationEngineFactory.FULL_CAPACITY, flightFile,
				SimulationEngineFactory.BASIC_SCENARIO, lowCapacityFile, airportFile, updateFile, myGDPModule,
				ordTimeZone);

		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println("Total scheduled ground delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.scheduledDelayID));
		List<Flight> myFlights = new ArrayList<Flight>(finalState.getFlightState().getLandedFlights());
		Collections.sort(myFlights, new FlightDateTimeFieldComparator(Flight.origETAFieldID));
		Iterator<Flight> myIter = myFlights.iterator();
		myStream.println("Landed Flights: ");
		while (myIter.hasNext()) {
			myStream.println(myIter.next().toString());
		}
		myStream.close();
	}

	@Test
	public void StaticPriorityValidationTest() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/PriorityValidationTestA");
		// Initialize start time
		DateTime startTime = DateTimeFactory.parse(startTimeFile, ordTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(24)));

		// Initialize flight handler
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);

		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myFieldGenerators);
		FlightState myFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		// Create the initial state
		AirportState myAirportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		CapacityScenarioState myCapacity = new CapacityScenarioState(40);
		DefaultState myInit = new DefaultState(startTime, myFlightState, myAirportState, myCapacity);

		// Create the flight updater
		NASStateUpdate myNASStateUpdate = new NASStateUpdate(new ConstantRunwayDistribution());
		UpdateModule myFlightUpdater = new UpdateModule(myNASStateUpdate, new DoNothingModule<CapacityScenarioState>());
		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myFlightModule = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(
				new AlwaysCriteria<DefaultState>(), myFlightUpdater);

		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Difference in Rate \t Ground RBS \t Air RBS \t Ground RBD \t Air RBD");
		int range = 10;
		for (int i = -1 * range; i <= range; i++) {
			System.out.println(i);
			FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(
					new ShiftedBasicPAARChooser(i), new FixedDurationGDPIntervalChooser(Duration.standardDays(2)),
					new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
			StandardTmiPlanner myRBSModule = new StandardTmiPlanner(myTmiChooser,
					new FlightDurationFieldComparator(Flight.origETAFieldID));
			StandardTmiPlanner myRBDModule = new StandardTmiPlanner(myTmiChooser,
					new FlightDurationFieldComparator(Flight.flightTimeID).reversed());

			StateCriteria<DefaultState> myCriteria = AtStartCriteriaFactory.parse(startTime);

			ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myRBSPair = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myCriteria, myRBSModule);
			ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myRBDPair = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myCriteria, myRBDModule);
			List<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myRBSModules = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
			List<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myRBDModules = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
			myRBSModules.add(myRBSPair);
			myRBSModules.add(myFlightModule);
			myRBDModules.add(myRBDPair);
			myRBDModules.add(myFlightModule);

			SimulationEngineInstance<DefaultState> myRBDEngine = new SimulationEngineInstance<DefaultState>(
					myRBDModules, new AllLandedCriteria<DefaultState>(), myFlightHandler, myInit);
			DefaultState finalRBDState = SimulationEngineRunner.run(myRBDEngine, Duration.standardMinutes(1));
			SimulationEngineInstance<DefaultState> myRBSEngine = new SimulationEngineInstance<DefaultState>(
					myRBSModules, new AllLandedCriteria<DefaultState>(), myFlightHandler, myInit);
			DefaultState finalRBSState = SimulationEngineRunner.run(myRBSEngine, Duration.standardMinutes(1));
			double rbdGroundCost = MetricCalculator.calculateTotalDurationField(
					finalRBDState.getFlightState().getLandedFlights(), Flight.scheduledDelayID).getMillis() / 60000;
			double rbsGroundCost = MetricCalculator.calculateTotalDurationField(
					finalRBSState.getFlightState().getLandedFlights(), Flight.scheduledDelayID).getMillis() / 60000;
			double rbdAirCost = MetricCalculator.calculateTotalDurationField(
					finalRBDState.getFlightState().getLandedFlights(), Flight.airQueueDelayID).getMillis() / 60000;
			double rbsAirCost = MetricCalculator.calculateTotalDurationField(
					finalRBSState.getFlightState().getLandedFlights(), Flight.airQueueDelayID).getMillis() / 60000;
			myStream.println(i + "\t" + rbsGroundCost + "\t" + rbsAirCost + "\t" + rbdGroundCost + "\t" + rbdAirCost);

		}
		myStream.close();
	}

	@Test
	public void DynamicPriorityValidationTest() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/PriorityValidationTestB");
		// Initialize start time
		DateTime startTime = DateTimeFactory.parse(startTimeFile, ordTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(24)));

		// Initialize flight handler
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);

		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myFieldGenerators);
		FlightState myFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		// Create the initial state
		AirportState myAirportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		CapacityScenarioState myCapacity = CapacityScenarioFactory.parseLoToHigh(singleLoToHiFile, startTime);
		DefaultState myInit = new DefaultState(startTime, myFlightState, myAirportState, myCapacity);

		// Create the flight updater
		NASStateUpdate myNASStateUpdate = new NASStateUpdate(new ConstantRunwayDistribution());
		UpdateModule myFlightUpdater = new UpdateModule(myNASStateUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myFlightModule = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(
				new AlwaysCriteria<DefaultState>(), myFlightUpdater);

		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Difference in Rate \t Ground RBS \t Air RBS \t Ground RBD \t Air RBD");
		int range = 10;
		int num_trials = 1;
		for (int i = -1 * range; i <= range; i++) {
			System.out.println(i);
			double rbdGroundCost = 0.0;
			double rbsGroundCost = 0.0;
			double rbdAirCost = 0.0;
			double rbsAirCost = 0.0;
			for (int j = 0; j < num_trials; j++) {
				FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(
						new ShiftedBasicPAARChooser(i), new FixedDurationGDPIntervalChooser(Duration.standardDays(2)),
						new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
				StandardTmiPlanner myRBSModule = new StandardTmiPlanner(myTmiChooser,
						new FlightDurationFieldComparator(Flight.origETAFieldID));
				StandardTmiPlanner myRBDModule = new StandardTmiPlanner(myTmiChooser,
						new FlightDurationFieldComparator(Flight.flightTimeID).reversed());

				StateCriteria<DefaultState> myCriteria = StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
						new RateIncreaseCriteria<DefaultState>(60));

				ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myRBSPair = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myCriteria,
						myRBSModule);

				ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myRBDPair = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myCriteria,
						myRBDModule);

				List<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myRBSModules = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();

				List<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myRBDModules = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
				myRBSModules.add(myRBSPair);
				myRBSModules.add(myFlightModule);

				myRBDModules.add(myRBDPair);
				myRBDModules.add(myFlightModule);

				SimulationEngineInstance<DefaultState> myRBDEngine = new SimulationEngineInstance<DefaultState>(
						myRBDModules, new AllLandedCriteria<DefaultState>(), myFlightHandler, myInit);

				DefaultState finalRBDState = SimulationEngineRunner.run(myRBDEngine, Duration.standardMinutes(1));

				SimulationEngineInstance<DefaultState> myRBSEngine = new SimulationEngineInstance<DefaultState>(
						myRBSModules, new AllLandedCriteria<DefaultState>(), myFlightHandler, myInit);
				DefaultState finalRBSState = SimulationEngineRunner.run(myRBSEngine, Duration.standardMinutes(1));
				rbdGroundCost += MetricCalculator.calculateTotalDurationField(
						finalRBDState.getFlightState().getLandedFlights(), Flight.scheduledDelayID).getMillis() / 60000;
				rbsGroundCost += MetricCalculator.calculateTotalDurationField(
						finalRBSState.getFlightState().getLandedFlights(), Flight.scheduledDelayID).getMillis() / 60000;
				rbdAirCost += MetricCalculator.calculateTotalDurationField(
						finalRBDState.getFlightState().getLandedFlights(), Flight.airQueueDelayID).getMillis() / 60000;
				rbsAirCost += MetricCalculator.calculateTotalDurationField(
						finalRBSState.getFlightState().getLandedFlights(), Flight.airQueueDelayID).getMillis() / 60000;
			}
			myStream.println(i + "\t" + rbsGroundCost / num_trials + "\t" + rbsAirCost / num_trials + "\t"
					+ rbdGroundCost / num_trials + "\t" + rbdAirCost / num_trials);

		}
		myStream.close();
	}
}
