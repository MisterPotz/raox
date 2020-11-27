package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import org.apache.commons.math3.random.MersenneTwister;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class SelectPath implements Block, SimulatorDependent {

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

	private TransactStorage trueOutputTransactStorage = new TransactStorage();
	private TransactStorage falseOutputTransactStorage = new TransactStorage();
	private InputDock inputDock = new InputDock();
	private OutputDock trueOutputDock = () -> trueOutputTransactStorage.pullTransact();
	private OutputDock falseOutputDock = () -> falseOutputTransactStorage.pullTransact();
	private Supplier<Boolean> condition;

	public static enum SelectPathOutputs {
		TRUE("true"), FALSE("false");

		private SelectPathOutputs(final String output) {
			this.output = output;
		}

		public String getString() {
			return output;
		}

		private final String output;
	}

	public enum SelectPathMode {
		PROBABILITY, CONDITION
	}

	public SelectPath(Supplier<Boolean> condition) {
		this.condition = condition;
	}

	public SelectPath(double probability) {
		Supplier<Boolean> condition = new Supplier<Boolean>() {

			private final MersenneTwister generator = new MersenneTwister();

			@Override
			public Boolean get() {
				return generator.nextDouble() > probability ? false : true;
			}
		};
		this.condition = condition;
	}

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getTrueOutputDock() {
		return trueOutputDock;
	}

	public OutputDock getFalseOutputDock() {
		return falseOutputDock;
	}

	@Override
	public BlockStatus check() {
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		ByteBuffer data = ByteBuffer.allocate(TypeSize.BYTE);
		TransactStorage storage;
		if (condition.get()) {
			storage = trueOutputTransactStorage;
			data.put((byte) SelectPathOutputs.TRUE.ordinal());
		} else {
			storage = falseOutputTransactStorage;
			data.put((byte) SelectPathOutputs.FALSE.ordinal());
		}
		getSimulator().getDatabase().addProcessEntry(ProcessEntryType.SELECT_PATH, transact.getNumber(), data);

		if (!storage.pushTransact(transact))
			return BlockStatus.CHECK_AGAIN;

		return BlockStatus.SUCCESS;
	}
}
