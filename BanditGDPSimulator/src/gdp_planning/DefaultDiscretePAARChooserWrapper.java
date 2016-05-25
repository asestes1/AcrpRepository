package gdp_planning;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import function_util.BiFunctionEx;
import state_representation.DefaultState;
import state_representation.Flight;

public class DefaultDiscretePAARChooserWrapper implements BiFunctionEx<DefaultState, Interval, SortedMap<DateTime, Integer>, Exception> {
	private final Duration timePeriodDuration;
	private final DiscretePAARChooser discretePAARChooser;

	public DefaultDiscretePAARChooserWrapper(Duration timePeriodDuration, DiscretePAARChooser discretePAARChooser) {
		this.timePeriodDuration = timePeriodDuration;
		this.discretePAARChooser = discretePAARChooser;
	}

	@Override
	public SortedMap<DateTime, Integer> apply(DefaultState state, Interval gdpInterval) throws Exception {
		Set<Flight> sittingFlights = state.getFlightState().getSittingFlights();
		List<Integer> scheduledFlights = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(sittingFlights,
				timePeriodDuration, gdpInterval, state.getCurrentTime(), Flight.earliestETAFieldID);
		List<Integer> exemptFlights = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(
				state.getFlightState().getAirborneFlights(), timePeriodDuration, gdpInterval, Flight.depETAFieldID);
		List<DiscreteCapacityScenario> myScenarios = DiscreteScenarioUtilities.discretizeScenarios(state.getCapacityState(),
				gdpInterval, timePeriodDuration);
		List<Integer> discretePaars = discretePAARChooser.solveModel(scheduledFlights, exemptFlights, myScenarios);

		return DiscreteScenarioUtilities.discreteToContinuousPAARs(gdpInterval, discretePaars, timePeriodDuration);
	}

	public Duration getTimePeriodDuration() {
		return timePeriodDuration;
	}

	public DiscretePAARChooser getDiscretePAARChooser() {
		return discretePAARChooser;
	}

	/*
	 * public Interval findSolveInterval(Set<Flight> sittingFlights, Interval
	 * gdpInterval) { DateTime start = gdpInterval.getStart(); DateTime end =
	 * start.plus(
	 * timePeriodDuration.multipliedBy(numTimePeriods(gdpInterval)));
	 * if(sittingFlights.size() > 0){ DateTime earliestETA =
	 * findSittingFlightWithEarliestETA(sittingFlights);
	 * while(start.isAfter(earliestETA)){ start.minus(timePeriodDuration); }
	 * 
	 * }
	 * 
	 * return new Interval(start,end); }
	 * 
	 * public DateTime findSittingFlightWithEarliestETA(Set<Flight>
	 * sittingFlights) { Iterator<Flight> myIterator =
	 * sittingFlights.iterator(); //We can do this with no worries because we
	 * check in every function that calls this //to make sure sittingFlights is
	 * non-empty DateTime minETA = myIterator.next().getOrigETA();
	 * while(myIterator.hasNext()){ DateTime nextETA =
	 * myIterator.next().getOrigETA(); if(minETA.isAfter(nextETA)){ minETA =
	 * nextETA; } } return minETA; }
	 * 
	 * public long numTimePeriods(Interval interval){
	 * if((interval.toDurationMillis()) % timePeriodDuration.getMillis() == 0){
	 * return (interval.toDurationMillis())/timePeriodDuration.getMillis(); }
	 * return (interval.toDurationMillis())/timePeriodDuration.getMillis()+1; }
	 */

}
