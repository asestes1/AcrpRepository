package state_update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import model.StateAction;
import state_representation.AirportState;
import state_representation.DefaultState;
import state_representation.Flight;
import state_representation.FlightState;
import util_random.ParameterizedDistribution;

public class NASStateUpdate implements
	StateAction<DefaultState>{
	private final ParameterizedDistribution<Integer,Duration> runwayDist;
	
	public NASStateUpdate(ParameterizedDistribution<Integer,Duration> runwayDist) {
		this.runwayDist = runwayDist;
	}

	@Override
	public DefaultState act(DefaultState state, FlightHandler flightHandler, Duration timeStep) {
		DateTime nextTime = state.getCurrentTime().plus(timeStep);

		Set<Flight> nextLandedFlights =
				new HashSet<Flight>(state.getFlightState()
						.getLandedFlights());
		SortedSet<Flight> nextAirborneFlights = new TreeSet<Flight>(
				new FlightDateTimeFieldComparator(Flight.aETAFieldID));
		SortedSet<Flight> nextSittingFlights = new TreeSet<Flight>(
				new FlightDateTimeFieldComparator(Flight.aETDFieldID));

		int currentCapacity = state.getCapacityState().getCapacity();
		

		// Handle the airborne flights
		Flight nextFlight;
		boolean allHandled = false;
		SortedSet<Flight> airborneFlights = state.getFlightState()
				.getAirborneFlights();
		Iterator<Flight> iter = airborneFlights.iterator();
		DateTime nextLandTime = state.getAirportState()
				.getNextAvailable();
		
		int nextQueueLength = 0;
		while (iter.hasNext() && allHandled == false) {
			// Get next flight
			nextFlight = iter.next();
			// If the flight is not going to land, then it remains airborne
			if (nextFlight.getaETA().isAfter(nextTime)) {
				allHandled = true;
				nextAirborneFlights.addAll(airborneFlights.tailSet(nextFlight));
				// Otherwise, we will attempt to land the flight.
			} else {
				// If the runway will open in this time period, then land the flight
				if (nextLandTime.isBefore(nextTime)) {
					// Wait until the runway is available
					Duration timeToLand = runwayDist.sample(currentCapacity);
					if(!nextLandTime.isAfter(nextFlight.getaETA())){
						nextLandTime = nextFlight.getaETA().plus(timeToLand);
					}else{
						nextFlight = flightHandler.airHold(nextFlight, nextLandTime);
						nextLandTime = nextLandTime.plus(timeToLand);
					}
					nextLandedFlights.add(flightHandler.land(nextFlight));
				//If the runway will not open in this time period, then hold the flight
				} else {
					// Delay flight until beginning of next time period.
					nextAirborneFlights.add(flightHandler.airHold(nextFlight,nextTime));
					nextQueueLength++;
				}
			}
		}

		// Handle the sitting flights
		SortedSet<Flight> sittingFlights = state.getFlightState()
				.getSittingFlights();
		iter = sittingFlights.iterator();
		while (iter.hasNext()) {
			// Get next flight
			nextFlight = iter.next();
			// Once we reach the flights whose departure time is later than the
			// current time, we are done.
			if (nextFlight.getaETD().isAfter(nextTime)) {
				allHandled = true;
				nextSittingFlights.addAll(sittingFlights.tailSet(nextFlight));
			} else {
				// If our departure time is earlier than current time, we take
				// off, are added to the
				// list of airborne flights and receive en route delay.
				nextAirborneFlights.add(flightHandler.enRouteDelay(
						flightHandler.takeOff(nextFlight)));
			}
		}
		
		FlightState nextFlightState = new FlightState(nextLandedFlights,
				nextAirborneFlights,nextSittingFlights,
				state.getFlightState().getCancelledFlights());
		AirportState nextAirportState = new AirportState(nextLandTime,nextQueueLength);
		return new DefaultState(nextTime,nextFlightState,
				nextAirportState,state.getCapacityState());
	}

	public ParameterizedDistribution<Integer, Duration> getRunwayDist() {
		return runwayDist;
	}


	public NASStateUpdate setRunwayDist(ParameterizedDistribution<Integer,Duration> runwayDist) {
		return new NASStateUpdate(runwayDist);
	}
	
	
}
