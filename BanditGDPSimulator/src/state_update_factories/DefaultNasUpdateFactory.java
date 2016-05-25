package state_update_factories;

import java.io.File;
import java.util.Scanner;

import org.joda.time.Duration;

import state_update.NASStateUpdate;
import util_random.ConstantRunwayDistribution;
import util_random.DistributionFactory;
import util_random.ParameterizedDistribution;

public final class DefaultNasUpdateFactory {
	private DefaultNasUpdateFactory() {

	}

	/**
	 * This implements a factory which takes a file and produces the default
	 * update module for the default NASState object. File should be in the
	 * following format:
	 * 
	 * [Distribution of runway times - see ParameteredDistributionFactory]
	 */
	public static NASStateUpdate parse(File input) throws Exception {
		Scanner myScanner = new Scanner(input);
		return parse(myScanner);
	}

	public static NASStateUpdate parse(Scanner input) throws Exception {
		ParameterizedDistribution<Integer, Duration> runwayDist = DistributionFactory.parseParameterizedDistribution(input);
		return new NASStateUpdate(runwayDist);
	}
	
	public static NASStateUpdate makeDefault(){
		return new NASStateUpdate(new ConstantRunwayDistribution());
	}
}
