package util_random;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joda.time.Duration;

public final class DistributionFactory {

	/**
	 * This function produces a distribution from three strings
	 * 
	 * @param type
	 *            - the type of distribution e.g. geometric, uniform, binomial
	 * @param intParamString
	 *            - a string containing a list of integer parameters, comma
	 *            separated
	 * @param doubleParamString
	 *            - a string containing a list of double parameters, comma
	 *            separated
	 * @return
	 */
	private static Distribution<Duration> makeDistribution(String type, String intParamString,
			String doubleParamString) {
		List<Integer> intParams = new ArrayList<Integer>();
		String[] stringParams = intParamString.split(",");
		int numParams = stringParams.length;
		for (int i = 0; i < numParams; i++) {
			intParams.add(Integer.parseInt(stringParams[i]));
		}

		List<Double> dParams = new ArrayList<Double>();
		stringParams = doubleParamString.split(",");
		numParams = stringParams.length;
		for (int i = 0; i < numParams; i++) {
			dParams.add(Double.parseDouble(stringParams[i]));
		}

		if (type.equalsIgnoreCase("constant_sec")) {
			return new ConstantDistribution<Duration>(Duration.standardSeconds(intParams.get(0)));
		} else if (type.equalsIgnoreCase("uniform_sec")) {
			return new UniformDurationDistribution(intParams.get(0), intParams.get(1), Duration.standardSeconds(1));
		} else if (type.equalsIgnoreCase("bernoulli_uniform_sec")) {
			return new BernoulliUniformDistribution(dParams.get(0), intParams.get(0), intParams.get(1),
					Duration.standardSeconds(1));
		}
		return new ConstantDistribution<Duration>(Duration.standardSeconds(0));
	}

	public static Distribution<Duration> parseDistribution(Scanner input) throws Exception {
		String type = "";
		while (type.isEmpty() && input.hasNext()) {
			type = input.nextLine();
		}
		String intParams = input.nextLine();
		String doubleParams = input.nextLine();
		return makeDistribution(type, intParams, doubleParams);
	}

	/**
	 * This function produces a distribution from three strings
	 * 
	 * @param type
	 *            - the type of distribution e.g. geometric, uniform, binomial
	 * @return
	 * @throws Exception
	 */
	public static ParameterizedDistribution<Integer, Duration> parseParameterizedDistribution(String type) throws Exception {

		if (type.equalsIgnoreCase("constant_runway")) {
			return new ConstantRunwayDistribution();
		}
		throw new Exception("Invalid distribution type specified");
	}

	/**
	 * This takes an open scanner object, whose next non-empty line describes a
	 * parameterized distribution.
	 * 
	 * @author Alex2
	 *
	 */
	public static ParameterizedDistribution<Integer, Duration> parseParameterizedDistribution(Scanner input) throws Exception {
		String line = "";
		while (line.isEmpty() && input.hasNextLine()) {
			line = input.nextLine();
		}
		return parseParameterizedDistribution(line);
	}
}
