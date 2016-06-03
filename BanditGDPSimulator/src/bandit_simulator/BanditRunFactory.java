package bandit_simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import bandit_objects.SimpleTmiAction;
import bandit_solvers.UniRandomTmiGenerator;
import function_util.BiFunctionEx;
import state_factories.CapacityScenarioFactory;
import state_factories.FlightFactory;
import state_factories.FlightStateFactory;
import state_representation.AirportState;
import state_representation.CapacityScenario;
import state_representation.CapacityScenarioState;
import state_representation.DefaultState;
import state_representation.FlightState;
import state_update.FlightHandler;

public final class BanditRunFactory {
	public final static int MAXIMUM_CAPACITY = 50;
	public final static int NO_TMI = 0;
	public final static int RAND_TMI = 1;
	public final static int HIST_TMI = 2;

	private BanditRunFactory() {

	}

	public static Generator<SimpleTmiAction> makeDefaultEWRGdpGenerator() {
		IntegerDistribution startTimeDistribution = new UniformIntegerDistribution(157, 780);
		IntegerDistribution durationDistribution = new UniformIntegerDistribution(239, 989);
		IntegerDistribution scopeDistribution = new UniformIntegerDistribution(800, 3000);
		IntegerDistribution rateDistribution = new UniformIntegerDistribution(16, 50);
		return new UniRandomTmiGenerator(scopeDistribution, startTimeDistribution, durationDistribution,
				rateDistribution);
	}

	public static Map<LocalDate, Map<LocalDate, Double>> parseDistanceFile(File file) throws IOException {
		Reader in = new FileReader(file);
		CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
		List<CSVRecord> records = parser.getRecords();
		Iterator<CSVRecord> myRecordsIter = records.iterator();
		CSVRecord header = myRecordsIter.next();
		int numDays = header.size();
		Map<LocalDate, Map<LocalDate, Double>> distances = new HashMap<LocalDate, Map<LocalDate, Double>>();
		for (CSVRecord myRecord = myRecordsIter.next(); myRecordsIter.hasNext(); myRecord = myRecordsIter.next()) {
			String[] yearMonthDay0 = myRecord.get(0).split("-");
			int year0 = Integer.parseInt(yearMonthDay0[0]);
			int month0 = Integer.parseInt(yearMonthDay0[1]);
			int day0 = Integer.parseInt(yearMonthDay0[2]);
			LocalDate myDate0 = new LocalDate(year0, month0, day0);
			Map<LocalDate, Double> distancesThisRow = new HashMap<LocalDate, Double>();
			for (int i = 1; i < numDays; i++) {
				String[] yearMonthDay1 = header.get(i).split("-");
				int year1 = Integer.parseInt(yearMonthDay1[0]);
				int month1 = Integer.parseInt(yearMonthDay1[1]);
				int day1 = Integer.parseInt(yearMonthDay1[2]);
				LocalDate myDate1 = new LocalDate(year1, month1, day1);
				double distance = Double.parseDouble(myRecord.get(i));
				distancesThisRow.put(myDate1, distance);
			}
			distances.put(myDate0, distancesThisRow);
		}
		parser.close();
		return distances;
	}

