package bandit_simulator;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

import bandit_objects.SimpleTmiAction;
import cancel_module.DefaultGDPCancelModule;
import function_util.BiFunctionEx;
import gdp_planning.GDPPlanningHelper;
import metrics.MetricCalculator;
import model.CriteriaActionPair;
import model.GdpAction;
import model.SimulationEngineInstance;
import model.SimulationEngineRunner;
import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
import state_criteria.StateCriteria;
import state_criteria.TmiRateCriteria;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightHandler;
import state_update.UpdateModule;

public class RunStateFunction implements BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> {
	private final FlightHandler myHandler;
	private final UpdateModule myUpdate;
	private final Duration timeStep;
	private final Double airDelayWeight;

	public RunStateFunction(FlightHandler myHandler, UpdateModule myUpdate, Duration step, Double airDelayWeight) {
		super();
		this.myHandler = myHandler;
		this.myUpdate = myUpdate;
		this.timeStep = step;
		this.airDelayWeight = airDelayWeight;
	}

	@Override
	public Double apply(DefaultState state, SimpleTmiAction action) throws Exception {
		DefaultState initialState = GDPPlanningHelper.implementTmi(state, action, myHandler,
				new FlightDateTimeFieldComparator(Flight.origETAFieldID));

		List<CriteriaActionPair<DefaultState>> myModules = new ArrayList<CriteriaActionPair<DefaultState>>();
		myModules.add(new CriteriaActionPair<DefaultState>(new AlwaysCriteria<DefaultState>(), myUpdate));
		SimulationEngineInstance<DefaultState> myInstance = new SimulationEngineInstance<DefaultState>(myModules,
				new AllLandedCriteria<DefaultState>(), myHandler, initialState);
		if(action.getType() == SimpleTmiAction.GDP_TYPE){
			GdpAction myGdp = new GdpAction(state.getCurrentTime(),action);
			myModules.add(new CriteriaActionPair<DefaultState>(
					new StateCriteria.LimitedCriteria<DefaultState>(0, 1, new TmiRateCriteria<DefaultState>(myGdp,120)),
					new DefaultGDPCancelModule()));
		}

		DefaultState finalState = SimulationEngineRunner.run(myInstance, timeStep,System.out,0);
		Duration airDelay = MetricCalculator.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(),
				Flight.airQueueDelayID);
		Duration groundDelay = MetricCalculator
				.calculateTotalDurationField(finalState.getFlightState().getLandedFlights(), Flight.scheduledDelayID);
		System.out.println(action);
		System.out.println("Air: "+ airDelay);
		System.out.println("Ground: "+groundDelay);
		Double totalDelay = airDelayWeight * (airDelay.getMillis() / (60000.0)) + groundDelay.getMillis() / (60000.0);
		return -1 * totalDelay;
	}

}
