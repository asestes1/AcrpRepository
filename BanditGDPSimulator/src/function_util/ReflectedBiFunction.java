package function_util;

import java.util.function.BiFunction;

public class ReflectedBiFunction<R,S> implements BiFunction<R, S,Double>{
	private final BiFunction<R,S,Double> originalFunction;
	private final double bound;
	
	public ReflectedBiFunction(BiFunction<R,S,Double> originalFunction,double d) {
		this.originalFunction = originalFunction;
		this.bound = d;
	}
	@Override
	public Double apply(R r,S s) {
		double result = Math.abs(originalFunction.apply(r,s));
		int roundedQuotient = (int) (result/bound);
		double remainder = result % bound;
		if(roundedQuotient % 2 == 0){
			return remainder;
		}else{
			return bound-remainder;
		}
	}
}
