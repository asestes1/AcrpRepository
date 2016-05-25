package gdp_planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import gurobi.GRBException;
import model.StateAction;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;

/**
 * This class implements the Hofkin integer programming method for choosing
 * PAARs
 * 
 * @author Alex2
 *
 */
public class DirectHofkinModel implements StateAction<DefaultState> {
	private final Duration timePeriodDuration;
	private final Function<DefaultState, Interval> myIntervalChooser;
	private final Comparator<Flight> flightComparator;
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
	public DirectHofkinModel(double groundCost, double airCost, Duration timePeriodDuration,
			Function<DefaultState, Interval> myIntervalChooser, Comparator<Flight> flightComparator)
					throws GRBException {
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.timePeriodDuration = timePeriodDuration;
		this.myIntervalChooser = myIntervalChooser;
		this.flightComparator = flightComparator;
	}

	@Override
	public DefaultState act(DefaultState state, FlightHandler flightHandler, Duration timeStep) throws Exception {
		Interval gdpInterval = myIntervalChooser.apply(state);
		Set<Flight> sittingFlights = state.getFlightState().getSittingFlights();
		List<Set<Flight>> scheduledFlights = GDPPlanningHelper.aggregateFlightsByFlightTimeField(sittingFlights,
				timePeriodDuration, gdpInterval, state.getCurrentTime(), Flight.earliestETAFieldID);
		int length = scheduledFlights.size();
		List<Integer> numScheduledFlights = new ArrayList<Integer>(length);
		for (int i = 0; i < scheduledFlights.size(); i++) {
			numScheduledFlights.add(scheduledFlights.get(i).size());
		}
		List<Integer> exemptFlights = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(
				state.getFlightState().getAirborneFlights(), timePeriodDuration, gdpInterval, Flight.depETAFieldID);
		List<DiscreteCapacityScenario> myScenarios = DiscreteScenarioUtilities
				.discretizeScenarios(state.getCapacityState(), gdpInterval, timePeriodDuration);
		List<Integer> discretePaars = new HofkinModel(groundCost, airCost).solveModel(numScheduledFlights,
				exemptFlights, myScenarios);
		PriorityQueue<Flight> myQueue = new PriorityQueue<Flight>(flightComparator);

		SortedSet<Flight> newSittingFlights = new TreeSet<Flight>(
				new FlightDateTimeFieldComparator(Flight.aETDFieldID));
		int current_group = 0;
		int current_slot = 0;
		boolean done = false;
		int remaining = discretePaars.get(0);
		myQueue.addAll(scheduledFlights.get(0));
		boolean slots_used = false;
		boolean flights_scheduled = false;
		while (done == false) {
			while (remaining == 0 && slots_used == false) {
				current_slot++;
				if (current_slot >= length) {
					slots_used = true;
				} else {
					remaining = discretePaars.get(current_slot);
				}
			}
			while (myQueue.isEmpty() && flights_scheduled == false) {
				current_group++;
				if (current_group >= length) {
					flights_scheduled = true;
				} else {
					myQueue.addAll(scheduledFlights.get(current_group));
				}
			}
			if (current_group > current_slot) {
				throw new Exception("Error in Hofkin solution: some slots unusable");
			}
			if (slots_used == true && flights_scheduled == false) {
				throw new Exception("Error in Hofkin solution: not enough slots");
			}
			if (slots_used == true && flights_scheduled == true) {
				done = true;
			}
			Duration currentDelays = timePeriodDuration.multipliedBy(current_slot - current_group);
			while (!myQueue.isEmpty() && remaining > 0) {
				Flight nextFlight = myQueue.poll();
				DateTime newETA = nextFlight.getEarliestETA(state.getCurrentTime()).plus(currentDelays);
				nextFlight = flightHandler.controlArrival(nextFlight, newETA, state.getCurrentTime());
				newSittingFlights.add(nextFlight);
				remaining--;
			}
		}
		return state.setFlightState(state.getFlightState().setSittingFlights(newSittingFlights));

	}

	public Duration getTimePeriodDuration() {
		return timePeriodDuration;
	}

	public Function<DefaultState, Interval> getMyIntervalChooser() {
		return myIntervalChooser;
	}

	public Comparator<Flight> getFlightComparator() {
		return flightComparator;
	}

	public double getGroundCost() {
		return groundCost;
	}

	public double getAirCost() {
		return airCost;
	}

}
