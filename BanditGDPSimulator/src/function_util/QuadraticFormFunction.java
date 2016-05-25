package function_util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class QuadraticFormFunction implements BiFunction<RealVector, SimpleTmiAction, Double> {
	private final Map<Integer, RealMatrix> tmiCoeffMatrixMap;
	private final Map<Integer, RealVector> tmiCoeffVectorMap;
	private final Map<Integer, Double> tmiConstantMap;

	public QuadraticFormFunction(RealMatrix gdpCoeffMatrix, RealVector gdpCoeffVector, Double gdpConstant,
			RealMatrix noTmiMatrix, RealVector noTmiCoeffVector, Double noTmiConstant) {
		super();
		int stateDimension = noTmiCoeffVector.getDimension();
		int totalDimension = gdpCoeffMatrix.getRowDimension();
		int gsDimension = totalDimension - stateDimension - 1;
		int[] gsRows = new int[totalDimension - 1];
		for (int i = 0; i < totalDimension - 1; i++) {
			if (i < stateDimension) {
				gsRows[i] = i;
			} else {
				gsRows[i] = i + 1;
			}
		}
		tmiCoeffMatrixMap = new HashMap<Integer, RealMatrix>();
		tmiCoeffMatrixMap.put(SimpleTmiAction.GDP_TYPE, gdpCoeffMatrix);
		tmiCoeffMatrixMap.put(SimpleTmiAction.GS_TYPE, gdpCoeffMatrix.getSubMatrix(gsRows, gsRows));
		tmiCoeffMatrixMap.put(SimpleTmiAction.NONE_TYPE, noTmiMatrix);

		tmiCoeffVectorMap = new HashMap<Integer, RealVector>();
		tmiCoeffVectorMap.put(SimpleTmiAction.GDP_TYPE, gdpCoeffVector);
		tmiCoeffVectorMap.put(SimpleTmiAction.GS_TYPE, gdpCoeffVector.getSubVector(0, stateDimension)
				.append(gdpCoeffVector.getSubVector(stateDimension, gsDimension)));
		tmiCoeffVectorMap.put(SimpleTmiAction.NONE_TYPE, noTmiCoeffVector);

		tmiConstantMap = new HashMap<Integer, Double>();
		tmiConstantMap.put(SimpleTmiAction.GDP_TYPE, gdpConstant);
		tmiConstantMap.put(SimpleTmiAction.GS_TYPE, gdpConstant);
		tmiConstantMap.put(SimpleTmiAction.NONE_TYPE, noTmiConstant);

	}

	@Override
	public Double apply(RealVector state, SimpleTmiAction action) {
		RealVector myVector = new ArrayRealVector(state.toArray(), action.asVector().toArray());
		int type = action.getType();
		return tmiConstantMap.get(type) + tmiCoeffVectorMap.get(type).dotProduct(myVector)
				+ tmiCoeffMatrixMap.get(type).operate(myVector).dotProduct(myVector);
	}

	public SimpleTmiAction getMaximumAction(RealVector state) {
		SimpleTmiAction bestGdp = getBestTmi(state,SimpleTmiAction.GDP_TYPE);
		SimpleTmiAction bestGs = getBestTmi(state,SimpleTmiAction.GS_TYPE);
		double valueOfNoTmi = this.apply(state, new SimpleTmiAction());
		double valueOfBestGdp = this.apply(state, bestGdp);
		double valueOfBestGs = this.apply(state, bestGs);

		if (valueOfNoTmi >= valueOfBestGdp && valueOfNoTmi >= valueOfBestGs) {
			return new SimpleTmiAction();
		} else if(valueOfBestGdp >= valueOfBestGs){
			return bestGdp;
		}else{
			return bestGs;
		}
	}

	public SimpleTmiAction getBestTmi(RealVector state, int type) {
		int tmiDim = SimpleTmiAction.getDimensionByType(type);
		int stateDim = state.getDimension();
		RealMatrix tmiCoeffMatrix = tmiCoeffMatrixMap.get(type);
		RealVector tmiCoeffVector = tmiCoeffVectorMap.get(type);
		
		RealMatrix gdpSubMatrix = tmiCoeffMatrix.getSubMatrix(stateDim, stateDim + tmiDim - 1, stateDim,
				stateDim + tmiDim - 1);
		RealMatrix crossSubMatrix = tmiCoeffMatrix.getSubMatrix(0, stateDim, stateDim, stateDim + tmiDim - 1);
		RealVector coeffVector = tmiCoeffVector.getSubVector(stateDim, stateDim + tmiDim - 1);
		
		CholeskyDecomposition myCholesky = new CholeskyDecomposition(gdpSubMatrix);
		DecompositionSolver mySolver = myCholesky.getSolver();
		RealVector rhs = (coeffVector.mapMultiply(0.5)).add(crossSubMatrix.operate(state));
		
		return new SimpleTmiAction(mySolver.solve(rhs));
	}
}
