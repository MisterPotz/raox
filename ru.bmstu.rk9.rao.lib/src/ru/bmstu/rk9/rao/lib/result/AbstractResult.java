package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class AbstractResult<T> extends RaoNameable implements SimulatorDependent {
	private final SimulatorId simulatorId;

	@Override
	public SimulatorId getSimulatorId() {
	return simulatorId;
	}

	protected ISimulator getSimulator() {
	return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}

	protected SimulatorWrapper getSimulatorWrapper() {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}

	public AbstractResult(Statistics<T> statistics, SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
		this.statistics = statistics;
	}

	public final JSONObject getData() {
		JSONObject datasetData = new JSONObject();
		datasetData.put("name", getName());
		statistics.updateData(datasetData);
		return datasetData;
	};

	public final void update(T value, double time) {
		statistics.update(value, time);
		getSimulator().getDatabase().addResultEntry(this, value);
	};

	public final void prepareData() {
		statistics.prepareData();
	}

	protected final Statistics<T> statistics;
}
