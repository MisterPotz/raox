package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
	public static <T> T safeNewInstance(Class<T> targetClass, Constructor<?> constructor, Object ... arguments) {
		try {
			Object newInstance = constructor.newInstance(arguments);
			if (newInstance.getClass().equals(targetClass)) {
				return (T) newInstance;
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T safeGet(Class<T> targetClass, Field field, Object object) {
		try {
			Object fieldObject = field.get(object);
			if (fieldObject.getClass().equals(targetClass)) {
				return (T) fieldObject;
			}
			return null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
