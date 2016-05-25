package util_random;


/**
 * This is
 * 
 * @author Alex2
 *
 */
public class ConstantDistribution<T> implements Distribution<T> {
	private final T parameter;

	/**
	 * Constructor
	 * 
	 * @param parameter
	 *            - the single parameter of the constant distribution
	 */
	public ConstantDistribution(T parameter) {
		this.parameter = parameter;
	}

	/**
	 * This should return a sample from the distribution
	 * 
	 * @return
	 */
	public T sample() {
		return parameter;
	}

	/**
	 * 
	 * @param stream
	 */
	public String toString() {
		return "Constant Distribution with parameter " + parameter.toString();
	}

}
