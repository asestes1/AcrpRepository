package function_util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.linear.RealVector;

public class MultiVarPolynomial implements VectorDifferentiableFunction {
	private final int n_variables;
	//This stores the non-zero coefficients. All other terms are assumed to be zero.
	private final Map<IntTuple,Double> coefficients;
	
	public MultiVarPolynomial(int n_variables, Map<IntTuple,Double> coefficients) {
		this.n_variables = n_variables;
		this.coefficients = new HashMap<IntTuple,Double>(coefficients);
	}
	
	public MultiVarPolynomial(MultiVarPolynomial polynomial) {
		this.n_variables = polynomial.getN_variables();
		this.coefficients = polynomial.getCoefficients();
	}
	
	@Override
	public Double apply(RealVector t) {
		double value = 0.0;
		Iterator<IntTuple> my_key_iter = coefficients.keySet().iterator();
		while(my_key_iter.hasNext()){
			IntTuple next_term = my_key_iter.next();
			double product = coefficients.get(next_term);
			for(int i =0; i < n_variables;i++){
				product = product*Math.pow(t.getEntry(i),next_term.getEntry(i));
			}
			value += product;
		}
		return value;
	}

	@Override
	public Function<RealVector, RealVector> derivative() {
		List<MultiVarPolynomial> my_partial_derivatives = 
				new ArrayList<MultiVarPolynomial>(n_variables);
		for(int i=0; i < n_variables;i++){
			my_partial_derivatives.add(partial_derivative(i));
		}
		return FunctionHelper.vectorize(my_partial_derivatives);
	}

	public int getN_variables() {
		return n_variables;
	}
	
	public MultiVarPolynomial partial_derivative(int x_index){
		HashMap<IntTuple,Double> new_coeff = new HashMap<IntTuple,Double>();
		Iterator<IntTuple> my_term_iter = coefficients.keySet().iterator();
		while(my_term_iter.hasNext()){
			IntTuple next_term = my_term_iter.next();
			int x_degree = next_term.getEntry(x_index);
			double value = coefficients.get(next_term)*x_degree;
			if(x_degree >= 1){
				new_coeff.put(next_term.setEntry(x_index, x_degree-1),value);
			}
		}
		return new MultiVarPolynomial(n_variables,new_coeff);
	}

	public Map<IntTuple, Double> getCoefficients() {
		return new HashMap<IntTuple,Double>(coefficients);
	}
	
}
