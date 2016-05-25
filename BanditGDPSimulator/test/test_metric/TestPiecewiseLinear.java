package test_metric;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import metrics.MetricCalculator;
import metrics.PiecewiseLinearFunction;

import org.joda.time.Duration;
import org.junit.Test;

import state_representation.Flight;

public class TestPiecewiseLinear {

	@Test
	public void TestPLF(){
		SortedMap<Double,Double> myFunctionMap1 = new TreeMap<Double,Double>();
		myFunctionMap1.put(0.0,1.0);
		myFunctionMap1.put(30.0, 2.0);
		PiecewiseLinearFunction myFunction1 = new PiecewiseLinearFunction(myFunctionMap1,0.0);

		System.out.println(myFunction1.evaluateAt(70));
	}
	
	@Test
	public void TestAggregate() throws Exception{
		SortedMap<Double,Double> myFunctionMap1 = new TreeMap<Double,Double>();
		myFunctionMap1.put(0.0,1.0);
		myFunctionMap1.put(30.0, 2.0);
		PiecewiseLinearFunction myFunction1 = new PiecewiseLinearFunction(myFunctionMap1,0.0);
		Flight myFlight = new Flight("0", 100,200, null, null,
				null, null, null, null, false, true, false, false,
				Duration.ZERO,Duration.ZERO, Duration.ZERO, Duration.standardMinutes(70), Duration.ZERO);
		Set<Flight> myFlights = new HashSet<Flight>();
		myFlights.add(myFlight);
		System.out.println(MetricCalculator.calculateFunctionOfField(myFlights, myFunction1, Flight.scheduledDelayID));
	}
}
