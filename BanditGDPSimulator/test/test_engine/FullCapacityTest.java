package test_engine;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.junit.Test;

import metrics.MetricCalculator;
import model.DoNothingModule;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import model.StateAction;
import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
import state_criteria.StateCriteria;
import state_factories.AirportStateFactory;
import state_factories.DateTimeFactory;
import state_factories.FlightStateFactory;
import state_representation.AirportState;
import state_representation.CapacityScenarioState;
import state_representation.DefaultState;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.DefaultFlightHandler;
import state_update.FlightHandler;
import state_update.UpdateModule;
import state_update_factories.DefaultNasUpdateFactory;
import state_update_factories.FlightHandlerFactory;
import util_random.ConstantDistribution;
import util_random.UniformDurationDistribution;

public class FullCapacityTest {

	@Test
	public void FullCapacityFactoryTest() throws Exception {
		File timeFile = new File("TestFiles/TimeFiles/timeFileA");
		File initFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
		File outFile = new File("TestOutputFiles/TestFactoryOutput/FullCapacityTestFactoryOut1");
		DateTime startTime = DateTimeFactory.parse(timeFile,DateTimeZone.UTC);

		FlightState init = FlightStateFactory.parseFullCapacityState(initFile, startTime);
		PrintStream myStream = new PrintStream(outFile);
		myStream.println(init.toString());
		myStream.close();
	}

	@Test
	public void FullCapacityNoDelayTest() throws Exception {
		File timeFile = new File("TestFiles/TimeFiles/timeFileA");
		File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
		File flightFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
		File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");

		File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
		File outFile = new File("TestOutputFiles/TestEngineOutput/FullCapacityTestOut1");

		DateTime startTime = DateTimeFactory.parse(timeFile,DateTimeZone.UTC);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);

		AirportState airportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		FlightState flightState = FlightStateFactory.parseFullCapacityState(flightFile, startTime);
		CapacityScenarioState capacityState = new CapacityScenarioState(60);

		DefaultState init = new DefaultState(startTime, flightState, airportState, capacityState);

		UpdateModule myUpdateModule = new UpdateModule(DefaultNasUpdateFactory.parse(updateFile),
				new DoNothingModule<CapacityScenarioState>());
		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> flightAction = ImmutablePair.of(
				new AlwaysCriteria<DefaultState>(), myUpdateModule);
		ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myList = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
		myList.add(flightAction);
		SimulationEngineInstance<DefaultState> myEngine = new SimulationEngineInstance<DefaultState>(myList,
				new AllLandedCriteria<DefaultState>(), myFlightHandler, init);
		DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Total airqueue delay: " + MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID));
		myStream.println(finalState.toString());
		myStream.close();
	}

	@Test
	public void FullCapacityDelaysTest() throws Exception {
		File timeFile = new File("TestFiles/TimeFiles/timeFileA");
		File flightFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
		File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
		File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
		File outFile = new File("TestOutputFiles/ValidationTests/FullCapacityDelaysTestOut1");
		DateTime startTime = DateTimeFactory.parse(timeFile,DateTimeZone.UTC);

		// Create initial state
		AirportState airportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		FlightState flightState = FlightStateFactory.parseFullCapacityState(flightFile, startTime);
		CapacityScenarioState capacityState = new CapacityScenarioState(60);

		DefaultState init = new DefaultState(startTime, flightState, airportState, capacityState);
		;

		int num_trials = 30;
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Variance \t Average Air Delay");
		for (int i = 1; i <= 30; i++) {
			double air_queue_delay = 0;
			for (int j = 0; j < num_trials; j++) {
				DefaultFlightHandler myFlightHandler = new DefaultFlightHandler(
						new ConstantDistribution<Duration>(Duration.ZERO),
						new UniformDurationDistribution(-1 * i, i, Duration.standardMinutes(1)));
				UpdateModule myUpdateModule = new UpdateModule(DefaultNasUpdateFactory.parse(updateFile),
						new DoNothingModule<CapacityScenarioState>());

				ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> flightAction = ImmutablePair.of(
						new AlwaysCriteria<DefaultState>(), myUpdateModule);

				ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myList = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
				myList.add(flightAction);

				SimulationEngineInstance<DefaultState> myEngine = new SimulationEngineInstance<DefaultState>(myList,
						new AllLandedCriteria<DefaultState>(), myFlightHandler, init);
				DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
				air_queue_delay += MetricCalculator.calculateTotalDurationField(
						finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID).getMillis() / 60000;

			}
			double variance = (i * i) / 3.0;
			myStream.println(variance + "\t" + air_queue_delay / num_trials);
			System.out.println(i);
			System.out.println(variance);
		}
		myStream.close();
	}

	@Test
	public void CrunchCapacityNoDelayTest() throws Exception {
		File timeFile = new File("TestFiles/TimeFiles/timeFileA");
		File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
		File flightFile = new File("TestFiles/StateInitializationFiles/FullCapacityTest1");
		File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
		File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
		File outFile = new File("TestOutputFiles/ValidationTests/Engine_Test_Vary_Cap");
		int range = 10;
		PrintStream myStream = new PrintStream(outFile);
		myStream.println("Difference in demand and capacity: ");
		for (int i = -1 * range; i <= range; i++) {
			DateTime startTime = DateTimeFactory.parse(timeFile,DateTimeZone.UTC);
			FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
			AirportState airportState = AirportStateFactory.parseAirportState(airportFile, startTime);
			FlightState flightState = FlightStateFactory.parseFullCapacityState(flightFile, startTime);
			CapacityScenarioState capacityState = new CapacityScenarioState(60 + i);

			DefaultState init = new DefaultState(startTime, flightState, airportState, capacityState);
			;
			UpdateModule myUpdateModule = new UpdateModule(DefaultNasUpdateFactory.parse(updateFile),
					new DoNothingModule<CapacityScenarioState>());

			ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> flightAction = ImmutablePair.of(
					new AlwaysCriteria<DefaultState>(), myUpdateModule);

			ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> myList = new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();
			myList.add(flightAction);

			SimulationEngineInstance<DefaultState> myEngine = new SimulationEngineInstance<DefaultState>(myList,
					new AllLandedCriteria<DefaultState>(), myFlightHandler, init);
			DefaultState finalState = SimulationEngineRunner.run(myEngine, Duration.standardMinutes(1));
			myStream.println(i + "\t" + MetricCalculator
					.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.airQueueDelayID)
					.getMillis() / 60000.0);
		}
		myStream.close();

	}
}
