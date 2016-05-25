package function_util;

import java.io.File;
import java.util.Scanner;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;

public class QuadraticFormFunctionFactory {
	private QuadraticFormFunctionFactory() {

	}

	public static final QuadraticFormFunction parseQuadraticForm(File myFile) throws Exception {
		String input = "";
		Scanner myScanner = new Scanner(myFile);
		while (myScanner.hasNext() && !input.trim().equalsIgnoreCase("GDP")) {
			input = myScanner.nextLine();
		}

		RealMatrix gdpCoefficientMatrix = MatrixHelper.parseMatrixFromScanner(myScanner, SimpleTmiAction.GDP_DIMENSION,
				SimpleTmiAction.GDP_DIMENSION);

		while (myScanner.hasNext() && !input.trim().equalsIgnoreCase("STATE")) {
			input = myScanner.nextLine();
		}
		int stateDim = Integer.parseInt(myScanner.nextLine());
		RealMatrix stateMatrix = MatrixHelper.parseMatrixFromScanner(myScanner, stateDim, stateDim);

		while (myScanner.hasNext() && !input.trim().equalsIgnoreCase("INTERACTION")) {
			input = myScanner.nextLine();
		}
		RealMatrix interactionMatrix = MatrixHelper.parseMatrixFromScanner(myScanner, stateDim,
				SimpleTmiAction.GDP_DIMENSION);

		while (myScanner.hasNext() && !input.trim().equalsIgnoreCase("GDP_VECTOR")) {
			input = myScanner.nextLine();
		}
		RealVector gdpVector = MatrixHelper.parseVector(myScanner.nextLine());

		while (myScanner.hasNext() && !input.trim().equalsIgnoreCase("STATE_VECTOR")) {
			input = myScanner.nextLine();
		}
		RealVector stateVector = MatrixHelper.parseVector(myScanner.nextLine());

		while (myScanner.hasNext() && !input.trim().equalsIgnoreCase("CONSTANT")) {
			input = myScanner.nextLine();
		}
		double constant = Double.parseDouble(myScanner.nextLine());
		myScanner.close();
		return makeQuadraticForm(stateMatrix, gdpCoefficientMatrix, interactionMatrix, stateVector, gdpVector,
				constant);
	}

	public static final QuadraticFormFunction makeQuadraticForm(RealMatrix stateMatrix, RealMatrix gdpMatrix,
			RealMatrix crossMatrix, RealVector stateOptVector, RealVector gdpOptVector, Double constant) {
		RealMatrix fullGdpMatrix = MatrixHelper.symmetricMatrixCombination(stateMatrix, gdpMatrix, crossMatrix)
				.scalarMultiply(-1);
		RealVector fullGdpOptVector = stateOptVector.append(gdpOptVector);

		RealVector gdpCoeff = fullGdpMatrix.operate(fullGdpOptVector).mapMultiply(-2);

		RealVector stateCoeff = stateMatrix.operate(stateOptVector).mapMultiply(-2);

		double gdpConstant = constant + fullGdpMatrix.operate(fullGdpOptVector).dotProduct(fullGdpOptVector);
		double stateConstant = constant + stateMatrix.operate(stateOptVector).dotProduct(stateOptVector);
		return new QuadraticFormFunction(fullGdpMatrix, gdpCoeff, gdpConstant, stateMatrix, stateCoeff, stateConstant);
	}
}
