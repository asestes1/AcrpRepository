package gdp_planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import state_representation.CapacityScenario;
import state_representation.CapacityScenarioComparer;
import state_representation.CapacityScenarioState;
import state_representation.TimeCapacityScenarioComparator;

public final class DiscreteScenarioUtilities {
	private DiscreteScenarioUtilities(){
		
	}
	/**
	 * This takes a discrete PAAR and turns it into continuous PAARs
	 * @param solveInterval - the interval which the PAAR chooser operates on.
	 * @param gdpInterval - the interval during which the GDP planning occurs.
	 * @param discretePaars
	 * @return
	 */
	public static SortedMap<DateTime, Integer> discreteToContinuousPAARs(
			Interval gdpInterval,
			List<Integer> discretePaars,
			Duration timePeriodDuration) {
		DateTime currentTime = gdpInterval.getStart();
		SortedMap<DateTime,Integer> myPAARs = new TreeMap<DateTime,Integer>();
		int index = 0;
		while(currentTime.isBefore(gdpInterval.getEnd())){
			Double hourlyRate = discretePaars.get(index)*
					((double) Duration.standardHours(1).getMillis()/timePeriodDuration.getMillis());
			myPAARs.put(currentTime, hourlyRate.intValue());
			currentTime = currentTime.plus(timePeriodDuration);
			index++;
		}
		return myPAARs;
	}

	/**
	 * This takes a continuous capacity scenario and produces a discrete capacity scenario
	 * @param scenarios
	 * @param timePeriodDuration
	 * @param solveInterval
	 * @return
	 */
	public static List<DiscreteCapacityScenario> discretizeScenarios(CapacityScenarioState scenarioState, Interval solveInterval,
			Duration timePeriodDuration) {
		List<CapacityScenario> scenarios = scenarioState.getScenarios(); 
		List<DiscreteCapacityScenario> newScenarios = new ArrayList<DiscreteCapacityScenario>();
		Iterator<CapacityScenario> scenarioIterator = scenarios.iterator();
		while(scenarioIterator.hasNext()){
			CapacityScenario nextScenario = scenarioIterator.next();
			DiscreteCapacityScenario discreteScenario = discretizeScenario(
					nextScenario, solveInterval,timePeriodDuration);
			newScenarios.add(discreteScenario);
		}
		return newScenarios;
	}

	public static DiscreteCapacityScenario discretizeScenario(
			CapacityScenario nextScenario,Interval solveInterval, Duration timePeriodDuration) {
		//We start at the first time period
		DateTime currentStart = solveInterval.getStart();
		DateTime currentEnd = currentStart.plus(timePeriodDuration);
		
		//Our ending time will be the end of our time horizon
		DateTime end = solveInterval.getEnd();
		
		//Get our capacity map from our scenario
		SortedMap<DateTime,Integer> capacities = nextScenario.getCapacityMap();
		
		//Loop through the time periods
		List<Integer> discreteCapacities = new ArrayList<Integer>();
		Double allCapacity = 0.0;
		int allIntCapacity =0;
		while(currentStart.isBefore(end)){
			//Get the places where the capacity changes in our current time period
			SortedMap<DateTime,Integer> subMap = capacities.subMap(currentStart, currentEnd);
			List<DateTime> capacityChanges = new ArrayList<DateTime>(subMap.keySet());
			Collections.sort(capacityChanges);
			int numChanges = capacityChanges.size();
			double startCapacity = nextScenario.getCurrentCapacity(currentStart);
			if(numChanges == 0){
				allCapacity += startCapacity*((double) timePeriodDuration.getMillis()/
						Duration.standardHours(1).getMillis());
			}else{
				allCapacity += startCapacity*((double) new Duration(currentStart,capacityChanges.get(0)).getMillis()/
						Duration.standardHours(1).getMillis());
			}
			for(int i =0; i < numChanges;i++){
				DateTime startChange = capacityChanges.get(i);
				DateTime endChange;
				if(i < numChanges-1){
					endChange = capacityChanges.get(i+1);
				}else{
					endChange = currentEnd;
				}
				double fraction = ((double) new Duration(startChange,endChange).getMillis())/
						Duration.standardHours(1).getMillis();
				allCapacity += subMap.get(capacityChanges.get(i))*fraction;
			}
			int newCapacity = allCapacity.intValue() - allIntCapacity;
			allIntCapacity += newCapacity;
			discreteCapacities.add(newCapacity);
			currentStart = currentStart.plus(timePeriodDuration);
			currentEnd = currentEnd.plus(timePeriodDuration);
		}
		return new DiscreteCapacityScenario(nextScenario.getProbability(), discreteCapacities);
		
	}
	
