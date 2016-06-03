package gdp_planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import bandit_objects.SimpleTmiAction;
import model.GdpAction;
import state_representation.DefaultState;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightDurationFieldComparator;
import state_update.FlightHandler;

/**
 * This class provides functions that might be useful for gdp planning modules
 * 
 * @author Alex2
 *
 */
public final class GDPPlanningHelper {
	public static final int dayHourStart = 4;

	private GDPPlanningHelper() {

	}

	public static List<Integer> aggregateFlightCountsByDurationField(Set<Flight> flights, int fieldID,
			Duration shortestDuration, Duration longestDuration, Duration discretizedPeriodDuration) throws Exception {
		// Initialize first discrete time period
		Duration currentShortest = shortestDuration;
		Duration currentLongest = currentShortest.plus(discretizedPeriodDuration);
		// Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(flights);
		// Sort the list by the field
		Collections.sort(sortedFlights, new FlightDurationFieldComparator(fieldID));

		// We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		int currentCount = 0;
		List<Integer> myList = new ArrayList<Integer>();
		while (done == false && myIterator.hasNext()) {
			// Get the field from the next flight;
			Flight nextFlight = myIterator.next();
			Duration flightDurationField = nextFlight.getDurationField(fieldID);
			// If the flight is in the current time period, add it to the count.
			if (!flightDurationField.isShorterThan(currentShortest)
					&& flightDurationField.isShorterThan(currentLongest)) {
				currentCount++;
				// If the flight is not in the current time period, but is in
				// the larger interval,
				// add the count to the list, then find the next time period.
			} else if (!flightDurationField.isShorterThan(currentLongest)
					&& flightDurationField.isShorterThan(longestDuration)) {
				myList.add(currentCount);
				currentCount = 1;
				currentShortest = currentShortest.plus(discretizedPeriodDuration);
				currentLongest = currentLongest.plus(discretizedPeriodDuration);
				// If the flight doesn't fall in the next time period, keep
				// going
				while (!flightDurationField.isShorterThan(currentLongest)) {
					currentShortest = currentShortest.plus(discretizedPeriodDuration);
					currentLongest = currentLongest.plus(discretizedPeriodDuration);
					myList.add(0);
				}
				// If the flight is outside the interval, we are done.
			} else if (!flightDurationField.isShorterThan(longestDuration)) {
				done = true;
			}
		}
		// If we run out of flights, add zeroes to the end.
		myList.add(currentCount);
		while (currentLongest.isShorterThan(longestDuration)) {
			currentShortest = currentShortest.plus(discretizedPeriodDuration);
			currentLongest = currentLongest.plus(discretizedPeriodDuration);
			myList.add(0);
		}
		return myList;
	}

	/**
	 * This function takes a set of flights, some PAARs, and the GDP planning
	 * interval and creates a list of available arrival slots for flights which
	 * have not departed yet. This function differs from initializeSlotList in
	 * that en route flights are given slots.
	 * 
	 * @param flights
	 *            - the flights
	 * @param paars
	 *            - the PAARs. This is stored as a map between times and
	 *            integers.
	 * @param gdpInterval
	 *            - the GDP planning interval
	 * @return - a set of available arrival slots
	 * @throws Exception
	 *             - an exception is thrown if there are not enough available
	 *             slots to accommodate all of the sitting flights.
	 */
	public static SortedSet<DateTime> getSlotList(FlightState flights, SortedMap<DateTime, Integer> paars,
			Interval gdpInterval) throws Exception {

		return getSlotList(flights, paars, gdpInterval, Double.POSITIVE_INFINITY);
	}

	/**
	 * This function takes a set of flights, some PAARs, and the GDP planning
	 * interval and creates a list of available arrival slots for flights which
	 * have not departed yet. This function differs from initializeSlotList in
	 * that en route flights are given slots.
	 * 
	 * @param flights
	 *            - the flights
	 * @param paars
	 *            - the PAARs. This is stored as a map between times and
	 *            integers.
	 * @param gdpInterval
	 *            - the GDP planning interval
	 * @return - a set of available arrival slots
	 * @throws Exception
	 *             - an exception is thrown if there are not enough available
	 *             slots to accommodate all of the sitting flights.
	 */
	public static SortedSet<DateTime> getSlotList(FlightState flights, SortedMap<DateTime, Integer> paars,
			Interval gdpInterval, Double radius) throws Exception {
		Set<Flight> exemptFlights = flights.getAirborneFlights();
		Set<Flight> sittingFlights = flights.getSittingFlights();
		for (Flight f : sittingFlights) {
			if (f.getDistance() > radius) {
				exemptFlights.add(f);
			}
		}
		return assignSlots(exemptFlights, initializeSlotList(paars, gdpInterval), gdpInterval);
	}

