package bandit_run_tests;

import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.Duration;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import bandit_simulator.BanditRunFactory;
import bandit_simulator.RunStateFunction;
import model.Pair;
import state_representation.DefaultCapacityComparer;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.CapacityScenarioUpdate;
import state_update.DefaultFlightHandler;
import state_update.FlightHandler;
import state_update.NASStateUpdate;
import state_update.UpdateModule;
import state_update_factories.DefaultNasUpdateFactory;
import util_random.Distribution;
import util_random.UniformDurationDistribution;

public class RunStateFunctionTest {

	@Test
	public void testVaryRate() throws Exception{
		Map<DefaultState,Pair<SimpleTmiAction,Double>> outcomes = BanditRunFactory.readOutcomesFromFile(BanditRunTests.trainTmiFile);

		Duration timeStep = Duration.standardMinutes(1);

		double airDelayWeight = 1000.0;

		// Choose delay distributions and make the flight handler
		Distribution<Duration> depDelayDistribution = new UniformDurationDistribution(-5 * 60, 15 * 60,
				Duration.standardSeconds(1));
		Distribution<Duration> arrDelayDistribution = new UniformDurationDistribution(-15 * 60, 15 * 60,
				Duration.standardSeconds(1));
//		Distribution<Duration> depDelayDistribution = new ConstantDistribution<Duration>(Duration.ZERO);
//		Distribution<Duration> arrDelayDistribution =  new ConstantDistribution<Duration>(Duration.ZERO);
		FlightHandler myFlightHandler = new DefaultFlightHandler(depDelayDistribution, arrDelayDistribution);

		// Make update module
		NASStateUpdate myUpdate = DefaultNasUpdateFactory.makeDefault();
		UpdateModule myCompleteUpdate = new UpdateModule(myUpdate,
				new CapacityScenarioUpdate(new DefaultCapacityComparer()));
		RunStateFunction myNasRunner = new RunStateFunction(myFlightHandler, myCompleteUpdate, timeStep,
				airDelayWeight);
		DescriptiveStatistics myStats = new DescriptiveStatistics(); 
		for(DefaultState myState: outcomes.keySet()){
//			System.out.println(myState.getCapacityState().getActualScenario());
			Set<Flight> allFlights = myState.getFlightState().getSittingFlights();
			allFlights.addAll(myState.getFlightState().getAirborneFlights());
//			System.out.println(GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(allFlights, Duration.standardHours(1),
//					new Interval(myState.getCurrentTime(),myState.getCurrentTime().plus(Duration.standardHours(24))), Flight.origETAFieldID));
//			System.out.println(GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(allFlights, Duration.standardHours(24),
//					new Interval(myState.getCurrentTime(),myState.getCurrentTime().plus(Duration.standardHours(24))), Flight.origETAFieldID));
			SimpleTmiAction myAction = outcomes.get(myState).getItemA();
			myStats.addValue(myNasRunner.apply(myState, new SimpleTmiAction())-
					myNasRunner.apply(myState,myAction));
			
		}
		System.out.println(myStats.getMean()+", ("+myStats.getStandardDeviation()+")");
	}
}
