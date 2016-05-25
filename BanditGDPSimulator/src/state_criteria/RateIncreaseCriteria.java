package state_criteria;

import bandit_objects.Immutable;
import state_representation.DefaultState;

/**
 * This criteria is satisfied the first time that the capacity state reaches some threshold,
 * after which it will not be satisfied. 
 * @author Alex2
 *
 * @param <T>
 */
public class RateIncreaseCriteria<T extends DefaultState> implements StateCriteria<T>, Immutable{
	private final double thresholdRate;
	private final int timesInvoked;
	
	public RateIncreaseCriteria(double thresholdRate) {
		this.thresholdRate = thresholdRate;
		this.timesInvoked = 0;
	}
	
	public RateIncreaseCriteria(double thresholdRate, int timesInvoked) {
		this.thresholdRate = thresholdRate;
		this.timesInvoked = timesInvoked;
	}
	@Override
	public boolean isSatisfied(T state) {
		if(state.getCapacity() >= thresholdRate &&  timesInvoked < 1){
			return true;
		}
		return false;
	}
	@Override
	public StateCriteria<T> updateHistory(T state) {
		if(isSatisfied(state)){
			return new RateIncreaseCriteria<T>(thresholdRate,timesInvoked+1);
		}
		return this;
	}
}
