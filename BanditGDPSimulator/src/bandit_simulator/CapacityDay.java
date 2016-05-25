package bandit_simulator;

import java.util.Map;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.joda.time.LocalDate;

public class CapacityDay {
	private final LocalDate date;
	private final Map<Integer,IntegerDistribution> myDistribution;
	
	public CapacityDay(LocalDate date, Map<Integer, IntegerDistribution> myDistribution) {
		super();
		this.date = date;
		this.myDistribution = myDistribution;
	}

	public LocalDate getDate(){
		return date;
	}

	public Map<Integer, IntegerDistribution> getMyDistribution() {
		return myDistribution;
	}
	
	
}
