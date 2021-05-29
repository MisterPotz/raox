package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.process.Transact;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;

/**
 * Available RaoEntities in the class of user model
 */
public class SimulatorPreinitializationInfo {
	public final JSONObject modelStructure;
	public final List<Class<?>> resourceClasses = new ArrayList<>();

	private SimulatorCommonModelInfo simulatorCommonModelInfo;
	
	public final List<Constructor<?>> resourcePreinitializerCreators = new ArrayList<>();

	public SimulatorPreinitializationInfo() {
		modelStructure = generateModelStructureStub();
		resourceClasses.add(Transact.class);
	}
	
	public static final JSONObject generateModelStructureStub() {
		return new JSONObject().put(ModelStructureConstants.NAME, "").put(ModelStructureConstants.NUMBER_OF_MODELS, 1)
				.put(ModelStructureConstants.RESOURCE_TYPES, new JSONArray())
				.put(ModelStructureConstants.RESULTS, new JSONArray())
				.put(ModelStructureConstants.PATTERNS, new JSONArray())
				.put(ModelStructureConstants.EVENTS, new JSONArray())
				.put(ModelStructureConstants.LOGICS, new JSONArray())
				.put(ModelStructureConstants.SEARCHES, new JSONArray());
	}
	
	public void setSimulatorCommonModelInfo(SimulatorCommonModelInfo info) {
		this.simulatorCommonModelInfo = info;
		this.resourcePreinitializerCreators.clear();
		this.resourcePreinitializerCreators.addAll(findResourcePreinitializerClasses());
		this.resourceClasses.clear();
		this.resourceClasses.addAll(findResourceClasses());
	}

	public SimulatorCommonModelInfo getSimulatorCommonModelInfo() {
		return simulatorCommonModelInfo;
	}

	private List<Constructor<?>> findResourcePreinitializerClasses() {
		return StreamUtils.findConstructorsForClasses(simulatorCommonModelInfo.getInitializationScopeClass(), clazz -> clazz.getSimpleName().equals("resourcesPreinitializer"));
	}

	private List<Class<?>> findResourceClasses() {
		return Arrays.asList(simulatorCommonModelInfo.getModelClass().getDeclaredClasses()).stream().filter(clazz -> ComparableResource.class.isAssignableFrom(clazz)).collect(Collectors.toList());
	}
}
