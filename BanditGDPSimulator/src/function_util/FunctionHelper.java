package function_util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public abstract class FunctionHelper{
	public static <T,S extends Function<T,Double>> Function<T,RealVector> vectorize(List<S> function_list){
		return new VectorizedFunction<T,S>(function_list);
	}
	
	private static class VectorizedFunction<T,S extends Function<T,Double>> implements Function<T,RealVector>{
		private final List<S> function_list;
		public VectorizedFunction(List<S> function_list) {
			this.function_list = new ArrayList<S>(function_list);
		}
		@Override
		public RealVector apply(T t) {
			int length = function_list.size();
			RealVector my_vector = new ArrayRealVector(length);
			Iterator<S> my_iter = function_list.iterator();
			for (int i=0;i< length;i++){
				my_vector.setEntry(i,my_iter.next().apply(t));
			}
			return my_vector;
		}
	}


}
