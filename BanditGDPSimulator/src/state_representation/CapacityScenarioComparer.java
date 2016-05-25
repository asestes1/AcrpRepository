package state_representation;

import org.joda.time.DateTime;

public interface CapacityScenarioComparer {
	public boolean areEqual(CapacityScenario a, CapacityScenario b, DateTime t);
}
