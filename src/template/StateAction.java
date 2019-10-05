package template;

import java.util.HashMap;
import java.util.Map;

import logist.topology.Topology.City;

public class StateAction {
	private String name;
	private boolean pickUp; // Whether it is pickup task action or not (move)
	private double reward;
	private State state; // Current state when starting the action
	private City dest; // Destination city of the task or the move
	public Map<State, Double> transitionTable; // Associate each state with the probability to transition into it
	
	public StateAction(String name, boolean pickUp, double reward, State state, City dest) {
		this.name = name;
		this.pickUp = pickUp;
		this.reward = reward;
		this.state = state;
		this.dest = dest;
		
		this.transitionTable = new HashMap<State, Double>();
	}
	
	public String getName() {
		return this.name;
	}

	public boolean isPickUp() {
		return pickUp;
	}

	public double getReward() {
		return reward;
	}

	public State getState() {
		return state;
	}

	public City getDest() {
		return dest;
	}
}
