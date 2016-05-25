package state_representation;

import gdp_planning.DiscreteCapacityScenario;

import java.util.Comparator;
import java.util.Iterator;

public class TimeCapacityScenarioComparator<S extends DiscreteCapacityScenario> implements Comparator<S>{

	@Override
	public int compare(S o1, S o2) {
		int difference = 0;
		Iterator<Integer> caps1 = o1.getCapacity().iterator();
		Iterator<Integer> caps2 = o2.getCapacity().iterator();
		while(difference == 0 && caps1.hasNext() && caps2.hasNext()){
			difference = caps1.next() - caps2.next();
		}
		if(difference == 0){
			difference = o1.getCapacity().size() - o2.getCapacity().size();
		}
		if(difference == 0){
			if(o1.getProbability() < o2.getProbability()){
				return -1;
			}else if(o1.getProbability() > o2.getProbability()){
				return 1;
			}
		}
		return difference;
	}
	
}
