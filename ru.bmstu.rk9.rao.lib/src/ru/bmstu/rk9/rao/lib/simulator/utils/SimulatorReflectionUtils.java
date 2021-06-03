package ru.bmstu.rk9.rao.lib.simulator.utils;

import ru.bmstu.rk9.rao.lib.simulator.ReflectionUtils;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorCommonModelInfo;

public class SimulatorReflectionUtils {
    private SimulatorReflectionUtils() {

    }

    public static Object getInitializationField(Object modelInstance, SimulatorCommonModelInfo info) {
        return ReflectionUtils.safeGet(Object.class, info.getInitializationScopeField(), modelInstance);
    }
}
