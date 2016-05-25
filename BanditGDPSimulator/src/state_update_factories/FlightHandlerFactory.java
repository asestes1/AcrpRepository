package state_update_factories;

import java.io.File;
import java.util.Scanner;

import org.joda.time.Duration;

import state_update.DefaultFlightHandler;
import state_update.FlightHandler;
import util_random.Distribution;
import util_random.DistributionFactory;

public final class FlightHandlerFactory {
	private FlightHandlerFactory(){
		
	}
	
	public static FlightHandler parseFlightHandler(File input) throws Exception {
		Scanner myScanner = new Scanner(input);
		return parseFlightHandler(myScanner);
	}
	
	public static FlightHandler parseFlightHandler(Scanner input) throws Exception {
		Distribution<Duration> depDelayDist = DistributionFactory.parseDistribution(input);
		Distribution<Duration> arrDelayDist = DistributionFactory.parseDistribution(input);
		return new DefaultFlightHandler(depDelayDist, arrDelayDist);
	}
}
