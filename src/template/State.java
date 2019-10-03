package template;

import java.util.ArrayList;
import java.util.List;

import logist.topology.Topology.City;

public class State {
	private String name;
	private City city;
	private City taskDest;
	public List<StateAction> actionTable;
	
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
}
