package util_random;

/**
 * 
 * @author Alex2
 *
 * @param <T> The class of parameter used to sample
 * @param <S> The output of the sampling
 */
public interface ParameterizedDistribution<T,S> {
	public abstract S sample(T parameter);
	
}
