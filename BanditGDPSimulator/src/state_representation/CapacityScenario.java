package state_representation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import bandit_objects.Immutable;

public class CapacityScenario implements Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4562618189547194307L;
	private final double probability;
	private final SortedMap<DateTime, Integer> capacityMap;

	/**
	 * Constructor for the scenario class
	 * 
	 * @param probability
	 *            - the probability that the scenario occurs.
	 * @param capacityMap
	 *            - a map, the keys are times at which the capacity changes and
	 *            the values are the values that capacity changes to.
	 * @param gdpTimes
	 *            - the times at which the gdp planning module should be run
	 * @param responseTimes
	 *            - the times at which the airline response is run
	 * @param clearTime
	 *            - the time at which the weather clears
	 */
	public CapacityScenario(double probability,
			SortedMap<DateTime, Integer> capacityMap) {
		this.probability = probability;
		this.capacityMap = new TreeMap<DateTime, Integer>(capacityMap);

	}

	/**
	 * Returns the capacity at the current time in this scenario A note: if a
	 * time before the beginning of the capacity map is requested, then the
	 * method will throw an exception
	 * 
	 * @param time
	 * @return
	 */
	// TODO: Make this handle errors more gracefully
	public int getCurrentCapacity(DateTime time) {
		if (capacityMap.containsKey(time)) {
			return capacityMap.get(time);
		} else {
			return capacityMap.get(capacityMap.headMap(time).lastKey());
		}

	}

	/**
	 * This prints the information in the scenario out to a stream.
	 * 
	 * @param stream
	 */
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
		String string = "Probability: " + probability+"\n";
		string+= "Capacities: \n";
		Set<DateTime> keys = capacityMap.keySet();
		Iterator<DateTime> myIterator = keys.iterator();
		while (myIterator.hasNext()) {
			DateTime nextKey = myIterator.next();
			string += "Time of change: "+formatter.print(nextKey);
			string += "\t New Capacity: ";
			string += capacityMap.get(nextKey) +"\n";
		}
		return string;
	}

	public SortedMap<DateTime, Integer> getCapacityMap() {
		return new TreeMap<DateTime, Integer>(capacityMap);
	}

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * This scales the probability of the scenario by dividing it by the input
	 * double.
	 * 
	 * @param cumP
	 * @return
	 */
	public CapacityScenario setProbability(double p) {
		return new CapacityScenario(p, capacityMap);
	}
	
	public CapacityScenario setCapacities(SortedMap<DateTime,Integer> capacityMap){
		return new CapacityScenario(probability, new TreeMap<DateTime,Integer>(capacityMap));
	}

}
