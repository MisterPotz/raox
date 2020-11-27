package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class Terminate implements Block, SimulatorDependent {
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

	private InputDock inputDock = new InputDock();

	public InputDock getInputDock() {
		return inputDock;
	}

	@Override
	public BlockStatus check() {
		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return BlockStatus.NOTHING_TO_DO;

		getSimulator().getDatabase().addProcessEntry(ProcessEntryType.TERMINATE, currentTransact.getNumber(), null);
		Transact.eraseTransact(currentTransact);
		return BlockStatus.SUCCESS;
	}
}
