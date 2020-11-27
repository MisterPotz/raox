package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class RaoRuntime {
	public static final double getCurrentTime(SimulatorId simulatorId) {
		return SimulatorManagerImpl.getInstance().getSimulator(simulatorId).getTime();
	}
}
