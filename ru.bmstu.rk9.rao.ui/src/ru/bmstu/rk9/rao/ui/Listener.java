package ru.bmstu.rk9.rao.ui;
import ru.bmstu.rk9.rao.ui.Listener.Event;

public interface Listener<T extends Event> {
	public interface Event { }

	void notify(T eventType);
}
