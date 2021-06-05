package ru.bmstu.rk9.rao.lib.simulatormanager;

import java.util.List;

import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.SimulatorState;

/**
 * Serves as a layer between clients (events, blocks, etc.) and concrete simulator instances
 *
 */
public interface ISimulatorManager extends Listenable {
	SimulatorId addSimulator(ISimulator iSimulator);
	/**
	 * there may be cases in runtime when we have initialized the simulator but haven't yet initialized the wrapper.
	 * In that case this method is useful.
	 */
	ISimulator getSimulator(SimulatorId simulatorId);
	SimulatorWrapper getSimulatorWrapper(SimulatorId simulatorId);
	SimulatorId addSimulatorWrapper(SimulatorWrapper simulatorWrapper);
	List<SimulatorId> getAvailableIds();
	
	
	public enum SystemState {
		OFF /* no simulators running */, ON /* at least on simulator is up (executed or executing) */
	}
	
	public final class SystemUpdateEvent implements Listener.Event {
		private SystemState oldState;
		private SystemState newState;
		private boolean posterior;
		public SystemUpdateEvent(SystemState oldState, SystemState newState, boolean posterior) {
			super();
			this.oldState = oldState;
			this.newState = newState;
			this.posterior = posterior;
		}
		public SystemState getOldState() {
			return oldState;
		}
		public SystemState getNewState() {
			return newState;
		}
		@Override
		public boolean isPosterior() {
			return posterior;
		}
		@Override
		public Object getTag() {
			if (oldState == null && newState != null) {
				return SystemState.ON;
			}
			return SystemState.OFF;
		}
	}
}
