package state_update;

import java.util.Comparator;

import org.joda.time.Duration;

import state_representation.Flight;

/**
 * This sorts flights by DECREASING distance. Distance is measured in terms of
 * original estimated time of arrival minus original estimated time of departure
 *
 * @author Alex2
 *
 */
public class FlightTimeComparator implements Comparator<Flight> {

	@Override
	public int compare(Flight arg0, Flight arg1) {
		Duration dist0 = new Duration(arg0.getOrigETD(), arg0.getOrigETA());
		Duration dist1 = new Duration(arg1.getOrigETD(), arg1.getOrigETA());
		if (dist0.compareTo(dist1) == 0) {
			return arg0.getFlightNumber() - arg1.getFlightNumber();
		}
		return dist1.compareTo(dist0);
	}

}
