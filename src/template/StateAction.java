package template;

import java.util.HashMap;
import java.util.Map;

import logist.topology.Topology.City;

public class StateAction {
	private String name;
	private boolean pickUp;
	private double reward;
	private State state;
	private City dest;
	public Map<State, Double> transitionTable;
	
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
