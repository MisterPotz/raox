package ru.bmstu.rk9.rao.lib.runtime;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;

public class LoggerExtensions {
	public static void log(Object object) {
		SimulatorWrapper.getLogger().log(object);
	}
}
