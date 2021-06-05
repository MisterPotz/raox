package ru.bmstu.rk9.rao.lib.event;

import java.util.PriorityQueue;
import java.util.Comparator;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class EventScheduler implements SimulatorDependent {
	private final SimulatorId simulatorId;

	@Override
	public SimulatorId getSimulatorId() {
	return simulatorId;
	}

	private ISimulator getSimulator() {
	return SimulatorManagerImpl.getInstance().getSimulator(simulatorId);
	}

	private SimulatorWrapper getSimulatorWrapper() {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}
	
	public EventScheduler(SimulatorId simulatorId) {
		this.simulatorId = simulatorId;
	}
	
	private static Comparator<Event> comparator = new Comparator<Event>() {
		@Override
		public int compare(Event x, Event y) {
			if (x.getTime() < y.getTime())
				return -1;
			if (x.getTime() > y.getTime())
				return 1;
			return 0;
		}
	};

	private PriorityQueue<Event> eventList = new PriorityQueue<Event>(1, comparator);

	public void pushEvent(Event event) {
		if (event.getTime() >= getSimulator().getTime())
			eventList.add(event);
	}

	public Event popEvent() {
		return eventList.poll();
	}

	public boolean haveEvents() {
		return !eventList.isEmpty();
	}
}
