package dhoffkin_run_tests;

import gdp_factories.GdpPlannerFactory;
import gdp_planning.DirectExtendedHofkinModel;
import gdp_planning.DirectHofkinModel;
import gdp_planning.DirectMHDynModel;
import gdp_planning.GDPPlanningHelper;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import metrics.MetricCalculator;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import model.StateAction;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
import state_criteria.AtStartCriteriaFactory;
import state_criteria.PeriodicCriteria;
import state_criteria.StateCriteria;
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
import state_update.DefaultFlightHandler;
import state_update.FlightHandler;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update_factories.FlightHandlerFactory;
import util_random.ConstantDistribution;
import util_random.ConstantRunwayDistribution;
import util_random.Distribution;
import util_random.UniformIntDistribution;

public class DHoffkinTests {
	private final File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
	//private final File flightFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
	private final File btsFile = new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	//private final File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	//private final File capacityFile = new File("TestFiles/ScenarioFiles/testLoHiScenarioA");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");

	// private final File capacityFile = new
	// File("TestFiles/ScenarioFiles/singleScenarioLoHi");
	//private final File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
	private final File hofkinFile = new File("TestFiles/GDPFiles/HofkinTestFileA");
	//private final File roFile = new File("TestFiles/GDPFiles/RichettaOdoniTestA");
	//private final File ro1File = new File("TestFiles/GDPFiles/RichettaOdoniTestB");
	// private final File logFile = new File("TestFiles/GDPFiles/logFile");

	// private final File btsFlights = new
	// File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");

