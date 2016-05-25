package airline_response_factories;

import java.io.File;
import java.util.Scanner;

import org.joda.time.Duration;

import airline_response.DefaultAirlineResponse;

public final class AirlineResponseFactory {
	private AirlineResponseFactory(){
		
	}
	
	public static DefaultAirlineResponse parse(File input) throws Exception {
		return parse(new Scanner(input));
	}
	
	public static DefaultAirlineResponse parse(Scanner scanner){
		String input = "";
		while(scanner.hasNext() && input.isEmpty()){
			input = scanner.nextLine();
		}
		
		String logFile = input.trim();
		double flightDelayCost = Double.parseDouble(scanner.nextLine().trim());
		double passengerDelayCost = Double.parseDouble(scanner.nextLine().trim());
		int minutesAllowed = Integer.parseInt(scanner.nextLine().trim());
		return new DefaultAirlineResponse(logFile,
				flightDelayCost, passengerDelayCost, Duration.standardMinutes(minutesAllowed));
	}
}
