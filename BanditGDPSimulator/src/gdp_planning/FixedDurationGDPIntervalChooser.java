package gdp_planning;

import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import state_representation.DefaultState;

/**
 * This implementation of GDPIntervalChooser decides that the GDP starts
 * immediately and lasts a fixed amount of time.
 * 
 * @author Alex2
 *
 * @param <T>
 *            - the type of state in use.
 */
public class FixedDurationGDPIntervalChooser implements Function<DefaultState,Interval>{
	private final Duration duration;

	/**
	 * Standard constructor.
	 * 
	 * @param duration
	 *            - the duration of the GDP interval
	 */
	public FixedDurationGDPIntervalChooser(Duration duration) {
		this.duration = duration;
	}

	/**
	 * Chooses a GDPInterval which begins at the current time and lasts for a
	 * fixed amount of time.
	 */
	@Override
	public Interval apply(DefaultState state) {
		DateTime currentTime = state.getCurrentTime();
		return new Interval(currentTime, currentTime.plus(duration));
	}

}
