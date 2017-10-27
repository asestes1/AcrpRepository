package gdp_planning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MHDynModel {
	private final double groundCost;
	private final double airCost;
	private final GRBEnv myEnv ;
	
	/**
	 * Standard constructor
	 * @param groundCost - the cost of one time unit of ground delay.
	 * @param airCost - the cost of one time unit of air delay.
	 * @throws GRBException 
	 */
	public MHDynModel(double groundCost, double airCost) throws GRBException{
		this.groundCost = groundCost;
		this.airCost = airCost;
		this.myEnv = new GRBEnv("mhDynLog.log");
	}
	
	/**
	 * Standard constructor
	 * @param groundCost - the cost of one time unit of ground delay.
	 * @param airCost - the cost of one time unit of air delay.
	 * @param string - the name of the log file.
	 * @throws GRBException 
	 */
	public MHDynModel(double groundCost, double airCost, String logFileName) throws GRBException{
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
	public List<Integer> solveModel(List<ImmutablePair<Integer,Integer>> flights,
			List<Integer> exemptFlights,
			List<DiscreteCapacityScenario> scenarios,
			List<Set<Set<Integer>>> scenarioPartition,
			int desiredScenario) throws GRBException{
		//Set up the model
		GRBModel model = setUpModel(flights,exemptFlights,scenarios,scenarioPartition);
		//Solve the model
		model.optimize();
		//Read the PAARs from the solution of the model
		List<Integer> result = getResults(model,flights,exemptFlights.size(),desiredScenario);
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
	private List<Integer> getResults(GRBModel model,List<ImmutablePair<Integer,Integer>> flights, int numTimePeriods, int scenario)
			throws GRBException {
		int numFlights = flights.size();
		List<Integer> delays = new ArrayList<Integer>(numFlights);
		//Go through each time period
		Iterator<ImmutablePair<Integer,Integer>> flightIter = flights.iterator();
		int flightIndex =0;
		boolean assigned = true;
		while(flightIter.hasNext()){
			ImmutablePair<Integer,Integer> nextFlight = flightIter.next();
			int earlyETA = nextFlight.getLeft();
			int flightTime = nextFlight.getRight();
			for(int j =earlyETA; j < numTimePeriods-flightTime;j++){
				//Get the variable corresponding to the PAAR in time period i
				GRBVar assignVar = model.getVarByName("D"+flightIndex+","+j+","+scenario);
				Double value = assignVar.get(GRB.DoubleAttr.X);
				if(Math.abs(1-value) <= .000001){
					delays.add(j-earlyETA);
					assigned = true;
				}
				
			}
			if(assigned == false){
				delays.add(0);
			}
			flightIndex++;
			

		}
		return delays;
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
	private GRBModel setUpModel( List<ImmutablePair<Integer,Integer>> flights,
			List<Integer> exemptFlights,
			 List<DiscreteCapacityScenario> scenarios, List<Set<Set<Integer>>> scenarioPartition)
					 throws GRBException {
		//TODO: Should probably make the choice of log file a parameter
		GRBModel model = new GRBModel(myEnv);
		int numFlights = flights.size();
		int numTimePeriods = exemptFlights.size();
		int numScenarios = scenarios.size();
		
		GRBVar[][][] assignVar = new GRBVar[numFlights][numTimePeriods][numScenarios];
		GRBVar[][] airVar = new GRBVar[numTimePeriods-1][numScenarios];

		
		//Add variables which represent assignment of flights
		Iterator<ImmutablePair<Integer,Integer>> myFlightIter = flights.iterator();
		int flightIndex = 0;
		while(myFlightIter.hasNext()){
			ImmutablePair<Integer,Integer> nextFlight = myFlightIter.next();
			int earlyETA = nextFlight.getLeft();
			int flightTime = nextFlight.getRight();
			for(int j =earlyETA; j < numTimePeriods-flightTime;j++){
				for(int k = 0; k < numScenarios;k++){
					double probability = scenarios.get(k).getProbability();
						assignVar[flightIndex][j][k] = 
								model.addVar(0.0, 1.0, groundCost*(j-earlyETA)*probability,
							GRB.BINARY, "D"+flightIndex+","+j+","+k);
				}
			}
			flightIndex++;
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
		
		//Every flight assigned
		flightIndex = 0;
		myFlightIter = flights.iterator();
		while(myFlightIter.hasNext()){
			ImmutablePair<Integer,Integer> flight = myFlightIter.next();
			int earliestETA = flight.getLeft();
			int flightTime = flight.getRight();
			for(int k = 0; k < numScenarios; k++){
				GRBLinExpr sum = new GRBLinExpr();
				for(int j =earliestETA; j < numTimePeriods-flightTime;j++){
					sum.addTerm(1.0, assignVar[flightIndex][j][k]);
				}
				model.addConstr(sum, GRB.EQUAL,1.0,"F_Assign"+flightIndex+","+k);
			}

			flightIndex++;
		}
		
		//Add arrival conservation of flow constraints
		Iterator<DiscreteCapacityScenario> myScenarioIter = scenarios.iterator();
		int scenarioIndex = 0;
		while(myScenarioIter.hasNext()){
			DiscreteCapacityScenario currentScenario = myScenarioIter.next();
			for(int i =0;i < numTimePeriods;i++){
				GRBLinExpr inFlow = new GRBLinExpr();
				myFlightIter = flights.iterator();
				flightIndex = 0;
				while(myFlightIter.hasNext()){
					ImmutablePair<Integer,Integer> nextFlight = myFlightIter.next();
					int earlyETA = nextFlight.getLeft();
					int flightTime = nextFlight.getRight();
					if(i- flightTime >= earlyETA){
						inFlow.addTerm(1.0, assignVar[flightIndex][i-flightTime][scenarioIndex]);
					}
					flightIndex++;
				}
				if(i > 0){
					inFlow.addTerm(1.0, airVar[i-1][scenarioIndex]);
				}
				double capacity = currentScenario.getCapacity().get(i)-exemptFlights.get(i);
				GRBLinExpr outFlow = new GRBLinExpr();
				outFlow.addConstant(capacity);
				if(i < numTimePeriods - 1){
					outFlow.addTerm(1.0, airVar[i][scenarioIndex]);
				}
				model.addConstr(inFlow, GRB.LESS_EQUAL,outFlow,"AF"+i+","+scenarioIndex);
			}
			scenarioIndex++;
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
						myFlightIter = flights.iterator();
						flightIndex = 0;
						while(myFlightIter.hasNext()){
							ImmutablePair<Integer,Integer> nextFlight = myFlightIter.next();
							int earlyETA = nextFlight.getLeft();
							int flightTime = nextFlight.getRight();
							if(time+flightTime < numTimePeriods && time >= earlyETA){
								GRBLinExpr rhVar = new GRBLinExpr();
								rhVar.addTerm(1.0, assignVar[flightIndex][time][firstElement]);
								GRBLinExpr lhVar = new GRBLinExpr();
								lhVar.addTerm(1.0, assignVar[flightIndex][time][nextElement]);
								model.addConstr(lhVar, GRB.EQUAL,rhVar, "AAG"+
								time+","+flightTime+","+nextElement+","+firstElement);
							}
							flightIndex++;
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
