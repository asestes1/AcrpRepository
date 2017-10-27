package gdp_planning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
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
import metrics.MetricCalculator;
import model.StateAction;
import state_representation.DefaultCapacityComparer;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;

public class DirectExtendedHofkinModel implements StateAction<DefaultState> {
	private final Duration timePeriodDuration;
	private final Function<DefaultState, Interval> myIntervalChooser;
	private final Comparator<Flight> flightComparator;
	private final double groundCost;
	private final double airCost;
	private final double maxAirborne;
	

	/**
	 * Standard constructor
	 * 
	 * @param groundCost
	 *            - the cost of one time unit of ground delay.
	 * @param airCost
	 *            - the cost of one time unit of air delay.
	 * @throws GRBException
	 */
	public DirectExtendedHofkinModel(double groundCost, double airCost, Duration timePeriodDuration,
			Function<DefaultState, Interval> myIntervalChooser, Comparator<Flight> flightComparator)
					throws GRBException {
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.timePeriodDuration = timePeriodDuration;
		this.myIntervalChooser = myIntervalChooser;
		this.flightComparator = flightComparator;
		this.maxAirborne = ExtendedHofkinModel.UNLIMITED;
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param groundCost
	 *            - the cost of one time unit of ground delay.
	 * @param airCost
	 *            - the cost of one time unit of air delay.
	 * @throws GRBException
	 */
	public DirectExtendedHofkinModel(double groundCost, double airCost, double maxAirborne, Duration timePeriodDuration,
			Function<DefaultState, Interval> myIntervalChooser, Comparator<Flight> flightComparator)
					throws GRBException {
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.timePeriodDuration = timePeriodDuration;
		this.myIntervalChooser = myIntervalChooser;
		this.flightComparator = flightComparator;
		this.maxAirborne = maxAirborne;
	}

	@Override
	public DefaultState act(DefaultState state, FlightHandler flightHandler, Duration timeStep) throws Exception {
		Interval gdpInterval = myIntervalChooser.apply(state);
		Set<Flight> sittingFlights = state.getFlightState().getSittingFlights();
		Duration maxFlightTime = MetricCalculator.calculateMaxDurationField(sittingFlights, Flight.flightTimeID);
		List<Integer> exemptFlights = GDPPlanningHelper.aggregateFlightCountsByFlightTimeField(
				state.getFlightState().getAirborneFlights(), timePeriodDuration, gdpInterval, Flight.depETAFieldID);
		int numTimePeriods = exemptFlights.size();
		List<Set<Flight>> flightsByFlightTime = GDPPlanningHelper.aggregateFlightsByDurationField(sittingFlights,
				Flight.flightTimeID, Duration.ZERO, maxFlightTime, timePeriodDuration);

		int numFlightTimes = flightsByFlightTime.size();
		List<List<Set<Flight>>> flightsByFlightTimeETA = new ArrayList<List<Set<Flight>>>();
		List<List<Integer>> flightCountsByFlightTimeETA = new ArrayList<List<Integer>>();
		List<Integer> flightTimes = new ArrayList<Integer>();
		for (int i = 0; i < numFlightTimes; i++) {
			boolean hasFlights = false;
			List<Set<Flight>> flightsByETA = GDPPlanningHelper.aggregateFlightsByFlightTimeField(
					flightsByFlightTime.get(i), timePeriodDuration, gdpInterval, state.getCurrentTime(),
					Flight.earliestETDFieldID);
			List<Integer> flightCountsByETA = new ArrayList<Integer>(numTimePeriods);
			for (int j = 0; j < numTimePeriods; j++) {
				if (!flightsByETA.get(j).isEmpty()) {
					hasFlights = true;
				}
				flightCountsByETA.add(flightsByETA.get(j).size());
			}
			if (hasFlights) {
				flightsByFlightTimeETA.add(flightsByETA);
				flightCountsByFlightTimeETA.add(flightCountsByETA);
				flightTimes.add(i);
			}
		}

		List<DiscreteCapacityScenario> myScenarios = DiscreteScenarioUtilities
				.discretizeScenarios(state.getCapacityState(), gdpInterval, timePeriodDuration);
		List<Set<Set<Integer>>> myScenarioPartition = DiscreteScenarioUtilities.buildDiscreteScenarioTree(
				state.getCapacityState().getScenarios(), new DefaultCapacityComparer(), timePeriodDuration,
				gdpInterval);
		int worstScenario = DiscreteScenarioUtilities.getWorstScenario(myScenarios);

		List<List<Integer>> solution = new ExtendedHofkinModel(groundCost, airCost,maxAirborne).solveModel(
				flightCountsByFlightTimeETA, flightTimes, exemptFlights, myScenarios, myScenarioPartition,
				worstScenario);
		PriorityQueue<Flight> myQueue = new PriorityQueue<Flight>(flightComparator);

		SortedSet<Flight> newSittingFlights = new TreeSet<Flight>(
				new FlightDateTimeFieldComparator(Flight.aETDFieldID));

		Iterator<List<Integer>> myPaarIter = solution.iterator();
		Iterator<List<Set<Flight>>> myFlightIter = flightsByFlightTimeETA.iterator();
		while (myPaarIter.hasNext()) {
			List<Integer> discretePaars = myPaarIter.next();
			List<Set<Flight>> currentFlights = myFlightIter.next();
			Iterator<Set<Flight>> scheduledFlightsIter = currentFlights.iterator();

			int length = discretePaars.size();
			int remaining = discretePaars.get(0);
			myQueue.addAll(scheduledFlightsIter.next());
			boolean slots_used = false;
			boolean flights_scheduled = false;
			boolean done = false;
			int current_group = 0;
			int current_slot = 0;
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
					Set<Flight> currentDepartures = scheduledFlightsIter.next();
					if (current_group >= length) {
						flights_scheduled = true;
					} else {
						myQueue.addAll(currentDepartures);
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
		}

		return state.setFlightState(state.getFlightState().setSittingFlights(newSittingFlights));

	}
}
