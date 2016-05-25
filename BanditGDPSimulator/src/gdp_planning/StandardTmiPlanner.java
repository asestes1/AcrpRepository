package gdp_planning;

import java.util.Comparator;

import org.joda.time.Duration;

import function_util.FunctionEx;
import model.GdpAction;
import model.StateAction;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.FlightHandler;

public class StandardTmiPlanner implements StateAction<DefaultState>{
	private final FunctionEx<DefaultState,GdpAction,Exception> myTmiChooser;
	private final Comparator<Flight> assignmentPriority;
	
	public StandardTmiPlanner(FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser,Comparator<Flight> assignmentPriority) {
		this.myTmiChooser = myTmiChooser;
		this.assignmentPriority = assignmentPriority;
	}
	@Override
	public DefaultState act(DefaultState state, FlightHandler flightHandler, Duration timeStep) throws Exception {
		return GDPPlanningHelper.implementTmi(state, myTmiChooser.apply(state), flightHandler, assignmentPriority);
	}
	
	
}
