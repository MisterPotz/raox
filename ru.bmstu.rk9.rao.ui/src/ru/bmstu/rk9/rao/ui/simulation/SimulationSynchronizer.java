package ru.bmstu.rk9.rao.ui.simulation;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.RaoActivatorExtension;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;

public class SimulationSynchronizer {
	private SimulatorSubscriberManager simulationSubscriberManager;
	private volatile boolean simulationAborted = false;
	public UITimeUpdater uiTimeUpdater;
	public SimulationManager simulationManager;
	public final ExecutionAbortedListener executionAbortedListener = new ExecutionAbortedListener();
	public final ExecutionStartedListener executionStartedListener = new ExecutionStartedListener();
	
	public SimulationSynchronizer() {
		this.simulationSubscriberManager = null;
		this.uiTimeUpdater = null;
		initializeSubscribers();
	}
	
	private final void initializeSubscribers() {
		// TODO refactor-0001
//		listener.asSimulatorOnAndOnPostChange((event) -> {
//			if (simulationSubscriberManager == null) {
//				simulationSubscriberManager = new SimulatorSubscriberManager(RaoSimulatorHelper.getTargetSimulatorId());
//			}
//			if (simulationManager == null) {
//				simulationManager = new SimulationManager();
//			}
//			if (uiTimeUpdater == null) {
//				uiTimeUpdater = new UITimeUpdater();
//			}
//			simulationSubscriberManager.initialize(
//					Arrays.asList(new SimulatorSubscriberInfo(simulationManager.scaleManager, ExecutionState.TIME_CHANGED),
//							new SimulatorSubscriberInfo(simulationManager.speedManager, ExecutionState.STATE_CHANGED),
//							new SimulatorSubscriberInfo(simulationManager.speedManager, ExecutionState.SEARCH_STEP),
//							new SimulatorSubscriberInfo(executionAbortedListener, ExecutionState.EXECUTION_ABORTED),
//							new SimulatorSubscriberInfo(executionStartedListener, ExecutionState.EXECUTION_STARTED)));
//		});
//		listener.asSimulatorPreOffAndPreChange((event) -> {
//			deinitializeSubscribers();
//		});
	}

	public final void deinitializeSubscribers() {
		if (simulationSubscriberManager != null) {
			simulationSubscriberManager.deinitialize();
		}
		if (uiTimeUpdater != null) {
			uiTimeUpdater.deinitializeSubscribers();
		}
		simulationSubscriberManager = null;
		simulationManager = null;
		uiTimeUpdater = null;
	}

	private volatile ExecutionMode executionMode;

	public void setExecutionMode(ExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	public class UITimeUpdater {
		private double actualTimeScale = 0;
		private DecimalFormat scaleFormatter = new DecimalFormat("0.######");
		private DecimalFormat timeFormatter = new DecimalFormat("0.0#####");
		private SimulatorSubscriberManager simulatorSubscriberManager;
		private RealTimeSubscriberManager realTimeSubscriberManager;
		Subscriber commonSubscriber = new Subscriber() {
			@Override
			public void fireChange() {
				PlatformUI.getWorkbench().getDisplay().asyncExec(updater);
			}
		};
		private Runnable updater = () -> {
			// TODO refactor-0001
//			StatusView.setValue("Simulation time".intern(), 20, timeFormatter.format(currentSimulatorWrapper.getTime()));
//			StatusView.setValue("Actual scale".intern(), 10, scaleFormatter.format(60060d / actualTimeScale));
		};
		
		UITimeUpdater() {
//			initializeSubscribers(RaoSimulatorHelper.getTargetSimulatorId());
		}

		private final void initializeSubscribers(SimulatorId simulatorId) {
			if (simulationSubscriberManager == null) {
				simulationSubscriberManager = new SimulatorSubscriberManager(simulatorId);
			}
			if (realTimeSubscriberManager == null) {
				realTimeSubscriberManager = new RealTimeSubscriberManager(simulatorId);
			}
			simulatorSubscriberManager.initialize(
					Arrays.asList(new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_STARTED),
							new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_COMPLETED)));
			realTimeSubscriberManager.initialize(Arrays.asList(updater));
		}

