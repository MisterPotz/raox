package ru.bmstu.rk9.rao.lib.simulatormanager;
import ru.bmstu.rk9.rao.lib.simulatormanager.Listener.Event;

public interface Listener {
	public interface Event {
		boolean isPosterior();
		Object getTag();
	}

	void update(Event event);
}
