package test_response;

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

import airline_response.DefaultAirlineResponse;
import gdp_factories.GdpPlannerFactory;
import gdp_planning.StandardTmiPlanner;
import metrics.MetricCalculator;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import model.StateAction;
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
import util_random.UniformDurationDistribution;
import util_random.UniformIntDistribution;

public class TestDefaultResponse {
	private final File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
	private final File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	private final File capacityFile = new File("TestFiles/ScenarioFiles/testLoHiScenarioA");
	private final File hofkinFile = new File("TestFiles/GDPFiles/HofkinTestFileA");
	private final File btsFlights = new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");

	@Test
	public void validationTest() throws Exception {
		File outFile = new File("TestOutputFiles/ValidationTests/TestValidationReponseA");
		PrintStream myStream = new PrintStream(outFile);
		StandardTmiPlanner myHofkinPlanner = GdpPlannerFactory.parseHofkinPlanner(hofkinFile);
		DefaultAirlineResponse myDefault = new DefaultAirlineResponse("responseLog.log", 32.0, 0.1,
				Duration.standardMinutes(15));
		for (int i = 1; i <= 12; i++) {
			myResponseRun(30, myHofkinPlanner, "Hofkin model with response: " + i, myDefault,
					new UniformDurationDistribution(i, i + 2, Duration.standardHours(1)), myStream);
		}
		/*
		 * PAARBasedPlanner myFlatRichettaOdoniPlanner = new
		 * DefaultRichettaOdoniFileFactory().parse(roFile); myGDPPlannerRun(0,
		 * myFlatRichettaOdoniPlanner, "Richetta Odoni flat model: ", myStream);
		 * 
		 * PAARBasedPlanner myIncRichettaOdoniPlanner = new
		 * DefaultRichettaOdoniFileFactory().parse(ro1File); myGDPPlannerRun(0,
		 * myIncRichettaOdoniPlanner, "Richetta Odoni model 1: ", myStream);
		 * 
		 * PAARBasedPlanner myDecRichettaOdoniPlanner = new
		 * DefaultRichettaOdoniFileFactory().parse(ro2File); myGDPPlannerRun(0,
		 * myDecRichettaOdoniPlanner, "Richetta Odoni model 2: ", myStream);
		 */

		myStream.close();

	}

	private void myResponseRun(int numTrials, StandardTmiPlanner myPlanner, String description,
			DefaultAirlineResponse myResponse, Distribution<Duration> maxDelayDist, PrintStream myStream)
					throws Exception {

		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		UniformIntDistribution myDistribution = new UniformIntDistribution(100, 400);
		// Initialize start time
		DateTime startTime = DateTimeFactory.parse(startTimeFile, ordTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(24)));

		// Initialize the flight factory
		HashMap<Integer, Distribution<Integer>> myIntFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myIntFieldGenerators.put(Flight.numPassengersID, myDistribution);
		HashMap<Integer, Distribution<Duration>> myDurationFieldGenerators = new HashMap<Integer, Distribution<Duration>>();
		myDurationFieldGenerators.put(Flight.maxDelayID, maxDelayDist);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFlights, runInterval, ordTimeZone,
				FlightFactory.BTS_FORMAT_ID, myIntFieldGenerators, new HashMap<Integer, Distribution<DateTime>>(),
				myDurationFieldGenerators);
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
		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myFlightModule = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(
				new AlwaysCriteria<DefaultState>(), myFlightUpdater);

		StateCriteria<DefaultState> myGDPCriteria = AtStartCriteriaFactory.parse(startTime);

		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myGDPModule = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myGDPCriteria, myPlanner);

		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> myResponseModule = new ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>(myGDPCriteria,
				myResponse);

		List<ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>>> myModules = new ArrayList<ImmutablePair<StateCriteria<DefaultState>, StateAction<DefaultState>>>();
		myModules.add(myGDPModule);
		myModules.add(myResponseModule);
		myModules.add(myFlightModule);

		double maxDelay = 0.0;
		double passengerDelay = 0.0;
		double groundDelay = 0.0;
		double airDelay = 0.0;
		double averageCostFlat = 0.0;
		double numCancelled = 0.0;
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
			averageCostFlat += groundDelay + airDelay * 3;
			numCancelled += finalState.getFlightState().getCancelledFlights().size();
		}
		numCancelled = numCancelled / numTrials;
		maxDelay = maxDelay / numTrials;
		groundDelay = groundDelay / numTrials;
		airDelay = airDelay / numTrials;
		passengerDelay = passengerDelay / numTrials;
		averageCostFlat = averageCostFlat / numTrials;

		myStream.println(description);
		myStream.println("Average max delay: " + maxDelay);
		myStream.println("Average ground delay: " + groundDelay);
		myStream.println("Average passenger delay: " + passengerDelay);
		myStream.println("Average air delay: " + airDelay);
		myStream.println("Average flat cost: " + averageCostFlat);
		myStream.println("Average cancellations: " + numCancelled);
	}
}
