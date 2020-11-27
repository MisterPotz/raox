package ru.bmstu.rk9.rao.ui;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.TargetSimulatorManager.SimulatorUpdateEvent;

public interface TargetSimulatorManager extends Listenable<SimulatorUpdateEvent, SimulatorChangeListener>{
	SimulatorWrapper getTargetSimulatorWrapper();
	void switchTargetSimular(SimulatorId simulatorId);
	
	public class SimulatorUpdateEvent implements Listener.Event {
		
	}
}
