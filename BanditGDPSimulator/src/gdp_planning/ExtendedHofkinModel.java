package gdp_planning;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExtendedHofkinModel {
	private final double groundCost;
	private final double airCost;
	private final GRBEnv myEnv ;
	
	/**
	 * Standard constructor
	 * @param groundCost - the cost of one time unit of ground delay.
	 * @param airCost - the cost of one time unit of air delay.
	 * @throws GRBException 
	 */
	public ExtendedHofkinModel(double groundCost, double airCost) throws GRBException{
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.myEnv = new GRBEnv("extendedHofkinLog.log");
	}
	
	/**
	 * Standard constructor
	 * @param groundCost - the cost of one time unit of ground delay.
	 * @param airCost - the cost of one time unit of air delay.
	 * @param string - the name of the log file.
	 * @throws GRBException 
	 */
	public ExtendedHofkinModel(double groundCost, double airCost, String logFileName) throws GRBException{
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.myEnv = new GRBEnv(logFileName);
	}
	
	/**
	 * This solves the Hofkin model for the given inputs
	 * @param numArriving - a list which describes the number of flights arriving at a given time
	 * @param scenarios - a set of capacity scenarios, which give the capacity in each time period
	 * @return - the list of PAARs produced by the IP solution.
	 * @throws GRBException
	 */
	public List<List<Integer>> solveModel(List<List<Integer>> numDeparting,
			List<Integer> flightTimes,
			List<Integer> exemptFlights,
			List<DiscreteCapacityScenario> scenarios,
			List<Set<Set<Integer>>> scenarioPartition,
			int desiredScenario) throws GRBException{
		//Set up the model
		GRBModel model = setUpModel(numDeparting,flightTimes,exemptFlights,scenarios,scenarioPartition);
		//Solve the model
		model.optimize();
		//Read the PAARs from the solution of the model
		List<List<Integer>> result = getResults(model,flightTimes,
				numDeparting.size(),exemptFlights.size(),desiredScenario);
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
	private List<List<Integer>> getResults(GRBModel model,List<Integer> flightTimes,int numFlightTimes, int numTimePeriods, int scenario)
			throws GRBException {
		List<List<Integer>> paars = new ArrayList<List<Integer>>(numFlightTimes);
		//Go through each time period
		for(int j = 0; j < numFlightTimes;j++){
			int flightTime = flightTimes.get(j);
			List<Integer> nextPaars = new ArrayList<Integer>(numTimePeriods);
			for(int i =0; i+flightTime < numTimePeriods;i++){
				//Get the variable corresponding to the PAAR in time period i
				GRBVar paar = model.getVarByName("F"+i+","+scenario+","+j);
				Double value = paar.get(GRB.DoubleAttr.X);
				nextPaars.add(value.intValue());
			}
			paars.add(nextPaars);

		}
		return paars;
	}

	/**
	 * This sets up the Hofkin IP model in Gurobi.
	 * @param numDeparting 
	 * @param numArriving - a list where the ith entry is the number of flights arriving
	 * in the ith time period.
	 * @param exemptFlights
	 * @param exemptFlights2 
	 * @param scenarios
	 * @param scenarioPartition 
	 * @return
	 * @throws GRBException
	 */
	private GRBModel setUpModel( List<List<Integer>> numDeparting,List<Integer> flightTimes,
			List<Integer> exemptFlights,
			 List<DiscreteCapacityScenario> scenarios, List<Set<Set<Integer>>> scenarioPartition)
					 throws GRBException {
		//TODO: Should probably make the choice of log file a parameter
		GRBModel model = new GRBModel(myEnv);
		int numFlightTimes = numDeparting.size();
		int numTimePeriods = exemptFlights.size();
		int numScenarios = scenarios.size();
		System.out.println(numFlightTimes);
		System.out.println(numTimePeriods);
		System.out.println(numScenarios);

		GRBVar[][][] groundVar = new GRBVar[numTimePeriods-1][numScenarios][numFlightTimes];
		GRBVar[][][] sendVar = new GRBVar[numTimePeriods][numScenarios][numFlightTimes];
		GRBVar[][] airVar = new GRBVar[numTimePeriods-1][numScenarios];

		
		//Add variables which represent ground delay of flights
			for(int j =0; j < numScenarios; j++){
				double probability = scenarios.get(j).getProbability();
				for(int i =0; i < numTimePeriods-1;i++){
					for(int k = 0; k < numFlightTimes;k++){
						 groundVar[i][j][k] = model.addVar(0.0, GRB.INFINITY, groundCost*probability,
								GRB.INTEGER, "D"+i+","+j+","+k);
					}
				}
			}
		
		//Add variables which represent flights flying to the destination
		for(int k = 0; k < numFlightTimes;k++){
			int flightTime = flightTimes.get(k);
			for(int i =0; i+flightTime < numTimePeriods;i++){
				for(int j =0; j < numScenarios; j++){
					sendVar[i][j][k] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "F"+i+","+j+","+k);
				}
			}
		}
		
		//Add variables which represent air delays 
		for(int j = 0; j < numScenarios; j++){
			double probability = scenarios.get(j).getProbability();
			for(int i =0; i < numTimePeriods-1; i++){		
				airVar[i][j] = model.addVar(0.0, GRB.INFINITY, airCost*probability,
						GRB.INTEGER, "A"+i+","+j);
			}
		}
		model.update();
		
		//Add departure conservation of flow constraints
		for(int k = 0; k < numFlightTimes;k++){
			int flightTime = flightTimes.get(k);
			for(int i =0; i < numTimePeriods;i++){
				for(int j = 0; j < numScenarios; j++){
					GRBLinExpr inFlow = new GRBLinExpr();
					GRBLinExpr outFlow = new GRBLinExpr();
					inFlow.addConstant(numDeparting.get(k).get(i));
					if(i > 0){
						inFlow.addTerm(1.0, groundVar[i-1][j][k]);;
					}
					if(flightTime+i < numTimePeriods){
						outFlow.addTerm(1.0,sendVar[i][j][k]);
					}
					if(i < numTimePeriods-1){
						outFlow.addTerm(1.0,groundVar[i][j][k]);
					}
					//num in - num_out = -num_arriving
					model.addConstr(inFlow, GRB.EQUAL,outFlow,"DF"+i);
				}
			}
		}
		
		//Add arrival conservation of flow constraints
		for(int j =0; j < numScenarios;j++){
			DiscreteCapacityScenario currentScenario = scenarios.get(j);

			for(int i =0;i < numTimePeriods;i++){
				GRBLinExpr inFlow = new GRBLinExpr();
				for(int k =0; k < numFlightTimes; k++){
					int flightTime = flightTimes.get(k);
					if(i - flightTime >= 0){
						inFlow.addTerm(1.0, sendVar[i-flightTime][j][k]);
					}
				}
				if(i > 0){
					inFlow.addTerm(1.0, airVar[i-1][j]);
				}
				double capacity = currentScenario.getCapacity().get(i)-exemptFlights.get(i);
				GRBLinExpr outFlow = new GRBLinExpr();
				outFlow.addConstant(capacity);
				if(i < numTimePeriods - 1){
					outFlow.addTerm(1.0, airVar[i][j]);
				}
				model.addConstr(inFlow, GRB.LESS_EQUAL,outFlow,"AF"+i+","+j);
			}
		}
		//Add anti-anticipatory constraints
		Iterator<Set<Set<Integer>>> partitionIter = scenarioPartition.iterator();
		int time = 0;
		while(partitionIter.hasNext()){
			Set<Set<Integer>> currentPartition = partitionIter.next();
			Iterator<Set<Integer>> setIter = currentPartition.iterator();
			while(setIter.hasNext()){
				Set<Integer> currentSet = setIter.next();
				if(currentSet.size() > 1){
					Iterator<Integer> itemIter = currentSet.iterator();
					int firstElement = itemIter.next();
					while(itemIter.hasNext()){
						int nextElement = itemIter.next();
						for(int k = 0; k < numFlightTimes; k++){
							int flightTime = flightTimes.get(k);
							if(time+flightTime < numTimePeriods){
								GRBLinExpr rhVar = new GRBLinExpr();
								rhVar.addTerm(1.0, groundVar[time][firstElement][k]);
								GRBLinExpr lhVar = new GRBLinExpr();
								lhVar.addTerm(1.0, groundVar[time][nextElement][k]);
								model.addConstr(lhVar, GRB.EQUAL,rhVar, "AAG"+
								firstElement+","+nextElement+","+time+","+k);
								
								rhVar = new GRBLinExpr();
								rhVar.addTerm(1.0, sendVar[time][firstElement][k]);
								lhVar = new GRBLinExpr();
								lhVar.addTerm(1.0, sendVar[time][nextElement][k]);
								model.addConstr(lhVar, GRB.EQUAL,rhVar, "AAS"+
								firstElement+","+nextElement+","+time+","+k);
							}
						}
					}
				}
			}
			time++;
		}
		model.update();
		
		
		return model;
	}
}
