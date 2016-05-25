package state_representation;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import bandit_objects.Immutable;

/**
 * This class is an object containing a flight
 * 
 * @author Alex2
 * 
 */
public class Flight implements Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 832808882274187973L;
	private static AtomicInteger nextFlightInt = new AtomicInteger();
	// Field IDs for DateTime fields
	public static final int origETDFieldID = 0;
	public static final int origETAFieldID = 1;
	public static final int cETDFieldID = 2;
	public static final int cETAFieldID = 3;
	public static final int aETDFieldID = 4;
	public static final int aETAFieldID = 5;
	public static final int depETAFieldID = 6;
	public static final int bestETAFieldID = 7;
	public static final int earliestETAFieldID = 8;
	public static final int earliestETDFieldID = 9;

	// Field IDs for duration fields.
	public static final int departureDelayID = 10;
	public static final int enrouteDelayID = 11;
	public static final int airQueueDelayID = 12;
	public static final int scheduledDelayID = 13;
	public static final int revertedDelayID = 14;
	public static final int maxDelayID = 15;
	public static final int flightTimeID = 16;

	// Field IDs for int fields
	public static final int flightNumberID = 17;
	public static final int numPassengersID = 18;

	private final Integer flightNumber;
	private final String airline;
	private final Integer numPassengers;
	private final Integer distance;

	// These are the original estimated time of departure and arrival.
	// These should never change and should always be considered valid.
	private final DateTime origETD;
	private final DateTime origETA;

	// These are the controlled times of departure and arrival
	// These should only be considered valid if the plane has
	// been delayed by a GDP.
	// These should only be changed by a GDP planning module,
	// and these should not change until a plane has landed.
	private final DateTime cETD;
	private final DateTime cETA;

	// These are the actual times of departure and arrival
	// The actual time of departure should always be considered valid
	// This should not change once the plane has taken off
	private final DateTime aETD;

	// The actual time of arrival should be considered valid only after
	// the plane has taken off.
	// This should not change once the plane has landed.
	private final DateTime aETA;

	private final boolean airborne;
	private final boolean landed;
	private final boolean gdpDelayed;
	private final boolean cancelled;

	private final Duration departureDelay;
	private final Duration enRouteDelay;
	private final Duration airQueueDelay;
	private final Duration scheduledDelay;
	private final Duration revertedDelay;
	private final Duration maxDelay;

	/**
	 * Standard constructor for the flight class
	 * 
	 * @param flightNumber
	 * @param airline
	 * @param numPassengers
	 * @param origETD
	 * @param origETA
	 * @param cETD
	 * @param cETA
	 * @param aETD
	 * @param aETA
	 * @param airborne
	 * @param landed
	 * @param gdpDelayed
	 * @param enRouteDelay
	 * @param airQueueDelay
	 * @param scheduledDelay
	 * @param revertedDelay
	 */
	public Flight(String airlineId, Integer numPassengers, Integer  distance, DateTime origETD, DateTime origETA, DateTime cETD,
			DateTime cETA, DateTime aETD, DateTime aETA, boolean airborne, boolean landed, boolean gdpDelayed,
			boolean cancelled, Duration departureDelay, Duration enRouteDelay, Duration airQueueDelay,
			Duration scheduledDelay, Duration revertedDelay) {
		this.flightNumber = nextFlightInt.incrementAndGet();
		this.airline = airlineId;
		this.numPassengers = numPassengers;
		this.distance = distance;

		this.origETD = origETD;
		this.origETA = origETA;

		this.cETD = cETD;
		this.cETA = cETA;

		this.aETD = aETD;
		this.aETA = aETA;

		this.airborne = airborne;
		this.landed = landed;
		this.gdpDelayed = gdpDelayed;
		this.cancelled = cancelled;

		this.departureDelay = departureDelay;
		this.enRouteDelay = enRouteDelay;
		this.airQueueDelay = airQueueDelay;
		this.scheduledDelay = scheduledDelay;
		this.revertedDelay = revertedDelay;
		this.maxDelay = null;
	}

	public Flight(String  airlineId, Integer numPassengers, Integer distance, DateTime origETD, DateTime origETA, DateTime cETD,
			DateTime cETA, DateTime aETD, DateTime aETA, boolean airborne, boolean landed, boolean gdpDelayed,
			boolean cancelled, Duration departureDelay, Duration enRouteDelay, Duration airQueueDelay,
			Duration scheduledDelay, Duration revertedDelay, Duration maxDelay) {
		this.flightNumber = nextFlightInt.incrementAndGet();
		this.airline = airlineId;
		this.numPassengers = numPassengers;
		this.distance = distance;

		this.origETD = origETD;
		this.origETA = origETA;

		this.cETD = cETD;
		this.cETA = cETA;

		this.aETD = aETD;
		this.aETA = aETA;

		this.airborne = airborne;
		this.landed = landed;
		this.gdpDelayed = gdpDelayed;
		this.cancelled = cancelled;

		this.departureDelay = departureDelay;
		this.enRouteDelay = enRouteDelay;
		this.airQueueDelay = airQueueDelay;
		this.scheduledDelay = scheduledDelay;
		this.revertedDelay = revertedDelay;
		this.maxDelay = maxDelay;
	}

	public Flight setIntField(int fieldId, Integer value) throws Exception {
		if (fieldId == flightNumberID) {
			return setFlightNumber(value);
		} else if (fieldId == numPassengersID) {
			return setNumPassengers(value);
		} else {
			throw new Exception();
		}
	}

	public Flight setDurationField(int fieldID, Duration value) throws Exception {
		if (fieldID == departureDelayID) {
			return setDepartureDelay(value);
		} else if (fieldID == enrouteDelayID) {
			return setEnRouteDelay(value);
		} else if (fieldID == airQueueDelayID) {
			return setAirQueueDelay(value);
		} else if (fieldID == scheduledDelayID) {
			return setScheduledDelay(value);
		} else if (fieldID == revertedDelayID) {
			return setRevertedDelay(value);
		} else if (fieldID == maxDelayID) {
			return setMaxDelay(value);
		} else {
			throw new Exception("Invalid entry.");
		}
	}

	public Flight setDateTimeField(int fieldID, DateTime value) throws Exception {
		if (fieldID == aETAFieldID) {
			return setaETA(value);
		} else if (fieldID == aETDFieldID) {
			return setaETD(value);
		} else if (fieldID == origETAFieldID) {
			return setOrigETA(value);
		} else if (fieldID == origETDFieldID) {
			return setOrigETD(value);
		} else if (fieldID == cETAFieldID) {
			return setcETA(value);
		} else if (fieldID == cETDFieldID) {
			return setcETD(value);
		}
		throw new Exception("Invalid entry.");
	}

	public Duration getDurationField(int fieldID) throws Exception {
		if (fieldID == departureDelayID) {
			return departureDelay;
		} else if (fieldID == enrouteDelayID) {
			return enRouteDelay;
		} else if (fieldID == airQueueDelayID) {
			return airQueueDelay;
		} else if (fieldID == scheduledDelayID) {
			return scheduledDelay;
		} else if (fieldID == revertedDelayID) {
			return revertedDelay;
		} else if (fieldID == flightTimeID) {
			return getFlightTime();
		} else if (fieldID == maxDelayID) {
			return getMaxDelay();
		}
		throw new Exception("Invalid entry.");
	}

	public DateTime getDateTimeField(int fieldID) throws Exception {
		if (fieldID == aETAFieldID) {
			return aETA;
		} else if (fieldID == aETDFieldID) {
			return aETD;
		} else if (fieldID == origETAFieldID) {
			return origETA;
		} else if (fieldID == origETDFieldID) {
			return origETD;
		} else if (fieldID == cETAFieldID) {
			return cETA;
		} else if (fieldID == cETDFieldID) {
			return cETD;
		} else if (fieldID == depETAFieldID) {
			return getDepETA();
		}
		throw new Exception("Invalid entry.");
	}

	public DateTime getDateTimeField(int fieldID, DateTime currentTime) throws Exception {
		if (fieldID == bestETAFieldID) {
			return getBestETA(currentTime);
		} else if (fieldID == earliestETAFieldID) {
			return getEarliestETA(currentTime);
		} else if (fieldID == earliestETDFieldID) {
			return getEarliestETD(currentTime);
		} else {
			return getDateTimeField(fieldID);
		}
	}

	public DateTime getBestETD(DateTime currentTime) {
		if (isGdpDelayed()) {
			if (currentTime.isAfter(cETD)) {
				return (currentTime);
			} else {
				return (cETD);
			}
		} else {
			if (currentTime.isAfter(origETD)) {
				return (currentTime.plus(getFlightTime()));
			} else {
				return (origETA);
			}
		}
	}
	
	public DateTime getBestETA(DateTime currentTime) {
		if (isGdpDelayed()) {
			if (currentTime.isAfter(cETD)) {
				return (currentTime.plus(getFlightTime()));
			} else {
				return (cETA);
			}
		} else {
			if (currentTime.isAfter(origETD)) {
				return (currentTime.plus(getFlightTime()));
			} else {
				return (origETA);
			}
		}
	}

	/**
	 * This returns the scheduled flight time of the flight.
	 * 
	 * @return
	 */
	public Duration getFlightTime() {
		return new Duration(origETD, origETA);
	}

	/**
	 * This returns the earliest time that a flight could depart.
	 * 
	 * @param currentTime
	 * @return
	 */
	public DateTime getEarliestETD(DateTime currentTime) {
		if (origETD.isBefore(currentTime)) {
			return currentTime;
		} else {
			return origETD;
		}
	}

	/**
	 * This returns the earliest time that a flight could arrive.
	 * 
	 * @param currentTime
	 * @return
	 */
	public DateTime getEarliestETA(DateTime currentTime) {
		return getEarliestETD(currentTime).plus(getFlightTime());
	}

	/**
	 * This prints the fields of the flight. Note: does not add a new line
	 * character at the end
	 */
	@Override
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
		String myString = flightNumber + " " + airline + " " + numPassengers + "\t";
		myString += formatter.print(origETD) + "\t" + formatter.print(origETA) + "\t";
		myString += formatter.print(aETD) + "\t";

		if (aETA != null) {
			myString += formatter.print(aETA) + "\t";
		} else {
			myString += "xxxx/xx/xx xx:xx \t";
		}

		if (cETD != null) {
			myString += formatter.print(cETD) + "\t";
		} else {
			myString += "xxxx/xx/xx xx:xx \t";
		}

		if (cETA != null) {
			myString += formatter.print(cETA) + "\t";
		} else {
			myString += "xxxx/xx/xx xx:xx \t";
		}
		myString += airborne + "\t" + landed + "\t" + gdpDelayed;
		myString += "\t" + airQueueDelay.toStandardMinutes() + "\t" + departureDelay.toStandardMinutes() + "\t"
				+ enRouteDelay.toStandardMinutes() + "\t" + scheduledDelay.toStandardMinutes() + "\t"
				+ revertedDelay.toStandardMinutes();
		return myString;
	}

	/**
	 * This returns the total amount that the airplane has been delayed. Not a
	 * true getter; calculates total delay from departureDelay, enRouteDelay,
	 * airQueueDelay, scheduleDelay, and revertedDelay.
	 * 
	 * @return
	 */
	public Duration getTotalDelay() {
		return departureDelay.plus(enRouteDelay).plus(airQueueDelay).plus(scheduledDelay).minus(revertedDelay);
	}

	/**
	 * This returns the current scheduled departure time
	 * 
	 * @return
	 */
	public DateTime getScheduledDeparture() {
		if (gdpDelayed) {
			return cETD;
		}
		return origETD;
	}

	/**
	 * This should not be called if the flight has not departed
	 * 
	 * @return
	 */
	public DateTime getDepETA() {
		return aETD.plus(new Duration(origETD, origETA));
	}

	// ------------------Getters ------------------------------------
	public Integer getFlightNumber() {
		return flightNumber;
	}

	public String  getAirlineId() {
		return airline;
	}

	public Integer getNumPassengers() {
		return numPassengers;
	}

	public DateTime getOrigETD() {
		return origETD;
	}

	public DateTime getOrigETA() {
		return origETA;
	}

	public DateTime getcETD() {
		return cETD;
	}

	public DateTime getcETA() {
		return cETA;
	}

	public DateTime getaETD() {
		return aETD;
	}

	public DateTime getaETA() {
		return aETA;
	}

	public boolean isAirborne() {
		return airborne;
	}

	public boolean isLanded() {
		return landed;
	}

	public boolean isGdpDelayed() {
		return gdpDelayed;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public Duration getDepartureDelay() {
		return departureDelay;
	}

	public Duration getEnRouteDelay() {
		return enRouteDelay;
	}

	public Duration getAirQueueDelay() {
		return airQueueDelay;
	}

	public Duration getScheduledDelay() {
		return scheduledDelay;
	}

	public Duration getRevertedDelay() {
		return revertedDelay;
	}

	// ----------------Setters ------------------------------
	public Flight setFlightNumber(Integer flightNumber) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setAirline(String airline) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setNumPassengers(Integer numPassengers) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setOrigETD(DateTime origETD) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setOrigETA(DateTime origETA) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setcETD(DateTime cETD) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setcETA(DateTime cETA) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setaETD(DateTime aETD) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setaETA(DateTime aETA) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setAirborne(boolean airborne) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setLanded(boolean landed) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setGdpDelayed(boolean gdpDelayed) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setCancelled(boolean cancelled) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setDepartureDelay(Duration departureDelay) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setEnRouteDelay(Duration enRouteDelay) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setAirQueueDelay(Duration airQueueDelay) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setScheduledDelay(Duration scheduledDelay) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public Flight setRevertedDelay(Duration revertedDelay) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	/**
	 * @return the maxDelay
	 */
	public Duration getMaxDelay() {
		return maxDelay;
	}

	public Flight setMaxDelay(Duration maxDelay) {
		return new Flight(airline, numPassengers, distance, origETD, origETA, cETD, cETA, aETD, aETA, airborne, landed,
				gdpDelayed, cancelled, departureDelay, enRouteDelay, airQueueDelay, scheduledDelay, revertedDelay,
				maxDelay);
	}

	public static AtomicInteger getNextFlightInt() {
		return nextFlightInt;
	}

	public static void setNextFlightInt(AtomicInteger nextFlightInt) {
		Flight.nextFlightInt = nextFlightInt;
	}

	public static Integer getOrigetdfieldid() {
		return origETDFieldID;
	}

	public static Integer getOrigetafieldid() {
		return origETAFieldID;
	}

	public static Integer getCetdfieldid() {
		return cETDFieldID;
	}

	public static Integer getCetafieldid() {
		return cETAFieldID;
	}

	public static Integer getAetdfieldid() {
		return aETDFieldID;
	}

	public static Integer getAetafieldid() {
		return aETAFieldID;
	}

	public static Integer getDepetafieldid() {
		return depETAFieldID;
	}

	public static Integer getBestetafieldid() {
		return bestETAFieldID;
	}

	public static Integer getEarliestetafieldid() {
		return earliestETAFieldID;
	}

	public static Integer getEarliestetdfieldid() {
		return earliestETDFieldID;
	}

	public static Integer getDeparturedelayid() {
		return departureDelayID;
	}

	public static Integer getEnroutedelayid() {
		return enrouteDelayID;
	}

	public static Integer getAirqueuedelayid() {
		return airQueueDelayID;
	}

	public static Integer getScheduleddelayid() {
		return scheduledDelayID;
	}

	public static Integer getReverteddelayid() {
		return revertedDelayID;
	}

	public static Integer getMaxdelayid() {
		return maxDelayID;
	}

	public static Integer getFlighttimeid() {
		return flightTimeID;
	}

	public static Integer getFlightnumberid() {
		return flightNumberID;
	}

	public static Integer getNumpassengersid() {
		return numPassengersID;
	}

	public Integer getDistance() {
		return distance;
	}

	
}
