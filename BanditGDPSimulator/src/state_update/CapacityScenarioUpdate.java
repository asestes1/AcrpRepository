package state_update;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import model.OtherStateAction;
import state_representation.CapacityScenario;
import state_representation.CapacityScenarioComparer;
import state_representation.CapacityScenarioState;

public class CapacityScenarioUpdate implements OtherStateAction<CapacityScenarioState>{
	CapacityScenarioComparer scenarioCompare;
	
	public CapacityScenarioUpdate(CapacityScenarioComparer scenarioCompare) {
		this.scenarioCompare = scenarioCompare;
	}
	
	@Override
	public CapacityScenarioState act(CapacityScenarioState state,DateTime currentTime, Duration timeStep) {
		//Time increases by the time step.
		DateTime nextTime = currentTime.plus(timeStep);
		//Actual scenario does not change
		CapacityScenario actualScenario = state.getActualScenario();
		List<CapacityScenario> currentScenarios = state.getScenarios();
		
		//Get the new capacity from actual scenario using the new time.
		int nextCapacity = actualScenario.getCurrentCapacity(nextTime);
		//Initialize the new scenarios
		List<CapacityScenario> newScenarios = new ArrayList<CapacityScenario>();
		//Create an iterator for our scenarios
		Iterator<CapacityScenario> myIterator = currentScenarios.iterator();
		while(myIterator.hasNext()){
			CapacityScenario nextScenario = myIterator.next();
			//Only keep a scenario if it still "looks the same" as our current scenario.
			if(scenarioCompare.areEqual(actualScenario, nextScenario, nextTime)){
				newScenarios.add(nextScenario);
			}
		}
		
		return new CapacityScenarioState(nextCapacity,actualScenario , newScenarios);
	}
	

}