	@Test 
	public void ORDTest() throws Exception {
		//Parameters
		File myFlightFile = btsFile;
		DateTime startTime = DateTimeFactory.parse("2013/6/8 6:00",ordTimeZone);
		System.out.println(startTime);
		DateTime endTime = startTime.plus(Duration.standardHours(24));
		Distribution<Duration> my_departure_delay_dist = new ConstantDistribution<Duration>(Duration.ZERO);
		Distribution<Duration> my_arrival_delay_dist = new ConstantDistribution<Duration>(Duration.ZERO);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		StateAction<DefaultState> myPlanner = GdpPlannerFactory.parseDirectExtendedHofkinModel(hofkinFile);
		File outputfile = new File("TestOutputFiles/TestGDPSolvers/TestDHofkinORD40To60");
		int numTrials = 50;
		
		Interval runInterval = new Interval(startTime, endTime);
		FlightHandler myFlightHandler = new DefaultFlightHandler(my_departure_delay_dist,my_arrival_delay_dist);

		//Read flights from BTS file
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(myFlightFile, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myFieldGenerators);
		
		//Create initial state.
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		
		AirportState initialAirportState = new AirportState(startTime);


		// Create the flight updater
		NASStateUpdate myNASStateUpdate = new NASStateUpdate(new ConstantRunwayDistribution());
		UpdateModule myFlightUpdater = new UpdateModule(myNASStateUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		// Create the criteria-action pairs
		ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>> myFlightModule = ImmutablePair.of(
				new AlwaysCriteria<DefaultState>(), myFlightUpdater);

		StateCriteria<DefaultState> myGDPCriteria = new PeriodicCriteria<DefaultState>(Duration.standardHours(1));

		ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>> myGDPModule = new ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>>(
				myGDPCriteria, myPlanner);

		List<ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>>> myModules = new ArrayList<ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>>>();
		myModules.add(myGDPModule);
		myModules.add(myFlightModule);
		double maxDelay = 0.0;
		double passengerDelay = 0.0;
		double groundDelay = 0.0;
		double totalGroundDelay = 0.0;
		double totalAirDelay = 0.0;
		double airDelay = 0.0;
		double averageCostFlat = 0.0;
		for (int i = 0; i < numTrials; i++) {
			CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
					Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10),40, 60);
			DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
					initialCapacityState);
			SimulationEngineInstance<DefaultState> myEngine = new SimulationEngineInstance<DefaultState>(myModules,
					new AllLandedCriteria<DefaultState>(), myFlightHandler, myInitialState);
			DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
			maxDelay += MetricCalculator
					.calculateMaxDurationField(finalState.getFlightState().getLandedFlights(), Flight.scheduledDelayID)
					.getMillis() / 60000;
			passengerDelay += MetricCalculator
					.calculateTotalPassengerDurationField(finalState.getFlightState().getLandedFlights(),
							Flight.scheduledDelayID)
					.getMillis() / 60000
					+ MetricCalculator.calculateTotalPassengerDurationField(
							finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID).getMillis() / 60000;
			groundDelay = MetricCalculator.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(),
					Flight.scheduledDelayID).getMillis() / 60000;
			airDelay = MetricCalculator
					.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID)
					.getMillis() / 60000;
			totalGroundDelay += groundDelay;
			totalAirDelay += airDelay;
			averageCostFlat += groundDelay + airDelay * 2;
		}
		maxDelay = maxDelay / numTrials;
		totalGroundDelay = totalGroundDelay / numTrials;
		totalAirDelay = totalAirDelay / numTrials;
		passengerDelay = passengerDelay / numTrials;
		averageCostFlat = averageCostFlat / numTrials;

		PrintStream myStream = new PrintStream(outputfile);
		myStream.println("50 trials run at ORD, Extended Hofkin model: ");
		myStream.println("Average max delay: " + maxDelay);
		myStream.println("Average ground delay: " + totalGroundDelay);
		myStream.println("Average passenger delay: " + passengerDelay);
		myStream.println("Average air delay: " + totalAirDelay);
		myStream.println("Average flat cost: " + averageCostFlat);
		myStream.close();

	}
	
	@Test
	public void testDirectExtendedHofkin() throws Exception {
		// File outFile = new
		// File("TestOutputFiles/TestGDPSolvers/TestExtendedHofkinA");
		DateTime startTime = DateTimeFactory.parse(startTimeFile, ordTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(24)));

		// DateTime endTime = startTime.plus(Duration.standardHours(6));
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		// FlightState initialFlightState = new
		// FullCapacityFactory().makeInitialState(startTime, endTime, 60);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myFieldGenerators);
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
				Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10), 40, 60);

		AirportState initialAirportState = new AirportState(startTime);
		DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
				initialCapacityState);

		// StateCriteria<DefaultState> myHofkinCriteria =
		// StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
		// new RateIncreaseCriteria(60));
		DirectExtendedHofkinModel myHofkinModel = GdpPlannerFactory.parseDirectExtendedHofkinModel(hofkinFile);
		// ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>
		// myHofkinModule= new
		// ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myHofkinCriteria,myHofkinModel);

		DefaultState nextState = myHofkinModel.act(myInitialState, myFlightHandler, Duration.standardMinutes(1));
		Set<Flight> sittingFlights = nextState.getFlightState().getSittingFlights();
		List<Integer> flightsByFlightTime = GDPPlanningHelper.aggregateFlightCountsByDurationField(sittingFlights,
				Flight.flightTimeID, Duration.standardHours(0), Duration.standardHours(10),
				Duration.standardMinutes(30));
		List<Duration> delaysByFlightTime = MetricCalculator.aggregateDurationMetricByDurationField(sittingFlights,
				Flight.flightTimeID, Flight.scheduledDelayID, Duration.standardHours(0), Duration.standardHours(10),
				Duration.standardMinutes(30));

		System.out.println("Total delays by flight time: ");
		for (int i = 0; i < delaysByFlightTime.size(); i++) {
			if (flightsByFlightTime.get(i) > 0) {
				System.out.println(
						i + ": " + delaysByFlightTime.get(i).getStandardSeconds()/3600);
			} else {
				System.out.println(i + ": 0");

			}
		}
	}

	@Test
	public void testDirectMHDyn() throws Exception {
		// File outFile = new File("TestOutputFiles/TestGDPSolvers/TestMHDynA");
		DateTime startTime = DateTimeFactory.parse(startTimeFile, ordTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(24)));

		// DateTime endTime = startTime.plus(Duration.standardHours(6));
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		// FlightState initialFlightState = new
		// FullCapacityFactory().makeInitialState(startTime, endTime, 60);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myFieldGenerators);
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
				Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10), 40, 60);

		AirportState initialAirportState = new AirportState(startTime);
		DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
				initialCapacityState);
		;

		// StateCriteria<DefaultState> myMHCriteria =
		// StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
		// new RateIncreaseCriteria(60));
		DirectMHDynModel myMHModel = GdpPlannerFactory.parseDirectMHDynModel(hofkinFile);
		// ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>
		// myMHModule = new
		// ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myMHCriteria,
		// myMHModel);

		DefaultState nextState = myMHModel.act(myInitialState, myFlightHandler, Duration.standardMinutes(1));
		Set<Flight> sittingFlights = nextState.getFlightState().getSittingFlights();
		List<Integer> flightsByFlightTime = GDPPlanningHelper.aggregateFlightCountsByDurationField(sittingFlights,
				Flight.flightTimeID, Duration.standardHours(0), Duration.standardHours(10),
				Duration.standardMinutes(30));
		List<Duration> delaysByFlightTime = MetricCalculator.aggregateDurationMetricByDurationField(sittingFlights,
				Flight.flightTimeID, Flight.scheduledDelayID, Duration.standardHours(0), Duration.standardHours(10),
				Duration.standardMinutes(30));

		System.out.println("Average delays by flight time: ");
		for (int i = 0; i < delaysByFlightTime.size(); i++) {
			if (flightsByFlightTime.get(i) > 0) {
				System.out.println(
						i + ": " + delaysByFlightTime.get(i).getStandardMinutes() / flightsByFlightTime.get(i));
			} else {
				System.out.println(i + ": 0");

			}
		}

		System.out.println("Delays by flight time: ");
		for (int i = 0; i < delaysByFlightTime.size(); i++) {
			if (flightsByFlightTime.get(i) > 0) {
				System.out.println(i + ": " + delaysByFlightTime.get(i).getStandardMinutes() / 60.0);
			} else {
				System.out.println(i + ": 0");

			}
		}
	}

	@Test
	public void testDirectHofkin() throws Exception {
		// File outFile = new
		// File("TestOutputFiles/TestGDPSolvers/TestHofkinB");
		DateTime startTime = DateTimeFactory.parse(startTimeFile, ordTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(24)));

		// DateTime endTime = startTime.plus(Duration.standardHours(6));
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		// FlightState initialFlightState = new
		// FullCapacityFactory().makeInitialState(startTime, endTime, 60);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myFieldGenerators);
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
				Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10),40, 60);

		AirportState initialAirportState = new AirportState(startTime);
		DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
				initialCapacityState);
		;

		// StateCriteria<DefaultState> myHofkinCriteria =
		// StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
		// new RateIncreaseCriteria(60));
		DirectHofkinModel myHofkinModel = GdpPlannerFactory.parseDirectHofkinModel(hofkinFile);

		// ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>
		// myHofkinModule = new
		// ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myHofkinCriteria,
		// myHofkinModel);

		DefaultState nextState = myHofkinModel.act(myInitialState, myFlightHandler, Duration.standardMinutes(1));
		Set<Flight> sittingFlights = nextState.getFlightState().getSittingFlights();
		List<Integer> flightsByFlightTime = GDPPlanningHelper.aggregateFlightCountsByDurationField(sittingFlights,
				Flight.flightTimeID, Duration.standardHours(0), Duration.standardHours(10),
				Duration.standardMinutes(30));
		List<Duration> delaysByFlightTime = MetricCalculator.aggregateDurationMetricByDurationField(sittingFlights,
				Flight.flightTimeID, Flight.scheduledDelayID, Duration.standardHours(0), Duration.standardHours(10),
				Duration.standardMinutes(30));
		System.out.println("Average: ");
		for (int i = 0; i < delaysByFlightTime.size(); i++) {
			if (flightsByFlightTime.get(i) > 0) {
				System.out.println(
						i + ": " + delaysByFlightTime.get(i).getStandardMinutes() / flightsByFlightTime.get(i));
			} else {
				System.out.println(i + ": 0");

			}
		}

		System.out.println("Total: ");
		for (int i = 0; i < delaysByFlightTime.size(); i++) {
			if (flightsByFlightTime.get(i) > 0) {
				System.out.println(i + ": " + delaysByFlightTime.get(i).getStandardSeconds() / 3600.0);
			} else {
				System.out.println(i + ": 0");

			}
		}
		/*
		 * DefaultNASStateUpdate myNASUpdate = new
		 * DefaultNASStateUpdateFileFactory().parse(updateFile);
		 * DefaultUpdateModule myUpdate = new DefaultUpdateModule(myNASUpdate,
		 * new DefaultCapacityScenarioUpdate(new DefaultCapacityComparer()));
		 * ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>
		 * myUpdateModule = new
		 * ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(
		 * new AlwaysCriteria<DefaultState>(), myUpdate);
		 * 
		 * List<ImmutablePair<StateCriteria<DefaultState>,StateAction<
		 * DefaultState>>> modules = new
		 * ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<
		 * DefaultState>>>(); modules.add(myHofkinModule);
		 * modules.add(myUpdateModule);
		 * 
		 * StateCriteria<DefaultState> myEndCriteria = new AllLandedCriteria();
		 * 
		 * 
		 * List<CapacityScenario> myScenarios =
		 * initialCapacityState.getScenarios(); Duration totalAir =
		 * Duration.ZERO; Duration totalGroundDelay = Duration.ZERO;
		 * Iterator<CapacityScenario> myScenarioIter = myScenarios.iterator();
		 * while(myScenarioIter.hasNext()){ myInitialState =
		 * myInitialState.setCapacity(
		 * initialCapacityState.setActualScenario(myScenarioIter.next()));
		 * SimulationEngineCore<DefaultState> myEngine = new
		 * SimulationEngineCore<DefaultState>(modules, myEndCriteria, startTime,
		 * myFlightHandler, myInitialState, Duration.standardMinutes(1));
		 * DefaultState finalState = myEngine.run(); totalAir =
		 * totalAir.plus(MetricCalculator.calculateTotalDurationField(finalState
		 * .getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		 * totalGroundDelay =
		 * totalGroundDelay.plus(MetricCalculator.calculateTotalDurationField(
		 * finalState.getFlightState().getLandedFlights(),
		 * Flight.scheduledDelayID)); } PrintStream myStream = new
		 * PrintStream(outFile); int numTrials = myScenarios.size();
		 * myStream.println("Total airqueue delay: "+(double)
		 * totalAir.getStandardHours()/numTrials); myStream.println(
		 * "Total scheduled delay: "+(double)
		 * totalGroundDelay.getStandardHours()/numTrials); myStream.close();
		 */
	}

}
