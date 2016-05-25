package bandit_solvers;

import java.util.function.Function;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import random_processes.GaussianTmiComparerFactory;

@Deprecated
public final class BayesLrFactory {
	private BayesLrFactory() {

	}

	public final static BayesLrGreedySolver MakeOneDegreeGreedyBayesLr() {
		StandardLinearTmiBasis basis = new StandardLinearTmiBasis();
		return new BayesLrGreedySolver(basis.dimension, basis, defaultDegreeOneActivityVector());
	}

	public final static BayesLrTsSolver MakeOneDegreeTsBayesLr() {
		StandardLinearTmiBasis basis = new StandardLinearTmiBasis();
		return new BayesLrTsSolver(basis.dimension, basis, defaultDegreeOneActivityVector());
	}

	public final static BayesLrUcbSolver MakeOneDegreeUcbBayesLr(double power) {
		StandardLinearTmiBasis basis = new StandardLinearTmiBasis();
		return new BayesLrUcbSolver(basis.dimension, basis, defaultDegreeOneActivityVector(), power);
	}

	public final static BayesLrUcbSolver MakeOneDegreeUcbBayesLr() {
		StandardLinearTmiBasis basis = new StandardLinearTmiBasis();
		return new BayesLrUcbSolver(basis.dimension, basis, defaultDegreeOneActivityVector(), 0.0);
	}

	private final static class StandardLinearTmiBasis implements Function<SimpleTmiAction, RealVector> {
		public StandardLinearTmiBasis() {

		}

		private final int dimension = SimpleTmiAction.GDP_DIMENSION + 1;

		@Override
		public RealVector apply(SimpleTmiAction t) {
			RealVector values = new ArrayRealVector(dimension);
			values.setEntry(0, 1);
			for (int i = 1; i < dimension; i++) {
				values.setEntry(i, 0.0);
			}
			if (!(t.getType()== SimpleTmiAction.NONE_TYPE)) {
				int start = 1;
				for (int i = 0; i < SimpleTmiAction.GDP_DIMENSION; i++) {
					RealVector gdpVector = t.asVector();
					values.setEntry(start + i, gdpVector.getEntry(i));
				}
			}
			return values;
		}

	}

	private static final RealVector defaultDegreeOneActivityVector() {
		RealVector myVector = new ArrayRealVector(1);
		myVector.setEntry(0, 1.0);
		return myVector.append(GaussianTmiComparerFactory.getDefaultGdpActivity());
	}
}
