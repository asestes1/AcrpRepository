package bandit_solvers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import bandit_objects.SimpleTmiAction;
import function_util.MatrixHelper;

@Deprecated
public abstract class BayesLrSolver extends IndexSolver {
	protected final int basisFunctionDimension;
	protected final Function<SimpleTmiAction,RealVector> basisFunction;
	protected final RealMatrix priorCovMatrix;

	protected List<RealVector> basisHistory;
	protected RealMatrix capacitanceMatrix;
	//The ith vector of this matrix is phi_i^T * V where V is the prior covariance
	//submatrix
	protected List<RealVector> basisPriorProducts;
	//The entry (i,j) of this matrix consists of (phi_i^T * V * phi_j)*s_ij where
	//phi_i is the basis function for the ith observation and s_ij
	//is the similarity between states i and j.
	protected List<List<Double>> basisPriorBasisProducts;
	
	public BayesLrSolver(int basis_function_dimension,
			Function<SimpleTmiAction,RealVector> basis_function,
			RealMatrix priorCovMatrix) {
		super(1.0);
		this.basisFunctionDimension = basis_function_dimension;
		this.basisFunction = basis_function;
		this.priorCovMatrix = priorCovMatrix;
		basisHistory = new ArrayList<RealVector>();
		capacitanceMatrix = null;
		basisPriorProducts = new ArrayList<RealVector>();
		basisPriorBasisProducts = new ArrayList<List<Double>>();
	}
	
	@Override
	public void reset() {
		super.reset();
		basisHistory = new ArrayList<RealVector>();
		capacitanceMatrix = null;
		basisPriorProducts = new ArrayList<RealVector>();
		basisPriorBasisProducts = new ArrayList<List<Double>>();
	}
	
	public BayesLrSolver(int basis_function_dimension,
			Function<SimpleTmiAction,RealVector> basis_function) {
		this(basis_function_dimension,basis_function,
				MatrixHelper.makeIdentityMatrix(basis_function_dimension));
	}
	
	public BayesLrSolver(int basis_function_dimension,
			Function<SimpleTmiAction,RealVector> basis_function,
			RealVector activity) {
		this(basis_function_dimension,basis_function,
				MatrixHelper.makeDiagonalMatrix(activity));
	}

	public void addHistory(RealVector context,SimpleTmiAction  action, Double outcome){
		RealVector newBasisEval = basisFunction.apply(action);
		int historyLength = context.getDimension();
		
		//We store a vector of the dot product of any pair of vectors that
		//we have seen, and also the rank one matrices.
		Iterator<RealVector> basisPriorProdIter = basisPriorProducts.iterator();
		Iterator<List<Double>> basisPriorBasisProdIter 
				= basisPriorBasisProducts.iterator();		
		List<Double> newBasisPriorBasisProducts = new ArrayList<Double>(historyLength+1);
		RealVector perturbVector = new ArrayRealVector(historyLength);
		for(int i=0;i< historyLength;i++){
			//This gives us phi_i * V where V is the covariance submatrix
			RealVector nextBasisPriorProds = basisPriorProdIter.next();
			
			//Calculate s_in * phi_i * V * phi_n where V is the prior covariance matrix 
			// and phi_i and phi_n are the basis evaluations.
			Double nextBasisPriorBasisProd = nextBasisPriorProds.dotProduct(newBasisEval)
													*context.getEntry(i);
			newBasisPriorBasisProducts.add(nextBasisPriorBasisProd);
			basisPriorBasisProdIter.next().add(nextBasisPriorBasisProd);
			perturbVector.setEntry(i,nextBasisPriorBasisProd);
		}
		
		double selfPriorSelfProduct = newBasisEval.dotProduct(
				priorCovMatrix.operate(newBasisEval));
		newBasisPriorBasisProducts.add(selfPriorSelfProduct);
		basisPriorBasisProducts.add(newBasisPriorBasisProducts);
		basisPriorProducts.add(priorCovMatrix.operate(newBasisEval));
		
		//Calculate the capacitance matrix
		if(historyLength > 0){
			RealVector prodPerturbVector = capacitanceMatrix.operate(perturbVector);
			Double scaleFactor = 1/((1+selfPriorSelfProduct)-prodPerturbVector.dotProduct(
					perturbVector));
			RealMatrix perturbMatrix = MatrixHelper.makeRankOneMatrix(
					prodPerturbVector, prodPerturbVector);
			RealMatrix upperLeft = capacitanceMatrix.add(
					perturbMatrix.scalarMultiply(scaleFactor));
			RealVector upperRight = prodPerturbVector.mapMultiply(-1*scaleFactor);
			RealVector lowerLeft = upperLeft.preMultiply(perturbVector)
					.mapMultiply(-1.0/(1+selfPriorSelfProduct));
			//Copy all the elements to the correct entry.
			capacitanceMatrix = new BlockRealMatrix(historyLength+1,historyLength+1);
			for(int i =0; i < historyLength;i++){
				for(int j =0;j < historyLength;j++){
					capacitanceMatrix.setEntry(i, j, upperLeft.getEntry(i, j));
				}
			}
			for(int i =0; i < historyLength;i++){
				capacitanceMatrix.setEntry(i, historyLength, upperRight.getEntry(i));
			}
			for(int j =0; j < historyLength;j++){
				capacitanceMatrix.setEntry(historyLength,j, lowerLeft.getEntry(j));
			}
			capacitanceMatrix.setEntry(historyLength, historyLength, scaleFactor);
		}else{
			capacitanceMatrix = new BlockRealMatrix(1,1);
			capacitanceMatrix.setEntry(0, 0, 1/(1+selfPriorSelfProduct));
		}
		basisHistory.add(newBasisEval);
		super.addHistory(context, action, outcome);
	}
	
