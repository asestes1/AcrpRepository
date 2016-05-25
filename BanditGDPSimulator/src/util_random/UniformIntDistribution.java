package util_random;

import java.util.Random;

public class UniformIntDistribution implements Distribution<Integer> {
	Random generator;
	private final int lower;
	private final int upper;
	//This is the number of units that the d

	public UniformIntDistribution(int lower, int upper) {
		this.lower = lower;
		this.upper = upper;
		generator = new Random();
	}

	@Override
	public Integer sample() {
		return generator.nextInt(upper-lower) + lower ;
	}

	@Override
	public String toString() {
		return "Uniform Distribution on interval [" + lower + ","
				+ upper + "]";

	}
}
