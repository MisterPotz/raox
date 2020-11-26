package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.notification.DefferedSubscriberManager;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;

public class SimulatorSubscriberManager extends DefferedSubscriberManager<SimulatorSubscriberInfo> {
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
			SimulatorWrapper.getExecutionStateNotifier().addSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : subscribersInfo)
			SimulatorWrapper.getExecutionStateNotifier().removeSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}
}
