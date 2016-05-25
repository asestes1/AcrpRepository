package gdp_planning;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the Hofkin integer programming method
 * for choosing PAARs
 * @author Alex2
 *
 */
public class HofkinModel implements DiscretePAARChooser{
	private final double groundCost;
	private final double airCost;
	private final GRBEnv myEnv ;

	
	/**
	 * Standard constructor
	 * @param groundCost - the cost of one time unit of ground delay.
	 * @param airCost - the cost of one time unit of air delay.
	 * @throws GRBException 
	 */
	public HofkinModel(double groundCost, double airCost) throws GRBException{
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.myEnv = new GRBEnv("hofkinLog.log");
	}
	
	/**
	 * This solves the Hofkin model for the given inputs
	 * @param numArriving - a list which describes the number of flights arriving at a given time
	 * @param scenarios - a set of capacity scenarios, which give the capacity in each time period
	 * @return - the list of PAARs produced by the IP solution.
	 * @throws GRBException
	 */
	public List<Integer> solveModel(List<Integer> numArriving,
			List<Integer> exemptFlights,
			List<DiscreteCapacityScenario> scenarios) throws GRBException{
	
		//Set up the model
		GRBModel model = setUpModel(numArriving,exemptFlights,scenarios);
		//Solve the model
		model.optimize();
		//Read the PAARs from the solution of the model
		List<Integer> result = getResults(numArriving.size(),model,exemptFlights);
		return result;
	}

	/**
	 * This reads the results from the Gurobi optimization, creating
	 * a list of PAARs
	 * @param numTimePeriods - the number of time periods
	 * @param model - the Gurobi model
	 * @return - the PAARs from the solution of the model
	 * @throws GRBException
	 */
	private List<Integer> getResults(int numTimePeriods, GRBModel model, List<Integer> exemptFlights)
			throws GRBException {
		List<Integer> myList = new ArrayList<Integer>(numTimePeriods);
		//Go through each time period
		for(int i =0; i < numTimePeriods;i++){
			//Get the variable corresponding to the PAAR in time period i
			GRBVar paar = model.getVarByName("F"+i);
			Double value = paar.get(GRB.DoubleAttr.X);
			myList.add(value.intValue());
		}
		return myList;
	}

	/**
	 * This sets up the Hofkin IP model in Gurobi.
	 * @param numArriving - a list where the ith entry is the number of flights arriving
	 * in the ith time period.
	 * @param exemptFlights
	 * @param scenarios
	 * @return
	 * @throws GRBException
	 */
	private GRBModel setUpModel( List<Integer> numArriving,
			List<Integer> exemptFlights, List<DiscreteCapacityScenario> scenarios) throws GRBException {
		//TODO: Should probably make the choice of log file a parameter
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
		
		//Add variables which represent ground delay of flights
		for(int i =0; i < numTimePeriods-1;i++){
			GRBVar groundDelayVar = model.addVar(0.0, GRB.INFINITY, groundCost, GRB.INTEGER, "D"+i);
			departureFlowConstraints[i].addTerm(-1.0, groundDelayVar);
			departureFlowConstraints[i+1].addTerm(1.0,groundDelayVar);
		}
		
		//Add variables which represent flights flying to the destination
		for(int i =0; i < numTimePeriods;i++){
			GRBVar flightVar = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "F"+i);
			departureFlowConstraints[i].addTerm(-1.0, flightVar);
			for(int j =0; j < numScenarios; j++){
				arrivalFlowConstraints[i][j].addTerm(1.0, flightVar);
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
			//num in - num_out = -num_arriving
			model.addConstr(departureFlowConstraints[i], GRB.EQUAL, -1.0*numArriving.get(i),"DF"+i);
		}
		
		//Add arrival conservation of flow constraints
		for(int i =0;i < numTimePeriods;i++){
			for(int j =0; j < numScenarios;j++){
				DiscreteCapacityScenario currentScenario = scenarios.get(j);
				double capacity = currentScenario.getCapacity().get(i)-exemptFlights.get(i);
				model.addConstr(arrivalFlowConstraints[i][j], GRB.LESS_EQUAL,capacity,"AF"+i+","+j);
			}
		}
		model.update();
		return model;
	}
	
}
