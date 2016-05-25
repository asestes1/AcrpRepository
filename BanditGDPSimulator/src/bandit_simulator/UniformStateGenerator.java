package bandit_simulator;

import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class UniformStateGenerator implements Generator<RealVector> {
	private Random numberGenerator;
	private final int dimension;

	public UniformStateGenerator(int dimension) {
		numberGenerator = new Random();
		this.dimension = dimension;
	}

	@Override
	public RealVector generate() {
		RealVector my_vector = new ArrayRealVector(dimension);
		for (int i = 0; i < dimension; i++) {
			my_vector.setEntry(i, numberGenerator.nextDouble());
		}
		return my_vector;
	}

}
