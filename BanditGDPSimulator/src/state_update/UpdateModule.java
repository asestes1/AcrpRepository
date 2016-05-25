package state_update;

import org.joda.time.Duration;

import model.OtherStateAction;
import model.StateAction;
import state_representation.CapacityScenarioState;
import state_representation.DefaultState;

public class UpdateModule
	implements StateAction<DefaultState> {
	private final StateAction<DefaultState> nasStateUpdate;
	private final OtherStateAction<CapacityScenarioState> capacityUpdate;
	
	public UpdateModule(StateAction<DefaultState> nasStateUpdate,
			OtherStateAction<CapacityScenarioState> capacityUpdate){
		this.nasStateUpdate = nasStateUpdate;
		this.capacityUpdate = capacityUpdate;
	}
	
	public DefaultState act(DefaultState state,
		FlightHandler flightHandler, Duration timeStep) throws Exception{
		DefaultState nextNASState = nasStateUpdate.act(state, flightHandler, timeStep);
		CapacityScenarioState nextWeatherState = capacityUpdate.act(state.getCapacityState(),state.getCurrentTime(),timeStep);
		return new DefaultState(nextNASState.getCurrentTime(),nextNASState.getFlightState(),
				nextNASState.getAirportState(), nextWeatherState);
	}



	public StateAction<DefaultState> getNasStateUpdate() {
		return nasStateUpdate;
	}

	public OtherStateAction<CapacityScenarioState> getCapacityUpdate() {
		return capacityUpdate;
	}

	public UpdateModule setTimeStep(Duration timeStep) {
		return new UpdateModule(nasStateUpdate, capacityUpdate);
	}

	public UpdateModule setNasStateUpdate(StateAction<DefaultState> nasStateUpdate) {
		return new UpdateModule(nasStateUpdate, capacityUpdate);
	}

	public UpdateModule setCapacityUpdate(OtherStateAction<CapacityScenarioState> capacityUpdate) {
		return new UpdateModule(nasStateUpdate, capacityUpdate);
	}

}
