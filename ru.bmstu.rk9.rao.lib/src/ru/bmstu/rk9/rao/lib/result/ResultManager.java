package ru.bmstu.rk9.rao.lib.result;

import java.util.LinkedList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class ResultManager implements SimulatorDependent {
	private final SimulatorId simulatorId;

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
	
	public ResultManager(List<AbstractResult<?>> results, SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
		this.results.addAll(results);
		// fix-0004 exception here
		getSimulatorWrapper().getExecutionStateNotifier().addSubscriber(this.stateChangedSubscriber,
				SimulatorWrapper.ExecutionState.STATE_CHANGED);
		getSimulatorWrapper().getExecutionStateNotifier().addSubscriber(this.executionCompletedSubscriber,
				SimulatorWrapper.ExecutionState.EXECUTION_COMPLETED);
	}

	private final Subscriber stateChangedSubscriber = new Subscriber() {

		@Override
		public void fireChange() {
			for (AbstractResult<?> abstractResult : results) {
				if (!(abstractResult instanceof EvaluatableResult))
					continue;
				EvaluatableResult<?> result = (EvaluatableResult<?>) abstractResult;
				if (result.getResultMode() == ResultMode.AUTO)
					result.update();
			}
		}
	};

	private final Subscriber executionCompletedSubscriber = new Subscriber() {

		@Override
		public void fireChange() {
			for (AbstractResult<?> abstractResult : results) {
				abstractResult.prepareData();
			}
		}
	};

	private final List<AbstractResult<?>> results = new LinkedList<AbstractResult<?>>();

	public List<AbstractResult<?>> getResults() {
		return results;
	}
}
