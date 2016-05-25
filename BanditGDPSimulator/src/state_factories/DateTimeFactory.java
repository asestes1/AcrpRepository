package state_factories;

import java.io.File;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class DateTimeFactory {
	private DateTimeFactory() {

	}

	/**
	 * This uses a file to create a DateTime object. The file should consist of
	 * single line in the following format:
	 * 
	 * yyyy/mm/dd hh:mm
	 *
	 */
	@Deprecated
	public static DateTime parse(File file) throws Exception {
		Scanner scanner = new Scanner(file);
		String line = "";
		while (scanner.hasNextLine() && line.isEmpty()) {
			line = scanner.nextLine();
		}
		scanner.close();
		if (line.isEmpty()) {
			throw new Exception("File is empty.");
		}
		return parse(line);
	}
	
	@Deprecated
	public static DateTime parse(String inputString) {
		inputString = inputString.trim();
		String[] strings = inputString.split(" +");
		String[] date = strings[0].split("/");
		String[] time = strings[1].split(":");
		int year = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		int day = Integer.parseInt(date[2]);
		int hour = Integer.parseInt(time[0]);
		int minute = Integer.parseInt(time[1]);
		return new DateTime(year,month,day,hour,minute);
	}
	
	public static DateTime parse(String inputString, DateTimeZone myTimeZone) {
		inputString = inputString.trim();
		String[] strings = inputString.split(" +");
		String[] date = strings[0].split("/");
		String[] time = strings[1].split(":");
		int year = Integer.parseInt(date[0]);
		int month = Integer.parseInt(date[1]);
		int day = Integer.parseInt(date[2]);
		int hour = Integer.parseInt(time[0]);
		int minute = Integer.parseInt(time[1]);
		return new DateTime(year,month,day,hour,minute,myTimeZone);
	}

	public static DateTime parse(File file, DateTimeZone timeZone) throws Exception {
		Scanner scanner = new Scanner(file);
		String line = "";
		while (scanner.hasNextLine() && line.isEmpty()) {
			line = scanner.nextLine();
		}
		scanner.close();
		if (line.isEmpty()) {
			throw new Exception("File is empty.");
		}
		return parse(line,timeZone);
	}
}
