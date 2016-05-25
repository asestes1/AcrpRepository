package state_representation;

import java.util.Iterator;
import java.util.SortedMap;

import org.joda.time.DateTime;

public class DefaultCapacityComparer implements CapacityScenarioComparer {

	/**
	 * This checks to see if the capacity profiles of the scenarios are the same
	 * up to the current time.
	 * 
	 * @param other
	 * @param currentTime
	 * @return
	 */
	public boolean areEqual(CapacityScenario a, CapacityScenario b, DateTime currentTime) {
		SortedMap<DateTime, Integer> myMap = a.getCapacityMap();
		Iterator<DateTime> myCapIter = a.getCapacityMap().headMap(currentTime).keySet().iterator();
		SortedMap<DateTime, Integer> otherMap = b.getCapacityMap();
		Iterator<DateTime> theirCapIter = otherMap.headMap(currentTime).keySet().iterator();

		// First, we compare the capacities which have been observed
		while (myCapIter.hasNext()) {
			if (theirCapIter.hasNext()) {
				if (!(myMap.get(myCapIter.next()).equals(otherMap.get(theirCapIter.next())))) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (theirCapIter.hasNext()) {
			return false;
		}
		return true;
	}

}
