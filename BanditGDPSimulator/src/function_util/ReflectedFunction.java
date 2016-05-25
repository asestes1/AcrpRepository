package function_util;

import java.util.function.Function;

public class ReflectedFunction<R> implements Function<R,Double>{
	private final Function<R,Double> originalFunction;
	private final double bound;
	
	public ReflectedFunction(Function<R,Double> originalFunction,double d) {
		this.originalFunction = originalFunction;
		this.bound = d;
	}
	@Override
	public Double apply(R t) {
		double result = Math.abs(originalFunction.apply(t));
		int roundedQuotient = (int) (result/bound);
		double remainder = result % bound;
		if(roundedQuotient % 2 == 0){
			return remainder;
		}else{
			return bound-remainder;
		}
	}

}
