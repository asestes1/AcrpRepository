package function_util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class MatrixHelper {
	private MatrixHelper(){
		
	}
	/**
	 * This forms the matrix uv^T.
	 * @param u
	 * @param v
	 * @return
	 */
	public static final RealMatrix makeRankOneMatrix(RealVector u, RealVector v){
		int m = u.getDimension();
		int n = v.getDimension();
		RealMatrix myMatrix = new BlockRealMatrix(m, n);
		for(int i =0; i < m;i++){
			for(int j =0; j < n;j++){
				myMatrix.setEntry(i, j, u.getEntry(i)*v.getEntry(j));
			}
		}
		return myMatrix;
	}
	
	public static RealMatrix makeIdentityMatrix(int size){
		RealMatrix myMatrix = new BlockRealMatrix(size, size);
		for(int i =0; i < size;i++){
			for(int j =0; j < size;j++){
				if(i== j){
					myMatrix.setEntry(i, j, 1.0);
				}else{
					myMatrix.setEntry(i, j, 0.0);
				}
			}
		}
		return myMatrix;
	}
	
	public static final RealMatrix makeDiagonalMatrix(RealVector array){
		int matrixSize = array.getDimension();
		RealMatrix myMatrix = 
				new BlockRealMatrix(matrixSize,matrixSize);
		for(int i= 0; i < matrixSize;i++){
			for (int j=0;j < matrixSize;j++){
				if(i == j){
					myMatrix.setEntry(i, j, array.getEntry(i));
				}else{
					myMatrix.setEntry(i, j, 0.0);
				}
			}
		}
		return myMatrix;
	}
	
	public static final RealMatrix makeMatrix(List<List<Double>> matrix){
		int numRows = matrix.size();
		int numCols = matrix.get(0).size();
		RealMatrix myMatrix = 
				new BlockRealMatrix(numRows,numCols);
		Iterator<List<Double>> rowIter_i = matrix.iterator();
		for(int i= 0; i < numRows;i++){
			Iterator<Double> elementIter_ij = rowIter_i.next().iterator();
			for (int j=0;j < numCols;j++){
				myMatrix.setEntry(i, j, elementIter_ij.next());
			}
		}
		return myMatrix;
	}
	
	public static final RealVector makeVector(List<Double> vector){
		int vectorSize = vector.size();
		RealVector myVector = new ArrayRealVector(vectorSize);
		Iterator<Double> vectorValueIter = vector.iterator();
		for(int i=0;i < vectorSize;i++){
			myVector.setEntry(i, vectorValueIter.next());
		}
		return myVector;
	}
	
	public static final RealMatrix parseMatrixFromScanner(Scanner openScanner, int m, int n) throws Exception{
		double[][] doubleMatrix = new double[m][n];
		if(openScanner.hasNextLine()){
			for(int i=0; i < m;i++){
				if(openScanner.hasNextLine()){
					String rowString = openScanner.nextLine().trim();
					String[] elements = rowString.split(",");
					if(elements.length != n){
						throw new Exception("Dimensions of GDP coefficients are incorrect");
					}
					for(int j =0; j < n;j++){
						doubleMatrix[i][j] = Double.parseDouble(elements[j]);
					}
				}else{
					throw new Exception("Dimensions of GDP coefficients are incorrect");
				}
			}
		}else{
			throw new Exception("GDP coefficients are missing.");
		}
		return new BlockRealMatrix(doubleMatrix);
	}
	
	public static final RealMatrix symmetricMatrixCombination(RealMatrix upperLeft,
														RealMatrix lowerRight,
														RealMatrix upperRight){
		int numRows = upperLeft.getRowDimension()+lowerRight.getRowDimension();
		int numColumns = upperLeft.getColumnDimension()
						 +lowerRight.getColumnDimension();
		RealMatrix fullMatrix = new BlockRealMatrix(numRows,numColumns);
		fullMatrix.setSubMatrix(upperLeft.getData(), 0, 0);
		fullMatrix.setSubMatrix(lowerRight.getData(),
								upperLeft.getRowDimension(),
								upperLeft.getColumnDimension());
		fullMatrix.setSubMatrix(upperRight.getData(), 0,
								upperLeft.getColumnDimension());
		fullMatrix.setSubMatrix(upperRight.transpose().getData(),
								upperLeft.getRowDimension(),
								0);
		return fullMatrix;
	}
	
	public static final RealVector parseVector(String line){
		String[] elements = line.split(",");
		int length = elements.length;
		RealVector myVector = new ArrayRealVector(length);
		for(int i =0; i < length;i++){
			myVector.setEntry(i, Double.parseDouble(elements[i]));
		}
		return myVector;
	}
	
	public static final RealMatrix makeZerosMatrix(int rows,int columns){

		RealMatrix myMatrix = 
				new BlockRealMatrix(rows,columns);
		for(int i= 0; i < rows;i++){
			for (int j=0;j < columns;j++){
				myMatrix.setEntry(i, j, 0.0);
			}
		}
		return myMatrix;
	}
	
	public static final void addSimilarityInPlace(List<List<Double>> similarityMatrix, RealVector newSimilarity ){
		int nSim = similarityMatrix.size();
		Iterator<List<Double>> rowIter = similarityMatrix.iterator();
		List<Double> newRow = new LinkedList<Double>();
		for(int i=0; i < nSim;i++){
			Double nextEntry = newSimilarity.getEntry(i);
			rowIter.next().add(nextEntry);
			newRow.add(nextEntry);
		}
		similarityMatrix.add(newRow);
	}
}
