package state_criteria;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import bandit_objects.Immutable;
import state_representation.DefaultState;

public class QueueLengthCriteria<T extends DefaultState> implements StateCriteria<T>, Immutable {
	private final Integer maxHistoryLength;
	private final CircularFifoQueue<Integer> history;
	private final Double maxQueueLength;

	public QueueLengthCriteria(Integer maxHistoryLength,CircularFifoQueue<Integer> history, Double maxQueueLength) {
		this.maxHistoryLength = maxHistoryLength;
		this.history = new CircularFifoQueue<Integer>(history);
		this.maxQueueLength = maxQueueLength;
	}
	
	public QueueLengthCriteria(Integer maxHistoryLength, Double maxQueueLength) {
		this.maxHistoryLength = maxHistoryLength;
		this.history = new CircularFifoQueue<Integer>(maxHistoryLength);
		this.maxQueueLength = maxQueueLength;
	}

	@Override
	public boolean isSatisfied(T state) {
		Integer historyLength = history.size();
		Integer totalQueueLength = 0;
		for (Integer q : history) {
			totalQueueLength += q;
		}
		Double averageQueueLength = totalQueueLength / (double) historyLength;
		if(averageQueueLength <= maxQueueLength){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public StateCriteria<T> updateHistory(T state) {
		Integer queueLength = state.getAirportState().getQueueLength();
		CircularFifoQueue<Integer> newHistory = new CircularFifoQueue<Integer>(history);
		newHistory.add(queueLength);
		return new QueueLengthCriteria<>(maxHistoryLength, maxQueueLength);
	}
}
