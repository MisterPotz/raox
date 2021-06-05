package ru.bmstu.rk9.rao.ui;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager;
import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager.SystemState;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listenable;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listener;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;

public interface TargetSimulatorManager extends Listenable {
	SimulatorWrapper getTargetSimulatorWrapper();

	boolean isTargetSimulatorOn();

	void switchTargetSimulator(SimulatorId simulatorId);

	ISimulatorManager getSimulatorManager();

	SystemState getState();
	
	public static enum SwitchEvent {
		APRIORI, POSTERIOR
	}

	/**
	 *
	 *	The notification of this event can happen in 2 stages:
	 *	1. aposteriori - the switch is about to happen and subscribers can unsubscribe before that happens
	 *	2. posterior - the switch is happened and the state is changed
	 */
	public final class TargetSimulatorSwitchEvent implements Listener.Event {
		private SimulatorId newSimulatorId;
		private SimulatorId oldSimulatorId;
		// true if the replacement of simulators happened
		private boolean posterior;

		public TargetSimulatorSwitchEvent(SimulatorId newSimulatorId, SimulatorId oldSimulatorId, boolean posterior) {
			super();
			this.newSimulatorId = newSimulatorId;
			this.oldSimulatorId = oldSimulatorId;
			this.posterior = posterior;
		}

		public SimulatorId getNewSimulatorId() {
			return newSimulatorId;
		}

		public SimulatorId getOldSimulatorId() {
			return oldSimulatorId;
		}

		public boolean isPosterior() {
			return posterior;
		}

		@Override
		public Object getTag() {
			if (posterior) {
				return SwitchEvent.POSTERIOR;
			}
			return SwitchEvent.APRIORI;
		}
	}
}
