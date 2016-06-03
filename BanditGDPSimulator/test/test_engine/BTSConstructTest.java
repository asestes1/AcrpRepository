package test_engine;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import function_util.FunctionEx;
import gdp_planning.ConstantRadiusChooser;
import gdp_planning.FixedDurationGDPIntervalChooser;
import gdp_planning.GDPPlanningHelper;
import gdp_planning.IndParamPlanner;
import gdp_planning.ShiftedBasicPAARChooser;
import gdp_planning.StandardTmiPlanner;
import metrics.MetricCalculator;
import model.GdpAction;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import model.StateAction;
import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
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
import state_update.FlightDurationFieldComparator;
import state_update.FlightHandler;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update_factories.FlightHandlerFactory;
import util_random.ConstantRunwayDistribution;
import util_random.Distribution;
import util_random.UniformIntDistribution;

public class BTSConstructTest {
	private final File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
	private final File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	private final File btsFile = new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	private final File singleLoToHiFile = new File("TestFiles/ScenarioFiles/singleScenarioLoHi");
	public static final DateTimeZone ewrTimeZone = DateTimeZone.forID("America/New_York");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");


	@Test
	public void ORDTestCase() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/ORD_Run");
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		Interval runInterval = new Interval(startTime,startTime.plus(Duration.standardHours(24)));
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		HashMap<Integer,Distribution<Integer>> myFieldGenerators = new HashMap<Integer,Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval,ordTimeZone,FlightFactory.BTS_FORMAT_ID,myFieldGenerators);
		btsState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);
		List<Integer> demandCounts = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(
				btsState.getSittingFlights(), Duration.standardHours(1),
				new Interval(startTime, startTime.plus(Duration.standardDays(1))), Flight.origETAFieldID);
		PrintStream myStream = new PrintStream(outFile);

		/*
		 * Create the initial state FlightState myFlightState =
		 * myFlightFactory.parse(btsFile,startTime); AirportState myAirportState
		 * = new AirportStateFileFactory().parse(airportFile, startTime);
		 * BasicCapacityState myCapacity = new BasicCapacityState(40);
		 * DefaultState<BasicCapacityState> myInit = new
		 * DefaultState<BasicCapacityState>(myFlightState,myAirportState,
		 * myCapacity);
		 * 
		 * //Create the flight updater DefaultNASStateUpdate<BasicCapacityState>
		 * myNASStateUpdate = new DefaultNASStateUpdate<BasicCapacityState>(new
		 * ConstantRunwayDistribution());
		 * DefaultUpdateModule<BasicCapacityState> myFlightUpdater = new
		 * DefaultUpdateModule<BasicCapacityState>(myNASStateUpdate, new
		 * DoNothingModule<BasicCapacityState>());
		 * CriteriaActionPair<DefaultState<BasicCapacityState>> myFlightModule =
		 * new CriteriaActionPair<DefaultState<BasicCapacityState>>( new
		 * AlwaysCriteria<DefaultState<BasicCapacityState>>(), myFlightUpdater);
		 * 
		 * 
		 * myStream.println(demandCounts.toString()); myStream.println(
		 * "Difference in Rate \t Ground RBS \t Air RBS \t Ground RBD \t Air RBD"
		 * );
		 * 
		 * 
		 * 
		 * List<CriteriaActionPair<DefaultState<BasicCapacityState>>> myModules
		 * = new
		 * ArrayList<CriteriaActionPair<DefaultState<BasicCapacityState>>>();
		 * myModules.add(myFlightModule);
		 * 
		 * SimulationEngineCore<DefaultState<BasicCapacityState>> myEngine = new
		 * SimulationEngineCore<DefaultState<BasicCapacityState>> (myModules,
		 * new AllLandedCriteria<BasicCapacityState>(), startTime,
		 * myFlightHandler, myInit, Duration.standardMinutes(1));
		 * DefaultState<BasicCapacityState> finalState = myEngine.run();
		 * 
		 * double airCost =
		 * MetricCalculator.calculateTotalDurationField(finalState.
		 * getFlightState() .getLandedFlights(),
		 * Flight.airQueueDelayID).getMillis()/60000.0;
		 * 
		 * myStream.println("Total aircost: "+airCost); List<Duration>
		 * delaysByHour = MetricCalculator.aggregateFieldByTimePeriod(
		 * finalState.getFlightState().getLandedFlights(),
		 * Flight.origETAFieldID,Flight.airQueueDelayID, new
		 * Interval(startTime,startTime.plus(Duration.standardDays(1))),
		 * Duration.standardHours(1)); myStream.println(
		 * "Hour \t Demand \t Delays");
		 */
		for (int i = 0; i < demandCounts.size(); i++) {
			myStream.println(i + "\t" + demandCounts.get(i));
		}
		myStream.println(btsState.getSittingFlights().size());
		// myStream.println(finalState.toString());
		myStream.close();
	}

	@Test
	public void RationByBTS() throws Exception {
		File outFile = new File("TestOutputFiles/TestEngineOutput/ORD_RBX_Run");
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		Interval runInterval = new Interval(startTime,startTime.plus(Duration.standardHours(24)));

		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);
		HashMap<Integer,Distribution<Integer>> myFieldGenerators = new HashMap<Integer,Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval,ordTimeZone,FlightFactory.BTS_FORMAT_ID,myFieldGenerators);
		FlightState myFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);
		
		AirportState myAirportState =AirportStateFactory.parseAirportState(airportFile, startTime);
		CapacityScenarioState myCapacity = new CapacityScenarioState(40);
		DefaultState myInit = new DefaultState(startTime, myFlightState, myAirportState, myCapacity);


		FunctionEx<DefaultState, GdpAction, Exception> myChooser = new IndParamPlanner(new ShiftedBasicPAARChooser(0),new FixedDurationGDPIntervalChooser(Duration.standardDays(1)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		StandardTmiPlanner myRBDModule = new StandardTmiPlanner(myChooser,new FlightDurationFieldComparator(Flight.flightTimeID).reversed());
		StandardTmiPlanner myRBSModule = new StandardTmiPlanner(myChooser,new FlightDurationFieldComparator(Flight.origETAFieldID));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("RBD:");
		myStream.println(myRBDModule.act(myInit, myFlightHandler, Duration.standardMinutes(1)));
		myStream.println("RBS:");
		myStream.println(myRBSModule.act(myInit, myFlightHandler, Duration.standardMinutes(1)));
		myStream.close();
	}

	@Test
	public void testChangingCapacity() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/ORD_2Cap_Run");
		// Initialize start time
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		Interval runInterval = new Interval(startTime,startTime.plus(Duration.standardHours(24)));

		// Initialize flight handler
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);

		// Initialize flights
		HashMap<Integer,Distribution<Integer>> myFieldGenerators = new HashMap<Integer,Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval,ordTimeZone,FlightFactory.BTS_FORMAT_ID,myFieldGenerators);
		FlightState myFlightState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);
		
		AirportState myAirportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		CapacityScenarioState myCapacity = CapacityScenarioFactory.parseLoToHigh(singleLoToHiFile, startTime);
		DefaultState myInit = new DefaultState(startTime, myFlightState, myAirportState, myCapacity);

		// Create the flight updater
		NASStateUpdate myNASStateUpdate = new NASStateUpdate(new ConstantRunwayDistribution());
		UpdateModule myFlightUpdater = new UpdateModule(myNASStateUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));

		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myFlightModule = ImmutablePair.of(
				new AlwaysCriteria<DefaultState>(), myFlightUpdater);

		List<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myModules = 
				new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
		myModules.add(myFlightModule);

		SimulationEngineInstance<DefaultState> myEngine = new SimulationEngineInstance<DefaultState>(myModules,
				new AllLandedCriteria<DefaultState>(), myFlightHandler, myInit);
		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));

		double airCost = MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID)
				.getMillis() / 60000.0;

		PrintStream myStream = new PrintStream(outFile);

		myStream.println("Total aircost: " + airCost);
		List<Duration> delaysByHour = MetricCalculator.aggregateDurationMetricByTimeField(
				finalState.getFlightState().getLandedFlights(), Flight.origETAFieldID, Flight.airQueueDelayID,
				new Interval(startTime, startTime.plus(Duration.standardDays(1))), Duration.standardHours(1));
		myStream.println("Hour \t Demand \t Delays");
		for (int i = 0; i < delaysByHour.size(); i++) {
			myStream.println(i + "\t" + delaysByHour.get(i).getMillis() / 60000.0);
		}
		myStream.close();
	}

}