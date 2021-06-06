package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;

public class Notifier<T extends Enum<T>> {

	private class SubscriptionState {
		private final Subscription subscription = new Subscription();
		private boolean hadNotifications = false;
		private Object latestPayload = null;

		private void updateState(Boolean hadNotifications, Object latestPayload) {
			this.hadNotifications = hadNotifications;
			this.latestPayload = latestPayload;
		}
	}

	private final Map<T, SubscriptionState> subscriptionStates;

	public Notifier(Class<T> enumClass) {
		subscriptionStates = new ConcurrentHashMap<T, SubscriptionState>();

		for (T category : enumClass.getEnumConstants()) {
			subscriptionStates.put(category, new SubscriptionState());
		}
	}

	public void notifySubscribers(T category) {
		notifySubscribers(category, null);
	}

	public void notifySubscribers(T category, Object payload) {
		SubscriptionState subscriptionState = subscriptionStates.get(category);
		Subscription subscription = subscriptionState.subscription;

		for (Subscriber subscriber : subscription.subscribers.keySet()) {
			if (subscription.subscribers.get(subscriber).contains(SubscriptionType.ONE_SHOT))
				subscription.removeSubscriber(subscriber);
			
			notifySubscriber(subscriber, payload);
		}

		subscriptionState.hadNotifications = true;
		subscriptionState.updateState(true, payload);
	}

	/**
	 * don't fire event with payload if subscriber doesn't accept payload or if payload is null
	 */
	private void notifySubscriber(Subscriber subscriber, Object payload) {
		if (subscriber.acceptsPayload() && payload != null) {
			subscriber.fireChangeWithPayload(payload);
		} else if (!subscriber.acceptsPayload()) {
			subscriber.fireChange();
		}
	}

	public void addSubscriber(Subscriber subscriber, T category) {
		addSubscriber(subscriber, category, EnumSet.noneOf(SubscriptionType.class));
	}

	public void addSubscriber(Subscriber subscriber, T category, EnumSet<SubscriptionType> subscriptionFlags) {
		SubscriptionState subscriptionState = subscriptionStates.get(category);

		subscriptionState.subscription.addSubscriber(subscriber, subscriptionFlags);

		if (!subscriptionFlags.contains(SubscriptionType.IGNORE_ACCUMULATED) && subscriptionState.hadNotifications)
			notifySubscriber(subscriber, subscriptionState.latestPayload);
	}

	public void removeSubscriber(Subscriber subscriber, T category) {
		subscriptionStates.get(category).subscription.removeSubscriber(subscriber);
	}

	public void removeAllSubscribers(T category) {
		subscriptionStates.get(category).subscription.subscribers.clear();
	}
}