	/**
	 * This takes a set of chosen PAARs and a GDP and creates a list of
	 * available slots. This function differs from getSlotList in that slots are
	 * not allocated to en route flights
	 * 
	 * @param paars
	 *            - the PAARs, in the form of a sorted map.
	 * @param gdpInterval
	 *            - the GDP planning horizon interval
	 * @return - the set of slots, sorted by time.
	 */
	public static SortedSet<DateTime> initializeSlotList(SortedMap<DateTime, Integer> paars, Interval gdpInterval) {
		// We start at the start time of the GDP
		DateTime startTime = gdpInterval.getStart();
		DateTime endTime = gdpInterval.getEnd();

		// We find the capacity at the start of the GDP in the most likely
		// scenario
		int currentCap;
		if (paars.containsKey(startTime)) {
			currentCap = paars.get(startTime);
		} else {
			currentCap = paars.get(paars.headMap(startTime).lastKey());
		}
		// Allocate a sorted set to store our slots
		SortedSet<DateTime> slotList = new TreeSet<DateTime>();
		// Get an iterator which will find the times at which the capacity
		// changes
		Iterator<DateTime> iter = paars.tailMap(startTime).keySet().iterator();

		// Now we build the slot list. We do this in intervals, where the
		// capacity is constant
		// on that interval
		DateTime intervalStart = startTime;
		DateTime intervalEnd = startTime;

		// Check to see if the capacity changes again
		while (iter.hasNext() && intervalEnd.compareTo(endTime) < 0) {
			intervalEnd = iter.next();
			if (intervalEnd.compareTo(endTime) < 0) {
				// Find the number of slots between now and the next capacity
				// change
				int numSlots = ((int) new Duration(intervalStart, intervalEnd).getStandardMinutes() * currentCap) / 60;
				// Add these slots to the list
				for (long i = 0; i < numSlots; i++) {
					Double timeToLand = 60 * 60 * 1000.0 / currentCap;
					slotList.add(intervalStart.plus(Duration.millis(i * timeToLand.intValue())));
				}

				// Move the starting point
				intervalStart = intervalEnd;
				// Get the capacity for the next interval
				currentCap = paars.get(intervalStart);
			}
		}

		// Find the number of slots between the final capacity change and the
		// end
		int numSlots = ((int) new Duration(intervalStart, endTime).getStandardMinutes() * currentCap) / 60;
		// Add these slots to the list
		for (long i = 0; i < numSlots; i++) {
			Double timeToLand = 60 * 60 * 1000.0 / currentCap;
			slotList.add(intervalStart.plus(Duration.millis(i * timeToLand.intValue())));
		}
		slotList.add(endTime);
		return slotList;
	}

	/**
	 * This takes a set of available slots and removes slots for airborne
	 * flights.
	 * 
	 * @param flights
	 *            - a set of flights. Any non-airborne flights will be ignored.
	 * @param slotList
	 *            - the list of available slots
	 * @param gdpTimes
	 *            - the time interval of interest
	 * @return - the set of remaining slots after airborne en-route flights are
	 *         given slots.
	 * @throws Exception
	 */
	public static SortedSet<DateTime> assignSlots(Set<Flight> flights, SortedSet<DateTime> slotList, Interval gdpTimes)
			throws Exception {
		// Get start time and end time
		DateTime start = gdpTimes.getStart();
		DateTime end = gdpTimes.getEnd();

		// Get the exempt flights
		LinkedList<Flight> exemptFlights = new LinkedList<Flight>(flights);
		Collections.sort(exemptFlights, new FlightDateTimeFieldComparator(Flight.origETAFieldID));
		Iterator<Flight> airborneFlightIter = exemptFlights.iterator();
		boolean done = false;

		// We stop once we have looked at all of the flights, or we reach a
		// flight whose original arrival time is beyond the GDP planning period.
		while (airborneFlightIter.hasNext() && done == false) {
			// Get the next flight
			Flight nextArrival = airborneFlightIter.next();
			// If the arrival time is beyond the GDP planning period, we're done
			if (nextArrival.isAirborne()) {
				if (nextArrival.getOrigETA().compareTo(end) > 0) {
					done = true;
					// If the arrival time is within the GDP planning period,
					// need
					// to give the flight a slot
					// Slots are assigned based on the ETA given the flight's
					// departure
				} else
					if (nextArrival.getDepETA().compareTo(start) >= 0 && nextArrival.getDepETA().compareTo(end) < 0) {
					SortedSet<DateTime> tailSet = slotList.tailSet(nextArrival.getDepETA());
					if (!tailSet.isEmpty()) {
						slotList.remove(tailSet.first());
					}
				}
			}
		}
		return slotList;
	}

