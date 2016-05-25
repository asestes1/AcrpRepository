package state_factories;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_representation.CapacityScenario;
import state_representation.CapacityScenarioState;

public final class CapacityScenarioFactory {
	private CapacityScenarioFactory() {

	}

	/**
	 * This implements a factory which uses a file to construct a basic capacity
	 * state object. The file should just contain a single integer which is the
	 * hourly capacity of the airport
	 */
	public static CapacityScenarioState parseBasicState(File input) throws Exception {
		Scanner myScanner = new Scanner(input);
		int capacity = Integer.parseInt(myScanner.nextLine());
		myScanner.close();
		return new CapacityScenarioState(capacity);
	}

	public static CapacityScenarioState parseScenarios(Collection<CapacityScenario> scenarios, DateTime currentTime) {
		double cumP = 0;
		for (CapacityScenario nextScenario : scenarios) {
			cumP += nextScenario.getProbability();
		}
		return parseScenarios(scenarios, cumP, currentTime);
	}

	/**
	 * This uses the calculated summed probability of the scenarios to normalize
	 * the scenarios.
	 * 
	 * @param scenarios:
	 *            a set of scenarios
	 * @param cumP:
	 *            the sum of probabilities of the scenarios
	 * @param currentTime:
	 *            the current time
	 * @return: a scenario state.
	 */
	private static CapacityScenarioState parseScenarios(Collection<CapacityScenario> scenarios, double cumP,
			DateTime currentTime) {
		List<CapacityScenario> newScenarioSet = new ArrayList<CapacityScenario>();
		boolean actualChosen = false;
		double randomNumber = Math.random();
		double newCumP = 0;

		CapacityScenario actualScenario = null;
		for (CapacityScenario nextScenario : scenarios) {
			double newP = nextScenario.getProbability() / cumP;
			nextScenario = nextScenario.setProbability(newP);
			newScenarioSet.add(nextScenario);

			newCumP += newP;
			// This assigns the actual scenario
			if (!actualChosen && newCumP > randomNumber) {
				actualScenario = nextScenario;
				actualChosen = true;
			}
		}
		int currentCapacity = actualScenario.getCurrentCapacity(currentTime);
		return new CapacityScenarioState(currentCapacity, actualScenario, newScenarioSet);
	}

	public static CapacityScenarioState parseLoToHigh(DateTime currentTime, Duration earlyTime, Duration lateTime,
			Duration step, int low, int high) {
		// Get the number of scenarios, and assume all scenarios have equal
		// probability
		int numScenarios = (int) ((lateTime.minus(earlyTime).getMillis()) / step.getMillis());
		double probability = 1 / ((double) numScenarios);

		// Now we make the scenarios
		List<CapacityScenario> myScenarios = new ArrayList<CapacityScenario>();
		for (int i = 0; i < numScenarios; i++) {
			DateTime clearTime = currentTime.plus(step.multipliedBy(i)).plus(earlyTime);

			SortedMap<DateTime, Integer> capacityMap = new TreeMap<DateTime, Integer>();
			capacityMap.put(currentTime, low);
			capacityMap.put(clearTime, high);
			myScenarios.add(new CapacityScenario(probability, capacityMap));
		}
		return parseScenarios(myScenarios, currentTime);
	}

	/**
	 * This reads a file of the following format: [minimum minutes to
	 * clear],[maximum minutes to clear], time step (minutes) [lo
	 * capacity],[high capacity]
	 *
	 */
	public static CapacityScenarioState parseLoToHigh(Scanner myScanner, DateTime currentTime)
			throws FileNotFoundException {

		String nextLine = "";
		while (nextLine.isEmpty() && myScanner.hasNext()) {
			nextLine = myScanner.nextLine();
		}
		String fields[] = nextLine.split(",");
		Duration earlyTime = Duration.standardMinutes(Integer.parseInt(fields[0].trim()));
		Duration lateTime = Duration.standardMinutes(Integer.parseInt(fields[1].trim()));
		int stepMin = Integer.parseInt(fields[2].trim());

		// Read the second line, which describes the capacities
		fields = myScanner.nextLine().split(",");
		int low = Integer.parseInt(fields[0].trim());
		int high = Integer.parseInt(fields[1].trim());
		myScanner.close();

		return parseLoToHigh(currentTime, earlyTime, lateTime, Duration.standardMinutes(stepMin), low, high);
	}

	public static CapacityScenarioState parseLoToHigh(File file, DateTime currentTime) throws FileNotFoundException {
		return parseLoToHigh(new Scanner(file), currentTime);
	}

	public static CapacityScenarioState parseScenario(CapacityScenario scenario, DateTime currentTime) {
		Set<CapacityScenario> myScenarioSet = new HashSet<CapacityScenario>();
		myScenarioSet.add(scenario);
		return parseScenarios(myScenarioSet, currentTime);
	}
}
