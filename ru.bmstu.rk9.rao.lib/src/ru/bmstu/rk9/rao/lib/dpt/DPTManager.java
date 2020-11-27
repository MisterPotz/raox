package ru.bmstu.rk9.rao.lib.dpt;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorDependent;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;

public class DPTManager implements Subscriber, SimulatorDependent {
	private final SimulatorId simulatorId;

	@Override
	public SimulatorId getSimulatorId() {
		return simulatorId;
	}
	
	public DPTManager(Collection<AbstractDecisionPoint> decisionPoints, SimulatorId simulatorId) {
		SimulatorManagerImpl.getInstance().getSimulator(simulatorId).getExecutionStateNotifier().addSubscriber(this,
				SimulatorWrapper.ExecutionState.EXECUTION_ABORTED, EnumSet.of(SubscriptionType.ONE_SHOT));
		this.simulatorId = simulatorId;
		this.decisionPoints = new TreeSet<AbstractDecisionPoint>(prioritizer);
		this.decisionPoints.addAll(decisionPoints);
	}

	private final SortedSet<AbstractDecisionPoint> decisionPoints;

	volatile private boolean dptAllowed = true;

	@Override
	public void fireChange() {
		this.dptAllowed = false;
	}

	public boolean checkDPT() {
		Iterator<AbstractDecisionPoint> dptIterator = decisionPoints.iterator();

		while (dptIterator.hasNext() && dptAllowed)
			if (dptIterator.next().check())
				return true;

		return false;
	}

	public static Comparator<AbstractDecisionPoint> prioritizer = new Comparator<AbstractDecisionPoint>() {
		@Override
		public int compare(AbstractDecisionPoint x, AbstractDecisionPoint y) {
			if (x.getPriority() == null)
				return 1;

			if (y.getPriority() == null)
				return -1;

			if (x.getPriority().get() > y.getPriority().get())
				return -1;

			if (x.getPriority().get() <= y.getPriority().get())
				return 1;

			return 0;
		}
	};

}
