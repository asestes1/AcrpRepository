package state_factories;

import java.security.InvalidParameterException;

import javax.naming.directory.InvalidAttributesException;

import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import state_representation.Flight;

public final class FlightFactory {
	public final static int BTS_FORMAT_ID = 0;
	public final static int BASIC_FORMAT_ID = 1;
	public final static int ADL_FORMAT_ID = 2;
	private FlightFactory() {

	}

	private static UninitializedFlightStruct parseAdlCsvRecord(CSVRecord record,int year, int month, int day){
		String etdTimeString = record.get("OETD");
		int etdTimeInt = (int) Double.parseDouble(etdTimeString);
		int etdYear = year;
		int etdMonth = month;
		int etdDay = (etdTimeInt)/10000;
		int	etdHour = (etdTimeInt %10000)/100;
		int etdMinute = etdTimeInt % 100;	
		if(etdDay < day -15){
			if(month != 12){
				etdMonth++;
			}else{
				etdMonth = 1;
				etdYear++;
			}
		}else if(etdDay > day + 15){
			if(month != 1){
				etdMonth--;
			}else{
				etdMonth=12;
				etdYear--;
			}
		}
		
		String etaTimeString = record.get("OETA");
		int etaTimeInt = (int) Double.parseDouble(etaTimeString);
		int etaYear = year;
		int etaMonth = month;
		int etaDay = (etaTimeInt)/10000;
		int	etaHour = (etaTimeInt %10000)/100;
		int etaMinute = etaTimeInt % 100;
		if(etaDay < day -15){
			if(month != 12){
				etaMonth++;
			}else{
				etaMonth = 1;
				etaYear++;
			}
		}else if(etaDay > day + 15){
			if(month != 1){
				etaMonth--;
			}else{
				etaMonth=12;
				etaYear--;
			}
		}

		String airlineId = record.get("MAJOR");
		int distance = (int) Double.parseDouble(record.get("GCD"));

		// Get the original departure and arrival times
		DateTime origETA = new DateTime(etaYear, etaMonth, etaDay, etaHour, etaMinute, DateTimeZone.UTC);
		DateTime origETD = new DateTime(etdYear, etdMonth, etdDay, etdHour, etdMinute, DateTimeZone.UTC);
		return new UninitializedFlightStruct(airlineId, distance, origETD, origETA);
	}
	
	private static UninitializedFlightStruct parseBtsCsvRecord(CSVRecord record, DateTimeZone timeZone){
		int etdYear = Integer.parseInt(record.get("Year"));
		int etdMonth = Integer.parseInt(record.get("Month"));
		int etdDay = Integer.parseInt(record.get("DayofMonth"));
		String flightTimeString = record.get("CRSElapsedTime");
		Duration flightTime = Duration.standardMinutes((int) Double.parseDouble(flightTimeString));
		String etaTimeString = record.get("CRSArrTime");

		int etaTimeInt = (int) Double.parseDouble(etaTimeString);
		int	etaHour = etaTimeInt/100;
		int etaMinute = etaTimeInt % 100;

		String airlineId = record.get("Carrier");
		int distance = (int) Double.parseDouble(record.get("Distance"));

		// Get the original departure and arrival times
		DateTime origETA = new DateTime(etdYear, etdMonth, etdDay, etaHour, etaMinute,timeZone);
		DateTime origETD = origETA.minus(flightTime);
		return new UninitializedFlightStruct(airlineId, distance, origETD, origETA);
	}
	
	static UninitializedFlightStruct parseBasicFlight(CSVRecord record, DateTimeZone timeZone) {

		// Get the flight number, airline and number of passengers
		String airlineId = record.get(1).trim();
		Integer distance = Integer.parseInt(record.get(2));

		// Get the original departure and arrival times
		DateTime origETD = DateTimeFactory.parse(record.get(3), timeZone);
		DateTime origETA = DateTimeFactory.parse(record.get(4), timeZone);

		return new UninitializedFlightStruct(airlineId, distance, origETD, origETA);
	}

	private static class UninitializedFlightStruct {
		public UninitializedFlightStruct(String airlineId, Integer distance, DateTime origETD, DateTime origETA) {
			this.airlineId = airlineId;
			this.distance = distance;
			this.origETA = origETA;
			this.origETD = origETD;
		}

		public final String airlineId;
		public final Integer distance;
		public final DateTime origETD;
		public final DateTime origETA;
	}

	/**
	 * Flights are not given any delay. If the flights original departure time
	 * is before the current time, then the flight will have taken off. If the
	 * flights original eta is before the current time, then the flight will
	 * land.
	 */
	public static Flight parseBasicFlight(UninitializedFlightStruct struct, DateTime currentTime) {
		boolean airborne = false;
		boolean landed = false;
		boolean gdpDelayed = false;
		boolean cancelled = false;
		Integer numPassengers = null;
		DateTime aETD = struct.origETD;
		DateTime aETA = null;
		DateTime cETD = null;
		DateTime cETA = null;
		Duration zero = Duration.millis(0);

		// Check to see if flight has taken off
		if (aETD.isBefore(currentTime)) {
			aETA = struct.origETA;
			// Check to see if flight has landed
			if (aETA.isBefore(currentTime)) {
				landed = true;
			} else {
				airborne = true;
			}
		}
		return new Flight(struct.airlineId, numPassengers,struct.distance, struct.origETD, struct.origETA,
				cETD, cETA, aETD, aETA, airborne, landed, gdpDelayed, cancelled, zero, zero, zero, zero, zero);
	}

	/**
	 * Note: if you are parsing ADL data, it is assumed that the current time occurs on the same day as the ADL file.
	 * @param csvRecord
	 * @param currentTime
	 * @param format
	 * @return
	 * @throws InvalidAttributesException
	 */
	public static Flight parseFlight(CSVRecord csvRecord, DateTime currentTime, DateTimeZone timeZone, int format) throws InvalidAttributesException {
		Flight myFlight =null;
		if (format == BTS_FORMAT_ID) {
			myFlight= parseBasicFlight(parseBtsCsvRecord(csvRecord, timeZone), currentTime);
		} else if (format == BASIC_FORMAT_ID) {
			myFlight =parseBasicFlight(parseBasicFlight(csvRecord, timeZone), currentTime);
		}else if(format == ADL_FORMAT_ID){
			myFlight = parseBasicFlight(parseAdlCsvRecord(csvRecord, currentTime.getYear(),currentTime.getMonthOfYear(), currentTime.getDayOfMonth()),
					currentTime);
		}else{
			throw new InvalidParameterException("Specified format id is invalid.");
		}
		return myFlight;
	}

}
