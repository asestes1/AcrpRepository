package metrics;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class PiecewiseLinearFunction {
	private final SortedMap<Double,Double> function;
	private final double start;
	
	public PiecewiseLinearFunction(SortedMap<Double,Double> function, double start){
		this.function = new TreeMap<Double,Double>(function);
		this.start = start;
	}
	
	public PiecewiseLinearFunction(PiecewiseLinearFunction other) {
		this.function = other.getFunction();
		this.start = other.getStart();
	}

	public double evaluateAt(double x){
		Set<Double> keys = function.keySet();
		Iterator<Double> myIter = keys.iterator();
		double prevKey = myIter.next();
		double value = start;
		double nextKey = prevKey;
		while(myIter.hasNext()){
			nextKey = myIter.next();
			if(nextKey > x){
				value += function.get(prevKey)*(x-prevKey);
				return value;
			}else{
				value += function.get(prevKey)*(nextKey-prevKey);
				prevKey = nextKey;
			}
		}
		return (value + (x-nextKey)*function.get(nextKey));
	}

	public SortedMap<Double, Double> getFunction() {
		return new TreeMap<Double,Double>(function);
	}

	public double getStart() {
		return start;
	}
	
}
