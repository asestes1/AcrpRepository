package gdp_planning;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.List;

import metrics.PiecewiseLinearFunction;

//TODO: the output part of this needs to be redone, as the PAARs it outputs
//are not really what should be output.
public class RichettaOdoniModel implements DiscretePAARChooser{
	private final PiecewiseLinearFunction groundDelayCostFunction;
	private final double airCost;
	
	public RichettaOdoniModel(PiecewiseLinearFunction groundCost, double airCost){
		this.groundDelayCostFunction = new PiecewiseLinearFunction(groundCost);
		this.airCost = airCost;
	}
	
	/**
	 * This solves the Richetta-Odooni model for the given inputs
	 * @param numArriving - a list which describes the number of flights arriving at a given time
	 * @param scenarios - a set of capacity scenarios, which give the capacity in each time period
	 * @return
	 * @throws GRBException
	 */
	public List<Integer> solveModel(List<Integer> numArriving, List<Integer> numExempt,
			List<DiscreteCapacityScenario> scenarios) throws GRBException{
	
		GRBModel model = setUpModel(numArriving,numExempt,scenarios);
		model.optimize();
		List<Integer> result = getResults(numArriving.size(),model,numExempt);
		return result;
	}

	/**
	 * This reads the results from the Gurobi optimization, creating
	 * a list of PAARs
	 * @param numTimePeriods
	 * @param model
	 * @return
	 * @throws GRBException
	 */
	private List<Integer> getResults(int numTimePeriods, GRBModel model, List<Integer> exemptFlights) throws GRBException {
		List<Integer> myList = new ArrayList<Integer>(exemptFlights);
		for(int i =0; i < numTimePeriods;i++){
			for(int j = i; j < numTimePeriods;j++){
				GRBVar paar = model.getVarByName("F"+i+","+j);
				Double value = paar.get(GRB.DoubleAttr.X);
				myList.set(j,  myList.get(j)+value.intValue());
			}
		}
		return myList;
	}

	private GRBModel setUpModel( List<Integer> numArriving,
			List<Integer> numExempt, List<DiscreteCapacityScenario> scenarios) throws GRBException {
		//TODO: Should probably make the choice of log file a parameter
		GRBEnv myEnv = new GRBEnv("richettaOdoniLog.log");
		GRBModel model = new GRBModel(myEnv);
		int numTimePeriods = numArriving.size();
		int numScenarios = scenarios.size();
		GRBLinExpr[] departureFlowConstraints = new GRBLinExpr[numTimePeriods];
		GRBLinExpr[][] arrivalFlowConstraints = new GRBLinExpr[numTimePeriods][numScenarios];
		for(int i =0 ; i < numTimePeriods; i++){
			departureFlowConstraints[i] = new GRBLinExpr();
		}
		for(int i =0; i < numTimePeriods;i++){
			for(int j =0; j < numScenarios;j++){
				arrivalFlowConstraints[i][j] = new GRBLinExpr();
			}
		}
		//Add variables which represent flow of flights
		for(int i =0; i < numTimePeriods;i++){
			for(int j = i; j < numTimePeriods;j++){
				double cost = groundDelayCostFunction.evaluateAt(j-i);
				GRBVar flightVar = model.addVar(0.0, GRB.INFINITY,
						cost,GRB.INTEGER, "F"+i+","+j);
				departureFlowConstraints[i].addTerm(-1.0, flightVar);
				for(int k = 0; k <numScenarios;k++){
					arrivalFlowConstraints[j][k].addTerm(1.0, flightVar);
				}
			}
		}
		
		
		//Add variables which represent air delays 
		for(int i =0; i < numTimePeriods-1; i++){		
			for(int j = 0; j < numScenarios; j++){
				double probability = scenarios.get(j).getProbability();
				GRBVar airDelayVar = model.addVar(0.0, GRB.INFINITY, airCost*probability,
						GRB.INTEGER, "A"+i+","+j);
				arrivalFlowConstraints[i][j].addTerm(-1.0, airDelayVar);
				arrivalFlowConstraints[i+1][j].addTerm(1.0, airDelayVar);
			}
		}
		model.update();
		//Add departure conservation of flow constraints
		for(int i =0; i < numTimePeriods;i++){
			//num_in - num_out = - num_arriving
			model.addConstr(departureFlowConstraints[i], GRB.EQUAL, -1.0*numArriving.get(i),"DF"+i);
		}
		
		//Add arrival conservation of flow constraints
		for(int i =0;i < numTimePeriods;i++){
			for(int j =0; j < numScenarios;j++){
				DiscreteCapacityScenario currentScenario = scenarios.get(j);
				double capacity = currentScenario.getCapacity().get(i)-numExempt.get(i);
				model.addConstr(arrivalFlowConstraints[i][j], GRB.LESS_EQUAL,capacity,"AF"+i+","+j);
			}
		}
		model.update();
		return model;
	}
}
