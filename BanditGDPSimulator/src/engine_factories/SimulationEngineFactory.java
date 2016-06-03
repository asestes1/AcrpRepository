package engine_factories;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;

import gdp_factories.GdpPlannerFactory;
import model.SimulationEngineInstance;
import model.StateAction;
import state_criteria.AllLandedCriteria;
import state_criteria.AlwaysCriteria;
import state_criteria.AtStartCriteriaFactory;
import state_criteria.StateCriteria;
import state_factories.AirportStateFactory;
import state_factories.CapacityScenarioFactory;
import state_factories.DateTimeFactory;
import state_factories.FlightStateFactory;
import state_representation.AirportState;
import state_representation.CapacityScenarioState;
import state_representation.DefaultState;
import state_representation.FlightState;
import state_update.FlightHandler;
import state_update_factories.DefaultNasUpdateFactory;
import state_update_factories.FlightHandlerFactory;

public final class SimulationEngineFactory {
	// Constants for types of flight files.
	public static final int FULL_CAPACITY = 0;
	public static final int BTS_FLIGHT = 1;
	public static final int BASIC_FLIGHT = 2;

	// Constants for types of scenario files.
	public static final int BASIC_SCENARIO = 3;
	public static final int LO_TO_HIGH_SCENARIO = 4;

	// Constants for solver modules
	public static final int DIRECT_HOFKIN = 5;
	public static final int DIRECT_EXTENDED_HOFKIN = 6;
	public static final int DIRECT_MUKHERJEE_HANSEN = 7;
	public static final int PAAR_HOFKIN = 8;
	public static final int PAAR_RO = 9;

	private SimulationEngineFactory() {

	}

	public static SimulationEngineInstance<DefaultState> makeSimulationInstance(File startTimeFile,
			File flightHandlerFile, int flightFileType, File flightFile, int capacityFileType, File capacityFile,
			File airportFile, File updateFile, DateTimeZone timeZone) throws Exception {
		DateTime startTime = DateTimeFactory.parse(startTimeFile,timeZone);
		Interval myInterval = new Interval(startTime,startTime.plus(Duration.standardDays(1)));
		FlightHandler flightHandler = FlightHandlerFactory.parseFlightHandler(flightHandlerFile);

		FlightState flightState = null;
		if (flightFileType == FULL_CAPACITY) {
			flightState = FlightStateFactory.parseFullCapacityState(flightFile, startTime);
		} else if (flightFileType == BTS_FLIGHT || flightFileType == BASIC_FLIGHT) {
			flightState = FlightStateFactory.parseFlightState(flightFile, myInterval, timeZone,flightFileType);
		} else {
			throw new IllegalArgumentException(
					"The value " + flightFileType + " is not a valid value for the type of flight file.");
		}

		CapacityScenarioState capacityState = null;
		if (capacityFileType == BASIC_SCENARIO) {
			capacityState = CapacityScenarioFactory.parseBasicState(capacityFile);
		} else if (capacityFileType == LO_TO_HIGH_SCENARIO) {
			capacityState = CapacityScenarioFactory.parseLoToHigh(capacityFile, startTime);
		}

		AirportState airportState = AirportStateFactory.parseAirportState(airportFile, startTime);
		DefaultState initState = new DefaultState(startTime, flightState, airportState, capacityState);

		StateAction<DefaultState> updateModule = DefaultNasUpdateFactory.parse(updateFile);

		List<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>> actionList = 
				new ArrayList<ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>>>();

		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> flightAction = ImmutablePair.of(new AlwaysCriteria<DefaultState>(), updateModule);

		StateCriteria<DefaultState> endCriteria = new AllLandedCriteria<DefaultState>();
		actionList.add(flightAction);

		return new SimulationEngineInstance<DefaultState>(actionList, endCriteria, flightHandler, initState);

	}

	public static SimulationEngineInstance<DefaultState> makeSimulationInstance(File startTimeFile,
			File flightHandlerFile, int flightFileType, File flightFile, int capacityFileType, File capacityFile,
			File airportFile, File updateFile, int solverType, File solverFile, DateTimeZone timeZone) throws Exception {
		SimulationEngineInstance<DefaultState> myInstance = makeSimulationInstance(startTimeFile, flightHandlerFile,
				flightFileType, flightFile, capacityFileType, capacityFile, airportFile, updateFile, timeZone);
		StateCriteria<DefaultState> gdpCriteria = AtStartCriteriaFactory
				.parse(myInstance.getInitialState().getCurrentTime());
		StateAction<DefaultState> gdpModule = null;
		if (solverType == DIRECT_HOFKIN) {
			gdpModule = GdpPlannerFactory.parseDirectHofkinModel(solverFile);
		} else if (solverType == DIRECT_EXTENDED_HOFKIN) {
			gdpModule = GdpPlannerFactory.parseDirectExtendedHofkinModel(solverFile);
		} else if (solverType == DIRECT_MUKHERJEE_HANSEN) {
			gdpModule = GdpPlannerFactory.parseDirectMHDynModel(solverFile);
		} else if (solverType == PAAR_HOFKIN) {
			gdpModule = GdpPlannerFactory.parseHofkinPlanner(solverFile);
		} else if (solverType == PAAR_RO) {
			gdpModule = GdpPlannerFactory.parseROPlanner(solverFile);
		}

		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> gdpAction = ImmutablePair.of(gdpCriteria, gdpModule);
		return addPreAction(myInstance, gdpAction);
	}

	public static SimulationEngineInstance<DefaultState> makeSimulationInstance(File startTimeFile,
			File flightHandlerFile, int flightFileType, File flightFile, int capacityFileType, File capacityFile,
			File airportFile, File updateFile, StateAction<DefaultState> gdpModule, DateTimeZone timeZone) throws Exception {
		SimulationEngineInstance<DefaultState> myInstance = makeSimulationInstance(startTimeFile, flightHandlerFile,
				flightFileType, flightFile, capacityFileType, capacityFile, airportFile, updateFile, timeZone);
		StateCriteria<DefaultState> gdpCriteria = AtStartCriteriaFactory
				.parse(myInstance.getInitialState().getCurrentTime());
		ImmutablePair<StateCriteria<DefaultState>,StateAction<DefaultState>> gdpAction = ImmutablePair.of(gdpCriteria, gdpModule);
		return addPreAction(myInstance, gdpAction);
	}

	public static <T> SimulationEngineInstance<T> addPreAction(SimulationEngineInstance<T> instance,
			ImmutablePair<StateCriteria<T>,StateAction<T>> preAction) {
		List<ImmutablePair<StateCriteria<T>,StateAction<T>>> myModules = instance.getModules();
		myModules.add(0, preAction);
		instance = instance.setModules(myModules);
		return instance;
	}

	public static <T> SimulationEngineInstance<T> addPostAction(SimulationEngineInstance<T> instance,
			ImmutablePair<StateCriteria<T>,StateAction<T>> postAction) {
		List<ImmutablePair<StateCriteria<T>,StateAction<T>>> myModules = instance.getModules();
		myModules.add(postAction);
		instance = instance.setModules(myModules);
		return instance;
	}
}
