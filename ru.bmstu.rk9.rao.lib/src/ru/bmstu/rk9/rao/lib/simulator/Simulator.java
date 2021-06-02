package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import ru.bmstu.rk9.rao.lib.contract.RaoGenerationContract;
import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.dpt.DPTManager;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.event.EventScheduler;
import ru.bmstu.rk9.rao.lib.logger.Logger;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.process.Process;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.result.ResultManager;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.utils.SimulatorReflectionUtils;

public class Simulator implements ISimulator {
	private Object modelInstance;
	private Object initializationScopeInstance;
	private Integer simulatorId;

	private void assertHasModel() {
		if (modelInstance == null) {
			throw new IllegalStateException("model was not passed to a simulator before calling the methods that require the model presence within the simulator");
		}
	}
	
	private void assertHasId() {
		if (simulatorId == null) {
			throw new IllegalStateException("simulatorid was not set");
		}
	}

	@Override
	public void preinitilize(SimulatorPreinitializationInfo preinitializationInfo) {
		
		modelState = new ModelState(preinitializationInfo.resourceClasses);
		database = new Database(preinitializationInfo.modelStructure);
		staticModelData = new StaticModelData(preinitializationInfo.modelStructure);
		logger = new Logger();

		assertHasId();

		Constructor<?> modelConstructor = 
		ReflectionUtils.safeGetConstructor(preinitializationInfo.getSimulatorCommonModelInfo().getModelClass(), RaoGenerationContract.SIMULATOR_ID_CLASS);
		
		
		if (modelConstructor == null) {
			System.out.println("Simulator preinitialization failed");
			return;		}
		
		Object modelInstance = ReflectionUtils.safeNewInstance(Object.class, modelConstructor, simulatorId);

		if (modelInstance == null) {
			System.out.println("Simulator preinitialization failed");
			return;		
			
		}
		
		setModelInstance(modelInstance);
		// set some info 
		initializeInitializationScopeInstance(preinitializationInfo.getSimulatorCommonModelInfo());

		
		assertHasModel();


		for (Constructor<?> resourcePreinitializer : preinitializationInfo.resourcePreinitializerCreators) {
			Runnable runnableInstance = ReflectionUtils.safeNewInstance(Runnable.class, resourcePreinitializer, initializationScopeInstance);
			if (runnableInstance != null) {
				runnableInstance.run();
			}		
		}
	}
	

	@Override
	public void initialize(SimulatorInitializationInfo initializationInfo) {
		assertHasModel();

		executionStateNotifier = new Notifier<>(ExecutionState.class);
		dptManager = createDPTManager(initializationInfo.getDecisionPoints(), initializationScopeInstance);
		processManager = createProcess(initializationInfo.processBlocks, initializationScopeInstance);
		resultManager = createResultManager(initializationInfo.results, initializationScopeInstance);

		setTerminateConditions(initializationInfo);
		runInitializers(initializationInfo);

		database.addMemorizedResourceEntries(null, null, null);
	}

	private static DPTManager createDPTManager(List<Constructor<?>> decisionPointConstructors, Object initializationScopeInstance) {
		return new DPTManager(decisionPointConstructors.stream().map(constructor ->
		 ReflectionUtils.safeNewInstance(AbstractDecisionPoint.class, constructor, initializationScopeInstance))
		 .collect(Collectors.toList()));
	}

	private static Process createProcess(List<Block> processBlocks, Object initializationScopeInstance) {
		return new Process(processBlocks);
	}

	private static ResultManager createResultManager(List<Field> results, Object initializationScopeInstance) {
		return new ResultManager(results.stream().map(field -> ReflectionUtils.safeGet(AbstractResult.class, field, initializationScopeInstance))
		.collect(Collectors.toList()));
	}

	private void setTerminateConditions(SimulatorInitializationInfo initializationInfo) {
		initializationInfo.terminateConditions.stream()
		.map( constructor -> (Supplier<Boolean>) ReflectionUtils.safeNewInstance(Supplier.class, constructor, this.initializationScopeInstance))
		.forEach( supplier -> terminateList.add(supplier));
	} 

