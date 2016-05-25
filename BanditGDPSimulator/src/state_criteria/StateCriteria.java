package state_criteria;

/**
 * Class implementing this function provide a criteria which decides whether a
 * module should be run.
 * 
 * @author Alex2
 *
 * @param <T>
 *            - the type of state object in use.
 */
public interface StateCriteria<T> {

	/**
	 * This takes the state and the current time and determines whether an
	 * action should occur.
	 * 
	 * @param state
	 *            - the current state
	 * @param currentTime
	 *            - the current time
	 * @return - boolean, true if some action should take place, false
	 *         otherwise.
	 */
	public boolean isSatisfied(T state);

	public static <R> StateCriteria<R> and(StateCriteria<R> state1, StateCriteria<R> state2) {
		return new AndCriteria<R>(state1, state2);
	}

	public static <R> StateCriteria<R> or(StateCriteria<R> state1, StateCriteria<R> state2) {
		return new OrCriteria<R>(state1, state2);
	}

	public static <R> StateCriteria<R> not(StateCriteria<R> state1) {
		return new NotCriteria<R>(state1);
	}

	public class AndCriteria<R> implements StateCriteria<R> {
		private final StateCriteria<R> criteriaA;
		private final StateCriteria<R> criteriaB;

		public AndCriteria(StateCriteria<R> criteriaA, StateCriteria<R> criteriaB) {
			this.criteriaA = criteriaA;
			this.criteriaB = criteriaB;
		}

		@Override
		public boolean isSatisfied(R state) {
			return criteriaA.isSatisfied(state) && criteriaB.isSatisfied(state);

		}

		@Override
		public StateCriteria<R> updateHistory(R state) {
			return new AndCriteria<>(criteriaA.updateHistory(state), criteriaB.updateHistory(state));
		}

	}

	public class OrCriteria<R> implements StateCriteria<R> {
		private final StateCriteria<R> criteriaA;
		private final StateCriteria<R> criteriaB;

		public OrCriteria(StateCriteria<R> criteriaA, StateCriteria<R> criteriaB) {
			this.criteriaA = criteriaA;
			this.criteriaB = criteriaB;
		}

		@Override
		public boolean isSatisfied(R state) {
			return criteriaA.isSatisfied(state) || criteriaB.isSatisfied(state);
		}

		@Override
		public StateCriteria<R> updateHistory(R state) {
			return new OrCriteria<>(criteriaA.updateHistory(state), criteriaB.updateHistory(state));
		}

	}

	public class NotCriteria<R> implements StateCriteria<R> {
		private final StateCriteria<R> criteria;

		public NotCriteria(StateCriteria<R> criteria) {
			this.criteria = criteria;
		}

		@Override
		public boolean isSatisfied(R state) {
			return !criteria.isSatisfied(state);
		}

		@Override
		public StateCriteria<R> updateHistory(R state) {
			return new NotCriteria<R>(criteria.updateHistory(state));
		}

	}
	
	public class LimitedCriteria<R> implements StateCriteria<R> {
		private final Integer timesRun;
		private final Integer maxTimesRun;
		private final StateCriteria<R> criteria;
		
		public LimitedCriteria(Integer timesRun, Integer maxTimesRun, StateCriteria<R> criteria) {
			this.timesRun = timesRun;
			this.maxTimesRun = maxTimesRun;
			this.criteria = criteria;
		}

		@Override
		public boolean isSatisfied(R state) {
			if(timesRun >= maxTimesRun){
				return false;
			}else{
				return criteria.isSatisfied(state);
			}
		}

		@Override
		public StateCriteria<R> updateHistory(R state) {
			Integer newTimesRun = timesRun;
			if(timesRun < maxTimesRun && criteria.isSatisfied(state)){
				newTimesRun++;
			}
			return new LimitedCriteria<>(newTimesRun, maxTimesRun, criteria.updateHistory(state));
		}
	}

	public StateCriteria<T> updateHistory(T state);
}
