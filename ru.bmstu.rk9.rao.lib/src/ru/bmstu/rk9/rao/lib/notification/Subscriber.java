package ru.bmstu.rk9.rao.lib.notification;

public interface Subscriber {
	public void fireChange();

	default boolean acceptsPayload() {
		return false;
	}

	default void fireChangeWithPayload(/* NonNull */  Object object) {

	}
}
