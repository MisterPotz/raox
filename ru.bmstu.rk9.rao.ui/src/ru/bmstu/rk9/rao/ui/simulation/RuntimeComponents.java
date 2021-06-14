package ru.bmstu.rk9.rao.ui.simulation;

import ru.bmstu.rk9.rao.ui.notification.RealTimeUpdater;

public class RuntimeComponents {
	public static RealTimeUpdater realTimeUpdater = null;
	public static SimulationSynchronizer simulationSynchronizer = null;

	private static boolean isInitialized = false;

	public static final boolean isInitialized() {
		return isInitialized;
	}
	
	public static final void initialize() {
		realTimeUpdater = new RealTimeUpdater();
		simulationSynchronizer = new SimulationSynchronizer();
		isInitialized = true;
	}

	public static final void deinitialize() {
		isInitialized = false;
		realTimeUpdater.deinitialize();
		simulationSynchronizer.deinitializeSubscribers();
	}

//	public static final void initialize() {
//		// subscribe only when simulator becomes available (switched on)
//		listener.asSimulatorOnAndOnPostChange((event) -> {
//			realTimeUpdater = new RealTimeUpdater();
//			simulationSynchronizer = new SimulationSynchronizer();
//			isInitialized = true;
//		});
//		listener.asSimulatorPreOffAndPreChange((event) -> {
//			deinitialize();
//		});
//	}
//
//	public static final void deinitialize() {
//		isInitialized = false;
//		if (realTimeUpdater != null) {
//			realTimeUpdater.deinitialize();
//		}
//		if (simulationSynchronizer != null) {
//			simulationSynchronizer.deinitializeSubscribers();
//		}
//		realTimeUpdater = null;
//		simulationSynchronizer = null;
//	}
}
