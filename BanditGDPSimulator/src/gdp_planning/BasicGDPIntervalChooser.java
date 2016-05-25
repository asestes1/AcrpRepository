package gdp_planning;

import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import state_representation.DefaultState;

/**
 * This is an implementation of the GDPIntervalChooser class which
 * sets the GDP interval to be the interval from the current time
 * to the end time
 * @author Alex2
 *
 * @param <T> = the type of state object being used. Should extend/implement
 * TimeState
 */
public class BasicGDPIntervalChooser implements Function<DefaultState,Interval>{
	private final DateTime endTime;

	/**
	 * Standard constructor.
	 * @param endTime
	 * @return
	 */
	public BasicGDPIntervalChooser setEndTime(DateTime endTime){
		return new BasicGDPIntervalChooser(endTime);
	}
	
	/**
	 * This sets the GDP interval to be the interval from the current time to 
	 * the end time.
	 */
	@Override
	public Interval apply(DefaultState state) {
		return new Interval(state.getCurrentTime(),endTime);
	}
	
	
	public BasicGDPIntervalChooser(DateTime endTime) {
		this.endTime = endTime;
	}
	public DateTime getEndTime() {
		return endTime;
	}
}
