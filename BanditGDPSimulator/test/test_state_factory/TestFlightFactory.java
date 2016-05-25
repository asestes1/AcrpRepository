package test_state_factory;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.FlightHandler;
import state_update_factories.FlightHandlerFactory;
import util_random.Distribution;
import util_random.UniformIntDistribution;

public class TestFlightFactory {
	private final File flightsFile = 
			new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	private final File outFile =
			new File("TestOutputFiles/TestUpdateOutput/TestFlightFactoryOutA");
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");

	@Test
	public void testFlightFactory() throws Exception{
		DateTime currentTime = new DateTime(2013,6,8,6,0,ordTimeZone);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		HashMap<Integer, Distribution<Integer>> myFieldGenerators = new HashMap<Integer, Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, new UniformIntDistribution(100, 400));
		FlightState btsState = FlightStateFactory.parseFlightState(flightsFile, currentTime,ordTimeZone, FlightFactory.BTS_FORMAT_ID,
				myFieldGenerators);
		FlightState myState = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);
				PrintStream outStream = new PrintStream(outFile);
		outStream.println(myState.toString());
		outStream.close();	
	}
}