	/**
	 * This takes a set of flights, a time interval, and a duration. The time
	 * interval will be broken into periods whose length is that of the given
	 * duration. This function will find the number of flights such that a given
	 * DateTime field of that flight falls into each discretized time period.
	 * For example, this function could be used to find the number of flights
	 * with original arrival time falling in each hour of given time interval.
	 * 
	 * @param flights
	 *            - the set of flights
	 * @param discretizedPeriodDuration
	 *            - the length of discretized time period
	 * @param timeInterval
	 *            - the interval
	 * @param fieldID
	 *            - the ID of the field of interest.
	 * @return - a list, where the ith entry is the number of flights whose
	 *         given field falls into the ith time period of the time interval.
	 * @throws Exception
	 */
	public static List<Integer> aggregateFlightCountsByFlightTimeField(Set<Flight> flights,
			Duration discretizedPeriodDuration, Interval timeInterval, DateTime currentTime, int fieldID)
					throws Exception {

		DateTime end = timeInterval.getEnd();
		// Initialize first discrete time period
		DateTime currentPeriodStart = timeInterval.getStart();
		DateTime currentPeriodEnd = currentPeriodStart.plus(discretizedPeriodDuration);
		// Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(flights);
		// Sort the list by the field
		Collections.sort(sortedFlights, new FlightDateTimeFieldComparator(fieldID));

		// We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		int currentCount = 0;
		List<Integer> myList = new ArrayList<Integer>();
		while (done == false && myIterator.hasNext()) {
			// Get the field from the next flight;
			DateTime oETA = myIterator.next().getDateTimeField(fieldID, currentTime);
			// If the flight is in the current time period, add it to the count.
			if (!currentPeriodStart.isAfter(oETA) && oETA.isBefore(currentPeriodEnd)) {
				currentCount++;
				// If the flight is not in the current time period, but is in
				// the larger interval,
				// add the count to the list, then find the next time period.
			} else if (!oETA.isBefore(currentPeriodEnd) && oETA.isBefore(end)) {
				myList.add(currentCount);
				currentCount = 1;
				currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
				currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
				// If the flight doesn't fall in the next time period, keep
				// going
				while (!currentPeriodEnd.isAfter(oETA)) {
					currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
					currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
					myList.add(0);
				}
				// If the flight is outside the interval, we are done.
			} else if (!oETA.isBefore(end)) {
				done = true;
			}
		}
		// If we run out of flights, add zeroes to the end.
		myList.add(currentCount);
		while (currentPeriodEnd.isBefore(end)) {
			currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
			currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
			myList.add(0);
		}
		return myList;
	}