	public RealMatrix formSubpriorBasisProd(RealVector similarities){
		int nObs = basisHistory.size();
		RealMatrix priorBasisMatrix = new BlockRealMatrix(basisFunctionDimension,nObs);
		Iterator<RealVector> basisPriorProdIter = basisPriorProducts.iterator(); 
		for(int i=0; i < nObs;i++){
			priorBasisMatrix.setColumnVector(i, 
					basisPriorProdIter.next().mapMultiply(similarities.getEntry(i)));
		}
		return priorBasisMatrix;
	}
	/**
	 * This calculates the posterior mean of the coefficients
	 * given a vector of similarities between the current 
	 * weather/traffic state and the previous states.
	 * @param similarities
	 * @return
	 */
	public RealVector calculateMean(RealVector similarities){
		RealMatrix subpriorBasisProduct = formSubpriorBasisProd(similarities);
		RealVector rewardVector = MatrixHelper.makeVector(rewardHistory);
		RealMatrix basisPriorBasisMatrix = MatrixHelper.makeMatrix(basisPriorBasisProducts);

		RealVector firstTerm = subpriorBasisProduct.operate(rewardVector);
		RealVector secondTerm = (subpriorBasisProduct.multiply(capacitanceMatrix))
								 .multiply(basisPriorBasisMatrix).operate(rewardVector);
		return firstTerm.subtract(secondTerm);
	}
	
	/**
	 * This calculates the posterior covariance of the coefficients
	 * at a given state, given a vector of similarities between
	 * the current weather/traffic state and the previous states.
	 * @param similarities
	 * @return
	 */
	public RealMatrix calculateCov(RealVector similarities){
		RealMatrix priorBasisMatrix = formSubpriorBasisProd(similarities);
		return priorCovMatrix.subtract(
				priorBasisMatrix.multiply(capacitanceMatrix)
				.multiply(priorBasisMatrix.transpose()));
	}
	
	/**
	 * This calculates the posterior Beta given the observed values.
	 * @param unseen_similarities
	 * @return
	 */
	public double getBeta(){
		RealVector rewardVector = MatrixHelper.makeVector(rewardHistory);
		RealMatrix basisPriorBasisProd = MatrixHelper.makeMatrix(basisPriorBasisProducts);
		RealVector basisPriorBasisRewardProd = basisPriorBasisProd.operate(rewardVector);
		Double firstTerm = rewardVector.dotProduct(rewardVector);
		Double secondTerm = basisPriorBasisRewardProd.dotProduct(rewardVector);
		Double thirdTerm = capacitanceMatrix.operate(basisPriorBasisRewardProd).dotProduct(
							basisPriorBasisRewardProd);
		return 0.5*(firstTerm-(secondTerm-thirdTerm));
	}
	
	@Override
	public abstract Map<SimpleTmiAction, Double> getIndices(RealVector similarities, int remaining_time);
}
