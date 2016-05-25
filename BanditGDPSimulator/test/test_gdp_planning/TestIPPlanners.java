package test_gdp_planning;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.junit.Test;

import engine_factories.SimulationEngineFactory;
import gdp_factories.GdpPlannerFactory;
import gdp_planning.DirectExtendedHofkinModel;
import gdp_planning.DirectHofkinModel;
import gdp_planning.DirectMHDynModel;
import gdp_planning.GDPPlanningHelper;
import gdp_planning.StandardTmiPlanner;
import metrics.MetricCalculator;
import metrics.PiecewiseLinearFunction;
import model.CriteriaActionPair;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
import state_criteria.AtStartCriteriaFactory;
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
import state_update.FlightHandler;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update_factories.FlightHandlerFactory;
import util_random.ConstantRunwayDistribution;
import util_random.Distribution;
import util_random.UniformIntDistribution;

public class TestIPPlanners {
	private final File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
	private final File flightFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
	private final File btsFile = new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	private final File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	private final File capacityFile = new File("TestFiles/ScenarioFiles/testLoHiScenarioA");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");

	// private final File capacityFile = new
	// File("TestFiles/ScenarioFiles/singleScenarioLoHi");
	private final File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
	private final File hofkinFile = new File("TestFiles/GDPFiles/HofkinTestFileA");
	private final File roFile = new File("TestFiles/GDPFiles/RichettaOdoniTestA");
	private final File ro1File = new File("TestFiles/GDPFiles/RichettaOdoniTestB");
	// private final File logFile = new File("TestFiles/GDPFiles/logFile");

	// private final File btsFlights = new
	// File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");

