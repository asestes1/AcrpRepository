package state_factories;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import state_representation.Flight;
import state_representation.FlightState;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;
import util_random.ConstantRunwayDistribution;
import util_random.Distribution;

public final class FlightStateFactory {
	private FlightStateFactory(){
		
	}
	public static FlightState makeFullCapacityState(DateTime startTime,
			Duration duration, int capacity){
		return makeFullCapacityState(startTime, startTime.plus(duration), capacity);
	}
	
	public static FlightState makeFullCapacityState(DateTime startTime,
			DateTime endTime, int capacity ){
		ConstantRunwayDistribution runwayTime = new ConstantRunwayDistribution();
		DateTime nextArrival = startTime;
		SortedSet<Flight> arrivals = 
				new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETAFieldID));
		
		SortedSet<Flight> departures = 
				new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETDFieldID));
		//For the first hour, create flights which are in the air and which are separated
		//according to the capacity of the airport
		int flightTime = 0;
		while(nextArrival.isBefore(endTime)){
			flightTime = flightTime % 4 + 1;
			if(nextArrival.minusHours(flightTime).isBefore(startTime)){
				Flight nextFlight = new Flight("0", 200,100,
					nextArrival.minusHours(flightTime), nextArrival, null, null,
					nextArrival.minusHours(flightTime), nextArrival, true, false,false,false,
					Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
					arrivals.add(nextFlight);

			}else{
				Flight nextFlight = new Flight("0", 200,100,
						nextArrival.minusHours(flightTime), nextArrival, null, null,
						nextArrival.minusHours(flightTime), null, true, false,false,false,
						Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
				departures.add(nextFlight);
			}
			nextArrival = nextArrival.plus(runwayTime.sample(capacity));
		}
		
		FlightState myFlights = 
				new FlightState(new HashSet<Flight>(),arrivals,departures, new HashSet<Flight>());
		return myFlights;
	}
	
	/**
	 * This is an implementation of FlightInitializer
	 * This assumes that all flights that were scheduled to depart before the
	 * current time left on time. Flights which haven't departed yet are given
	 * departure delays
	 *
	 */
	public static FlightState parseFullCapacityState(File input,DateTime startTime)
			throws Exception {
		Scanner myScanner = new Scanner(input);
		return parseFullCapacityState(myScanner,startTime);
	}
	
	public static FlightState parseFullCapacityState(Scanner scanner,DateTime startTime)
			throws Exception {
		String input = "";
		while(scanner.hasNext() && input.isEmpty()){
			input = scanner.nextLine();
		}
		Duration duration = Duration.standardMinutes(Integer.parseInt(input.trim()));
		int capacity = Integer.parseInt(scanner.nextLine());
		return makeFullCapacityState(startTime,duration, capacity);
	}
	
	public static FlightState parseFlightState(File file,DateTime currentTime,DateTimeZone timeZone,int formatId) throws Exception{
		return parseFlightState(file, currentTime, timeZone,formatId, new HashMap<Integer,Distribution<Integer>>(),
				new HashMap<Integer,Distribution<DateTime>>(),new HashMap<Integer,Distribution<Duration>>());
	}
	
	public static FlightState parseFlightState(File file,DateTime currentTime,DateTimeZone timeZone,int formatId,
			HashMap<Integer,Distribution<Integer>> intFieldGenerators) throws Exception{
		return parseFlightState(file, currentTime, timeZone,formatId,  intFieldGenerators,
				new HashMap<Integer,Distribution<DateTime>>(),new HashMap<Integer,Distribution<Duration>>());
	}
	
	public static FlightState parseFlightState(File file,DateTime currentTime,DateTimeZone timeZone,
			int formatId,
			HashMap<Integer,Distribution<Integer>> intFieldGenerators,
			HashMap<Integer,Distribution<DateTime>> dateTimeGenerators,
			HashMap<Integer,Distribution<Duration>> durationGenerators) throws Exception{
		Reader in = new FileReader(file);
		CSVFormat myFormat = null;
		if(formatId == FlightFactory.BTS_FORMAT_ID){
			myFormat = CSVFormat.EXCEL.withHeader();
		}else if(formatId == FlightFactory.BASIC_FORMAT_ID){
			myFormat = CSVFormat.EXCEL;
		}else{
			in.close();
			throw new IllegalArgumentException("Invalid argument for file format id.");
		}
		CSVParser parser = new CSVParser(in, myFormat);
		
		Set<Flight> landedFlights = new HashSet<Flight>();
		SortedSet<Flight> airborneFlights = 
				new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETAFieldID));
		SortedSet<Flight> sittingFlights =
				new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETDFieldID));
	
		for(CSVRecord record: parser){
			//Read the string and make a flight
			Flight nextFlight = FlightFactory.parseFlight(record, currentTime,timeZone,formatId);
			Iterator<Integer> myFieldIds = intFieldGenerators.keySet().iterator();
			while(myFieldIds.hasNext()){
				Integer nextFieldId = myFieldIds.next();
				nextFlight = nextFlight.setIntField(nextFieldId, intFieldGenerators.get(nextFieldId).sample());
			}
			myFieldIds = dateTimeGenerators.keySet().iterator();
			while(myFieldIds.hasNext()){
				Integer nextFieldId = myFieldIds.next();
				nextFlight = nextFlight.setDateTimeField(nextFieldId, dateTimeGenerators.get(nextFieldId).sample());
			}
			myFieldIds = durationGenerators.keySet().iterator();
			while(myFieldIds.hasNext()){
				Integer nextFieldId = myFieldIds.next();
				nextFlight = nextFlight.setDurationField(nextFieldId, durationGenerators.get(nextFieldId).sample());
			}
			if(nextFlight.getOrigETD().isBefore(nextFlight.getOrigETA())){
				if(nextFlight.isLanded()){
					landedFlights.add(nextFlight);
				}else if(nextFlight.isAirborne()){
					airborneFlights.add(nextFlight);
				}else{
					sittingFlights.add(nextFlight);
				}

			}
		}
		in.close();
		parser.close();
		return new FlightState(landedFlights, airborneFlights, sittingFlights, new HashSet<Flight>());
	}
	
	public static FlightState delaySittingFlights(FlightHandler myFlightHandler, FlightState flightState){
		SortedSet<Flight> myNewSittingFlights = new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETDFieldID));
		Iterator<Flight> myFlightIter = flightState.getSittingFlights().iterator();
		while(myFlightIter.hasNext()){
			myNewSittingFlights.add(myFlightHandler.depDelay(myFlightIter.next()));
		}
		return flightState.setSittingFlights(myNewSittingFlights);
	}
	
	public static FlightState parseBtsFlightFile(String btsDirName,String filePrefix, LocalDate date, DateTime startTime,
			DateTimeZone timeZone,int formatId) throws Exception {
		String fileName =btsDirName+"/"+date.getYear()+"/"+date.getMonthOfYear()+"/";
		fileName += filePrefix+"_"+date.getYear()+"_"+date.getMonthOfYear()+"_"+date.getDayOfMonth()+".csv";
		File myFile = new File(fileName);
		return FlightStateFactory.parseFlightState(myFile, startTime, timeZone, formatId);
	}
	
	public static FlightState parseAdlFlightFile(String adlDirName,String filePrefix, LocalDate date, DateTime startTime, DateTimeZone timeZone,
			int formatId) throws Exception {
		String monthString = ""+date.getMonthOfYear();
		if(monthString.length() == 1){
			monthString = "0"+monthString;
		}
		String dayString = ""+date.getDayOfMonth();
		if(dayString.length() == 1){
			dayString = "0"+dayString;
		}
		String fileName =adlDirName+"/"+date.getYear()+"/"+date.getMonthOfYear()+"/";
		fileName += filePrefix+date.getYear()+monthString+dayString+".csv";
		File myFile = new File(fileName);
		return FlightStateFactory.parseFlightState(myFile, startTime, timeZone, formatId);
	}
	
}
