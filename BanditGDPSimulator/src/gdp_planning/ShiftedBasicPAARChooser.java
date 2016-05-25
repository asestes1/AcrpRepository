package gdp_planning;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import function_util.BiFunctionEx;
import state_representation.DefaultState;

public class ShiftedBasicPAARChooser
 implements BiFunction<DefaultState, Interval, SortedMap<DateTime, Integer>>, 
 BiFunctionEx<DefaultState, Interval, SortedMap<DateTime, Integer>, Exception> {
	private final int shift;
	
	public ShiftedBasicPAARChooser(int shift){
		this.shift = shift;
	}
	/**
	 * This sets all PAARs in the time interval to be the same as the current capacity
	 */
	@Override
	public SortedMap<DateTime, Integer> apply(DefaultState state,
			Interval GDPInterval) {
		//Create the PAAR map
		SortedMap<DateTime,Integer> myMap = new TreeMap<DateTime,Integer>();
		myMap.put(GDPInterval.getStart(),state.getCapacity()+shift);
		//Return the map
		return myMap;
	}

}
