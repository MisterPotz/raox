package ru.bmstu.rk9.rao.lib.simulatormanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listenable.SimpleListenable;

/* ThreadSafe */
public class SimulatorManagerImpl extends SimpleListenable implements ISimulatorManager {
	private ConcurrentHashMap<SimulatorId, ISimulator> simulatorMap;
	private ConcurrentHashMap<SimulatorId, SimulatorWrapper> simulatorWrapperMap;
	private static SimulatorManagerImpl instance = null;
	
	private SimulatorManagerImpl() {
		simulatorMap = new ConcurrentHashMap<SimulatorId, ISimulator>();
		simulatorWrapperMap = new ConcurrentHashMap<SimulatorId, SimulatorWrapper>();
	}
	
	public static synchronized SimulatorManagerImpl getInstance() {
		if (instance == null) {
			instance = new SimulatorManagerImpl();
		}
		return instance;
	}
		
	@Override
	public SimulatorId addSimulator(ISimulator iSimulator) {
		int oldAmountOfIds = getAvailableIds().size();

		SimulatorId key = iSimulator.getSimulatorId();
		if (simulatorMap.get(key) != null || simulatorWrapperMap.get(key) != null) {
			System.out.println("WARN: given simulator is already in storage");
			return null;
		}
		simulatorMap.put(iSimulator.getSimulatorId(), iSimulator);
		if (oldAmountOfIds == 0) {
			notifySimulatorAvailable();
		}
		return iSimulator.getSimulatorId();
	}

	@Override 
	public ISimulator getSimulator(SimulatorId simulatorId) {
		SimulatorWrapper probableWrapper = simulatorWrapperMap.get(simulatorId);
		ISimulator candidate;
		if (probableWrapper != null) {
			candidate = probableWrapper.getSimulator();
		} else {
			candidate = simulatorMap.get(simulatorId);
		}
		return candidate;
	}
	
	public void cleanSimulators() {
		simulatorMap.clear();
		simulatorWrapperMap.clear();
	}
	
	private void notifySimulatorAvailable() {
		// notify after the actual change - so subscribers can get the simulator
		notifyListeners(new SystemUpdateEvent(SystemState.OFF, SystemState.ON, true));
	}
	
	private void notifySimulatorEmpty() {
		// notify of emptiness before the simulator is gone - for normal processes of shutdown to happen
		notifyListeners(new SystemUpdateEvent(SystemState.ON, SystemState.OFF, false));
	}
	
	@Override 
	public SimulatorWrapper getSimulatorWrapper(SimulatorId simulatorId) {
		return simulatorWrapperMap.get(simulatorId);
	}
	
	@Override
	public SimulatorId addSimulatorWrapper(SimulatorWrapper simulatorWrapper) {
		int oldAmountOfIds = getAvailableIds().size();
		if (simulatorWrapperMap.get(simulatorWrapper.getSimulatorId()) != null) {
			System.out.println("WARN: given simulator wrapper is already in storage");
			return null;
		}
		simulatorWrapperMap.put(simulatorWrapper.getSimulatorId(), simulatorWrapper);
		// must also delete possible duplicates
		simulatorMap.remove(simulatorWrapper.getSimulatorId());
		if (oldAmountOfIds == 0) {
			notifySimulatorAvailable();
		}
		return simulatorWrapper.getSimulatorId();
	}

	@Override
	public List<SimulatorId> getAvailableIds() {
		HashSet<SimulatorId> existingIds = new HashSet();
		Iterator<SimulatorId> simpleSimulatorKeys = simulatorMap.keys().asIterator();
		Iterator<SimulatorId> simulatorWrapperKeys = simulatorWrapperMap.keys().asIterator();
		while (simpleSimulatorKeys.hasNext()) {
			SimulatorId next = simpleSimulatorKeys.next();
			existingIds.add(next);
		}
		while (simulatorWrapperKeys.hasNext()) {
			SimulatorId next = simulatorWrapperKeys.next();
			existingIds.add(next);
		}
	    return new ArrayList<SimulatorId>(existingIds);
	}
}
