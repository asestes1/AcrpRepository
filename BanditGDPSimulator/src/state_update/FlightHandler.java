package state_update;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_representation.Flight;

/**
 * This is an interface which describes all the actions that are
 * required for updating flights.
 * @author Alex2
 *
 */
public interface FlightHandler {

	/**
	 * This function should handle the final landing of the flight
	 * @param nextFlight the flight
	 * @return A copy of the flight that has landed
	 */
	public Flight land(Flight nextFlight);

	/**
	 * This function should handle delaying a flight in the air
	 * @param nextFlight the flight
	 * @param timeHeld the amount of time that the flight is held
	 * @return a copy of the flight after being held
	 */
	public Flight airHold(Flight nextFlight, Duration timeHeld);
	
	/**
	 * This function should handle the taking off of a flight
	 * @param flight the flight
	 * @return a copy of the flight which has taken off.
	 */
	public Flight takeOff(Flight flight);
	
	/**
	 * This function should assign enroute delay to the flight.
	 * @param flight: the flight
	 * @return a copy of the flight with en-route delay added
	 */
	public Flight enRouteDelay(Flight flight);

	/**
	 * This function should add random departure delay to the flight
	 * @param flight: the flight
	 * @return a copy of the flight with the delay added
	 */
	public Flight depDelay(Flight flight);

	/**
	 * This function should delay the flight in the air until the given time
	 * @param nextFlight: the flight
	 * @param nextLandTime: the time at which the flight will no longer be held
	 * @return a copy of the flight with newly added delays
	 */
	public Flight airHold(Flight nextFlight, DateTime newETA);
	
	/**
	 * Set the controlled arrival time of the flight
	 * @param flight: the flight
	 * @param cETA: the new controlled arrival time
	 * @param currentTime: the current time
	 * @return a copy of the flight with newly set cETA
	 */
	public Flight controlArrival(Flight flight, DateTime cETA, DateTime currentTime);
	
	/**
	 * Ground delay the flight for a certain amount of time
	 * @param flight: the flight
	 * @param gdpDelay: the amount of ground delay to apply
	 * @param currentTime: the current time
	 * @return a copy of the flight with ground delay added.
	 */
	public Flight gdpDelay(Flight flight,Duration gdpDelay, DateTime currentTime);

	/**
	 * Cancel the flight
	 * @param nextFlight: the flight
	 * @return a copy of the flight which is now cancelled
	 */
	public Flight cancel(Flight nextFlight) ;

	/**
	 * Release a flight which was in a ground delay program
	 * @param flight: the flight
	 * @param currentTime: the current time
	 * @return a copy of the flight which is now released from the ground delay program.
	 */
	public Flight release(Flight flight, DateTime currentTime);
}
