package state_representation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import bandit_objects.Immutable;

/**
 * This is an immutable class
 * @author Alex2
 *
 */
public class FlightState implements Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6729743078159572597L;

	private final Set<Flight> landedFlights;

	// The list of airborne flights should be sorted in ascending order of
	// actual time of arrival
	private final SortedSet<Flight> airborneFlights;

	// The list of sitting flights should be sorted in ascending order of actual
	// time of departure
	private final SortedSet<Flight> sittingFlights;
	
	private final HashSet<Flight> cancelledFlights;
	
	public FlightState(Set<Flight> landedFlights, SortedSet<Flight> airborneFlights,
			SortedSet<Flight> sittingFlights, Set<Flight> cancelledFlights){
		this.landedFlights = new HashSet<Flight>(landedFlights);
		this.airborneFlights = new TreeSet<Flight>(airborneFlights);
		this.sittingFlights = new TreeSet<Flight>(sittingFlights);
		this.cancelledFlights = new HashSet<Flight>(cancelledFlights);
	}

	//--------------------- Getters ----------------------
	public Set<Flight> getLandedFlights() {
		return new HashSet<Flight>(landedFlights);
	}

	public SortedSet<Flight> getAirborneFlights() {
		return new TreeSet<Flight>(airborneFlights);
	}

	public SortedSet<Flight> getSittingFlights() {
		return new TreeSet<Flight>(sittingFlights);
	}
	
	public Set<Flight> getCancelledFlights(){
		return new HashSet<Flight>(cancelledFlights);
	}
	
	//-------------------- Setters ------------------------
	public FlightState setLandedFlights(Set<Flight> landedFlights){
		return new FlightState(landedFlights,airborneFlights,sittingFlights,cancelledFlights);
	}
	
	public FlightState setAirborneFlights(SortedSet<Flight> airborneFlights){
		return new FlightState(landedFlights,airborneFlights,sittingFlights,cancelledFlights);
	}
	
	public FlightState setSittingFlights(SortedSet<Flight> sittingFlights){
		return new FlightState(landedFlights,airborneFlights,sittingFlights,cancelledFlights);
	}
	
	public FlightState setCancelledFlights(Set<Flight> cancelledFlights){
		return new FlightState(landedFlights,airborneFlights,sittingFlights,cancelledFlights);
	}
	
	@Override
	public String toString(){
		String myString = "Sitting flights: \n";
		Iterator<Flight> myIterator = sittingFlights.iterator();
		while(myIterator.hasNext()){
			myString += myIterator.next().toString()+"\n";
		}
		
		myString += "Airborne flights: \n";
		myIterator = airborneFlights.iterator();
		while(myIterator.hasNext()){
			myString += myIterator.next().toString()+"\n";
		}
		
		myString += "Landed flights: \n";
		myIterator = landedFlights.iterator();
		while(myIterator.hasNext()){
			myString += myIterator.next().toString()+"\n";
		}
		return myString;
	}
}
