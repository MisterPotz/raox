package ru.bmstu.rk9.rao.lib.simulatormanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ru.bmstu.rk9.rao.lib.simulatormanager.Listener.Event;

public interface Listenable {
	void addListener(Listener listener);

	void removeListener(Listener listener);

	void notifyListeners(Event event);

	public class SimpleListenable implements Listenable {
		private final List<Listener> listeners;

		public SimpleListenable() {
			listeners = new ArrayList<Listener>();
		}
		
		@Override
		public void addListener(Listener listener) {
			listeners.add(listener);		
		}

		@Override
		public void removeListener(Listener listener) {
			listeners.remove(listener);		
		}

		@Override
		public void notifyListeners(Event event) {
			for (Listener listener : listeners) {
				listener.update(event);
			}		
		}
	}
}
