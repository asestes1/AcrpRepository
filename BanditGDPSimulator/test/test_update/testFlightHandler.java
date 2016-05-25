package test_update;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.junit.Test;

import junit.framework.TestCase;
import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.FlightHandler;
import state_update_factories.FlightHandlerFactory;

public class testFlightHandler extends TestCase{
	private final File flightsFile = 
			new File("TestFiles/FlightLists/testBasicFlightsA");
	private final File flightHandlerFile = 
			new File("TestFiles/FlightHandlerFiles/FlightHandlerTestA");
	
	@Test
	public void testAirHold() throws Exception{
		File outFile =
				new File("TestOutputFiles/TestUpdateOutput/FlightHandlerTestAirHoldAOut");
		DateTime currentTime = new DateTime(2011,12,12,10,03,DateTimeZone.UTC);
		Duration timeHeld = Duration.standardMinutes(5);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		FlightState myState = FlightStateFactory.parseFlightState(flightsFile, currentTime,DateTimeZone.UTC, FlightFactory.BASIC_FORMAT_ID);
		Iterator<Flight> myIterator = myState.getAirborneFlights().iterator();
		PrintStream outStream = new PrintStream(outFile);
		while(myIterator.hasNext()){
			outStream.println(myFlightHandler.airHold(myIterator.next(),timeHeld).toString());
		}
		outStream.close();
		
	}
	
	@Test
	public void testDepDelay() throws Exception{
		File outFile =
				new File("TestOutputFiles/TestUpdateOutput/FlightHandlerTestDepDelayAOut");
		DateTime currentTime = new DateTime(2011,12,12,10,03,DateTimeZone.UTC);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		FlightState myState = FlightStateFactory.parseFlightState(flightsFile, currentTime,DateTimeZone.UTC, FlightFactory.BASIC_FORMAT_ID);
		Iterator<Flight> myIterator = myState.getSittingFlights().iterator();
		PrintStream outStream = new PrintStream(outFile);
		while(myIterator.hasNext()){
			outStream.println(myFlightHandler.depDelay(myIterator.next()).toString());
		}
		outStream.close();
		
	}
	
	@Test
	public void testEnRouteDelay() throws Exception{
		File outFile =
				new File("TestOutputFiles/TestUpdateOutput/FlightHandlerTestEnRouteDelayAOut");
		DateTime currentTime = new DateTime(2011,12,12,10,03,DateTimeZone.UTC);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		FlightState myState = FlightStateFactory.parseFlightState(flightsFile, currentTime,DateTimeZone.UTC, FlightFactory.BASIC_FORMAT_ID);
		Iterator<Flight> myIterator = myState.getAirborneFlights().iterator();
		PrintStream outStream = new PrintStream(outFile);
		while(myIterator.hasNext()){
			outStream.println(myFlightHandler.enRouteDelay(myIterator.next()).toString());
		}
		outStream.close();
	}
	
	@Test
	public void testLand() throws Exception{
		File outFile =
				new File("TestOutputFiles/TestUpdateOutput/FlightHandlerTestLandAOut");
		DateTime currentTime = new DateTime(2011,12,12,10,03,DateTimeZone.UTC);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		FlightState myState = FlightStateFactory.parseFlightState(flightsFile, currentTime,DateTimeZone.UTC, FlightFactory.BASIC_FORMAT_ID);
		Iterator<Flight> myIterator = myState.getAirborneFlights().iterator();
		PrintStream outStream = new PrintStream(outFile);
		while(myIterator.hasNext()){
			outStream.println(myFlightHandler.land(myIterator.next()).toString());
		}
		outStream.close();	
	}
	
	@Test
	public void testTakeOff() throws Exception{
		File outFile =
				new File("TestOutputFiles/TestUpdateOutput/FlightHandlerTestTakeoffAOut");
		DateTime currentTime = new DateTime(2011,12,12,10,03,DateTimeZone.UTC);
		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		FlightState myState = FlightStateFactory.parseFlightState(flightsFile, currentTime,DateTimeZone.UTC, FlightFactory.BASIC_FORMAT_ID);
		Iterator<Flight> myIterator = myState.getSittingFlights().iterator();
		PrintStream outStream = new PrintStream(outFile);
		while(myIterator.hasNext()){
			outStream.println(myFlightHandler.takeOff(myIterator.next()).toString());
		}
		outStream.close();	
	}
}
