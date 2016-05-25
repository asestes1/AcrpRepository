package state_criteria;

import org.joda.time.DateTime;

import state_representation.DefaultState;

public final class AtStartCriteriaFactory{
	private AtStartCriteriaFactory() {
		
	}
	
	public static <T extends DefaultState> StateCriteria<T> parse(DateTime input) throws Exception {
		return StateCriteria.not(new AfterTimeCriteria<T>(input));
	}

}