	/**
	 * This takes a set of flights, a time interval, and a duration. The time
	 * interval will be broken into periods whose length is that of the given
	 * duration. This function will find the number of flights such that a given
	 * DateTime field of that flight falls into each discretized time period.
	 * For example, this function could be used to find the flights with
	 * original arrival time falling in each hour of given time interval.
	 * 
	 * @param flights
	 *            - the set of flights
	 * @param discretizedPeriodDuration
	 *            - the length of discretized time period
	 * @param timeInterval
	 *            - the interval
	 * @param fieldID
	 *            - the ID of the field of interest.
	 * @return - a list, where the ith entry is the number of flights whose
	 *         given field falls into the ith time period of the time interval.
	 * @throws Exception
	 */
	public static List<Set<Flight>> aggregateFlightsByFlightTimeField(Set<Flight> flights,
			Duration discretizedPeriodDuration, Interval timeInterval, int fieldID) throws Exception {

		DateTime end = timeInterval.getEnd();
		// Initialize first discrete time period
		DateTime currentPeriodStart = timeInterval.getStart();
		DateTime currentPeriodEnd = currentPeriodStart.plus(discretizedPeriodDuration);
		// Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(flights);
		// Sort the list by the field
		Collections.sort(sortedFlights, new FlightDateTimeFieldComparator(fieldID));

		// We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		List<Set<Flight>> myList = new ArrayList<Set<Flight>>();
		Set<Flight> currentSet = new HashSet<Flight>();
		while (done == false && myIterator.hasNext()) {
			// Get the field from the next flight;
			Flight nextFlight = myIterator.next();
			DateTime oETA = nextFlight.getDateTimeField(fieldID);
			// If the flight is in the current time period, add it to the count.
			if (!currentPeriodStart.isAfter(oETA) && oETA.isBefore(currentPeriodEnd)) {
				currentSet.add(nextFlight);
				// If the flight is not in the current time period, but is in
				// the larger interval,
				// add the count to the list, then find the next time period.
			} else if (!oETA.isBefore(currentPeriodEnd) && oETA.isBefore(end)) {
				myList.add(currentSet);
				currentSet = new HashSet<Flight>();
				currentSet.add(nextFlight);
				currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
				currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
				// If the flight doesn't fall in the next time period, keep
				// going
				while (!currentPeriodEnd.isAfter(oETA)) {
					currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
					currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
					myList.add(new HashSet<Flight>());
				}
				// If the flight is outside the interval, we are done.
			} else if (!oETA.isBefore(end)) {
				done = true;
			}
		}
		// If we run out of flights, add zeroes to the end.
		myList.add(currentSet);
		while (currentPeriodEnd.isBefore(end)) {
			currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
			currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
			myList.add(new HashSet<Flight>());
		}
		return myList;
	}

	/**
	 * This takes a set of flights, a time interval, and a duration. The time
	 * interval will be broken into periods whose length is that of the given
	 * duration. This function will find the number of flights such that a given
	 * DateTime field of that flight falls into each discretized time period.
	 * For example, this function could be used to find the number of flights
	 * with original arrival time falling in each hour of given time interval.
	 * 
	 * @param flights
	 *            - the set of flights
	 * @param discretizedPeriodDuration
	 *            - the length of discretized time period
	 * @param timeInterval
	 *            - the interval
	 * @param fieldID
	 *            - the ID of the field of interest.
	 * @return - a list, where the ith entry is the number of flights whose
	 *         given field falls into the ith time period of the time interval.
	 * @throws Exception
	 */
	public static List<Set<Flight>> aggregateFlightsByFlightTimeField(Set<Flight> flights,
			Duration discretizedPeriodDuration, Interval timeInterval, DateTime currentTime, int fieldID)
					throws Exception {

		DateTime end = timeInterval.getEnd();
		// Initialize first discrete time period
		DateTime currentPeriodStart = timeInterval.getStart();
		DateTime currentPeriodEnd = currentPeriodStart.plus(discretizedPeriodDuration);
		// Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(flights);
		// Sort the list by the field
		Collections.sort(sortedFlights, new FlightDateTimeFieldComparator(currentTime, fieldID));

		// We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		List<Set<Flight>> myList = new ArrayList<Set<Flight>>();
		Set<Flight> currentSet = new HashSet<Flight>();
		while (done == false && myIterator.hasNext()) {
			// Get the field from the next flight;
			Flight nextFlight = myIterator.next();
			DateTime oETA = nextFlight.getDateTimeField(fieldID, currentTime);
			// If the flight is in the current time period, add it to the count.
			if (!currentPeriodStart.isAfter(oETA) && oETA.isBefore(currentPeriodEnd)) {
				currentSet.add(nextFlight);
				// If the flight is not in the current time period, but is in
				// the larger interval,
				// add the count to the list, then find the next time period.
			} else if (!oETA.isBefore(currentPeriodEnd) && oETA.isBefore(end)) {
				myList.add(currentSet);
				currentSet = new HashSet<Flight>();
				currentSet.add(nextFlight);
				currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
				currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
				// If the flight doesn't fall in the next time period, keep
				// going
				while (!currentPeriodEnd.isAfter(oETA)) {
					currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
					currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
					myList.add(new HashSet<Flight>());
				}
				// If the flight is outside the interval, we are done.
			} else if (!oETA.isBefore(end)) {
				done = true;
			}
		}
		// If we run out of flights, add zeroes to the end.
		myList.add(currentSet);
		while (currentPeriodEnd.isBefore(end)) {
			currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
			currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
			myList.add(new HashSet<Flight>());
		}
		return myList;
	}

