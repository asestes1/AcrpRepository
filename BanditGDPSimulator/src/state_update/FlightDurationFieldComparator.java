package state_update;

import java.util.Comparator;

import state_representation.Flight;

public class FlightDurationFieldComparator implements Comparator<Flight>{
	private final int fieldID;
	
	public FlightDurationFieldComparator(int fieldID) {
		this.fieldID = fieldID;
	}
	@Override
	public int compare(Flight arg0, Flight arg1) {
		try {
			if (arg0.getDurationField(fieldID).equals(
					arg1.getDurationField(fieldID))) {
				return arg0.getFlightNumber() - arg1.getFlightNumber();
			}else{
				return arg0.getDurationField(fieldID).compareTo(
						arg1.getDurationField(fieldID));
			}
		} catch (Exception e) {
			return arg0.getFlightNumber() - arg1.getFlightNumber();
		}
	}
}
