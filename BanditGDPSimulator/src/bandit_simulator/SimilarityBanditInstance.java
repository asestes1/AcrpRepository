package bandit_simulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.Immutable;

/**
 * This class represents an instance of a similarity-context bandit instance.
 * This will generate the context vectors beforehand, so multiple runs of the
 * instance will generate the same result if a deterministic algorithm is used.
 * This class is immutable.
 * 
 * @author Alex
 *
 */
public class SimilarityBanditInstance implements Immutable {
	private final List<RealVector> unseenStates;
	private final BanditOutcome historicalOutcomes;

	public SimilarityBanditInstance(List<RealVector> unseenContextStates, BanditOutcome
			historicalOutcomes) {
		this.unseenStates = new ArrayList<RealVector>(unseenContextStates);
		this.historicalOutcomes = historicalOutcomes;
	}

	public List<RealVector> getUnseenStates() {
		return new ArrayList<RealVector>(unseenStates);
	}

	public BanditOutcome getHistoricalOutcomes(){
		return historicalOutcomes;
	}

	@Override
	public String toString() {
		String thisString = "Historical outcomes: \n";
		thisString += historicalOutcomes.toString();

		int unseenSize = unseenStates.size();
		thisString += "Pregenerated States, " + unseenSize + " in total: \n";
		Iterator<RealVector> myStateIter = unseenStates.iterator();
		for (int i = 0; i < unseenSize; i++) {
			thisString += myStateIter.next().toString() + "\n";
		}
		return thisString;
	}

	public int getMaxTimeHorizon() {
		return unseenStates.size();
	}
}
