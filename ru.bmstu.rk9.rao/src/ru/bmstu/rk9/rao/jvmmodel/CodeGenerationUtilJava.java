package ru.bmstu.rk9.rao.jvmmodel;

import java.util.function.Function;
import java.util.List;

public class CodeGenerationUtilJava {
    public static <T> String createEnumerationString(List<T> objects, Function<T, String> fun) {
		int i = 0;
		StringBuilder builder = new StringBuilder();
		for (T o: objects) {
			builder.append(fun.apply(o));
			if (i < objects.size() - 1) {
				builder.append(",");
			}
			i++;
		}

		return builder.toString();
	}
}
