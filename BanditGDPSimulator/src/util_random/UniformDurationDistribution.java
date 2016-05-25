package util_random;

import java.util.Random;

import org.joda.time.Duration;

public class UniformDurationDistribution implements Distribution<Duration> {
	Random generator;
	private final int lower;
	private final int upper;
	//This is the number of units that the d
	private final Duration unit;

	public UniformDurationDistribution(int lower, int upper, Duration unit) {
		this.lower = lower;
		this.upper = upper;
		this.unit = unit;
		generator = new Random();
	}

	@Override
	public Duration sample() {
		return unit.multipliedBy(generator.nextInt(upper-lower) + lower) ;
	}

	@Override
	public String toString() {
		return "Uniform Distribution on interval [" + lower + ","
				+ upper + "]";

	}

}
