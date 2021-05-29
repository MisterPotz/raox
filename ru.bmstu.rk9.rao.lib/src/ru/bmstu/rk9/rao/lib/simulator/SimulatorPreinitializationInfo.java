package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.;
import java.util.stream.Collectors;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.ModelStructureConstants;
import ru.bmstu.rk9.rao.lib.process.Transact;

/**
 * Available RaoEntities in the class of user model
 */
public class SimulatorPreinitializationInfo {
	public SimulatorPreinitializationInfo() {
		modelStructure = generateModelStructureStub();
		resourceClasses.add(Transact.class);
	}

	public final JSONObject modelStructure;
	public final List<Class<?>> resourceClasses = new ArrayList<>();
	
	private SimulatorCommonModelInfo simulatorCommonModelInfo;
	
//	public final List<Runnable> resourcePreinitializers = new ArrayList<>();
	public List<Constructor<?>> resourcePreinitializerCreators;
	
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
		this.resourcePreinitializerCreators = findResourcePreinitializerClasses(info.getInitializationScopeClass());
	}

	private List<Constructor<?>> findResourcePreinitializerClasses(Class<?> initializationScopeClass) {
		return Arrays.asList(initializationScopeClass.getDeclaredClasses())
		.stream()
		.filter(clazz -> clazz.getSimpleName().equals("resourcesPreinitializer"))
		.map(clazz -> {
			try {
				return clazz.getDeclaredConstructor(initializationScopeClass);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
	}
}
