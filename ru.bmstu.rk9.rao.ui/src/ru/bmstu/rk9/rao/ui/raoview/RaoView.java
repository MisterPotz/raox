package ru.bmstu.rk9.rao.ui.raoview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ru.bmstu.rk9.rao.lib.simulatormanager.SimulatorId;
import ru.bmstu.rk9.rao.ui.monitorview.ConditionalMenuItem;

public abstract class RaoView extends ViewPart {
	
	public static String ID_PREFIX = "ru.bmstu.rk9.rao.ui.";
	
	protected final static Map<SimulatorId , Integer> opened = new HashMap<>();
	protected SimulatorId currentWidget;
	private static int id = 0;
	
	public static Map<SimulatorId, Integer> getOpened() {
		return opened;
	}
	
	public static void addToOpened(final SimulatorId simulator, final int id) {
		opened.put(simulator, id);
	}
		
	private final void initialize(SimulatorId simulator, String viewFullName) {
		currentWidget = simulator;
		// TODO: set different names for different kinds of views: 
		// Results for Model 1 etc.
		setPartName(viewFullName.substring(viewFullName.lastIndexOf('.') + 1, viewFullName.lastIndexOf('V')) + " " + String.valueOf(simulator.getId()));
	}
	
	private static class ConditionalMenuItemImpl extends ConditionalMenuItem {
		private final String id;
		
		public ConditionalMenuItemImpl(String viewId, TableViewer viewer, Menu parent) {
			super(viewer, parent, viewId.substring(viewId.lastIndexOf('.') + 1, viewId.lastIndexOf('V')));
			id = viewId;
		}
		
		public String getId() {
			return id;
		}

		@Override
		public boolean isEnabled(SimulatorId simulator) {
			return true;
		}
		
		@Override
		public void show(SimulatorId simulator) {
			try {
				RaoView newView = (RaoView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(getId(), String.valueOf(RaoView.id), IWorkbenchPage.VIEW_CREATE);
				addToOpened(simulator, RaoView.id++);
				newView.initialize(simulator, id);

			} catch (PartInitException e) {
				e.printStackTrace();
			}	
		}
		
	}
	
	public static ConditionalMenuItem createConditionalMenuItem(TableViewer viewer, Menu parent, String viewId) {
		return new ConditionalMenuItemImpl(viewId, viewer, parent);
	}
	
	public static String getIdForView(Class<?> viewClass) {
		return ID_PREFIX + viewClass.getSimpleName();
	}
}
