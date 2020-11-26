package ru.bmstu.rk9.rao.lib.simulatormanager;

import java.util.HashMap;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;

public class SimulatorManagerImpl implements ISimulatorManager {
	private HashMap<SimulatorId, ISimulator> simulatorMap;
	private static SimulatorManagerImpl instance = null;
	
	private SimulatorManagerImpl() {
		simulatorMap = new HashMap<SimulatorId, ISimulator>();
	}
	
	public static synchronized SimulatorManagerImpl getInstance() {
		if (instance == null) {
			instance = new SimulatorManagerImpl();
		}
		return instance;
	}
	
	@Override
	public SimulatorId addSimulator(ISimulator iSimulator) {
		SimulatorId newSimulatorId = SimulatorId.generateSimulatorId();
		simulatorMap.put(newSimulatorId, iSimulator);
		return newSimulatorId;
	}

	@Override
	public ISimulator getSimulator(SimulatorId simulatorId) {
		return simulatorMap.get(simulatorId);
	}
}
