package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public abstract class SimulatorModel implements SimulatorDependent {
	private SimulatorId simulatorId;

	public SimulatorModel(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}

	@Override
	public final SimulatorId getSimulatorId() {
		return simulatorId;
	}

	public final SimulatorWrapper getSimulatorWrapper() {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}

	public final ISimulator getSimulator() {
		return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}
	
	public final double getCurrentTime() {
		return getSimulator().getTime();
	}
}
