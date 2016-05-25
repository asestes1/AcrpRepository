package model;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public interface OtherStateAction<T> {
	public T act(T state, DateTime currentTime,Duration timeStep) throws Exception;
	
}
