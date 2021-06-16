package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class Generate implements Block, SimulatorDependent {
	private final SimulatorId simulatorId;

	public Generate(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}

	@Override
	public SimulatorId getSimulatorId() {
		return simulatorId;
	}


	private ISimulator getSimulator() {
		return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}

	private SimulatorWrapper getSimulatorWrapper() {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}

	public Generate(Supplier<Double> interval, SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
		this.interval = interval;
		getSimulator().pushEvent(new GenerateEvent(interval.get()));
	}

	private Supplier<Double> interval;
	private boolean ready = false;
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();

	public OutputDock getOutputDock() {
		return outputDock;
	}

	private class GenerateEvent extends Event {
		public GenerateEvent(double time) {
			super(Generate.this.simulatorId);
			this.time = time;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void execute() {
			ready = true;
		}
	}

	@Override
	public BlockStatus check() {
		if (!ready)
			return BlockStatus.NOTHING_TO_DO;

		if (transactStorage.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}
		Transact transact = Transact.create(simulatorId);
		transactStorage.pushTransact(transact);
		getSimulator().getDatabase().addProcessEntry(ProcessEntryType.GENERATE,
				transact.getNumber(), null);

		Double time = getSimulator().getTime() + interval.get();
		getSimulator().pushEvent(new GenerateEvent(time));
		ready = false;
		return BlockStatus.SUCCESS;
	}
}
