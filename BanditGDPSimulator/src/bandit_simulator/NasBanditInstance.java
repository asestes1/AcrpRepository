package bandit_simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;

import bandit_objects.Immutable;
import state_representation.DefaultState;

/**
 * This class represents an instance of a similarity-context bandit instance.
 * This will generate the context vectors beforehand, so multiple runs of the
 * instance will generate the same result if a deterministic algorithm is used.
 * This class is immutable.
 * 
 * @author Alex
 *
 */
public class NasBanditInstance implements Immutable, Serializable {

	private static final long serialVersionUID = -1218409151536156281L;
	private final List<DefaultState> unseenStates;
	private final List<RealVector> unseenDistances;
	private final List<Double> unseenBaseOutcome;
	private final NasBanditOutcome historicalOutcomes;

	public NasBanditInstance(List<DefaultState> unseenContextStates, List<RealVector> unseenDistances,List<Double> unseenBaseOutcome,
			NasBanditOutcome historicalOutcomes) {
		this.unseenStates = new ArrayList<DefaultState>(unseenContextStates);
		this.unseenDistances = new ArrayList<RealVector>(unseenDistances);
		this.unseenBaseOutcome = unseenBaseOutcome;
		this.historicalOutcomes = historicalOutcomes;
	}

	public List<Double> getUnseenBaseOutcome() {
		return unseenBaseOutcome;
	}

	public List<DefaultState> getUnseenStates() {
		return new ArrayList<DefaultState>(unseenStates);
	}

	public NasBanditOutcome getHistoricalOutcomes(){
		return historicalOutcomes;
	}

	@Override
	public String toString() {
		String thisString = "Historical outcomes: \n";
		thisString += historicalOutcomes.toString();

		int unseenSize = unseenStates.size();
		thisString += "Pregenerated States, " + unseenSize + " in total: \n";
		Iterator<DefaultState> myStateIter = unseenStates.iterator();
		for (int i = 0; i < unseenSize; i++) {
			thisString += myStateIter.next().toString() + "\n";
		}
		return thisString;
	}

	public int getMaxTimeHorizon() {
		return unseenStates.size();
	}

	public List<RealVector> getUnseenDistances() {
		return new ArrayList<RealVector>(unseenDistances);
	}
	
	
}
