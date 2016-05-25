package airline_response;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_representation.Flight;
import state_representation.FlightState;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;

/**
 * This class implements the airline response model described in
 * Vakili and Ball 2009. This uses Gurobi to solve an integer program.
 * @author Alex2
 *
 */
public class VakiliBallAirlineResponse {
	private final double flightDelayCost;
	private final double passengerDelayCost;
	private final Duration timeAllowed; 
	GRBEnv myEnv ;


	/**
	 * Default constructor.
	 * @param myFlightHandler - a flight handler, which is used to reassign flights and 
	 * add appropriate random delays.
	 * @param logFileName - the name of the file which Gurobi will use.
	 * @param flightDelayCost - the cost of one minute of flight delay
	 * @param passengerDelayCost - the cost of one minute of passenger delay
	 * @param timeAllowed - this is a duration of time before lateness costs anything. 
	 * For example, if a duration of 15 minutes is passed in, then a flight which arrives
	 * 17 minutes late will only have delay costs for 2 minutes of delay.
	 * @throws GRBException 
	 */
	public VakiliBallAirlineResponse(String logFileName,
			double flightDelayCost, double passengerDelayCost, Duration timeAllowed) throws GRBException{
		this.flightDelayCost = flightDelayCost;
		this.passengerDelayCost = passengerDelayCost;
		this.timeAllowed = timeAllowed;
		this.myEnv = new GRBEnv(logFileName);
	}

	/**
	 * This carries out the Vakili-Ball airline response model on the input flight state.
	 * @param flights - the state of all flights in the simulation
	 * @param currentTime - the current time of the simulation
	 * @return - the state of the flights in the simulation with the response applied
	 * @throws Exception 
	 */
	public FlightState respond(FlightHandler flightHandler,FlightState flights,
			DateTime currentTime) throws Exception {
		//Divide the flights by airline
		Map<String,Set<Flight>> airlineFlights = divideFlightsByAirline(flights.getSittingFlights());
		
		//Get the set of airlines
		Set<String> airlines = airlineFlights.keySet();
		
		//Allocate sets for updated flights
		SortedSet<Flight> nextSittingFlights = 
				new TreeSet<Flight>(new FlightDateTimeFieldComparator(Flight.aETDFieldID));
		Set<Flight> nextCancelledFlights = new HashSet<Flight>(flights.getCancelledFlights());
		
		//Go through all the airlines
		for(String nextAirline:airlines){	
			//Carry out the swaps. This also adds flights to the flight sets.
			airlineSwaps(flightHandler,airlineFlights.get(nextAirline),
					nextSittingFlights,nextCancelledFlights,currentTime);
		}
		
		//Return a flight state with the new sets of flights
		return new FlightState(flights.getLandedFlights(),flights.getAirborneFlights(),
				nextSittingFlights,nextCancelledFlights);
	}

	/**
	 * This performs the swaps for a given airline, using an integer program 
	 * described in Vakili & Ball 2009
	 * @param flights - the set of controlled flights which we will be swapping.
	 * @param nextSittingFlights - a set which contains the sitting flights. 
	 * Flights will be added to this.
	 * @param nextCancelledFlights - a set which contains the cancelled flights. 
	 * Flights will be added to this.
	 * @throws Exception 
	 */
	private void airlineSwaps(FlightHandler myFlightHandler,Set<Flight> flights,
			SortedSet<Flight> nextSittingFlights,
			Set<Flight> nextCancelledFlights, DateTime currentTime) throws Exception {
			//Start gurobi environment
			
			//Set up IP
			GRBModel myModel = setUpModel(myEnv,flights);
			
			//Solve IP
			myModel.optimize();
			
			//Apply the solution to the flights
			readSolution(myFlightHandler,myModel,flights,nextSittingFlights,
					nextCancelledFlights, currentTime);
	}
	
	/**
	 * This function takes the Gurobi IP solution and applies it
	 * to the flights.
	 * @param myModel - the Gubori Model, with solution. Should already be optimized
	 * @param flights - the flights that will be updated
	 * @param nextSittingFlights - the set to add the updated flights
	 * @param nextCancelledFlights - the set to add cancelled flights
	 * @param currentTime - the current time
	 * @throws Exception 
	 */
	private void readSolution(FlightHandler myFlightHandler,GRBModel myModel, Set<Flight> flights,
			SortedSet<Flight> nextSittingFlights,
			Set<Flight> nextCancelledFlights,DateTime currentTime) throws Exception {
		
		//Turn the set into a list so that we can refer to it by index
		List<Flight> myFlightList = new ArrayList<Flight>(flights);
		int size =myFlightList.size();
		
		//For each flight, find the slot it was assigned to.
		//Index i is for flights
		for(int i =0; i <size; i++){
			boolean assigned  = false;

			//Get the next flight
			Flight nextFlight = myFlightList.get(i);
			
			//Index j is for slots
			for(int j =0; j < size; j++){
				Flight nextSlot = myFlightList.get(j);
				if(!nextFlight.getOrigETA().isAfter(nextSlot.getcETA())){
					Duration delayTime = new Duration(nextFlight.getOrigETA(),nextSlot.getcETA());
					
					//We can't delay a flight longer than it's max delay
					if(!delayTime.isLongerThan(nextFlight.getMaxDelay())){
				
						//Get the next slot
						
						// Get the gurobi indicating the assignment of flight i to slot j
						GRBVar flightVar = myModel.getVarByName(
								"SF"+nextFlight.getFlightNumber()+
								",F"+nextSlot.getFlightNumber());
						
						//Check the value of this indicator value
						double value = flightVar.get(GRB.DoubleAttr.X);
						
						//If the value is 1, then we have assigned flight i to slot j
						if(Math.abs(value - 1.0) < .0001){
							//Carry out that assignment
							assigned = true;
							nextSittingFlights.add(myFlightHandler.controlArrival(nextFlight,nextSlot.getcETA(),currentTime));
						}
					}
				}
			}
			
			//Get the variable corresponding to cancellation of flight i
			GRBVar cancelVar = myModel.getVarByName("C"+nextFlight.getFlightNumber());
			
			//Get the value of that variable
			double value = cancelVar.get(GRB.DoubleAttr.X);
			
			//If the value of that variable is one, then we cancel flight i
			if(Math.abs(value-1.0)<.0001){
				//Perform this cancellation
				assigned = true;
				nextCancelledFlights.add(myFlightHandler.cancel(nextFlight));
			}
			if(!assigned){
				throw new Exception("Error: unassigned flight; "+nextSittingFlights.toString());
			}
		}

	}
	
