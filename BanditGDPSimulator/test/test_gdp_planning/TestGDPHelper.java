package test_gdp_planning;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import gdp_planning.DiscreteScenarioUtilities;
import gdp_planning.GDPPlanningHelper;
import state_factories.CapacityScenarioFactory;
import state_factories.DateTimeFactory;
import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.CapacityScenario;
import state_representation.CapacityScenarioState;
import state_representation.DefaultCapacityComparer;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;
import state_update_factories.FlightHandlerFactory;
import util_random.Distribution;
import util_random.UniformIntDistribution;

public class TestGDPHelper {
	private final File flightHandlerFile = new File("TestFiles/FlightHandlerFiles/FlightHandlerNoDelaysTestA");
	private final File btsFile = new File("TestFiles/FlightLists/BTS_ORD_6_8_2013.csv");
	private final File startTimeFile = new File("TestFiles/TimeFiles/timeFileA");
	public static final DateTimeZone ordTimeZone = DateTimeZone.forID("America/Chicago");


	@Test
	public void testMakeScenarioTree(){
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",ordTimeZone);
		CapacityScenarioState scenarioState = 
				CapacityScenarioFactory.parseLoToHigh(startTime, Duration.standardHours(0),
						Duration.standardHours(24),Duration.standardMinutes(30), 40, 60);
		List<CapacityScenario> scenarios = scenarioState.getScenarios();
		Interval gdpInterval = new Interval(startTime,startTime.plus(Duration.standardHours(4)));
		List<Set<Set<Integer>>> myTree = DiscreteScenarioUtilities.buildDiscreteScenarioTree(scenarios,
				new DefaultCapacityComparer(), Duration.standardMinutes(30), gdpInterval);
		for(int i =0; i < scenarios.size();i++){
			System.out.println(i+": "+scenarios.get(i).toString());
		}
		for(int i =0; i < myTree.size();i++){
			System.out.println(i+": "+myTree.get(i));
		}
	}
	@Test
	public void testInitializeSlotList(){
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",ordTimeZone);
		DateTime endTime = startTime.plus(Duration.standardHours(3));
		SortedMap<DateTime,Integer> paars = new TreeMap<DateTime,Integer>();
		paars.put(startTime, 58);
		SortedSet<DateTime> slots = GDPPlanningHelper.initializeSlotList(paars,new Interval(startTime,endTime));
		
		Iterator<DateTime> myIter = slots.iterator();
		System.out.println("Slot allocation test: ");

		while(myIter.hasNext()){
			System.out.println(myIter.next().toString());
		}
	}
	
	@Test public void testAssignSlots() throws Exception{
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",ordTimeZone);
		DateTime endTime = startTime.plus(Duration.standardHours(3));
		SortedMap<DateTime,Integer> paars = new TreeMap<DateTime,Integer>();
		FlightState myFlights = 
				FlightStateFactory.makeFullCapacityState(startTime,Duration.standardHours(5),60);
		paars.put(startTime, 58);
		SortedSet<DateTime> slots = GDPPlanningHelper.initializeSlotList(paars,new Interval(startTime,endTime));
		slots = GDPPlanningHelper.assignSlots(myFlights.getAirborneFlights(),slots,new Interval(startTime,endTime) );
		Iterator<DateTime> myIter = slots.iterator();
		System.out.println("Removing airborne test: ");
		while(myIter.hasNext()){
			System.out.println(myIter.next().toString());
		}
	}
	
	@Test public void testGetSlotList() throws Exception{
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",ordTimeZone);
		DateTime endTime = startTime.plus(Duration.standardHours(3));
		SortedMap<DateTime,Integer> paars = new TreeMap<DateTime,Integer>();
		FlightState myFlights = 
				FlightStateFactory.makeFullCapacityState(startTime,Duration.standardHours(5),60);
		paars.put(startTime, 58);
		SortedSet<DateTime> slots = GDPPlanningHelper.getSlotList(myFlights, paars, new Interval(startTime,endTime) );
		Iterator<DateTime> myIter = slots.iterator();
		System.out.println("Get slots test: ");
		while(myIter.hasNext()){
			System.out.println(myIter.next().toString());
		}
	}
	
	@Test
	public void testAggregateFlightDemandCounts() throws Exception{
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",ordTimeZone);
		DateTime endTime = startTime.plus(Duration.standardHours(5));
		FlightState myFlights = 
				FlightStateFactory.makeFullCapacityState(startTime,Duration.standardHours(10),60);
		List<Integer> myList = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(
				myFlights.getSittingFlights(),Duration.standardMinutes(15),
				new Interval(startTime,endTime),Flight.depETAFieldID); 
		System.out.println("Aggregate demand test: ");
		for(int i =0; i < myList.size();i++){
			System.out.println(myList.get(i));
		}
	}
	
	@Test
	public void testAggregateFlights() throws Exception{
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",ordTimeZone);
		DateTime endTime = startTime.plus(Duration.standardHours(5));
		FlightState myFlights = 
				FlightStateFactory.makeFullCapacityState(startTime,Duration.standardHours(10),60);
		System.out.println(myFlights.toString());
		List<Set<Flight>> myList = GDPPlanningHelper.aggregateFlightsByFlightTimeField(
				myFlights.getSittingFlights(),Duration.standardMinutes(15),
				new Interval(startTime,endTime),Flight.origETAFieldID); 
		System.out.println("Aggregate flights test: ");
		for(int i =0; i < myList.size();i++){
			System.out.println("Interval: "+startTime.plus(Duration.standardMinutes(i*15)).toString());
			List<Flight> currentSet = new ArrayList<Flight>(myList.get(i));
			Collections.sort(currentSet,new FlightDateTimeFieldComparator(Flight.origETAFieldID));
			Iterator<Flight> myIter = currentSet.iterator();
			while(myIter.hasNext()){
				Flight nextFlight = myIter.next();
				System.out.println(nextFlight);

			}
		}
	}
	
	@Test
	public void testAggregateFlightsDuration() throws Exception{
		DateTime startTime = DateTimeFactory.parse(startTimeFile,ordTimeZone);
		Interval runInterval = new Interval(startTime,startTime.plus(Duration.standardHours(24)));

		FlightHandler myFlightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);
		UniformIntDistribution myPassengerDistribution = new UniformIntDistribution(100, 400);

		HashMap<Integer,Distribution<Integer>> myFieldGenerators = new HashMap<Integer,Distribution<Integer>>();
		myFieldGenerators.put(Flight.numPassengersID, myPassengerDistribution);
		FlightState btsState = FlightStateFactory.parseFlightState(btsFile, runInterval,ordTimeZone,FlightFactory.BTS_FORMAT_ID,myFieldGenerators);
		FlightState myFlights = FlightStateFactory.delaySittingFlights(myFlightHandler, btsState);
		
		List<Set<Flight>> myList = GDPPlanningHelper.aggregateFlightsByDurationField(
				myFlights.getSittingFlights(),Flight.flightTimeID,Duration.standardMinutes(0),Duration.standardMinutes(360),
				Duration.standardMinutes(1));
		System.out.println("Aggregate flights test: ");
		for(int i =0; i < myList.size();i++){
			System.out.println("Interval: "+i);
			List<Flight> currentSet = new ArrayList<Flight>(myList.get(i));
			Collections.sort(currentSet,new FlightDateTimeFieldComparator(Flight.origETAFieldID));
			Iterator<Flight> myIter = currentSet.iterator();
			while(myIter.hasNext()){
				Flight nextFlight = myIter.next();
				System.out.println(nextFlight);

			}
		}
	}
}
