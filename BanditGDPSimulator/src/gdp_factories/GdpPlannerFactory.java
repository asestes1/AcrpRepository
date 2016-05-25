package gdp_factories;

import java.io.File;
import java.util.Comparator;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.Duration;

import function_util.FunctionEx;
import gdp_planning.ConstantRadiusChooser;
import gdp_planning.DefaultDiscretePAARChooserWrapper;
import gdp_planning.DirectExtendedHofkinModel;
import gdp_planning.DirectHofkinModel;
import gdp_planning.DirectMHDynModel;
import gdp_planning.FixedDurationGDPIntervalChooser;
import gdp_planning.HofkinModel;
import gdp_planning.IndParamPlanner;
import gdp_planning.RichettaOdoniModel;
import gdp_planning.StandardTmiPlanner;
import metrics.PiecewiseLinearFunction;
import model.GdpAction;
import state_representation.DefaultState;
import state_representation.Flight;
import state_update.FlightDateTimeFieldComparator;
import state_update.FlightDurationFieldComparator;

public final class GdpPlannerFactory {
	private GdpPlannerFactory() {
	}

	public static DirectExtendedHofkinModel parseDirectExtendedHofkinModel(File input) throws Exception {
		DirectHofkinModel myHofkinModel = parseDirectHofkinModel(new Scanner(input));
		return new DirectExtendedHofkinModel(myHofkinModel.getGroundCost(), myHofkinModel.getAirCost(),
				myHofkinModel.getTimePeriodDuration(), myHofkinModel.getMyIntervalChooser(),
				myHofkinModel.getFlightComparator());
	}

	public static DirectHofkinModel parseDirectHofkinModel(File input) throws Exception {
		return parseDirectHofkinModel(new Scanner(input));
	}

	public static DirectHofkinModel parseDirectHofkinModel(Scanner scanner) throws Exception {
		// Find first non-empty line
		String input = "";
		while (scanner.hasNext() && input.isEmpty()) {
			input = scanner.nextLine();
		}
		// Get the ground cost
		double groundCost = Double.parseDouble(input.trim());
		double airCost = Double.parseDouble(scanner.nextLine().trim());
		String assignType = scanner.nextLine();
		Comparator<Flight> myComparator;
		if (assignType.equalsIgnoreCase("RBS")) {
			myComparator = new FlightDateTimeFieldComparator(Flight.origETAFieldID);
		} else if (assignType.equalsIgnoreCase("RBD")) {
			myComparator = new FlightDurationFieldComparator(Flight.flightTimeID).reversed();
		} else {
			throw new Exception("Invalid argument for assignment type: " + assignType);
		}
		int minsPerPeriod = Integer.parseInt(scanner.nextLine().trim());
		int gdpDuration = Integer.parseInt(scanner.nextLine().trim());

		return new DirectHofkinModel(groundCost, airCost, Duration.standardMinutes(minsPerPeriod),
				new FixedDurationGDPIntervalChooser(Duration.standardMinutes(gdpDuration)), myComparator);
	}

	public static DirectMHDynModel parseDirectMHDynModel(File input) throws Exception {
		DirectHofkinModel myHofkinModel = parseDirectHofkinModel(new Scanner(input));
		return new DirectMHDynModel(myHofkinModel.getGroundCost(), myHofkinModel.getAirCost(),
				myHofkinModel.getTimePeriodDuration(), myHofkinModel.getMyIntervalChooser());
	}

	public static StandardTmiPlanner parseHofkinPlanner(File input) throws Exception {
		return parseHofkinPlanner(new Scanner(input));
	}

	public static StandardTmiPlanner parseHofkinPlanner(Scanner scanner) throws Exception {
		// Find first non-empty line
		String input = "";
		while (scanner.hasNext() && input.isEmpty()) {
			input = scanner.nextLine();
		}
		// Get the ground cost
		double groundCost = Double.parseDouble(input.trim());
		double airCost = Double.parseDouble(scanner.nextLine().trim());
		String assignType = scanner.nextLine();
		Comparator<Flight> myComparator;
		if (assignType.equalsIgnoreCase("RBS")) {
			myComparator = new FlightDateTimeFieldComparator(Flight.origETAFieldID);
		} else if (assignType.equalsIgnoreCase("RBD")) {
			myComparator = new FlightDurationFieldComparator(Flight.flightTimeID).reversed();
		} else {
			throw new Exception("Invalid argument for assignment type: " + assignType);
		}
		int minsPerPeriod = Integer.parseInt(scanner.nextLine().trim());
		int gdpDuration = Integer.parseInt(scanner.nextLine().trim());

		HofkinModel myModel = new HofkinModel(groundCost, airCost);

		DefaultDiscretePAARChooserWrapper myPAARChooser = new DefaultDiscretePAARChooserWrapper(
				Duration.standardMinutes(minsPerPeriod), myModel);
		FunctionEx<DefaultState,GdpAction,Exception> myTmiChooser = new IndParamPlanner(myPAARChooser,
				new FixedDurationGDPIntervalChooser(Duration.standardMinutes(gdpDuration)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		return new StandardTmiPlanner(myTmiChooser, myComparator);
	}

	public static StandardTmiPlanner parseROPlanner(File input) throws Exception {
		return parseROPlanner(new Scanner(input));
	}

	public static StandardTmiPlanner parseROPlanner(Scanner scanner) throws Exception {
		// Find first non-empty line
		String input = "";
		while (scanner.hasNext() && input.isEmpty()) {
			input = scanner.nextLine();
		}

		if (!input.equalsIgnoreCase("START")) {
			throw new Exception("Start of ground costs not found. ");
		}

		boolean done = false;
		SortedMap<Double, Double> costMap = new TreeMap<Double, Double>();
		while (done == false && scanner.hasNextLine()) {
			input = scanner.nextLine();
			if (input.equalsIgnoreCase("END")) {
				done = true;
			} else {
				String[] splitStr = input.split(",");
				double minutesDelay = Double.parseDouble(splitStr[0].trim());
				double cost = Double.parseDouble(splitStr[1].trim());
				costMap.put(minutesDelay, cost);
			}
		}
		if (!costMap.containsKey(0.0)) {
			throw new Exception("Cost function must be defined at 0");
		}
		double airCost = Double.parseDouble(scanner.nextLine().trim());
		String assignType = scanner.nextLine();
		Comparator<Flight> myComparator;
		if (assignType.equalsIgnoreCase("RBS")) {
			myComparator = new FlightDateTimeFieldComparator(Flight.origETAFieldID);
		} else if (assignType.equalsIgnoreCase("RBD")) {
			myComparator = new FlightDurationFieldComparator(Flight.flightTimeID).reversed();
		} else {
			throw new Exception("Invalid argument for assignment type: " + assignType);
		}
		int minsPerPeriod = Integer.parseInt(scanner.nextLine().trim());
		int gdpDuration = Integer.parseInt(scanner.nextLine().trim());

		RichettaOdoniModel myModel = new RichettaOdoniModel(new PiecewiseLinearFunction(costMap, 0.0), airCost);
		DefaultDiscretePAARChooserWrapper myPAARChooser = new DefaultDiscretePAARChooserWrapper(
				Duration.standardMinutes(minsPerPeriod), myModel);
		FunctionEx<DefaultState, GdpAction, Exception> myTmiChooser = new IndParamPlanner(myPAARChooser,
				new FixedDurationGDPIntervalChooser(Duration.standardMinutes(gdpDuration)),
				new ConstantRadiusChooser(Double.POSITIVE_INFINITY));
		return new StandardTmiPlanner(myTmiChooser, myComparator);
	}
}
