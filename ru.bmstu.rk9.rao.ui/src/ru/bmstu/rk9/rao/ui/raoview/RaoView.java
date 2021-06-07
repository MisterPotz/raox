package ru.bmstu.rk9.rao.ui.raoview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.ui.actions.IntroduceIndirectionAction;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorWrapper;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorManagerImpl;
import ru.bmstu.rk9.rao.ui.monitorview.ConditionalMenuItem;
import ru.bmstu.rk9.rao.ui.raoview.ViewManager.ViewType;

public abstract class RaoView extends ViewPart {
	
	public static String ID_PREFIX = "ru.bmstu.rk9.rao.ui.";
	
	protected SimulatorId simulatorId;
	private static int id = 0;
		
	protected void setSimulatorId(SimulatorId simulatorId) {
		assertNoSimulatorId(null);
		this.simulatorId = simulatorId;
		initializeSimulatorRelated();
	}
	
	private void assertNoSimulatorId(String reason) {
		if (simulatorId != null) {
			String stringMessage = "simulatorId must not present at view";
			if (reason != null) {
				stringMessage += " " + reason;
			}
			throw new IllegalStateException(stringMessage);
		}
	}
	
	private final void initialize(SimulatorId simulatorId, String viewFullName) {
		setPartName(viewFullName.substring(viewFullName.lastIndexOf('.') + 1, viewFullName.lastIndexOf('V')) + " " + simulatorId.toString());
		setSimulatorId(simulatorId);
	}
	
	protected abstract void initializeSimulatorRelated();
	
	protected void simNonNull(Action action) {
		if (simulatorId != null) {
			SimulatorWrapper wrapper = getSimulatorWrapper(simulatorId);
			 SimulatorActionArgs args = new SimulatorActionArgs(simulatorId, wrapper.getSimulator(), wrapper);
			 action.action(args);
		} else {
			logProblem("simulatorId is not set");
		}
	}
	
	private static SimulatorWrapper getSimulatorWrapper(SimulatorId simulatorId) {
		return SimulatorManagerImpl.getInstance().getSimulatorWrapper(simulatorId);
	}
	
	private void logProblem(String problem) {
		System.out.print("RaoView: " + this.toString() + "; " + "problem: " + problem);
	}
	
	private static class ConditionalMenuItemImpl extends ConditionalMenuItem {
		private final ViewType viewType;
		
		public ConditionalMenuItemImpl(ViewType viewType, TableViewer viewer, Menu parent) {
			super(viewer, parent, viewType.getId().substring(viewType.getId().lastIndexOf('.') + 1, viewType.getId().lastIndexOf('V')));
			this.viewType = viewType;
		}
		
		public String getId() {
			return viewType.getId();
		}

		@Override
		public boolean isEnabled(SimulatorId simulator) {
			return true;
		}
		
		@Override
		public void show(SimulatorId simulatorId) {
			try {
				RaoView newView = (RaoView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(getId(), simulatorId.toString(), IWorkbenchPage.VIEW_ACTIVATE);
				newView.initialize(simulatorId, getId());

			} catch (PartInitException e) {
				e.printStackTrace();
			}	
		}
		
	}
	
	public static ConditionalMenuItem createConditionalMenuItem(TableViewer viewer, Menu parent, ViewType viewType) {
		return new ConditionalMenuItemImpl(viewType, viewer, parent);
	}
	
	public static String getIdForView(Class<?> viewClass) {
		return ID_PREFIX + viewClass.getSimpleName();
	}
	
	protected interface Action {
		void action(SimulatorActionArgs args);
	}
	
	protected class SimulatorActionArgs {
		private final SimulatorId simulatorId;
		private final ISimulator simulator;
		private final SimulatorWrapper simulatorWrapper;
		
		public SimulatorActionArgs(SimulatorId simulatorId, ISimulator simulator, SimulatorWrapper simulatorWrapper) {
			super();
			this.simulatorId = simulatorId;
			this.simulator = simulator;
			this.simulatorWrapper = simulatorWrapper;
		}
		public SimulatorId getSimulatorId() {
			return simulatorId;
		}
		public ISimulator getSimulator() {
			return simulator;
		}
		public SimulatorWrapper getSimulatorWrapper() {
			return simulatorWrapper;
		}
	}
}
