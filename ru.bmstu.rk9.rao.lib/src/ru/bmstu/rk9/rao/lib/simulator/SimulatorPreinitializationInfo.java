package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

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
	
	public Class<?> modelClass;
	public Class<?> initializationScopeClass;
	
//	public final List<Runnable> resourcePreinitializers = new ArrayList<>();
	public final List<Function<Object, Runnable>> resourcePreinitializerCreators = new ArrayList<>();
	
	public static final JSONObject generateModelStructureStub() {
		return new JSONObject().put(ModelStructureConstants.NAME, "").put(ModelStructureConstants.NUMBER_OF_MODELS, 1)
				.put(ModelStructureConstants.RESOURCE_TYPES, new JSONArray())
				.put(ModelStructureConstants.RESULTS, new JSONArray())
				.put(ModelStructureConstants.PATTERNS, new JSONArray())
				.put(ModelStructureConstants.EVENTS, new JSONArray())
				.put(ModelStructureConstants.LOGICS, new JSONArray())
				.put(ModelStructureConstants.SEARCHES, new JSONArray());
	}
	

}