	@Test
	public void testDirectExtendedHofkin() throws Exception {
		// File outFile = new
		// File("TestOutputFiles/TestGDPSolvers/TestExtendedHofkinA");
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		// DateTime endTime = startTime.plus(Duration.standardHours(6));
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		// FlightState initialFlightState = new
		// FullCapacityFactory().makeInitialState(startTime, endTime, 60);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, startTime, ordTimeZone,FlightFactory.BTS_FORMAT_ID,
				myFieldGenerators);
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
				Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10), 20, 60);

		AirportState initialAirportState = new AirportState(startTime);
		DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
				initialCapacityState);

		// StateCriteria<DefaultState> myHofkinCriteria =
		// StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
		// new RateIncreaseCriteria(60));
		DirectExtendedHofkinModel myHofkinModel = GdpPlannerFactory.parseDirectExtendedHofkinModel(hofkinFile);
		// CriteriaActionPair<DefaultState> myHofkinModule= new
		// CriteriaActionPair<DefaultState>(myHofkinCriteria,myHofkinModel);

		DefaultState nextState = myHofkinModel.act(myInitialState, myFlightHandler, Duration.standardMinutes(1));
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
		/*
		 * DefaultNASStateUpdate myNASUpdate = new
		 * DefaultNASStateUpdateFileFactory().parse(updateFile);
		 * DefaultUpdateModule myUpdate = new DefaultUpdateModule(myNASUpdate,
		 * new DefaultCapacityScenarioUpdate(new DefaultCapacityComparer()));
		 * CriteriaActionPair<DefaultState> myUpdateModule = new
		 * CriteriaActionPair<DefaultState>( new AlwaysCriteria<DefaultState>(),
		 * myUpdate);
		 * 
		 * List<CriteriaActionPair<DefaultState>> modules = new
		 * ArrayList<CriteriaActionPair<DefaultState>>();
		 * modules.add(myHofkinModule); modules.add(myUpdateModule);
		 * 
		 * StateCriteria<DefaultState> myEndCriteria = new AllLandedCriteria();
		 * 
		 * 
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

	@Test
	public void testDirectMHDyn() throws Exception {
		// File outFile = new File("TestOutputFiles/TestGDPSolvers/TestMHDynA");
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		// DateTime endTime = startTime.plus(Duration.standardHours(6));
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		// FlightState initialFlightState = new
		// FullCapacityFactory().makeInitialState(startTime, endTime, 60);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, startTime, ordTimeZone,FlightFactory.BTS_FORMAT_ID,
				myFieldGenerators);
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
				Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10), 20, 60);

		AirportState initialAirportState = new AirportState(startTime);
		DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
				initialCapacityState);
		;

		// StateCriteria<DefaultState> myMHCriteria =
		// StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
		// new RateIncreaseCriteria(60));
		DirectMHDynModel myMHModel = GdpPlannerFactory.parseDirectMHDynModel(hofkinFile);
		// CriteriaActionPair<DefaultState> myMHModule = new
		// CriteriaActionPair<DefaultState>(myMHCriteria, myMHModel);

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
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		// DateTime endTime = startTime.plus(Duration.standardHours(6));
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		// FlightState initialFlightState = new
		// FullCapacityFactory().makeInitialState(startTime, endTime, 60);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		// Initialize flights
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, startTime,ordTimeZone, FlightFactory.BTS_FORMAT_ID,
				myFieldGenerators);
		FlightState initialFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		CapacityScenarioState initialCapacityState = CapacityScenarioFactory.parseLoToHigh(startTime,
				Duration.standardHours(4), Duration.standardHours(9), Duration.standardMinutes(10), 20, 60);

		AirportState initialAirportState = new AirportState(startTime);
		DefaultState myInitialState = new DefaultState(startTime, initialFlightState, initialAirportState,
				initialCapacityState);
		;

		// StateCriteria<DefaultState> myHofkinCriteria =
		// StateCriteria.or(AtStartCriteriaFactory.parse(startTime),
		// new RateIncreaseCriteria(60));
		DirectHofkinModel myHofkinModel = GdpPlannerFactory.parseDirectHofkinModel(hofkinFile);

		// CriteriaActionPair<DefaultState> myHofkinModule = new
		// CriteriaActionPair<DefaultState>(myHofkinCriteria,
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
				System.out.println(i + ": " + delaysByFlightTime.get(i).getStandardMinutes() / 60.0);
			} else {
				System.out.println(i + ": 0");

			}
		}
		/*
		 * DefaultNASStateUpdate myNASUpdate = new
		 * DefaultNASStateUpdateFileFactory().parse(updateFile);
		 * DefaultUpdateModule myUpdate = new DefaultUpdateModule(myNASUpdate,
		 * new DefaultCapacityScenarioUpdate(new DefaultCapacityComparer()));
		 * CriteriaActionPair<DefaultState> myUpdateModule = new
		 * CriteriaActionPair<DefaultState>( new AlwaysCriteria<DefaultState>(),
		 * myUpdate);
		 * 
		 * List<CriteriaActionPair<DefaultState>> modules = new
		 * ArrayList<CriteriaActionPair<DefaultState>>();
		 * modules.add(myHofkinModule); modules.add(myUpdateModule);
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

	@Test
	public void testHofkin() throws Exception {
		File outFile = new File("TestOutputFiles/TestGDPSolvers/TestHofkinA");

		SimulationEngineInstance<DefaultState> myEngine = SimulationEngineFactory.makeSimulationInstance(startTimeFile,
				flightHandlerFile, SimulationEngineFactory.FULL_CAPACITY, flightFile,
				SimulationEngineFactory.LO_TO_HIGH_SCENARIO, capacityFile, airportFile, updateFile,
				SimulationEngineFactory.PAAR_HOFKIN, hofkinFile,ordTimeZone);

		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println("Total scheduled delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.scheduledDelayID));
		myStream.println(finalState.toString());
		myStream.close();
	}

	@Test
	public void testRO() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/TestROA");

		SimulationEngineInstance<DefaultState> myEngine = SimulationEngineFactory.makeSimulationInstance(startTimeFile,
				flightHandlerFile, SimulationEngineFactory.FULL_CAPACITY, flightFile,
				SimulationEngineFactory.LO_TO_HIGH_SCENARIO, capacityFile, airportFile, updateFile,
				SimulationEngineFactory.PAAR_RO, roFile,ordTimeZone);

		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println("Total scheduled delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.scheduledDelayID));
		myStream.println(finalState.toString());
		myStream.close();
	}

	@Test
	public void validationTests() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/TestValidationAOther");
		PrintStream myStream = new PrintStream(outFile);
		StandardTmiPlanner myHofkinPlanner = GdpPlannerFactory.parseHofkinPlanner(hofkinFile);
		myGDPPlannerRun(30, myHofkinPlanner, "Hofkin model: ", myStream);
		StandardTmiPlanner myFlatRichettaOdoniPlanner = GdpPlannerFactory.parseROPlanner(roFile);
		myGDPPlannerRun(30, myFlatRichettaOdoniPlanner, "Richetta Odoni flat model: ", myStream);

		StandardTmiPlanner myIncRichettaOdoniPlanner = GdpPlannerFactory.parseROPlanner(ro1File);
		myGDPPlannerRun(30, myIncRichettaOdoniPlanner, "Richetta Odoni model 1: ", myStream);

		myStream.close();
	}

	private void myGDPPlannerRun(int numTrials, StandardTmiPlanner myPlanner, String description, PrintStream myStream)
			throws Exception {
		SortedMap<Double, Double> myFunctionMap1 = new TreeMap<Double, Double>();
		myFunctionMap1.put(0.0, 1.0);
		myFunctionMap1.put(30.0, 4.0);
		myFunctionMap1.put(60.0, 5.0);
		PiecewiseLinearFunction myFunction1 = new PiecewiseLinearFunction(myFunctionMap1, 0.0);

		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		UniformIntDistribution myDistribution = new UniformIntDistribution(100, 400);
		// Initialize start time
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);

		// Initialize the flight factory
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, startTime,ordTimeZone, FlightFactory.BTS_FORMAT_ID,
				myFieldGenerators);
		FlightState myFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);

		// Create the initial state
		AirportState myAirportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		CapacityScenarioState myCapacity = CapacityScenarioFactory.parseLoToHigh(capacityFile, startTime);
		DefaultState myInit = new DefaultState(startTime, myFlightState, myAirportState, myCapacity);

		// Create the flight updater
		NASStateUpdate myNASStateUpdate = new NASStateUpdate(new ConstantRunwayDistribution());
		UpdateModule myFlightUpdater = new UpdateModule(myNASStateUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		// Create the criteria-action pairs
		CriteriaActionPair<DefaultState> myFlightModule = new CriteriaActionPair<DefaultState>(
				new AlwaysCriteria<DefaultState>(), myFlightUpdater);

		StateCriteria<DefaultState> myGDPCriteria = AtStartCriteriaFactory.parse(startTime);

		CriteriaActionPair<DefaultState> myGDPModule = new CriteriaActionPair<DefaultState>(myGDPCriteria, myPlanner);

		List<CriteriaActionPair<DefaultState>> myModules = new ArrayList<CriteriaActionPair<DefaultState>>();
		myModules.add(myGDPModule);
		myModules.add(myFlightModule);
		double maxDelay = 0.0;
		double passengerDelay = 0.0;
		double groundDelay = 0.0;
		double totalGroundDelay = 0.0;
		double totalAirDelay = 0.0;
		double airDelay = 0.0;
		double averageCostFlat = 0.0;
		double averageCost1 = 0.0;
		double averageCost2 = 0.0;
		for (int i = 0; i < numTrials; i++) {

			SimulationEngineInstance<DefaultState> myEngine = new SimulationEngineInstance<DefaultState>(myModules,
					new AllLandedCriteria<DefaultState>(), myFlightHandler, myInit);
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
			averageCostFlat += groundDelay + airDelay * 3;
			averageCost1 += MetricCalculator.calculateFunctionOfField(finalState.getFlightState().getLandedFlights(),
					myFunction1, Flight.scheduledDelayID) + airDelay * 3;
		}
		maxDelay = maxDelay / numTrials;
		totalGroundDelay = totalGroundDelay / numTrials;
		totalAirDelay = totalAirDelay / numTrials;
		passengerDelay = passengerDelay / numTrials;
		averageCostFlat = averageCostFlat / numTrials;
		averageCost1 = averageCost1 / numTrials;
		averageCost2 = averageCost2 / numTrials;

		myStream.println(description);
		myStream.println("Average max delay: " + maxDelay);
		myStream.println("Average ground delay: " + totalGroundDelay);
		myStream.println("Average passenger delay: " + passengerDelay);
		myStream.println("Average air delay: " + totalAirDelay);
		myStream.println("Average flat cost: " + averageCostFlat);
		myStream.println("Average cost 1: " + averageCost1);
		myStream.println("Average cost 2: " + averageCost2);
	}
}
