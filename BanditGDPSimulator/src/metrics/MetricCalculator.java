package metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import state_representation.Flight;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightDurationFieldComparator;

/**
 * This is a class which calculates metrics from a set of flights
 * @author Alex2
 *
 */
public class MetricCalculator {
	
	private MetricCalculator(){
		
	}
	
	/**
	 * This sums a field for all flights where the field is of type Duration.
	 * @param flightList
	 * @param fieldID
	 * @return
	 * @throws Exception
	 */
	public static Duration calculateTotalDurationField(Set<Flight> flightList, int fieldID)
			throws Exception {
		Duration totalDuration = Duration.ZERO;

		Iterator<Flight> iter = flightList.iterator();
		while (iter.hasNext()) {
			totalDuration = totalDuration.plus(iter.next().getDurationField(fieldID));
		}
		return totalDuration;
	}
	
	/**
	 * This sums a field for all flights where the field is of type Duration.
	 * @param flightList
	 * @param fieldID
	 * @return
	 * @throws Exception
	 */
	public static Duration calculateTotalPassengerDurationField(Set<Flight> flightList, int fieldID)
			throws Exception {
		Duration totalDuration = Duration.ZERO;

		Iterator<Flight> iter = flightList.iterator();
		while (iter.hasNext()) {
			Flight nextFlight = iter.next();
			totalDuration = totalDuration.plus(nextFlight.getDurationField(fieldID)
					.multipliedBy(nextFlight.getNumPassengers()));
		}
		return totalDuration;
	}
	
	public static Duration calculateAverageDurationField(Set<Flight> flightList, int fieldID)
			throws Exception{
		int numFlights = flightList.size();
		return calculateTotalDurationField(flightList, fieldID).dividedBy(numFlights);
	}
	
	public static Duration calculateMaxDurationField(Set<Flight> flightList, int fieldID)
	throws Exception{
		Duration maxDuration = Duration.ZERO;

		Iterator<Flight> iter = flightList.iterator();
		while (iter.hasNext()) {
			Duration nextDuration = iter.next().getDurationField(fieldID);
			if(nextDuration.isLongerThan(maxDuration)){
				maxDuration = nextDuration;
			}
		}
		return maxDuration;
	}
	
	public static double calculateFunctionOfField(Set<Flight> flightList, PiecewiseLinearFunction function, int fieldID) throws Exception{
		Double total = 0.0;

		Iterator<Flight> iter = flightList.iterator();
		while (iter.hasNext()) {
			Duration nextDuration = iter.next().getDurationField(fieldID);
			total += function.evaluateAt( nextDuration.getMillis()/(double)Duration.standardMinutes(1).getMillis()); 
		}
		return total;
	}
	
	public static List<Duration> aggregateDurationMetricByDurationField(Set<Flight> flights, int fieldID,
			int metricID,
			Duration shortestDuration,
			Duration longestDuration,
			Duration discretizedPeriodDuration)
			throws Exception{
		//Initialize first discrete time period
		Duration currentShortest = shortestDuration; 
		Duration currentLongest = currentShortest.plus(discretizedPeriodDuration);
		//Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(
				flights);
		//Sort the list by the field
		Collections.sort(sortedFlights,
				new FlightDurationFieldComparator(fieldID));
		
		//We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		Duration currentCount = Duration.ZERO;
		List<Duration> myList = new ArrayList<Duration>();
		while(done == false && myIterator.hasNext()){
			//Get the field from the next flight;
			Flight nextFlight = myIterator.next();
			Duration flightDurationField = nextFlight.getDurationField(fieldID);
			//If the flight is in the current time period, add it to the count.
			if(!flightDurationField.isShorterThan(currentShortest) && flightDurationField.isShorterThan(currentLongest)){
				currentCount = currentCount.plus(nextFlight.getDurationField(metricID));
			//If the flight is not in the current time period, but is in the larger interval,
			//add the count to the list, then find the next time period.
			}else if(!flightDurationField.isShorterThan(currentLongest) &&
					flightDurationField.isShorterThan(longestDuration)){
				myList.add(currentCount);
				currentCount = nextFlight.getDurationField(metricID);
				currentShortest = currentShortest.plus(discretizedPeriodDuration);
				currentLongest = currentLongest.plus(discretizedPeriodDuration);
				//If the flight doesn't fall in the next time period, keep going
				while(!flightDurationField.isShorterThan(currentLongest)){
					currentShortest = currentShortest.plus(discretizedPeriodDuration);
					currentLongest = currentLongest.plus(discretizedPeriodDuration);
					myList.add(Duration.ZERO);
				}
			//If the flight is outside the interval, we are done.
			}else if(!flightDurationField.isShorterThan(longestDuration)){
				done = true;
			}
		}
		//If we run out of flights, add zeroes to the end.
		myList.add(currentCount);
		while(currentLongest.isShorterThan(longestDuration)){
			currentShortest = currentShortest.plus(discretizedPeriodDuration);
			currentLongest = currentLongest.plus(discretizedPeriodDuration);
			myList.add(Duration.ZERO);
		}
		return myList;
	}
	
	public static List<Duration> aggregateDurationMetricByTimeField(Set<Flight> flights, int fieldID,
			int metricID,
			Interval timeInterval,
			Duration discretizedPeriodDuration)
			throws Exception{
		DateTime end = timeInterval.getEnd();
		//Initialize first discrete time period
		DateTime currentPeriodStart = timeInterval.getStart();
		DateTime currentPeriodEnd = currentPeriodStart.plus(discretizedPeriodDuration);
		//Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(
				flights);
		//Sort the list by the field
		Collections.sort(sortedFlights,
				new FlightDateTimeFieldComparator(fieldID));
		
		//We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		Duration currentCount = Duration.ZERO;
		List<Duration> myList = new ArrayList<Duration>();
		while(done == false && myIterator.hasNext()){
			//Get the field from the next flight;
			Flight nextFlight = myIterator.next();
			DateTime oETA = nextFlight.getDateTimeField(fieldID);
			//If the flight is in the current time period, add it to the count.
			if(!currentPeriodStart.isAfter(oETA) && oETA.isBefore(currentPeriodEnd)){
				currentCount = currentCount.plus(nextFlight.getDurationField(metricID));
			//If the flight is not in the current time period, but is in the larger interval,
			//add the count to the list, then find the next time period.
			}else if(!oETA.isBefore(currentPeriodEnd) && oETA.isBefore(end)){
				myList.add(currentCount);
				currentCount = nextFlight.getDurationField(metricID);
				currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
				currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
				//If the flight doesn't fall in the next time period, keep going
				while(!currentPeriodEnd.isAfter(oETA)){
					currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
					currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
					myList.add(Duration.ZERO);
				}
			//If the flight is outside the interval, we are done.
			}else if(!oETA.isBefore(end)){
				done = true;
			}
		}
		//If we run out of flights, add zeroes to the end.
		myList.add(currentCount);
		while(currentPeriodEnd.isBefore(end)){
			currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
			currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
			myList.add(Duration.ZERO);
		}
		return myList;
	}
	
}
