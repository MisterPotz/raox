package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import ru.bmstu.rk9.rao.lib.contract.RaoGenerationContract;
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
	
	private Class<?> modelClass;
	private Class<?> initializationScopeClass;
	private Field initializationScopeField;
	
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
	
	public void setModelClass(Class<?> modelClass) {
		this.modelClass = modelClass;
		this.initializationScopeClass = findInitializationScopeClass(modelClass);
		this.initializationScopeField = findInitializationScopeField(modelClass);
	}

	private static Class<?> findInitializationScopeClass(Class<?> modelClass) {
		Optional<Class<?>> optionalClass = Arrays.asList(modelClass.getDeclaredClasses())
		.stream().filter(clazz -> clazz.getName().equals(RaoGenerationContract.INITIALIZATION_SCOPE_CLASS)).findFirst();

		if (optionalClass.isPresent()) {
			return optionalClass.get();
		}
		return null;
	}

	private static Field findInitializationScopeField(Class<?> modelClass) {
		Optional<Field> optionalField = Arrays.asList(modelClass.getDeclaredFields())
		.stream().filter(field -> field.getName().equals(RaoGenerationContract.INITIALIZATION_SCOPE_FIELD))
		.findFirst();
		
		if (optionalField.isPresent()) {
			return optionalField.get();
		} else {
			return null;
		}
	}

	public Class<?> getModelClass() {
		return modelClass;
	}

	public Class<?> getInitializationScopeClass() {
		return initializationScopeClass;
	}

	public Field getInitializationScopeField() {
		return initializationScopeField;
	}
}
