package ru.bmstu.rk9.rao.ui.simulation;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.ui.RaoSimulatorHelper;

public interface UiSimulatorDependent {
	default SimulatorId getTargetSimulatorId() {
		return RaoSimulatorHelper.getTargetSimulatorId();
	}

	default ISimulator getTargetSimulator() {
		return RaoSimulatorHelper.getTargetSimulator();
	}

	default SimulatorWrapper getTargetSimulatorWrapper() {
		return RaoSimulatorHelper.getTargetSimulatorWrapper();
	}
}
