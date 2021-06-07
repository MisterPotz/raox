package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class EvaluatableResult<T> extends AbstractResult<T> {

	public EvaluatableResult(
			AbstractDataSource<T> dataSource, 
			ResultMode resultMode, 
			Statistics<T> statistics,
			SimulatorId simulatorId) {
		super(statistics, simulatorId);
		this.dataSource = dataSource;
		this.resultMode = resultMode;
	}

	public final void update() {
		if (!dataSource.condition())
			return;
		final T value = dataSource.evaluate();
		double time = getSimulator().getTime();
		update(value, time);
	};

	private final ResultMode resultMode;

	private final AbstractDataSource<T> dataSource;

	public final ResultMode getResultMode() {
		return resultMode;
	}
}
