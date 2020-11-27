package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class UpdatableResult<T> extends AbstractResult<T> {

	public UpdatableResult(Statistics<T> statistics, SimulatorId simulatorId) {
		super(statistics, simulatorId);
	}

	public final void update(T value) {
		double time = getSimulator().getTime();
		update(value, time);
	};
}
