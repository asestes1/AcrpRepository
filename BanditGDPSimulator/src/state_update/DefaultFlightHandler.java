package state_update;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_representation.Flight;
import util_random.Distribution;

/**
 * This is the default implementation of the flight handler class.
 * @author Alex2
 *
 */
public class DefaultFlightHandler implements FlightHandler{
	private final Distribution<Duration> depDelayDistribution;
	private final Distribution<Duration> arrDelayDistribution;
	
	public DefaultFlightHandler(Distribution<Duration> depDelayDistribution,
			Distribution<Duration> arrDelayDistribution){
		this.depDelayDistribution = depDelayDistribution;
		this.arrDelayDistribution = arrDelayDistribution;
	}
	
	@Override
	public Flight land(Flight nextFlight) {
		return nextFlight.setAirborne(false).setLanded(true);
	}

	@Override
	public Flight airHold(Flight nextFlight, Duration timeHeld) {
		Duration updatedAirQueueDelay = nextFlight.getAirQueueDelay().plus(timeHeld);
		DateTime updatedaETA = nextFlight.getaETA().plus(timeHeld);
		return nextFlight.setaETA(updatedaETA).setAirQueueDelay(updatedAirQueueDelay);
	}
	
	@Override
	public Flight takeOff(Flight flight) {
		Flight newFlight =  flight.setaETA(flight.getaETD().plus(flight.getFlightTime()));
		return newFlight.setAirborne(true);
	}
	
	@Override
	public Flight enRouteDelay(Flight flight) {
		Duration delay = arrDelayDistribution.sample();
		Flight newFlight = flight.setEnRouteDelay(flight.getEnRouteDelay().plus(delay));
		return newFlight.setaETA(flight.getaETA().plus(delay));
	}

	@Override
	public Flight depDelay(Flight flight) {
		Duration delay = depDelayDistribution.sample();
		Flight newFlight = flight.setDepartureDelay(flight.getDepartureDelay().plus(delay));
		return newFlight.setaETD(flight.getaETD().plus(delay));
	}

	@Override
	public Flight airHold(Flight nextFlight, DateTime nextLandTime) {
		return airHold(nextFlight,new Duration(nextFlight.getaETA(),nextLandTime));
		
	}

	@Override
	public Flight controlArrival(Flight flight, DateTime cETA, DateTime currentTime) {
		Duration delay = new Duration(flight.getOrigETA(), cETA);
		return gdpDelay(flight,delay, currentTime);		
	}
	
	@Override
	public Flight gdpDelay(Flight flight,Duration gdpDelay, DateTime currentTime) {
		// Adjust the controlled times of arrival
		DateTime newcETA = flight.getOrigETA().plus(gdpDelay);
		DateTime newcETD = flight.getOrigETD().plus(gdpDelay);

		// Set the new departure time
		Duration newDepartureDelay = flight.getDepartureDelay();

		if (!flight.isGdpDelayed() && flight.getOrigETD().compareTo(currentTime) < 0) {
			newDepartureDelay = new Duration(flight.getOrigETD(), currentTime);
			gdpDelay = gdpDelay.minus(newDepartureDelay);
		}
		
		return depDelay(flight.setcETA(newcETA).setcETD(newcETD).setaETD(newcETD)
				.setDepartureDelay(newDepartureDelay).setGdpDelayed(true)
				.setScheduledDelay(gdpDelay));
	}

	@Override
	public Flight cancel(Flight nextFlight) {
		return nextFlight.setCancelled(true);
	}

	@Override
	public Flight release(Flight flight, DateTime currentTime) {
		Flight newFlight = flight;
		if(!flight.isAirborne() && flight.isGdpDelayed() 
				&& !flight.isLanded() && !flight.isCancelled()){
			newFlight = newFlight.setGdpDelayed(false);
			if(flight.getOrigETD().isBefore(currentTime)){
				newFlight = flight.setaETD(currentTime);
				if(flight.getcETD().isAfter(currentTime)){
					newFlight = newFlight.setRevertedDelay(
						newFlight.getRevertedDelay().plus(
						new Duration(currentTime,flight.getcETD())));
				}
				newFlight = depDelay(newFlight);
			}
		}
		return newFlight;
	}

	public Distribution<Duration> getDepDelayDistribution() {
		return depDelayDistribution;
	}

	public Distribution<Duration> getArrDelayDistribution() {
		return arrDelayDistribution;
	}

	public DefaultFlightHandler setDepDelayDistribution(Distribution<Duration> depDelayDistribution) {
		return new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);
	}

	public DefaultFlightHandler setArrDelayDistribution(Distribution<Duration> arrDelayDistribution) {
		return new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);
	}

}
