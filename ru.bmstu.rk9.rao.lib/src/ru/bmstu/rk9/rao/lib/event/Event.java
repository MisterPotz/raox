package ru.bmstu.rk9.rao.lib.event;

import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public abstract class Event implements SimulatorDependent {
	private SimulatorId simulatorId;

	@Override
	public SimulatorId getSimulatorId() {
	return simulatorId;
	}

	@Override
	public void setSimulatorId(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}

	private ISimulator getSimulator() {
	return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}

	private SimulatorWrapper getSimulatorWrapper() {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}
	protected double time;

	public final double getTime() {
		return time;
	}

	public abstract String getName();

	public final void run() {
		execute();
		getSimulator().getDatabase().addEventEntry(this);
		getSimulator().getDatabase().addMemorizedResourceEntries(
				this.getName() + "." + SerializationConstants.CREATED_RESOURCES, null, null);
	}

	protected abstract void execute();
}
