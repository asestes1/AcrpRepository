package state_criteria;

import org.joda.time.DateTime;

import state_representation.DefaultState;

/**
 * This return true if the current time is after a fixed time.
 * @author Alex2
 *
 * @param <T> the type of state object in use
 */
public class AfterTimeCriteria<T extends DefaultState> implements StateCriteria<T >{
	private final DateTime setTime;
	
	/**
	 * Standard constructor.
	 * @param setTime - the set time. This criteria will return false if the current time
	 * is before this fixed time, and will return false otherwise
	 */
	public AfterTimeCriteria(DateTime setTime) {
		this.setTime = setTime;
	}
	
	/**
	 * This will return false if the current time is before the prespecified fixed time,
	 * and true otherwise.
	 */
	@Override
	public boolean isSatisfied(T state) {
		DateTime time= state.getCurrentTime();
		if(time.isAfter(setTime)){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public StateCriteria<T> updateHistory(T state) {
		// TODO Auto-generated method stub
		return this;
	}
	
}
