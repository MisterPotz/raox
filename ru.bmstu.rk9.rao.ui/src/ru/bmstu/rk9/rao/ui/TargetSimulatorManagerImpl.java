package ru.bmstu.rk9.rao.ui;

import java.util.HashSet;
import com.google.inject.Singleton;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager;
import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager.SystemState;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listener;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listener.Event;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listenable.SimpleListenable;
import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager.SystemUpdateEvent;
@Singleton
public class TargetSimulatorManagerImpl extends SimpleListenable implements TargetSimulatorManager, Listener {
	private ISimulatorManager simulatorManager;
	private SimulatorId targetSimulatorId;
	private final HashSet<Listener> simulatorListeners;
	
	public TargetSimulatorManagerImpl() {
		this.simulatorListeners = new HashSet<Listener>();
		simulatorManager = SimulatorManagerImpl.getInstance();
	}
	
	@Override
	public ISimulatorManager getSimulatorManager() {
		return simulatorManager;
	}

	@Override
	public SimulatorWrapper getTargetSimulatorWrapper() {
		return simulatorManager.getSimulatorWrapper(targetSimulatorId);
	}

	@Override
	public void switchTargetSimulator(SimulatorId simulatorId) {
		SimulatorId old = targetSimulatorId;
		SimulatorId newId = simulatorId;
		Event notificationEvent = null;
		if (old != null && this.targetSimulatorId != null) {
			notificationEvent = new TargetSimulatorSwitchEvent(old, newId, false);
			notifyListeners(notificationEvent);
			this.targetSimulatorId = simulatorId;
			notificationEvent = new TargetSimulatorSwitchEvent(old, newId, true);
			notifyListeners(notificationEvent);
		} 
	}

	@Override
	public boolean isTargetSimulatorOn() {
		return false;
	}

	@Override
	public SystemState getState() {
		if (simulatorManager.getAvailableIds().size() > 0) {
			return SystemState.ON;
		}
		return SystemState.OFF;
	}

	@Override
	public void update(Event event) {
		if (event instanceof SystemUpdateEvent) {
			if (event.getTag().equals(SystemState.ON)) {
				
			}
			notifyListeners(event);
		}
	}
}
