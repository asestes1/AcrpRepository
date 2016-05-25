package bandit_objects;

import java.io.Serializable;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import function_util.HashDoubleUtil;

public class SimpleTmiAction implements Immutable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2750215117505746573L;
	public static final int GDP_DIMENSION = 4;
	public static final int GS_DIMENSION = 3;

	public static final int INVALID_TYPE = -1;
	public static final int GDP_TYPE = 2;
	public static final int GS_TYPE = 3;
	public static final int NONE_TYPE = 0;

	private final Integer type;
	private final Double rate;
	private final Double radius;
	private final Double startTimeMin;
	private final Double durationMin;

	public SimpleTmiAction(SimpleTmiAction myAction) {
		this.type = myAction.type;
		this.rate = myAction.rate;
		this.radius = myAction.radius;
		this.startTimeMin = myAction.startTimeMin;
		this.durationMin = myAction.durationMin;
	}
	
	public SimpleTmiAction(boolean valid) {
		if(valid){
			this.type = NONE_TYPE;
		}else{
			this.type = INVALID_TYPE;
		}
		this.rate = null;
		this.radius = null;
		this.startTimeMin = null;
		this.durationMin = null;
	}
	
	public SimpleTmiAction() {
		super();
		this.type = NONE_TYPE;
		this.rate = null;
		this.radius = null;
		this.startTimeMin = null;
		this.durationMin = null;
	}

	public SimpleTmiAction(Double rate, Double startTimeMin, Double durationMin, Double radius) {
		super();
		this.type = GDP_TYPE;
		this.rate = rate;
		this.radius = radius;
		this.startTimeMin = startTimeMin;
		this.durationMin = durationMin;
	}
	
	public SimpleTmiAction(Double startTimeMin, Double durationMin, Double radius) {
		super();
		this.type = GS_TYPE;
		this.rate = null;
		this.radius = radius;
		this.startTimeMin = startTimeMin;
		this.durationMin = durationMin;
	}

	public SimpleTmiAction(RealVector vector) {
		if (vector.getDimension() == 0) {
			this.type = NONE_TYPE;
			this.rate = null;
			this.radius = null;
			this.startTimeMin = null;
			this.durationMin = null;
		} else if (vector.getDimension() == GS_DIMENSION) {
			this.type = GS_TYPE;
			this.rate = null;
			this.startTimeMin = vector.getEntry(0);
			this.durationMin = vector.getEntry(1);
			this.radius = vector.getEntry(2);
		} else if (vector.getDimension() == GDP_DIMENSION) {
			this.type = GDP_TYPE;
			this.rate = vector.getEntry(0);
			this.startTimeMin = vector.getEntry(1);
			this.durationMin = vector.getEntry(2);
			this.radius = vector.getEntry(3);
		} else {
			throw new DimensionMismatchException(vector.getDimension(), GDP_DIMENSION);
		}
	}

	public SimpleTmiAction(int i, int i2, int j, int k) {
		this((double) i, (double) i2, (double) j, (double) k);
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static Integer getGdpDimension() {
		return GDP_DIMENSION;
	}

	public static Integer getGsDimension() {
		return GS_DIMENSION;
	}

	public static Integer getGdpType() {
		return GDP_TYPE;
	}

	public static Integer getGsType() {
		return GS_TYPE;
	}
	
	public static Integer getDimensionByType(Integer type){
		if(type.equals(GDP_TYPE)){
			return GDP_DIMENSION;
		}else if(type.equals(GS_TYPE)){
			return GS_DIMENSION;
		}else{
			return 0;
		}
	}

	public static Integer getNoneType() {
		return NONE_TYPE;
	}

	public int getType() {
		return type;
	}

	public Double getRate() {
		return rate;
	}

	public Double getRadius() {
		return radius;
	}

	public Double getStartTimeMin() {
		return startTimeMin;
	}

	public Double getDurationMin() {
		return durationMin;
	}

	public RealVector asVector() {
		RealVector myVector = new ArrayRealVector(vectorDimension());
		if(type.equals(GDP_TYPE)){
			myVector.setEntry(0, rate);
			myVector.setEntry(1, startTimeMin);
			myVector.setEntry(2, durationMin);
			myVector.setEntry(3, radius);
		}else if(type.equals(GS_TYPE)){
			myVector.setEntry(0, startTimeMin);
			myVector.setEntry(1, durationMin);
			myVector.setEntry(2, radius);
		}

		return myVector;
	}

	public int vectorDimension() {
		if(type.equals(GDP_TYPE)){
			return GDP_DIMENSION;
		}else if(type.equals(GS_TYPE)){
			return GS_DIMENSION;
		}else{
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SimpleTmiAction) {
			SimpleTmiAction other = ((SimpleTmiAction) o);
			if(!type.equals(other.type)){
				return false;
			}else if(type.equals(NONE_TYPE) || type.equals(INVALID_TYPE)){
				return true;
			}else if(radius.equals(other.radius) && durationMin.equals(other.durationMin)
					&& startTimeMin.equals(other.startTimeMin)){
				if(type.equals(GS_TYPE)){
					return true;
				}else if (type.equals(GDP_TYPE) && type.equals(other.type) && rate.equals(other.rate)) {
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}

		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = type;
		result = 37 * result + HashDoubleUtil.HashDouble(startTimeMin);
		result = 37 * result + HashDoubleUtil.HashDouble(durationMin);
		result = 37 * result + HashDoubleUtil.HashDouble(radius);
		result = 37 * result + HashDoubleUtil.HashDouble(rate);
		return (int) result;

	}

	@Override
	public String toString() {
		if (type.equals(NONE_TYPE)) {
			return "TMI type: no action.";
		}else if(type.equals(INVALID_TYPE)){
			return "TMI type: not simple.";
		}else {
			String myString = "TMI type: ";
			if (type.equals(GDP_TYPE)) {
				myString += "GDP. Rate: " + rate;
			} else if(type.equals(GS_TYPE)){
				myString += "GS. ";
			}else{
				return "Unknown type: "+type;
			}
			myString += ", Start time: " + startTimeMin + ", Duration: " + durationMin + ", Radius: " + radius;
			return myString;
		}
	}
}
