package state_representation;

import java.io.Serializable;

import org.joda.time.DateTime;

import bandit_objects.Immutable;
import model.TimeState;

/**
 * This is the default implementation of the state class
 * @author Alex2
 *
 */
public class DefaultState implements CapacityState, TimeState, Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1464096989442762137L;
	private final DateTime currentTime;
	private final FlightState flightState;
	private final AirportState airportState;
	private final CapacityScenarioState capacityState;
	
	/**
	 * Standard constructor
	 * @param currentTime
	 * @param flights
	 * @param airport
	 * @param weather
	 */
	public DefaultState(DateTime currentTime, FlightState flightState,
			AirportState airportState,CapacityScenarioState capacityState){
		this.currentTime = currentTime;
		this.flightState = flightState;
		this.airportState = airportState;
		this.capacityState = capacityState;
	}


	
	@Override
	public String toString(){
		String myString = "Flight State: \n";
		myString += flightState.toString() + "\n";
		myString += "Airport State: \n";
		myString += airportState.toString() + "\n";
		myString += "Capacity Conditions: \n";
		myString += capacityState.toString()+"\n";
		return myString;
	}
	
	@Override
	public int getCapacity() {
		return capacityState.getCapacity();
	}



	public FlightState getFlightState() {
		return flightState;
	}



	public AirportState getAirportState() {
		return airportState;
	}



	public CapacityScenarioState getCapacityState() {
		return capacityState;
	}
	
	public DateTime getCurrentTime(){
		return currentTime;
	}
	//--------- Setters ---------------
	public DefaultState setFlightState(FlightState flightState){
		return new DefaultState(currentTime,flightState,airportState,capacityState);
	}
	
	public DefaultState setAirportState(AirportState airportState){
		return new DefaultState(currentTime,flightState,airportState,capacityState);
	}
	
	public DefaultState setCapacity(CapacityScenarioState capacityState){
		return new DefaultState(currentTime,flightState,airportState,capacityState);
	}

}
