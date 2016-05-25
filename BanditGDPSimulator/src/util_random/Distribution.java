package util_random;


public interface Distribution<T> {

	/**
	 * 
	 * @return a single sample from the distribution.
	 */
	public abstract T sample();

	public abstract String toString();

}
