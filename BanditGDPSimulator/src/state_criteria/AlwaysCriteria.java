package state_criteria;

/**
 * An implementation of StateCriteria in which the
 * criteria is always satisfied. Should be used
 * if you want the module to run in every iteration
 * @author Alex2
 *
 * @param <T> the type of object used to represent the state.
 */
public class AlwaysCriteria<T> implements StateCriteria<T>{

	/**
	 * Will always return true in this implementation.
	 */
	@Override
	public boolean isSatisfied(T state) {
		return true;
	}

	@Override
	public StateCriteria<T> updateHistory(T state) {
		return this;
	}

}
