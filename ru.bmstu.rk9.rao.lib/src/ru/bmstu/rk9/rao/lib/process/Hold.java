package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class Hold implements Block, SimulatorDependent{

	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();
	private Supplier<Double> duration;
	private SimulatorId simulatorId;

	public Hold(SimulatorId simulatorId) {
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

	public Hold(Supplier<Double> duration) {
		this.duration = duration;
	}

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (transactStorage.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}

		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		addHoldEntryToDatabase(transact, HoldAction.IN);
		Double time = getSimulator().getTime() + duration.get();
		getSimulator().pushEvent(new HoldEvent(transact, time));
		return BlockStatus.SUCCESS;
	}

	private void addHoldEntryToDatabase(Transact transact, HoldAction holdAction) {
		ByteBuffer data = ByteBuffer.allocate(TypeSize.BYTE);
		data.put((byte) holdAction.ordinal());
		getSimulator().getDatabase().addProcessEntry(ProcessEntryType.HOLD, transact.getNumber(), data);
	}

	
	public static enum HoldAction {
		IN("in"), OUT("out");

		private HoldAction(final String action) {
			this.action = action;
		}

		public String getString() {
			return action;
		}

		private final String action;
	}
	
	private class HoldEvent extends Event {
		private Transact transact;

		public HoldEvent(Transact transact, double time) {
			super(Hold.this.simulatorId);
			this.time = time;
			this.transact = transact;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void execute() {
			if (!transactStorage.pushTransact(transact))
				throw new ProcessException("Transact collision in Hold block");
			addHoldEntryToDatabase(transact, HoldAction.OUT);
		}
	}
}
