package random_processes;

import java.util.HashMap;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public final class GaussianTmiComparerFactory {
	private GaussianTmiComparerFactory(){
		
	}
	private static final double rateVar= 10.87533169;
	private static final double startVar= 4974.050068;
	private static final double durationVar= 18916.78948;
	private static final double radiusVar= 674892.3393;
	

	private static final RealVector defaultTmiActivity 
	= new ArrayRealVector(new double[] {1/(rateVar),
			1/(startVar),1/(durationVar),1/(radiusVar)});
	
	public static final RealVector getDefaultGdpActivity(){
		return new ArrayRealVector(defaultTmiActivity);
	}
	
	public static final RealVector getDefaultGsActivity(){
		return new ArrayRealVector(defaultTmiActivity).getSubVector(1, 3);
	}
	public static final GaussianTmiComparer makeDefaultTmiComparer(){
		HashMap<Integer,GaussianKernel> myMap = new HashMap<Integer,GaussianKernel>();
		myMap.put(SimpleTmiAction.GDP_TYPE,new GaussianKernel(1.0,1.0, getDefaultGdpActivity()));
		myMap.put(SimpleTmiAction.GS_TYPE, new GaussianKernel(1.0,1.0, getDefaultGsActivity()));
		return new GaussianTmiComparer(myMap);
	}
	
	public static final GaussianTmiComparer makeDefaultTmiComparer(double bandwidth){
		HashMap<Integer,GaussianKernel> myMap = new HashMap<Integer,GaussianKernel>();
		myMap.put(SimpleTmiAction.GDP_TYPE,new GaussianKernel(1.0,bandwidth, getDefaultGdpActivity()));
		myMap.put(SimpleTmiAction.GS_TYPE, new GaussianKernel(1.0,bandwidth, getDefaultGsActivity()));
		return new GaussianTmiComparer(myMap);
	}
}
