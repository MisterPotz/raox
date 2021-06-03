package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.process.Block;

public class SimulatorInitializationInfo {
	public final /* Runnable */ List<Constructor<?>> initList = new ArrayList<>();
	public final /* Supplier<Boolean> */ List<Constructor<?>> terminateConditions = new ArrayList<>();
	private final /* AbstractDecisionPoint */ List<Constructor<?>> decisionPoints = new ArrayList<>();
	public final /* Block */ List<Block> processBlocks = new ArrayList<>();
	public final /* AbstractResult */ List<Field> results = new ArrayList<>();

	private SimulatorCommonModelInfo simulatorCommonModelInfo;

	public SimulatorCommonModelInfo getSimulatorCommonModelInfo() {
		return simulatorCommonModelInfo;
	}

	public void setSimulatorCommonModelInfo(SimulatorCommonModelInfo simulatorCommonModelInfo) {
		this.simulatorCommonModelInfo = simulatorCommonModelInfo;
		this.initList.addAll(findInitClassesList(simulatorCommonModelInfo));
		this.terminateConditions.addAll(findTerminateConditions(simulatorCommonModelInfo));
	}

	public void setDecisionPointClasses(List<Class<?>> decisionPointClasses) {
		decisionPoints.clear();
		decisionPoints.addAll(StreamUtils.convert(decisionPointClasses, cl -> {
			try {
				Constructor<?> constructor = cl.getConstructor(simulatorCommonModelInfo.getInitializationScopeClass());
				constructor.setAccessible(true);
				return constructor;
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}));
	}

	public void setResultFields(List<Field> fields) {
		results.clear();
		for (Field i : fields) {
			i.setAccessible(true);
		}
		results.addAll(fields);
	}

	private static List<Constructor<?>> findTerminateConditions(SimulatorCommonModelInfo info) {
		/**
		 * 27/03/2021 the following lines of code collect model declared methods that
		 * change static values of model class and interact with CurrentSimulator
		 */
		/**
		 * init method of the model, when called the state is changed and first event is
		 * planned
		 */
		return StreamUtils.findConstructorsForClasses(info.getInitializationScopeClass(),
				cl -> cl.getSimpleName().equals("terminateCondition"));
	}

	private static List<Constructor<?>> findInitClassesList(SimulatorCommonModelInfo simulatorCommonModelInfo) {
		return StreamUtils.findConstructorsForClasses(simulatorCommonModelInfo.getInitializationScopeClass(),
				c -> c.getSimpleName().equals("init"));
	}

	public List<Block> getProcessBlocks() {
		return processBlocks;
	}

	public List<Constructor<?>> getDecisionPoints() {
		return decisionPoints;
	}
}
