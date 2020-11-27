package ru.bmstu.rk9.rao.lib.simulatormanager;

public interface SimulatorDependent {
	/**
	 * @return the id of instance of simulator that is connected to the realization of this interface
	 */
	SimulatorId getSimulatorId();
	default void setSimulatorId(SimulatorId simulatorId) {
	}
}