	/**
	 * This takes a set of flights, a time interval, and a duration. The time
	 * interval will be broken into periods whose length is that of the given
	 * duration. This function will find the number of flights such that a given
	 * DateTime field of that flight falls into each discretized time period.
	 * For example, this function could be used to find the number of flights
	 * with original arrival time falling in each hour of given time interval.
	 * 
	 * @param flights
	 *            - the set of flights
	 * @param discretizedPeriodDuration
	 *            - the length of discretized time period
	 * @param timeInterval
	 *            - the interval
	 * @param fieldID
	 *            - the ID of the field of interest.
	 * @return - a list, where the ith entry is the number of flights whose
	 *         given field falls into the ith time period of the time interval.
	 * @throws Exception
	 */
	public static List<Integer> aggregateFlightCountsByFlightTimeField(Set<Flight> flights,
			Duration discretizedPeriodDuration, Interval timeInterval, int fieldID) throws Exception {

		DateTime end = timeInterval.getEnd();
		// Initialize first discrete time period
		DateTime currentPeriodStart = timeInterval.getStart();
		DateTime currentPeriodEnd = currentPeriodStart.plus(discretizedPeriodDuration);
		// Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(flights);
		// Sort the list by the field
		Collections.sort(sortedFlights, new FlightDateTimeFieldComparator(fieldID));

		// We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		int currentCount = 0;
		List<Integer> myList = new ArrayList<Integer>();
		while (done == false && myIterator.hasNext()) {
			// Get the field from the next flight;
			DateTime oETA = myIterator.next().getDateTimeField(fieldID);
			// If the flight is in the current time period, add it to the count.
			if (!currentPeriodStart.isAfter(oETA) && oETA.isBefore(currentPeriodEnd)) {
				currentCount++;
				// If the flight is not in the current time period, but is in
				// the larger interval,
				// add the count to the list, then find the next time period.
			} else if (!oETA.isBefore(currentPeriodEnd) && oETA.isBefore(end)) {
				myList.add(currentCount);
				currentCount = 1;
				currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
				currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
				// If the flight doesn't fall in the next time period, keep
				// going
				while (!currentPeriodEnd.isAfter(oETA)) {
					currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
					currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
					myList.add(0);
				}
				// If the flight is outside the interval, we are done.
			} else if (!oETA.isBefore(end)) {
				done = true;
			}
		}
		// If we run out of flights, add zeroes to the end.
		myList.add(currentCount);
		while (currentPeriodEnd.isBefore(end)) {
			currentPeriodStart = currentPeriodStart.plus(discretizedPeriodDuration);
			currentPeriodEnd = currentPeriodEnd.plus(discretizedPeriodDuration);
			myList.add(0);
		}
		return myList;
	}

