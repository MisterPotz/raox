package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
	public static <T> T safeNewInstance(Class<T> targetClass, Constructor<?> constructor, Object ... arguments) {
		try {
			constructor.setAccessible(true);
			Object newInstance = constructor.newInstance(arguments);
			if (newInstance != null && targetClass.isAssignableFrom(newInstance.getClass())) {
				return (T) newInstance;
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Constructor<?> safeGetConstructor(Class<?> clazz, Class<?> ... constructorArguments) {
		try {
			return clazz.getDeclaredConstructor(constructorArguments);
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T safeGet(Class<T> targetClass, Field field, Object object) {
		try {
			field.setAccessible(true);
			Object fieldObject = field.get(object);
			if (fieldObject != null && targetClass.isAssignableFrom(fieldObject.getClass())) {
				return (T) fieldObject;
			}
			return null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