	private void runInitializers(SimulatorInitializationInfo initializationInfo) {
		initializationInfo.initList.stream().map(constructor -> 
			(Runnable) ReflectionUtils.safeNewInstance(Runnable.class, constructor, this.initializationScopeInstance)
		).forEach(Runnable::run);
	}

	private Database database;

	@Override
	public Database getDatabase() {
		return database;
	}

	private StaticModelData staticModelData;

	@Override
	public StaticModelData getStaticModelData() {
		return staticModelData;
	}

	private ModelState modelState;

	@Override
	public ModelState getModelState() {
		return modelState;
	}

	@Override
	public void setModelState(ModelState modelState) {
		this.modelState = modelState;
	}

	private volatile double time = 0;

	@Override
	public double getTime() {
		return time;
	}

	private EventScheduler eventScheduler = new EventScheduler();

	@Override
	public void pushEvent(Event event) {
		eventScheduler.pushEvent(event);
	}

	private List<Supplier<Boolean>> terminateList = new LinkedList<>();

	private DPTManager dptManager;

	private Process processManager;

	private ResultManager resultManager;

	@Override
	public List<AbstractResult<?>> getResults() {
		return resultManager.getResults();
	}

	private Logger logger;

	@Override
	public Logger getLogger() {
		return logger;
	}

	private Notifier<ExecutionState> executionStateNotifier;

	@Override
	public Notifier<ExecutionState> getExecutionStateNotifier() {
		return executionStateNotifier;
	}

	@Override
	public void notifyChange(ExecutionState category) {
		executionStateNotifier.notifySubscribers(category);
	}

	private volatile boolean executionAborted = false;

	@Override
	public void abortExecution() {
		executionAborted = true;
	}

	@Override
	public SimulationStopCode run() {
		database.addSystemEntry(Database.SystemEntryType.SIM_START);

		notifyChange(ExecutionState.EXECUTION_STARTED);
		notifyChange(ExecutionState.TIME_CHANGED);
		notifyChange(ExecutionState.STATE_CHANGED);

		while (!executionAborted) {
			if (checkTerminate())
				return SimulationStopCode.TERMINATE_CONDITION;

			if (dptManager.checkDPT()) {
				notifyChange(ExecutionState.STATE_CHANGED);
				continue;
			}

			ProcessStatus processStatus = processManager.scan();
			if (processStatus == ProcessStatus.SUCCESS) {
				notifyChange(ExecutionState.STATE_CHANGED);
				continue;
			} else if (processStatus == ProcessStatus.FAILURE) {
				return SimulationStopCode.RUNTIME_ERROR;
			}

			if (!eventScheduler.haveEvents())
				return SimulationStopCode.NO_MORE_EVENTS;

			Event event = eventScheduler.popEvent();
			time = event.getTime();
			event.run();

			notifyChange(ExecutionState.TIME_CHANGED);
			notifyChange(ExecutionState.STATE_CHANGED);
		}

		return SimulationStopCode.USER_INTERRUPT;
	}

	private boolean checkTerminate() {
		for (Supplier<Boolean> c : terminateList)
			if (c.get())
				return true;
		return false;
	}

	@Override
	public void setModelInstance(Object modelInstance) {
		this.modelInstance = modelInstance;
	}

	@Override
	public Object getModelInstance() {
		return modelInstance;
	}

	private void initializeInitializationScopeInstance(SimulatorCommonModelInfo info) {
		if (initializationScopeInstance == null) {
			initializationScopeInstance = SimulatorReflectionUtils.getInitializationField(getModelInstance(), info);
		}
	}

	@Override
	public void setSimulatorId(Integer simulatorId) {
		this.simulatorId = simulatorId;
	}

	@Override
	public Integer getSimulatorId() {
		return simulatorId;
	}
}