	public static Map<LocalDate, Map<Integer, IntegerDistribution>> parseCapacityFile(File file) throws IOException {
		Reader in = new FileReader(file);
		CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withHeader());
		Map<LocalDate, Map<Integer, IntegerDistribution>> myDayCapacityDistributions = new HashMap<LocalDate, Map<Integer, IntegerDistribution>>();
		int[] integer_values = new int[MAXIMUM_CAPACITY + 1];
		for (int i = 0; i <= MAXIMUM_CAPACITY; i++) {
			integer_values[i] = i;
		}
		for (CSVRecord record : parser) {
			int year = Integer.parseInt(record.get("Year"));
			int month = Integer.parseInt(record.get("Month"));
			int day = Integer.parseInt(record.get("Day"));
			LocalDate myDate = new LocalDate(year, month, day);
			if (!myDayCapacityDistributions.containsKey(myDate)) {
				myDayCapacityDistributions.put(myDate, new HashMap<Integer, IntegerDistribution>());
			}
			int hour = Integer.parseInt(record.get("Local.Hour"));

			double[] probability = new double[MAXIMUM_CAPACITY + 1];
			double previousRisk = 1.0;
			for (int i = 1; i <= MAXIMUM_CAPACITY; i++) {
				double nextRisk = Double.parseDouble(record.get("V" + i));
				probability[i] = (previousRisk - nextRisk);
				previousRisk = nextRisk;
			}
			myDayCapacityDistributions.get(myDate).put(hour,
					new EnumeratedIntegerDistribution(integer_values, probability));
		}
		parser.close();
		return myDayCapacityDistributions;
	}

	public static Map<LocalDate, SimpleTmiAction> parseTmiFile(File file, boolean gsValid) throws IOException {
		Reader in = new FileReader(file);
		CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withHeader());
		Map<LocalDate, SimpleTmiAction> myTmiActions = new HashMap<LocalDate, SimpleTmiAction>();
		for (CSVRecord record : parser) {
			String dateString = record.get("BASE_DAY");
			String[] dateFields = dateString.split("-");
			int year = Integer.parseInt(dateFields[0]);
			int month = Integer.parseInt(dateFields[1]);
			int day = Integer.parseInt(dateFields[2]);
			LocalDate myDate = new LocalDate(year, month, day);

			boolean valid = Integer.parseInt(record.get("VALID_R")) == 1
					&& Integer.parseInt(record.get("VALID_S")) == 1;
			if (valid) {
				String tmiType = record.get("TMI_TYPE").trim();
				Double scope = (double) Integer.parseInt(record.get("RADIUS"));
				Double startTimeMinute = (double) Integer.parseInt(record.get("ETASTART_MIN"));
				Double duration = (double) Integer.parseInt(record.get("ETA_DUR"));
				if (tmiType.equals("GDP")) {
					Double rate = (double) Integer.parseInt(record.get("RATE"));
					myTmiActions.put(myDate, new SimpleTmiAction(rate, startTimeMinute, duration, scope));
				} else if (gsValid) {
					myTmiActions.put(myDate, new SimpleTmiAction(startTimeMinute, duration, scope));

				} else {
					myTmiActions.put(myDate, new SimpleTmiAction(false));
				}
			} else {
				myTmiActions.put(myDate, new SimpleTmiAction(false));
			}
		}
		parser.close();
		return myTmiActions;
	}

	public static <T> ImmutablePair<List<T>, List<T>> randomSample(Collection<T> objects, int size1, int size2) {
		Random random = new Random();
		List<T> myList = new ArrayList<T>(objects);
		List<T> myFirstObjects = new ArrayList<T>();
		List<T> mySecondObjects = new ArrayList<T>();
		int numObjects = objects.size();
		System.out.println(numObjects);
		System.out.println(size1 + size2);
		for (int i = 0; i < size1 + size2; i++) {
			int nextInt = random.nextInt(numObjects - i);
			T nextObject = myList.get(nextInt);
			T lastObject = myList.get(numObjects - i - 1);
			myList.set(nextInt, lastObject);
			if (i < size1) {
				myFirstObjects.add(nextObject);
			} else {
				mySecondObjects.add(nextObject);
			}
		}
		return new ImmutablePair<List<T>, List<T>>(myFirstObjects, mySecondObjects);
	}

	public static <T> List<T> shuffle(Collection<T> objects) {
		Random random = new Random();
		List<T> myList = new ArrayList<T>(objects);
		int numObjects = objects.size();
		for (int i = 0; i < numObjects; i++) {
			int nextInt = random.nextInt(numObjects - i);
			T nextObject = myList.get(nextInt);
			T lastObject = myList.get(numObjects - i - 1);
			myList.set(nextInt, lastObject);
			myList.set(numObjects - i - 1, nextObject);
		}
		return myList;
	}

	public static Set<LocalDate> getValidDates(Map<LocalDate, SimpleTmiAction> myTmiActions,
			Set<LocalDate> availableDates) {
		Set<LocalDate> invalidDates = new TreeSet<LocalDate>();
		for (Map.Entry<LocalDate, SimpleTmiAction> entry : myTmiActions.entrySet()) {
			SimpleTmiAction action = entry.getValue();
			LocalDate date = entry.getKey();
			if (action.getType() == SimpleTmiAction.INVALID_TYPE) {
				invalidDates.add(date);
			}
		}
		// Get the days that will be used
		Set<LocalDate> myDates = new HashSet<LocalDate>(availableDates);
		myDates.removeAll(invalidDates);
		return myDates;
	}

	public static void makeNoOutcomeDayPools(File capacityFile, File tmiFile, File distanceFile, File trainTmiFile,
			File testTmiFile, File trainNoTmiFile, File testNoTmiFile, String adlDirName, String adlFilePrefix,
			DateTimeZone timeZone, int startHour, int runHours, FlightHandler myFlightHandler) throws IOException {
		System.out.println("Reading capacity file");
		Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacityDistributions = BanditRunFactory
				.parseCapacityFile(capacityFile);
		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		// Read TMIs for each day
		System.out.println("Reading tmi file.");
		Map<LocalDate, SimpleTmiAction> myTmiActions = BanditRunFactory.parseTmiFile(tmiFile, true);
		System.out.println("Total num TMIs: " + myTmiActions.keySet().size());
		// Convert capacities into states
		System.out.println("Converting capacities into states.");
		Map<LocalDate, DefaultState> myStates = new HashMap<LocalDate, DefaultState>();
		for (Map.Entry<LocalDate, Map<Integer, IntegerDistribution>> entry : myCapacityDistributions.entrySet()) {
			try {
				DefaultState myState = makeStateFromCapacities(entry.getKey(), entry.getValue(), startHour, runHours,
						adlDirName, adlFilePrefix, myFlightHandler, timeZone);
				myStates.put(entry.getKey(), myState);
			} catch (Exception e) {
				System.out.println("Unable to make state: " + entry.getKey());
				System.out.println(e);
			}
		}

		Set<LocalDate> allDates = getValidDates(myTmiActions, myStates.keySet());
		allDates.retainAll(myDistances.keySet());

		// Find out which valid dates had tmis
		Set<LocalDate> tmiDates = new HashSet<LocalDate>(myTmiActions.keySet());
		tmiDates.retainAll(allDates);
		int numTmis = tmiDates.size();
		System.out.println("Num with tmis: " + numTmis);

		Set<LocalDate> noTmiDates = new HashSet<LocalDate>(allDates);
		noTmiDates.removeAll(tmiDates);
		int numNoTmis = noTmiDates.size();
		System.out.println("Num without tmis: " + numNoTmis);

		// Separate the tmi dates into a train set and a test set.
		int numTrainTmi = numTmis / 2;
		ImmutablePair<List<LocalDate>, List<LocalDate>> separatedTmiDays = BanditRunFactory.randomSample(tmiDates,
				numTrainTmi, numTmis - numTrainTmi);
		Set<LocalDate> trainTmiDays = new HashSet<LocalDate>(separatedTmiDays.getLeft());
		Set<LocalDate> testTmiDays = new HashSet<LocalDate>(separatedTmiDays.getRight());

		int numTrainNoTmi = numNoTmis / 2;
		ImmutablePair<List<LocalDate>, List<LocalDate>> separatedNoTmiDays = BanditRunFactory.randomSample(noTmiDates,
				numTrainNoTmi, numNoTmis - numTrainNoTmi);
		Set<LocalDate> trainNoTmiDays = new HashSet<LocalDate>(separatedNoTmiDays.getLeft());
		Set<LocalDate> testNoTmiDays = new HashSet<LocalDate>(separatedNoTmiDays.getRight());

		Map<LocalDate, DefaultState> trainTmiStates = subMap(myStates, trainTmiDays);
		Map<LocalDate, DefaultState> testTmiStates = subMap(myStates, testTmiDays);
		Map<LocalDate, DefaultState> trainNoTmiStates = subMap(myStates, trainNoTmiDays);
		Map<LocalDate, DefaultState> testNoTmiStates = subMap(myStates, testNoTmiDays);
		writeObjectToFile(trainTmiStates, trainTmiFile);
		writeObjectToFile(testTmiStates, testTmiFile);
		writeObjectToFile(trainNoTmiStates, trainNoTmiFile);
		writeObjectToFile(testNoTmiStates, testNoTmiFile);
	}

	public static <S, T> Map<S, T> subMap(Map<S, T> myMap, Set<S> mySubset) {
		Map<S, T> mySubMap = new HashMap<S, T>();
		for (S item : mySubset) {
			mySubMap.put(item, myMap.get(item));
		}
		return mySubMap;
	}

	public static void addHistoricalTmiOutcomesToPool(File tmiFile, boolean gsValid, File distanceFile, File stateFile,
			File outFile, BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner) throws Exception {
		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		// Read TMIs for each day
		System.out.println("Reading tmi file.");
		Map<LocalDate, SimpleTmiAction> myTmiActions = BanditRunFactory.parseTmiFile(tmiFile, gsValid);

		// Convert capacities into states
		System.out.println("Reading states from file.");
		@SuppressWarnings("unchecked")
		Map<LocalDate, DefaultState> myStates = (Map<LocalDate, DefaultState>) readObjectFromFile(stateFile);
		
		Set<LocalDate> availableDates = new HashSet<LocalDate>(myStates.keySet());
		availableDates.retainAll(myDistances.keySet());
		
		// Find out which dates are valid
		Set<LocalDate> validDates = getValidDates(myTmiActions, availableDates);
		System.out.println(validDates.size());
		
		System.out.println("Generating outcomes");
		writeObjectToFile(makeOutcomes(validDates, myStates, myTmiActions, myRunner), outFile);
	}

	public static void addGeneratedTmiOutcomesToPool(Generator<SimpleTmiAction> myGenerator, File distanceFile,
			File stateFile, File outFile, BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner)
					throws Exception {
		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		// Convert capacities into states
		System.out.println("Reading states from file.");
		@SuppressWarnings("unchecked")
		Map<LocalDate, DefaultState> myStates = (Map<LocalDate, DefaultState>) readObjectFromFile(stateFile);
		Set<LocalDate> myValidDates = myDistances.keySet();
		myValidDates.retainAll(myStates.keySet());

		Map<LocalDate, SimpleTmiAction> myTmiActions = new HashMap<LocalDate, SimpleTmiAction>();
		for (LocalDate d : myValidDates) {
			myTmiActions.put(d, myGenerator.generate());
		}

		writeObjectToFile(makeOutcomes(myStates.keySet(), myStates, myTmiActions, myRunner), outFile);
	}

	public static void writeObjectToFile(Object object, File file) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(object);
		out.close();
		fileOut.close();
	}

	public static Object readObjectFromFile(File file) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		Object myInstances = objectIn.readObject();
		fileIn.close();
		objectIn.close();
		return myInstances;
	}

	public static Map<LocalDate, ImmutableTriple<DefaultState, SimpleTmiAction, Double>> makeOutcomes(
			Set<LocalDate> dates, Map<LocalDate, DefaultState> myStates, Map<LocalDate, SimpleTmiAction> myTmiActions,
			BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner) throws Exception {
		Map<LocalDate, ImmutableTriple<DefaultState, SimpleTmiAction, Double>> outcomeMap = new HashMap<LocalDate, ImmutableTriple<DefaultState, SimpleTmiAction, Double>>();
		for (Map.Entry<LocalDate, DefaultState> entry : myStates.entrySet()) {
			LocalDate date = entry.getKey();
			if (dates.contains(date)) {
				SimpleTmiAction myAction = new SimpleTmiAction();
				if (myTmiActions.containsKey(date)) {
					myAction = myTmiActions.get(date);
				}
				DefaultState myInitialState = entry.getValue();
				double outcome = myRunner.apply(myInitialState, myAction);
				outcomeMap.put(date, ImmutableTriple.of(myInitialState, myAction, outcome));
			}
		}
		return outcomeMap;
	}
	// Set<LocalDate> noTmiDates = myDistances.keySet().removeAll(tmiDates);

	public static double distanceToSimilarity(double distance, double bandwidth) {
		return Math.exp(-Math.pow(distance / bandwidth, 2.0) / 2);
	}

	public static DefaultState makeStateFromCapacities(LocalDate date, Map<Integer, IntegerDistribution> hourMap,
			int startHour, int runHours, String adlDirName, String adlFilePrefix, FlightHandler myFlightHandler,
			DateTimeZone myTimeZone) throws Exception {
		// Generate the start time
		DateTime startTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(),
				startHour, 0, myTimeZone);
		Interval runInterval = new Interval(startTime, startTime.plus(Duration.standardHours(runHours)));

		// Create the capacity distribution
		SortedMap<DateTime, Integer> capacityMap = new TreeMap<DateTime, Integer>();
		for (Integer hour : hourMap.keySet()) {
			DateTime currentTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(),
					hour, 0, myTimeZone);
			capacityMap.put(currentTime, hourMap.get(hour).sample());
		}
		CapacityScenarioState myCapacities = CapacityScenarioFactory
				.parseScenario(new CapacityScenario(1.0, capacityMap), startTime);

		// Create the initial runway state
		AirportState myAirportState = new AirportState(startTime);

		// Read the flights
		FlightState myFlightState = FlightStateFactory.parseAdlFlightFile(adlDirName, adlFilePrefix, date, runInterval,
				myTimeZone, FlightFactory.ADL_FORMAT_ID);
		FlightStateFactory.delaySittingFlights(myFlightHandler, myFlightState);

		// Combine everything
		return new DefaultState(startTime, myFlightState, myAirportState, myCapacities);
	}

	public static List<NasBanditInstance> createInstancesFromPool(File distanceFile, File tmiPoolFile,File noTmiPoolFile,
			int numInstance, int numTmi, int numNoTmi, int numHistory, int numRun, double bandwidth)
					throws IOException, ClassNotFoundException {
		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		@SuppressWarnings("unchecked")
		Map<LocalDate, ImmutableTriple<DefaultState,SimpleTmiAction, Double>> myTmiPool = (Map<LocalDate, ImmutableTriple<DefaultState,SimpleTmiAction, Double>>) readObjectFromFile(
				tmiPoolFile);
		System.out.println(myTmiPool.keySet().size());
		@SuppressWarnings("unchecked")
		Map<LocalDate, ImmutableTriple<DefaultState,SimpleTmiAction, Double>> noTmiPool = (Map<LocalDate, ImmutableTriple<DefaultState,SimpleTmiAction, Double>>) readObjectFromFile(
				noTmiPoolFile);
		Map<LocalDate, ImmutableTriple<DefaultState,SimpleTmiAction, Double>> allPool = new HashMap<LocalDate, ImmutableTriple<DefaultState,SimpleTmiAction, Double>>(myTmiPool);
		allPool.putAll(noTmiPool);

		List<NasBanditInstance> myInstances = new LinkedList<NasBanditInstance>();
		for (int k = 0; k < numInstance; k++) {
			List<LocalDate> allDates = randomSample(myTmiPool.keySet(), numTmi, 0).getLeft();
			allDates.addAll(randomSample(noTmiPool.keySet(), numNoTmi, 0).getLeft());
			allDates = shuffle(allDates);
			allDates = allDates.subList(0, numHistory + numRun);

			// Make lists to store everything.
			List<RealVector> unseenSimilarities = new ArrayList<RealVector>();
			List<Double> unseenBaseOutcomes = new ArrayList<Double>();

			List<RealVector> similarities = new ArrayList<RealVector>();
			List<DefaultState> unseenStates = new ArrayList<DefaultState>();
			List<SimpleTmiAction> myTakenActions = new ArrayList<SimpleTmiAction>();
			List<Double> myRewards = new ArrayList<Double>();
			
			Iterator<LocalDate> myStateIterator = allDates.iterator();
			for (int i = 0; i < numHistory + numRun; i++) {
				// Get the initial state of NAS
				LocalDate initialDate = myStateIterator.next();
				// Get the similarity vector
				RealVector myContext = new ArrayRealVector(i);
				Iterator<LocalDate> oldDateIter = allDates.iterator();
				Map<LocalDate, Double> myDistanceRow = myDistances.get(initialDate);
				for (int j = 0; j < i; j++) {
					LocalDate otherDate =oldDateIter.next();
					double similarity = distanceToSimilarity(myDistanceRow.get(otherDate), bandwidth);
					myContext.setEntry(j, similarity);
				}

				ImmutableTriple<DefaultState,SimpleTmiAction, Double> myActionOutcomePair = allPool.get(initialDate);
				if (i < numHistory) {
					similarities.add(myContext);
					myTakenActions.add(myActionOutcomePair.getMiddle());
					myRewards.add(myActionOutcomePair.getRight());
				} else {
					unseenSimilarities.add(myContext);
					unseenStates.add(myActionOutcomePair.getLeft());
					unseenBaseOutcomes.add(myActionOutcomePair.getRight());
				}
			}

			// Construct the set of historical outcomes
			NasBanditOutcome historicalOutcomes = new NasBanditOutcome(similarities,
					myTakenActions, myRewards);
			myInstances.add(new NasBanditInstance(unseenStates,
					unseenSimilarities, unseenBaseOutcomes, historicalOutcomes));
		}

		return myInstances;
	}
}
