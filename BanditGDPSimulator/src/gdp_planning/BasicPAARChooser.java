package gdp_planning;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import function_util.BiFunctionEx;
import state_representation.DefaultState;

/**
 * This sets all PAARs from the current time forward to be the 
 * currently observed capacity.
 * @author Alex2
 *
 * @param <T> the type of the state object that is being used.
 */
public class BasicPAARChooser implements BiFunction<DefaultState, Interval, SortedMap<DateTime,Integer>>, 
BiFunctionEx<DefaultState, Interval, SortedMap<DateTime, Integer>, Exception>{


	/**
	 * This sets all PAARs in the time interval to be the same as the current capacity
	 */
	@Override
	public SortedMap<DateTime, Integer> apply(DefaultState state,
			Interval GDPInterval) {
		//Create the PAAR map
		SortedMap<DateTime,Integer> myMap = new TreeMap<DateTime,Integer>();
		myMap.put(GDPInterval.getStart(),state.getCapacity());
		//Return the map
		return myMap;
	}

}
