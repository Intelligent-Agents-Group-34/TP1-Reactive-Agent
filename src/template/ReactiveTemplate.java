package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;
	private List<State> stateSpace;
	private Map<State, Double> V;
	private Map<State, StateAction> bestActions;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.numActions = 0;
		this.myAgent = agent;
		
		this.stateSpace = new ArrayList<State>();
		this.V = new HashMap<State, Double>();
		this.bestActions = new HashMap<State, StateAction>();
		this.createState(topology, td, 10);
		this.printStates();
		this.computeBestActionList(0.5);
		this.printBestActions();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		State state = null;
		StateAction bestAction;
		
		City currentCity = vehicle.getCurrentCity();

		if (availableTask == null) {
			// Find the corresponding state
			for(State s : this.stateSpace) {
				if(s.getCity() == currentCity && !s.hasTask()) {
					state = s;
				}
			}
			
			// Get the best action at this state and execute it
			bestAction = bestActions.get(state);
			action = new Move(bestAction.getDest());
		}
		else {
			// Find the corresponding state
			for(State s : this.stateSpace) {
				if(s.getCity() == currentCity && s.getTaskDest() == availableTask.deliveryCity) {
					state = s;
				}
			}
			
			// Get the best action at this state and execute it
			bestAction = bestActions.get(state);
			if(bestAction.isPickUp()) {
				action = new Pickup(availableTask);
			}
			else {
				action = new Move(bestAction.getDest());
			}
		}
		
		System.out.println("Current state: " + state.getName());
		System.out.println("Chosen best action: " + bestAction.getName());
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	
	private void createState(Topology topology, TaskDistribution td, double costPerKm){
		// Create states
		for(City currentCity : topology) {
			for (City taskDest : topology) {
				State state;
				String name;
				if(currentCity != taskDest) {
					name = "Task to " + taskDest.name + " at " + currentCity.name;
					state = new State(name, currentCity, taskDest);
				}
				else {
					name = "No task at " + currentCity.name;
					state = new State(name, currentCity, null);
				}
				this.stateSpace.add(state);
			}
		}
		
		// Add actions to them
		for(State state : this.stateSpace) {
			// Move actions
			for(City neiCity : state.getCity().neighbors()) {
				String name = "Move to neighbor city " + neiCity.name;
				double reward = -costPerKm*state.getCity().distanceTo(neiCity);
				StateAction action = new StateAction(name, false, reward, state, neiCity);
				for(State nextState : this.stateSpace) {
					if(nextState.getCity() == neiCity) {
						action.transitionTable.put(nextState, td.probability(neiCity, nextState.getTaskDest()));
					}
					else {
						action.transitionTable.put(nextState, 0d);
					}
				}
				state.actionTable.add(action);
			}
			
			// Pick up action
			if(state.hasTask()) {
				String name = "Accept task to " + state.getTaskDest();
				double reward = td.reward(state.getCity(), state.getTaskDest()) - costPerKm*state.getCity().distanceTo(state.getTaskDest());
				StateAction action = new StateAction(name, true, reward, state, state.getTaskDest());
				for(State nextState : this.stateSpace) {
					if(nextState.getCity() == state.getTaskDest()) {
						action.transitionTable.put(nextState, td.probability(nextState.getCity(), nextState.getTaskDest()));
					}
					else {
						action.transitionTable.put(nextState, 0d);
					}
				}
				state.actionTable.add(action);
			}
		}
	}
	
	private void computeBestActionList(double discountFactor) {
		// Init the accumulated values
		for(State s : this.stateSpace) {
			this.V.put(s, 0d);
		}
		
		boolean hasChanged = true;
		while(hasChanged) {
			hasChanged = false;
			for(State s : this.stateSpace) {
				double maxQ = Double.NEGATIVE_INFINITY;
				StateAction bestAction = null;
				for(StateAction a : s.actionTable) {
					double Q = 0;
					for(Map.Entry<State, Double> entry : a.transitionTable.entrySet()) {
						Q += entry.getValue()*V.get(entry.getKey());
					}
					Q *= discountFactor;
					Q += a.getReward();
					if(Q > maxQ) {
						maxQ = Q;
						bestAction = a;
					}
				}
				double oldMaxQ = this.V.put(s, maxQ);
				hasChanged |= oldMaxQ == maxQ;
				this.bestActions.put(s, bestAction);
			}
		}
	}
	
	private void printStates() {
		for(State s : this.stateSpace) {
			System.out.println(s.getName());
		}
	}
	
	private void printBestActions() {
		for(Map.Entry<State, StateAction> entry : this.bestActions.entrySet()) {
			System.out.println("At State \"" + entry.getKey().getName() + "\" the best action is \"" + entry.getValue().getName() + "\"");
		}
	}
}
