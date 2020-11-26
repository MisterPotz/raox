package ru.bmstu.rk9.rao.lib.simulatormanager;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;

/**
 * Serves as a layer between clients (events, blocks, etc.) and concrete simulator instancexw
 *
 */
public interface ISimulatorManager {
	SimulatorId addSimulator(ISimulator iSimulator);
	ISimulator getSimulator(SimulatorId simulatorId);
}
