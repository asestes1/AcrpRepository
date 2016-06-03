package model;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_criteria.StateCriteria;
import state_representation.DefaultState;
import state_update.FlightHandler;

public final class SimulationEngineRunner {
	private SimulationEngineRunner(){
		
	}
	
	/**
	 * This runs the simulation and logs intermediate steps.
	 * @param state - the starting state
	 * @param out - a PrintStream to log intermediate steps.
	 * @param verbosity - determines how much is logged. Currently binary,
	 * if 1 outputs the state at the end of each iteration, if 0 outputs
	 * only at the end of simulation
	 * @return - the final state
	 * @throws Exception an exception is thrown if one of the modules or
	 * criteria throws an exception.
	 */
	public static <T extends TimeState> T run(SimulationEngineInstance<T> instance, Duration timeStep, PrintStream out, int verbosity)
			throws Exception {
		T state = instance.getInitialState();
		List<ImmutablePair<StateCriteria<T>,StateAction<T>>> modules = instance.getModules();
		DateTime currentTime = state.getCurrentTime();
		FlightHandler flightHandler = instance.getFlightHandler();
		StateCriteria<T> endCriteria = instance.getEndCriteria();
		// Run until we reach the end time
		while (!endCriteria.isSatisfied(state)){
			List<ImmutablePair<StateCriteria<T>,StateAction<T>>> updatedModules = new LinkedList<ImmutablePair<StateCriteria<T>,StateAction<T>>>();
			T newState = state;
			for(ImmutablePair<StateCriteria<T>,StateAction<T>> myModule: modules){
				StateCriteria<T> myCriteria = myModule.getLeft();
				StateAction<T> myAction = myModule.getRight();
				if(myCriteria.isSatisfied(state)){
					newState = myAction.act(state,flightHandler,timeStep);
					if(verbosity == 3){
						out.println(state.toString());
					}
					/*
					if(verbosity >= 2 && (state instanceof DefaultState)){
						FlightState flights = ((DefaultState) state).getFlightState();
						out.println("Sitting: "+flights.getSittingFlights().size());
						out.println("Airborne: "+flights.getAirborneFlights().size());
						out.println("Landed: "+flights.getLandedFlights().size());
						out.println("Cancelled: "+flights.getCancelledFlights().size());
					}
					*/
				}
				StateCriteria<T> newCriteria = myCriteria.updateHistory(state);
				endCriteria = endCriteria.updateHistory(state);
				updatedModules.add(ImmutablePair.of(newCriteria, myAction));
				state=newState;
			}
			currentTime = currentTime.plus(timeStep);
			if(verbosity >=1 ){
				out.println(currentTime.toString());
			}
			if(verbosity == 4){
				System.out.println(((DefaultState) newState).getAirportState().getQueueLength());
			}
		}
		return state;
	}
	
	/**
	 * Runs a simulation and produces the ending state
	 * @param state - the starting state
	 * @return - the end state
	 * @throws Exception - throws an exception if one of the modules
	 * or criteria throws an exception.
	 */
	public static <T extends TimeState> T run(SimulationEngineInstance<T> instance, Duration timeStep) throws Exception {
		return run(instance,timeStep,null,0);
	}
}
