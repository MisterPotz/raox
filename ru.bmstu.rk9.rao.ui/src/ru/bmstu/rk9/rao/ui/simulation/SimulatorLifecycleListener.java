package ru.bmstu.rk9.rao.ui.simulation;

import java.util.HashMap;

import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager.SystemState;
import ru.bmstu.rk9.rao.lib.simulatormanager.ISimulatorManager.SystemUpdateEvent;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listener;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;
import ru.bmstu.rk9.rao.ui.TargetSimulatorManager.SwitchEvent;
import ru.bmstu.rk9.rao.ui.TargetSimulatorManager.TargetSimulatorSwitchEvent;

/**
 *
 *	Helper manager that is aware of available simulators lifecycle and able to perfrom actions based 
 *	on the lifecycle
 */
public class SimulatorLifecycleListener implements Listener {
	private OnEventReceiver onAvailable;
	private OnEventReceiver onNotAvailable;
	private HashMap<Object, OnEventReceiver> receivers;
	
	public SimulatorLifecycleListener() {
		this.receivers = new HashMap<Object, OnEventReceiver>();
		RaoActivatorExtension.getTargetSimulatorManager().addListener(this);
	}
	
	public void onSimulatorAvailable(OnEventReceiver onAvailable) {
		receivers.put(SystemState.ON, onAvailable);
	}
	
	public void onSimulatorNotAvailable(OnEventReceiver onNotAvailableReceiver) {
		receivers.put(SystemState.OFF, onAvailable);
	}
	
	public void asSimulatorOnAndOnPostChange(OnEventReceiver onEventReceiver) {
		onSimulatorAvailable(onEventReceiver);
		onSimulatorChangePosterior(onEventReceiver);
	}
	
	public void asSimulatorPreOffAndPreChange(OnEventReceiver onEventReceiver) {
		onSimulatorChangeApriori(onEventReceiver);
		onSimulatorNotAvailable(onEventReceiver);
	}
	
	public void onSimulatorChangeApriori(OnEventReceiver onPreChange) {
		receivers.put(SwitchEvent.APRIORI, onPreChange);
	}
	
	public void onSimulatorChangePosterior(OnEventReceiver onPostChange) {
		receivers.put(SwitchEvent.POSTERIOR, onPostChange);
	}
	
	@Override
	public void update(Event event) {
		Object eventType = event.getTag();
		findReceiverAndInvoke(eventType, event);
	}
	
	private void findReceiverAndInvoke(Object eventType, Event event) {
		OnEventReceiver receiver = receivers.get(eventType);
		if (receiver != null) {
			receiver.receive(event);
		}
	}
	
	public interface OnEventReceiver {
		void receive(Event event);
	}
}