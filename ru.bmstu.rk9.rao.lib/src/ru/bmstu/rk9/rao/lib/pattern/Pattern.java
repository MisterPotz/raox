package ru.bmstu.rk9.rao.lib.pattern;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public abstract class Pattern implements SimulatorDependent {
	private SimulatorId simulatorId;

	@Override
	public SimulatorId getSimulatorId() {
	return simulatorId;
	}

	@Override
	public void setSimulatorId(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}

	protected ISimulator getSimulator() {
	return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}

	protected SimulatorWrapper getSimulatorWrapper() {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}
	
	public static enum ExecutedFrom {
		SEARCH(Database.ResourceEntryType.SEARCH), SOLUTION(Database.ResourceEntryType.SOLUTION);

		public final Database.ResourceEntryType resourceSpecialStatus;

		private ExecutedFrom(Database.ResourceEntryType resourceSpecialStatus) {
			this.resourceSpecialStatus = resourceSpecialStatus;
		}
	}

	public abstract void run();

	public abstract void finish();

	public abstract boolean selectRelevantResources();

	public abstract String getTypeName();

	protected final List<Integer> relevantResourcesNumbers = new ArrayList<Integer>();

	public final List<Integer> getRelevantResourcesNumbers() {
		return relevantResourcesNumbers;
	}

	public final void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom, String dptName) {
		getSimulator().getDatabase().addMemorizedResourceEntries(
				this.getTypeName() + "." + SerializationConstants.CREATED_RESOURCES, executedFrom, dptName);
	}
}
