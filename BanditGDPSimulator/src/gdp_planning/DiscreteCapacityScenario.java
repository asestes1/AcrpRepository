package gdp_planning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is a discretized version of a capacity scenario. 
 * Instead of containing times at which the capacity changes,
 * a discrete capacity scenario describes the number of flights
 * which may arrive in each discrete time period.
 * @author Alex2
 *
 */
public class DiscreteCapacityScenario {
	private final double probability;
	private final List<Integer> capacity;
	
	/**
	 * Standard constructor. Deep-copy of capacity list is made 
	 * in order to make this class immutable.
	 * @param probability
	 * @param capacity
	 */
	public DiscreteCapacityScenario(double probability, List<Integer> capacity){
		this.probability = probability;
		this.capacity = new ArrayList<Integer>(capacity);
	}

	public double getProbability() {
		return probability;
	}

	public List<Integer> getCapacity() {
		return new ArrayList<Integer>(capacity);
	}
	
	@Override
	public String toString(){
		String myString = "P="+probability+", C:";
		Iterator<Integer> myIter = capacity.iterator();
		while(myIter.hasNext()){
			myString += myIter.next();
			if(myIter.hasNext()){
				myString+=",";
			}
		}
		return myString;
	}
	
	
}
