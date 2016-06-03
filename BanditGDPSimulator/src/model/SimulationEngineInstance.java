package model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;

import state_criteria.StateCriteria;
import state_update.FlightHandler;


/**
 * This is the core logic for the ground delay simulation.
 * @author Alex2
 *
 * @param <T> the type of state in use
 */
public class SimulationEngineInstance<T> {
	private final List<ImmutablePair<StateCriteria<T>,StateAction<T>>> modules;
	private final StateCriteria<T> endCriteria;
	private final FlightHandler flightHandler;
	private final T initialState;
	
	/**
	 * Standard constructor. Deep copy of the list of modules is used,
	 * so as long as the modules and the state criteria are immutable,
	 * then this class will also be immutable.
	 * @param modules - a list of pairs of criteria and corresponding modules.
	 * @param endCriteria - the criteria to stop the simulation
	 * @param startTime - the start time of the simulation
	 * @param timeStep - the time step.
	 */
	public SimulationEngineInstance( 
			List<ImmutablePair<StateCriteria<T>,StateAction<T>>> modules, 
			StateCriteria<T> endCriteria,
			FlightHandler flightHandler,
			T initialState){
		this.endCriteria = endCriteria;
		this.modules = new ArrayList<ImmutablePair<StateCriteria<T>,StateAction<T>>>(modules);
		this.flightHandler = flightHandler;
		this.initialState = initialState;
	}
	
	public List<ImmutablePair<StateCriteria<T>,StateAction<T>>> getModules(){
		return new ArrayList<ImmutablePair<StateCriteria<T>,StateAction<T>>>(modules);
	}
	
	public StateCriteria<T> getEndCriteria() {
		return endCriteria;
	}

	public FlightHandler getFlightHandler() {
		return flightHandler;
	}

	public T getInitialState() {
		return initialState;
	}

	public SimulationEngineInstance<T> setStartTime(DateTime time){
		return new SimulationEngineInstance<T>(modules,
				endCriteria, flightHandler, initialState);
	}
	
	public SimulationEngineInstance<T> setModules(List<ImmutablePair<StateCriteria<T>,StateAction<T>>> modules){
		return new SimulationEngineInstance<T>(modules,
				endCriteria, flightHandler, initialState);
	}
	
}
