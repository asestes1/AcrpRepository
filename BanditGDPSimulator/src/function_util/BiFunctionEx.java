package function_util;

public interface BiFunctionEx<S,T,U,X extends Throwable> {
	public U apply(S arg1,T arg2) throws X;
}
