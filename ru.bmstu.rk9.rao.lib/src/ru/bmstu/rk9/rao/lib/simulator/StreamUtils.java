package ru.bmstu.rk9.rao.lib.simulator;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class StreamUtils {
	static List<Constructor<?>> findConstructorsForClasses(Class<?> classToSearchIn, Predicate<Class<?>> predicate) {
		return Arrays.asList(classToSearchIn.getDeclaredClasses()).stream()
		.filter(predicate)
		.map(cl -> {
			Constructor<?> constructor;
			try {
				constructor = cl.getDeclaredConstructor(classToSearchIn);			
				constructor.setAccessible(true);
				return constructor;
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());
	}

	static List<Constructor<?>> convert(List<Class<?>> toConvert, Function<Class<?>, Constructor<?>> mapper) {
		return toConvert.stream().map(mapper).filter(Objects::nonNull).collect(Collectors.toList());
	}
}
