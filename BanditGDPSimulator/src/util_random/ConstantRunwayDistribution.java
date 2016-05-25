package util_random;

import org.joda.time.Duration;

public class ConstantRunwayDistribution implements
		ParameterizedDistribution<Integer, Duration> {

	@Override
	public Duration sample(Integer parameter) {
		return Duration.standardHours(1).dividedBy(parameter);
	}


}
