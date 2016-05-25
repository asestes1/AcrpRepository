package function_util;

public interface FunctionEx<T,U,X extends Throwable> {
	
	public U apply(T argument) throws X;

}
