package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.SystemEntryType;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.logger.Logger;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public class SimulatorWrapper implements SimulatorDependent {
	private ISimulator currentSimulator = null;
	private SimulatorState currentSimulatorState = SimulatorState.DEINITIALIZED;
	private final Notifier<SimulatorState> simulatorStateNotifier;
	
	private HashMap<String, Double> set;
	
	public HashMap<String, Double> getVarConstSet() {
		return set;
	}
	
	public void setVarConstSet(HashMap<String, Double> set) {
		this.set = set;
	}
	
	
	public SimulatorWrapper(ISimulator simulator) {
		setCurrentSimulatorState(SimulatorState.DEINITIALIZED);
		currentSimulator = simulator;
		this.simulatorStateNotifier =  new Notifier<SimulatorState>(SimulatorState.class);
	}

	public synchronized void preinitialize() {
		preinitialize(new SimulatorPreinitializationInfo());
	}

	public synchronized void preinitialize(SimulatorPreinitializationInfo preinitializationInfo) {
		if (isRunning)
			throw new RaoLibException("Cannot start new simulation while previous one is still running");

		if (currentSimulator == null)
			throw new RaoLibException("Cannot start new simulation: current simulator is not set");

		if (currentSimulatorState != SimulatorState.DEINITIALIZED)
			throw new RaoLibException("Cannot start new simulation: simulator wasn't deinitialized");

		currentSimulator.preinitilize(preinitializationInfo);
		setCurrentSimulatorState(SimulatorState.PREINITIALIZED);
	}

	public synchronized void initialize(SimulatorInitializationInfo initializationInfo) {
		if (currentSimulatorState != SimulatorState.PREINITIALIZED)
			throw new RaoLibException("Simulation wasn't correctly preinitialized");

		currentSimulator.initialize(initializationInfo);
		setCurrentSimulatorState(SimulatorState.INITIALIZED);
	}

	public enum SimulatorState {
		INITIALIZED, DEINITIALIZED, PREINITIALIZED
	};

	private final void setCurrentSimulatorState(SimulatorState simulatorState) {
		if (this.currentSimulatorState == simulatorState)
			return;

		this.currentSimulatorState = simulatorState;
		simulatorStateNotifier.notifySubscribers(simulatorState);
	}

	public final void notifyError() {
		onFinish(SystemEntryType.RUN_TIME_ERROR);
		setCurrentSimulatorState(SimulatorState.DEINITIALIZED);
	}

	public final Notifier<SimulatorState> getSimulatorStateNotifier() {
		return simulatorStateNotifier;
	}

	public boolean isInitialized() {
		return currentSimulatorState == SimulatorState.INITIALIZED;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public Database getDatabase() {
		return currentSimulator.getDatabase();
	}

	public StaticModelData getStaticModelData() {
		return currentSimulator.getStaticModelData();
	}

	public ModelState getModelState() {
		return currentSimulator.getModelState();
	}

	public void setModelState(ModelState modelState) {
		currentSimulator.setModelState(modelState);
	}

	public double getTime() {
		return currentSimulator.getTime();
	}

	public void pushEvent(Event event) {
		currentSimulator.pushEvent(event);
	}

	public Logger getLogger() {
		return currentSimulator.getLogger();
	}

	public enum ExecutionState {
		NOT_STARTED("Not started"), EXECUTION_STARTED("In process"), EXECUTION_COMPLETED("Finished"), EXECUTION_ABORTED, STATE_CHANGED, TIME_CHANGED, SEARCH_STEP;
		
		String stateString;
		
		private ExecutionState() {
			
		}
		
		private ExecutionState(String stateString) {
			this.stateString = stateString;
		}
		
		public String toString() {
			return stateString;
		}
	}

	private ExecutionState executionState = ExecutionState.NOT_STARTED;
	
	public Notifier<ExecutionState> getExecutionStateNotifier() {
		return currentSimulator.getExecutionStateNotifier();
	}

	private void setExecutionState(ExecutionState category) {
		ExecutionState[] states = {ExecutionState.EXECUTION_STARTED, ExecutionState.EXECUTION_COMPLETED};
		
		if (Arrays.asList(states).contains(category)) {
			executionState = category;
		}
	}
	
	public ExecutionState getExecutionState() {
		return executionState;
	}
	
	private void notifyChange(ExecutionState category) {
		setExecutionState(category);
		currentSimulator.notifyChange(category);
	}

	public List<AbstractResult<?>> getResults() {
		return currentSimulator.getResults();
	}

	private volatile boolean isRunning = false;

	public synchronized void stopExecution() {
		if (currentSimulatorState != SimulatorState.INITIALIZED)
			return;

		currentSimulator.abortExecution();
		notifyChange(ExecutionState.EXECUTION_ABORTED);
	}

	public ISimulator getSimulator() {
		return currentSimulator;
	}

	public enum SimulationStopCode {
		USER_INTERRUPT("Model terminated by user"), NO_MORE_EVENTS("Stopped by terminate condition"), TERMINATE_CONDITION("Stopped by terminate condition"), RUNTIME_ERROR("Runtime error"); // SIMULATION_CONTINUES;
	
		private String stringCode;
		
		private SimulationStopCode() {}
		
		private SimulationStopCode(String stringCode) {
			this.stringCode = stringCode;
		}
		
		public String toString() {
			return stringCode;
		}
		
	}

	public SimulationStopCode run() {
		isRunning = true;

		return stop(currentSimulator.run());
	}

	private void onFinish(Database.SystemEntryType simFinishType) {
		try {
			currentSimulator.getDatabase().addSystemEntry(simFinishType);
			notifyChange(ExecutionState.EXECUTION_COMPLETED);
		} finally {
			isRunning = false;
		}
	}

	private SimulationStopCode stop(SimulationStopCode code) {
		Database.SystemEntryType simFinishType;
		switch (code) {
		case USER_INTERRUPT:
			simFinishType = SystemEntryType.ABORT;
			break;
		case NO_MORE_EVENTS:
			simFinishType = SystemEntryType.NO_MORE_EVENTS;
			break;
		case TERMINATE_CONDITION:
			simFinishType = SystemEntryType.NORMAL_TERMINATION;
			break;
		case RUNTIME_ERROR:
			simFinishType = SystemEntryType.RUN_TIME_ERROR;
			break;
		default:
			throw new RaoLibException("Internal error: invalid simulation stop code");
		}

		onFinish(simFinishType);
		return code;
	}

	@Override
	public SimulatorId getSimulatorId() {
		return currentSimulator.getSimulatorId();
	}
}