		private final void deinitializeSubscribers() {
			simulatorSubscriberManager.deinitialize();
			realTimeSubscriberManager.deinitialize();
			simulationSubscriberManager = null;
			realTimeSubscriberManager = null;
		}
	}

	public void setSimulationSpeed(int value) {
		if (value < 1 || value > 100)
			throw new SimulationComponentsException("Incorrect simulation speed value " + value);
		if (simulationManager != null) {
			simulationManager.speedDelayMillis = (long) (-Math.log10(value / 100d) * 1000d);
		}
	}

	public void setSimulationScale(double value) {
		simulationManager.timeScale = 60060d / value;
		simulationManager.startRealTime = System.currentTimeMillis();
		// TODO refactor-0001
//		SimulatorWrapper currentSimulator = RaoActivatorExtension.getTargetSimulatorManager().getTargetSimulatorWrapper();
//		simulationManager.startSimulationTime = currentSimulator.isRunning() ? currentSimulator.getTime() : 0;
	}

	private void delay(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public class ExecutionAbortedListener implements Subscriber {
		@Override
		public void fireChange() {
			SimulationSynchronizer.this.simulationAborted = true;
		}
	}

	public class ExecutionStartedListener implements Subscriber {
		@Override
		public void fireChange() {
			SimulationSynchronizer.this.simulationAborted = false;
			setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
			setSimulationSpeed(SpeedSelectionToolbar.getSpeed());
			setExecutionMode(SimulationModeDispatcher.getMode());
		}
	}

	// TODO refactor-0001
	public class SimulationManager {
		private volatile double timeScale = 0.3;
		public final SpeedManager speedManager = new SpeedManager();
		public final ScaleManager scaleManager = new ScaleManager();
		private long startRealTime;
		private double startSimulationTime;
		private volatile long speedDelayMillis = 0;
//		private SimulatorWrapper currentSimulatorWrapper = RaoActivatorExtension.getTargetSimulatorManager().getTargetSimulatorWrapper();

		private void processPause() {
			while (executionMode == ExecutionMode.PAUSE && !simulationAborted)
				delay(25);

			updateTimes();
		}

		private void updateTimes() {
			startRealTime = System.currentTimeMillis();
//			startSimulationTime = currentSimulatorWrapper.getTime();
		}
		
		public class ScaleManager implements Subscriber {
			@Override
			public void fireChange() {
//				double currentSimulationTime = currentSimulatorWrapper.getTime();
//				long currentRealTime = System.currentTimeMillis();
//
//				if (currentSimulationTime != 0) {
//					switch (executionMode) {
//					case PAUSE:
//						processPause();
//						break;
//
//					case NORMAL_SPEED:
//						long waitTime = (long) ((currentSimulationTime - startSimulationTime) * timeScale)
//								- (currentRealTime - startRealTime);
//
//						if (waitTime > 0) {
//							while (executionMode == ExecutionMode.NORMAL_SPEED && waitTime > 0 && !simulationAborted) {
//								delay(waitTime > 50 ? 50 : waitTime);
//								waitTime = (long) ((currentSimulationTime - startSimulationTime) * timeScale)
//										- (System.currentTimeMillis() - startRealTime);
//							}
//							uiTimeUpdater.actualTimeScale = timeScale;
//						} else
//							uiTimeUpdater.actualTimeScale = (currentRealTime - startRealTime) / currentSimulationTime;
//						break;
//
//					default:
//						uiTimeUpdater.actualTimeScale = 0;
//						updateTimes();
//					}
//				}
			}
		}

		public class SpeedManager implements Subscriber {
			@Override
			public void fireChange() {
				switch (executionMode) {
				case PAUSE:

					processPause();

					break;

				case FAST_FORWARD:
				case NORMAL_SPEED:

					long startTime = System.currentTimeMillis();

					long timeToWait = speedDelayMillis;
					while (timeToWait > 0 && (executionMode == ExecutionMode.FAST_FORWARD
							|| executionMode == ExecutionMode.NORMAL_SPEED) && !simulationAborted) {
						delay(timeToWait > 50 ? 50 : timeToWait);
						timeToWait = startTime + speedDelayMillis - System.currentTimeMillis();
					}

					break;
				default:
					break;
				}
			}
		}
	}

	public static enum ExecutionMode {
		PAUSE("P"), NO_ANIMATION("NA"), FAST_FORWARD("FF"), NORMAL_SPEED("NS");

		final private String type;

		ExecutionMode(final String type) {
			this.type = type;
		}

		public String getString() {
			return type;
		}

		public static final ExecutionMode getByString(final String type) {
			for (final ExecutionMode executionMode : values()) {
				if (executionMode.type.equals(type))
					return executionMode;
			}
			throw new SimulationComponentsException("Unknown simulation mode: " + type);
		}
	}
}
