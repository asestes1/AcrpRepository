package state_criteria;

import state_representation.DefaultState;

/**
 * This criteria returns true if all flights have landed or
 * have been cancelled. This
 * implementation is for state objects which are of the
 * DefaultState type.
 * @author Alex2
 *
 * @param <T> the representation of capacity in the stae object.
 */
public class AllLandedCriteria<T extends DefaultState> implements StateCriteria<T>{

	/**
	 * Returns true if there are no more remaining airborne or sitting flights.
	 */
	@Override
	public boolean isSatisfied(T state) {
		//Find the number of airborne flights
		int numAirborne = state.getFlightState().getAirborneFlights().size();
		//Find the number of sitting flights
		int numSitting = state.getFlightState().getSittingFlights().size();
		//If there are not any airborne or sitting flights, we return true,
		if(numAirborne+numSitting == 0){
			return true;
		//If there are any airborne or sitting flights, we return false.
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
