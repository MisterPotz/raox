package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class LoggerExtensions {
	public static void log(SimulatorId simulatorId, Object object) {
		SimulatorManagerImpl.getInstance().getSimulator(simulatorId).getLogger().log(object);
	}
}
