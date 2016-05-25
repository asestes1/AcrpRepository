package test_state_factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.joda.time.DateTimeZone;
import org.junit.Test;

import state_factories.CapacityScenarioFactory;
import state_factories.DateTimeFactory;
import state_representation.CapacityScenarioState;

public class TestLoHighCapacityStateFactory {

	@Test
	public void testFactory() throws FileNotFoundException {
		File testFile = new File("TestFiles/ScenarioFiles/testLoHiScenarioA");
		File outFile = new File("TestOutputFiles/TestFactoryOutput/testLoToHiFactoryA");
		PrintStream myStream = new PrintStream(outFile);
		CapacityScenarioState myState = CapacityScenarioFactory.parseLoToHigh(testFile,  DateTimeFactory.parse("2007/7/25 12:00",DateTimeZone.UTC));
		myStream.println(myState.toString());
		myStream.close();
	}
}
