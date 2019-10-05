package template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveAgent implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;
	private List<State> stateSpace; // List of all possible states

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.numActions = 0;
		this.myAgent = agent;
		
		this.stateSpace = new ArrayList<State>();
		int costPerKm = agent.vehicles().get(0).costPerKm();
		this.createStates(topology, td, costPerKm);
		this.computeBestActionList(0.5);
		this.printBestActions();
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		State state;
		StateAction bestAction;
		
		City currentCity = vehicle.getCurrentCity();

		if (availableTask == null) {
			// Find the corresponding state
			state = findCorrespondingState(currentCity, null);
			
			// Get the best action and execute it
			bestAction = state.getBestAction();
			action = new Move(bestAction.getDest());
		}
		else {
			// Get the current state
			state = findCorrespondingState(currentCity, availableTask.deliveryCity);
			
			// Get the best action and execute it
			bestAction = state.getBestAction();
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
	
	private void createStates(Topology topology, TaskDistribution td, double costPerKm){
		// Create one state for each combination of two cities
		for(City currentCity : topology) { // First city is the current one
			for (City taskDest : topology) { // Second city is the task destination
				State state;
				String name;
				
				if(currentCity != taskDest) {
					name = "Task to " + taskDest.name + " at " + currentCity.name;
					state = new State(name, currentCity, taskDest);
				}
				else { // The two cities being the same corresponds to the case where there is no pickup task
					name = "No task at " + currentCity.name;
					state = new State(name, currentCity, null);
				}
				
				// Add the state to the state space list
				this.stateSpace.add(state);
			}
		}
		
		// Add actions to them
		for(State state : this.stateSpace) {
			// Move actions
			for(City neiCity : state.getCity().neighbors()) { // One action per neighbor city
				String name = "Move to neighbor city " + neiCity.name;
				// Reward is minus the cost of the path to the neighbor city
				double reward = -costPerKm*state.getCity().distanceTo(neiCity);
				
				StateAction action = new StateAction(name, false, reward, state, neiCity);
				
				// Create the transition table for this action
				for(State nextState : this.stateSpace) {
					// The probability is non-zero only if the city of the next state corresponds
					// to the arriving city of the action
					if(nextState.getCity() == neiCity) {
						action.transitionTable.put(nextState, td.probability(neiCity, nextState.getTaskDest()));
					}
					else {
						action.transitionTable.put(nextState, 0d);
					}
				}
				
				// Add the action to the action table of the state
				state.actionTable.add(action);
			}
			
			// Pick up action
			if(state.hasTask()) { // Only add the pickup action if the state has a task
				String name = "Accept task to " + state.getTaskDest();
				
				// Reward is the reward of the task minus the cost of the path to the destination city
				double reward = td.reward(state.getCity(), state.getTaskDest()) - costPerKm*state.getCity().distanceTo(state.getTaskDest());
				
				StateAction action = new StateAction(name, true, reward, state, state.getTaskDest());
				
				// Create the transition table for this action (same as above)
				for(State nextState : this.stateSpace) {
					if(nextState.getCity() == state.getTaskDest()) {
						action.transitionTable.put(nextState, td.probability(nextState.getCity(), nextState.getTaskDest()));
					}
					else {
						action.transitionTable.put(nextState, 0d);
					}
				}
				
				// Add the action to the action table of the state
				state.actionTable.add(action);
			}
		}
	}
	
	private void computeBestActionList(double discountFactor) {		
		boolean hasChanged = true;
		while(hasChanged) { // Loop until the results are not changing anymore
			hasChanged = false;
			
			// For each state
			for(State s : this.stateSpace) {
				double maxQ = Double.NEGATIVE_INFINITY;
				StateAction bestAction = null;
				
				// Find the action which yields the best value
				for(StateAction a : s.actionTable) {
					double Q = 0;
					
					for(Map.Entry<State, Double> entry : a.transitionTable.entrySet()) {
						Q += entry.getValue()*entry.getKey().getValue();
					}
					Q *= discountFactor;
					Q += a.getReward();
					
					if(Q > maxQ) {
						maxQ = Q;
						bestAction = a;
					}
				}
				
				// If the result is different, update the state properties
				if(s.getValue() != maxQ || s.getBestAction() != bestAction) {
					s.setValue(maxQ);
					s.setBestAction(bestAction);
					hasChanged = true; // The loop will start over
				}
			}
		}
	}
	
	// Return the state corresponding to the current city with the specified task destination.
	// Set taskDest to null for no task.
	private State findCorrespondingState(City currentCity, City taskDest) {
		State corrState = null;
		for(State s : this.stateSpace) {
			if(s.getCity() == currentCity && s.getTaskDest() == taskDest) {
				corrState = s;
				break;
			}
		}
		
		return corrState;
	}
	
	private void printBestActions() {
		for(State s : stateSpace) {
			System.out.println("At State \"" + s.getName() + "\" the best action is \"" + s.getBestAction().getName() + "\"");
		}
	}
}
