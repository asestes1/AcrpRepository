package state_criteria;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import model.GdpAction;
import state_representation.DefaultState;

public class TmiRateCriteria<T extends DefaultState> implements StateCriteria<T>{
	private final GdpAction gdpAction;
	private final CircularFifoQueue<Boolean> history;
	private final int historyLength;
	
	public GdpAction getGdpAction() {
		return gdpAction;
	}

	public TmiRateCriteria(GdpAction gdpAction, CircularFifoQueue<Boolean> history, int historyLength) {
		super();
		this.gdpAction = gdpAction;
		this.history = new CircularFifoQueue<Boolean>(history);
		this.historyLength = historyLength;
	}
	
	public TmiRateCriteria(GdpAction gdpAction, Integer historyLength) {
		super();
		this.gdpAction = gdpAction;
		this.history = new CircularFifoQueue<Boolean>();
		this.historyLength = historyLength;
	}

	@Override
	public boolean isSatisfied(T state) {
		if(historyLength < history.size()){
			return false;
		}
		for(Boolean observation: history){
			if(!observation){
				return false;
			}
		}
		return true;
	}

	@Override
	public StateCriteria<T> updateHistory(T state) {
		CircularFifoQueue<Boolean> newHistory = new CircularFifoQueue<Boolean>(history);
		if(!state.getCurrentTime().isBefore(gdpAction.getGdpInterval().getStart())){
			Integer gdpRate = gdpAction.getPaars().get(state.getCurrentTime());
			Integer stateRate = state.getCapacity();
			newHistory.add(gdpRate <= stateRate);
		}
		return new TmiRateCriteria<T>(gdpAction, newHistory, historyLength);
	}

}
