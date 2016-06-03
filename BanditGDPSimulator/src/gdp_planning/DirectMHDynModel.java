package gdp_planning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import gurobi.GRBException;
import model.StateAction;
import state_representation.DefaultCapacityComparer;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.FlightHandler;

public class DirectMHDynModel implements StateAction<DefaultState> {
	private final Duration timePeriodDuration;
	private final Function<DefaultState, Interval> myIntervalChooser;
	private final double groundCost;
	private final double airCost;

	/**
	 * Standard constructor
	 * 
	 * @param groundCost
	 *            - the cost of one time unit of ground delay.
	 * @param airCost
	 *            - the cost of one time unit of air delay.
	 * @throws GRBException
	 */
	public DirectMHDynModel(double groundCost, double airCost, Duration timePeriodDuration,
			Function<DefaultState, Interval> myIntervalChooser) throws GRBException {
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.timePeriodDuration = timePeriodDuration;
		this.myIntervalChooser = myIntervalChooser;
	}

	@Override
	public DefaultState act(DefaultState state, FlightHandler flightHandler, Duration timeStep) throws Exception {
		Interval gdpInterval = myIntervalChooser.apply(state);
		List<Flight> sittingFlights = new ArrayList<Flight>(state.getFlightState().getSittingFlights());
		List<ImmutablePair<Integer, Integer>> discretizedFlights = GDPPlanningHelper.discretizeFlights(sittingFlights,
				timePeriodDuration, state.getCurrentTime());
		List<Integer> exemptFlights = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(
				state.getFlightState().getAirborneFlights(), timePeriodDuration, gdpInterval, Flight.depETAFieldID);
		List<DiscreteCapacityScenario> myScenarios = DiscreteScenarioUtilities
				.discretizeScenarios(state.getCapacityState(), gdpInterval, timePeriodDuration);
		List<Set<Set<Integer>>> myScenarioPartition = DiscreteScenarioUtilities.buildDiscreteScenarioTree(
				state.getCapacityState().getScenarios(), new DefaultCapacityComparer(), timePeriodDuration,
				gdpInterval);
		int worstScenario = DiscreteScenarioUtilities.getWorstScenario(myScenarios);
		List<Integer> delays = new MHDynModel(groundCost, airCost).solveModel(discretizedFlights, exemptFlights,
				myScenarios, myScenarioPartition, worstScenario);
		SortedSet<Flight> newSittingFlights = new TreeSet<Flight>();
		Iterator<Integer> myDelayIter = delays.iterator();
		Iterator<Flight> myFlightIter = sittingFlights.iterator();
		while (myDelayIter.hasNext()) {
			int nextDelayInt = myDelayIter.next();
			Duration nextDelayDuration = timePeriodDuration.multipliedBy(nextDelayInt);
			Flight nextFlight = myFlightIter.next();
			DateTime newETA = nextFlight.getEarliestETA(state.getCurrentTime()).plus(nextDelayDuration);
			nextFlight = flightHandler.controlArrival(nextFlight, newETA, state.getCurrentTime());

		}

		return state.setFlightState(state.getFlightState().setSittingFlights(newSittingFlights));

	}

	public Duration getTimePeriodDuration() {
		return timePeriodDuration;
	}

	public Function<DefaultState, Interval> getMyIntervalChooser() {
		return myIntervalChooser;
	}

	public double getGroundCost() {
		return groundCost;
	}

	public double getAirCost() {
		return airCost;
	}
}
