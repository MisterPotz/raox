package ru.bmstu.rk9.rao.ui.notification;

import ru.bmstu.rk9.rao.lib.notification.DefferedSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.RaoSimulatorHelper;
import ru.bmstu.rk9.rao.ui.simulation.RuntimeComponents;
import ru.bmstu.rk9.rao.ui.simulation.UiSimulatorDependent;

public class RealTimeSubscriberManager extends DefferedSubscriberManager<Runnable>{
	public RealTimeSubscriberManager() {
		super(RaoSimulatorHelper.getTargetSimulatorId());
	}

	@Override
	protected void registerExecutionSubscribers() {
		for (Runnable runnable : subscribersInfo)
			RuntimeComponents.realTimeUpdater.addScheduledAction(runnable);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (Runnable runnable : subscribersInfo)
			RuntimeComponents.realTimeUpdater.removeScheduledAction(runnable);
	}
}