	/**
	 * This
	 * 
	 * @param flights
	 * @param fieldID
	 * @param shortestDuration
	 * @param longestDuration
	 * @param discretizedPeriodDuration
	 * @return
	 * @throws Exception
	 */
	public static List<Set<Flight>> aggregateFlightsByDurationField(Set<Flight> flights, int fieldID,
			Duration shortestDuration, Duration longestDuration, Duration discretizedPeriodDuration) throws Exception {
		// Initialize first discrete time period
		Duration currentShortest = shortestDuration;
		Duration currentLongest = currentShortest.plus(discretizedPeriodDuration);
		// Copy the flights
		LinkedList<Flight> sortedFlights = new LinkedList<Flight>(flights);
		// Sort the list by the field
		Collections.sort(sortedFlights, new FlightDurationFieldComparator(fieldID));

		// We will go through the flights and add them to the list.
		Iterator<Flight> myIterator = sortedFlights.iterator();
		boolean done = false;
		Set<Flight> currentSet = new HashSet<Flight>();
		List<Set<Flight>> myList = new ArrayList<Set<Flight>>();
		while (done == false && myIterator.hasNext()) {
			// Get the field from the next flight;
			Flight nextFlight = myIterator.next();
			Duration flightDurationField = nextFlight.getDurationField(fieldID);
			// If the flight is in the current time period, add it to the count.
			if (!flightDurationField.isShorterThan(currentShortest)
					&& flightDurationField.isShorterThan(currentLongest)) {
				currentSet.add(nextFlight);
				// If the flight is not in the current time period, but is in
				// the larger interval,
				// add the count to the list, then find the next time period.
			} else if (!flightDurationField.isShorterThan(currentLongest)
					&& flightDurationField.isShorterThan(longestDuration)) {
				myList.add(currentSet);
				currentSet = new HashSet<Flight>();
				currentSet.add(nextFlight);
				currentShortest = currentShortest.plus(discretizedPeriodDuration);
				currentLongest = currentLongest.plus(discretizedPeriodDuration);
				// If the flight doesn't fall in the next time period, keep
				// going
				while (!flightDurationField.isShorterThan(currentLongest)) {
					currentShortest = currentShortest.plus(discretizedPeriodDuration);
					currentLongest = currentLongest.plus(discretizedPeriodDuration);
					myList.add(new HashSet<Flight>());
				}
				// If the flight is outside the interval, we are done.
			} else if (!flightDurationField.isShorterThan(longestDuration)) {
				done = true;
			}
		}
		// If we run out of flights, add zeroes to the end.
		myList.add(currentSet);
		while (currentLongest.isShorterThan(longestDuration)) {
			currentShortest = currentShortest.plus(discretizedPeriodDuration);
			currentLongest = currentLongest.plus(discretizedPeriodDuration);
			myList.add(new HashSet<Flight>());
		}
		return myList;
	}

	/**
	 * This divides the time horizon into discrete, evenly-spaced time intervals
	 * and finds the number of flights that are arriving in each time interval
	 * 
	 * @param sittingFlights
	 * @param timePeriodDuration
	 * @param currentTime
	 * @return
	 */
	public static List<ImmutablePair<Integer, Integer>> discretizeFlights(List<Flight> sittingFlights,
			Duration timePeriodDuration, DateTime currentTime) {
		Iterator<Flight> flightIterator = sittingFlights.iterator();
		List<ImmutablePair<Integer, Integer>> discreteFlights = new ArrayList<ImmutablePair<Integer, Integer>>();
		while (flightIterator.hasNext()) {
			Flight nextFlight = flightIterator.next();
			DateTime earlyETA = nextFlight.getEarliestETA(currentTime);
			Duration timeFromNow = new Duration(currentTime, earlyETA);
			int numTimePeriods = (int) (timeFromNow.getMillis() / timePeriodDuration.getMillis());
			int flightTimePeriods = (int) (nextFlight.getFlightTime().getMillis() / timePeriodDuration.getMillis());
			ImmutablePair<Integer, Integer> myFlightPair = ImmutablePair.of(numTimePeriods, flightTimePeriods);
			discreteFlights.add(myFlightPair);
		}
		return discreteFlights;
	}

	/**
	 * This function applies the given TmiAction to the specified state, using
	 * the given flight handler
	 * 
	 * @param state
	 * @param tmiAction
	 * @param flightHandler
	 * @return
	 * @throws Exception
	 */
	public static DefaultState implementTmi(DefaultState state, SimpleTmiAction tmiAction, FlightHandler flightHandler,
			Comparator<Flight> assignmentPriority) throws Exception {
		// If the TmiAction is a none-type, then don't alter the state
		if (tmiAction.getType() == (int) SimpleTmiAction.NONE_TYPE) {
			return state;
			// If it is a GDP or ground stop, then extract the parameters, and
			// call another function to handle it.
		} else {
			SortedMap<DateTime, Integer> paars = new TreeMap<DateTime, Integer>();
			double startMinutes = tmiAction.getStartTimeMin();

			DateTime currentTime = state.getCurrentTime();
			DateTime tmiStartTime = new DateTime(currentTime.getYear(), currentTime.getMonthOfYear(),
					currentTime.getDayOfMonth(), dayHourStart, 0, currentTime.getChronology().getZone())
							.plusMinutes((int) startMinutes);
			DateTime tmiEndTime = tmiStartTime.plusMinutes(tmiAction.getDurationMin().intValue());
			Interval tmiInterval = new Interval(tmiStartTime, tmiEndTime);
			if (tmiAction.getType() == (int) SimpleTmiAction.GDP_TYPE) {
				paars.put(tmiStartTime, tmiAction.getRate().intValue());
				return implementGdp(state, paars, tmiInterval, flightHandler, tmiAction.getRadius().intValue(),
						assignmentPriority);
			} else {
				return implementGs(state, tmiInterval, flightHandler, tmiAction.getRadius().intValue());
			}
		}

	}

