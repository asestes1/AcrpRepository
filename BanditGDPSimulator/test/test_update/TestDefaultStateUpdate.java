package test_update;

import java.io.File;
import java.io.PrintStream;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.junit.Test;

import model.DoNothingModule;
import state_factories.AirportStateFactory;
import state_factories.CapacityScenarioFactory;
import state_factories.DateTimeFactory;
import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.CapacityScenarioState;
import state_representation.DefaultState;
import state_representation.FlightState;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update.FlightHandler;
import state_update_factories.DefaultNasUpdateFactory;
import state_update_factories.FlightHandlerFactory;

public class TestDefaultStateUpdate {

	@Test
	public void testDefaultStateUpdate() throws Exception {
		File flightFile = new File("TestFiles/FlightLists/testBasicFlightsA");
		File airportFile = new File("TestFiles/AirportFiles/testAirportFileA");
		File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerTestA");
		File capacityFile = new File("TestFiles/CapacityFiles/testBasicCapacityA");
		File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
		File updateFile = new File("TestFiles/AirportUpdateFiles/DefaultNASStateUpdateTestA");
		File outFile = new File("TestOutputFiles/TestUpdateOutput/TestDefaultUpdateA");
		DateTime startTime = DateTimeFactory.parse(startTimeFile,DateTimeZone.UTC);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		
		FlightState initialFlightState = FlightStateFactory.parseFlightState(flightFile, startTime,DateTimeZone.UTC, FlightFactory.BASIC_FORMAT_ID);

		DefaultState myStart = new DefaultState(startTime, initialFlightState, AirportStateFactory.parseAirportState(airportFile, startTime),
				CapacityScenarioFactory.parseBasicState(capacityFile));

		NASStateUpdate myNASUpdate = DefaultNasUpdateFactory.parse(updateFile);

		// Create the default state update module
		UpdateModule myUpdate = new UpdateModule(myNASUpdate,
				new DoNothingModule<CapacityScenarioState>());

		PrintStream myOutStream = new PrintStream(outFile);
		DefaultState state1 = myUpdate.act(myStart, myFlightHandler, Duration.standardMinutes(1));
		myOutStream.println(state1.toString());
		DateTime currentTime = startTime;
		for (int i = 0; i < 10; i++) {
			myOutStream.println(currentTime.toString());
			currentTime = currentTime.plus(Duration.standardMinutes(1));
			state1 = myUpdate.act(state1, myFlightHandler, Duration.standardMinutes(1));
			myOutStream.println(state1);
		}

		myOutStream.close();
	}
}
