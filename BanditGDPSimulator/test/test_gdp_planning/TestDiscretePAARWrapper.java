package test_gdp_planning;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;

import gdp_planning.DefaultDiscretePAARChooserWrapper;
import gdp_planning.DiscreteCapacityScenario;
import gdp_planning.DiscreteScenarioUtilities;
import state_factories.CapacityScenarioFactory;
import state_factories.DateTimeFactory;
import state_representation.CapacityScenarioState;

public class TestDiscretePAARWrapper {
	@Test
	public void testDiscreteToContinuousPAARs(){
		DefaultDiscretePAARChooserWrapper myWrapper =
				new DefaultDiscretePAARChooserWrapper(Duration.standardMinutes(20),
				null);
		DateTime startTime = DateTimeFactory.parse("2012/10/10 10:00",DateTimeZone.UTC);
		DateTime endTime = startTime.plus(Duration.standardHours(5));
		Interval myInterval = new Interval(startTime,endTime);
		List<Integer> myDiscretePAARs = new ArrayList<Integer>();
		for(int i =0; i < 20;i++){
			myDiscretePAARs.add(i);
		}
		SortedMap<DateTime,Integer> continuousPAARs = 
				DiscreteScenarioUtilities.discreteToContinuousPAARs(myInterval, myDiscretePAARs,
						myWrapper.getTimePeriodDuration());
		System.out.println("Discrete-to-Continuous-PAARs test: ");
		System.out.println(continuousPAARs.toString());
	}
	
	@Test
	public void testDiscretizeScenarios(){
		DefaultDiscretePAARChooserWrapper myWrapper =
				new DefaultDiscretePAARChooserWrapper(Duration.standardMinutes(1),
				null);
		DateTime startTimeCap =  DateTimeFactory.parse("2012/10/10 9:00",DateTimeZone.UTC);
		CapacityScenarioState myScenarios = CapacityScenarioFactory.parseLoToHigh(
				startTimeCap, Duration.standardHours(3),Duration.standardHours(4),
				Duration.standardMinutes(60), 30, 60);
		DateTime startTime =  DateTimeFactory.parse("2012/10/10 10:00",DateTimeZone.UTC);
		DateTime endTime = startTime.plus(Duration.standardHours(5));
		Interval myInterval = new Interval(startTime,endTime);
		List<DiscreteCapacityScenario> scenarios = 
				DiscreteScenarioUtilities.discretizeScenarios(myScenarios, myInterval,
						myWrapper.getTimePeriodDuration());
		System.out.println("Discretize Scenarios test: ");
		System.out.println(scenarios.toString());
	}
}
