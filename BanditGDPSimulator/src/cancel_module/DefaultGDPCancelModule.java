package cancel_module;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.Duration;

import model.StateAction;
import state_representation.DefaultState;
import state_representation.Flight;
import state_representation.FlightState;
import state_update.DefaultFlightHandler;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;

/**
 * This is a module implementing a state action on the default state
 * defined for the ground delay program simulation engine.
 * @author Alex2
 *
 *
 */
public class DefaultGDPCancelModule
implements StateAction<DefaultState>{
	
	/**
	 * This action releases all controlled flights from the GDP, using
	 * the flighthandler's release function.
	 * @see DefaultFlightHandler
	 */
	@Override
	public DefaultState act(DefaultState state,FlightHandler myFlightHandler,Duration timeStep)
			throws Exception {
		//Create a list which will hold the updated sitting flights
		SortedSet<Flight> newSittingFlights = 
				new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETDFieldID));
		//Get the iterator for the sitting flights
		Iterator<Flight> myflightIterator=
				state.getFlightState().getSittingFlights().iterator();
		//Go through all the sitting flights
		while(myflightIterator.hasNext()){
			//Get the next flight.
			Flight nextFlight = myflightIterator.next();
			//Release the flight from the GDP
			nextFlight = myFlightHandler.release(nextFlight,state.getCurrentTime());
			//Add the flight to our new list
			newSittingFlights.add(nextFlight);
		}
		//Update the flights state
		//Sitting flights are changed, all other flights remain the same
		FlightState newFlights = state.getFlightState().setSittingFlights(newSittingFlights);
		return state.setFlightState(newFlights);
	}

}