	/**
	 * This initializes the integer program for the Vakili Ball model.
	 * @param myEnv - the gurobi environment
	 * @param flights - the set of relevant flights for an airline
	 * @return the gurobi IP model
	 * @throws GRBException - an exception is thrown if there are any problems updating the IP
	 */
	private GRBModel setUpModel(GRBEnv myEnv, Set<Flight> flights) throws GRBException{
		List<Flight> myFlightList = new ArrayList<Flight>(flights);
		GRBModel myModel = new GRBModel(myEnv);
		//These constraints ensure that every flight is assigned to exactly one slot
		GRBLinExpr[] flightConstraints = new GRBLinExpr[flights.size()];
		//These constraints ensure that every slot is given at most one flight
		GRBLinExpr[] slotConstraints  = new GRBLinExpr[flights.size()];
		
		for(int i =0; i < flights.size(); i++){
			flightConstraints[i] = new GRBLinExpr();
			slotConstraints[i] = new GRBLinExpr();
		}
		//Set up model
		//Index i for flights
		int size = myFlightList.size();

		for(int i = 0; i < size; i++){
			//Take a flight
			Flight nextFlight = myFlightList.get(i);
			
			//Index j for slots
			for(int j =0; j < size;j++){
				
				//Take a second flight
				Flight otherFlight = myFlightList.get(j);
				
				//We can't allocate a flight to a slot which occurs before its original ETA
				//TODO: consider changing this to earliest ETA.
				if(!nextFlight.getOrigETA().isAfter(otherFlight.getcETA())){
					
					//Find the delay incurred by moving the first flight to the second flight
					Duration delayTime = new Duration(nextFlight.getOrigETA(),otherFlight.getcETA());
					
					//We can't delay a flight longer than it's max delay
					if(!delayTime.isLongerThan(nextFlight.getMaxDelay())){
						double objective = 0;
						
						//Calculate the objective value from delaying flight A to the time of flight B
						if(delayTime.isLongerThan(timeAllowed)){
							objective = 
									(flightDelayCost+passengerDelayCost*nextFlight.getNumPassengers())
									*(delayTime.minus(timeAllowed).getStandardMinutes());
						}
						
						//Create a gurobi variable which indicates whether flight i is assigned to slot j
						GRBVar var = myModel.addVar(0.0, 1.0, objective, GRB.BINARY, 
								"SF"+nextFlight.getFlightNumber()+
								",F"+otherFlight.getFlightNumber());
						
						//Add the assignment variables into the constraints
						flightConstraints[i].addTerm(1.0, var);
						slotConstraints[j].addTerm(1.0, var);
					}
				}
			}
			//Calculate the objective value of cancel the flight
			double cancelObjective = (flightDelayCost+
					passengerDelayCost*nextFlight.getNumPassengers())*
					(nextFlight.getMaxDelay().minus(timeAllowed).getStandardMinutes());
			
			//Create a gurobi variable which indicates whether flight i is cancelled
			GRBVar cancelVar = myModel.addVar(0.0, 1.0,cancelObjective, GRB.BINARY,
					"C"+nextFlight.getFlightNumber());
			
			//Add the cancellation variables into the constraints
			flightConstraints[i].addTerm(1.0, cancelVar);
		}
		
		//Add the constraints to the model
		myModel.update();
		for(int i=0; i < size;i++){
			myModel.addConstr(flightConstraints[i],GRB.EQUAL , 1.0, "FLIGHT"+i);
			myModel.addConstr(slotConstraints[i], GRB.LESS_EQUAL, 1.0 ,"SLOT"+i);
		}
		//Update the model so that the constraints will be added
		myModel.update();

		return myModel;
	}

	/**
	 * This divides the flights into sets based on the airline. This generates
	 * a map from the airline's name to the set of controlled flights under
	 * that airline
	 * @param flights the set of flights
	 * @return the map of airline names to flights
	 */	
	private Map<String,Set<Flight>> divideFlightsByAirline(Set<Flight> flights){
		//Initialize the mapping from airlines to flights
		Map<String,Set<Flight>> myMap = new HashMap<String,Set<Flight>>();
		
		//Go through the flights 
		Iterator<Flight> myIterator = flights.iterator();
		while(myIterator.hasNext()){
			//Get the next flight
			Flight nextFlight = myIterator.next();
			
			//Get the airline of the flight
			String airlineId = nextFlight.getAirlineId();
			
			//If the flight is controlled 
			if(nextFlight.isGdpDelayed()){
				//If the airline doesn't have a flight yet, initialize its set of flights
				if(!myMap.containsKey(nextFlight.getAirlineId())){
					myMap.put(airlineId, new HashSet<Flight>());
				}
				//Add the flight to the set of flights mapped from the airline
				myMap.get(airlineId).add(nextFlight);
			}
		}
		//Return the mapping.
		return myMap;
	}
	
}
