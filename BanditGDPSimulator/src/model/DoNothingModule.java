package model;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_update.FlightHandler;

/**
 * This is an implementation of StateAction
 * in which the action is to do nothing.
 * @author Alex2
 *
 * @param <T> the type of state in use
 */
public class DoNothingModule<T> implements 
StateAction<T>, OtherStateAction<T> {


	/**
	 * Simply returns the input state.
	 */
	@Override
	public T act(T state,FlightHandler flightHandler, Duration timeInterval) {
		return state;
	}

	@Override
	public T act(T state, DateTime currentTime, Duration timeStep)
			throws Exception {
		return state;
	}


}
