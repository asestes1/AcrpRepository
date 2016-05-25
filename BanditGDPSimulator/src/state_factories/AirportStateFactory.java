package state_factories;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import state_representation.AirportState;

public final class AirportStateFactory {
	private AirportStateFactory(){
		
	}
	
	/**
	 * This implements a factory which takes a file and creates an AirportState. The file
	 * should be in the form:
	 * 
	 * First line - next available runway time, in format 'yyyy/mm/dd hh:mm'
	 *
	 */
	public static AirportState parseAirportState(File file,DateTime dateTime) throws FileNotFoundException{
		return parseAirportState(new Scanner(file),dateTime);
	}
	
	public static AirportState parseAirportState(Scanner scanner, DateTime dateTime) {
		//Find first non-empty line
		String input = "";
		while(scanner.hasNext() && input.isEmpty()){
			input = scanner.nextLine();
		}
		int minutesDelay = Integer.parseInt(input.trim());
		return new AirportState(dateTime.plus(Duration.standardMinutes(minutesDelay)));
	}
}
