package state_representation;

import java.io.Serializable;

import bandit_objects.Immutable;

/**
 * @author Alex2
 *
 */
public class NASState implements Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4239561812713402883L;
	private final FlightState flightState;
	private final AirportState airportState;
	
	public NASState(FlightState flightState,AirportState airportState) {
		this.flightState = flightState;
		this.airportState = airportState;
	}

	@Override
	public String toString(){
		String myString = "Flights: \n";
		myString += flightState.toString()+"\n";
		myString += "Airport: \n";
		myString += airportState.toString()+"\n";
		return myString;
	}
	
	//---------Getters -----------------------
	public FlightState getFlightState() {
		return flightState;
	}

	public AirportState getAirportState() {
		return airportState;
	}
	
	//---------Setters -----------------------
	public NASState setFlightState(FlightState flightState) {
		return new NASState(flightState,airportState);
	}

	public NASState setAirportState(AirportState airportState) {
		return new NASState(flightState,airportState);
	}
	

	
}