	public static List<Set<Set<Integer>>> buildDiscreteScenarioTree(List<CapacityScenario> scenarios,
			CapacityScenarioComparer myScenarioComparer, Duration timePeriodDuration,
			Interval interval){
		DateTime startTime = interval.getStart();
		DateTime endTime = interval.getEnd();
		DateTime currentStart = startTime;
		DateTime currentEnd = startTime.plus(timePeriodDuration);
		List<Set<Set<Integer>>> myList = new ArrayList<Set<Set<Integer>>>();
		HashSet<Integer> currentSet = new HashSet<Integer>();
		for(int i =0; i < scenarios.size();i++){
			currentSet.add(i);
		}
		myList.add(partitionSet(currentSet, scenarios, currentStart, myScenarioComparer));
		
		currentStart = currentStart.plus(timePeriodDuration);
		currentEnd = currentEnd.plus(timePeriodDuration);
		int count = 0;
		while(currentStart.isBefore(endTime)){
			Iterator<Set<Integer>> mySetIterator = myList.get(count).iterator();
			HashSet<Set<Integer>> nextPartition = new HashSet<Set<Integer>>();
			while(mySetIterator.hasNext()){
				nextPartition.addAll(partitionSet(mySetIterator.next(),
						scenarios, currentStart, myScenarioComparer));
			}
			myList.add(nextPartition);
			count++;
			currentStart = currentStart.plus(timePeriodDuration);
			currentEnd = currentEnd.plus(timePeriodDuration);
		}
		return myList;
		
	}
	
	/**
	 * This partitions the given set of scenarios into sets of scenarios that are indistinguishable at the given time.
	 * @param scenarioSet - the set of indices of the scenarios
	 * @param fullList - the list of scenarios
	 * @param currentStart - the start time 
	 * @param myScenarioComparer - the method of comparing scenarios
	 * @return
	 */
	public static Set<Set<Integer>> partitionSet(Set<Integer> scenarioSet, List<CapacityScenario> fullList,
			DateTime currentStart,CapacityScenarioComparer myScenarioComparer){
		//Initialize the partition
		Set<Set<Integer>> currentSet = new HashSet<Set<Integer>>();
		
		//Go through the scenarios in scenarioSet
		Iterator<Integer> myScenarioIter = scenarioSet.iterator();
		while(myScenarioIter.hasNext()){
			//Consider the next scenario.
			boolean assigned = false;
			int nextScenario = myScenarioIter.next();
			//Go through the sets in our partition, and check if the next scenario is indistinguishable from any set. If it is, then it belongs in that set.
			Iterator<Set<Integer>> myPartitionIter = currentSet.iterator();
			while(myPartitionIter.hasNext() && assigned == false){
				Set<Integer> nextSet = myPartitionIter.next();
				Iterator<Integer> myGroupIter = nextSet.iterator();
				if(myScenarioComparer.areEqual(fullList.get(myGroupIter.next()), 
						fullList.get(nextScenario), currentStart)){
					nextSet.add(nextScenario);
					assigned = true;
				}
			}
			//If the scenario does not belong in any of the existing sets of our partition, then we put it by itself.
			if(assigned == false){
				Set<Integer> newSet = new HashSet<Integer>();
				newSet.add(nextScenario);
				currentSet.add(newSet);
			}
		}
		return currentSet;
	}
	
	public static int getWorstScenario(List<DiscreteCapacityScenario> scenarios){
		Iterator<DiscreteCapacityScenario> myIterator = scenarios.iterator();
		DiscreteCapacityScenario currentWorst = null;
		int currentWorstIndex;
		if(!myIterator.hasNext()){
			return -1;
		}else{
			currentWorst = myIterator.next();
			currentWorstIndex = 0;
		}
		int currentIndex = 1;
		Comparator<DiscreteCapacityScenario> myComparator = 
				new TimeCapacityScenarioComparator<DiscreteCapacityScenario>();
		while(myIterator.hasNext()){
			DiscreteCapacityScenario nextScenario = myIterator.next();
			if(myComparator.compare(nextScenario, currentWorst) < 0){
				currentWorst = nextScenario;
				currentWorstIndex = currentIndex;
			}
			currentIndex++;
		}
		return currentWorstIndex;
	}
	
}
