package state_criteria;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_representation.DefaultState;

public class PeriodicCriteria<T extends DefaultState> implements StateCriteria<T> {
	private final DateTime lastRun; 
	private final Duration timePeriod; 

	@Override
	public String toString(){
		String my_str = "";
		if(this.lastRun == null){
			my_str = "PCriteria, never run";
		}else{
			my_str = "PCriteria, last run: "+lastRun.toString();
		}
		my_str += ". Time Period: "+timePeriod.toString();
		return my_str;
	}
	
	public PeriodicCriteria(Duration timePeriod) {
		this.lastRun = null;
		this.timePeriod = timePeriod;
	}
	
	public PeriodicCriteria(Duration timePeriod, DateTime lastRun) {
		this.lastRun = lastRun;
		this.timePeriod = timePeriod;
	}
	
	@Override
	public StateCriteria<T> updateHistory(T state) {
		if(isSatisfied(state)){
			return new PeriodicCriteria<T>(this.timePeriod, state.getCurrentTime());
		}
		return this;
	}

	@Override
	public boolean isSatisfied(T state) {
		if(this.lastRun == null ){
			return true;
		}else if(!timePeriod.isLongerThan(new Duration(this.lastRun,state.getCurrentTime()))){
			return true;
		}
		return false;
	}

}
