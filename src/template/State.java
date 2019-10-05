package template;

import java.util.ArrayList;
import java.util.List;

import logist.topology.Topology.City;

public class State {
	private String name;
	private City city; // Current city
	private City taskDest; // Destination city of the available task. Null if no task
	public List<StateAction> actionTable; // List of doable actions at this state
	
	private double value = 0; // Value of the state
	private StateAction bestAction = null; // Best action to do at this state
	
	public State(String name, City city, City taskDest) {
		this.name = name;
		this.city = city;
		this.taskDest = taskDest;
		this.actionTable = new ArrayList<StateAction>();
	}
	
	public boolean hasTask() {
		return this.taskDest != null;
	}
	
	public String getName() {
		return this.name;
	}

	public City getCity() {
		return city;
	}

	public City getTaskDest() {
		return taskDest;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public StateAction getBestAction() {
		return bestAction;
	}

	public void setBestAction(StateAction bestAction) {
		this.bestAction = bestAction;
	}
}
