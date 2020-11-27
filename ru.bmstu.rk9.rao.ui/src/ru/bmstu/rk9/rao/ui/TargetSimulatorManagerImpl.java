package ru.bmstu.rk9.rao.ui;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class TargetSimulatorManagerImpl implements TargetSimulatorManager {
	private SimulatorId targetSimulatorId;
	private final List<SimulatorChangeListener> simulatorListeners;
	
	public TargetSimulatorManagerImpl() {
		this.simulatorListeners = new ArrayList<SimulatorChangeListener>();
	}
	
	@Override
	public void registerListener(SimulatorChangeListener listener) {
		simulatorListeners.add(listener);
	}

	@Override
	public SimulatorWrapper getTargetSimulatorWrapper() {
		// TODO Auto-generated method stub
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(targetSimulatorId);
	}

	@Override
	public void deleteListener(SimulatorChangeListener listener) {
		simulatorListeners.remove(listener);
	}

	@Override
	public void notifyListeners(SimulatorUpdateEvent event) {
		for (SimulatorChangeListener listener : simulatorListeners) {
			listener.notify(event);
		}
	}

	@Override
	public void switchTargetSimular(SimulatorId simulatorId) {
		// TODO implement and use this method in some new view that will switch the current simulator
	}
}
