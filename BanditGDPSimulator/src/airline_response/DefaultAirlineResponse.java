package airline_response;

import org.joda.time.Duration;

import model.StateAction;
import state_representation.DefaultState;
import state_representation.FlightState;
import state_update.FlightHandler;

/**
 * This is the default airline response module implemented
 * for the ground delay program simulator. This class is
 * basically a wrapper for the VakiliBallAirlineResponse class
 * 
 * @see VakiliBallAirlineResponse.java
 * @author Alex2
 *
 */
public class DefaultAirlineResponse
implements StateAction<DefaultState>{
	private final String logFileName;
	private final double flightDelayCost;
	private final double passengerDelayCost;
	private final Duration timeAllowed; 

	/**
	 * Default constructor.
	 * @param myFlightHandler - a flight handler, which is used to reassign flights and 
	 * add appropriate random delays.
	 * @param logFile - the file which gurobi will use as the log file.
	 * @param myFlightHandler
	 * @param logFileName
	 * @param flightDelayCost
	 * @param passengerDelayCost
	 * @param timeAllowed
	 */
	public DefaultAirlineResponse(String logFileName,
			double flightDelayCost, double passengerDelayCost, Duration timeAllowed){
		this.logFileName = logFileName;
		this.flightDelayCost = flightDelayCost;
		this.passengerDelayCost = passengerDelayCost;
		this.timeAllowed = timeAllowed;
	}
	
	/**
	 * This calls the vakili-ball airline response model and applies it to the state.
	 * @throws Exception 
	 */
	@Override
	public DefaultState act(DefaultState state,FlightHandler flightHandler,Duration timeStep) throws Exception {
		//Create the response solver
		VakiliBallAirlineResponse myResponse = 
				new VakiliBallAirlineResponse(logFileName,
				flightDelayCost,passengerDelayCost,timeAllowed);
		//Apply the response module
		FlightState myFlightState = myResponse.respond(flightHandler,state.getFlightState(),
				state.getCurrentTime());
		//Return the results
		return state.setFlightState(myFlightState);
	}
	
	
}
