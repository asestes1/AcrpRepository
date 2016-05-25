package model;

import org.joda.time.Duration;

import state_update.FlightHandler;

public interface StateAction<T> {
	public T act(T state,FlightHandler flightHandler, Duration timeStep) throws Exception;
}
