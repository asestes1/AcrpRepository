package state_representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import bandit_objects.Immutable;

public class CapacityScenarioState implements Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8631758847373233265L;
	private final int capacity;
	private final CapacityScenario actualScenario;
	private final List<CapacityScenario> scenarios;
	public static final DateTime earliestDate = new DateTime(1900, 1, 1, 0, 0,DateTimeZone.UTC); 
	public CapacityScenarioState(int capacity) {
		this.capacity = capacity;
		SortedMap<DateTime,Integer> mySortedMap = new TreeMap<DateTime,Integer>();
		mySortedMap.put(earliestDate, capacity);
		this.actualScenario = new CapacityScenario(1.0, mySortedMap);
		this.scenarios = new ArrayList<CapacityScenario>();
		scenarios.add(actualScenario);
	}
	
	public CapacityScenarioState(int capacity, CapacityScenario actualScenario,
			List<CapacityScenario> scenarios) {
		this.capacity = capacity;
		this.actualScenario = actualScenario;
		this.scenarios = new ArrayList<CapacityScenario>(scenarios);
	}
	
	//------------------Getters --------------------------------------------
	public CapacityScenario getActualScenario(){
		return actualScenario;
	}
	
	public List<CapacityScenario> getScenarios() {
		return new ArrayList<CapacityScenario>(scenarios);
	}
	
	//-------------------------------Setters --------------------------------
	public CapacityScenarioState setActualScenario(CapacityScenario actualScenario){
		return new CapacityScenarioState(getCapacity(), actualScenario, scenarios);
	}
	
	public CapacityScenarioState setScenarios(List<CapacityScenario> scenarios){
		return new CapacityScenarioState(getCapacity(), actualScenario, scenarios);
	}
	
	/**
	 * This returns a capacity state with a randomly chosen scenario as the actual scenario
	 * @return
	 */
	public CapacityScenarioState setActualScenario(){
		Iterator<CapacityScenario> myIter = scenarios.iterator();
		boolean actualChosen = false;
		double randomNumber = Math.random();
		double newCumP = 0;

		CapacityScenario actualScenario = null;
		while (myIter.hasNext()) {
			CapacityScenario nextScenario = myIter.next();
			double probability = nextScenario.getProbability();
			newCumP += probability;
			
			// This assigns the actual scenario
			if (!actualChosen && newCumP > randomNumber) {
				actualScenario = nextScenario;
				actualChosen = true;
			}
		}
		return setActualScenario(actualScenario);
	}
	
	/**
	 * This prints the information in this class to the given PrintStream
	 * 
	 * @param stream
	 *            - the input PrintStream, should be open
	 */
	@Override
	public String toString() {
		String myString = "Actual scenario: \n";
		myString += actualScenario.toString();
		myString += "All scenarios: \n";
		Iterator<CapacityScenario> iter = scenarios.iterator();
		while (iter.hasNext()) {
			CapacityScenario nextScenario = iter.next();
			myString += nextScenario.toString();
			myString+="\n";
		}
		return myString;
	}
	
	public int getCapacity() {
		return capacity;
	}
	

	
}
