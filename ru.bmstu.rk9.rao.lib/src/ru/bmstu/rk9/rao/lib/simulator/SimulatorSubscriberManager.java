package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.notification.DefferedSubscriberManager;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class SimulatorSubscriberManager extends DefferedSubscriberManager<SimulatorSubscriberInfo>
		implements SimulatorDependent {

	public SimulatorSubscriberManager(SimulatorId simulatorId) {
		super(simulatorId);
	}

	public static class SimulatorSubscriberInfo {
		public SimulatorSubscriberInfo(Subscriber subscriber, SimulatorWrapper.ExecutionState notificationCategory) {
			this.subscriber = subscriber;
			this.notificationCategory = notificationCategory;
		}

		final Subscriber subscriber;
		final SimulatorWrapper.ExecutionState notificationCategory;
	}

	@Override
	protected void registerExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : subscribersInfo)
			getSimulatorWrapper().getExecutionStateNotifier().addSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : subscribersInfo)
			getSimulatorWrapper().getExecutionStateNotifier().removeSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}
}
