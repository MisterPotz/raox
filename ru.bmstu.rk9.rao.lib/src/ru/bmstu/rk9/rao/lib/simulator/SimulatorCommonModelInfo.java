package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import ru.bmstu.rk9.rao.lib.contract.RaoGenerationContract;


public class SimulatorCommonModelInfo {
    private Class<?> modelClass;
	private Class<?> initializationScopeClass;
	private Field initializationScopeField;


    public void setModelClass(Class<?> modelClass) {
		this.modelClass = modelClass;
		this.initializationScopeClass = findInitializationScopeClass(modelClass);
		this.initializationScopeField = findInitializationScopeField(modelClass);
	}

	private static Class<?> findInitializationScopeClass(Class<?> modelClass) {
		Optional<Class<?>> optionalClass = Arrays.asList(modelClass.getDeclaredClasses())
		.stream().filter(clazz -> clazz.getSimpleName().equals(RaoGenerationContract.INITIALIZATION_SCOPE_CLASS)).findFirst();

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