	private static DefaultState implementGs(DefaultState state, Interval tmiInterval, FlightHandler flightHandler,
			int radius) {
		FlightState flights = state.getFlightState();
		SortedSet<Flight> sittingFlights = flights.getSittingFlights();
		SortedSet<Flight> newSittingFlights = new TreeSet<Flight>(sittingFlights.comparator());
		DateTime currentTime = state.getCurrentTime();
		DateTime gsEnd = tmiInterval.getEnd();
		for (Flight f : sittingFlights) {
			if (tmiInterval.contains(f.getBestETD(currentTime)) && f.getDistance() <= radius) {
				newSittingFlights.add(flightHandler.controlArrival(f, gsEnd.plus(f.getFlightTime()), currentTime));
			} else {
				newSittingFlights.add(f);
			}
		}
		return state.setFlightState(state.getFlightState().setSittingFlights(newSittingFlights));
	}

	public static DefaultState implementGdp(DefaultState state, SortedMap<DateTime, Integer> paars,
			Interval gdpInterval, FlightHandler flightHandler, double radius, Comparator<Flight> assignmentPriority)
					throws Exception {
		FlightState flights = state.getFlightState();
		DateTime currentTime = state.getCurrentTime();
		// Get the list of available slots
		SortedSet<DateTime> slotList = GDPPlanningHelper.getSlotList(flights, paars, gdpInterval, radius);

		// Get the list of flights that we need to assign slots to
		List<Flight> sitting = new LinkedList<Flight>(flights.getSittingFlights());
		// Sort flights by distance
		Collections.sort(sitting, assignmentPriority);

		// Make a new sorted set to store the updated flights
		SortedSet<Flight> newSittingFlights = new TreeSet<Flight>(
				new FlightDateTimeFieldComparator(Flight.aETDFieldID));

		// Get the GDP time period
		DateTime start = gdpInterval.getStart();
		DateTime end = gdpInterval.getEnd();

		// Assign the flights to slots
		for (Flight nextSitting : sitting) {
			// If the arrival time is within the GDP planning period, need to
			// give the flight a slot
			// Slots are assigned based on the ETA given at the flight's
			// departure
			DateTime earliestETA = nextSitting.getEarliestETA(currentTime);

			// Check to see if the flight falls within the GDP time period
			if (earliestETA.compareTo(start) >= 0 && earliestETA.compareTo(end) < 0
					&& nextSitting.getDistance() <= radius) {
				// Find the next available slot
				SortedSet<DateTime> tailSet = slotList.tailSet(earliestETA);
				if (!tailSet.isEmpty()) {
					// When we add the flight, we control its arrival and we add
					// a random amount of delay.
					newSittingFlights.add(flightHandler.controlArrival(nextSitting, tailSet.first(), currentTime));
					slotList.remove(tailSet.first());
				} else {
					// If there are not enough slots, then assign flight to
					// latest time;
					// TODO: Perhaps other ways of handling this.
					newSittingFlights.add(flightHandler.controlArrival(nextSitting, end, currentTime));
				}
			} else {
				// If the flight is outside the control of the GDP, just add
				// it to the new set of flights without changing it
				newSittingFlights.add(nextSitting);
			}
		}
		FlightState newFlightState = state.getFlightState().setSittingFlights(newSittingFlights);
		return state.setFlightState(newFlightState);
	}

	public static DefaultState implementTmi(DefaultState state, GdpAction chooseTmi, FlightHandler flightHandler,
			Comparator<Flight> assignmentPriority) throws Exception {
		return implementGdp(state, chooseTmi.getPaars(), chooseTmi.getGdpInterval(), flightHandler,
				chooseTmi.getRadius(), assignmentPriority);
	}
}
