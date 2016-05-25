package gdp_planning;

import java.util.List;

/**
 * Classes implementing this interface are able to choose 
 * PAARs given discretized flight demand information and
 *  capacity scenarios
 * @author Alex2
 *
 */
public interface DiscretePAARChooser {
	
	/**
	 * This function should choose a set of PAARS. 
	 * @param numArriving: a list of integers, where the ith element describes the number
	 *  of flights arriving in the ith time period
	 * @param scenarios: a list of scenarios which describe capacity profiles of the 
	 * destination airport
	 * @return a list of integers, where the ith element is the PAAR in the ith time period
	 * @throws Exception
	 */
	public List<Integer> solveModel(List<Integer> scheduledFlights,
			List<Integer> exemptFlights,
			List<DiscreteCapacityScenario> myScenarios) throws Exception;
}
