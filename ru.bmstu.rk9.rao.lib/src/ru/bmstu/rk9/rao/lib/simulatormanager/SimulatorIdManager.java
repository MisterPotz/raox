package ru.bmstu.rk9.rao.lib.simulatormanager;

class SimulatorIdManager {
	private long nextId = 1;
	private static SimulatorIdManager instance;
	private SimulatorIdManager() {
		
	}
	
	public synchronized static SimulatorIdManager getInstance() {
		if (instance == null) {
			instance = new SimulatorIdManager();
		}
		return instance;
	}
	
	long getNewId() {
		return nextId++;
	}
}
