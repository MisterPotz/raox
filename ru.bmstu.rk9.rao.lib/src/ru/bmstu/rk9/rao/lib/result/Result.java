package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class Result {
	private SimulatorId simulatorId;

	public Result(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}

	public <T> UpdatableResult<T> create(Statistics<T> statistics) {
		return new UpdatableResult<T>(statistics, simulatorId);
	}

	public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource, ResultMode resultMode,
			Statistics<T> statistics) {
		return new EvaluatableResult<T>(dataSource, resultMode, statistics, simulatorId);
	}

	public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource,
			Statistics<T> statistics) {
		return new EvaluatableResult<T>(dataSource, ResultMode.AUTO, statistics, simulatorId);
	}

	public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource,
			ResultMode resultMode) {
		return new EvaluatableResult<T>(dataSource, resultMode, dataSource.getDefaultStatistics(),
				simulatorId);
	}

	public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource) {
		return new EvaluatableResult<T>(dataSource, ResultMode.AUTO,
				dataSource.getDefaultStatistics(), simulatorId);
	}

	// Utility methods

	static public <T> UpdatableResult<T> create(
			Statistics<T> statistics, 
			SimulatorId simulatorId) {
		return new UpdatableResult<T>(statistics, simulatorId);
	}

	static public <T> EvaluatableResult<T> create(
			AbstractDataSource<T> dataSource, ResultMode resultMode,
			Statistics<T> statistics, 
			SimulatorId simulatorId) {
		return new EvaluatableResult<T>(dataSource, resultMode, statistics, simulatorId);
	}

	static public <T> EvaluatableResult<T> create(
			AbstractDataSource<T> dataSource,
			Statistics<T> statistics,
			SimulatorId simulatorId) {
		return new EvaluatableResult<T>(dataSource, ResultMode.AUTO, statistics, simulatorId);
	}

	static public <T> EvaluatableResult<T> create(
			AbstractDataSource<T> dataSource, 
			ResultMode resultMode,
			SimulatorId simulatorId) {
		return new EvaluatableResult<T>(dataSource, resultMode, dataSource.getDefaultStatistics(), simulatorId);
	}

	static public <T> EvaluatableResult<T> create(
			AbstractDataSource<T> dataSource,
			SimulatorId simulatorId) {
		return new EvaluatableResult<T>(dataSource, ResultMode.AUTO, dataSource.getDefaultStatistics(), simulatorId);
	}
}
