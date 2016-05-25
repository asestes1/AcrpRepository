package model;

import state_criteria.StateCriteria;

/**
 * In the ground delay simulation, this pairs a criteria which determines
 * whether a module should be run and the corresponding module. This does
 * not add additional functionality to the pair class; mostly used for 
 * convenience to shorten declaration of objects. For example,
 * Pair<StateCriteria<DefaultState<BasicCapacityState>>,
 * StateAction<DefaultState<BasicCapacityState>>> becomes
 * CriteriaActionPair<DefaultState<BasicCapacityState>>
 * @author Alex2
 *
 * @param <T> the type of state object in use.
 */
public class CriteriaActionPair<T> extends Pair<StateCriteria<T>,StateAction<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7359654145967172304L;

	/**
	 * Standard constructor.
	 * @param itemA - the criteria
	 * @param itemB - the module
	 */
	public CriteriaActionPair(StateCriteria<T> itemA, StateAction<T> itemB) {
		super(itemA, itemB);
	}

}
