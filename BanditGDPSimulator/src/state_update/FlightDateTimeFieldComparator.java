package state_update;

import java.io.Serializable;
import java.util.Comparator;

import org.joda.time.DateTime;

import bandit_objects.Immutable;
import state_representation.Flight;

public class FlightDateTimeFieldComparator implements Comparator<Flight>, Immutable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6156129390955762488L;
	private final int fieldID;
	private final DateTime currentTime;
	private final boolean timeDependent;
	
	public FlightDateTimeFieldComparator(int fieldID){
		this.fieldID = fieldID;
		this.currentTime = null;
		this.timeDependent = false;
	}
	public FlightDateTimeFieldComparator(DateTime currentTime, int fieldID2) {
		this.fieldID = fieldID2;
		this.currentTime = currentTime;
		this.timeDependent = true;
	}
	@Override
	public int compare(Flight arg0, Flight arg1) {
		try {
			if(timeDependent){
				if (arg0.getDateTimeField(fieldID,currentTime).equals(
						arg1.getDateTimeField(fieldID,currentTime))) {
					return arg0.getFlightNumber() - arg1.getFlightNumber();
				}else{
					return arg0.getDateTimeField(fieldID,currentTime).compareTo(
							arg1.getDateTimeField(fieldID,currentTime));
				}
			}else{
				if (arg0.getDateTimeField(fieldID).equals(
						arg1.getDateTimeField(fieldID))) {
					return arg0.getFlightNumber() - arg1.getFlightNumber();
				}else{
					return arg0.getDateTimeField(fieldID).compareTo(
							arg1.getDateTimeField(fieldID));
				}
			}
		} catch (Exception e) {
			return arg0.getFlightNumber() - arg1.getFlightNumber();
		}
	}

}
