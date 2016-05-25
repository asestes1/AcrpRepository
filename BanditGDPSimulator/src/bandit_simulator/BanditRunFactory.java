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
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import bandit_objects.SimpleTmiAction;
import function_util.BiFunctionEx;
import model.Pair;
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

	private BanditRunFactory() {

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

	public static Map<LocalDate, SimpleTmiAction> parseTmiFile(File file) throws IOException {
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
				} else {
					myTmiActions.put(myDate, new SimpleTmiAction(startTimeMinute, duration, scope));
				}
			} else {
				myTmiActions.put(myDate, new SimpleTmiAction(false));
			}
		}
		parser.close();
		return myTmiActions;
	}

	public static <T> Pair<List<T>, List<T>> randomSample(Collection<T> objects, int size1, int size2) {
		Random random = new Random();
		List<T> myList = new ArrayList<T>(objects);
		List<T> myFirstObjects = new ArrayList<T>();
		List<T> mySecondObjects = new ArrayList<T>();
		int numObjects = objects.size();
		for (int i = 0; i < size1 + size2; i++) {
			int nextInt = random.nextInt(numObjects - i);
			T nextObject = myList.get(nextInt);
			T lastObject = myList.get(size1 + size2 - i - 1);
			myList.set(nextInt, lastObject);
			if (i < size1) {
				myFirstObjects.add(nextObject);
			} else {
				mySecondObjects.add(nextObject);
			}
		}
		return new Pair<List<T>, List<T>>(myFirstObjects, mySecondObjects);
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
			Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacityDistributions,
			Map<LocalDate, Map<LocalDate, Double>> myDistances) {
		Set<LocalDate> invalidDates = new TreeSet<LocalDate>();
		for (Map.Entry<LocalDate, SimpleTmiAction> entry : myTmiActions.entrySet()) {
			SimpleTmiAction action = entry.getValue();
			LocalDate date = entry.getKey();
			if (action.getType() == SimpleTmiAction.INVALID_TYPE) {
				invalidDates.add(date);
			}
		}
		// Get the days that will be used
		Set<LocalDate> myDates = myCapacityDistributions.keySet();
		myDates.retainAll(myDistances.keySet());
		myDates.removeAll(invalidDates);
		return myDates;
	}

	public static void makeDayPool(File capacityFile, File tmiFile, File distanceFile, File trainTmiFile,
			File testTmiFile, File trainNoTmiFile, File testNoTmiFile, String btsDirName, String btsFilePrefix,
			DateTimeZone timeZone,
			int startHour, int numTmi, int numNoTmi, FlightHandler myFlightHandler,
			BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner) throws Exception {
		// Read capacity distributions for each day
		System.out.println("Reading capacity file");
		Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacityDistributions = BanditRunFactory
				.parseCapacityFile(capacityFile);

		// Read TMIs for each day
		System.out.println("Reading tmi file.");
		Map<LocalDate, SimpleTmiAction> myTmiActions = BanditRunFactory.parseTmiFile(tmiFile);

		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		// Convert capacities into states
		System.out.println("Converting capacities into states.");
		Map<LocalDate, DefaultState> myStates = new HashMap<LocalDate, DefaultState>();
		for (Map.Entry<LocalDate, Map<Integer, IntegerDistribution>> entry : myCapacityDistributions.entrySet()) {
			DefaultState myState = makeStateFromCapacities(entry.getKey(), entry.getValue(), startHour, btsDirName,
					btsFilePrefix, myFlightHandler,timeZone);
			myStates.put(entry.getKey(), myState);
		}

		// Find out which dates are valid
		Set<LocalDate> validDates = getValidDates(myTmiActions, myCapacityDistributions, myDistances);

		// Find out which valid dates had tmis
		Set<LocalDate> tmiDates = new HashSet<LocalDate>(myTmiActions.keySet());
		tmiDates.retainAll(validDates);

		// Separate the tmi dates into a train set and a test set.
		Set<LocalDate> trainTmiDays = new HashSet<LocalDate>(
				BanditRunFactory.randomSample(tmiDates, numTmi, 0).getItemA());
		Set<LocalDate> testTmiDays = new HashSet<LocalDate>(tmiDates);
		testTmiDays.removeAll(trainTmiDays);

		System.out.println("Generating outcomes for TMI training set");
		writeOutcomesToFile(makeOutcomes(trainTmiDays, myStates, myTmiActions, myRunner), trainTmiFile);
		System.out.println("Generating outcomes for TMI test set");
		writeOutcomesToFile(makeOutcomes(testTmiDays, myStates, myTmiActions, myRunner), testTmiFile);

		// Separate the non-tmi dates into a train set and a test set.
		Set<LocalDate> noTmiDates = new HashSet<LocalDate>(myCapacityDistributions.keySet());
		noTmiDates.removeAll(myTmiActions.keySet());

		Set<LocalDate> trainNoTmiDays = new HashSet<LocalDate>(
				BanditRunFactory.randomSample(noTmiDates, numNoTmi, 0).getItemA());

		Set<LocalDate> testNoTmiDays = new HashSet<LocalDate>(noTmiDates);
		testTmiDays.removeAll(trainTmiDays);

		System.out.println("Generating outcomes for no TMI training set");
		writeOutcomesToFile(makeOutcomes(trainNoTmiDays, myStates, myTmiActions, myRunner), trainNoTmiFile);
		System.out.println("Generating outcomes for no TMI test set");
		writeOutcomesToFile(makeOutcomes(testNoTmiDays, myStates, myTmiActions, myRunner), testNoTmiFile);
	}

	public static void writeOutcomesToFile(Map<DefaultState, Pair<SimpleTmiAction, Double>> outcomes, File file)
			throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(outcomes);
		out.close();
		fileOut.close();
	}

	public static Map<DefaultState, Pair<SimpleTmiAction, Double>> readOutcomesFromFile(File file)
			throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		@SuppressWarnings("unchecked")
		Map<DefaultState, Pair<SimpleTmiAction, Double>> myInstances = (Map<DefaultState, Pair<SimpleTmiAction, Double>>) objectIn
				.readObject();
		fileIn.close();
		objectIn.close();
		return myInstances;
	}

	public static Map<DefaultState, Pair<SimpleTmiAction, Double>> makeOutcomes(Set<LocalDate> dates,
			Map<LocalDate, DefaultState> myStates, Map<LocalDate, SimpleTmiAction> myTmiActions,
			BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner) throws Exception {
		Map<DefaultState, Pair<SimpleTmiAction, Double>> outcomeMap = new HashMap<DefaultState, Pair<SimpleTmiAction, Double>>();
		for (Map.Entry<LocalDate, DefaultState> entry : myStates.entrySet()) {
			LocalDate date = entry.getKey();
			if (dates.contains(date)) {
				SimpleTmiAction myAction = new SimpleTmiAction();
				if (myTmiActions.containsKey(date)) {
					myAction = myTmiActions.get(date);
				}
				DefaultState myInitialState = entry.getValue();
				double outcome = myRunner.apply(myInitialState, myAction);
				outcomeMap.put(myInitialState, new Pair<SimpleTmiAction, Double>(myAction, outcome));
			}
		}
		return outcomeMap;
	}
	// Set<LocalDate> noTmiDates = myDistances.keySet().removeAll(tmiDates);

	public static double distanceToSimilarity(double distance, double bandwidth){
		return Math.exp(-Math.pow(distance/bandwidth,2.0)/2);
	}
	
	public static DefaultState makeStateFromCapacities(LocalDate date, Map<Integer, IntegerDistribution> hourMap,
			int startHour, String adlDirName, String adlFilePrefix, FlightHandler myFlightHandler, DateTimeZone myTimeZone) throws Exception {
		// Generate the start time
		DateTime startTime = new DateTime(date.year().get(), date.monthOfYear().get(), date.dayOfMonth().get(),
				startHour, 0,myTimeZone);

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
		FlightState myFlightState = FlightStateFactory.parseAdlFlightFile(adlDirName, adlFilePrefix, date, startTime,myTimeZone,
				FlightFactory.BTS_FORMAT_ID);
		FlightStateFactory.delaySittingFlights(myFlightHandler, myFlightState);

		// Combine everything
		return new DefaultState(startTime, myFlightState, myAirportState, myCapacities);
	}

	public static List<NasBanditInstance> createInstancesFromPool(File distanceFile, File tmiPoolFile, File noTmiPoolFile,
			int numInstance, int numTmi, int numNoTmi, int numHistory, int numRun, double bandwidth)
					throws IOException, ClassNotFoundException {
		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		Map<DefaultState, Pair<SimpleTmiAction, Double>> myTmiPool = readOutcomesFromFile(tmiPoolFile);
		Map<DefaultState, Pair<SimpleTmiAction, Double>> myNoTmiPool = readOutcomesFromFile(noTmiPoolFile);

		List<NasBanditInstance> myInstances = new LinkedList<NasBanditInstance>();
		for (int k = 0; k < numInstance; k++) {
			List<DefaultState> allStates = randomSample(myTmiPool.keySet(), numTmi, 0).getItemA();
			allStates.addAll(randomSample(myNoTmiPool.keySet(), numNoTmi, 0).getItemA());
			allStates = shuffle(allStates);
			allStates = allStates.subList(0, numHistory + numRun);

			// Make lists to store everything.
			List<RealVector> unseenSimilarities = new ArrayList<RealVector>();
			List<Double> unseenBaseOutcomes = new ArrayList<Double>();

			List<RealVector> similarities = new ArrayList<RealVector>();
			List<SimpleTmiAction> myTakenActions = new ArrayList<SimpleTmiAction>();
			List<Double> myRewards = new ArrayList<Double>();
			int iterationNum = 0;
			Iterator<DefaultState> myStateIterator = allStates.iterator();
			for (int i = 0; i < numHistory + numRun; i++) {
				// Get the initial state of NAS
				DefaultState myInitialState = myStateIterator.next();
				DateTime startTime = myInitialState.getCurrentTime();
				LocalDate initialDate = new LocalDate(startTime.getYear(), startTime.getMonthOfYear(),
						startTime.getDayOfMonth());
				// Get the similarity vector
				RealVector myContext = new ArrayRealVector(i);
				Iterator<DefaultState> oldStateIter = allStates.iterator();
				Map<LocalDate, Double> myDistanceRow = myDistances.get(initialDate);
				for (int j = 0; j < iterationNum; j++) {
					DateTime otherStartTime = oldStateIter.next().getCurrentTime();
					LocalDate otherDate = new LocalDate(otherStartTime.getYear(), otherStartTime.getMonthOfYear(),
							otherStartTime.getDayOfMonth());
					
					double similarity = distanceToSimilarity(myDistanceRow.get(otherDate), bandwidth);
					myContext.setEntry(j, similarity);
				}
				if (i < numHistory) {
					similarities.add(myContext);
				} else {
					unseenSimilarities.add(myContext);
				}

				Pair<SimpleTmiAction, Double> myActionOutcomePair;
				if (myTmiPool.containsKey(myInitialState)) {
					myActionOutcomePair = myTmiPool.get(myInitialState);

				} else {
					myActionOutcomePair = myNoTmiPool.get(myInitialState);
				}

				if (i < numHistory) {
					myTakenActions.add(myActionOutcomePair.getItemA());
					myRewards.add(myActionOutcomePair.getItemB());
				} else {
					unseenBaseOutcomes.add(myActionOutcomePair.getItemB());
				}
			}

			// Construct the set of historical outcomes
			NasBanditOutcome historicalOutcomes = new NasBanditOutcome(allStates.subList(0, numHistory), similarities,
					myTakenActions, myRewards);
			myInstances.add(new NasBanditInstance(allStates.subList(numHistory, numHistory + numRun),
					unseenSimilarities, unseenBaseOutcomes, historicalOutcomes));
		}

		return myInstances;
	}

	public static List<NasBanditInstance> createInstances(File capacityFile, File tmiFile, File distanceFile,
			String btsDirName, String btsFilePrefix, DateTimeZone timeZone, int numInstances, int numHistory, int numRun, int startHour,
			FlightHandler myFlightHandler, BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner, double bandwidth)
					throws Exception {

		// Read capacity distributions for each day
		System.out.println("Reading capacity file");
		Map<LocalDate, Map<Integer, IntegerDistribution>> myCapacityDistributions = BanditRunFactory
				.parseCapacityFile(capacityFile);
		// Read TMIs for each day
		System.out.println("Reading tmi file.");
		Map<LocalDate, SimpleTmiAction> myTmiActions = BanditRunFactory.parseTmiFile(tmiFile);

		// Read capacity distances for each day
		System.out.println("Reading distance file.");
		Map<LocalDate, Map<LocalDate, Double>> myDistances = BanditRunFactory.parseDistanceFile(distanceFile);

		Set<LocalDate> myDates = getValidDates(myTmiActions, myCapacityDistributions, myDistances);
		for (LocalDate date : new HashSet<LocalDate>(myDates)) {
			if (!myTmiActions.containsKey(date)) {
				// myTmiActions.put(date, new SimpleTmiAction());
				myDates.remove(date);
			}
		}

		// Convert the capacity distributions into a set of initial states
		System.out.println("Generating states from capacities");
		Map<LocalDate, DefaultState> myInitialStates = new HashMap<LocalDate, DefaultState>();
		for (LocalDate date : myDates) {
			myInitialStates.put(date, makeStateFromCapacities(date, myCapacityDistributions.get(date), startHour,
					btsDirName, btsFilePrefix, myFlightHandler, timeZone));
		}

		// Choose the days that will form the history and unseen days for our
		// run
		List<NasBanditInstance> myInstances = new ArrayList<NasBanditInstance>();
		for (int i = 0; i < numInstances; i++) {
			System.out.println("Generating instance " + i);
			Pair<List<LocalDate>, List<LocalDate>> chosenDays = BanditRunFactory.randomSample(myDates, numHistory,
					numRun);
			List<LocalDate> historicalDays = chosenDays.getItemA();
			List<LocalDate> unseenDays = chosenDays.getItemB();

			// Make lists to store all the historical results.
			List<DefaultState> myInitialConditions = new ArrayList<DefaultState>();
			List<RealVector> similarities = new ArrayList<RealVector>();
			List<SimpleTmiAction> myTakenActions = new ArrayList<SimpleTmiAction>();
			List<Double> myRewards = new ArrayList<Double>();
			int iterationNum = 0;
			for (LocalDate date : chosenDays.getItemA()) {
				// Get the initial state of NAS
				DefaultState myInitialState = myInitialStates.get(date);
				myInitialConditions.add(myInitialState);

				// Get the similarity vector
				RealVector myContext = new ArrayRealVector(iterationNum);
				Iterator<LocalDate> oldDateIter = historicalDays.iterator();
				Map<LocalDate, Double> myDistanceRow = myDistances.get(date);
				for (int j = 0; j < iterationNum; j++) {
					double similarity = distanceToSimilarity(myDistanceRow.get(oldDateIter.next()), bandwidth);

					myContext.setEntry(j, similarity);
				}
				similarities.add(myContext);

				// Get the action that was taken
				SimpleTmiAction myAction = myTmiActions.get(date);
				myTakenActions.add(myAction);

				// Apply the action that was taken to get the reward
				double outcome = myRunner.apply(myInitialState, myAction);
				myRewards.add(outcome);
				iterationNum++;
			}

			// Construct the set of historical outcomes
			NasBanditOutcome historicalOutcomes = new NasBanditOutcome(myInitialConditions, similarities,
					myTakenActions, myRewards);

			// Get the states the similarity vectors for the unseen outcomes
			iterationNum = 0;

			List<DefaultState> unseenStates = new ArrayList<DefaultState>();
			List<RealVector> unseenSimilarities = new ArrayList<RealVector>();
			List<Double> unseenBaseOutcomes = new ArrayList<Double>();
			for (LocalDate date : unseenDays) {
				// Get the initial state of NAS
				unseenStates.add(myInitialStates.get(date));

				// Calculate the similarity vector
				Iterator<LocalDate> oldDateIter = historicalDays.iterator();
				Map<LocalDate, Double> myDistanceRow = myDistances.get(date);
				RealVector myContext = new ArrayRealVector(iterationNum + numHistory);
				for (int j = 0; j < numHistory; j++) {
					double distances = myDistanceRow.get(oldDateIter.next());
					myContext.setEntry(j, distances);
				}
				Iterator<LocalDate> unseenDateIter = unseenDays.iterator();
				for (int j = 0; j < iterationNum; j++) {
					double distances = myDistanceRow.get(unseenDateIter.next());
					myContext.setEntry(numHistory + j, distances);
				}
				unseenSimilarities.add(myContext);
				iterationNum++;

				Double myOutcome = myRunner.apply(myInitialStates.get(date), myTmiActions.get(date));
				unseenBaseOutcomes.add(myOutcome);
			}
			myInstances.add(
					new NasBanditInstance(unseenStates, unseenSimilarities, unseenBaseOutcomes, historicalOutcomes));
		}
		return myInstances;
	}

	public static void serializeInstances(File instanceFile, File capacityFile, File tmiFile, File distanceFile,
			String btsDirName, String btsFilePrefix, DateTimeZone timeZone,int numInstances, int numHistory, int numRun, int startHour,
			FlightHandler myFlightHandler, BiFunctionEx<DefaultState, SimpleTmiAction, Double, Exception> myRunner,double bandwidth)
					throws Exception {

		List<NasBanditInstance> myInstances = BanditRunFactory.createInstances(capacityFile, tmiFile, distanceFile,
				btsDirName, btsFilePrefix,timeZone, numInstances, numHistory, numRun, startHour, myFlightHandler, myRunner,bandwidth);

		FileOutputStream fileOut = new FileOutputStream(instanceFile);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(myInstances);
		out.close();
		fileOut.close();
	}

	@SuppressWarnings("unchecked")
	public static List<NasBanditInstance> deserializeInstances(File instanceFile)
			throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(instanceFile);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		List<NasBanditInstance> myInstances = (List<NasBanditInstance>) objectIn.readObject();
		fileIn.close();
		objectIn.close();
		return myInstances;
	}
}
