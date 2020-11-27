package ru.bmstu.rk9.rao.lib.simulatormanager;

import java.util.List;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;

/**
 * Serves as a layer between clients (events, blocks, etc.) and concrete simulator instances
 *
 */
public interface ISimulatorManager {
	SimulatorId addSimulator(ISimulator iSimulator);
	/**
	 * there may be cases in runtime when we have initialized the simulator but haven't yet initialized the wrapper.
	 * In that case this method is useful.
	 */
	ISimulator getSimulator(SimulatorId simulatorId);
	SimulatorWrapper getSimulatorWrapper(SimulatorId simulatorId);
	SimulatorId addSimulatorWrapper(SimulatorWrapper simulatorWrapper);
	List<SimulatorId> getAvailableIds();
}
