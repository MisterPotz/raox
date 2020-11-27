package ru.bmstu.rk9.rao.ui;

import ru.bmstu.rk9.rao.ui.Listener.Event;

public interface Listenable<R extends Event, T extends Listener<R>> {
	void registerListener(T listener);
	void deleteListener(T listener);
	void notifyListeners(R event);
}
 