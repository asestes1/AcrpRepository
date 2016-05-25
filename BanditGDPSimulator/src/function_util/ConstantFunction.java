package function_util;

import java.util.function.Function;

import bandit_objects.SimpleTmiAction;

public class ConstantFunction implements Function<SimpleTmiAction,Double> {
	protected final double constant;
	
	public ConstantFunction() {
		this.constant = 0.0;
	}
	
	public ConstantFunction(double constant) {
		this.constant = constant;
	}
	
	public ConstantFunction(ConstantFunction function){
		this.constant = function.getConstant();
	}
	@Override
	public Double apply(SimpleTmiAction t) {
		return constant;
	}
	
	public double getConstant(){
		return constant;
	}
}
